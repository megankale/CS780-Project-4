import java.util.*;
import javax.jdo.*;

@javax.jdo.annotations.PersistenceCapable

public class Flight
{
	String airlineCompanyName;
	String flightNum; // { airlineCompanyName, flightNum } is a key
	Airport origin; 
	Airport destination;
	Time departTime;
	Time arriveTime;

	public String toString() {
		return airlineCompanyName+" "+flightNum+" "+
		       origin.name+" "+departTime.hour+":"+departTime.minute+" "+
		       destination.name+" "+arriveTime.hour+":"+arriveTime.minute ;
	}

	public static Flight find(String airlineCompanyName, String flightNum, PersistenceManager pm) {
		/* 
			Returns the flight that has the two parameter values; returns null if no such flight exists.
			 airlineCompanyName, flightNum  is assumed to be a key for Flight class.
            The function is applied to the database held by the persistence manager "pm".
        */

        Query q = pm.newQuery();
        q.setClass(Flight.class);
        q.declareParameters("String airlineCompanyName, String flightNum");
        q.setFilter("this.airlineCompanyName == airlineCompanyName && this.flightNum == flightNum");
		
		@SuppressWarnings("unchecked")
		Flight result = Utility.extract((Collection<Flight>) q.execute(airlineCompanyName, flightNum));
		
		return result;
	}

	public static Collection<Flight> getFlights(String a1, String a2, Query q) {
		/* 
			Given airport names a1 and a2, the function returns the collection of
	   		all flights departing from a1 and arriving to a2.
	   		Sort the result by (airlineCompanyName, flightNum). 
	   	*/

	   	q.setClass(Flight.class);
	   	q.declareParameters("String a1, String a2");
	   	q.setFilter("this.origin.name == a1 && this.destination.name == a2");
		q.setOrdering("airlineCompanyName ascending, flightNum ascending");

	   	@SuppressWarnings("unchecked")
		Collection<Flight> result = (Collection<Flight>) q.execute(a1, a2);

	   	return result;
	}

	public static Collection<Flight> getFlightsForCities(String c1, String c2, Query q) {
		/* 
			Given city names c1 and c2, the function returns the collection of all flights 
	   		departing from an airport close to c1 and arriving to an airport close to c2. 
	   		Sort the result by (airlineCompanyName, flightNum).
	   	*/

	   	q.setClass(Flight.class);
	   	q.declareParameters("String c1, String c2");
	   	q.setFilter("this.origin.closeTo.name == c1 && this.destination.closeTo.name == c2");
		q.setOrdering("airlineCompanyName ascending, flightNum ascending");

	   	@SuppressWarnings("unchecked")
		Collection<Flight> result = (Collection<Flight>) q.execute(c1, c2);

	   	return result;
	}

	public static Collection<Flight> getFlightsDepartTime(String a1, String a2, int h1, int m1, int h2, int m2, Query q) {
		/* 
			Given airport names a1 and a2 and times h1:m1 and h2:m2,
	   		the function returns the collection of all flights departing from a1 and arriving to a2
	   		satisfying the condition that the departure time is h1:m1 at earliest and h2:m2 at latest.
	   		Note that the time interval from h1:m1 to h2:m2 may include midnight.
	   		Sort the result by (airlineCompanyName, flightNum).
	   	*/

	   	q.setClass(Flight.class);
	   	q.declareParameters("String a1, String a2, int h1, int m1, int h2, int m2");
		q.setFilter("this.origin.name == a1 &&" + "this.destination.name == a2 &&" + "this.departTime.isInInterval(h1, m1, h2, m2)");
		q.setOrdering("airlineCompanyName ascending, flightNum ascending");

		Object[] args = new Object[] {a1, a2, h1, m1, h2, m2};

	   	@SuppressWarnings("unchecked")
		Collection<Flight> result = (Collection<Flight>) q.executeWithArray(args);

	   	return result;
	}

	@SuppressWarnings("unchecked")
	public static Collection<Object[]> getFlightsConnection(String a1, String a2, int h1, int m1, int h2, int m2, int connectionAtLeast, int connectionAtMost, Query q) {
		/* 
			Given airport names a1 and a2, times h1:m1 and h2:m2, and connection time lower and upper bounds in minutes,
	   		connectionAtLeast and connectionAtMost, the function returns the pairs of all flights f and f1 satisfying
	   		the following conditions:
	   		1. f departs from a1 and arrives to a connecting airport "ca" different from a2; and
	   		2. The departure time of f is h1:m1 at earliest and h2:m2 at latest; and
	   		3. There is a second flight f1 from "ca" to a2; and
	   		4. The connecting time, i.e. the time interval in minutes between 
	      	   the arrival time of f and the departure time of f1, is at least connectionAtLeast
	           and at most connectionAtMost. 
	   		Note again that the relevant time intervals may include midnight.
	   		Sort the result by (f.airlineCompanyName, f.flightNum, f1.airlineCompanyName, f1.flightNum).
	   	*/

	   	q.setClass(Flight.class);
		q.declareParameters("String a1, String a2, int h1, int m1, int h2, int m2, int connectionAtLeast, int connectionAtMost");
		q.declareVariables("Flight f; Flight f1; Airport ca");
		q.setFilter("f.origin.name == a1 && " +
					"f.destination.name == ca.name && " +
					"ca.name != f1.destination.name && " +
					"f.departTime.isInInterval(h1, m1, h2, m2) && " +
					"f1.origin.name = ca.name && " +
					"f1.destination.name == a2 && " +
					"f1.departTime.differenceFrom(f.arriveTime) >= connectionAtLeast && " +
					"f1.departTime.differenceFrom(f.arriveTime) <= connectionAtMost");
		q.setResult("distinct f, f1");
		q.setOrdering("f.airlineCompanyName ascending, f.flightNum ascending, f1.airlineCompanyName ascending, f1.flightNum ascending");

		Object[] args = {a1, a2, h1, m1, h2, m2, connectionAtLeast, connectionAtMost};

		@SuppressWarnings("rawtypes")
		Collection result = (Collection) q.executeWithArray(args);

		return result;
	}

	public static Collection<Object[]> groupByCompany(Query q) {
		/* 
			Group the flights by their airline company names.
	   		Then return the set of 2-tuples <airlineCompanyName: String, num: int> where:
				num = the total number of flights operated by airlineCompanyName
			Sort the result by airlineCompanyName.
		*/

		q.setClass(Flight.class);
		q.setGrouping("this.airlineCompanyName");
		q.setResult("this.airlineCompanyName, count(this)");
		q.setOrdering("this.airlineCompanyName ascending");

		@SuppressWarnings("unchecked")
		Collection<Object[]> result = (Collection<Object[]>) q.execute();

		return result;
	}
}