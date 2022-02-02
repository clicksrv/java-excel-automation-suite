package frameworkextensions.applicationhandler;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import frameworkcore.dbtools.SQLUtilities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ApplicationLogCapture {

    SQLUtilities sql = new SQLUtilities();

    public String captureScreenErrorLogs(String errCd) {

        String sqlQuery = "SELECT SESSION_ID, APPL_NAME, CREATED_ON_TS FROM UT_ERR WHERE CHANNEL = 'PORTAL' AND ERR_CODE = '"
                + errCd + "' ORDER BY CREATED_ON_TS DESC FETCH FIRST ROW ONLY";

        HashMap<String, String> resultMap = null;

        int attemptCounter = 0;

        while (attemptCounter < 3 && (null == resultMap || null == resultMap.get("SESSION_ID"))) {
            try {
                Thread.sleep(2000);
                resultMap = sql.executeMultiColumnSQLQuery(sqlQuery);
            } catch (SQLException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Failed to retrieve Session ID for Screen: " + errCd);
                return null;
            } catch (InterruptedException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Thread Interrupted!");
            }
            attemptCounter++;
        }

        String result = "Last error log record with Error Code '" + errCd + "' for Portlet '"
                + resultMap.get("APPL_NAME") + "' at '" + resultMap.get("CREATED_ON_TS") + "' with Session ID: '"
                + resultMap.get("SESSION_ID") + "'";

        // result = captureServiceErrorLogs(resultMap.get("SESSION_ID"), result);

        return result;
    }

    public String captureBatchErrorLogs(String batchName) {

        String sqlQuery = "SELECT SESSION_ID, CREATED_ON_TS FROM UT_ERR WHERE CHANNEL = 'BATCH' AND USER_ID = '"
                + batchName + "' ORDER BY CREATED_ON_TS DESC FETCH FIRST ROW ONLY";

        HashMap<String, String> resultMap = null;

        int attemptCounter = 0;

        while (attemptCounter < 3 && (null == resultMap || null == resultMap.get("SESSION_ID"))) {
            try {
                Thread.sleep(2000);
                resultMap = sql.executeMultiColumnSQLQuery(sqlQuery);
            } catch (SQLException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e,
                        "Failed to retrieve Session ID for Batch Job: " + batchName);
                return null;
            } catch (InterruptedException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Thread Interrupted!");
            }
            attemptCounter++;
        }

        String result = "'" + batchName + "' generated Error Logs at '" + resultMap.get("CREATED_ON_TS")
                + "' with Session ID: '" + resultMap.get("SESSION_ID") + "'";

        // result = captureServiceErrorLogs(sessionID, result);

        return result;
    }

    private String captureServiceErrorLogs(String sessionID, String result) {

        if (StringUtils.contains(result, "T2ServiceFault")) {
            String serviceLog = null;
            String sqlQuery = "SELECT EXCP_TRACE FROM UT_ERR WHERE CHANNEL = 'INT_WEB_SERVICE' AND SESSION_ID = '"
                    + sessionID + "' ORDER BY CREATED_ON_TS DESC FETCH FIRST 1 ROW ONLY";

            try {
                serviceLog = sql.executeSQLQuery(sqlQuery);
            } catch (SQLException e) {
                ErrorHandler.frameworkError(ErrLvl.ERROR, e,
                        "Failed to retrieve Service Logs for Session ID: " + sessionID);
            }

            if (StringUtils.isNotBlank(serviceLog)) {
                result = result + "\nService Logs:\n" + serviceLog;
            }
        }

        return result;
    }

}
