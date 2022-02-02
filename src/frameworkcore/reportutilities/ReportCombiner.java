package frameworkcore.reportutilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.excelreaders.RunManagerReader;

/**
 * @author Vedhanth Reddy (v.reddy.monajigari)
 */
public class ReportCombiner {

	private OPCPackage pkg = null;
	private String finalPath;
	private String finalReportPath = null;
	private XSSFWorkbook workbook = null;
	private XSSFSheet worksheet = null, finalWorksheet = null, mainWorksheeet = null;
	private File finalreport = null;
	private FileOutputStream outputStream = null;
	private XSSFWorkbook finalReportWorkbook = null;
	private int summaryRowNum = 0, testNumber = 1;

	private static int rowNum = 0, cellNum = 0, mainRowNum = 0, totalTest = 0;
	boolean created = false;
	String finalStatusOfTestCase = "PASS";
	int totalStepsForTestCase = 0, totalFailedOfTestCase = 0, totalPassOfTestCase = 0;

	public final void combineTestReports(String path) {

		finalReportPath = path + "Report.xlsx";

		boolean tests = false;

		RunManagerReader runManagerReader = null;

		File dir = new File(path);
		File[] fileListArray = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xlsx");
			}
		});

		totalTest = fileListArray.length - 1;
		List<File> fileList = new ArrayList<File>();

		for (File file : fileListArray) {
			if (file.getName().startsWith("Main")) {
				fileList.add(file);
				break;
			}
		}

		for (File file : fileListArray) {
			if (!file.getName().startsWith("Main")) {
				fileList.add(file);
			}
		}

		try {

			finalreport = new File(finalReportPath);
			outputStream = new FileOutputStream(finalreport);
			finalReportWorkbook = new XSSFWorkbook();

			for (File file : fileList) {

				workbook = new XSSFWorkbook(file);
				if (file.getName().contains("Main.xlsx")) {
					// Reading main sheet
					prepareSummary(workbook);
					for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
						mainRowNum = 0;

						worksheet = workbook.getSheetAt(i);
						finalWorksheet = finalReportWorkbook.createSheet(worksheet.getSheetName());
						Iterator<Row> rows = worksheet.rowIterator();

						while (rows.hasNext()) {

							XSSFRow row = (XSSFRow) rows.next();
							XSSFRow currentRow = finalWorksheet.createRow(mainRowNum);

							@SuppressWarnings("rawtypes")
							Iterator cells = row.cellIterator();
							cellNum = 0;
							while (cells.hasNext()) {
								XSSFCell cell = (XSSFCell) cells.next();
								XSSFCell currentCell = currentRow.createCell(cellNum);
								currentCell.setCellValue(cell.getStringCellValue());
								cellNum++;

							}
							mainRowNum++;
							cellNum = 0;
						}
					}

				} else {
					prepareSummary(workbook);

					if (!tests) {
						tests = true;
						finalWorksheet = finalReportWorkbook.createSheet("Tests");
						XSSFRow currentRow = finalWorksheet.createRow(rowNum);
						// currentRow.setRowStyle();
						currentRow.createCell(0, CellType.STRING).setCellValue("Test_Suite");
						currentRow.createCell(1, CellType.STRING).setCellValue("TC_ID");
						currentRow.createCell(2, CellType.STRING).setCellValue("Timestamp");
						currentRow.createCell(3, CellType.STRING).setCellValue("Node");
						currentRow.createCell(4, CellType.STRING).setCellValue("Status");
						currentRow.createCell(5, CellType.STRING).setCellValue("Activity");

						currentRow.createCell(6, CellType.STRING).setCellValue("Screenshot Path");
						rowNum++;

					}

					for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
						worksheet = workbook.getSheetAt(i);
						String testCaseId = worksheet.getSheetName();

						runManagerReader = new RunManagerReader(workbook, null);

						runManagerReader.setDatasheetName(testCaseId);
						String testSuite = runManagerReader.getValue(1, 6);

						for (int j = 3; j <= worksheet.getLastRowNum(); j++) {

							XSSFRow currentRow = finalWorksheet.createRow(rowNum);

							XSSFCell currentCell = currentRow.createCell(cellNum, CellType.STRING);
							currentCell.setCellValue(testSuite);
							cellNum++;

							currentCell = currentRow.createCell(cellNum, CellType.STRING);
							currentCell.setCellValue(testCaseId);
							cellNum++;

							for (int k = 0; k < 5; k++) {
								currentCell = currentRow.createCell(cellNum);
								currentCell.setCellValue(runManagerReader.getValue(j, k));
								cellNum++;
							}

							cellNum = 0;
							rowNum++;
						}

					}
				}
				workbook.close();
			}
			finalReportWorkbook.write(outputStream);
			outputStream.close();

			finalReportWorkbook.close();

		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void prepareSummary(XSSFWorkbook workbook) {

		RunManagerReader excelDataAccess = null;
		int summarycellNum = 0;

		if (!created) {
			excelDataAccess = new RunManagerReader(workbook, null);

			excelDataAccess.setDatasheetName("Main");
			created = true;
			mainWorksheeet = finalReportWorkbook.createSheet("Summary");
			XSSFRow currentRow = mainWorksheeet.createRow(summaryRowNum);
			// Creating top Row

			currentRow.createCell(0, CellType.STRING).setCellValue("Context");
			currentRow.createCell(1, CellType.STRING).setCellValue("Environment");
			currentRow.createCell(2, CellType.STRING).setCellValue("Execution Started");
			currentRow.createCell(3, CellType.STRING).setCellValue("Execution Finished");
			currentRow.createCell(4, CellType.STRING).setCellValue("Time Taken");

			currentRow.createCell(5, CellType.STRING).setCellValue("Build Number");
			currentRow.createCell(6, CellType.STRING).setCellValue("Parallel Threads");
			currentRow.createCell(7, CellType.STRING).setCellValue("No. of Tests");
			summaryRowNum++;
			currentRow = mainWorksheeet.createRow(summaryRowNum);
			currentRow.createCell(0, CellType.STRING).setCellValue("Run Summary");
			currentRow.createCell(1, CellType.STRING).setCellValue(excelDataAccess.getValue(1, 0));
			currentRow.createCell(2, CellType.STRING).setCellValue(excelDataAccess.getValue(1, 3));
			currentRow.createCell(3, CellType.STRING).setCellValue(excelDataAccess.getValue(1, 4));
			currentRow.createCell(4, CellType.STRING).setCellValue(excelDataAccess.getValue(1, 5));

			currentRow.createCell(5, CellType.STRING).setCellValue(excelDataAccess.getValue(1, 1));
			currentRow.createCell(6, CellType.STRING).setCellValue(excelDataAccess.getValue(1, 2));
			currentRow.createCell(7, CellType.STRING).setCellValue("" + totalTest);

			summaryRowNum++;
			currentRow = mainWorksheeet.createRow(summaryRowNum);
			// currentRow.setRowStyle();
			currentRow.createCell(0, CellType.STRING).setCellValue("Test No.");
			currentRow.createCell(1, CellType.STRING).setCellValue("Test_Suite");

			currentRow.createCell(2, CellType.STRING).setCellValue("TC_ID");

			currentRow.createCell(3, CellType.STRING).setCellValue("Execution Started");
			currentRow.createCell(4, CellType.STRING).setCellValue("Execution Finished");
			currentRow.createCell(5, CellType.STRING).setCellValue("Time Taken");

			currentRow.createCell(6, CellType.STRING).setCellValue("Final Status");
			currentRow.createCell(7, CellType.STRING).setCellValue("Total Steps");
			currentRow.createCell(8, CellType.STRING).setCellValue("Steps Passed");
			currentRow.createCell(9, CellType.STRING).setCellValue("Steps Failed");

			currentRow.createCell(10, CellType.STRING).setCellValue("Browser");
			currentRow.createCell(11, CellType.STRING).setCellValue("Platform");
			currentRow.createCell(12, CellType.STRING).setCellValue("Node Name");
			summaryRowNum++;
		} else {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

				finalStatusOfTestCase = "PASS";
				totalStepsForTestCase = 0;
				totalFailedOfTestCase = 0;
				totalPassOfTestCase = 0;
				worksheet = workbook.getSheetAt(i);
				String testCaseId = worksheet.getSheetName();

				excelDataAccess = new RunManagerReader(workbook, null);

				excelDataAccess.setDatasheetName(testCaseId);
				// Get Steps Details
				getStepsSummary(excelDataAccess);
				// set TC_ID
				XSSFRow currentRow = mainWorksheeet.createRow(summaryRowNum);
				XSSFCell currentCell = currentRow.createCell(summarycellNum, CellType.STRING);
				currentCell.setCellValue(testNumber);
				summarycellNum++;

				// SET Test Suite
				currentCell = currentRow.createCell(summarycellNum, CellType.STRING);

				currentCell.setCellValue(excelDataAccess.getValue(1, "Suite"));
				summarycellNum++;

				// SET Test No.
				currentCell = currentRow.createCell(summarycellNum, CellType.STRING);

				currentCell.setCellValue(testCaseId);
				summarycellNum++;
				// SET Execution Started
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(excelDataAccess.getValue(1, "Execution Started"));
				summarycellNum++;
				// SET Execution Finished
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(excelDataAccess.getValue(1, "Execution Finished"));
				summarycellNum++;
				// SET Time
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(excelDataAccess.getValue(1, "Elapsed Time"));
				summarycellNum++;

				// SET Final Status
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(finalStatusOfTestCase);
				summarycellNum++;
				//
				// SET Total Steps
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(totalStepsForTestCase);
				summarycellNum++;
				// SET Steps Passed
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(totalPassOfTestCase);
				summarycellNum++;
				// SET Steps Failed
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(totalFailedOfTestCase);
				summarycellNum++;

				// SET B
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(excelDataAccess.getValue(1, "Browser"));
				summarycellNum++;
				// SETPlatform
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(excelDataAccess.getValue(1, "Platform"));
				summarycellNum++;
				// SET HostName
				currentCell = currentRow.createCell(summarycellNum);
				currentCell.setCellValue(excelDataAccess.getValue(1, "HostName"));
				summarycellNum++;

				summarycellNum = 0;
				summaryRowNum++;

			}

			testNumber++;
		}
	}

	private void getStepsSummary(RunManagerReader excelDataAccess) {

		for (int i = 2; i < excelDataAccess.getLastRowNum(); i++) {

			String status = excelDataAccess.getValue(i + 1, 2);

			if (StringUtils.equals(status, "PASS")) {
				totalPassOfTestCase++;
				totalStepsForTestCase++;
			} else if (StringUtils.equals(status, "FAIL") || StringUtils.equals(status, "FATAL")) {
				totalFailedOfTestCase++;
				totalStepsForTestCase++;
				finalStatusOfTestCase = "Fail";
			}

		}

	}

	/*
	 * private void openWorkbook(String filePath) { try { pkg =
	 * OPCPackage.open(filePath, PackageAccess.READ); workbook = new
	 * XSSFWorkbook(pkg); } catch (FileNotFoundException e) {
	 * ErrorHandler.testError(ErrLvl.FATAL, e, "File not found at: " + filePath,
	 * null); } catch (IOException e) { ErrorHandler.testError(ErrLvl.FATAL, e,
	 * "Unable to open file at: " + filePath, null); } catch (InvalidFormatException
	 * e) { ErrorHandler.testError(ErrLvl.FATAL, e, "File Format is invalid at: " +
	 * filePath, null); } }
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
			ErrorHandler.testError(ErrLvl.FATAL, e, "Unable to close file at: " + finalPath, null);
		}

	}

}
