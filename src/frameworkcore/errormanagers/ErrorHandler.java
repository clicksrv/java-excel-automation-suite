package frameworkcore.errormanagers;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.testthread.TestCaseVariables;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ErrorHandler {

    public enum ErrLvl {
        WARNING("! WARNING: "), ERROR("! ERROR: "), FATAL("!!! FATAL ERROR: ");

        private String message;

        ErrLvl(String value) {
            message = value;
        }
    }

    /**
     * This method is used handle the framework errors.
     * 
     * @param errLvl - This parameter is of ErrLvl type
     * @param errorMsg - This parameter is of String data type.
     */

    public static synchronized void frameworkError(ErrLvl errLvl, Exception e, String errorMsg) {

        String plainErrorString = Jsoup.parse(errorMsg).text();
        String consoleErrorString = errLvl.message + plainErrorString;

        printErrorDemarcator(null, consoleErrorString.length());
        printErrorMessage(null, consoleErrorString);

        if (null != e) {

            String errorCause = "";
            String errorStack = "";

            StringWriter errorStackSWriter = null;

            errorStackSWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorStackSWriter));

            errorStack = errorStackSWriter.toString();

            if (!(null == e.getCause())) {
                errorCause = e.getCause().toString();
            }

            printStackTrace(null, errorStack);

            if (StringUtils.isNotBlank(errorCause)) {
                printCause(null, errorCause);
            }

            writeFrameworkExceptionToMainReport(plainErrorString, errorStack, errorCause);
        }

        printErrorDemarcator(null, consoleErrorString.length());

        logLine(null, "");

        if (errLvl.equals(ErrLvl.FATAL)) {
            if (null == e) {
                throw new TestException(plainErrorString);
            } else {
                throw new TestException(e.toString(), plainErrorString);
            }
        }

    }

    /**
     * This method is used handle the test errors.
     * 
     * @param errLvl - This parameter is of ErrLvl type
     * @param errorMsg - This parameter is of String data type.
     */

    public static synchronized void testError(ErrLvl errLvl, Exception e, String errorMsg,
            TestCaseVariables testCaseVariables) {

        String plainErrorString = Jsoup.parse(errorMsg).text();
        String consoleErrorString = errLvl.message + plainErrorString;

        printErrorDemarcator(testCaseVariables, consoleErrorString.length());
        printErrorMessage(testCaseVariables, consoleErrorString);

        Status status = null;

        if (errLvl.equals(ErrLvl.FATAL)) {
            status = Status.FATAL;
        } else if (errLvl.equals(ErrLvl.ERROR)) {
            status = Status.ERROR;
        } else if (errLvl.equals(ErrLvl.WARNING)) {
            status = Status.WARNING;
        }

        writeTestExceptionToTestReport(testCaseVariables, status, errorMsg);

        if (null != e) {

            String errorCause = "";
            String errorStack = "";

            StringWriter errorStackSWriter = null;

            errorStackSWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorStackSWriter));

            errorStack = errorStackSWriter.toString();

            if (!(null == e.getCause())) {
                errorCause = e.getCause().toString();
            }

            printStackTrace(testCaseVariables, errorStack);

            if (StringUtils.isNotBlank(errorCause)) {
                printCause(testCaseVariables, errorCause);
            }

            writeTestExceptionToMainReport(testCaseVariables,
                    "Failure occured during execution of Step: " + getCurrentScriptStep(testCaseVariables)
                            + "\nof Script: " + testCaseVariables.currentScript + "\n\n" + plainErrorString,
                    errorStack, errorCause);
        }

        printErrorDemarcator(testCaseVariables, consoleErrorString.length());

        logLine(testCaseVariables, "");

        if (errLvl.equals(ErrLvl.FATAL)) {
            if (null == e) {
                throw new TestException(plainErrorString);
            } else {
                throw new TestException(e, plainErrorString);
            }
        }

    }

    public static synchronized void printException(Exception e, TestCaseVariables testCaseVariables) {

        String consoleErrorString = "!!! FATAL UNHANDLED EXCEPTION\n\n";

        printErrorDemarcator(testCaseVariables, consoleErrorString.length() + 20);
        printErrorMessage(testCaseVariables, consoleErrorString);

        String errorCause = "";
        String errorStack = "";

        StringWriter errorStackSWriter = null;

        errorStackSWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(errorStackSWriter));

        errorStack = errorStackSWriter.toString();

        if (!(null == e.getCause())) {
            errorCause = e.getCause().toString();
        }

        printStackTrace(testCaseVariables, errorStack);

        if (StringUtils.isNotBlank(errorCause)) {
            printCause(testCaseVariables, errorCause);
        }

        printErrorDemarcator(testCaseVariables, consoleErrorString.length() + 20);

        writeTestExceptionToTestReport(testCaseVariables, Status.FATAL, consoleErrorString + "\n" + errorStack);

    }

    /**
     * This method is used to cause of the error.
     * 
     * @param cause - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */

    private static void printCause(TestCaseVariables testCaseVariables, String cause) {

        logLine(testCaseVariables, "");
        logLine(testCaseVariables, "Cause: ");
        logLine(testCaseVariables, cause);
    }

    /**
     * This method is used to strace the step causing the error.
     * 
     * @param errorStack - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */

    private static void printStackTrace(TestCaseVariables testCaseVariables, String errorStack) {

        logLine(testCaseVariables, "");
        logLine(testCaseVariables, "Stack Trace: ");
        logLine(testCaseVariables, errorStack);
    }

    /**
     * This method is used to print the error message.
     * 
     * @param errorMsg - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */

    private static void printErrorMessage(TestCaseVariables testCaseVariables, String errorMsg) {

        logLine(testCaseVariables, errorMsg);

        if (null == testCaseVariables) {
            logLine(testCaseVariables, "Framework caught an error!");
        } else {
            logLine(testCaseVariables, "\tFailure occured during execution of Step: "
                    + getCurrentScriptStep(testCaseVariables) + "\n\t\t of Script: " + testCaseVariables.currentScript);
        }

    }

    private static int getCurrentScriptStep(TestCaseVariables testCaseVariables) {
        return (testCaseVariables.currentStepNumber + 1);
    }

    /**
     * This method is used to print the error demarcator.
     * 
     * @param length - This parameter is of Intger data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */
    private static void printErrorDemarcator(TestCaseVariables testCaseVariables, int length) {
        syncWait();

        logLine(testCaseVariables, "");

        for (int i = 0; i < length; i++) {
            log(testCaseVariables, "-");
        }

        logLine(testCaseVariables, "");
        logLine(testCaseVariables, "");

    }

    /**
     * This method is used to write error in test report.
     * 
     * @param errorMsg - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */

    private static void writeTestExceptionToTestReport(TestCaseVariables testCaseVariables, Status status,
            String errorMsg) {
        testCaseVariables.excelReporter.createStep(status, errorMsg + "\nFailure occured during execution of Step: "
                + getCurrentScriptStep(testCaseVariables) + "\nof Script: " + testCaseVariables.currentScript);
    }

    /**
     * This method is used to write test exception to main report.
     * 
     * @param errorMsg - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     * @param errorStack - This parameter is of String data type.
     * @param errorCause - This parameter is of String data type.
     */

    private static synchronized void writeTestExceptionToMainReport(TestCaseVariables testCaseVariables,
            String errorMsg, String errorStack, String errorCause) {

        if (null == errorCause) {
            errorCause = "";
        }

        String source = "Test Case: " + testCaseVariables.currentTestCase;

        if (null != testCaseVariables.currentMethodKeyword) {
            source = source + "\n" + "Keyword: " + testCaseVariables.currentMethodKeyword;
        } else if (null != testCaseVariables.currentPageOrBJStreamKeyword) {
            if (StringUtils.contains(testCaseVariables.currentPageOrBJStreamKeyword, "BatchJob")) {
                source = source + "\n" + "Batch Job Stream: " + testCaseVariables.currentPageOrBJStreamKeyword;
            } else {
                source = source + "\n" + "Screen: " + testCaseVariables.currentPageOrBJStreamKeyword;
            }
        }

        FrameworkEntities.excelReporter.addThreadReportEntry(source, errorMsg, errorStack, errorCause);
    }

    /**
     * This method is used to write framework exception to main report.
     * 
     * @param errorMsg - This parameter is of String data type.
     * @param errorStack - This parameter is of String data type.
     * @param errorCause - This parameter is of String data type.
     */

    private static synchronized void writeFrameworkExceptionToMainReport(String errorMsg, String errorStack,
            String errorCause) {

        if (null == errorCause) {
            errorCause = "";
        }

        FrameworkEntities.excelReporter.addThreadReportEntry("Framework", errorMsg, errorStack, errorCause);
    }

    /**
     * @param str - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */
    private static synchronized void log(TestCaseVariables testCaseVariables, String str) {

        if (null == str) {
            str = "";
        }

        if (TestSettings.writeTestConsoleToFile && null != testCaseVariables) {
            testCaseVariables.logWriter.append(str);
        }

        System.err.print(str);
    }

    /**
     * This method is used to write into log file.
     * 
     * @param str - This parameter is of String data type.
     * @param testCaseVariables - This parameter is of
     *         TestCaseVariables type.
     */
    private static synchronized void logLine(TestCaseVariables testCaseVariables, String str) {

        if (null == str) {
            str = "";
        }

        if (TestSettings.writeTestConsoleToFile && null != testCaseVariables) {
            testCaseVariables.logWriter.append(str);
            testCaseVariables.logWriter.append("\n");
        }

        System.err.println(str);
    }

    private static void syncWait() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            System.err.println("Interrupted!");
        }
    }
}
