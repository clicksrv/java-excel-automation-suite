package frameworkextensions.screenelementhandlers.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import frameworkcore.datablocks.TreeTableDTO;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Ashish Vishwakarma (ashish.b.vishwakarma)
 */
public class TreeTableInstructionParser extends TestThread {

    public TreeTableInstructionParser(ThreadEntities threadEntities) {
        super(threadEntities);
    }

    public TreeTableDTO parseTreeTableInstruction(String instruction) {

        String beforeWhere = instruction.substring(0, instruction.indexOf("where"));
        String nodePath = instruction.substring(instruction.indexOf("nodepath"), instruction.lastIndexOf("["));
        String afterNodepath = instruction.substring(instruction.lastIndexOf("[") + 1, instruction.lastIndexOf("]"));

        TreeTableDTO treeTable = new TreeTableDTO();
        Map<String, List<String>> beforeWhereMap = new HashMap<String, List<String>>();
        Map<String, List<String>> nodePathMap = new LinkedHashMap<String, List<String>>();
        Map<String, List<String>> afterNodepathMap = new HashMap<String, List<String>>();

        beforeWhereMap = parseBeforeWhereInstruction(beforeWhereMap, beforeWhere);
        nodePathMap = parseNodepathInstruction(nodePathMap, nodePath);
        afterNodepathMap = parseAfterNodepathInstruction(afterNodepathMap, afterNodepath);

        treeTable.setBeforeWhereMap(beforeWhereMap);
        treeTable.setNodePathMap(nodePathMap);
        treeTable.setAfterNodepathMap(afterNodepathMap);

        return treeTable;
    }

    private Map<String, List<String>> parseBeforeWhereInstruction(Map<String, List<String>> beforeWhereMap,
            String beforeWhere) {
        while (beforeWhere.trim().length() > 1) {
            String elementName = null;
            String actionName = null;
            String elementType = null;
            String actionXpath = null;
            String value = null;
            if (beforeWhere.contains("~")) {
                beforeWhere = beforeWhere.substring(beforeWhere.indexOf("~") + 1);
                elementName = beforeWhere.substring(0, beforeWhere.indexOf("~"));
                if (elementName.contains("-Span")) {
                    elementName = elementName.replaceAll("\\-Span", "").trim();
                }
                beforeWhere = beforeWhere.substring(beforeWhere.indexOf(elementName) + elementName.length());

                elementType = beforeWhere.substring(beforeWhere.indexOf(".") + 1, beforeWhere.indexOf("("));
                beforeWhere = beforeWhere.substring(beforeWhere.indexOf("("));

                actionXpath = beforeWhere.substring(beforeWhere.indexOf("(") + 1, beforeWhere.indexOf(")"));
                beforeWhere = beforeWhere.substring(beforeWhere.indexOf("."));

                actionName = beforeWhere.substring(beforeWhere.indexOf(".") + 1);
                if (actionName.contains("AssertValue")) {
                    actionName = actionName.substring(0, actionName.indexOf("="));

                    String temp = beforeWhere.substring(beforeWhere.indexOf("'") + 1);
                    value = temp.substring(0, temp.indexOf("'"));

                    temp = actionName + "='" + value + "'";
                    beforeWhere = beforeWhere.substring(beforeWhere.indexOf(temp) + temp.length());
                } else {
                    beforeWhere = beforeWhere.substring(beforeWhere.indexOf(actionName) + actionName.length());
                }
            } else {
                beforeWhere = beforeWhere.trim();
                beforeWhere = beforeWhere.substring(beforeWhere.indexOf(" ") + 1);
                String[] temp = beforeWhere.split("\\.");
                elementName = temp[0];
                actionName = temp[1];
                beforeWhere = "";
            }
            List<String> xpathList = new ArrayList<String>();
            xpathList.add(elementType);
            xpathList.add(actionXpath);
            xpathList.add(actionName);
            xpathList.add(value);

            beforeWhereMap.put(elementName, xpathList);
        }

        return beforeWhereMap;
    }

    private Map<String, List<String>> parseNodepathInstruction(Map<String, List<String>> nodePathMap, String nodePath) {
        nodePath = nodePath.substring(0, nodePath.indexOf("AND"));
        while (nodePath.trim().length() > 1) {
            String nodeName = null;
            String nodeType = null;
            String nodeXpath = null;
            nodePath = nodePath.substring(nodePath.indexOf("'") + 1);
            nodeName = nodePath.substring(0, nodePath.indexOf("'"));
            nodePath = nodePath.substring(nodePath.indexOf(nodeName) + nodeName.length());

            nodeType = nodePath.substring(nodePath.indexOf(".") + 1, nodePath.indexOf("("));
            nodePath = nodePath.substring(nodePath.indexOf(nodeType) + nodeType.length());

            if (nodePath.contains("[")) {
                nodeXpath = nodePath.substring(nodePath.indexOf("(") + 1, nodePath.indexOf("]") + 1);
            } else {
                nodeXpath = nodePath.substring(nodePath.indexOf("(") + 1, nodePath.indexOf(")"));
            }
            nodePath = nodePath.substring(nodePath.indexOf(nodeXpath) + nodeXpath.length());

            List<String> nodeList = new ArrayList<String>();
            nodeList.add(nodeType);
            nodeList.add(nodeXpath);

            nodePathMap.put(nodeName, nodeList);
        }

        return nodePathMap;
    }

    private Map<String, List<String>> parseAfterNodepathInstruction(Map<String, List<String>> afterNodepathMap,
            String afterNodepath) {
        for (int i = 0; i < afterNodepath.lastIndexOf("'"); i++) {
            List<String> valueList = new ArrayList<String>();
            afterNodepath = afterNodepath.substring(afterNodepath.indexOf("~") + 1);
            String columnName = afterNodepath.substring(0, afterNodepath.indexOf("~"));
            afterNodepath = afterNodepath.substring(afterNodepath.indexOf(columnName) + columnName.length());

            if (columnName.contains("[Span]")) {
                columnName = columnName.replaceAll("\\[Span]", "").trim();
            }

            String columnType = afterNodepath.substring(afterNodepath.indexOf(".") + 1, afterNodepath.indexOf("("));
            afterNodepath = afterNodepath.substring(afterNodepath.indexOf("(") + 1);

            String columnXpath = afterNodepath.substring(0, afterNodepath.indexOf(")"));
            afterNodepath = afterNodepath.substring(afterNodepath.indexOf(")") + 1);

            afterNodepath = afterNodepath.substring(afterNodepath.indexOf("'") + 1);
            String columnValue = afterNodepath.substring(0, afterNodepath.indexOf("'"));
            afterNodepath = afterNodepath.substring(afterNodepath.indexOf(columnValue) + columnValue.length());

            valueList.add(columnType);
            valueList.add(columnXpath);
            valueList.add(columnValue);

            afterNodepathMap.put(columnName, valueList);
        }
        return afterNodepathMap;
    }

}
