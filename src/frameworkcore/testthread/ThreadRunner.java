package frameworkcore.testthread;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.openqa.selenium.WebDriverException;

import frameworkcore.FrameworkConstants;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.reportutilities.TimeCalculator;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ThreadRunner implements Runnable {

    private TestParameters testParameters;
    private String testStatus;

    private String startTime;
    private String finishTime;

    public ThreadRunner(TestParameters testParameters) {
        super();
        this.testParameters = testParameters;
    }

    @Override
    public void run() {

        startTime = getCurrentTimeInFormat();
        testStatus = startTestThread();
        FrameworkEntities.testThreadStatus.put(testParameters.getCurrentTestCaseOutput(), testStatus);

    }

    private String startTestThread() {

        TestThread testThread = null;

        try {
            testThread = new TestThread(testParameters);
            finishTime = getCurrentTimeInFormat();

            if (testThread.didExecutionFail()) {
                testStatus = "Failed after executing " + testThread.getNoOfStepsExecuted() + " steps in "
                        + getTimeElapsed() + "!";
            } else {
                testStatus = testThread.getNoOfStepsExecuted() + " steps executed successfully in " + getTimeElapsed()
                        + "!";
            }

        } catch (WebDriverException we) {
            finishTime = getCurrentTimeInFormat();
            testStatus = "Failed to start and connect to the browser!";
        } catch (Exception e) {
            finishTime = getCurrentTimeInFormat();
            testStatus = "Failed after executing " + testThread.getNoOfStepsExecuted() + " steps in " + getTimeElapsed()
                    + "!";
        }

        return testStatus;
    }

    private String getCurrentTimeInFormat() {
        DateFormat dateFormat = new SimpleDateFormat(FrameworkConstants.StandardFormats.TIME_FORMAT);
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    private String getTimeElapsed() {
        SimpleDateFormat timerDateFormat = new SimpleDateFormat(FrameworkConstants.StandardFormats.TIME_FORMAT);

        try {
            TimeCalculator time = new TimeCalculator();
            return time.getTimeDifference(timerDateFormat, startTime, finishTime);
        } catch (Exception e) {
            e.printStackTrace();
            String timeDifference = "";
            return timeDifference;
        }

    }

}
