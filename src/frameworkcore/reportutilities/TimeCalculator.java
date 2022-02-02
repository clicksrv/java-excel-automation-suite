package frameworkcore.reportutilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TimeCalculator {

    /**
     * Returns time difference in format: 2 days, 22 hours, 1 minute, 22 seconds,
     * 323 milliseconds
     * 
     * @param timerDateFormat - Framework's time format.
     * @param startTime       - Start time in the timerDateFormat.
     * @param finishTime      - Finish time in the timerDateFormat.
     * @return String Time Difference
     * @throws ParseException when format is not parsable in given format
     */
    public String getTimeDifference(SimpleDateFormat timerDateFormat, String startTime, String finishTime)
            throws ParseException {

        Date d1 = timerDateFormat.parse(startTime);
        Date d2 = timerDateFormat.parse(finishTime);

        long diff = d2.getTime() - d1.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long diffMilliseconds = diff - ((diffSeconds * 1000) + (diffMinutes * 1000 * 60) + (diffHours * 1000 * 60 * 60)
                + (diffDays * 1000 * 60 * 60 * 24));

        String daysDifference = diffDays + " day" + (diffDays > 1 ? "s" : "") + ", ";
        String hrsDifference = diffHours + " hour" + (diffHours > 1 ? "s" : "") + ", ";
        String minsDifference = diffMinutes + " minute" + (diffMinutes > 1 ? "s" : "") + ", ";
        String secsDifference = diffSeconds + " second" + (diffSeconds > 1 ? "s" : "") + ", ";
        String msecsDifference = diffMilliseconds + " millisecond" + (diffMilliseconds > 1 ? "s" : "");

        String timeDifference = "";

        if (diffDays > 0) {
            timeDifference = daysDifference;
        }

        if (!timeDifference.isEmpty()) {
            timeDifference = timeDifference + hrsDifference;
        } else if (diffHours > 0) {
            timeDifference = hrsDifference;
        }

        if (!timeDifference.isEmpty()) {
            timeDifference = timeDifference + minsDifference;
        } else if (diffMinutes > 0) {
            timeDifference = minsDifference;
        }

        if (!secsDifference.isEmpty()) {
            timeDifference = timeDifference + secsDifference;
        } else if (diffSeconds > 0) {
            timeDifference = secsDifference;
        }

        if (!msecsDifference.isEmpty()) {
            timeDifference = timeDifference + msecsDifference;
        } else {
            timeDifference = msecsDifference;
        }

        startTime = null;
        finishTime = null;

        return timeDifference;
    }

}
