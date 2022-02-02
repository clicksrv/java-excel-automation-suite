package frameworkcore.testdatamanagement;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import frameworkcore.FrameworkConstants.StandardFormats;
import frameworkcore.datablocks.AddressBlock;
import frameworkcore.dbtools.SQLUtilities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testdatamanagement.RandomDataCreator.State;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ValueParser extends TestThread {

    public ValueParser(ThreadEntities threadEntities) {
        super(threadEntities);
    }

    TestDataManager tdm = new TestDataManager(threadEntities);

    public final static String GLOBAL_KEY = "GlobalVariable";
    public final static String RANDOM_KEY = "Random";
    public final static String CONCAT_KEY = "Concat(";
    public final static String SYSTEM_KEY = "System.";
    public final static String SQL_QUERY_KEY = "SELECT ";
    public final static String SQL_UPDATE_QUERY_KEY = "UPDATE ";
    public final static String FRAMEWORK_PATH_KEY = "(PATH)";

    private final static String CUR_DATE_KEY = "CurrentDate";

    private final static String RANDOM_NAME_KEY = "Name.";
    private final static String RANDOM_EMAIL_KEY = "Email.";
    private final static String RANDOM_DATE_KEY = "DateBetween(";
    private final static String RANDOM_SSN_KEY = "SSN.";
    private final static String RANDOM_SSN_NO_HYPHEN_KEY = "NoHyphen.";
    private static final String RANDOM_SSN_KEY_WITH_NO_HYPHEN = "SSN[NoHyphen].";

    private final static String RANDOM_NUMBER_KEY = "NumberBetween(";

    private final static String RANDOM_STREETNAME_KEY = "StreetName.";
    private final static String RANDOM_APTHOUSEOTH_KEY = "AptHouseOth.";

    private final static String RANDOM_NONTX_CITY_KEY = "City.NonTexas.";
    private final static String RANDOM_NONTX_STATE_KEY = "State.NonTexas.";
    private final static String RANDOM_NONTX_ZIP_KEY = "ZIP.NonTexas.";

    private final static String RANDOM_CITY_KEY = "City.";
    private final static String RANDOM_COUNTY_KEY = "County.";
    private final static String RANDOM_STATE_KEY = "State.";
    private final static String RANDOM_ZIP_KEY = "ZIP.";

    private final static String RANDOM_EMPLOYERNAME_KEY = "EmprName.";
    private final static String RANDOM_ORGNAME_KEY = "OrgName.";

    private static final String QUOTE_MATCH_PATTERN = "(['])(?:(?=(\\\\?))\\2.)*?\\1";

    private static AddressBlock addressBlock = null;

    public String parseValue(String value) {
        if (value.toLowerCase().startsWith(CONCAT_KEY.toLowerCase())) {
            value = value.substring(CONCAT_KEY.length());

            String globalVarName = "";

            if (value.substring(value.lastIndexOf(")")).length() > 2) {
                globalVarName = value.substring(value.lastIndexOf(").") + 2);
            }

            value = value.substring(0, value.lastIndexOf(")"));

            int index = value.indexOf("&");

            String[] concatList = null;

            if (index != -1) {
                concatList = value.split("&");

                value = "";
            } else {
                concatList = new String[1];
                concatList[0] = value;
            }

            for (String concatValue : concatList) {
                concatValue = concatValue.trim();

                if (concatValue.startsWith("\"") && concatValue.endsWith("\"")) {
                    concatValue = concatValue.substring(1, concatValue.length() - 1);
                } else {
                    try {
                        concatValue = checkGlobalOrRandomValue(concatValue);
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted!");
                    }
                }

                value = value + concatValue;
            }

            if (!globalVarName.isEmpty()) {
                tdm.setTestGlobal(globalVarName, value);
            }

        } else {
            try {
                value = checkGlobalOrRandomValue(value);
            } catch (InterruptedException e) {
                System.err.println("Interrupted!");
            }
        }
        return value;
    }

    /**
     * This method takes user query as an input and replaces the Parseable Values
     * from their actual values in the query.
     * 
     * @param input value
     * @return resulting parsed value
     */
    public String parseStatementsWithParseableValueInQuotes(String value) {
        Pattern p = Pattern.compile(QUOTE_MATCH_PATTERN);

        Matcher m = p.matcher(value);

        LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();

        while (m.find()) {
            String parsingText = StringUtils.substring(value, m.start() + 1, m.end() - 1).trim();

            boolean startsWithPerc = parsingText.startsWith("%");
            boolean endsWithPerc = parsingText.endsWith("%");

            String parsedResult = parsingText;

            if (startsWithPerc) {
                parsedResult = parsedResult.substring(1);
            }
            if (endsWithPerc) {
                parsedResult = parsedResult.substring(0, parsedResult.length() - 1);
            }

            parsedResult = (startsWithPerc ? "%" : "") + parseValue(parsedResult) + (endsWithPerc ? "%" : "");

            replacements.put(parsingText, parsedResult);
        }

        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            value = StringUtils.replace(value, "'" + replacement.getKey() + "'", "'" + replacement.getValue() + "'");
        }

        return value;
    }

    private String checkGlobalOrRandomValue(String value) throws InterruptedException {

        if (value.toLowerCase().startsWith(GLOBAL_KEY.toLowerCase())) {

            value = value.substring(GLOBAL_KEY.length());

            String format = null;
            String[] formattingList = null;

            if (value.contains("[") && value.contains("]")) {
                format = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
                value = StringUtils.replace(value, "[" + format + "]", "");
                formattingList = format.split("\\s*,\\s*");
            }

            value = value.substring(1);

            value = tdm.getTestGlobal(value);

            if (StringUtils.isNotEmpty(format)) {

                for (int i = 0; i < formattingList.length; i++) {
                    switch (formattingList[i]) {
                    case "NoHyphen":
                        value = value.replace("-", "");
                        break;
                    default:
                        try {
                            Date FromDate = StandardFormats.defaultDateFormat.parse(value);
                            value = new SimpleDateFormat(formattingList[i]).format(FromDate);
                        } catch (Exception ex) {
                            ErrorHandler.testError(ErrLvl.ERROR, ex, "Invalid Date Format!", testCaseVariables);
                        }
                    }
                }
            }
        } else if (value.toLowerCase().startsWith(SYSTEM_KEY.toLowerCase())) {
            value = value.substring(SYSTEM_KEY.length());

            if (StringUtils.startsWith(value.toLowerCase(), CUR_DATE_KEY.toLowerCase())) {

                String format = null;
                if (value.contains("[") && value.contains("]")) {
                    format = value.substring(value.indexOf("[") + 1, value.indexOf("]"));

                    value = StringUtils.replace(value, "[" + format + "]", "");
                }

                value = value.substring(CUR_DATE_KEY.length());

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());

                if (value.contains("(")) {

                    value = value.substring(1);
                    if (value.contains(")")) {
                        value = value.substring(0, value.length() - 1);
                    }
                    if (value.contains("M") || value.contains("m")) {
                        value = value.replace("M", "").replace("m", "");
                        int monthsDeviation = Integer.parseInt(value);
                        calendar.add(Calendar.MONTH, monthsDeviation);
                    } else if (value.contains("Y") || value.contains("y")) {
                        value = value.replace("Y", "").replace("y", "");
                        int yearsDeviation = Integer.parseInt(value);
                        calendar.add(Calendar.YEAR, yearsDeviation);
                    } else {
                        int daysDeviation = Integer.parseInt(value);
                        calendar.add(Calendar.DATE, daysDeviation);
                    }
                }

                value = StandardFormats.defaultDateFormat.format(calendar.getTime());
                Thread.sleep(50);

                if (StringUtils.isNotEmpty(format)) {
                    try {
                        Date FromDate = StandardFormats.defaultDateFormat.parse(value);
                        value = new SimpleDateFormat(format).format(FromDate);
                    } catch (Exception ex) {
                        ErrorHandler.testError(ErrLvl.ERROR, ex, "Invalid Date Format!", testCaseVariables);
                    }
                }
            }

        } else if (value.toLowerCase().startsWith(RANDOM_KEY.toLowerCase())) {

            String originalValue = value;
            value = value.substring(RANDOM_KEY.length());

            String varName = null;

            if (StringUtils.startsWith(value.toLowerCase(), RANDOM_NAME_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_NAME_KEY.length());
                value = rdc.randomName().toUpperCase();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_EMAIL_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_EMAIL_KEY.length());
                value = rdc.randomEmail("@test.com").toUpperCase();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_DATE_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_DATE_KEY.length());

                String randomRange = varName.substring(0, varName.lastIndexOf(")"));

                String format = null;
                if (varName.contains("[") && varName.contains("]")) {
                    format = varName.substring(varName.indexOf("[") + 1, varName.indexOf("]"));
                    varName = StringUtils.replace(varName, "[" + format + "]", "");
                }

                varName = varName.substring(varName.indexOf(".") + 1);
                String[] lowerUpperLimit = randomRange.split(",");
                int min = Integer.parseInt(lowerUpperLimit[0]);
                int max = Integer.parseInt(lowerUpperLimit[1]);

                value = rdc.randomDateBetween(min, max);
                Thread.sleep(50);

                tdm.setTestGlobal(varName, value);

                if (StringUtils.isNotEmpty(format)) {
                    try {
                        Date FromDate = StandardFormats.defaultDateFormat.parse(value);
                        value = new SimpleDateFormat(format).format(FromDate);
                    } catch (Exception ex) {
                        ErrorHandler.testError(ErrLvl.ERROR, ex, "Invalid Date Format!", testCaseVariables);
                    }
                }
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_NUMBER_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_NUMBER_KEY.length());
                String randomRange = varName.substring(0, varName.indexOf(")."));
                varName = varName.substring(varName.indexOf(").") + 2);
                String[] lowerUpperLimit = randomRange.split(",");
                long min = Long.parseLong(lowerUpperLimit[0]);
                long max = Long.parseLong(lowerUpperLimit[1]);

                if (min > max) {
                    ErrorHandler.testError(ErrLvl.FATAL, null,
                            "Min value '" + min + "' is greater than Max value '" + max + "'!", testCaseVariables);
                }

                value = rdc.randomNumberBetween(min, max).toString();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_SSN_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_SSN_KEY.length());

                value = rdc.randomSSN();

                Thread.sleep(50);
                tdm.setTestGlobal(varName, value);

                if (varName.contains(RANDOM_SSN_NO_HYPHEN_KEY)) {
                    varName = varName.substring(RANDOM_SSN_NO_HYPHEN_KEY.length());
                    value = value.replace("-", "");
                } else if (varName.contains(RANDOM_SSN_KEY_WITH_NO_HYPHEN)) {
                    varName = varName.replace(RANDOM_SSN_KEY_WITH_NO_HYPHEN, "");
                    value = value.replace("-", "");
                }

            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_SSN_KEY_WITH_NO_HYPHEN.toLowerCase())) {
                varName = value.substring(RANDOM_SSN_KEY_WITH_NO_HYPHEN.length());

                value = rdc.randomSSN();

                Thread.sleep(50);
                tdm.setTestGlobal(varName, value);

                value = value.replace("-", "");

            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_STREETNAME_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_STREETNAME_KEY.length());
                value = rdc.randomStreetName().toUpperCase();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_APTHOUSEOTH_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_APTHOUSEOTH_KEY.length());
                value = rdc.randomAptHouseOthNumber().toUpperCase();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_NONTX_CITY_KEY.toLowerCase())) {
                if (addressBlock == null) {
                    addressBlock = rdc.randomAddressBlock(State.NON_TEXAS);
                }
                varName = value.substring(RANDOM_NONTX_CITY_KEY.length());
                value = addressBlock.getCity();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_CITY_KEY.toLowerCase())) {
                if (addressBlock == null) {
                    addressBlock = rdc.randomAddressBlock(State.TEXAS);
                }
                varName = value.substring(RANDOM_CITY_KEY.length());
                value = addressBlock.getCity();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_COUNTY_KEY.toLowerCase())) {
                if (addressBlock == null || null == addressBlock.getCountyCode()) {
                    addressBlock = rdc.randomAddressBlock(State.TEXAS);
                }
                varName = value.substring(RANDOM_COUNTY_KEY.length());

                String format = null;

                if (varName.contains("[") && varName.contains("]")) {
                    format = varName.substring(varName.indexOf("[") + 1, varName.indexOf("]"));
                    varName = StringUtils.replace(varName, "[" + format + "]", "");
                }

                if (StringUtils.equalsIgnoreCase(format, "Decode")) {
                    value = addressBlock.getCountyDecode();
                } else {
                    value = addressBlock.getCountyCode();
                }

                value = addressBlock.getCity();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_NONTX_STATE_KEY.toLowerCase())) {
                if (addressBlock == null) {
                    addressBlock = rdc.randomAddressBlock(State.NON_TEXAS);
                }

                varName = value.substring(RANDOM_NONTX_STATE_KEY.length());

                String format = null;

                if (varName.contains("[") && varName.contains("]")) {
                    format = varName.substring(varName.indexOf("[") + 1, varName.indexOf("]"));
                    varName = StringUtils.replace(varName, "[" + format + "]", "");
                }

                if (StringUtils.equalsIgnoreCase(format, "Decode")) {
                    value = addressBlock.getStateDecode();
                } else {
                    value = addressBlock.getStateCode();
                }

                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_STATE_KEY.toLowerCase())) {
                if (addressBlock == null) {
                    addressBlock = rdc.randomAddressBlock(State.NON_TEXAS);
                }
                varName = value.substring(RANDOM_STATE_KEY.length());

                String format = null;

                if (varName.contains("[") && varName.contains("]")) {
                    format = varName.substring(varName.indexOf("[") + 1, varName.indexOf("]"));
                    varName = StringUtils.replace(varName, "[" + format + "]", "");
                }

                if (StringUtils.equalsIgnoreCase(format, "Decode")) {
                    value = "TEXAS";
                } else {
                    value = "TX";
                }

                value = "TX";
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_NONTX_ZIP_KEY.toLowerCase())) {
                if (addressBlock == null) {
                    addressBlock = rdc.randomAddressBlock(State.NON_TEXAS);
                }
                varName = value.substring(RANDOM_NONTX_ZIP_KEY.length());
                value = addressBlock.getZip();
                tdm.setTestGlobal(varName, value);
                addressBlock = null;
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_ZIP_KEY.toLowerCase())) {
                if (addressBlock == null) {
                    addressBlock = rdc.randomAddressBlock(State.TEXAS);
                }
                varName = value.substring(RANDOM_ZIP_KEY.length());
                value = addressBlock.getZip();
                tdm.setTestGlobal(varName, value);
                addressBlock = null;
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_EMPLOYERNAME_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_EMPLOYERNAME_KEY.length());
                value = rdc.randomEmployerOrOrganizationName().toUpperCase();
                tdm.setTestGlobal(varName, value);
            } else if (StringUtils.startsWith(value.toLowerCase(), RANDOM_ORGNAME_KEY.toLowerCase())) {
                varName = value.substring(RANDOM_ORGNAME_KEY.length());
                value = rdc.randomEmployerOrOrganizationName().toUpperCase();
                tdm.setTestGlobal(varName, value);
            } else {
                value = originalValue;
            }
        }

        return value;
    }

    public String checkParseabilityAndParse(String value) {
        String originalValue = value;

        if (value.toLowerCase().startsWith("select ")) {
            value = parseStatementsWithParseableValueInQuotes(value);
            SQLUtilities sql = new SQLUtilities();
            try {
                value = sql.executeSQLQuery(value);
            } catch (SQLException e) {
                ErrorHandler.testError(ErrLvl.ERROR, e, "SQL Execution Failed! Query: " + value, testCaseVariables);
            }
        } else {
            value = parseValue(value);
        }

        if (StringUtils.isBlank(value)) {
            ErrorHandler.testError(ErrLvl.FATAL, null, originalValue + " returned null value!", testCaseVariables);
        }
        return value;
    }

}
