package frameworkcore.excelreaders;

import java.io.Closeable;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public abstract class ExcelReader implements AutoCloseable, Closeable {

    private OPCPackage pkg = null;
    private String excelFilePath = null;

    private XSSFWorkbook workbook = null;
    private XSSFSheet sheet = null;

    private DataFormatter formatter = new DataFormatter();

    public ExcelReader(String excelFilePath, String sheetName) {

        try {
            openWorkbook(excelFilePath);
        } catch (InvalidOperationException | InvalidFormatException | IOException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Failed to load workbook at: " + excelFilePath);
        }
        if (null != sheetName) {
            loadWorksheet(sheetName);
        }

    }

    /**
     * This method is used to open a workbook. Follow this with
     * loadSheet(index/sheetName) method to load a sheet. Finally use
     * closeWorkbook() to close the workbook.
     * 
     * @throws InvalidFormatException
     * @throws InvalidOperationException
     * @throws IOException
     * @param filePath - This parameter is of String type
     */
    private void openWorkbook(String excelFilePath)
            throws InvalidOperationException, InvalidFormatException, IOException {
        this.excelFilePath = excelFilePath;
        ZipSecureFile.setMinInflateRatio(0);
        pkg = OPCPackage.open(excelFilePath, PackageAccess.READ);
        workbook = new XSSFWorkbook(pkg);
    }

    /**
     * This method is used to close the workbook
     */
    public void closeWorkbook() {
        try {
            if (null != workbook) {
                workbook.close();
            }
            if (null != pkg) {
                pkg.close();
            }
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Unable to close file at: " + excelFilePath);
        }
    }

    @Override
    public void close() {
        closeWorkbook();
    }

    /**
     * Use this method load a sheet by Name in the workbook to the ExcelReader Class
     * 
     * @param sheetName is the name of the sheet.
     * @throws IllegalArgumentException if index is not available on the workbook.
     */
    public void loadWorksheet(String sheetName) throws IllegalArgumentException {
        int index = workbook.getSheetIndex(sheetName);
        if (index == -1) {
            throw new IllegalArgumentException(sheetName + " is not present in workbook!");
        }
        sheet = workbook.getSheetAt(index);
    }

    /**
     * Use this method load a sheet by index in the workbook to the ExcelReader
     * Class
     * 
     * @param index is the index of the sheet.
     * @throws IllegalArgumentException if index is not available on the workbook.
     */
    public void loadWorksheet(int index) throws IllegalArgumentException {
        if (null == sheet) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null, "No sheet loaded!");
        }

        sheet = workbook.getSheetAt(index);
    }

    /**
     * This method is used to get row count
     * 
     * @return row count
     */
    public int getRowCount() {
        if (null == sheet) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null, "No sheet loaded!");
        }

        int number = sheet.getLastRowNum() + 1;
        return number;
    }

    /**
     * This method is used to get cell value force formatted as String. Sheet must
     * be loaded.
     * 
     * @param Argument : rowNum - This parameter is of integer data type
     * @param Argument : colNum - This parameter is of integer data type
     * @return
     */
    public String getCellValue(int rowNum, int colNum) {

        if (null == sheet) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null, "No sheet loaded!");
        }
        try {
            return formatter.formatCellValue((sheet).getRow(rowNum).getCell(colNum)).trim();
        } catch (Exception e) {
            return "";
        }
    }
}
