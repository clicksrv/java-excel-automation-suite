package frameworkcore.testdatamanagement;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.FrameworkConstants;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TestDataManager extends TestThread {

    public TestDataManager(ThreadEntities threadEntities) {
        super(threadEntities);
    }

    ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
    public ApplicationSync applicationSync = new ApplicationSync(threadEntities);
    public WebElementManager elementManager = new WebElementManager(threadEntities);

    public void setTestGlobal(String varName, String valueORXpath) {

        if (!varName.isEmpty()) {
            if (testCaseVariables.globalTestVariables.containsKey(varName)) {

                if (TestSettings.testVariableOverwriteAllowed) {

                    if (valueORXpath.startsWith("//") || valueORXpath.startsWith(".//")) {
                        applicationSync.waitForApplicationToLoad();
                        valueORXpath = elementManager.getTextFromWebElement(valueORXpath);
                    }

                    testCaseVariables.globalTestVariables.put(varName, valueORXpath);
                    reportingManager.updateTestLog(Status.INFO, "Global Test Variable '"
                            + FrameworkConstants.HTMLFormat.FIELD + varName + FrameworkConstants.HTMLFormat.CLOSE
                            + "' exists with the value '" + FrameworkConstants.HTMLFormat.VALUE
                            + testCaseVariables.globalTestVariables.get(varName) + FrameworkConstants.HTMLFormat.CLOSE
                            + "' and is being over-written with '" + FrameworkConstants.HTMLFormat.VALUE + valueORXpath
                            + FrameworkConstants.HTMLFormat.CLOSE + "'.", false);
                } else {
                    String errorMsg = "Global Test Variable '" + FrameworkConstants.HTMLFormat.FIELD + varName
                            + FrameworkConstants.HTMLFormat.CLOSE + "' exists with the value '"
                            + FrameworkConstants.HTMLFormat.VALUE + testCaseVariables.globalTestVariables.get(varName)
                            + FrameworkConstants.HTMLFormat.CLOSE
                            + "' and over-writing is not permitted! Check script/preload data!";

                    reportingManager.updateTestLog(Status.FATAL, errorMsg, false);
                }
            } else {
                if (valueORXpath.startsWith("//") || valueORXpath.startsWith(".//")) {
                    applicationSync.waitForApplicationToLoad();
                    valueORXpath = elementManager.getTextFromWebElement(valueORXpath);
                }

                testCaseVariables.globalTestVariables.put(varName, valueORXpath);

                reportingManager.updateTestLog(Status.INFO,
                        "Global Test Variable '" + FrameworkConstants.HTMLFormat.FIELD + varName
                                + FrameworkConstants.HTMLFormat.CLOSE + "' set to value '"
                                + FrameworkConstants.HTMLFormat.VALUE + valueORXpath
                                + FrameworkConstants.HTMLFormat.CLOSE + "'.",
                        false);
            }
        }

    }

    public String getTestGlobal(String key) {

        if (testCaseVariables.globalTestVariables.containsKey(key)) {

            return testCaseVariables.globalTestVariables.get(key);

        } else {

            String errorMsg = key + " does not exist in the Global Test Variables!!!";
            ErrorHandler.testError(ErrLvl.ERROR, null, errorMsg, testCaseVariables);

            return null;
        }

    }

    public boolean checkTestGlobal(String key) {

        return testCaseVariables.globalTestVariables.containsKey(key);

    }

    public void removeTestGlobal(String key) {

        if (testCaseVariables.globalTestVariables.containsKey(key)) {
            testCaseVariables.globalTestVariables.remove(key);
        }

    }
}
