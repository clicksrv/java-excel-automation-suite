package frameworkcore.executors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkConstants;
import frameworkcore.FrameworkStatics;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.batchjobtools.BatchFileReader;
import frameworkcore.batchjobtools.BatchFileWriter;
import frameworkcore.batchjobtools.GoAnywhereFTPUtility;
import frameworkcore.batchjobtools.GoAnywhereWebUtility;
import frameworkcore.batchjobtools.XMLLayoutInterpreter;
import frameworkcore.datablocks.Metadata.BatchJobStreamMetadata.BatchJob;
import frameworkcore.dbtools.SQLActions;
import frameworkcore.dbtools.SQLUtilities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.excelreaders.BatchScriptReader;
import frameworkcore.excelreaders.LibraryReader;
import frameworkcore.syslogcapturetools.LogCaptureManager;
import frameworkcore.testdatamanagement.TestDataManager;
import frameworkcore.testdatamanagement.ValueParser;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.applicationhandler.BatchJobStatus;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class BatchUtility extends TestThread {

    String workbookName = null;
    BatchScriptReader reader = null;

    boolean batchFailed = false;

    String jobPath = null;
    List<BatchJob> batchJobs = null;
    List<String> batchJobsToSkip = null;
    List<String> batchJobsOnFail = null;
    List<String> batchJobsToExecute = null;

    String testCaseId = testParameters.getCurrentTestScript();

    final List<String> validLayouts = Arrays.asList("TXT", "XML", "CSV");

    public BatchUtility(ThreadEntities threadEntities, String executionBatchList) {

        super(threadEntities);

        workbookName = testCaseVariables.currentPageOrBJStreamKeyword.trim();

        testCaseVariables.excelReporter
                .createNode("Handled Batch: " + testCaseVariables.currentPageOrBJStreamKeyword.trim());

        if (!FrameworkEntities.metadata.getBatchJobStreamMetadata().isMetadataLoaded()) {
            try (LibraryReader libraryReader = new LibraryReader();) {
                libraryReader.loadBatchMetadata();
            }
        }

        if (FrameworkStatics.activeBatchJobStreams.contains(testCaseVariables.currentPageOrBJStreamKeyword)) {
            System.out.println("Batch Job Stream is already active on another thread!");
            System.out.println("Waiting..");
            synchronized (FrameworkStatics.activeBatchJobStreams) {
                try {
                    FrameworkStatics.activeBatchJobStreams.wait();
                } catch (InterruptedException e) {
                    ErrorHandler.testError(ErrLvl.ERROR, e,
                            "Thread was interrupted while waiting for batch job stream to be released.",
                            testCaseVariables);
                }
            }
        }

        FrameworkStatics.markActiveBatchJobStream(testCaseVariables);

        if (StringUtils.isNotBlank(executionBatchList)) {
            batchJobsToExecute = Arrays.asList(executionBatchList.split("\\s*,\\s"));
        }

        driver.manage().timeouts().pageLoadTimeout(FrameworkTimeouts.batchJobTimeout, TimeUnit.SECONDS);
    }

    ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
    ValueParser parser = new ValueParser(threadEntities);

    /**
     * This method is used to Handle batch Job stream.
     */
    public void handleBatchJobStream() {

        reader = new BatchScriptReader(workbookName, threadEntities);

        try {
            batchJobs = FrameworkEntities.metadata.getBatchJobStreamMetadata().getBatchJobsInStream(workbookName);
            jobPath = batchJobs.get(0).getJobPath();

            boolean areBatchJobsPrePopulated = (batchJobsToExecute != null);

            if (!areBatchJobsPrePopulated) {
                batchJobsToExecute = new ArrayList<String>();
            }

            for (BatchJob batch : batchJobs) {
                if (batch.getOrdinal() > 0 && !areBatchJobsPrePopulated) {
                    batchJobsToExecute.add(batch.getBatchJobName());
                } else if (batch.getOrdinal() < 0) {
                    batchJobsOnFail.add(batch.getBatchJobName());
                }
            }

            if (areBatchJobsPrePopulated) {
                batchJobsToSkip = new ArrayList<String>();

                for (BatchJob batch : batchJobs) {
                    if (!batchJobsToExecute.contains(batch.getBatchJobName())) {
                        batchJobsToSkip.add(batch.getBatchJobName());
                    }
                }
            }

            for (BatchJob batch : batchJobs) {

                String batchName = batch.getBatchJobName();
                String ifmPropKey = null;

                if (StringUtils.startsWith(batchName, "Load") || StringUtils.startsWith(batchName, "Write")) {
                    ifmPropKey = batch.getFileName();
                    if (StringUtils.isBlank(ifmPropKey)) {
                        reportingManager.updateTestLog(Status.WARNING,
                                "IFM Batch Property Key is missing for: " + batchName, false);
                    }
                }

                String batchFileBaseName = null;

                if (StringUtils.startsWith(batchName, "Load")) {
                    batchFileBaseName = FrameworkPropertyFiles.ifmInboundProperties.getProperty(ifmPropKey);
                    batchFileBaseName = batchFileBaseName.substring(0, batchFileBaseName.indexOf("*"));
                } else if (StringUtils.startsWith(batchName, "Write")) {
                    batchFileBaseName = FrameworkPropertyFiles.ifmOutboundProperties.getProperty(ifmPropKey);
                }

                if (null != batchJobsToSkip) {
                    if (!batchJobsToSkip.contains(batchName)) {

                        checkAndWaitForExistingBatchJobInstanceToFinish(batchName);

                        String processDate = executePreBatchJobActions(batchName, batchFileBaseName);
                        executeBatchJob(batchName, processDate);
                        executePostBatchJobActions(batchName, batchFileBaseName);
                    } else {
                        reportingManager.updateTestLog(Status.INFO,
                                TextHTMLWrapper.wrapValue(batchName) + " was skipped.", false);
                    }
                } else {
                    checkAndWaitForExistingBatchJobInstanceToFinish(batchName);

                    String processDate = executePreBatchJobActions(batchName, batchFileBaseName);
                    executeBatchJob(batchName, processDate);
                    executePostBatchJobActions(batchName, batchFileBaseName);
                }
            }

        } catch (Exception e) {
            ErrorHandler.testError(ErrLvl.FATAL, e, "Caught an exception in Batch Utility!", testCaseVariables);
        } finally {
            reader.closeWorkbook();
            FrameworkStatics.unmarkActiveBatchJobStream(testCaseVariables);
            driver.manage().timeouts().pageLoadTimeout(FrameworkTimeouts.pageLoadTimeOut, TimeUnit.SECONDS);
        }
    }

    private void checkAndWaitForExistingBatchJobInstanceToFinish(String batchName) {

        boolean batchAlreadyRunning = true;
        int waitCounter = 0;

        SQLUtilities sql = new SQLUtilities();

        while (batchAlreadyRunning && (waitCounter * 5) <= FrameworkTimeouts.batchJobTimeout) {
            BatchJobStatus batchJobStatus = new BatchJobStatus();

            ArrayList<String> statusResults = batchJobStatus.checkIfBatchJobAlreadyRunning(sql, batchName);

            batchAlreadyRunning = Boolean.parseBoolean(statusResults.get(0));

            if (batchAlreadyRunning) {
                if (waitCounter == 0) {
                    reportingManager
                            .updateTestLog(Status.INFO,
                                    batchName + " is already running on the application which started at '"
                                            + statusResults.get(1) + "'. Waiting for existing instance to finish!",
                                    false);
                    log("Waiting..");
                } else if (waitCounter % 15 == 0) {
                    log(waitCounter + "..");
                }

                threadSleep(5000);
                waitCounter++;
            }

            if (!batchAlreadyRunning && waitCounter > 0) {
                logLine();
            }
        }

        if (batchAlreadyRunning) {
            reportingManager.updateTestLog(Status.FATAL,
                    "Existing Batch Job Instance did not complete in Batch Job Timeout period of "
                            + FrameworkTimeouts.batchJobTimeout + " seconds!",
                    false);
        }
    }

    /**
     * This method is used to perform actions needs to complete before batch job
     * execution
     * 
     * @param batchJobName - This parameter is of String data type
     * @param baseFileName - This parameter is of String data type
     */
    private String executePreBatchJobActions(String batchJobName, String baseFileName) {
        int rowCount = reader.getRowCount(reader.scriptSheetName);

        boolean fileOpenToWrite = false;
        String layoutType = null;
        String layoutSheetName = null;
        File file = null;
        int recordCounter = 0;
        String processDate = null;
        SQLActions sqlAssert = null;

        XMLLayoutInterpreter xmlLayoutInterpreter = null;
        BatchFileWriter writer = null;

        for (int i = 1; i <= rowCount; i++) {
            String actionType = null;
            String fieldName = null;
            String lengthOrDepth = null;
            String value = null;
            String expectedValue = null;

            if (StringUtils.equals(testCaseId, reader.getScriptCellData(0, i))
                    && (StringUtils.equals("Pre", reader.getScriptCellData(1, i)))
                    && (StringUtils.equals(batchJobName, reader.getScriptCellData(2, i)))) {
                actionType = reader.getScriptCellData(3, i);
                fieldName = reader.getScriptCellData(4, i);
                lengthOrDepth = reader.getScriptCellData(5, i);
                value = reader.getScriptCellData(6, i);
                expectedValue = reader.getScriptCellData(7, i);

                switch (actionType) {
                case "Skip Batch Jobs":
                    if (null != batchJobsToSkip) {
                        ErrorHandler.testError(ErrLvl.ERROR, null,
                                "'Skip Batch Jobs' can only be used once per Batch Job Stream in one Test Case!",
                                testCaseVariables);
                    } else if (StringUtils.isNotEmpty(value)) {
                        batchJobsToSkip = Arrays.asList(value.split("\\s*,\\s"));
                    }
                    break;
                case "Create New File":
                    layoutType = value.toUpperCase();
                    if (StringUtils.isBlank(layoutType)) {
                        ErrorHandler
                                .testError(ErrLvl.FATAL, null,
                                        "Please specify a valid layout type in step '" + actionType
                                                + "'. Valid layout types are: " + validLayouts.toString(),
                                        testCaseVariables);
                    } else if (!validLayouts.contains(layoutType)) {
                        ErrorHandler.testError(ErrLvl.FATAL, null, layoutType + " is not an accepted Layout Type.",
                                testCaseVariables);
                    }

                    new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "BatchFiles"
                            + FrameworkEntities.fileSeparator + "inbound" + FrameworkEntities.fileSeparator).mkdirs();

                    file = new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "BatchFiles"
                            + FrameworkEntities.fileSeparator + "inbound" + FrameworkEntities.fileSeparator
                            + baseFileName + "_" + testCaseId + "_" + getCurrentTime("yyyy-MM-dd_HH-mm-ss") + "."
                            + layoutType.toLowerCase());
                    try {
                        writer = new BatchFileWriter(file);
                    } catch (IOException e) {
                        ErrorHandler.testError(ErrLvl.FATAL, e,
                                "Failed to create file writer for file: " + file.getAbsolutePath(), testCaseVariables);
                    }
                    fileOpenToWrite = true;

                    reportingManager.updateTestLog(Status.DEBUG, "File creation started!", false);

                    break;

                case "Add New Record":
                    recordCounter++;
                    layoutSheetName = value;

                    if (StringUtils.isBlank(layoutSheetName)) {
                        ErrorHandler.testError(ErrLvl.FATAL, null,
                                "Please specify the layout sheet name for which record is being added in step '"
                                        + actionType + "'",
                                testCaseVariables);
                    }

                    String recordLayoutType = reader.getActiveWorkbook().getSheet(layoutSheetName).getRow(0).getCell(1)
                            .getStringCellValue();

                    if (!StringUtils.equalsIgnoreCase(layoutType, recordLayoutType)) {
                        ErrorHandler.testError(ErrLvl.FATAL, null, "Record Layout Type is: " + recordLayoutType
                                + " while expected layout is: " + layoutType, testCaseVariables);
                    }

                    if (recordCounter > 1) {
                        try {
                            writer.write("\n");
                        } catch (IOException e) {
                            ErrorHandler.testError(ErrLvl.FATAL, e,
                                    "Failed to move to new record on file: " + file.getAbsolutePath(),
                                    testCaseVariables);
                        }
                    }
                    if (StringUtils.equals(layoutType, "XML")) {
                        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                        writer.writeLine(str);
                        xmlLayoutInterpreter = new XMLLayoutInterpreter(reader.getActiveWorkbook(), layoutSheetName,
                                layoutSheetName);
                        xmlLayoutInterpreter.writeAndReachUptoFirstValueField(writer);
                    }

                    reportingManager.updateTestLog(Status.DEBUG, "New record added to file!", false);

                    break;

                case "Set File Value":
                    if (fileOpenToWrite) {
                        if (StringUtils.isNotBlank(value)) {
                            value = parser.checkParseabilityAndParse(value);
                        }

                        if (StringUtils.equals(layoutType, "TXT")) {
                            writer.writeWithFixedLength(value, Integer.parseInt(lengthOrDepth));
                        } else if (StringUtils.equals(layoutType, "XML")) {
                            xmlLayoutInterpreter.writeAndReachUptoNextValueField(fieldName,
                                    Integer.parseInt(lengthOrDepth), value, writer);
                        } else if (StringUtils.equals(layoutType, "CSV")) {
                            System.out.println("TODO!!!");
                        }
                    } else {
                        ErrorHandler.testError(ErrLvl.FATAL, null,
                                actionType + " without performed without starting new file!", testCaseVariables);
                    }
                    break;

                case "Save File":
                    if (fileOpenToWrite) {
                        try {
                            writer.close();
                            System.out.println();
                            reportingManager
                                    .updateTestLog(Status.INFO,
                                            "File generated: " + FrameworkConstants.HTMLFormat.VALUE
                                                    + file.getAbsolutePath() + FrameworkConstants.HTMLFormat.CLOSE,
                                            false);
                        } catch (IOException e) {
                            ErrorHandler.testError(ErrLvl.FATAL, e, "Failed to save file: " + file.getAbsolutePath(),
                                    testCaseVariables);
                        }
                    } else {
                        ErrorHandler.testError(ErrLvl.FATAL, null,
                                actionType + " without performed without starting new file!", testCaseVariables);
                    }
                    fileOpenToWrite = false;

                    reportingManager.updateTestLog(Status.DEBUG, "File saved!", false);
                    break;

                case "Upload File":
                    if (null == file) {
                        file = new File(value);
                    }

                    while (!file.canExecute()) {
                    }

                    if (StringUtils.equalsIgnoreCase("FTP", TestSettings.goAnywhereAccessMethod)) {
                        GoAnywhereFTPUtility goAnywhereUtility = new GoAnywhereFTPUtility(threadEntities);
                        goAnywhereUtility.uploadFileToInbound(file);
                    } else if (StringUtils.equalsIgnoreCase("WebUI", TestSettings.goAnywhereAccessMethod)) {
                        GoAnywhereWebUtility goAnywhereUtility = new GoAnywhereWebUtility(threadEntities);
                        goAnywhereUtility.uploadFileToInbound(file);
                    } else {
                        ErrorHandler.testError(ErrLvl.FATAL, null,
                                "Unaccepted Go Anywhere Access Method: " + TestSettings.goAnywhereAccessMethod,
                                testCaseVariables);
                    }

                    reportingManager.updateTestLog(Status.DEBUG, "File uploaded to GoAnywhere!", false);
                    break;

                case "Assert DB":
                    sqlAssert = new SQLActions(threadEntities);
                    sqlAssert.assertDB(value, expectedValue, Status.FAIL);
                    break;

                case "Assert DB Fatally":
                    sqlAssert = new SQLActions(threadEntities);
                    sqlAssert.assertDB(value, expectedValue, Status.FATAL);
                    break;

                case "Set Global":
                    TestDataManager tdm = new TestDataManager(threadEntities);
                    String varName = null;
                    String varVal = null;

                    if (value.startsWith(ValueParser.GLOBAL_KEY)) {
                        value = value.substring(ValueParser.GLOBAL_KEY.length());
                    }

                    int equationIndex = value.indexOf("=");

                    if (equationIndex == -1) {
                        String errorMsg = "Global Variable for " + value
                                + " is not being assigned any value for SetGlobal action!";
                        ErrorHandler.testError(ErrLvl.ERROR, null, errorMsg, testCaseVariables);
                        break;
                    }

                    varName = value.substring(0, equationIndex).trim();
                    varVal = value.substring(equationIndex + 1).trim();

                    varVal = parser.checkParseabilityAndParse(varVal);

                    tdm.setTestGlobal(varName, varVal);
                    break;

                case "Set Process Date":
                    processDate = value;
                    reportingManager.updateTestLog(Status.DEBUG, "Process Date set to " + processDate + "!", false);
                    break;

                default:
                    ErrorHandler.testError(ErrLvl.ERROR, null,
                            actionType + " is an invalid action type for Pre-Batch execution!", testCaseVariables);
                }
            }
        }

        return processDate;
    }

    /**
     * This method is used to execute batch job
     * 
     * @param batchJobName - This parameter is of String data type
     * @param processDate - This parameter is of String data type
     */
    private void executeBatchJob(String batchJobName, String processDate) {

        String batchURL = urlProperties.getProperty("Batch_" + TestSettings.currentEnvironment) + jobPath + "&jobFile="
                + batchJobName;

        if (null != processDate) {
            batchURL = batchURL + "&processDate=" + processDate;
        }

        reportingManager.updateTestLog(Status.INFO, "Hitting URL: " + batchURL, false);

        startTimer();
        driver.get(batchURL);
        String timeTaken = stopTimerAndReturnElapsedTime();

        String resultText = null;
        try {
            resultText = driver.findElement(By.xpath(".//pre")).getText();
        } catch (NoSuchElementException e) {
            reportingManager.updateTestLog(Status.FAIL, "Failed to execute batch!", true);
        }

        if (StringUtils.contains(resultText, "COMPLETED")) {
            updateBatchHitResult(timeTaken, resultText, Status.PASS);
        } else if (StringUtils.contains(resultText, "FAILED")) {
            batchFailed = true;

            updateBatchHitResult(timeTaken, resultText, Status.FAIL);

            LogCaptureManager logCaptureManager = new LogCaptureManager();
            String logs = logCaptureManager.captureLogsForBatch(batchJobName);

            if (StringUtils.isNotBlank(logs)) {
                reportingManager.updateTestLog(Status.INFO, logs, false);
            }
        } else {
            updateBatchHitResult(timeTaken, resultText, Status.WARNING);
        }
    }

    /**
     * This method is used to update batch result
     * 
     * @param timeTaken - This parameter is of String data type
     * @param resultText - This parameter is of String data type
     * @param status - This parameter is of Status type
     */
    private void updateBatchHitResult(String timeTaken, String resultText, Status status) {

        reportingManager
                .updateTestLog(status,
                        FrameworkConstants.HTMLFormat.FIELD + resultText + FrameworkConstants.HTMLFormat.CLOSE + " in "
                                + FrameworkConstants.HTMLFormat.VALUE + timeTaken + FrameworkConstants.HTMLFormat.CLOSE,
                        true);
    }

    /**
     * This method is used to execute post batch job actions
     * 
     * @param timeTaken - This parameter is of String data type
     * @param resultText - This parameter is of String data type
     * @param status - This parameter is of Status type
     */

    private void executePostBatchJobActions(String batchJobName, String baseFileName) {
        int rowCount = reader.getRowCount(reader.scriptSheetName);
        File file = null;
        BatchFileReader batchFileReader = null;
        String layoutType = null;
        String layoutSheetName = null;
        boolean recordFound = false;
        SQLActions sqlAssert = null;

        for (int i = 1; i <= rowCount; i++) {
            String actionType = null;
            String fieldName = null;
            String lengthDepth = null;
            String value = null;
            String expectedValue = null;

            if (StringUtils.equals(testCaseId, reader.getScriptCellData(0, i))
                    && (StringUtils.equals("Post", reader.getScriptCellData(1, i)))
                    && (StringUtils.equals(batchJobName, reader.getScriptCellData(2, i)))) {
                actionType = reader.getScriptCellData(3, i);
                fieldName = reader.getScriptCellData(4, i);
                lengthDepth = reader.getScriptCellData(5, i);
                value = reader.getScriptCellData(6, i);
                expectedValue = reader.getScriptCellData(7, i);

                switch (actionType) {
                case "Download File":
                    if (StringUtils.equalsIgnoreCase("FTP", TestSettings.goAnywhereAccessMethod)) {
                        GoAnywhereFTPUtility goAnywhereUtility = new GoAnywhereFTPUtility(threadEntities);
                        file = goAnywhereUtility.downloadFileFromOutbound(baseFileName, testCaseId);
                    } else if (StringUtils.equalsIgnoreCase("WebUI", TestSettings.goAnywhereAccessMethod)) {
                        GoAnywhereWebUtility goAnywhereUtility = new GoAnywhereWebUtility(threadEntities);
                        file = goAnywhereUtility.downloadFileFromOutbound(baseFileName, testCaseId);
                    } else {
                        ErrorHandler.testError(ErrLvl.FATAL, null,
                                "Unaccepted Go Anywhere Access Method: " + TestSettings.goAnywhereAccessMethod,
                                testCaseVariables);
                    }

                    if (null == file) {
                        reportingManager.updateTestLog(Status.FATAL, "File was not downloaded successfully!", false);
                    }

                    String fileName = file.getName();

                    layoutType = fileName.substring(fileName.lastIndexOf(".")).toUpperCase();

                    break;

                case "Open File":
                    try {
                        if (null != file) {
                            if (!validLayouts.contains(layoutType)) {
                                ErrorHandler.testError(ErrLvl.FATAL, null,
                                        layoutType + " is not an accepted Layout Type for reading.", testCaseVariables);
                            }
                            batchFileReader = new BatchFileReader(new FileReader(file));
                        } else {
                            ErrorHandler.testError(ErrLvl.FATAL, null, "No file downloaded!", testCaseVariables);
                        }
                    } catch (FileNotFoundException e1) {
                        ErrorHandler.testError(ErrLvl.FATAL, e1, "File not found: " + file.getAbsolutePath(),
                                testCaseVariables);
                    }
                    break;

                case "Check Record":
                    layoutSheetName = value;

                    if (layoutType.equals("TXT")) {
                        recordFound = batchFileReader.checkIfTXTRecordPresent(expectedValue);
                    } else if (layoutType.equals("XML")) {
                        String parentTag = reader.getActiveWorkbook().getSheet(layoutSheetName).getRow(1).getCell(0)
                                .getStringCellValue();

                        recordFound = batchFileReader.checkIfXMLRecordPresent(parentTag, expectedValue);
                    }

                    if (recordFound) {
                        reportingManager.updateTestLog(Status.PASS, "Expected record found in the generated file!",
                                false);
                    } else {
                        reportingManager.updateTestLog(Status.FAIL, "Expected record not found in the generated file!",
                                false);
                    }

                    break;

                case "Assert File Value":

                    if (recordFound) {

                    }

                    break;

                case "Assert DB":
                    sqlAssert = new SQLActions(threadEntities);
                    sqlAssert.assertDB(value, expectedValue, Status.FAIL);
                    break;

                case "Assert DB Fatally":
                    sqlAssert = new SQLActions(threadEntities);
                    sqlAssert.assertDB(value, expectedValue, Status.FATAL);
                    break;

                case "Set Global":

                    TestDataManager tdm = new TestDataManager(threadEntities);
                    String varName = null;
                    String varVal = null;

                    if (value.startsWith(ValueParser.GLOBAL_KEY)) {
                        value = value.substring(ValueParser.GLOBAL_KEY.length());
                    }

                    int equationIndex = value.indexOf("=");

                    if (equationIndex == -1) {
                        String errorMsg = "Global Variable for " + value
                                + " is not being assigned any value for SetGlobal action! Please write in this format: 'GlobalVariable.Variable Name=Value'";
                        ErrorHandler.testError(ErrLvl.ERROR, null, errorMsg, testCaseVariables);
                        break;
                    }

                    varName = value.substring(0, equationIndex).trim();
                    varVal = value.substring(equationIndex + 1).trim();

                    varVal = parser.checkParseabilityAndParse(varVal);

                    tdm.setTestGlobal(varName, varVal);

                    break;

                default:
                    ErrorHandler.testError(ErrLvl.ERROR, null,
                            actionType + " is an invalid action type for Post-Batch execution!", testCaseVariables);
                }

            }
        }
    }

}
