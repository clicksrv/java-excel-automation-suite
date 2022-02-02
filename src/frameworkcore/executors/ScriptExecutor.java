package frameworkcore.executors;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import com.aventstack.extentreports.Status;

import org.apache.commons.lang.StringUtils;

import frameworkcore.EncryptionToolkit;
import frameworkcore.FrameworkStatics;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.dbtools.SQLActions;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.excelreaders.ScriptReader;
import frameworkcore.testdatamanagement.TestDataManager;
import frameworkcore.testdatamanagement.ValueParser;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.methods.FrameworkKeywords;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 * @author Surbhi Sinha (surbhi.b.sinha)
 * @author Ashish Vishwakarma (ashish.b.vishwakarma)
 * @author Vedhanth Reddy (v.reddy.monajigari)
 */
public class ScriptExecutor extends TestThread {

	public ScriptExecutor(ThreadEntities threadEntities) {
		super(threadEntities);
	}

	enum ScriptType {
		STANDARD_SCRIPT, PRE_DEFINED_FLOW;
	}

	ScriptReader scriptReader = null;

	/**
	 * This method is used to execute Script
	 */
	public void executeScript() {
		parseScript(ScriptType.STANDARD_SCRIPT, null, null);
	}

	/**
	 * This method is used to parse Script
	 * 
	 * @param scriptType - This parameter is of String data type
	 * @param flowName - This parameter is of String data type
	 * @param dataSet - This parameter is of String data type
	 */

	private void parseScript(ScriptType scriptType, String flowName, String dataSet) {

		ValueParser valueParser = new ValueParser(threadEntities);

		int offset = 0;

		int startStep = 2;
		int endStep = 2;

		switch (scriptType) {
		case STANDARD_SCRIPT:

			scriptReader = new ScriptReader(super.testParameters);

			offset = 0;

			startStep = testParameters.getStartStep();
			endStep = testParameters.getEndStep();

			if (endStep < 2) {
				endStep = scriptReader.getRowCount(super.testParameters.getCurrentTestScript());
			}

			break;

		case PRE_DEFINED_FLOW:

			scriptReader = new ScriptReader(flowName);

			offset = 1;

			startStep = scriptReader.getDataSetStartStep(dataSet);
			endStep = scriptReader.getDataSetEndStep(dataSet);

			break;

		default:
			ErrorHandler.testError(ErrLvl.FATAL, null, "Unknown script type!", testCaseVariables);

		}

		LinkedList<Integer> envSteps = null;

		for (int i = startStep - 1; i < endStep; i++) {

			if (null != envSteps) {
				if (envSteps.size() != 0) {
					i = envSteps.getFirst();
					envSteps.removeFirst();
				} else {
					envSteps = null;
					while (!scriptReader.readStep(i, 6 + offset).isEmpty()) {
						i++;
					}
				}
			}

			ScriptActionBlock actionBlock = new ScriptActionBlock(scriptReader, i, offset);

			if (null == envSteps && !actionBlock.getEnvironmentStepGroup().isEmpty()) {
				envSteps = new LinkedList<Integer>();

				for (int j = i; !actionBlock.getEnvironmentStepGroup().isEmpty(); j++) {
					if (actionBlock.getEnvironmentStepGroup().equalsIgnoreCase(TestSettings.currentEnvironment)) {
						envSteps.add(j);
					} else if (actionBlock.getEnvironmentStepGroup().equalsIgnoreCase("Default")) {
						envSteps.add(j);
					}
				}

				if (envSteps.size() == 0) {
					continue;
				}
			}

			actionBlock.setAttributes(scriptReader, i, offset);

			String printString = "";

			if (scriptType.equals(ScriptType.STANDARD_SCRIPT)) {
				testCaseVariables.currentStepNumber = i;
			} else if (scriptType.equals(ScriptType.PRE_DEFINED_FLOW)) {
				if (testCaseVariables.currentPreDefinedFlowLevel > testCaseVariables.currentPreDefinedFlowStepNumber
						.size()) {
					testCaseVariables.currentPreDefinedFlowStepNumber
							.add(testCaseVariables.currentPreDefinedFlowLevel - 1, i + 1);
				} else if (testCaseVariables.currentPreDefinedFlowLevel == testCaseVariables.currentPreDefinedFlowStepNumber
						.size()) {
					testCaseVariables.currentPreDefinedFlowStepNumber
							.remove(testCaseVariables.currentPreDefinedFlowStepNumber.size() - 1);
					testCaseVariables.currentPreDefinedFlowStepNumber.add(i + 1);
				}
			}

			boolean preRequisitesMet = true;
			SQLActions sqlAssert = null;

			switch (actionBlock.getKeywordType()) {
			case "Handle Screen":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getElementName())) {
					reportingManager.updateTestLog(Status.ERROR, "Element Name is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getAction())) {
					reportingManager.updateTestLog(Status.ERROR, "Screen Action is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getElementType())) {
					reportingManager.updateTestLog(Status.ERROR, "Element Type is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getElementXpath())) {
					reportingManager.updateTestLog(Status.ERROR, "Xpath is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Handling Screen not met!", false);
				}

				testCaseVariables.currentPageOrBJStreamKeyword = actionBlock.getKeyword();

				if (!StringUtils.equals(actionBlock.getKeyword(), scriptReader.readStep(i - 1, 1 + offset))) {
					printString = "*   Handling Screen: " + actionBlock.getKeyword() + "   *";

					printMethodStart(printString);

					testCaseVariables.excelReporter.createNode("Handled Screen: " + actionBlock.getKeyword());
				}

				PageUtility pageUtility = new PageUtility(threadEntities);

				if (actionBlock.getAction().equals("Use Custom Handler")) {
					pageUtility.executeCustomCode(actionBlock.getValue(), actionBlock.getElementXpath());
				} else {
					if (StringUtils.isNotBlank(actionBlock.getValue())
							&& !(StringUtils.equals(actionBlock.getAction(), "Set Value To GlobalVariable"))) {
						actionBlock.setValue(valueParser.checkParseabilityAndParse(actionBlock.getValue()));
					}
					pageUtility.handlePageElement(actionBlock);
				}

				if (!StringUtils.equals(actionBlock.getKeyword(), scriptReader.readStep(i + 1, 1 + offset))) {
					printMethodCompletion(actionBlock.getKeyword());
				}

				testCaseVariables.currentPageOrBJStreamKeyword = null;

				break;

			case "Execute Method":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.equals("navigate", actionBlock.getKeyword())
						&& StringUtils.isEmpty(actionBlock.getValue())) {
					reportingManager.updateTestLog(Status.ERROR, "Value is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Executing Method not met!", false);
				}

				testCaseVariables.currentMethodKeyword = actionBlock.getKeyword();

				if (StringUtils.equals("navigate", actionBlock.getKeyword())) {
					printString = "*   Navigating through links: " + actionBlock.getValue() + "   *";
				} else if (StringUtils.equals("testScreenForADACompliance", actionBlock.getKeyword())) {
					printString = "";
				} else {
					printString = "*   Executing Method: " + actionBlock.getKeyword() + "   *";
				}

				if (StringUtils.isNotBlank(printString)) {
					printMethodStart(printString);
				}

				if (StringUtils.equals("navigate", actionBlock.getKeyword())) {
					FrameworkKeywords keywords = new FrameworkKeywords(threadEntities);
					keywords.navigate(actionBlock.getValue());
				} else {
					invokeMethod(actionBlock.getKeyword());
				}

				if (!StringUtils.equals("testScreenForADACompliance", actionBlock.getKeyword())) {
					printMethodCompletion(actionBlock.getKeyword());
				}

				testCaseVariables.currentMethodKeyword = null;

				break;

			case "Handle Batch Job Stream":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL,
							"Pre-Requisites for Handling Batch Job Stream not met!", false);
				}

				testCaseVariables.currentPageOrBJStreamKeyword = actionBlock.getKeyword();

				printString = "*   Handling Batch Job Stream: " + actionBlock.getKeyword() + "   *";

				printMethodStart(printString);

				BatchUtility batchUtility = new BatchUtility(threadEntities, actionBlock.getValue());
				batchUtility.handleBatchJobStream();

				printMethodCompletion(actionBlock.getKeyword());

				testCaseVariables.currentPageOrBJStreamKeyword = null;

				break;

			case "Invoke Defined Flow":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getValue())) {
					reportingManager.updateTestLog(Status.ERROR, "Value is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Invoking Custom Flow not met!",
							false);
				}

				if (TestSettings.writeTestConsoleToFile) {
					testCaseVariables.logWriter.append("\n");
					for (int x = 0; x <= testCaseVariables.currentPreDefinedFlowLevel; x++) {
						testCaseVariables.logWriter.append("#");
					}
					testCaseVariables.logWriter.append("# Invoking PreDefined Flow '" + actionBlock.getKeyword()
							+ "' with DataSet '" + actionBlock.getValue() + "'");
				} else {
					System.out.println();
					for (int x = 0; x <= testCaseVariables.currentPreDefinedFlowLevel; x++) {
						System.out.print("#");
					}
					System.out.print("# Invoking PreDefined Flow '" + actionBlock.getKeyword() + "' with DataSet '"
							+ actionBlock.getValue() + "'");
				}

				testCaseVariables.currentPreDefinedFlowLevel++;

				ScriptExecutor predefinedFlowExecutor = new ScriptExecutor(threadEntities);
				predefinedFlowExecutor.parseScript(ScriptType.PRE_DEFINED_FLOW, actionBlock.getKeyword(),
						actionBlock.getValue());

				testCaseVariables.currentPreDefinedFlowLevel--;

				if (testCaseVariables.currentPreDefinedFlowLevel < testCaseVariables.currentPreDefinedFlowStepNumber
						.size()) {
					testCaseVariables.currentPreDefinedFlowStepNumber
							.remove(testCaseVariables.currentPreDefinedFlowStepNumber.size() - 1);
				}

				if (TestSettings.writeTestConsoleToFile) {
					testCaseVariables.logWriter.append("\n");
					for (int x = 0; x <= testCaseVariables.currentPreDefinedFlowLevel; x++) {
						testCaseVariables.logWriter.append(">");
					}
					testCaseVariables.logWriter.append("> Completed PreDefined Flow '" + actionBlock.getKeyword()
							+ "' with DataSet '" + actionBlock.getValue() + "'");

					testCaseVariables.logWriter.append("\n");
				} else {
					System.out.println();
					for (int x = 0; x <= testCaseVariables.currentPreDefinedFlowLevel; x++) {
						System.out.print(">");
					}
					System.out.print("> Completed PreDefined Flow '" + actionBlock.getKeyword() + "' with DataSet '"
							+ actionBlock.getValue() + "'");
					System.out.println();
				}

				break;

			case "Wait (in ms)":

				if (StringUtils.isEmpty(actionBlock.getValue())) {
					reportingManager.updateTestLog(Status.ERROR, "Value is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Waiting not met!", false);
				}

				printString = "*   Waiting for: " + actionBlock.getValue() + " ms    *";

				printMethodStart(printString);

				try {
					Thread.sleep(Long.parseLong(actionBlock.getValue()));
				} catch (NumberFormatException e) {
					ErrorHandler.testError(ErrLvl.ERROR, e, actionBlock.getValue() + " is not a valid number!",
							testCaseVariables);
				} catch (InterruptedException e) {
					ErrorHandler.testError(ErrLvl.FATAL, e, "Thread interrupted!", testCaseVariables);
				}

				reportingManager.updateTestLog(Status.INFO, "Explicitly waited for " + actionBlock.getValue() + " ms",
						false);

				printString = "> Finished Waiting";

				if (TestSettings.writeTestConsoleToFile) {
					testCaseVariables.logWriter.append("\n" + printString + "\n");
				} else {
					System.out.println();
					System.out.println(printString);
					System.out.println();
				}

				break;

			case "Set Value To GlobalVariable":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getValue())) {
					reportingManager.updateTestLog(Status.ERROR, "Value is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL,
							"Pre-Requisites for Setting Value to GlobalVariable not met!", false);
				}

				TestDataManager tdm = new TestDataManager(threadEntities);
				String varName = actionBlock.getKeyword();

				if (varName.startsWith(ValueParser.GLOBAL_KEY)) {
					varName = varName.substring(ValueParser.GLOBAL_KEY.length());
				}

				if (StringUtils.isNotBlank(actionBlock.getValue())) {
					actionBlock.setValue(valueParser.checkParseabilityAndParse(actionBlock.getValue()));
				}

				tdm.setTestGlobal(varName, actionBlock.getValue());
				break;
			case "Assert DB":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getValue())) {
					reportingManager.updateTestLog(Status.ERROR, "Value is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Asserting Database not met!",
							false);
				}

				sqlAssert = new SQLActions(threadEntities);
				sqlAssert.assertDB(actionBlock.getKeyword(), actionBlock.getValue(), Status.FAIL);
				break;

			case "Assert DB Fatally":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (StringUtils.isEmpty(actionBlock.getValue())) {
					reportingManager.updateTestLog(Status.ERROR, "Value is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Asserting Database not met!",
							false);
				}

				sqlAssert = new SQLActions(threadEntities);
				sqlAssert.assertDB(actionBlock.getKeyword(), actionBlock.getValue(), Status.FATAL);
				break;

			case "Update DB":

				if (StringUtils.isEmpty(actionBlock.getKeyword())) {
					reportingManager.updateTestLog(Status.ERROR, "Keyword is blank!", false);
					preRequisitesMet = false;
				}
				if (!preRequisitesMet) {
					reportingManager.updateTestLog(Status.FATAL, "Pre-Requisites for Asserting Database not met!",
							false);
				}

				String decryptedQuery;

				try {
					decryptedQuery = EncryptionToolkit.decrypt(
							FrameworkPropertyFiles.updateQueryProperties.getProperty(actionBlock.getKeyword()),
							FrameworkEntities.encryptionKeyFile);
				} catch (Exception e) {
					ErrorHandler.testError(ErrLvl.FATAL, e, "Appropriate Key is required!", testCaseVariables);
				}

				ValueParser parser = new ValueParser(threadEntities);
				decryptedQuery = parser.parseStatementsWithParseableValueInQuotes(actionBlock.getKeyword());

				SQLActions updateSql = new SQLActions(threadEntities);
				int updatedRecords = updateSql.updateDB(decryptedQuery);

				if (updatedRecords > -1) {
					reportingManager.updateTestLog(Status.INFO, "Number of records updated: " + updatedRecords, false);
				}

				break;

			default:
				reportingManager.updateTestLog(Status.WARNING,
						"Blank keyword type encountered! Please write 'Skip' in Environment Step Group column for skipping steps.",
						false);
				continue;
			}
		}

	}

	/**
	 * This method is used to print Method Start
	 * 
	 * @param printString - This parameter is of String data type
	 */
	private void printMethodStart(String printString) {

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter
					.append("\n" + "## Test Case \t\t: " + testParameters.getCurrentTestCaseOutput() + "\n");
			printStars(printString.length());
			testCaseVariables.logWriter.append(printString + "\n");
			printStars(printString.length());
			testCaseVariables.logWriter.append("\n");
		} else {
			System.out.println();
			System.out.println("## Test Case \t\t: " + testParameters.getCurrentTestCaseOutput());
			printStars(printString.length());
			System.out.println(printString);
			printStars(printString.length());
			System.out.println();
		}
	}

	/**
	 * This method is used to print Method completion
	 * 
	 * @param keyword - This parameter is of String data type
	 */

	private void printMethodCompletion(String keyword) {
		String printString = "> Finished " + keyword;

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter.append("\n\n" + printString + "\n");
		} else {
			System.out.println();
			System.out.println(printString);
			System.out.println();
		}
	}

	/**
	 * This method is used when keyword is set to execute method
	 * 
	 * @param methodName - This parameter is of String data type
	 */
	private void invokeMethod(String methodName) {

		Boolean methodFound = false;
		final String CLASS_FILE_EXTENSION = ".java";

		File packageDirectory = new File(FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + "src"
				+ FrameworkEntities.fileSeparator + "frameworkextensions" + FrameworkEntities.fileSeparator
				+ "methods");

		File[] packageFiles = packageDirectory.listFiles();
		String packageName = "frameworkextensions.methods";

		for (int i = 0; i < packageFiles.length; i++) {

			File packageFile = packageFiles[i];
			String fileName = packageFile.getName();

			if (fileName.endsWith(CLASS_FILE_EXTENSION)) {
				String className = fileName.substring(0, fileName.length() - CLASS_FILE_EXTENSION.length());

				Class<?> reusableComponents = null;
				try {
					reusableComponents = Class.forName(packageName + "." + className);
				} catch (ClassNotFoundException e) {
					ErrorHandler.testError(ErrLvl.ERROR, e, packageName + "." + className + " class not found!",
							testCaseVariables);
				}
				Method executeComponent;

				try {
					methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
					executeComponent = reusableComponents.getMethod(methodName, (Class<?>[]) null);

				} catch (NoSuchMethodException ex) {
					continue;
				}

				methodFound = true;

				Constructor<?> ctor = reusableComponents.getDeclaredConstructors()[0];
				Object businessComponent = null;
				try {
					businessComponent = ctor.newInstance(threadEntities);

				} catch (InstantiationException e) {
					ErrorHandler.testError(ErrLvl.FATAL, e,
							"'" + packageName + "." + className + " class' constructor could not be instantiated!",
							testCaseVariables);
				} catch (IllegalAccessException e) {
					ErrorHandler.testError(ErrLvl.FATAL, e,
							"'" + packageName + "." + className + " class' constructor could not be accessed!",
							testCaseVariables);
				} catch (IllegalArgumentException e) {
					ErrorHandler.testError(ErrLvl.FATAL, e, "'" + packageName + "." + className
							+ " class' constructor was not provided appropriate arguments!", testCaseVariables);
				} catch (InvocationTargetException e) {
					ErrorHandler.testError(
							ErrLvl.FATAL, e, "'" + packageName + "." + className
									+ " class' could not be completed successfully due to: " + e.getCause(),
							testCaseVariables);
				}

				try {
					executeComponent.invoke(businessComponent, (Object[]) null);
				} catch (IllegalAccessException e) {
					ErrorHandler.testError(ErrLvl.FATAL, e, "'" + packageName + "." + className
							+ " class' constructor was illegally accessed! (Tried to reflectively create an instance (other than an array), set or get a field, or invoke a method, but the currently executing method does not have access to the definition of the specified class, field, method or constructor.)",
							testCaseVariables);
				} catch (IllegalArgumentException e) {
					ErrorHandler.testError(ErrLvl.FATAL, e, "'" + packageName + "." + className
							+ " class' constructor was not provided appropriate arguments! (Method has been passed an illegal or inappropriate argument.)",
							testCaseVariables);
				} catch (InvocationTargetException e) {
					ErrorHandler.testError(
							ErrLvl.FATAL, e, "'" + packageName + "." + className
									+ " class' could not be completed successfully due to: " + e.getCause(),
							testCaseVariables);
				}

				break;
			}
		}

		if (!methodFound) {

			String errorMsg = "Method " + methodName + " was not found within any class inside package "
					+ properties.getProperty("Keywords");
			ErrorHandler.testError(ErrLvl.FATAL, null, errorMsg, testCaseVariables);

		}
	}

	private void printStars(int length) {
		for (int i = 1; i <= length; i++) {
			if (TestSettings.writeTestConsoleToFile) {
				testCaseVariables.logWriter.append("*");
			} else {
				System.out.print("*");
			}
		}

		if (TestSettings.writeTestConsoleToFile) {
			testCaseVariables.logWriter.append("\n");
		} else {
			System.out.print("\n");
		}
	}

}
