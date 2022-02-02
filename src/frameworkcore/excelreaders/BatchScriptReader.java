package frameworkcore.excelreaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.FrameworkConstants;
import frameworkcore.FrameworkStatics;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class BatchScriptReader extends TestThread {

	private String finalPath;
	private OPCPackage pkg = null;
	private XSSFWorkbook workbook = null;
	private XSSFSheet scriptSheet = null;
	private XSSFSheet batchListSheet = null;
	public final String scriptSheetName = "Script";
	public final String batchListSheetName = "JobStreamDefinition";
	public String layoutSheetName = null;
	private String dataSheetPath = null;
	private DataFormatter formatter = new DataFormatter();

	public BatchScriptReader(String workbookName, ThreadEntities threadEntities) {

		super(threadEntities);

		dataSheetPath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + "scripts"
				+ FrameworkEntities.fileSeparator + "batch_scripts" + FrameworkEntities.fileSeparator
				+ FrameworkConstants.FrameworkPaths.BATCH_DATASHEET;

		finalPath = (new StringBuilder(String.valueOf(dataSheetPath))).append(workbookName).append(".xlsm").toString();

		openWorkbook(finalPath);

		batchListSheet = workbook.getSheet(batchListSheetName);
		scriptSheet = workbook.getSheet(scriptSheetName);

	}

	/**
	 * This method is used to get active workbook
	 * 
	 * @return workbook
	 */
	public XSSFWorkbook getActiveWorkbook() {
		return workbook;
	}

	/**
	 * This method is used to open the workbook
	 * 
	 * @param filePath - This parameter is of String type
	 */
	private void openWorkbook(String filePath) {
		try {
			ZipSecureFile.setMinInflateRatio(0);
			pkg = OPCPackage.open(filePath, PackageAccess.READ);
			workbook = new XSSFWorkbook(pkg);
		} catch (InvalidOperationException | FileNotFoundException e) {
			FrameworkStatics.unmarkActiveBatchJobStream(testCaseVariables);
			ErrorHandler.testError(ErrLvl.FATAL, e, "File not found at: " + filePath, testCaseVariables);
		} catch (IOException e) {
			FrameworkStatics.unmarkActiveBatchJobStream(testCaseVariables);
			ErrorHandler.testError(ErrLvl.FATAL, e, "Unable to open file at: " + filePath, testCaseVariables);
		} catch (InvalidFormatException e) {
			FrameworkStatics.unmarkActiveBatchJobStream(testCaseVariables);
			ErrorHandler.testError(ErrLvl.FATAL, e, "File Format is invalid at: " + filePath, testCaseVariables);
		}
	}

	/**
	 * This method is used to close the open work books
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
			ErrorHandler.testError(ErrLvl.FATAL, e, "Unable to close file at: " + finalPath, testCaseVariables);
		}

	}

	/**
	 * This method is used to get functional area
	 */
	public String getFunctionalArea() {
		return getBatchCellData(0, 1);
	}

	/**
	 * This method is used to get the list of batches
	 * 
	 * @return batch list
	 */
	public LinkedHashMap<String, ArrayList<Object>> getBatchList() {

		int blRowNum = 0;
		LinkedHashMap<String, ArrayList<Object>> batchList = new LinkedHashMap<String, ArrayList<Object>>();

		while (StringUtils.isNotBlank(getBatchCellData(1, ++blRowNum))) {

			ArrayList<Object> parameters = new ArrayList<>();

			String batchJobName = getBatchCellData(1, blRowNum);// batchListSheet.getRow(blRowNum).getCell(1).getStringCellValue();

			if (StringUtils.startsWith(batchJobName, "Load") || StringUtils.startsWith(batchJobName, "Write")) {
				String fileName = getBatchCellData(2, blRowNum);
				parameters.add(fileName);
				batchList.put(batchJobName, parameters);
			} else {
				batchList.put(batchJobName, null);
			}

		}

		return batchList;
	}

	/**
	 * This method is used to get the data required for batch scripts
	 * 
	 * @param colNum - This parameter is of integer data type
	 * @param rowNum - This parameter is of integer data type
	 * @return cell value
	 */
	public String getBatchCellData(int colNum, int rowNum) {
		try {
			return formatter.formatCellValue((batchListSheet).getRow(rowNum).getCell(colNum)).trim();
		} catch (Exception e) {
			return "";
		}

	}

	/**
	 * This method is used to get the data required for scripts
	 * 
	 * @param colNum - This parameter is of integer data type
	 * @param rowNum - This parameter is of integer data type
	 * @return cell value
	 */
	public String getScriptCellData(int colNum, int rowNum) {
		try {
			return formatter.formatCellValue((scriptSheet).getRow(rowNum).getCell(colNum));
		} catch (Exception e) {
			return "";
		}

	}

	/**
	 * This method is used to get the total row count
	 * 
	 * @params Argument : sheetName - This parameter is of String type
	 * @return total number of rows
	 */
	public int getRowCount(String sheetName) {
		int index = workbook.getSheetIndex(sheetName);
		if (index == -1) {
			return 0;
		} else {
			scriptSheet = workbook.getSheetAt(index);
			int number = scriptSheet.getLastRowNum() + 1;
			return number;
		}

	}
}
