package frameworkcore.batchjobtools;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class XMLLayoutInterpreter {

    private XSSFSheet layoutSheet = null;
    private Integer usedRowCount = null;
    private Integer currentRow = null;
    private Integer currentColumn = null;
    private DataFormatter formatter = new DataFormatter();

    /**
     * This method is used to Interpret the XML layout.
     * 
     * @param wb - This parameter is of XSSFWorkbook data type
     * @param layoutType - This parameter is of String data type
     * @param layoutName - This parameter is of String data type
     */
    public XMLLayoutInterpreter(XSSFWorkbook wb, String layoutType, String layoutName) {
        layoutSheet = wb.getSheet(layoutName);
        usedRowCount = layoutSheet.getLastRowNum();
    }

    /**
     * This method is used to write and reach upto first value field.
     * 
     * @param writer - This parameter is of BatchFileWriter data type
     */
    public void writeAndReachUptoFirstValueField(BatchFileWriter writer) {
        currentRow = 1;
        currentColumn = 0;

        while (usedRowCount >= currentRow) {

            String tagName = getCellValue(currentRow, currentColumn);

            if (StringUtils.isNotBlank(tagName)) {

                if (StringUtils.startsWith(getCellValue(currentRow, currentColumn + 1), "!")) {
                    break;
                } else {
                    if (StringUtils.startsWith(tagName, "<!--")) {
                        writer.writeWithIndent(tagName, currentColumn);
                    } else if (StringUtils.startsWith(tagName, "!")) {
                        writer.writeWithIndent("</" + tagName.substring(1) + ">", currentColumn);
                    } else {
                        tagName = "<" + tagName + ">";
                        writer.writeWithIndent(tagName, currentColumn);
                    }
                }

                moveToNextRow();

            } else {
                ErrorHandler.frameworkError(ErrLvl.ERROR, null, "Unexpected end of XML Layout after row: " + currentRow
                        + 1 + " in: " + layoutSheet.getSheetName());
            }
        }
    }

    /**
     * This method is used write and reach up to next value field.
     * 
     * @param fieldName - This parameter is of String data type
     * @param depth - This parameter is of Integer data type
     * @param value - This parameter is of String data type
     * @param writer - This parameter is of BatchFileWriter data type
     */
    public void writeAndReachUptoNextValueField(String fieldName, Integer depth, String value, BatchFileWriter writer) {

        if (currentColumn != depth) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null, "Broken XML Layout at row: " + currentRow + 1 + " in: "
                    + layoutSheet.getSheetName()
                    + "\nThe layout should exactly match with the layout inserted\nin the script and should not be manually modified!");
        }

        while (usedRowCount >= currentRow) {
            String tagName = getCellValue(currentRow, currentColumn);

            if (StringUtils.isNotBlank(tagName)) {

                if (StringUtils.startsWith(getCellValue(currentRow, currentColumn + 1), "!")) {
                    if (!fieldName.equals(tagName)) {
                        break;
                    } else {
                        writer.writeWithIndent("<" + tagName + ">" + value + "</" + tagName + ">", currentColumn);
                    }
                } else if (StringUtils.startsWith(tagName, "<!--")) {
                    writer.writeWithIndent(tagName, currentColumn);
                } else if (StringUtils.startsWith(tagName, "!")) {
                    writer.writeWithIndent("</" + tagName.substring(1) + ">", currentColumn);
                } else {
                    writer.writeWithIndent("<" + tagName + ">", currentColumn);
                }

                moveToNextRow();
            }
        }
    }

    /**
     * This method is used to get the cell value.
     * 
     * @param currentRow - This parameter is of int data type
     * @param currentColumn - This parameter is of int data type
     */
    private String getCellValue(int currentRow, int currentColumn) {
        try {
            return formatter.formatCellValue((layoutSheet).getRow(currentRow).getCell(currentColumn));
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * This method is used move to next row.
     */
    private void moveToNextRow() {
        if (usedRowCount >= currentRow) {
            currentRow++;

            if ((currentColumn - 1) >= 0 && StringUtils.isNotBlank(getCellValue(currentRow, currentColumn - 1))) {
                currentColumn--;
            } else if (StringUtils.isNotBlank(getCellValue(currentRow, currentColumn))) {

            } else if (StringUtils.isNotBlank(getCellValue(currentRow, currentColumn + 1))) {
                currentColumn++;
            }
        }

    }
}
