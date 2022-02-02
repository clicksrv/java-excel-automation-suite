package frameworkcore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import frameworkcore.datablocks.Metadata;
import frameworkcore.reportutilities.MainReporter;
import frameworkcore.testdatamanagement.RandomDataCreator;
import frameworkcore.testthread.TestCaseVariables;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class FrameworkStatics {

	/**
	 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
	 */
	public static class FrameworkPropertyFiles {

		public static Properties frameworkProperties = new Properties();
		public static Properties devProperties = new Properties();
		public static Properties urlProperties = new Properties();
		public static Properties dbProperties = new Properties();
		public static Properties goAnywhereProperties = new Properties();
		public static Properties updateQueryProperties = new Properties();
		public static Properties ifmInboundProperties = new Properties();
		public static Properties ifmOutboundProperties = new Properties();

	}

	/**
	 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
	 */
	public static class FrameworkPaths {

		public static String runManagerPath;
		public static String libraryPath;
		public static String excelReportsPath;

		public static String screenshotPath;

		public static String preFinalReportPath;
		public static String finalReportPath;
		
		public static String tailfPath;
	}

	/**
	 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
	 */
	public static class FrameworkSettings {

		public static String projectName;
		public static Integer noOfThreads;
		public static boolean devMode = false;
		static String relativePath;

	}

	/**
	 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
	 */
	public static class TestSettings {

		public static boolean adaTestMode = false;
		public static boolean testVariableOverwriteAllowed = false;
		public static boolean writeTestConsoleToFile = false;
		public static boolean writeDebugToReport = false;
		public static boolean closeBrowserAtEndOfTest = true;
		public static boolean hideBrowser = false;
		public static boolean gridMode = false;
		public static boolean readBuildFromVersionFile = false;
		public static boolean fastExecutionMode = false;
		public static boolean waitForJSLoad = true;
		public static boolean createExtentWithSupportFiles = false;

		public static String currentEnvironment = null;
		public static String goAnywhereAccessMethod = null;
		public static String currentBuildNumber = null;
	}

	/**
	 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
	 */
	public static class FrameworkEntities {

		public static MainReporter excelReporter = null;
		public static File encryptionKeyFile = null;
		public static HashMap<String, String> testThreadStatus = new HashMap<String, String>();
		public static HashMap<String, HashMap<String, String>> testThreadVariables = new HashMap<String, HashMap<String, String>>();
		public final static String fileSeparator = System.getProperty("file.separator");
		public static RandomDataCreator dataGen = new RandomDataCreator();
		public static Metadata metadata = new Metadata();

	}

	/**
	 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
	 */
	public static class FrameworkTimeouts {

		public static int elementTimeOut = 15;
		public static int pageLoadTimeOut = 300;
		public static int processTimeout = 300;
		public static int batchJobTimeout = 900;

	}

	public static List<String> activeBatchJobStreams = Collections.synchronizedList(new ArrayList<String>());

	public static synchronized void markActiveBatchJobStream(TestCaseVariables testCaseVariables) {
		activeBatchJobStreams.add(testCaseVariables.currentPageOrBJStreamKeyword);
	}

	public static synchronized void unmarkActiveBatchJobStream(TestCaseVariables testCaseVariables) {
		activeBatchJobStreams.remove(testCaseVariables.currentPageOrBJStreamKeyword);
		synchronized (activeBatchJobStreams) {
			activeBatchJobStreams.notifyAll();
		}
	}

	public static String getRelativePath() {
		return FrameworkSettings.relativePath;
	}

	public static synchronized void setRelativePath(String relativePath) {
		FrameworkSettings.relativePath = relativePath;
	}

}
