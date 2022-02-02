package frameworkextensions.screenelementhandlers.utilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import frameworkcore.testdatamanagement.ValueParser;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Ashish Vishwakarma (ashish.b.vishwakarma)
 */
public class InstructionParser extends TestThread {

    public InstructionParser(ThreadEntities threadEntities) {

        super(threadEntities);
    }

    ValueParser valueParser = new ValueParser(threadEntities);

    public Map<String, List<String>> parseInstruction(String instruction) {
        Map<String, List<String>> columnNameAndValueMap = new LinkedHashMap<String, List<String>>();
        if (instruction.contains("ConfirmRow")) {
            String beforeWhere = instruction.substring(0, instruction.toLowerCase().indexOf("where"));
            columnNameAndValueMap = processConfirmInstruction(columnNameAndValueMap, beforeWhere);
            String afterWhere = instruction.substring(instruction.toLowerCase().indexOf("where"));
            columnNameAndValueMap = processInstruction(columnNameAndValueMap, afterWhere);
        } else {
            parseInstructionForUpdateAndDelete(columnNameAndValueMap, instruction);
        }
        return columnNameAndValueMap;
    }

    private Map<String, List<String>> parseInstructionForUpdateAndDelete(
            Map<String, List<String>> columnNameAndValueMap, String instruction) {
        String instruction1 = null;
        if (StringUtils.containsIgnoreCase(instruction, "where")) {
            instruction1 = instruction.substring(0, instruction.toLowerCase().indexOf("where"));
            columnNameAndValueMap = parseInstructionBeforeWhere(columnNameAndValueMap, instruction1);
            instruction = instruction.substring(instruction.toLowerCase().indexOf("where"));
            return processInstruction(columnNameAndValueMap, instruction);
        } else {
            instruction1 = instruction;
            return parseInstructionBeforeWhere(columnNameAndValueMap, instruction1);
        }
    }

    private Map<String, List<String>> parseInstructionBeforeWhere(Map<String, List<String>> columnNameAndValueMap,
            String instruction1) {
        while (instruction1.trim().length() > 1) {
            String elementName = null;
            String actionName = null;
            String elementType = null;
            String actionXpath = null;
            String value = null;
            instruction1 = instruction1.substring(instruction1.indexOf("~") + 1);
            elementName = instruction1.substring(0, instruction1.indexOf("~"));
            instruction1 = instruction1.substring(instruction1.indexOf(elementName) + elementName.length());

            elementType = instruction1.substring(instruction1.indexOf(".") + 1, instruction1.indexOf("("));
            instruction1 = instruction1.substring(instruction1.indexOf("("));

            actionXpath = instruction1.substring(instruction1.indexOf("(") + 1, instruction1.indexOf(")"));
            instruction1 = instruction1.substring(instruction1.indexOf("."));

            actionName = instruction1.substring(instruction1.indexOf(".") + 1);
            if (actionName.contains("Assert Field Value") || actionName.contains("Assert Selected Dropdown Value")
                    || actionName.contains("Assert Selected Dropdown Values")
                    || actionName.contains("Assert Selected Radio Button")
                    || actionName.contains("Assert Selected Checkboxes")) {
                actionName = actionName.substring(0, actionName.indexOf("="));

                String temp = instruction1.substring(instruction1.indexOf("'") + 1);
                value = temp.substring(0, temp.indexOf("'"));

                temp = actionName + "='" + value + "'";
                instruction1 = instruction1.substring(instruction1.indexOf(temp) + temp.length());
            } else if (actionName.contains("Set Field Value") || actionName.contains("Select Radio Button")
                    || actionName.contains("Clear and Set Field Value")
                    || actionName.contains("Select Dropdown By Value")
                    || actionName.contains("Select Dropdown By Index") || actionName.contains("Select Checkboxes")) {
                actionName = actionName.substring(0, actionName.indexOf("="));
                instruction1 = instruction1.substring(instruction1.indexOf("=") + 1);

                instruction1 = instruction1.substring(instruction1.indexOf("'") + 1);
                value = instruction1.substring(0, instruction1.indexOf("'"));
                instruction1 = instruction1.substring(instruction1.indexOf(value) + value.length());
            } else {
                instruction1 = instruction1.substring(instruction1.indexOf(actionName) + actionName.length());
            }
            List<String> xpathList = new ArrayList<String>();
            xpathList.add(elementType.trim());
            xpathList.add(actionXpath.trim());
            xpathList.add(actionName.trim());
            if (StringUtils.isNotBlank(value)) {
                xpathList.add(valueParser.parseValue(value.trim()));
            } else {
                xpathList.add(value);
            }

            columnNameAndValueMap.put(elementName, xpathList);
        }
        return columnNameAndValueMap;
    }

    private Map<String, List<String>> processInstruction(Map<String, List<String>> columnNameAndValueMap,
            String instruction) {
        for (int i = 0; i < instruction.lastIndexOf("'"); i++) {
            List<String> valueList = new ArrayList<String>();
            instruction = instruction.substring(instruction.indexOf("~") + 1);
            String columnName = instruction.substring(0, instruction.indexOf("~"));
            instruction = instruction.substring(instruction.indexOf(columnName) + columnName.length());

            if (columnName.contains("[Span]")) {
                columnName = columnName.replaceAll("\\[Span]", "").trim();
            }

            String columnType = instruction.substring(instruction.indexOf(".") + 1, instruction.indexOf("("));
            instruction = instruction.substring(instruction.indexOf("(") + 1);

            String columnXpath = instruction.substring(0, instruction.indexOf(")"));
            instruction = instruction.substring(instruction.indexOf(")") + 1);

            instruction = instruction.substring(instruction.indexOf("'") + 1);
            String columnValue = instruction.substring(0, instruction.indexOf("'"));
            instruction = instruction.substring(instruction.indexOf(columnValue) + columnValue.length());

            valueList.add(columnType.trim());
            valueList.add(columnXpath.trim());
            if (StringUtils.isNotBlank(columnValue)) {
                valueList.add(valueParser.parseValue(columnValue.trim()));
            } else {
                valueList.add(columnValue);
            }

            columnNameAndValueMap.put(columnName, valueList);
        }
        return columnNameAndValueMap;
    }

    private Map<String, List<String>> processConfirmInstruction(Map<String, List<String>> columnNameAndValueMap,
            String instruction) {
        String columnName = null;
        String columnType = null;
        String columnXpath = null;
        String actionType = null;
        String columnValue = null;
        for (int i = 0; i < instruction.length(); i++) {
            instruction = instruction.substring(instruction.indexOf("~") + 1);
            columnName = instruction.substring(0, instruction.indexOf("~"));
            instruction = instruction.substring(instruction.indexOf(columnName) + columnName.length());

            columnType = instruction.substring(instruction.indexOf(".") + 1, instruction.indexOf("("));
            instruction = instruction.substring(instruction.indexOf("("));

            columnXpath = instruction.substring(instruction.indexOf("(") + 1, instruction.indexOf(")"));
            instruction = instruction.substring(instruction.indexOf(")"));
            if (instruction.contains("=")) {
                actionType = instruction.substring(instruction.indexOf(".") + 1, instruction.indexOf("="));
                instruction = instruction.substring(instruction.indexOf("=") + 1);
                instruction = instruction.substring(instruction.indexOf("'") + 1);
                columnValue = instruction.substring(0, instruction.indexOf("'"));
                instruction = instruction.substring(instruction.indexOf("'") + 1);
            } else {
                actionType = instruction.substring(instruction.indexOf(".") + 1);
                instruction = instruction.substring(instruction.indexOf(actionType) + actionType.length());
                columnValue = null;
            }

            List<String> xpathList = new ArrayList<String>();
            xpathList.add(columnType.trim());
            xpathList.add(columnXpath.trim());
            xpathList.add(actionType.trim());
            if (StringUtils.isNotBlank(columnValue)) {
                xpathList.add(valueParser.parseValue(columnValue.trim()));
            } else {
                xpathList.add(columnValue);
            }

            columnNameAndValueMap.put(columnName + "#Set", xpathList);
        }
        return columnNameAndValueMap;
    }

}
