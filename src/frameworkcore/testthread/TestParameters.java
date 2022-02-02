package frameworkcore.testthread;

import java.util.HashMap;

import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.WebDriverFactory.Browsers;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TestParameters {

    private String currentTestScript;
    private String currentTestSuite;
    private String currentOutputTestCase;
    private String currentTestDescription;
    private String currentAuthor;
    private Browsers browser;

    private int startStep = 0;
    private int endStep = 0;

    private HashMap<String, String> dataPreLoad = null;

    /**
     * @return the currentTestScript
     */
    public String getCurrentTestScript() {
        return currentTestScript;
    }

    /**
     * @param currentTestScript the currentTestScript to set
     */
    public void setCurrentTestScript(String currentTestScript) {
        this.currentTestScript = currentTestScript;
    }

    /**
     * @return the startStep
     */
    public int getStartStep() {
        return startStep;
    }

    /**
     * @param startStep the startStep to set
     */
    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }

    /**
     * @return the endStep
     */
    public int getEndStep() {
        return endStep;
    }

    /**
     * @param endStep the endStep to set
     */
    public void setEndStep(int endStep) {
        this.endStep = endStep;
    }

    public String getCurrentTestCaseOutput() {
        return currentOutputTestCase;
    }

    public void setCurrentTestCaseOutput(String currentTestcase) {
        currentOutputTestCase = currentTestcase;
    }

    public String getCurrentTestDescription() {
        return currentTestDescription;
    }

    public void setCurrentTestDescription(String currentTestDescription) {
        this.currentTestDescription = currentTestDescription;
    }

    public String getCurrentAuthor() {
        return currentAuthor;
    }

    public void setCurrentAuthor(String currentAuthor) {
        this.currentAuthor = currentAuthor;
    }

    public Browsers getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {

        switch (browser.toUpperCase()) {
        case "FIREFOX":
        case "FF":
        case "GECKO":
            this.browser = Browsers.FIREFOX;
            break;

        case "CHROME":
        case "CHROMIUM":
            this.browser = Browsers.CHROME;
            break;

        case "IE":
        case "INTERNET EXPLORER":
        case "INTERNETEXPLORER":
        case "IEXPLORER":
        case "IEXPLORE":
            this.browser = Browsers.IE;
            break;

        case "EDGE":
            this.browser = Browsers.EDGE;
            break;

        case "SAFARI":
            this.browser = Browsers.SAFARI;
            break;
        default:
            ErrorHandler.frameworkError(ErrLvl.FATAL, null, browser + " is not a supported browser!");
        }
    }

    public String getEnvironment() {
        return TestSettings.currentEnvironment;
    }

    public HashMap<String, String> getDataPreLoad() {
        return dataPreLoad;
    }

    public void setDataPreLoad(HashMap<String, String> dataPreLoad) {
        this.dataPreLoad = dataPreLoad;
    }

    public String getCurrentTestSuite() {
        return currentTestSuite;
    }

    public void setCurrentTestSuite(String currentTestSuite) {
        this.currentTestSuite = currentTestSuite;
    }
}
