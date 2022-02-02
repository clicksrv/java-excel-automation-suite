package frameworkcore.batchjobtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class BatchFileWriter extends FileWriter {

    public BatchFileWriter(File file) throws IOException {
        super(file);
        this.file = file;
    }

    File file = null;

    public void writeLine(String str) {
        try {
            append(str);
            append("\n");
            // System.out.print(str);
            // System.out.print("\n");
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, e,
                    "Failed to write string \"" + str + "\" to file: " + file.getAbsolutePath());
        }
    }

    /**
     * This method is used append spaces in the string.
     * 
     * @param str - This parameter is of String data type
     * @param depth - This parameter is of int data type
     */
    public void writeWithIndent(String str, int depth) {
        try {
            for (int i = 0; i < depth; i++) {
                append("\t");
                // System.out.print("\t");
            }
            append(str);
            append("\n");
            // System.out.print(str);
            // System.out.print("\n");
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, e,
                    "Failed to write string \"" + str + "\" to file: " + file.getAbsolutePath());
        }
    }

    /**
     * This method is used to write the string with fixed lenght.
     * 
     * @param str - This parameter is of String data type
     * @param length - This parameter is of int data type
     */
    public void writeWithFixedLength(String str, int length) {

        int strLength = 0;

        strLength = getStringLength(str);

        if (strLength < length) {
            do {
                str = str + " ";
                strLength = getStringLength(str);
            } while (strLength < length);
        }

        try {
            append(str);
        } catch (IOException e) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, e,
                    "Failed to write string \"" + str + "\" to file: " + file.getAbsolutePath());
        }
    }

    private int getStringLength(String str) {
        int strLength = 0;
        if (str != null) {
            strLength = str.length();
        }
        return strLength;
    }

}
