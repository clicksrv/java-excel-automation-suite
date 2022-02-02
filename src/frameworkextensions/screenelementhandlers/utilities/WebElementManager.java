package frameworkextensions.screenelementhandlers.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.Status;
import com.google.common.base.Function;

import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkConstants.HTMLFormat;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.errormanagers.TestException;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class WebElementManager extends TestThread {

    private static final String NUMERIC_REGEX_CHECK = "[(\\$)?(\\,)?(\\-)?]";

    public WebElementManager(ThreadEntities threadEntities) {
        super(threadEntities);
        applicationSync = new ApplicationSync(threadEntities);
    }

    ApplicationSync applicationSync;

    /**
     * Waits for the element timeout period specified in the
     * framework_settings.properties or the developer_settings.properties. <br>
     * <br>
     * To only be used <strong>for</strong> Screen Handlers.
     * 
     * @param xpath
     * @param elementLabel
     * @return WebElement
     */
    public WebElement waitAndFindElement(final String xpath, String elementLabel) {

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(FrameworkTimeouts.elementTimeOut, TimeUnit.SECONDS)
                .pollingEvery(250, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

        WebElement webElement = null;

        try {
            webElement = wait.until(new Function<WebDriver, WebElement>() {

                @Override
                public WebElement apply(WebDriver driver) {
                    WebElement we = driver.findElement(By.xpath(xpath));
                    waitUntilElementAppears(we);
                    return we;
                }
            });
        } catch (TimeoutException e) {
            webElement = null;
            reportingManager.updateTestLog(Status.FAIL,
                    "Element " + HTMLFormat.FIELD + elementLabel + HTMLFormat.CLOSE
                            + " with the following xpath was not found : " + HTMLFormat.VALUE + xpath + HTMLFormat.CLOSE
                            + " within " + FrameworkTimeouts.elementTimeOut + " seconds!",
                    true);
        }

        return webElement;

    }

    /**
     * Waits for the element timeout period specified in the
     * framework_settings.properties or the developer_settings.properties. <br>
     * <br>
     * To only be used for element finding <strong>outside</strong> of Element
     * Handlers.
     * 
     * @param xpath
     * @param doNotHandleNotFoundError
     * @return WebElement
     */
    public WebElement waitAndFindElement(final String xpath, boolean doNotHandleNotFoundError) {

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(FrameworkTimeouts.elementTimeOut, TimeUnit.SECONDS)
                .pollingEvery(250, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

        WebElement webElement = null;
        try {
            webElement = wait.until(new Function<WebDriver, WebElement>() {

                @Override
                public WebElement apply(WebDriver driver) {
                    WebElement we = driver.findElement(By.xpath(xpath));
                    waitUntilElementAppears(we);
                    return we;
                }
            });
        } catch (TimeoutException e) {
            webElement = null;
            if (!doNotHandleNotFoundError) {
                reportingManager.updateTestLog(Status.FAIL,
                        "Element with the following xpath was not found : " + HTMLFormat.VALUE + xpath
                                + HTMLFormat.CLOSE + " within " + FrameworkTimeouts.elementTimeOut + " seconds!",
                        true);
            }
        }

        return webElement;

    }

    public WebElement waitAndFindElement(final String xpath) {

        return waitAndFindElement(xpath, false);

    }

    /**
     * Waits for the element timeout period specified in the
     * framework_settings.properties or the developer_settings.properties. <br>
     * <br>
     * To only be used <strong>internally for for finding sub-elements</strong> in
     * Screen Handlers.
     */
    public List<WebElement> waitAndFindElements(final String xpath) {
        List<WebElement> weList = null;
        int i = 0;
        int timeLimit = FrameworkTimeouts.elementTimeOut / 10;

        do {
            if (i > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.println("Thread Interrupted!!");
                }
            }

            weList = driver.findElements(By.xpath(xpath));
            i++;
        } while (weList.size() > 0 && i < timeLimit);

        if (weList.size() == 0) {
            reportingManager.updateTestLog(Status.FAIL,
                    "No Elements Found for: " + HTMLFormat.VALUE + xpath + HTMLFormat.CLOSE + "!", true);
            return null;
        }
        return weList;
    }

    public void waitUntilElementAppears(WebElement we) {
        try {
            if (driver.getCurrentUrl().contains("CSI")) {
                applicationSync.waitForApplicationToLoad();
            }
        } catch (TimeoutException e) {
            reportingManager.updateTestLog(Status.FAIL,
                    "Element not visible due to timeout error, as Timeout error have occurred!!" + e, true);
            System.out.println("Time out exception" + e);
            throw new TestException("");
        } catch (Exception e) {
            System.out.println("Element not present");
        }
    }

    public void waitUntilElementsAppears(List<WebElement> we) {
        try {
            applicationSync.waitForApplicationToLoad();

            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.visibilityOfAllElements(we));
        } catch (TimeoutException e) {
            reportingManager.updateTestLog(Status.FATAL, we + "" + "Elements did not appear within timeout period!",
                    true);
        } catch (Exception e) {
            System.out.println("Element not present");
        }
    }

    public boolean isElementPresent(WebElement we) {

        if (null == we) {
            return false;
        }

        return true;
    }

    @Deprecated
    public boolean isElementPresent_quickCheck(WebElement we) {

        if (null == we) {
            return false;
        }

        return true;

    }

    public boolean compareInputAndReturnedValue(String expectedValue, String actualValue) {

        if (null == expectedValue || null == actualValue) {
            return false;
        }

        expectedValue = expectedValue.trim();
        actualValue = actualValue.trim();

        boolean inputSuccess = false;
        double numericValue = 0;
        double numericReturnedValue = 0;
        boolean valuesNumeric = true;
        try {
            numericValue = Double.parseDouble(expectedValue.trim().replaceAll(NUMERIC_REGEX_CHECK, ""));
            numericReturnedValue = Double.parseDouble(actualValue.trim().replaceAll(NUMERIC_REGEX_CHECK, ""));
        } catch (NumberFormatException e) {
            valuesNumeric = false;
        }

        if (valuesNumeric) {
            inputSuccess = (numericValue == numericReturnedValue);
        } else {
            inputSuccess = StringUtils.equals(expectedValue, actualValue);
        }
        return inputSuccess;
    }

    public String getTextFromWebElement(String xpath) {
        WebElement we = null;

        we = waitAndFindElement(xpath);
        if (null == we) {
            return "";
        }

        applicationSync.waitForApplicationToLoad();
        xpath = we.getText();

        if (xpath.isEmpty()) {
            xpath = we.getAttribute("value");
        }

        if (xpath.isEmpty()) {
            String errorMsg = "No value found in text or value attribute of element: " + we.toString();
            ErrorHandler.testError(ErrLvl.ERROR, null, errorMsg, testCaseVariables);
        }
        return xpath;
    }

    public String getTextFromWebElement(WebElement we) {

        applicationSync.waitForApplicationToLoad();
        String value = we.getText();

        if (value.isEmpty()) {
            value = we.getAttribute("value");
        }

        return value;
    }

}
