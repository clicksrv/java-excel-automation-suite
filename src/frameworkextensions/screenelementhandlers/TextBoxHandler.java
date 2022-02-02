package frameworkextensions.screenelementhandlers;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.WebDriverFactory.Browsers;
import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testdatamanagement.TestDataManager;
import frameworkcore.testdatamanagement.ValueParser;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.JSUtilities;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo), Vedhanth Reddy
 *         (v.reddy.monajigari)
 */
public class TextBoxHandler extends TestThread implements IWebElementHandler {

    private static final String USE_ELEM_NAME_NO_VAR_NAME_STRING = "Global Variable not specified, using Element Name!";

    public TextBoxHandler(ThreadEntities threadEntities) {
        super(threadEntities);
    }

    ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
    JSUtilities jsUtilities = new JSUtilities(threadEntities);
    TextHTMLWrapper wrapInHTML = new TextHTMLWrapper();
    WebElementManager elementManager = new WebElementManager(threadEntities);
    ApplicationSync applicationSync = new ApplicationSync(threadEntities);

    /**
     * This method is used to enter value into textbox/textarea available on the
     * screen.
     * 
     * @param actionType - This parameter is of String data type
     * @param value - This parameter is of String data type
     * @param xpath - This parameter is of String data type
     * @param elementLabel - This parameter is of String data type
     */
    @Override
    public void handleElement(ScriptActionBlock actionBlock) {

        String actionType = actionBlock.getAction();
        String value = actionBlock.getValue();
        String xpath = actionBlock.getElementXpath();
        String elementLabel = actionBlock.getElementName();

        WebElement we = null;
        TestDataManager tdm = null;

        switch (actionType) {

        case "Clear and Set Field Value":
            applicationSync.waitForApplicationToLoad();
            we = elementManager.waitAndFindElement(xpath);
            if (elementManager.isElementPresent(we)) {

                int i = 0;
                boolean inputSuccess = false;
                boolean elementDisappeared = false;
                tdm = new TestDataManager(threadEntities);

                do {
                    if (null == we && i > 0) {
                        reportingManager.updateTestLog(Status.WARNING,
                                getElementDisappearedString(xpath, elementLabel, value), true);
                        break;
                    }

                    we.click();

                    if (testParameters.getBrowser().equals(Browsers.FIREFOX)) {
                    	we.sendKeys(Keys.CONTROL + "a");
                    	we.sendKeys(value);
                    	we.sendKeys(Keys.TAB);
                    } else {
                    	we.sendKeys(Keys.chord(Keys.CONTROL, "a"), value, Keys.TAB);
                    }

                    applicationSync.waitForApplicationToLoad();

                    i++;

                    boolean elementStale = false;
                    String returnedValue = null;
                    do {
                        elementStale = false;

                        try {
                            returnedValue = tdm.elementManager.getTextFromWebElement(we).trim();
                        } catch (StaleElementReferenceException e) {
                            elementStale = true;
                            try {
                                we = driver.findElement(By.xpath(xpath));
                            } catch (NoSuchElementException e2) {
                                reportingManager.updateTestLog(Status.WARNING,
                                        getElementDisappearedString(xpath, elementLabel, value), true);
                                elementDisappeared = true;
                                break;
                            }
                        }
                    } while (elementStale);

                    if (elementDisappeared) {
                        break;
                    }

                    if (null != returnedValue) {
                        inputSuccess = elementManager.compareInputAndReturnedValue(value, returnedValue);
                    }

                    if (!inputSuccess) {
                        String mismatchString = getInputMismatchWarningString(value, elementLabel, i, returnedValue);

                        reportingManager.updateTestLog(Status.WARNING, mismatchString, false);
                    }

                } while (i < 5 && !inputSuccess);

                if (inputSuccess) {
                    reportingManager.updateTestLog(Status.PASS,
                            getClearedAndEnteredSuccessfullyString(value, elementLabel), false);
                } else if (!elementDisappeared) {
                    reportingManager.updateTestLog(Status.FAIL, getExactInputFailureString(value, elementLabel), true);
                }
                break;

            } else {
                reportingManager.updateTestLog(Status.FAIL, getMissingElementString(xpath, elementLabel), true);
                break;
            }
        case "Set Field Value":
            applicationSync.waitForApplicationToLoad();
            we = elementManager.waitAndFindElement(xpath);
            if (elementManager.isElementPresent(we)) {

                int i = 0;
                boolean inputSuccess = false;
                boolean elementDisappeared = false;
                tdm = new TestDataManager(threadEntities);

                do {
                    if (null == we && i > 0) {
                        reportingManager.updateTestLog(Status.WARNING,
                                getElementDisappearedString(xpath, elementLabel, value), true);
                        break;
                    }

                    we.click();

                    if (i > 0) {
                        we.sendKeys(value, Keys.TAB);
                    } else {
                    	if (testParameters.getBrowser().equals(Browsers.FIREFOX)) {
                        	we.sendKeys(Keys.CONTROL + "a");
                        	we.sendKeys(value);
                        	we.sendKeys(Keys.TAB);
                        } else {
                        	we.sendKeys(Keys.chord(Keys.CONTROL, "a"), value, Keys.TAB);
                        }
                    }

                    applicationSync.waitForApplicationToLoad();

                    i++;

                    boolean elementStale = false;
                    String returnedValue = null;
                    do {
                        elementStale = false;

                        try {
                            returnedValue = tdm.elementManager.getTextFromWebElement(we).trim();
                        } catch (StaleElementReferenceException e) {
                            elementStale = true;
                            try {
                                we = driver.findElement(By.xpath(xpath));
                            } catch (NoSuchElementException e2) {
                                reportingManager.updateTestLog(Status.WARNING,
                                        getElementDisappearedString(xpath, elementLabel, value), true);
                                elementDisappeared = true;
                                break;
                            }
                        }
                    } while (elementStale);

                    if (elementDisappeared) {
                        break;
                    }

                    if (null != returnedValue) {
                        inputSuccess = elementManager.compareInputAndReturnedValue(value, returnedValue);
                    }

                    if (!inputSuccess) {
                        String mismatchString = getInputMismatchWarningString(value, elementLabel, i, returnedValue);

                        reportingManager.updateTestLog(Status.WARNING, mismatchString, false);
                    }

                } while (i < 5 && !inputSuccess);

                if (inputSuccess) {
                    reportingManager.updateTestLog(Status.PASS,
                            getClearedAndEnteredSuccessfullyString(value, elementLabel), false);
                } else if (!elementDisappeared) {
                    reportingManager.updateTestLog(Status.FAIL, getExactInputFailureString(value, elementLabel), true);
                }
                break;

            } else {
                reportingManager.updateTestLog(Status.FAIL, getMissingElementString(xpath, elementLabel), true);
                break;
            }

        case "Set Value To GlobalVariable":
            tdm = new TestDataManager(threadEntities);
            if (StringUtils.isNotBlank(value)) {

                if (value.startsWith(ValueParser.GLOBAL_KEY)) {
                    value = value.substring(ValueParser.GLOBAL_KEY.length());
                }

                tdm.setTestGlobal(value, xpath);
            } else {
                reportingManager.updateTestLog(Status.INFO, getUsingElementNameForGlobalVarString(), false);
                tdm.setTestGlobal(elementLabel, xpath);
            }
            break;

        case "Assert Field Value":
            applicationSync.waitForApplicationToLoad();

            we = elementManager.waitAndFindElement(xpath, elementLabel);
            if (null == we) {
                reportingManager.updateTestLog(Status.FAIL, getMissingElementString(xpath, elementLabel), true);
                break;
            }

            tdm = new TestDataManager(threadEntities);
            String returnedValue = tdm.elementManager.getTextFromWebElement(we);

            if (elementManager.compareInputAndReturnedValue(value, returnedValue)) {
                reportingManager.updateTestLog(Status.PASS, getAssertSuccessString(value, elementLabel, returnedValue),
                        false);
            } else {
                reportingManager.updateTestLog(Status.FAIL, getAssertFailureString(value, elementLabel, returnedValue),
                        true);
            }
            break;

        case "Click":
            we = elementManager.waitAndFindElement(xpath);

            if (null == we) {
                reportingManager.updateTestLog(Status.FAIL, getMissingElementString(xpath, elementLabel), true);
                break;
            }

            we.click();

            reportingManager.updateTestLog(Status.PASS, getClickedString(elementLabel), false);
            break;

        case "Check if Field is Enabled":
            checkIfFieldIsEnabled(xpath, value, elementLabel);
            break;

        case "Check if Field is Disabled":
            checkIfFieldIsDisabled(xpath, value, elementLabel);
            break;

        default:
            reportingManager.updateTestLog(Status.WARNING, getInvalidActionTypeString(actionType, elementLabel), true);
            break;

        }

    }

    private void checkIfFieldIsEnabled(String xpath, String value, String elementLabel) {
        WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

        if (we.isDisplayed() && we.isEnabled()) {
            reportingManager.updateTestLog(Status.PASS, getEnabledString(elementLabel), false);
        } else if (we.isDisplayed() && !we.isEnabled()) {
            reportingManager.updateTestLog(Status.FAIL, getDisabledString(elementLabel), true);
        } else {
            reportingManager.updateTestLog(Status.FAIL, getMissingElementString(xpath, elementLabel), true);
        }
    }

    private void checkIfFieldIsDisabled(String xpath, String value, String elementLabel) {
        WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

        if (we.isDisplayed() && we.isEnabled()) {
            reportingManager.updateTestLog(Status.FAIL, getEnabledString(elementLabel), true);
        } else if (we.isDisplayed() && !we.isEnabled()) {
            reportingManager.updateTestLog(Status.PASS, getDisabledString(elementLabel), false);
        } else {
            reportingManager.updateTestLog(Status.FAIL, getMissingElementString(xpath, elementLabel), true);
        }
    }

    private String getUsingElementNameForGlobalVarString() {
        return USE_ELEM_NAME_NO_VAR_NAME_STRING;
    }

    private String getDisabledString(String elementLabel) {
        return elementLabel + " is displayed but is not enabled!";
    }

    private String getEnabledString(String elementLabel) {
        return elementLabel + " is Enabled!";
    }

    private String getClickedString(String elementLabel) {
        return "User was able to click on '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' field.";
    }

    private String getInvalidActionTypeString(String actionType, String elementLabel) {
        return "'" + actionType + "' in step number '" + (testCaseVariables.currentStepNumber + 1)
                + "' is invalid for Element: '" + elementLabel + "'!";
    }

    private String getAssertFailureString(String value, String elementLabel, String returnedValue) {
        return "Expected value of field: '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' was: '"
                + TextHTMLWrapper.wrapValue(value) + " and it did not match the returned value: '"
                + TextHTMLWrapper.wrapValue(returnedValue) + "'!";
    }

    private String getAssertSuccessString(String value, String elementLabel, String returnedValue) {
        return "Expected value of field: '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' was: '"
                + TextHTMLWrapper.wrapValue(value) + "' and it matched the returned value: '"
                + TextHTMLWrapper.wrapValue(returnedValue) + "'!";
    }

    private String getInputMismatchWarningString(String value, String elementLabel, int i, String returnedValue) {
        return "Attempt " + i + ": Value returned from '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' is '"
                + TextHTMLWrapper.wrapValue(returnedValue) + "'; Value entered was '" + TextHTMLWrapper.wrapValue(value)
                + "'.";
    }

    private String getMissingElementString(String xpath, String elementLabel) {
        return "Web Element with xpath '" + TextHTMLWrapper.wrapValue(xpath) + "' for '"
                + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' is not present on the screen!";
    }

    private String getExactInputFailureString(String value, String elementLabel) {
        return "Failed to enter value '" + TextHTMLWrapper.wrapValue(value) + "' into '"
                + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' !";
    }

    private String getClearedAndEnteredSuccessfullyString(String value, String elementLabel) {
        return "User cleared and entered '" + TextHTMLWrapper.wrapValue(value) + "' in the field '"
                + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.";
    }

    private String getElementDisappearedString(String xpath, String elementLabel, String value) {
        return "WebElement '" + elementLabel + "' with xpath: '" + xpath + "' was attempted to be filled with value '"
                + value + "' but disappeared after typing in the value and could not be validated!";
    }

}
