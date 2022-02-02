package frameworkcore.excelreaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.FrameworkConstants;
import frameworkcore.FrameworkStatics;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestParameters;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ScriptReader {

	private String finalPath;
	private OPCPackage pkg = null;
	private XSSFWorkbook scriptWorkbook = null;
	private XSSFSheet scriptSheet = null;
	private String testScriptName = null;
	public String scriptTabName = null;
	private String scriptSheetPath = null;
	Properties properties = FrameworkPropertyFiles.frameworkProperties;
	private DataFormatter formatter = new DataFormatter();

	public ScriptReader(TestParameters testParameters) {

		testScriptName = testParameters.getCurrentTestScript();

		String scriptFileName = "Scripts_" + testParameters.getCurrentTestSuite();

		scriptSheetPath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + "scripts"
				+ FrameworkEntities.fileSeparator;

		finalPath = (new StringBuilder(String.valueOf(scriptSheetPath))).append(scriptFileName).append(".xlsx")
				.toString();

		openWorkbook(finalPath);

		scriptSheet = scriptWorkbook.getSheet(testScriptName);

		if (null == scriptSheet) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, null,
					"'" + testScriptName + "' was not found in '" + scriptFileName + ".xlsx'");
		}

	}

	public ScriptReader(String flowName) {

		String scriptFileName = FrameworkConstants.FrameworkPaths.PRE_DEFINED_FLOWS;

		finalPath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + scriptFileName;

		openWorkbook(finalPath);

		scriptSheet = scriptWorkbook.getSheet(flowName);

	}

	/**
	 * This method is used to open workbook
	 * 
	 * @param Argument : filePath - This parameter is of String type
	 */
	private void openWorkbook(String filePath) {
		try {
			pkg = OPCPackage.open(filePath, PackageAccess.READ);
			scriptWorkbook = new XSSFWorkbook(pkg);
		} catch (FileNotFoundException e) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, "File not found at: " + filePath);
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Unable to open file at: " + filePath);
		} catch (InvalidFormatException e) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, "File Format is invalid at: " + filePath);
		}
	}

	/**
	 * This method is used to close workbook
	 */
	public void closeWorkbook() {
		try {
			if (null != scriptWorkbook) {
				scriptWorkbook.close();
			}
			if (null != pkg) {
				pkg.close();
			}
		} catch (IOException e) {
			ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Unable to close file at: " + finalPath);
		}

	}

	/**
	 * This method is used to get 1st steps of dataset
	 * 
	 * @param Argument : dataSet - This parameter is of String type
	 * @return start step
	 */
	public int getDataSetStartStep(String dataSet) {
		int i;

		int lastRow = getRowCount();

		for (i = 1; i <= lastRow; i++) {
			if (readStep(i, 0).replaceAll("[(\\‚)]", ",").equals(dataSet.replaceAll("[(\\‚)]", ","))) {
				break;
			} else if (i == lastRow) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, null,
						dataSet + " Data set was not found for Pre-Defined Flow " + scriptSheet.getSheetName());
			}
		}

		return i + 1;
	}

	/**
	 * This method is used to get last steps of dataset
	 * 
	 * @param Argument : dataSet - This parameter is of String type
	 * @return end step
	 */
	public int getDataSetEndStep(String dataSet) {
		int i;

		int lastRow = getRowCount();

		for (i = lastRow; i >= 1; i--) {
			if (readStep(i, 0).replaceAll("[(\\‚)]", ",").equals(dataSet.replaceAll("[(\\‚)]", ","))) {
				break;
			} else if (i == 1) {
				ErrorHandler.frameworkError(ErrLvl.FATAL, null,
						dataSet + " Data set was not found for Pre-Defined Flow " + scriptSheet.getSheetName());
			}
		}

		return i + 1;
	}

	/**
	 * This method is used to get row count
	 * 
	 * @return row count
	 */
	private int getRowCount() {
		int number = scriptSheet.getLastRowNum() + 1;
		return number;
	}

	/**
	 * This method is used to get row count
	 * 
	 * @param Argument : sheetName - This parameter is of String type
	 * @return row count
	 */
	public int getRowCount(String sheetName) {
		int index = scriptWorkbook.getSheetIndex(sheetName);
		if (index == -1) {
			return 0;
		} else {
			scriptSheet = scriptWorkbook.getSheetAt(index);
			int number = scriptSheet.getLastRowNum() + 1;
			return number;
		}

	}

	/**
	 * This method is used to read steps
	 * 
	 * @param Argument : rowNum - This parameter is of integer data type
	 * @param Argument : colNum - This parameter is of integer data type
	 * @return
	 */
	public String readStep(int rowNum, int colNum) {
		try {
			return formatter.formatCellValue((scriptSheet).getRow(rowNum).getCell(colNum)).trim();
		} catch (Exception e) {
			return "";
		}
	}

}
