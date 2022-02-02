package frameworkcore;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import frameworkcore.FrameworkConstants.HTMLFormat;
import frameworkcore.FrameworkConstants.StandardFormats;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.FrameworkSettings;
import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.WebDriverFactory.Browsers;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.excelreaders.RunManagerReader;
import frameworkcore.reportutilities.CreateWordforScreenShots;
import frameworkcore.reportutilities.MainReporter;
import frameworkcore.reportutilities.ReportCombiner;
import frameworkcore.reportutilities.SendEmail;
import frameworkcore.reportutilities.TimeCalculator;
import frameworkcore.testthread.TestParameters;
import frameworkcore.testthread.ThreadRunner;
import frameworkextensions.applicationhandler.BuildNumberReader;

/**
 * Main class that will be invoked and will start execution of the framework.
 * This class reads values from the Run Manager and passes each Test Case to a
 * new Thread through ParallelRunner class and invokes the DriverScript class
 * for that test case with it's respective TestParameter.
 * 
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo@accenture.com);
 */
public class Main {

	private static String reportFolderName;

	private static RunModes currentRunMode;

	private static Boolean executableTestCaseFound = false;
	private static Boolean parallelExecution = false;

	private static RunManagerReader runManagerReader;

	private static File finalReport;

	private static String extentReportFile;

	private static ArrayList<String> suiteToExecute;
	private static ArrayList<TestParameters> testCases;

	private static HashMap<String, HashMap<String, String>> preloadHashMap = null;
	private static HashMap<Browsers, HashMap<RemoteWebDriver, String>> webDriverPool = new HashMap<>();

	private static DataFormatter formatter = new DataFormatter();

	private static String sendEmailFlag;
	private static String toEmailAddress;
	private static String ccEmailAddress;

	/**
	 * Initializing all entities for framework
	 */
	static {
		setRelativePath();
		loadPropertyFiles();
		setRunManagerPath();
	}

	/**
	 * Execution starting point of the framework.
	 * 
	 * @param args : args[0] : Execution Environment; args[1] : Report Generation
	 *             Path ; args[2] : Current Build Number; args[3+] : Suite Names
	 */
	public static void main(String args[]) {

		setRunMode(args);
		setRunSettings();
		setOtherPaths();
		prepareExcelReporter();
		getPreloadData();
		try {
			readTestSuiteConfigAndExecute();
		} catch (Throwable e) {
			Exception ex = new Exception(e);
			ErrorHandler.frameworkError(ErrLvl.FATAL, ex, "Fatal Exception caught in Main class!!!");
		} finally {
			if (executableTestCaseFound) {
				closeBrowserAndDrivers();
				printThreadStatus();
				saveTestCaseVariables();
				saveExcelReport();
				createWordDocumentOfScreenShots();
				consolidateReports();
				generateExtentReport();
				moveReportToReportFinalPath();
				sendEmail();
			}
			terminate();
		}
	}

	private static void closeBrowserAndDrivers() {

		RemoteWebDriver closeDriver;

		for (Browsers browser : Arrays.asList(Browsers.values())) {
			for (Entry<RemoteWebDriver, String> entry : webDriverPool.get(browser).entrySet()) {
				closeDriver = entry.getKey();

				// System.out.println("Closing " + browser.toString() + ": " +
				// closeDriver.toString());

				if (TestSettings.gridMode || TestSettings.closeBrowserAtEndOfTest) {
					closeDriver.quit();
				} else {
					closeDriver.manage().window().setPosition(new Point(0, 0));
				}
			}
		}

	}

	private static void createWordDocumentOfScreenShots() {
		if (currentRunMode.equals(RunModes.LOCAL)) {
			CreateWordforScreenShots.createScreenShotDocuments(FrameworkStatics.FrameworkPaths.preFinalReportPath);
		}
	}

	private static void sendEmail() {

		if (StringUtils.equalsIgnoreCase("LOCAL", currentRunMode.toString())) {
			if (StringUtils.equalsIgnoreCase(sendEmailFlag, "YES")) {
				File finalReportFile = null;
				finalReportFile = new File(
						FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator
								+ "ExcelReports" + FrameworkEntities.fileSeparator + "Report.xlsx");

				if (StringUtils.isNotEmpty(ccEmailAddress)) {

					SendEmail.sendMails(finalReportFile.getAbsolutePath(), toEmailAddress, ccEmailAddress);
				} else {
					ccEmailAddress = null;
					SendEmail.sendMails(finalReportFile.getAbsolutePath(), toEmailAddress, ccEmailAddress);
				}

			}
		}

	}

	/**
	 * This method is used to load the property files
	 */
	private static void loadPropertyFiles() {
		try {
			FrameworkStatics.FrameworkPropertyFiles.frameworkProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("framework-settings.properties")
							.toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, fe, "Framework Settings file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, ie, "Framework Settings could not be read!");
		}

		try {
			FrameworkStatics.FrameworkPropertyFiles.devProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("developer-settings.properties")
							.toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, fe, "Developer Settings file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, ie, "Developer Settings could not be read!");
		}

		FrameworkSettings.devMode = FrameworkStatics.FrameworkPropertyFiles.devProperties.getProperty("DeveloperMode")
				.equalsIgnoreCase("yes")
				|| FrameworkStatics.FrameworkPropertyFiles.devProperties.getProperty("DeveloperMode")
						.equalsIgnoreCase("on")
				|| FrameworkStatics.FrameworkPropertyFiles.devProperties.getProperty("DeveloperMode")
						.equalsIgnoreCase("true")
				|| FrameworkStatics.FrameworkPropertyFiles.devProperties.getProperty("DeveloperMode")
						.equalsIgnoreCase("1");

		try {
			FrameworkStatics.FrameworkPropertyFiles.urlProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("application-url-config.properties")
							.toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, fe, "Application URL Config Settings file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, ie, "Application URL Config Settings could not be read!");
		}

		try {
			FrameworkStatics.FrameworkPropertyFiles.dbProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("db-config.properties").toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, fe, "DB Config file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, ie, "DB Config could not be read!");
		}

		try {
			FrameworkStatics.FrameworkPropertyFiles.goAnywhereProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("goanywhere-config.properties")
							.toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, fe, "GoAnywhere Config file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, ie, "GoAnywhere Config could not be read!");
		}

		try {
			FrameworkStatics.FrameworkPropertyFiles.updateQueryProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("update-queries.properties").toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, fe, "Update Queries Properties file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, ie, "Update Queries Properties could not be read!");
		}

		try {
			FrameworkStatics.FrameworkPropertyFiles.ifmInboundProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("batch-ifm-inbound.properties")
							.toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, fe, "Batch IFM Inbound Properties file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, ie, "Batch IFM Inbound Properties could not be read!");
		}

		try {
			FrameworkStatics.FrameworkPropertyFiles.ifmOutboundProperties
					.load(new FileInputStream((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
							.append(FrameworkEntities.fileSeparator).append("config")
							.append(FrameworkEntities.fileSeparator).append("batch-ifm-outbound.properties")
							.toString()));
		} catch (FileNotFoundException fe) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, fe, "Batch IFM Outbound Properties file not found!");
		} catch (IOException ie) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, ie, "Batch IFM Outbound Properties could not be read!");
		}

	}

	/**
	 * This method is used to get all the pre loaded data
	 */
	private static void getPreloadData() {

		Gson gson = new Gson();

		String jsonFilePath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + "preload"
				+ FrameworkEntities.fileSeparator + "GlobalTestData.json";

		File jsonFile = new File(jsonFilePath);

		if (jsonFile.exists()) {

			String jsonContent = null;

			try {
				jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
			} catch (IOException e) {
				ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to read file at: " + jsonFile.getAbsolutePath());
			}

			Type type = new TypeToken<HashMap<String, HashMap<String, String>>>() {
			}.getType();
			preloadHashMap = gson.fromJson(jsonContent, type);
		}

	}

	private static void terminate() {
		System.out.println();
		System.out.println("~ Finished ~");

		System.exit(0);
	}

	private static void consolidateReports() {

		System.out.println();
		System.out.println("Processing Test Reports");

		ReportCombiner reportCombiner = new ReportCombiner();
		reportCombiner.combineTestReports(FrameworkStatics.FrameworkPaths.excelReportsPath);

		finalReport = new File(FrameworkPaths.excelReportsPath + "Report.xlsx");

		boolean reportCreated = false;
		boolean reportWriteable = false;
		do {
			reportCreated = finalReport.isFile();
		} while (!reportCreated);

		do {
			reportWriteable = finalReport.canWrite();
		} while (!reportWriteable);

		File finalReportTemp = new File(FrameworkPaths.excelReportsPath + "~$Report.xlsx");

		boolean tempFileExists = true;

		do {
			tempFileExists = finalReportTemp.isFile();
		} while (tempFileExists);

		File folder = new File(FrameworkPaths.excelReportsPath);
		File[] fileList = folder.listFiles();

		for (File file : fileList) {
			if (!file.getName().equals("Report.xlsx")) {
				file.delete();
			}
		}
	}

	private static void setRunSettings() {

		getSettingsFromRunManager();

		if (FrameworkSettings.devMode) {
			getSettingsFromProperties(FrameworkStatics.FrameworkPropertyFiles.devProperties);
		} else {
			getSettingsFromProperties(FrameworkStatics.FrameworkPropertyFiles.frameworkProperties);
		}

		if (TestSettings.currentBuildNumber.equalsIgnoreCase("auto")) {
			TestSettings.currentBuildNumber = BuildNumberReader.readVersionFile();
		}

		printRunSettings();

	}

	private static void setRunMode(String[] args) {

		if (args.length == 0) {
			currentRunMode = RunModes.LOCAL;

		} else if (args.length == 3) {
			currentRunMode = RunModes.TESTS_WITH_YES;

			TestSettings.currentEnvironment = args[0];
			FrameworkPaths.finalReportPath = args[1];
			TestSettings.currentBuildNumber = args[2];

		} else if (args.length > 3) {
			currentRunMode = RunModes.TEST_SUITES_FROM_ARGS;

			TestSettings.currentEnvironment = args[0];
			FrameworkPaths.finalReportPath = args[1];
			TestSettings.currentBuildNumber = args[2];

			suiteToExecute = new ArrayList<>();
			for (int argNo = 3; argNo < args.length; argNo++) {
				suiteToExecute.add(args[argNo]);
			}

		} else {
			ErrorHandler.frameworkError(ErrLvl.ERROR, null, "Unsupported Run Mode!");
		}
	}

	private static void printRunSettings() {

		if (FrameworkSettings.devMode) {
			System.out.println("Development Mode\tON");
		}

		System.out.println("Current Run Mode: \t\t" + currentRunMode);
		System.out.println("Current Run Environment: \t" + TestSettings.currentEnvironment.toUpperCase());
		if (!TestSettings.currentBuildNumber.equals("0.0.0")) {
			System.out.println("Current Build Number: \t\t" + TestSettings.currentBuildNumber);
		}
		if (currentRunMode.equals(RunModes.TEST_SUITES_FROM_ARGS)) {
			System.out.println("Suites selected: \t\t");
			for (String suite : suiteToExecute) {
				System.out.print("\t\t" + suite.toUpperCase() + "\n");
			}
		}

		if (TestSettings.fastExecutionMode) {
			System.out.println();
			System.err
					.println("IMP: Fast Execution Mode is on, full screenshots will not be taken unless a step fails!");
		}

		System.out.println();
	}

	private static void getSettingsFromProperties(Properties prop) {

		FrameworkSettings.projectName = getValueFromPropertyFile(FrameworkPropertyFiles.frameworkProperties,
				"ProjectName");
		FrameworkPaths.tailfPath = getValueFromPropertyFile(FrameworkPropertyFiles.frameworkProperties,
				"TailfPath");
		TestSettings.waitForJSLoad = Boolean.parseBoolean(
				getValueFromPropertyFile(FrameworkPropertyFiles.frameworkProperties, "AllowSetGlobalToOverwrite"));
		TestSettings.createExtentWithSupportFiles = Boolean.parseBoolean(
				getValueFromPropertyFile(FrameworkPropertyFiles.frameworkProperties, "CreateExtentWithSupportFiles"));
		TestSettings.createExtentWithSupportFiles = Boolean.parseBoolean(
				getValueFromPropertyFile(FrameworkPropertyFiles.frameworkProperties, "CreateExtentWithSupportFiles"));

		TestSettings.writeDebugToReport = Boolean.parseBoolean(getValueFromPropertyFile(prop, "WriteDebugToReport"));

		if (TestSettings.gridMode || parallelExecution) {
			TestSettings.writeTestConsoleToFile = true;
		} else {
			TestSettings.writeTestConsoleToFile = Boolean
					.parseBoolean(getValueFromPropertyFile(prop, "TestConsoleToFile"));
			if (FrameworkSettings.devMode) {
				TestSettings.closeBrowserAtEndOfTest = Boolean
						.parseBoolean(getValueFromPropertyFile(prop, "CloseBrowserAtEndOfTest"));
				TestSettings.hideBrowser = Boolean.parseBoolean(getValueFromPropertyFile(prop, "HideBrowserDuringRun"));
				TestSettings.fastExecutionMode = Boolean
						.parseBoolean(getValueFromPropertyFile(prop, "FastExecutionMode"));
			}
		}

		TestSettings.testVariableOverwriteAllowed = Boolean
				.parseBoolean(getValueFromPropertyFile(prop, "AllowSetGlobalToOverwrite"));
		FrameworkTimeouts.elementTimeOut = Integer.parseInt(getValueFromPropertyFile(prop, "ElementLoadTimeout"));
		FrameworkTimeouts.pageLoadTimeOut = Integer.parseInt(getValueFromPropertyFile(prop, "PageLoadTimeout"));
		FrameworkTimeouts.processTimeout = Integer.parseInt(getValueFromPropertyFile(prop, "ProcessTimeout"));
		FrameworkTimeouts.batchJobTimeout = Integer.parseInt(getValueFromPropertyFile(prop, "BatchJobTimeout"));
		TestSettings.goAnywhereAccessMethod = getValueFromPropertyFile(prop, "GoAnywhereAccessMethod");

	}

	private static String getValueFromPropertyFile(Properties prop, String key) {
		String value = prop.getProperty(key);

		if (null == value) {
			if (FrameworkSettings.devMode) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, null,
						"'" + key + "' was not found in developer-settings.properties! Please take an update!");
			} else {
				ErrorHandler.frameworkError(ErrLvl.FATAL, null,
						"'" + key + "' was not found in framework-settings.properties! Please take an update!");
			}
		}

		return value;
	}

	private static void printThreadStatus() {

		System.out.println();
		int counter = 0;

		for (Entry<String, String> thread : FrameworkEntities.testThreadStatus.entrySet()) {
			counter++;
			if (thread.getValue().contains("Failed")) {
				System.err.println("Thread " + counter + "> " + thread.getKey() + ": " + thread.getValue());
			} else {
				System.out.println("Thread " + counter + "> " + thread.getKey() + ": " + thread.getValue());
			}
		}
	}

	private static void saveTestCaseVariables() {

		Gson gson = new Gson();

		Map<String, TreeMap<String, String>> outputJSON = new HashMap<>();

		for (Entry<String, HashMap<String, String>> testCaseVariables : FrameworkEntities.testThreadVariables
				.entrySet()) {
			TreeMap<String, String> sortedMap = new TreeMap<String, String>(testCaseVariables.getValue());
			outputJSON.put(testCaseVariables.getKey(), sortedMap);
		}

		String json = gson.toJson(outputJSON);

		String jsonFilePath = FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator
				+ "GlobalTestData.json";

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(jsonFilePath));
			writer.write(json);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Unable to write Global Test Data File at: " + jsonFilePath);
		}

	}

	private static void generateExtentReport() {

		ExtentHtmlReporter reporter;

		extentReportFile = FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "Report.html";

		XSSFWorkbook workbook = null;
		OPCPackage pkg = null;
		try {
			pkg = OPCPackage.open(finalReport, PackageAccess.READ);
			workbook = new XSSFWorkbook(pkg);
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, e,
					"Unable to open Final Excel Report at " + finalReport.getAbsolutePath());
		} catch (InvalidFormatException e) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, e, finalReport.getAbsolutePath() + " is of Invalid File Format!");
		}

		reporter = new ExtentHtmlReporter(extentReportFile);
		reporter.config().setDocumentTitle("Automation Test Report");
		reporter.config().setReportName("Automation Test Report - " + TestSettings.currentEnvironment + " (Build: "
				+ TestSettings.currentBuildNumber + ")");
		reporter.config().setTheme(Theme.DARK);
		reporter.config().enableTimeline(false);

		XSSFSheet sheet = null;

		ExtentReports reports = new ExtentReports();

		reports.attachReporter(reporter);

		sheet = workbook.getSheet("Tests");

		String currentTest = "";
		String currentNode = "";

		ExtentTest test = null;
		ExtentTest step = null;

		HashMap<String, String> testCaseToTotalDuration = new HashMap<>();

		int readingRow = 1;
		try {
			while (!sheet.getRow(readingRow).getCell(1).getStringCellValue().isEmpty()) {

				String testCase = sheet.getRow(readingRow).getCell(1).getStringCellValue();
				if (!StringUtils.equals(testCase, currentTest)) {
					test = reports.createTest(testCase);
				}
				currentTest = testCase;

				String node = sheet.getRow(readingRow).getCell(3).getStringCellValue();
				if (!StringUtils.equals(node, currentNode)) {
					step = test.createNode(node);
				}
				currentNode = node;

				String timestamp = sheet.getRow(readingRow).getCell(2).getStringCellValue().substring(1);
				Status status = Status.valueOf(sheet.getRow(readingRow).getCell(4).getStringCellValue());
				String activity = sheet.getRow(readingRow).getCell(5).getStringCellValue();
				String screenshotPath = "";
				try {
					screenshotPath = sheet.getRow(readingRow).getCell(6).getStringCellValue();
				} catch (Exception e) {
					screenshotPath = "";
				}

				if (screenshotPath.isEmpty()) {
					step.log(status, activity + "<br><i>" + HTMLFormat.STEP + timestamp + HTMLFormat.CLOSE + "</i>");
				} else {
					try {
						step.log(status, activity + "<br><i>" + HTMLFormat.STEP + timestamp + HTMLFormat.CLOSE + "</i>",
								MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
					} catch (IOException e) {
						ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Adding screenshots to extent report failed!");
					}
				}
				readingRow = readingRow + 1;
			}
		} catch (Exception e) {
			// Do not perform anything, Exception is caught for Empty Cells
			// indicating end of file.
		}

		try {
			sheet = workbook.getSheet("Summary");

			readingRow = 3;

			while (!sheet.getRow(readingRow).getCell(2).getStringCellValue().isEmpty()) {

				String testCase = sheet.getRow(readingRow).getCell(2).getStringCellValue();
				String totalTime = sheet.getRow(readingRow).getCell(5).getStringCellValue();
				testCaseToTotalDuration.put(testCase, totalTime.substring(1));

				readingRow = readingRow + 1;
			}
		} catch (Exception e) {
			// Do not perform anything, Exception is caught for Empty Cells
			// indicating end of file.
		} finally {
			reports.flush();
			try {
				if (null != workbook) {
					workbook.close();
				}
				if (null != pkg) {
					pkg.close();
				}
			} catch (IOException e) {
				ErrorHandler.frameworkError(ErrLvl.ERROR, e,
						"Unable to close file at: " + finalReport.getAbsolutePath());
			}
		}

		if (readingRow > 1) {
			File input = new File(extentReportFile);

			System.out.println();

			Document doc = null;
			try {
				Thread.sleep(1000);
				doc = Jsoup.parse(input, "UTF-8", "");
			} catch (IOException e) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Unable to parse Report HTML as UTF-8 Format");
			} catch (InterruptedException e) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Sleep Interrupted!");
			}

			if (TestSettings.createExtentWithSupportFiles) {
				copyExtentReportSupportFiles();

				Elements link = doc.getElementsByTag("link");
				Elements script = doc.getElementsByTag("script");
				Elements img = doc.getElementsByTag("img");

				script.get(3).attr("src", "lib/extent.js");

				link.get(0).attr("href", "lib/textstyle.css");
				link.get(1).attr("href", "lib/icons.css");
				link.get(2).attr("href", "lib/extent.css");

				img.get(0).attr("src", "lib/data/logo.png");
			}

			Elements div = doc.getElementsByTag("div");
			Elements span = doc.getElementsByTag("span");
			Elements td = doc.getElementsByTag("td");

			String testCaseName = null;

			for (Element var : td) {
				if (var.hasClass("timestamp")) {
					Element tsVar = var.nextElementSibling().getElementsByTag("i").get(0);
					String timestamp = tsVar.text().substring(1);
					tsVar.remove();
					var.text(timestamp);
				}
			}

			TimeCalculator calculator = new TimeCalculator();
			
			for (Element var : div) {
				if (var.hasClass("collapsible-header") || var.hasClass("collapsible-header active")) {
					Elements bodyTd = var.nextElementSibling().getElementsByTag("td");
					List<Element> timestampElements = bodyTd.stream().filter(element -> element.hasClass("timestamp"))
							.collect(Collectors.toList());

					Elements headSpan = var.getElementsByTag("span");
					
					for (Element element2 : headSpan) {
						if (element2.hasClass("node-time")) {
							element2.text(timestampElements.get(0).text() + " to " + timestampElements.get(timestampElements.size() - 1).text());
						} else if (element2.hasClass("node-duration")) {
							try {
								element2.text(calculator.getTimeDifference(StandardFormats.defaultTimeFormat, timestampElements.get(0).text(), timestampElements.get(timestampElements.size() - 1).text()));
							} catch (ParseException e) {
								element2.text("Failed to parse time format");
							}
						} 
					}
				}
			}
			
			for (Element var : span) {
				if (var.hasClass("test-name")) {
					testCaseName = var.text();
				} else if (var.hasClass("test-time")) {
					String textToSet = testCaseToTotalDuration.get(testCaseName);
					var.text(textToSet);
				} else if (var.hasClass("label start-time")) {
					var.remove();
				} else if (var.hasClass("label end-time")) {
					var.remove();
				} else if (var.hasClass("label time-taken grey lighten-1 white-text")) {
					var.remove();
				}
			}
			
			final File file = new File(extentReportFile);
			try {
				FileUtils.writeStringToFile(file, doc.outerHtml(), "UTF-8");
			} catch (IOException e) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Unable to write to Report HTML with UTF-8 Format");
			}

			File finalReportTemp = new File(FrameworkPaths.excelReportsPath + "~$Report.xlsx");

			boolean tempFileExists = true;

			do {
				tempFileExists = finalReportTemp.isFile();
			} while (tempFileExists);
		} else

		{
			System.err.println("!!! No test steps were saved/executed !!!");
		}

	}

	private static void ifReqdPrintNoTestsToRunMsg() {
		if (!executableTestCaseFound) {
			String errMsg = "!!! No Test Cases are set to 'YES' in 'Run Manager' !!!";

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.err.println("Interrupted!");
			}
			System.err.println();
			for (int i = 0; i < errMsg.length(); i++) {
				System.err.print("!");
			}
			System.err.println();

			System.err.println(errMsg);

			for (int i = 0; i < errMsg.length(); i++) {
				System.err.print("!");
			}
			System.err.println();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.err.println("Interrupted!");
			}
		}
	}

	public static synchronized void addWebDriverToPool(Browsers browser, RemoteWebDriver driver, String testCaseName) {
		synchronized (webDriverPool) {
			// System.out.println("DEBUG: Registering driver in driver pool for '"
			// + testCaseName + "'");
			webDriverPool.get(browser).put(driver, testCaseName);
		}
	}

	public static synchronized void freeUpWebDriverinPool(Browsers browser, RemoteWebDriver driver) {
		synchronized (webDriverPool) {
			// System.out.println("DEBUG: Freeing up driver: " +
			// driver.toString());
			webDriverPool.get(browser).put(driver, null);
		}
	}

	public static synchronized RemoteWebDriver getWebDriverFromPool(Browsers browser, String testCaseName) {
		synchronized (webDriverPool) {
			// System.out.println("DEBUG: Fetching driver from driver pool for '"
			// + testCaseName + "'");

			for (Entry<RemoteWebDriver, String> entry : webDriverPool.get(browser).entrySet()) {
				// System.out.println("DEBUG: Checking drivers for " +
				// browser.toString() + ": "
				// + entry.getValue() + ": " + entry.getKey().toString());
				if (null == entry.getValue()) {
					// System.out.println("DEBUG: Using driver for " +
					// testCaseName + ": "
					// + entry.getKey().toString());
					entry.setValue(testCaseName);
					RemoteWebDriver driver = entry.getKey();
					driver.manage().deleteAllCookies();

					try {
						WebElement logoutLink = driver.findElement(
								By.xpath(FrameworkPropertyFiles.frameworkProperties.getProperty("LogOutElementXpath")));
						if (logoutLink.isDisplayed()) {
							logoutLink.click();
						}
					} catch (NoSuchElementException e) {
					}

					return driver;
				}
			}

			// System.out.println("DEBUG: No existing " + browser.toString() +
			// " driver in pool for '"
			// + testCaseName + "'");
			return null;
		}
	}

	private static void readTestSuiteConfigAndExecute() {

		for (Browsers browser : Arrays.asList(Browsers.values())) {
			webDriverPool.put(browser, new HashMap<RemoteWebDriver, String>());
		}

		runManagerReader = new RunManagerReader(FrameworkPaths.runManagerPath, null);
		XSSFWorkbook runMgrWb = runManagerReader.getWorkbook();

		testCases = new ArrayList<TestParameters>();

		if (currentRunMode.equals(RunModes.TEST_SUITES_FROM_ARGS)) {

			for (String suite : suiteToExecute) {
				String sheetName = "Suite_" + suite;
				System.out.println("Reading Test Suite from Run Manager: " + sheetName);
				readSuiteAndAddTestCases(sheetName);
			}
			executeTests(testCases);

		} else {
			Iterator<Sheet> sheetIterator = runMgrWb.iterator();

			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();

				if (sheet.getSheetName().equals("RunConfig") || sheet.getSheetName().equals("GridConfig")) {
					continue;
				}

				if (sheet.getSheetName().toUpperCase().startsWith("SUITE_")) {
					System.out.println("Reading Test Suite from Run Manager: " + sheet.getSheetName());
					readSuiteAndAddTestCases(sheet.getSheetName());
				}
			}
			executeTests(testCases);
		}

		ifReqdPrintNoTestsToRunMsg();

	}

	private static void readSuiteAndAddTestCases(String suiteSheetName) {
		runManagerReader.setDatasheetName(suiteSheetName);

		int lastTestCaseRow = runManagerReader.getLastRowNum();
		int warningRow = Integer.parseInt(
				FrameworkStatics.FrameworkPropertyFiles.frameworkProperties.getProperty("RunManagerRowWarningLimit"));

		if (lastTestCaseRow > warningRow) {
			ErrorHandler.frameworkError(ErrLvl.WARNING, null,
					"Last Row found is beyond " + warningRow + " in Run Manager Sheet: " + suiteSheetName);
		}

		for (int readingRowNumber = 1; readingRowNumber <= lastTestCaseRow; readingRowNumber++) {
			runManagerReader.setDatasheetName(suiteSheetName);

			String executeFlag = runManagerReader.getValue(readingRowNumber, "Execute");

			if (executeFlag.equalsIgnoreCase("Yes")) {

				executableTestCaseFound = true;

				runManagerReader.setDatasheetName(suiteSheetName);
				TestParameters testParameters = null;

				int iterations = 1;
				try {
					if (Integer.parseInt(runManagerReader.getValue(readingRowNumber, "Iterations")) > 1) {
						iterations = Integer.parseInt(runManagerReader.getValue(readingRowNumber, "Iterations"));
					}

				} catch (Exception e) {
				}

				for (int i = 1; i <= iterations; i++) {
					testParameters = new TestParameters();

					testParameters
							.setCurrentTestSuite(suiteSheetName.substring("SUITE_".length(), suiteSheetName.length()));
					if (iterations > 1) {
						testParameters.setCurrentTestCaseOutput(testParameters.getCurrentTestSuite() + "_"
								+ runManagerReader.getValue(readingRowNumber, "TC_ID") + "_i"
								+ String.format("%03d", i));
					} else {
						testParameters.setCurrentTestCaseOutput(testParameters.getCurrentTestSuite() + "_"
								+ runManagerReader.getValue(readingRowNumber, "TC_ID"));
					}

					getScriptMetadata(testParameters, readingRowNumber);

					testCases.add(testParameters);
				}

				System.out.println("\t Test Script to be executed:\t " + testParameters.getCurrentTestScript());
				if (iterations > 1) {
					System.out.println("\t\t No. of Iterations: " + iterations);
				}

			}
		}
	}

	private static void getScriptMetadata(TestParameters testParameters, int readingRowNumber) {

		testParameters.setCurrentTestDescription(runManagerReader.getValue(readingRowNumber, "Description"));
		testParameters.setCurrentAuthor(runManagerReader.getValue(readingRowNumber, "Author"));
		testParameters.setBrowser(runManagerReader.getValue(readingRowNumber, "Browser"));
		testParameters.setCurrentTestScript(runManagerReader.getValue(readingRowNumber, "TC_ID"));

		int startStep = 2;
		int endStep = 0;

		try {
			startStep = Integer.parseInt(runManagerReader.getValue(readingRowNumber, "Start Step"));
		} catch (Exception e) {
		}
		try {
			endStep = Integer.parseInt(runManagerReader.getValue(readingRowNumber, "End Step"));
		} catch (Exception e) {
		}

		testParameters.setStartStep(startStep);
		testParameters.setEndStep(endStep);

		if ((null != preloadHashMap) && (preloadHashMap.containsKey(testParameters.getCurrentTestCaseOutput()))) {
			testParameters.setDataPreLoad(preloadHashMap.get(testParameters.getCurrentTestCaseOutput()));
		}
	}

	private static void executeTests(ArrayList<TestParameters> testCases) {

		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat(TestSettings.currentEnvironment.toLowerCase() + "-test-%d").build();

		ExecutorService parallelExecutor = Executors.newFixedThreadPool(FrameworkSettings.noOfThreads, threadFactory);

		for (int currentTestNo = 0; currentTestNo < testCases.size(); currentTestNo++) {

			ThreadRunner thread = new ThreadRunner(testCases.get(currentTestNo));
			parallelExecutor.execute(thread);

		}

		if (currentRunMode.equals(RunModes.LOCAL)) {
			parallelExecutor.shutdown();
		} else {
			parallelExecutor.shutdown();
		}

		while (!parallelExecutor.isTerminated()) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Interrupted while waiting for Test Threads to complete!");
			}
		}
	}

	public static String getCurrentFormattedTime(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}

	private static void prepareExcelReporter() {
		FrameworkEntities.excelReporter = new MainReporter();
	}

	private static void copyExtentReportSupportFiles() {
		File srcDir = new File(System.getProperty("user.dir") + FrameworkEntities.fileSeparator + "lib"
				+ FrameworkEntities.fileSeparator + "extent");
		File destDir = new File(FrameworkPaths.preFinalReportPath);
		try {
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, e,
					"Failed to copy Extent Report support files to Report Destination.");
		}

		unzip(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "extent.zip",
				FrameworkPaths.preFinalReportPath);

		File zipFile = new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "extent.zip");

		do {
		} while (!zipFile.canWrite());

		zipFile.delete();
	}

	private static void unzip(String zipFilePath, String destDir) {
		try (ZipFile file = new ZipFile(zipFilePath)) {
			FileSystem fileSystem = FileSystems.getDefault();
			// Get file entries
			Enumeration<? extends ZipEntry> entries = file.entries();

			// We will unzip files in this folder
			// Files.createDirectory(fileSystem.getPath(destDir));

			// Iterate over entries
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				// If directory then create a new directory in uncompressed folder
				if (entry.isDirectory()) {
					Files.createDirectories(
							fileSystem.getPath(destDir + FrameworkEntities.fileSeparator + entry.getName()));
				}
				// Else create the file
				else {
					InputStream is = file.getInputStream(entry);
					BufferedInputStream bis = new BufferedInputStream(is);
					String uncompressedFileName = destDir + FrameworkEntities.fileSeparator + entry.getName();
					Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
					Files.createFile(uncompressedFilePath);
					FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
					while (bis.available() > 0) {
						fileOutput.write(bis.read());
					}
					fileOutput.close();
				}
			}
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Failed to extract extent support files from extent.zip!");
		}

	}

	private static void moveReportToReportFinalPath() {
		File srcDir;
		File destDir;

		if (currentRunMode.equals(RunModes.LOCAL)) {
			srcDir = new File(FrameworkPaths.preFinalReportPath);

			String originalPath = FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator;

			new File(originalPath).mkdir();

			destDir = new File(originalPath);

		} else {
			srcDir = new File(FrameworkPaths.preFinalReportPath);
			new File(FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator).mkdir();
			destDir = new File(FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator);
		}
		try {
			FileUtils.copyDirectory(srcDir, destDir);
			File folderPath = new File(System.getProperty("user.dir") + FrameworkEntities.fileSeparator + "Reports");
			deleteDirectory(folderPath);
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to move Reports to Final Report Destination.");
		}

		destDir.setLastModified(System.currentTimeMillis());

		File reportFolder = new File(
				FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator);

		File reportHTML = new File(
				FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator + "Report.html");

		File reportExcel = new File(FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator
				+ "ExcelReports" + FrameworkEntities.fileSeparator + "Report.xlsx");

		File dataFile = new File(FrameworkPaths.finalReportPath + reportFolderName + FrameworkEntities.fileSeparator
				+ "GlobalTestData.json");

		File dir = new File(reportFolder.getAbsolutePath());
		File[] testLogs = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".log");
			}
		});

		System.out.println("Final Report");
		System.out.println("------------");
		System.out.println("Folder:");
		System.out.println(reportFolder.getAbsolutePath());
		System.out.println();

		if (testLogs.length > 0) {
			System.out.println("Test Logs:");
			for (File file : testLogs) {
				System.out.println(file.getAbsolutePath());
			}
			System.out.println();
		}

		System.out.println("HTML:");
		System.out.println(reportHTML.getAbsolutePath());
		System.out.println();
		System.out.println("Excel:");
		System.out.println(reportExcel.getAbsolutePath());
		System.out.println();
		System.out.println("Global Test Data:");
		System.out.println(dataFile.getAbsolutePath());
	}

	private static void deleteDirectory(File folderPath) throws IOException {
		FileUtils.deleteDirectory(folderPath);
	}

	private static void saveExcelReport() {
		FrameworkEntities.excelReporter.finishReport();
	}

	private static void setRunManagerPath() {
		FrameworkPaths.runManagerPath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator
				+ FrameworkConstants.FrameworkPaths.RUN_MANAGER;
	}

	private static void setOtherPaths() {

		FrameworkPaths.libraryPath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + "ref"
				+ FrameworkEntities.fileSeparator + FrameworkConstants.FrameworkPaths.LIBRARY;

		reportFolderName = "Reports" + FrameworkEntities.fileSeparator + TestSettings.currentEnvironment
				+ FrameworkEntities.fileSeparator + "TestReport_"
				+ getCurrentFormattedTime("yyyyMMdd_hhmm").replace(" ", "_").replace(":", "-");

		FrameworkPaths.preFinalReportPath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator
				+ reportFolderName;

		new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "Screenshots"
				+ FrameworkEntities.fileSeparator).mkdirs();

		FrameworkPaths.screenshotPath = "Screenshots" + FrameworkEntities.fileSeparator;

		FrameworkEntities.encryptionKeyFile = new File(FrameworkStatics.getRelativePath()
				+ FrameworkEntities.fileSeparator + "lib" + FrameworkEntities.fileSeparator + "crypt"
				+ FrameworkEntities.fileSeparator + "automation_encryption_key.txt");
	}

	private static void setRelativePath() {
		
		BasicConfigurator.configure(); 
		LogManager.getRootLogger().setLevel(Level.OFF);
		
		String relativePath = new File(System.getProperty("user.dir")).getAbsolutePath();
		if (relativePath.contains("main")) {
			relativePath = new File(System.getProperty("user.dir")).getParent();
		}
		FrameworkStatics.setRelativePath(relativePath);

	}

	private static void getSettingsFromRunManager() {

		XSSFWorkbook workbook = null;
		OPCPackage pkg = null;
		try {
			pkg = OPCPackage.open(FrameworkPaths.runManagerPath, PackageAccess.READ);
			workbook = new XSSFWorkbook(pkg);

			XSSFSheet sheet = workbook.getSheet("RunConfig");

			if (currentRunMode.equals(RunModes.LOCAL)) {
				TestSettings.currentEnvironment = returnStringCellValue(sheet, 1, 1);
				FrameworkPaths.finalReportPath = returnStringCellValue(sheet, 2, 1);
				TestSettings.currentBuildNumber = returnStringCellValue(sheet, 3, 1);
				if (returnStringCellValue(sheet, 18, 1).equalsIgnoreCase("Yes")) {
					sendEmailFlag = "YES";
					toEmailAddress = returnStringCellValue(sheet, 19, 1);
					ccEmailAddress = returnStringCellValue(sheet, 20, 1);
				}
			}

			TestSettings.adaTestMode = returnStringCellValue(sheet, 4, 1).equalsIgnoreCase("Yes");
			TestSettings.gridMode = returnStringCellValue(sheet, 11, 1).equalsIgnoreCase("Yes");

			if (!TestSettings.gridMode) {
				parallelExecution = returnStringCellValue(sheet, 12, 1).equalsIgnoreCase("Yes");
				if (parallelExecution) {
					FrameworkSettings.noOfThreads = Integer.parseInt(returnStringCellValue(sheet, 13, 1));
				} else {
					FrameworkSettings.noOfThreads = 1;
				}
			} else {
				FrameworkSettings.noOfThreads = Integer.parseInt(returnStringCellValue(sheet, 13, 1));
			}

		} catch (IOException e) {
			String errorMsg = "Unable to open Run Manager!";
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, errorMsg);

		} catch (InvalidFormatException e) {
			String errorMsg = "Run Manager is not a valid Excel Sheet!";
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, errorMsg);

		} finally {
			if (pkg != null) {
				try {
					workbook.close();
					pkg.close();
				} catch (IOException e) {
					String errorMsg = "Unable to close Run Manager!";
					ErrorHandler.frameworkError(ErrLvl.FATAL, e, errorMsg);
				}
			}
		}
	}

	private static String returnStringCellValue(XSSFSheet sheet, int rowNum, int columnNum) {

		String cellValue = "";

		cellValue = formatter.formatCellValue(sheet.getRow(rowNum).getCell(columnNum));

		if (cellValue == null) {
			cellValue = "";
		}

		return cellValue;
	}

	private static enum RunModes {
		LOCAL, TESTS_WITH_YES, TEST_SUITES_FROM_ARGS
	}

}
