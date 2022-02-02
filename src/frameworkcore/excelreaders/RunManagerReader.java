package frameworkcore.excelreaders;

import java.io.IOException;
import java.util.Properties;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.errormanagers.TestException;
import frameworkcore.testthread.TestCaseVariables;

/**
 * @author Rahul Shaw
 */
public class RunManagerReader {

    private String filePath;
    private String fileName;
    private String absoluteFilePath;
    private String dataSheetName;
    private XSSFWorkbook workbook = null;

    Properties properties = FrameworkPropertyFiles.frameworkProperties;

    private DataFormatter formatter = new DataFormatter();

    private String errors = "";

    private TestCaseVariables testCaseVariables;

    /**
     * This method is used to get the datasheet name
     * 
     * @return sheetname
     */
    public String getDatasheetName() {
        return dataSheetName;
    }

    /**
     * This method is used to set the datasheet name
     * 
     * @params Argument : datasheetName - This parameter is of String data type
     */
    public void setDatasheetName(String datasheetName) {
        dataSheetName = datasheetName;
    }

    public RunManagerReader(String filePath, String fileName, TestCaseVariables testCaseVariables) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.testCaseVariables = testCaseVariables;
    }

    public RunManagerReader(String absoluteFilePath, TestCaseVariables testCaseVariables) {
        this.absoluteFilePath = absoluteFilePath;
        this.testCaseVariables = testCaseVariables;
    }

    public RunManagerReader(XSSFWorkbook workbook, TestCaseVariables testCaseVariables) {
        this.workbook = workbook;
        this.testCaseVariables = testCaseVariables;
    }

    /**
     * This method is used to check pre conditions
     * 
     */
    private void checkPreRequisites() {
        errors = "";

        if (dataSheetName == null) {
            String errorMsg = "DataTable.dataSheetName is not set!";
            processError(errorMsg);
        }

        if (!errors.isEmpty()) {
            throw new TestException(errors);
        }
    }

    /**
     * This method is used to process error
     * 
     * @params Argument : errorMsg - This parameter is of String data type
     */
    private void processError(String errorMsg) {
        System.err.println(errorMsg);
        if (errors.isEmpty()) {
            errors = errors + errorMsg;
        } else {
            errors = errors + "\n" + errorMsg;
        }
    }

    /**
     * This method is used to get workbook
     * 
     * @return file
     */
    public XSSFWorkbook getWorkbook() {
        return openFileForReading();
    }

    /**
     * This method is used to open workbook
     * 
     * @return workbook
     */
    private XSSFWorkbook openFileForReading() {

        if (workbook == null) {

            if (absoluteFilePath == null) {
                absoluteFilePath = (new StringBuilder(String.valueOf(filePath))).append(FrameworkEntities.fileSeparator)
                        .append(fileName).append(".xlsm").toString();
            }

            OPCPackage pkg = null;

            try {
                pkg = OPCPackage.open(absoluteFilePath, PackageAccess.READ);
            } catch (InvalidOperationException e) {
                String errorMsg = (new StringBuilder("Invalid operation performed on file: \""))
                        .append(absoluteFilePath).toString();
                if (null != testCaseVariables) {
                    ErrorHandler.testError(ErrLvl.FATAL, e, errorMsg, testCaseVariables);
                } else {
                    ErrorHandler.testError(ErrLvl.FATAL, e, errorMsg, testCaseVariables);
                }
            } catch (InvalidFormatException e) {
                String errorMsg = (new StringBuilder("The specified file \"")).append(absoluteFilePath)
                        .append("\" is of invalid format!").toString();
                if (null != testCaseVariables) {
                    ErrorHandler.testError(ErrLvl.FATAL, e, errorMsg, testCaseVariables);
                } else {
                    ErrorHandler.testError(ErrLvl.FATAL, e, errorMsg, testCaseVariables);
                }
            }
            try {
                workbook = new XSSFWorkbook(pkg);
            } catch (IOException e) {
                e.printStackTrace();
                String errorMsg = (new StringBuilder("Error while opening the specified Excel workbook \""))
                        .append(absoluteFilePath).append("\"").toString();
                if (null != testCaseVariables) {
                    ErrorHandler.testError(ErrLvl.FATAL, e, errorMsg, testCaseVariables);
                } else {
                    ErrorHandler.testError(ErrLvl.FATAL, e, errorMsg, testCaseVariables);
                }
            }

        }

        return workbook;
    }

    /**
     * This method is used to get workbook
     * 
     * @param Argument : workbook - This parameter is of XSSFWorkbook type
     * @return worksheet
     */
    private XSSFSheet getWorkSheet(XSSFWorkbook workbook) {
        XSSFSheet worksheet = workbook.getSheet(dataSheetName);
        if (worksheet == null) {

            String errorMsg = (new StringBuilder("The specified sheet \"")).append(dataSheetName)
                    .append("\" does not exist within the workbook \"").append(absoluteFilePath).append("\"")
                    .toString();

            if (null != testCaseVariables) {
                ErrorHandler.testError(ErrLvl.FATAL, null, errorMsg, testCaseVariables);
            } else {
                ErrorHandler.testError(ErrLvl.FATAL, null, errorMsg, testCaseVariables);
            }

            return null;
        } else {
            return worksheet;
        }
    }

    /**
     * This method is used to get cell value
     * 
     * @param Argument 1: worksheet - This parameter is of XSSFSheet type
     * @param Argument 2: rowNum - This parameter is of integer data type
     * @param Argument 3: columnNum - This parameter is of integer data type
     * @return cell value
     */
    private String getCellValue(XSSFSheet worksheet, int rowNum, int columnNum) {

        String cellValue;

        try {
            cellValue = formatter.formatCellValue((worksheet).getRow(rowNum).getCell(columnNum));
        } catch (Exception e) {
            cellValue = "";
        }

        return cellValue;
    }

    /**
     * This method is used to get cell value
     * 
     * @param Argument 1: worksheet - This parameter is of XSSFSheet type
     * @param Argument 2: row - This parameter is of XSSFRow type
     * @param Argument 3: columnNum - This parameter is of integer data type
     * @return cell value
     */
    private String getCellValue(XSSFSheet worksheet, XSSFRow row, int columnNum) {
        XSSFCell cell = row.getCell(columnNum);
        String cellValue;
        if (cell == null) {
            cellValue = "";
        } else {
            cellValue = cell.getStringCellValue().trim();
        }
        return cellValue;
    }

    /**
     * This method is used to get row number
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @param Argument 3: startRowNum - This parameter is of integer data type
     * @return row number
     */
    public int getRowNum(String key, int columnNum, int startRowNum) {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        for (int currentRowNum = startRowNum; currentRowNum <= worksheet.getLastRowNum(); currentRowNum++) {
            String currentValue = getCellValue(worksheet, currentRowNum, columnNum);
            if (currentValue.equals(key)) {
                return currentRowNum;
            }
        }

        return -1;
    }

    /**
     * This method is used to get row number
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @param Argument 3: startRowNum - This parameter is of integer data type
     * @param Argument 4: fragmentName - This parameter is of String type
     * @return row number
     */
    public int getRowNum(String key, int columnNum, int startRowNum, String fragmentName) {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        for (int currentRowNum = startRowNum; currentRowNum <= worksheet.getLastRowNum(); currentRowNum++) {
            String currentValue = getCellValue(worksheet, currentRowNum, columnNum);
            String fragmentValue = getCellValue(worksheet, currentRowNum, columnNum + 1);

            if (currentValue.equals(key) && fragmentValue.equals(fragmentName)) {
                return currentRowNum;
            }
        }

        return -1;
    }

    /**
     * This method is used to get row number
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @return row number
     */
    public int getRowNum(String key, int columnNum) {
        return getRowNum(key, columnNum, Integer.parseInt(properties.getProperty("DataTableValueStartRow")));
    }

    /**
     * This method is used to get row number
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @param Argument 3: fragmentName - This parameter is of String type
     * @return row number
     */
    public int getRowNum(String key, int columnNum, String fragmentName) {
        return getRowNum(key, columnNum, Integer.parseInt(properties.getProperty("DataTableValueStartRow")),
                fragmentName);
    }

    /**
     * This method is used to get row number
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @return business flow row number
     */
    public int getBFlowRowNum(String key, int columnNum) {
        return getRowNum(key, columnNum, Integer.parseInt(properties.getProperty("BusinessFlowValueStartRow")));
    }

    /**
     * This method is used to get last row number
     * 
     * @return last row number
     */
    public int getLastRowNum() {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        return worksheet.getLastRowNum();
    }

    /**
     * This method is used to get total row count
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @param Argument 3: startRowNum - This parameter is of integer data type
     * @return row count
     */
    public int getRowCount(String key, int columnNum, int startRowNum) {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        int rowCount = 0;
        Boolean keyFound = Boolean.valueOf(false);
        for (int currentRowNum = startRowNum; currentRowNum <= worksheet.getLastRowNum(); currentRowNum++) {
            String currentValue = getCellValue(worksheet, currentRowNum, columnNum);
            if (currentValue.equals(key)) {
                rowCount++;
                keyFound = Boolean.valueOf(true);
                continue;
            }
            if (keyFound.booleanValue()) {
                break;
            }
        }

        return rowCount;
    }

    /**
     * This method is used to get total row count
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @return row count
     */
    public int getRowCount(String key, int columnNum) {
        return getRowCount(key, columnNum, 0);
    }

    /**
     * This method is used to get total row count
     * 
     * @param Argument 1: key - This parameter is of String type
     * @param Argument 2: rowNum - This parameter is of integer data type
     * @return column number
     */
    public int getColumnNum(String key, int rowNum) {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        XSSFRow row = worksheet.getRow(rowNum);
        for (int currentColumnNum = 0; currentColumnNum < row.getLastCellNum(); currentColumnNum++) {
            String currentValue = getCellValue(worksheet, row, currentColumnNum);
            if (currentValue.equals(key)) {
                return currentColumnNum;
            }
        }

        return -1;
    }

    /**
     * This method is used to get value
     * 
     * @param Argument 1: rowNum - This parameter is of integer data type
     * @param Argument 2: columnNum - This parameter is of integer data type
     * @return value
     */
    public String getValue(int rowNum, int columnNum) {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        String cellValue = getCellValue(worksheet, rowNum, columnNum);
        return cellValue;
    }

    /**
     * This method is used to get value
     * 
     * @param Argument 1: rowNum - This parameter is of integer data type
     * @param Argument 2: columnHeader - This parameter is of String type
     * @return value
     */
    public String getValue(int rowNum, String columnHeader) {
        checkPreRequisites();
        XSSFWorkbook workbook = openFileForReading();
        XSSFSheet worksheet = getWorkSheet(workbook);
        XSSFRow row = worksheet.getRow(0);
        int columnNum = -1;
        for (int currentColumnNum = 0; currentColumnNum < row.getLastCellNum(); currentColumnNum++) {
            String currentValue = getCellValue(worksheet, row, currentColumnNum);
            if (!currentValue.equals(columnHeader)) {
                continue;
            }
            columnNum = currentColumnNum;
            break;
        }

        if (columnNum == -1) {
            String errorMsg = (new StringBuilder("The specified column header \"")).append(columnHeader)
                    .append("\" is not found in the sheet \"").append(dataSheetName).append("\"!").toString();

            if (null != testCaseVariables) {
                ErrorHandler.testError(ErrLvl.FATAL, null, errorMsg, testCaseVariables);
            } else {
                ErrorHandler.testError(ErrLvl.FATAL, null, errorMsg, testCaseVariables);
            }
            return null;
        } else {
            String cellValue = getCellValue(worksheet, rowNum, columnNum);
            return cellValue;
        }
    }
}
