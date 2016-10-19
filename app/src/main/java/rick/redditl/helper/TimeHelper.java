package rick.redditl.helper;

import java.util.concurrent.TimeUnit;

/**
 * Created by Rick on 2016-10-12.
 */
public class TimeHelper {


    /*
     *  input is a utc time in seconds
     *  returns a string in the structure 0 years/months/days/hours/minutes/seconds
     */
    public static String timeSincePost(long timeCreated) {
        //test
        //timeCreated = 1473916278000L;
        //Log.w("timeSincePost", Long.toString(timeCreated));

        //get current time
        long currentTime = System.currentTimeMillis()/1000;

        //get difference
        long timeDiff = currentTime - timeCreated;

        //greater than one year
        Long oneYear = 31556952L;
        if(timeDiff >= oneYear) {
            if(timeDiff < oneYear*2)
                return "1 year";
            else
                return Integer.toString(Math.round(timeDiff/oneYear)) + " years";
        }

        Long oneDay = TimeUnit.DAYS.toSeconds(1);
        //between one month to 11 month
        if(timeDiff >= oneDay*30) {
            if(timeDiff < oneDay*60)
                return "1 month";
            else
                return Integer.toString(Math.round(timeDiff/(oneDay*30))) + " months";
        }

        //between 1 day to 30 days
        if(timeDiff >= oneDay) {
            if(timeDiff < oneDay*2)
                return "1 day";
            else
                return Integer.toString(Math.round(timeDiff/(oneDay))) + " days";
        }

        Long oneHour = TimeUnit.HOURS.toSeconds(1);
        //between 1 hour to 24 hours
        if(timeDiff >= oneHour) {
            if(timeDiff < oneHour*2)
                return "1 hour";
            else
                return Integer.toString(Math.round(timeDiff/(oneHour))) + " hours";
        }

        Long oneMin = TimeUnit.MINUTES.toSeconds(1);
        //between 1 minute to 60 minutes
        if(timeDiff >= oneMin) {
            if(timeDiff < oneMin*2)
                return "1 minute";
            else
                return Integer.toString(Math.round(timeDiff/(oneMin))) + " minutes";
        }

        //between 1 minute to 60 minutes
        if(timeDiff <= 1)
            return Long.toString(timeDiff) + " second";
        else
            return Long.toString(timeDiff) + " seconds";




    }


}
