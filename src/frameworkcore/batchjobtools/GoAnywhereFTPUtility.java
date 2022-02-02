package frameworkcore.batchjobtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.EncryptionToolkit;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class GoAnywhereFTPUtility extends TestThread {

    Properties properties = FrameworkPropertyFiles.goAnywhereProperties;
    FTPClient goAnywhere = null;

    /**
     * This method provides GoAnywhere FTP Utility to the batch tool.
     * 
     * @param threadEntities - This parameter is of ThreadEntities data
     *         type
     */
    public GoAnywhereFTPUtility(ThreadEntities threadEntities) {

        super(threadEntities);

        String host = properties.getProperty("GoAnywhere_Host_" + TestSettings.currentEnvironment);
        String user = EncryptionToolkit.decrypt(
                properties.getProperty("GoAnywhere_User_" + TestSettings.currentEnvironment),
                FrameworkEntities.encryptionKeyFile);
        String pass = EncryptionToolkit.decrypt(
                properties.getProperty("GoAnywhere_Pass_" + TestSettings.currentEnvironment),
                FrameworkEntities.encryptionKeyFile);

        goAnywhere = new FTPClient();

        try {
            goAnywhere.connect(host);
            goAnywhere.login(user, pass);
            goAnywhere.enterLocalPassiveMode();
            goAnywhere.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (SocketException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Socket Exception on connection with GoAnywhere!");
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "IO Exception on connection with GoAnywhere!");
        }
    }

    /**
     * This method is used to upload File To Inbound folder in Go Anywhere.
     * 
     * @param fileToUpload - This parameter is of File data type
     */
    public void uploadFileToInbound(File fileToUpload) {

        String fileName = fileToUpload.getName();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileToUpload);
        } catch (FileNotFoundException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "File at " + fileToUpload.getAbsolutePath() + " not found!");
        }

        logLine("Uploading file to inbound: " + fileName);

        boolean done = false;
        try {
            done = goAnywhere.storeFile("/opt/batch/clean/devint2/inbound" + FrameworkEntities.fileSeparator + fileName,
                    inputStream);
            inputStream.close();
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Failed to upload file to GoAnywhere!");
        }

        if (done) {
            logLine("File uploaded successfully!");
        } else {
            ErrorHandler.testError(ErrLvl.FATAL, null, "File upload failed!", testCaseVariables);
        }
    }

    /**
     * This method is used to download File From Outbound folder from Go Anywhere.
     * 
     * @param baseFileName - This parameter is of String data type
     * @param testCaseName - This parameter is of String data type
     */
    public File downloadFileFromOutbound(String baseFileName, String testCaseName) {
        return null;
    }

}
