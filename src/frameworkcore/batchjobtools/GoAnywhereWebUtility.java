package frameworkcore.batchjobtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.EncryptionToolkit;
import frameworkcore.FileSelectionHandler;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class GoAnywhereWebUtility extends TestThread {

    Properties properties = FrameworkPropertyFiles.goAnywhereProperties;
    String username = null;
    String password = null;
    ApplicationMessageValidator generic = null;
    WebElementManager elementManager = null;

    /**
     * This method has the Go Anywhere Web Utility.
     * 
     * @param threadEntities - This parameter is of ThreadEntities data
     *         type
     */

    public GoAnywhereWebUtility(ThreadEntities threadEntities) {

        super(threadEntities);

        generic = new ApplicationMessageValidator(threadEntities);
        elementManager = new WebElementManager(threadEntities);

        driver.get(properties.getProperty("GoAnywhere_WebUI_URL"));
        username = properties.getProperty("GoAnywhere_WebUI_User_" + TestSettings.currentEnvironment);
        password = EncryptionToolkit.decrypt(
                properties.getProperty("GoAnywhere_WebUI_Pass_" + TestSettings.currentEnvironment),
                FrameworkEntities.encryptionKeyFile);

        boolean webUIReady = false;

        int i = 0;

        WebElement weUser = null;
        while (!webUIReady) {
            weUser = elementManager.waitAndFindElement(".//input[contains(@id,'username')]", true);

            if (null == weUser) {
                if (i == 9) {
                    reportingManager.updateTestLog(Status.FATAL, "GoAnywhere Web UI did not load successfully!", true);
                }
            } else {
                webUIReady = true;
            }

            i++;
        }

        weUser.clear();
        weUser.sendKeys(username);

        WebElement wePass = elementManager
                .waitAndFindElement(".//input[contains(@id,'value') and not(contains(@id,'value_hinput'))]");
        wePass.clear();
        wePass.sendKeys(password);

        WebElement weLogin = elementManager.waitAndFindElement(".//button[@type='submit']");
        weLogin.click();

        // Check based on error icon
        if (generic.isMessagePresent(driver.findElement(By.xpath(".//span[@class='ui-messages-error-icon']")))) {
            reportingManager.updateTestLog(Status.FATAL, "Failed to log in to GoAnywhere Web UI!", true);
        }

        // Check based on particular message
        /*
         * if (generic.isMessagePresent(driver.findElement(By
         * .xpath(".//span[text()='Invalid user name and/or password']")))) {
         * generic.updateTestLog(Status.FATAL, "Failed to log in to GoAnywhere Web UI!",
         * true); }
         */

    }

    /**
     * This method is used to upload File To Inbound folder of Go Anywhere.
     * 
     * @param fileToUpload - This parameter is of File data type
     */
    public void uploadFileToInbound(File fileToUpload) {

        try {
            String fileName = fileToUpload.getName();

            logLine("Uploading file to inbound: " + fileName);

            elementManager.waitAndFindElement(".//a[text()='inbound']").click();
            elementManager.waitAndFindElement(".//span[text()='Upload']").click();

            FileSelectionHandler.browseAndChooseFile(fileToUpload);

            WebElement uploadSuccessMessage = elementManager.waitAndFindElement(".//span[text()='Upload Complete']",
                    true);

            if (null != uploadSuccessMessage) {
                reportingManager.updateTestLog(Status.INFO, "File uploaded successfully!", true);
            } else {
                reportingManager.updateTestLog(Status.FATAL, "File upload failed!", true);
            }
        } catch (Exception e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Exception caught in Go Anywhere Web utility!");
        } finally {
            elementManager.waitAndFindElement(".//a[text()='Logout']").click();
        }
    }

    /**
     * This method is used to download File from Outbound folder of Go Anywhere.
     * 
     * @param baseFileName - This parameter is of String data type
     * @param testCaseName - This parameter is of String data type
     */
    public File downloadFileFromOutbound(String baseFileName, String testCaseName) {

        try {
            logLine("Downloading file from outbound: " + baseFileName);

            elementManager.waitAndFindElement(".//a[text()='outbound']").click();
            elementManager
                    .waitAndFindElement("/html/body/div[1]/div[2]/form[6]/div[1]/div/table/thead/tr/th[4]/span[2]")
                    .click();
            elementManager
                    .waitAndFindElement("/html/body/div[1]/div[2]/form[6]/div[1]/div/table/thead/tr/th[4]/span[2]")
                    .click();

            String fileName = baseFileName.substring(0, baseFileName.lastIndexOf("."));

            String downloadedFileName = elementManager.waitAndFindElements("//*[contains(text(),'" + fileName + "')]")
                    .get(0).getText();
            if (!StringUtils.contains(downloadedFileName, "DONE")) {
                elementManager.waitAndFindElements("//*[contains(text(),'" + fileName + "')]").get(0).click();
            } else {
                elementManager.waitAndFindElements("//*[contains(text(),'" + fileName + "')]").get(1).click();
                downloadedFileName = downloadedFileName.substring(0, downloadedFileName.length() - 5);
            }

            File downloadedFile = new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator
                    + "Downloads" + FrameworkEntities.fileSeparator + downloadedFileName);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to download file in desired time!");
            }
            String outputPath = FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "BatchFiles"
                    + FrameworkEntities.fileSeparator + "outbound" + FrameworkEntities.fileSeparator + testCaseName
                    + FrameworkEntities.fileSeparator;

            new File(outputPath).mkdirs();

            Path movedPath = null;

            try {
                movedPath = Files.move(Paths.get(downloadedFile.getAbsolutePath()),
                        Paths.get(outputPath + downloadedFileName));
            } catch (IOException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to move file!");
            }

            if (null != movedPath) {
                File file = new File(movedPath.toString());
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Exception caught in Go Anywhere Web utility!");
        } finally {
            elementManager.waitAndFindElement(".//a[text()='Logout']").click();
        }
        return null;
    }

}
