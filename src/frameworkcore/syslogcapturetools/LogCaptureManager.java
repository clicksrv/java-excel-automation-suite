package frameworkcore.syslogcapturetools;

import frameworkextensions.applicationhandler.ApplicationLogCapture;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class LogCaptureManager {

    ApplicationLogCapture logCapture = new ApplicationLogCapture();

    public String captureLogsForScreen(String errCd) {
        return logCapture.captureScreenErrorLogs(errCd);
    }

    public String captureLogsForBatch(String batchJobName) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println("Interrupted!");
        }

        return logCapture.captureBatchErrorLogs(batchJobName);
    }

}
