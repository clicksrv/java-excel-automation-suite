package frameworkcore.testthread;

import java.util.ArrayList;
import java.util.HashMap;

import frameworkcore.reportutilities.LogWriter;
import frameworkcore.reportutilities.TestReporter;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TestCaseVariables {

    public HashMap<String, String> globalTestVariables = null;

    public int currentStepNumber = 0;
    public int currentPreDefinedFlowLevel = 0;
    public int screenshotCount = 0;

    public ArrayList<Integer> currentPreDefinedFlowStepNumber = new ArrayList<>();

    public String currentScript;
    public String currentTestCase;
    public String currentPageOrBJStreamKeyword;
    public String currentMethodKeyword;
    public String currentPortlet;
    public String currentBatchJob;

    public LogWriter logWriter;

    public TestReporter excelReporter;

}
