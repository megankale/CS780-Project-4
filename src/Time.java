@javax.jdo.annotations.PersistenceCapable

public class Time
{
	// Represents a time in hour:minute. For example, 8:52 is 8:52 AM and 15:4 is 3:04 PM.

	int hour; // 0 -- 23
	int minute; // 0 -- 59

	public int differenceFrom(Time earlierTime) {
		/* 
			Returns the time difference in minutes between "earlierTime" and the target time object,
	   		with "earlierTime" always regarded as the earlier time. Note that the time interval may include midnight.
           	For example, suppose the target object represents 1:30. Then: 1.30 -> 1.30am
				if earlierTime = 23:30, the function will return 120 (minutes); 23.30 -> 11.30pm
           		if earlierTime = 0:30, the function will return 60 (minutes); 0.30 -> 12.30am
	   			if earlierTime = 9:30, the function will return 960 (minutes), etc. 9.30 -> 9.30pm
	   	*/

	   	int result = 0;
	   	int startingTime = (earlierTime.hour * 60) + earlierTime.minute;
	   	int endingTime = (this.hour * 60) + this.minute;

	   	if(endingTime - startingTime < 0) {
	   		result = (endingTime - startingTime) + 1440;
	   	} else {
	   		result = endingTime - startingTime;
	   	}

	   	return result;
	}

	public boolean isInInterval(int h1, int m1, int h2, int m2) {
		/* 
			Checks if the target time object is in the time interval from h1:m1 to h2:m2, inclusive,
	   		with h1:m1 always regarded as earlier than h2:m2. Note that the interval may include midnight.
	   	*/

	   	boolean result = false;

		int startingTime = (h1 * 60) + m1;
		int endingTime = (h2 * 60) + m2;
		int targetTime = (this.hour * 60) + this.minute;

		while(startingTime != endingTime) {
			if(startingTime == 1440) {
				startingTime = 0;
			} else {
				if(startingTime == targetTime) {
					result = true;
					break;
				}
			}

			startingTime += 1;
		}   	

	   	return result;
	}
}