package frameworkcore.batchjobtools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class BatchFileReader extends BufferedReader {

    public BatchFileReader(Reader reader) {
        super(reader);
    }

    int recordLineNumber = 0;

    int recordOpenNumber = 0;
    int recordCloseNumber = 0;

    /**
     * This method is used to check whether text record is present or not.
     * 
     * @param expectedValue - This parameter is of String data type
     * @return boolean
     */
    public boolean checkIfTXTRecordPresent(String expectedValue) {

        String recordText = null;
        int readingLineNumber = 0;
        boolean recordFound = false;

        while (recordText != null || readingLineNumber == 0) {
            try {
                readingLineNumber++;
                recordText = readLine();
            } catch (IOException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to read line from outbound file!");
            }

            if (null != recordText && recordText.contains(expectedValue)) {
                recordLineNumber = readingLineNumber;
                recordFound = true;
                break;
            }
        }

        return recordFound;
    }

    /**
     * This method is used to check if XML record is present or not.
     * 
     * @param parentTag - This parameter is of String data type
     * @param expectedValue - This parameter is of String data type
     * @return boolean
     */
    public boolean checkIfXMLRecordPresent(String parentTag, String expectedValue) {

        String recordText = null;
        int readingLineNumber = 0;
        boolean recordFound = false;

        while (recordText != null || readingLineNumber == 0) {
            try {
                readingLineNumber++;
                recordText = readLine();
            } catch (IOException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to read line from outbound file!");
            }

            if (null != recordText) {
                try {
                    readingLineNumber++;
                    recordText = readLine();
                } catch (IOException e) {
                    ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to read line from outbound file!");
                }

                if (recordText.contains(expectedValue)) {
                    recordFound = true;
                } else if (recordText.contains("<" + parentTag + ">")) {
                    recordOpenNumber = readingLineNumber;
                } else if (recordText.contains("</" + parentTag + ">")) {
                    recordCloseNumber = readingLineNumber;
                    if (recordFound) {
                        break;
                    }
                }
            }
        }

        if (!recordFound) {
            recordOpenNumber = 0;
            recordCloseNumber = 0;
        }

        return recordFound;
    }

}
