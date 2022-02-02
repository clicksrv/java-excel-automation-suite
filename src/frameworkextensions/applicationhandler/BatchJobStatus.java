package frameworkextensions.applicationhandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import frameworkcore.dbtools.SQLUtilities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class BatchJobStatus {

    public ArrayList<String> checkIfBatchJobAlreadyRunning(SQLUtilities sql, String batchName) {

        ArrayList<String> results = new ArrayList<>();

        HashMap<String, String> resultMap = null;

        try {
            resultMap = sql.executeMultiColumnSQLQuery(
                    "SELECT JOB_NAME, (SELECT START_TIME FROM UT_BAT_JOB_EXECUTION WHERE UT_BAT_JOB_EXECUTION.JOB_INSTANCE_ID = UT_BAT_JOB_INSTANCE.JOB_INSTANCE_ID ORDER BY UT_BAT_JOB_EXECUTION.LAST_UPDATED DESC FETCH FIRST 1 ROW ONLY) AS START_TIME FROM UT_BAT_JOB_INSTANCE WHERE JOB_NAME = '"
                            + batchName
                            + "' AND JOB_INSTANCE_ID IN (SELECT JOB_INSTANCE_ID FROM UT_BAT_JOB_EXECUTION WHERE STATUS = 'STARTED' AND START_TIME > timestamp(current timestamp - 24 HOURS))");
        } catch (SQLException e) {
            ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Batch Job Status Retrieval From DB Failed!");
        }

        if (null != resultMap.get("JOB_NAME")) {
            results.add("true");
        } else {
            results.add("false");
        }

        results.add(resultMap.get("START_TIME"));

        return results;
    }

}
