package frameworkcore.dbtools;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkConstants;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testdatamanagement.ValueParser;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo), Ashish Vishwakarma
 *         (ashish.b.vishwakarma)
 */
public class SQLActions extends TestThread {

    public SQLActions(ThreadEntities threadEntities) {

        super(threadEntities);
    }

    public void assertDB(String sqlQuery, String expectedValue, Status statusOnFailure) {
        if (sqlQuery.toUpperCase().startsWith(ValueParser.SQL_QUERY_KEY)) {
            ValueParser parser = new ValueParser(threadEntities);

            expectedValue = parser.parseValue(expectedValue);
            runSQLAssertion(sqlQuery, expectedValue, statusOnFailure);
        }
    }

    public int updateDB(String sqlQuery) {
        SQLUtilities sql = new SQLUtilities();
        int recordsUpdated = -1;

        try {
            reportingManager.updateTestLog(Status.INFO, "Executing Update Query: " + sqlQuery, false);
            recordsUpdated = sql.executeUpdateSQLQuery(sqlQuery);
        } catch (SQLException e) {
            if (e.getErrorCode() == -911) {
                reportingManager.updateTestLog(Status.INFO,
                        "The table on which update is to be performed is locked by another activity on the application! Will retry after 2 minutes...",
                        false);

                threadSleep(120000);

                try {
                    reportingManager.updateTestLog(Status.INFO, "Retrying Update Query: " + sqlQuery, false);
                    recordsUpdated = sql.executeUpdateSQLQuery(sqlQuery);
                } catch (SQLException e2) {
                    if (e.getErrorCode() == -911) {
                        ErrorHandler.testError(ErrLvl.ERROR, e2,
                                "Table is still locked! Moving forward without updating...", testCaseVariables);
                    } else {
                        ErrorHandler.testError(ErrLvl.ERROR, e2, "Error encountered in executing update query!",
                                testCaseVariables);
                    }

                }

            } else {
                ErrorHandler.testError(ErrLvl.ERROR, e, "Error encountered in executing update query!",
                        testCaseVariables);
            }
        }

        return recordsUpdated;
    }

    private void runSQLAssertion(String value, String expectedValue, Status onFailure) {

        SQLUtilities sql = new SQLUtilities();

        boolean singleResult = true;

        String resultColumns = value.substring(7, value.toUpperCase().indexOf(" FROM"));

        String resultValue = null;

        String[] resultColumnList = null;
        HashMap<String, String> colToResultValMap = null;
        HashMap<String, String> colToExpectedValMap = null;

        if (resultColumns.contains(",")) {
            resultColumnList = resultColumns.split("\\s*,\\s*");
            singleResult = false;
        }

        ValueParser parser = new ValueParser(threadEntities);
        String sqlQuery = parser.parseStatementsWithParseableValueInQuotes(value);

        try {
            if (singleResult) {
                resultValue = sql.executeSQLQuery(sqlQuery).trim();

                reportingManager.updateTestLog(Status.INFO, "DB Assertion Performed with Query: "
                        + FrameworkConstants.HTMLFormat.FIELD + sqlQuery + FrameworkConstants.HTMLFormat.CLOSE, false);

                String columnName = sql.identifyColumnName(sqlQuery);

                if (StringUtils.equals(expectedValue, "*")) {
                    if (StringUtils.isNotBlank(resultValue)) {
                        reportingManager.updateTestLog(Status.PASS,
                                getDBAssertPassString(expectedValue, resultValue, columnName), false);
                    } else {
                        reportingManager.updateTestLog(onFailure,
                                getDBAssertFailString(expectedValue, resultValue, columnName), false);
                    }
                } else if (StringUtils.isBlank(expectedValue)) {
                    if (StringUtils.isNotEmpty(resultValue)) {
                        reportingManager.updateTestLog(onFailure,
                                getDBAssertFailString(expectedValue, resultValue, columnName), false);
                    } else {
                        reportingManager.updateTestLog(Status.PASS,
                                getDBAssertPassString(expectedValue, resultValue, columnName), false);
                    }
                } else {
                    if (resultValue.equalsIgnoreCase(expectedValue.trim())) {
                        reportingManager.updateTestLog(Status.PASS,
                                getDBAssertPassString(expectedValue, resultValue, columnName), false);
                    } else {
                        reportingManager.updateTestLog(onFailure,
                                getDBAssertFailString(expectedValue, resultValue, columnName), false);
                    }
                }
            } else {
                colToResultValMap = sql.executeMultiColumnSQLQuery(sqlQuery);

                String[] expectedValues = null;

                if (expectedValue.contains(",")) {
                    expectedValues = expectedValue.split("\\s*,\\s*");

                    for (int idx = 0; idx < expectedValues.length; idx++) {
                        expectedValues[idx] = parser.checkParseabilityAndParse(expectedValues[idx]);
                    }

                    colToExpectedValMap = new HashMap<>();

                    for (int x = 0; x < expectedValues.length; x++) {
                        colToExpectedValMap.put(resultColumnList[x], expectedValues[x]);
                    }
                } else {
                    ErrorHandler.testError(ErrLvl.ERROR, null,
                            "Query returns multiple column values but expected values contain only a single result!",
                            testCaseVariables);
                }

                reportingManager.updateTestLog(Status.INFO, "DB Assertion Performed\nQuery: "
                        + FrameworkConstants.HTMLFormat.FIELD + sqlQuery + FrameworkConstants.HTMLFormat.CLOSE, false);

                for (String column : resultColumnList) {
                    String result = colToResultValMap.get(column);
                    String expected = colToExpectedValMap.get(column);

                    if (StringUtils.equals(expectedValue, "*")) {
                        if (StringUtils.isNotBlank(result)) {
                            reportingManager.updateTestLog(Status.PASS, getDBAssertPassString(expected, result, column),
                                    false);
                        } else {
                            reportingManager.updateTestLog(onFailure, getDBAssertFailString(expected, result, column),
                                    false);
                        }
                    } else if (StringUtils.isEmpty(expectedValue)) {
                        if (StringUtils.isNotEmpty(result)) {
                            reportingManager.updateTestLog(onFailure, getDBAssertFailString(expected, result, column),
                                    false);
                        } else {
                            reportingManager.updateTestLog(Status.PASS, getDBAssertPassString(expected, result, column),
                                    false);
                        }
                    } else {
                        if (result.equalsIgnoreCase(expected.trim())) {
                            reportingManager.updateTestLog(Status.PASS, getDBAssertPassString(expected, result, column),
                                    false);
                        } else {
                            reportingManager.updateTestLog(onFailure, getDBAssertFailString(expected, result, column),
                                    false);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            reportingManager.updateTestLog(onFailure, "No result row returned by SQL Query: " + sqlQuery, false);
        } catch (SQLException e) {
            ErrorHandler.testError(ErrLvl.ERROR, e, "SQL Execution Failed! SQL Query: " + sqlQuery, testCaseVariables);
        }
    }

    private String getDBAssertFailString(String expectedValue, String resultValue, String columnName) {
        return "DB Assert Result Did Not Match for Column '" + columnName + "'\n\tResult: "
                + FrameworkConstants.HTMLFormat.VALUE + resultValue + FrameworkConstants.HTMLFormat.CLOSE
                + "\n\tExpected: " + FrameworkConstants.HTMLFormat.VALUE + expectedValue
                + FrameworkConstants.HTMLFormat.CLOSE;
    }

    private String getDBAssertPassString(String expectedValue, String resultValue, String columnName) {
        return "DB Assert Result Matched for Column '" + columnName + "'\n\tResult: "
                + FrameworkConstants.HTMLFormat.VALUE + resultValue + FrameworkConstants.HTMLFormat.CLOSE
                + "\n\tExpected: " + FrameworkConstants.HTMLFormat.VALUE + expectedValue
                + FrameworkConstants.HTMLFormat.CLOSE;
    }

}
