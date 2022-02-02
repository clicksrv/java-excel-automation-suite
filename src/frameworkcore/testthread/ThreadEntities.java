package frameworkcore.testthread;

import org.openqa.selenium.remote.RemoteWebDriver;

import frameworkcore.reportutilities.ReportingManager;
import frameworkcore.testdatamanagement.RandomDataCreator;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ThreadEntities {

    private RemoteWebDriver driver;

    private TestParameters testParameters;

    private TestCaseVariables testCaseVariables;

    private RandomDataCreator rdc;

    private ReportingManager reportingManager;

    public ThreadEntities(RemoteWebDriver driver, TestParameters testParameters, TestCaseVariables testCaseVariables,
            RandomDataCreator rdc, ReportingManager reportingManager) {
        this.driver = driver;
        this.testParameters = testParameters;
        this.testCaseVariables = testCaseVariables;
        this.rdc = rdc;
        this.reportingManager = reportingManager;
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public TestParameters getTestParameters() {
        return testParameters;
    }

    public TestCaseVariables getTestCaseVariables() {
        return testCaseVariables;
    }

    public RandomDataCreator getRandomDataCreator() {
        return rdc;
    }

    public ReportingManager getReportingManager() {
        return reportingManager;
    }
}
