package frameworkcore.testthread;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import frameworkcore.FrameworkConstants;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.Main;
import frameworkcore.WebDriverFactory;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.errormanagers.TestException;
import frameworkcore.executors.ScriptExecutor;
import frameworkcore.reportutilities.LogWriter;
import frameworkcore.reportutilities.ReportingManager;
import frameworkcore.reportutilities.TestReporter;
import frameworkcore.reportutilities.TimeCalculator;
import frameworkcore.testdatamanagement.RandomDataCreator;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TestThread {

	protected ThreadEntities threadEntities;
	protected RemoteWebDriver driver;
	protected TestParameters testParameters;
	protected TestCaseVariables testCaseVariables;
	protected RandomDataCreator rdc;
	protected ReportingManager reportingManager;
	protected Properties properties = FrameworkPropertyFiles.frameworkProperties;
	protected Properties urlProperties = FrameworkPropertyFiles.urlProperties;

	private boolean exceptionCaught = false;
	private int logCharCounter = 0;
	private int stepsExecuted = 0;
	private String startTime = null;
	private String finishTime = null;
	private String logFilePath = null;
	private final SimpleDateFormat timerDateFormat = new SimpleDateFormat(
			FrameworkConstants.StandardFormats.TIME_FORMAT);
	private Process consoleDisplayProcess = null;

	public TestThread(TestParameters testParameters) {
		this.testParameters = testParameters;
		driveTestExecution();
	}

	public TestThread(ThreadEntities threadEntities) {
		this.threadEntities = threadEntities;
		driver = threadEntities.getDriver();
		testParameters = threadEntities.getTestParameters();
		testCaseVariables = threadEntities.getTestCaseVariables();
		rdc = threadEntities.getRandomDataCreator();
		reportingManager = threadEntities.getReportingManager();
	}

	private void driveTestExecution() {
		startReporter();
		try {
			setupThreadEntities();
			executeTestIterations();
		} catch (Exception e) {
			exceptionCaught = true;
			ErrorHandler.testError(ErrLvl.FATAL, e, "Fatal exception caught during Script Execution!!!",
					testCaseVariables);
		} finally {
			tearDown();
			writeAndCloseExcelReport();
		}
	}

	private void initializeLogWriter(String testCaseName) {

		if (TestSettings.writeTestConsoleToFile) {
			logFilePath = FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + testCaseName + ".log";

			System.out.println("Writing Test Case logs to:\n" + logFilePath);
			System.out.println();

			try {
				testCaseVariables.logWriter = new LogWriter(logFilePath, testCaseVariables);
			} catch (IOException e) {
				ErrorHandler.testError(ErrLvl.ERROR, e, "Unable to create Log File at: " + logFilePath,
						testCaseVariables);

				System.err.println("-- Moving logging to console --");
				TestSettings.writeTestConsoleToFile = false;
			}

		}
	}

	private void startReporter() {
		System.out.println();
		System.out.println("~ Starting thread preparation for Test Case: " + testParameters.getCurrentTestCaseOutput()
				+ " at " + Main.getCurrentFormattedTime("MM/dd/yyyy HH:mm:ss"));
		System.out.println();

		testCaseVariables = new TestCaseVariables();
		testCaseVariables.currentScript = testParameters.getCurrentTestScript();
		testCaseVariables.currentTestCase = testParameters.getCurrentTestCaseOutput();
		testCaseVariables.excelReporter = new TestReporter(testParameters);
		initializeLogWriter(testParameters.getCurrentTestCaseOutput());
	}

	private void writeAndCloseExcelReport() {
		testCaseVariables.excelReporter.finishReport();
		System.out.println("~ Finishing thread with Test Case: " + testParameters.getCurrentTestCaseOutput() + " at "
				+ Main.getCurrentFormattedTime("MM/dd/yyyy HH:mm:ss"));
		System.out.println();
		FrameworkEntities.testThreadVariables.put(testParameters.getCurrentTestCaseOutput(),
				testCaseVariables.globalTestVariables);
	}

	private void setupThreadEntities() {
		rdc = FrameworkEntities.dataGen;

		driver = Main.getWebDriverFromPool(testParameters.getBrowser(), testParameters.getCurrentTestCaseOutput());

		if (null == driver) {
			try {
				driver = WebDriverFactory.getDriver(testParameters.getBrowser(),
						testParameters.getCurrentTestCaseOutput());
			} catch (UnreachableBrowserException e) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, e,
						"Could not reach GRID Hub or Node! Check if GRID Hub is up and running and Nodes are registered to the GRID!");
			}

			Main.addWebDriverToPool(testParameters.getBrowser(), driver, testParameters.getCurrentTestCaseOutput());
		}

		if (null != testParameters.getDataPreLoad()) {
			testCaseVariables.globalTestVariables = testParameters.getDataPreLoad();

			System.out.println("\nTest Data preloaded:\n" + testCaseVariables.globalTestVariables);
		} else {
			testCaseVariables.globalTestVariables = new HashMap<String, String>();
		}

		ReportingManager reportingManager = new ReportingManager(testCaseVariables, driver, testParameters);

		threadEntities = new ThreadEntities(driver, testParameters, testCaseVariables, rdc, reportingManager);

		if (null != logFilePath) {
			if (!FrameworkPaths.tailfPath.isEmpty() && System.getProperty("os.name").toLowerCase().contains("win")) {
				try {
					String processString = "cmd.exe /c start \"" + TestSettings.currentEnvironment + " : "
							+ testParameters.getCurrentTestCaseOutput() + "\" " + FrameworkPaths.tailfPath
							+ " /cygdrive/c/" + logFilePath.replace("C:\\", "").replaceAll("\\\\", "/");

					consoleDisplayProcess = Runtime.getRuntime().exec(processString);

				} catch (Exception e1) {
					ErrorHandler.testError(ErrLvl.WARNING, e1, "Could not start tailf!", testCaseVariables);
				}
			}
		}
	}

	private void executeTestIterations() {
		System.out.println();
		System.out.println("~ Starting execution of Test Case: " + testParameters.getCurrentTestCaseOutput() + " at "
				+ Main.getCurrentFormattedTime("MM/dd/yyyy HH:mm:ss"));

		ScriptExecutor scriptExecutor = new ScriptExecutor(threadEntities);

		try {
			scriptExecutor.executeScript();
		} catch (TestException fx) {
			exceptionCaught = true;
		} catch (TimeoutException tx) {
			exceptionCaught = true;
			reportingManager.updateTestLogFatalWithException(tx, "Timed out!", true);
		} catch (Exception ex) {
			exceptionCaught = true;
			ErrorHandler.printException(ex, testCaseVariables);
		}
	}

	public boolean didExecutionFail() {
		return exceptionCaught;
	}

	public int getNoOfStepsExecuted() {
		return (stepsExecuted + 1);
	}

	private void tearDown() {
		System.out.println();

		if (TestSettings.writeTestConsoleToFile) {
			try {
				testCaseVariables.logWriter.flush();
				testCaseVariables.logWriter.close();
				testCaseVariables.logWriter = null;
			} catch (IOException e) {
				ErrorHandler.testError(ErrLvl.ERROR, e, "Failed to finalize writing to log file!", testCaseVariables);
			}
		}

		Main.freeUpWebDriverinPool(testParameters.getBrowser(), driver);

		stepsExecuted = testCaseVariables.currentStepNumber;

		if (null != consoleDisplayProcess) {
			try {
				String processString = "taskkill /F /FI \"WindowTitle eq " + TestSettings.currentEnvironment + " : "
						+ testParameters.getCurrentTestCaseOutput() + "\" /T";

				Runtime.getRuntime().exec(processString);
			} catch (Exception e1) {
				ErrorHandler.testError(ErrLvl.WARNING, e1, "Could not start tailf!", testCaseVariables);
			} finally {
				consoleDisplayProcess = null;
			}
		}
	}

	public void log(String str) {

		if (logCharCounter == 0) {
			str = "[LOG] " + str;
		}

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter.append(str);
		} else {
			System.out.print(str);
		}

		logCharCounter++;
	}

	public void logLine(String str) {

		logCharCounter = 0;

		if (!str.isEmpty()) {
			str = "[LOG] " + str + "\n";
		}

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter.append(str);
		} else {
			System.out.print(str);
		}
	}

	public void logError(String str) {

		if (logCharCounter == 0) {
			str = "[ERR] " + str;
		}

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter.append(str);
		} else {
			System.err.print(str);
		}

		logCharCounter++;
	}

	public void logErrorLine(String str) {

		logCharCounter = 0;

		if (!str.isEmpty()) {
			str = "[ERR] " + str + "\n";
		}

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter.append(str);
		} else {
			System.err.print(str);
		}
	}

	public void logLine() {
		logLine("");
	}

	public void logErrorLine() {
		logErrorLine("");
	}

	protected String getCurrentTime(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * Starts the timer.
	 */
	protected void startTimer() {

		Calendar calendar = Calendar.getInstance();
		startTime = timerDateFormat.format(calendar.getTime());
		finishTime = null;
	}

	/**
	 * Stops the timer, no time calculations are performed.
	 */
	protected void stopTimer() {
		Calendar calendar = Calendar.getInstance();
		if (null == finishTime) {
			finishTime = timerDateFormat.format(calendar.getTime());
		}
	}

	/**
	 * Stops the timer and returns formatted Time Difference. <br>
	 */
	protected String stopTimerAndReturnElapsedTime() {

		stopTimer();

		try {
			TimeCalculator time = new TimeCalculator();
			return time.getTimeDifference(timerDateFormat, startTime, finishTime);

		} catch (Exception e) {
			if (null == startTime) {
				startTime = "null";
			}
			ErrorHandler.testError(ErrLvl.ERROR, e,
					"Exception caught in stopTimer! Was timer started? Start Time: " + startTime, testCaseVariables);
			String timeDifference = "ERROR CAUGHT";
			startTime = null;
			finishTime = null;
			return timeDifference;
		}

	}

	public static void threadSleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			System.err.println("Interrupted!");
		}
	}
}
