package frameworkextensions.screenelementhandlers.utilities;

import java.util.List;

import com.aventstack.extentreports.Status;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ApplicationSync extends TestThread {

    public ApplicationSync(ThreadEntities threadEntities) {
        super(threadEntities);
    }

    public void waitForApplicationToLoad() {

        boolean waited = false;

        ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };

        Wait<WebDriver> wait = new WebDriverWait(driver, FrameworkTimeouts.pageLoadTimeOut);

        try {
            wait.until(expectation);
        } catch (Throwable error) {
            ErrorHandler.testError(ErrLvl.FATAL, new Exception(error), "Error thrown while waiting for page to load!",
                    testCaseVariables);
        }

        if (TestSettings.waitForJSLoad) {
            List<WebElement> loadingElements = driver.findElements(
                    By.xpath(FrameworkPropertyFiles.frameworkProperties.getProperty("LoadingElementXpath")));

            for (int i = 0; i < loadingElements.size(); i++) {
                try {
                    if (loadingElements.get(i).isDisplayed()) {

                        startTimer();
                        waited = true;

                        try {
                            wait = new WebDriverWait(driver, FrameworkTimeouts.pageLoadTimeOut);
                            wait.until(ExpectedConditions.invisibilityOf(loadingElements.get(i)));
                            break;
                        } catch (TimeoutException e) {
                            reportingManager.updateTestLogFatalWithException(e,
                                    "Loading Pop-up did not disappear within timeout period of "+FrameworkTimeouts.pageLoadTimeOut+" seconds!", true);
                        }
                    }
                } catch (StaleElementReferenceException | NoSuchElementException ex) {
                }

            }

            stopTimerAndReturnElapsedTime();

            if (waited) {
                reportingManager.updateTestLog(Status.INFO, "JS call took " + stopTimerAndReturnElapsedTime() + ".",
                        false);
            }
        }

    }

}
