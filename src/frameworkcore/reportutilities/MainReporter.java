package frameworkcore.reportutilities;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;

import frameworkcore.FrameworkStatics.FrameworkSettings;
import frameworkcore.FrameworkStatics.TestSettings;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class MainReporter extends ExcelReportUtility {

    public MainReporter() {
        super();
    }

    @Override
    protected void createThreadReportHeader() {
        XSSFRow rowHeader = sheet.createRow(2);

        rowHeader.createCell(0, CellType.STRING).setCellValue("Timestamp");
        rowHeader.createCell(1, CellType.STRING).setCellValue("Source");
        rowHeader.createCell(2, CellType.STRING).setCellValue("Error Message");
        rowHeader.createCell(3, CellType.STRING).setCellValue("Error Stack Trace");
        rowHeader.createCell(4, CellType.STRING).setCellValue("Cause");
    }

    @Override
    protected void createThreadInfoRows() {
        XSSFRow rowHeader = sheet.createRow(0);
        XSSFRow rowData = sheet.createRow(1);

        rowHeader.createCell(0, CellType.STRING).setCellValue("Environment");
        rowData.createCell(0, CellType.STRING).setCellValue(TestSettings.currentEnvironment);

        rowHeader.createCell(1, CellType.STRING).setCellValue("BuildVersion");
        rowData.createCell(1, CellType.STRING).setCellValue("'" + TestSettings.currentBuildNumber);

        rowHeader.createCell(2, CellType.STRING).setCellValue("Parallel Threads Allowed");
        rowData.createCell(2, CellType.STRING).setCellValue("'" + String.valueOf(FrameworkSettings.noOfThreads));

        rowHeader.createCell(3, CellType.STRING).setCellValue("Execution Started");
        execStartVal = rowData.createCell(3, CellType.STRING);
        execStartVal.setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));

        rowHeader.createCell(4, CellType.STRING).setCellValue("Execution Finished");
        execFinishVal = rowData.createCell(4, CellType.STRING);

        rowHeader.createCell(5, CellType.STRING).setCellValue("Elapsed Time");
        elapsedTimeVal = rowData.createCell(5, CellType.STRING);
    }

    public void addThreadReportEntry(String source, String errorMsg, String errorStack, String errorCause) {
        XSSFRow row = sheet.createRow(currentRow);

        row.createCell(0, CellType.STRING).setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));
        row.createCell(1, CellType.STRING).setCellValue(source);
        row.createCell(2, CellType.STRING).setCellValue(errorMsg);
        row.createCell(3, CellType.STRING).setCellValue(errorStack);
        row.createCell(4, CellType.STRING).setCellValue(errorCause);

        currentRow++;
    }

}
