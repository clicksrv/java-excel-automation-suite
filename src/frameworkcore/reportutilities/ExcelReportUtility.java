package frameworkcore.reportutilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.FrameworkConstants.StandardFormats;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestParameters;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public abstract class ExcelReportUtility {

    protected static final String TIME_FORMAT = StandardFormats.TIME_FORMAT;

    protected TestParameters testParameters = null;

    protected String threadName = null;
    protected String fileAbsolutePath = null;

    protected XSSFWorkbook workbook = null;
    protected XSSFSheet sheet = null;
    protected XSSFRow dataRow = null;

    protected XSSFCell execStartVal = null;
    protected XSSFCell execFinishVal = null;
    protected XSSFCell elapsedTimeVal = null;

    protected Integer currentRow = 3;

    protected File excelFolder;

    /**
     * Constructor to create the thread reporting instance.
     * @param testParameters Test Parameters object
     */
    public ExcelReportUtility(TestParameters testParameters) {

        this.testParameters = testParameters;
        threadName = testParameters.getCurrentTestCaseOutput();

        startReport();
    }

    public ExcelReportUtility() {

        threadName = "Main";
        startReport();
    }

    private void startReport() {
        if (null == FrameworkPaths.excelReportsPath) {
            FrameworkPaths.excelReportsPath = FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator
                    + "ExcelReports" + FrameworkEntities.fileSeparator;
        }

        excelFolder = new File(FrameworkPaths.excelReportsPath);

        if (!excelFolder.isDirectory()) {
            excelFolder.mkdir();
        }

        fileAbsolutePath = excelFolder + FrameworkEntities.fileSeparator + threadName + ".xlsx";

        createNewFileWithWritingSheet(threadName);

        createThreadReportHeader();
        createThreadInfoRows();
    }

    protected abstract void createThreadInfoRows();

    protected abstract void createThreadReportHeader();

    protected String getCurrentTimeInFormat(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    protected String getTimeElapsed(String startTime, String finishTime, String tsFormat) {
        SimpleDateFormat timerDateFormat = new SimpleDateFormat(TIME_FORMAT);

        try {
            TimeCalculator time = new TimeCalculator();
            return time.getTimeDifference(timerDateFormat, startTime.substring(1), finishTime.substring(1));
        } catch (Exception e) {
            e.printStackTrace();
            String timeDifference = "";
            return timeDifference;
        }

    }

    public void finishReport() {

        execFinishVal.setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));
        elapsedTimeVal.setCellValue("'"
                + getTimeElapsed(execStartVal.getStringCellValue(), execFinishVal.getStringCellValue(), TIME_FORMAT));

        writeAndCloseFile(fileAbsolutePath, workbook);
    }

    /**
     * This method is used to create new excel sheet
     * 
     * @param Argument : testCaseName
     */
    protected void writeAndCloseFile(String fileAbsolutePath, XSSFWorkbook workbook) {

        File file = new File(fileAbsolutePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            if (null != workbook) {
                workbook.write(outputStream);
                workbook.close();
            }
        } catch (IOException e) {
            String errorMsg = "Failed to complete write for '" + threadName + "' at '" + fileAbsolutePath + "'!";
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * This method is used to create new excel sheet
     * 
     * @param Argument : testCaseName
     */
    private void createNewFileWithWritingSheet(String testCaseName) {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet(testCaseName);
    }

}
