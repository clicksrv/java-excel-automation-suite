package frameworkcore.dbtools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import frameworkcore.EncryptionToolkit;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * This class provides all methods required to execute various types of SQL
 * Queries.
 * 
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo), Vedhanth Reddy
 *         (v.reddy.monajigari)
 */
public class SQLUtilities {

    private static String connectionUrl = "";
    private static String schema = "";
    private static String dbusername;
    private static String dbpassword;

    /**
     * The constructor instantiates with the required connection settings.
     */

    public SQLUtilities() {

        Properties properties = FrameworkPropertyFiles.dbProperties;

        connectionUrl = properties.getProperty("connectionUrl_" + TestSettings.currentEnvironment);
        dbusername = EncryptionToolkit.decrypt(properties.getProperty("dbusername_" + TestSettings.currentEnvironment),
                FrameworkEntities.encryptionKeyFile);
        dbpassword = EncryptionToolkit.decrypt(properties.getProperty("dbpassword_" + TestSettings.currentEnvironment),
                FrameworkEntities.encryptionKeyFile);
        schema = properties.getProperty("schema_" + TestSettings.currentEnvironment);

        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection connection = null;
    private String resultValue = null;
    private ResultSet rs;

    public List<String> executeSQLQueryList(String sqlQuery) throws SQLException {

        sqlQuery = fixAndOptimizeSelectQuery(sqlQuery.trim());

        String columnName = identifyColumnName(sqlQuery);

        if (StringUtils.equals(columnName, "*")) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null,
                    "SQL Query needs the column name to be specified and will not work with \"SELECT * FROM\"!");
        }

        // resultValue = null;

        if (connection == null) {
            connection = DriverManager.getConnection(connectionUrl, dbusername, dbpassword);
        }

        if (connection == null) {
            System.out.println("Database connection failed to " + TestSettings.currentEnvironment + " Environment");
        }
        connection.createStatement().execute("set current schema = " + schema + " ");
        Statement stmt = connection.createStatement();

        try {
            rs = stmt.executeQuery(sqlQuery);

            if (null == rs) {
                return null;
            }

        } catch (NullPointerException err) {
            return null;
        }
        List<String> resultList = new ArrayList<>();
        while (rs.next()) {

            resultList.add(rs.getString(columnName).toString());
            // resultValue = rs.getString(columnName).toString();

        }

        connection.close();
        connection = null;

        return resultList;

    }

    /**
     * This method executes the query and returns the result as a String.
     * 
     * @param sqlQuery The query with the expected column should be provided between
     *                 SELECT and FROM. Only one column name is allowed.
     * @return Value as a String from the Column Name in the Query Result.
     * @throws SQLException with SQL Error Code
     */
    public String executeSQLQuery(String sqlQuery) throws SQLException {

        sqlQuery = fixAndOptimizeSelectQuery(sqlQuery.trim());

        String columnName = identifyColumnName(sqlQuery);

        if (StringUtils.equals(columnName, "*")) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null,
                    "SQL Query needs the column name to be specified and will not work with \"SELECT * FROM\"!");
        }

        resultValue = null;

        if (connection == null) {
            connection = DriverManager.getConnection(connectionUrl, dbusername, dbpassword);
        }

        if (connection == null) {
            System.out.println("Database connection failed to " + TestSettings.currentEnvironment + " Environment");
        }
        connection.createStatement().execute("set current schema = " + schema + " ");
        Statement stmt = connection.createStatement();

        try {
            rs = stmt.executeQuery(sqlQuery);

            if (null == rs) {
                return "";
            }

        } catch (NullPointerException err) {
            return null;
        }

        while (rs.next()) {

            resultValue = rs.getString(columnName).toString();
            if (null == resultValue) {
                return "";
            }
        }

        connection.close();
        connection = null;

        return resultValue;
    }

    public String identifyColumnName(String sqlQuery) {

        int endIndex = sqlQuery.toUpperCase().indexOf(" FROM ");

        if (sqlQuery.substring(0, endIndex).contains("(")) {
            do {
                endIndex = endIndex + 2 + sqlQuery.substring(endIndex + 2).toUpperCase().indexOf(" FROM ");
            } while (!(StringUtils.countMatches(sqlQuery.substring(0, endIndex), "(") == StringUtils
                    .countMatches(sqlQuery.substring(0, endIndex), ")")));
        }

        String columnName = sqlQuery.substring(6, endIndex).replace("DISTINCT", "").replace("UNIQUE", "").toUpperCase()
                .trim();

        if (StringUtils.containsIgnoreCase(columnName, "COUNT")) {

            columnName = StringUtils.substring(columnName, 0, StringUtils.indexOf(columnName, " FROM"));

        }

        if (StringUtils.containsIgnoreCase(columnName, " AS ")) {
            columnName = StringUtils.substring(columnName, StringUtils.indexOf(columnName, " AS ") + 4);
        } else if (StringUtils.contains(columnName, ".")) {
            columnName = StringUtils.substring(columnName, StringUtils.indexOf(columnName, ".") + 1);
        }

        return columnName;
    }

    public String[] identifyMultipleColumnNames(String sqlQuery) {

        int endIndex = sqlQuery.toUpperCase().indexOf(" FROM ");

        if (sqlQuery.substring(0, endIndex).contains("(")) {
            do {
                endIndex = endIndex + 2 + sqlQuery.substring(endIndex + 2).toUpperCase().indexOf(" FROM ");
            } while (!(StringUtils.countMatches(sqlQuery.substring(0, endIndex), "(") == StringUtils
                    .countMatches(sqlQuery.substring(0, endIndex), ")")));
        }
        String tempColumnList = sqlQuery.substring(6, endIndex).toUpperCase();

        tempColumnList = tempColumnList.replace("DISTINCT", "").replace("UNIQUE", "");

        String[] columnList = tempColumnList.trim().split("\\s*,\\s*");

        for (int colIdx = 0; colIdx < columnList.length; colIdx++) {
            String columnName = columnList[colIdx];
            if (StringUtils.containsIgnoreCase(columnName, " AS ")) {
                columnList[colIdx] = StringUtils.substring(columnName, StringUtils.indexOf(columnName, " AS ") + 4);
            } else if (StringUtils.contains(columnName, ".")) {
                columnList[colIdx] = StringUtils.substring(columnName, StringUtils.indexOf(columnName, ".") + 1);
            }
        }

        return columnList;
    }

    /**
     * This method executes the query and returns the result as a String.
     * 
     * @param sqlQuery The query with the expected column should be provided between
     *                 SELECT and FROM. Multiple comma separated column names are
     *                 expected in standard SQL Format is allowed.
     * @return Hashmap of Column Name to Respective Value in the Query Result.
     * @throws SQLException with SQL Error Code
     */
    public HashMap<String, String> executeMultiColumnSQLQuery(String sqlQuery) throws SQLException {

        sqlQuery = fixAndOptimizeSelectQuery(sqlQuery);

        HashMap<String, String> colToValMap = new HashMap<String, String>();

        String[] columnList = identifyMultipleColumnNames(sqlQuery);

        for (String string : columnList) {

            if (StringUtils.equals(string, "*")) {
                ErrorHandler.frameworkError(ErrLvl.FATAL, null,
                        "SQL Query needs the column name to be specified and will not work with \"SELECT * FROM\"!");
            }

            colToValMap.put(string, null);
        }

        if (connection == null) {
            connection = DriverManager.getConnection(connectionUrl, dbusername, dbpassword);
        }

        if (connection == null) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null,
                    "Database connection failed to " + TestSettings.currentEnvironment + " Environment");
        }

        connection.createStatement().execute("set current schema = " + schema + " ");
        Statement stmt = connection.createStatement();
        try {
            rs = stmt.executeQuery(sqlQuery);
        } catch (NullPointerException err) {
            return null;
        }

        while (rs.next()) {
            for (Entry<String, String> column : colToValMap.entrySet()) {

                String columnVal = rs.getString(column.getKey());
                if (StringUtils.isNotBlank(columnVal)) {
                    column.setValue(columnVal);
                } else {
                    column.setValue("");
                }

            }
        }

        connection.close();
        connection = null;

        return colToValMap;
    }

    /**
     * This method executes the update query and returns the count of updated
     * records.
     * 
     * @param sqlQuery The query with the value should be provided in update query
     *                 for a particular column. Multiple comma separated column
     *                 names are expected in standard SQL Format is allowed.
     * @return int: number of records updated by the query
     * @throws SQLException with SQL Error Code
     */
    public int executeUpdateSQLQuery(String sqlQuery) throws SQLException {

        if (connection == null) {
            connection = DriverManager.getConnection(connectionUrl, dbusername, dbpassword);
        }

        if (connection == null) {
            System.out.println("Database connection failed to " + TestSettings.currentEnvironment + " Environment");
        }
        connection.createStatement().execute("set current schema = " + schema + " ");
        Statement stmt = connection.createStatement();

        sqlQuery = fixAndOptimizeUpdateQuery(sqlQuery);

        int updatedRecordCount = stmt.executeUpdate(sqlQuery);
        stmt.close();
        return updatedRecordCount;

    }

    public void deleteQuery(String sqlQuery) throws SQLException {

        if (connection == null) {
            connection = DriverManager.getConnection(connectionUrl, dbusername, dbpassword);
        }

        if (connection == null) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null,
                    "Database connection failed to " + TestSettings.currentEnvironment + " Environment");
        }

        connection.createStatement().execute("set current schema = " + schema + " ");
        Statement stmt = connection.createStatement();
        stmt.execute(sqlQuery);
        stmt.close();
    }

    public void closeConnection() throws SQLException {

        connection.close();

    }

    private String fixAndOptimizeSelectQuery(String sqlQuery) {

        if (!StringUtils.endsWith(sqlQuery.toUpperCase(), " WITH UR")) {
            sqlQuery = compressQuery(sqlQuery) + " WITH UR";
        }

        return sqlQuery;
    }

    private String fixAndOptimizeUpdateQuery(String sqlQuery) {

        return compressQuery(sqlQuery);
    }

    private String compressQuery(String sqlQuery) {
        sqlQuery = StringUtils.replace(sqlQuery, "\n", " ").trim();

        if (StringUtils.endsWith(sqlQuery, ";")) {
            sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1).trim();
        }
        return sqlQuery;
    }
}
