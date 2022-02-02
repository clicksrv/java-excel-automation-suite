package frameworkcore.reportutilities;

import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.aventstack.extentreports.Status;

import frameworkcore.testthread.TestParameters;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class TestReporter extends ExcelReportUtility {

    public TestReporter(TestParameters testParameters) {
        super(testParameters);
    }

    private String currentNode = "";

    @Override
    protected void createThreadReportHeader() {
        XSSFRow rowHeader = sheet.createRow(2);

        rowHeader.createCell(0, CellType.STRING).setCellValue("Timestamp");
        rowHeader.createCell(1, CellType.STRING).setCellValue("Node");
        rowHeader.createCell(2, CellType.STRING).setCellValue("Status");
        rowHeader.createCell(3, CellType.STRING).setCellValue("Activity");
        rowHeader.createCell(4, CellType.STRING).setCellValue("Screenshot Path");
        // rowHeader.createCell(5, CellType.STRING).setCellValue("Test_Suite");
    }

    private String getComputerName() {
        Map<String, String> env = System.getenv();

        if (env.containsKey("COMPUTERNAME")) {
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            return env.get("HOSTNAME");
        } else {
            return "Unknown Computer";
        }
    }

    @Override
    protected void createThreadInfoRows() {
        XSSFRow rowHeader = sheet.createRow(0);
        XSSFRow rowData = sheet.createRow(1);

        rowHeader.createCell(0, CellType.STRING).setCellValue("Browser");
        rowData.createCell(0, CellType.STRING).setCellValue(testParameters.getBrowser().toString().toUpperCase());

        rowHeader.createCell(1, CellType.STRING).setCellValue("Platform");
        rowData.createCell(1, CellType.STRING).setCellValue(System.getProperty("os.name"));

        rowHeader.createCell(2, CellType.STRING).setCellValue("HostName");
        rowData.createCell(2, CellType.STRING).setCellValue(getComputerName());

        rowHeader.createCell(3, CellType.STRING).setCellValue("Execution Started");
        execStartVal = rowData.createCell(3, CellType.STRING);
        execStartVal.setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));

        rowHeader.createCell(4, CellType.STRING).setCellValue("Execution Finished");
        execFinishVal = rowData.createCell(4, CellType.STRING);

        rowHeader.createCell(5, CellType.STRING).setCellValue("Elapsed Time");
        elapsedTimeVal = rowData.createCell(5, CellType.STRING);

        rowHeader.createCell(6, CellType.STRING).setCellValue("Suite");
        rowData.createCell(6, CellType.STRING).setCellValue(testParameters.getCurrentTestSuite());

    }

    public void createNode(String keyword) {

        currentNode = keyword;
    }

    public void createStepWithScreenshot(Status status, String actionPerformed, String scrshotPath) {
        XSSFRow row = sheet.createRow(currentRow);

        row.createCell(0, CellType.STRING).setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));
        row.createCell(1, CellType.STRING).setCellValue(currentNode);
        row.createCell(2, CellType.STRING).setCellValue(status.toString().toUpperCase());
        row.createCell(3, CellType.STRING).setCellValue(actionPerformed.replaceAll("\n", "<br>"));
        row.createCell(4, CellType.STRING).setCellValue(scrshotPath);
        // row.createCell(5,
        // CellType.STRING).setCellValue(testParameters.getCurrentTestSuite());

        currentRow++;
    }

    public void createStep(Status status, String actionPerformed) {

        boolean newRow = !currentRowContainsOnlyScreenshot();

        XSSFRow row;

        if (newRow) {
            row = sheet.createRow(currentRow);
            row.createCell(0, CellType.STRING).setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));
        } else {
            row = sheet.getRow(currentRow);
        }
        row.createCell(1, CellType.STRING).setCellValue(currentNode);
        row.createCell(2, CellType.STRING).setCellValue(status.toString().toUpperCase());
        row.createCell(3, CellType.STRING).setCellValue(actionPerformed.replaceAll("\n", "<br>"));
        row.createCell(4, CellType.STRING).setCellValue("");
        // row.createCell(5,
        // CellType.STRING).setCellValue(testParameters.getCurrentTestSuite());

        currentRow++;
    }

    public void captureShot(String scrshotPath) {
        XSSFRow row = sheet.createRow(currentRow);

        row.createCell(0, CellType.STRING).setCellValue("'" + getCurrentTimeInFormat(TIME_FORMAT));
        row.createCell(4, CellType.STRING).setCellValue(scrshotPath);

    }

    private boolean currentRowContainsOnlyScreenshot() {
        try {
            XSSFRow row = sheet.getRow(currentRow);

            boolean scrShotPresent = !row.getCell(4).getStringCellValue().isEmpty();
            boolean timeStamped = !row.getCell(0).getStringCellValue().isEmpty();
            boolean noActionPerformed = row.getCell(3).getStringCellValue().isEmpty();
            boolean noStatusPresent = row.getCell(2).getStringCellValue().isEmpty();
            boolean noNodePresent = row.getCell(1).getStringCellValue().isEmpty();

            if (scrShotPresent && timeStamped && noActionPerformed && noStatusPresent && noNodePresent) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

}
