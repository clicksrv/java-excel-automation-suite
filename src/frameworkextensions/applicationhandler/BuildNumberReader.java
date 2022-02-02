package frameworkextensions.applicationhandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.TestSettings;

/**
 * @author Vedhanth Reddy (v.reddy.monajigari), Saurav Kumar Sahoo
 *         (saurav.kumar.sahoo)
 */
public class BuildNumberReader {

    private final static String versionFilePath = FrameworkEntities.fileSeparator + FrameworkEntities.fileSeparator
            + "xyz123" + FrameworkEntities.fileSeparator + "Build_Number" + FrameworkEntities.fileSeparator;
    private static String currentBuildNumber = null;

    public static String readVersionFile() {

        File file = new File(
                versionFilePath + TestSettings.currentEnvironment + FrameworkEntities.fileSeparator + "version.txt");

        Properties versionFile = new Properties();

        try {
            versionFile.load(new FileInputStream(file));
            currentBuildNumber = versionFile.getProperty("build_number");
        } catch (FileNotFoundException fe) {
            currentBuildNumber = "0.0.0";
        } catch (IOException ie) {
            currentBuildNumber = "0.0.0";
        }

        return currentBuildNumber;

    }
}
