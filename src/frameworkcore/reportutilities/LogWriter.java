package frameworkcore.reportutilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestCaseVariables;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class LogWriter extends FileOutputStream {

    private TestCaseVariables testCaseVariables = null;

    public LogWriter(String filePath, TestCaseVariables testCaseVariables) throws FileNotFoundException {
        super(new File(filePath));
        this.testCaseVariables = testCaseVariables;
    }

    public void append(String str) {
        try {
            super.write(str.getBytes(), 0, str.length());
        } catch (IOException e) {
            if (str.equals("/n")) {
                str = "<NEW_LINE>";
            }

            TestSettings.writeTestConsoleToFile = false;
            ErrorHandler.testError(ErrLvl.ERROR, e, "Failed to write string: '" + str + "' to Log File",
                    testCaseVariables);
        }
    }

}
