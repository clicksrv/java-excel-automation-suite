package frameworkcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class FileSelectionHandler {

    /**
     * This method is used to browse and select the file
     * 
     * @params Argument : file - This parameter is of File type
     */
    public static synchronized void browseAndChooseFile(File file) {

        final String exeFilePath = FrameworkStatics.getRelativePath() + FrameworkEntities.fileSeparator + "Utilities"
                + FrameworkEntities.fileSeparator + "AutoITScripts" + FrameworkEntities.fileSeparator
                + "SelectFile.exe";

        try {

            String line;
            @SuppressWarnings("unused")
            String pidInfo = "";

            Thread.sleep(2500);

            Process p = Runtime.getRuntime().exec(exeFilePath + " " + file.getAbsolutePath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = input.readLine()) != null) {
                pidInfo += line;
            }

            input.close();

            Thread.sleep(250);

        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Failed to invoke AutoIT Uploader!");
        } catch (InterruptedException e) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Interrupted!");
        }

    }
}
