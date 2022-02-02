package frameworkcore.reportutilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Vedhanth Reddy (v.reddy.monajigari)
 */
public class CreateWordforScreenShots {

    private static XSSFWorkbook workbook = null;
    private static XSSFSheet worksheet = null;

    public static void createScreenShotDocuments(String path) {

        new File(path + FrameworkEntities.fileSeparator + "WordReports").mkdir();

        File dir = new File(path + FrameworkEntities.fileSeparator + "ExcelReports");

        File[] fileListArray = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xlsx");
            }
        });

        for (File currentFile : fileListArray) {
            try {
                workbook = new XSSFWorkbook(currentFile);

                if (!currentFile.getName().contains("Main.xlsx") && !currentFile.getName().contains("Report.xlsx")) {

                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

                        worksheet = workbook.getSheetAt(i);

                        createScreenShotDocumentForCase(worksheet, path);

                    }

                }

            } catch (InvalidFormatException | IOException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to Create Word Documents");
            } finally {

                try {
                    workbook.close();
                } catch (IOException e) {
                    ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to Create Word Documents");
                }
            }
        }
    }

    private static void createScreenShotDocumentForCase(XSSFSheet sheet, String path) {
        String screenShotDocumentsPath = path + FrameworkEntities.fileSeparator + "WordReports";

        String currentscreenshotPath = null;

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun xwpfRun = paragraph.createRun();

        for (int i = 3; i < sheet.getLastRowNum() + 1; i++) {

            currentscreenshotPath = sheet.getRow(i).getCell(4).getStringCellValue();

            if (!currentscreenshotPath.isEmpty()) {

                currentscreenshotPath = path + FrameworkEntities.fileSeparator + currentscreenshotPath;

                int format = XWPFDocument.PICTURE_TYPE_PNG;

                String node = sheet.getRow(i).getCell(1).getStringCellValue();

                String description = Jsoup.parse(sheet.getRow(i).getCell(3).getStringCellValue()).text();

                String commentText = "";
                if (StringUtils.contains(node, ": ")) {
                    commentText = StringUtils.substring(node, StringUtils.indexOf(node, ": ") + 2) + "\n" + description;
                } else {
                    commentText = node + "\n" + description;
                }
                xwpfRun.setText(commentText);

                xwpfRun.addBreak(BreakType.TEXT_WRAPPING);
                try (FileInputStream screenShotStream = new FileInputStream(new File(currentscreenshotPath));) {
                    BufferedImage bimg = ImageIO.read(new File(currentscreenshotPath));
                    int width = bimg.getWidth() / 3;
                    int height = bimg.getHeight() / 3;
                    xwpfRun.addPicture(screenShotStream, format, node, Units.toEMU(width), Units.toEMU(height));

                    bimg.flush();

                } catch (InvalidFormatException e) {
                    ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to create Screen shot documents");

                } catch (FileNotFoundException e) {
                    ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to find report documents");

                } catch (IOException e) {
                    ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to read Reprt file");

                }
                xwpfRun.addBreak(BreakType.TEXT_WRAPPING);
            }
        }

        String name = screenShotDocumentsPath + FrameworkEntities.fileSeparator + sheet.getSheetName() + ".docx";
        FileOutputStream out;
        try {
            out = new FileOutputStream(name);
            doc.write(out);
            doc.close();
            out.close();

            out = null;

        } catch (FileNotFoundException e) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to create Screen shot documents");
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to create Screen shot documents");
        }

    }

}
