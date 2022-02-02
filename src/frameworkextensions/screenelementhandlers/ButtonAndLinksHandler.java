package frameworkextensions.screenelementhandlers;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testdatamanagement.TestDataManager;
import frameworkcore.testdatamanagement.ValueParser;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator.MessageType;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ButtonAndLinksHandler extends TestThread implements IWebElementHandler {

	public ButtonAndLinksHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator msgValidator = new ApplicationMessageValidator(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	private static final String USE_ELEM_NAME_NO_VAR_NAME_STRING = "Global Variable not specified, using Element Name!";

	/**
	 * This method is use to handle all types of button and links available on the
	 * screen.
	 * 
	 * @param actionType   - This parameter is of String data type
	 * @param value        - This parameter is of String data type
	 * @param xpath        - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	@Override
	public void handleElement(ScriptActionBlock actionBlock) {

		String actionType = actionBlock.getAction();
		String value = actionBlock.getValue();
		String xpath = actionBlock.getElementXpath();
		String elementLabel = actionBlock.getElementName();
		String elementType = actionBlock.getElementType();

		WebElement we = null;
		TestDataManager tdm = new TestDataManager(threadEntities);

		switch (actionType) {
		case "Click":
			we = elementManager.waitAndFindElement(xpath, elementLabel);
			clickOnWebElement(elementLabel, elementType, we, MessageType.NO_SPECIFIC, null);
			break;

		case "Click and Handle Warning":
			we = elementManager.waitAndFindElement(xpath, elementLabel);
			clickOnWebElementAndHandleWarning(elementLabel, elementType, we, MessageType.NO_SPECIFIC, null);
			break;

		case "Assert Field Value":
			applicationSync.waitForApplicationToLoad();
			we = elementManager.waitAndFindElement(xpath, elementLabel);

			if (null == we) {
				reportingManager.updateTestLog(Status.FAIL, getElementNotFoundString(elementLabel, elementType), true);
				break;
			}

			String returnedValue = tdm.elementManager.getTextFromWebElement(we);

			if (elementManager.compareInputAndReturnedValue(value, returnedValue)) {
				reportingManager.updateTestLog(Status.PASS, getAssertSuccessString(value, returnedValue, elementType),
						true);
			} else {
				reportingManager.updateTestLog(Status.FAIL, getAssertFailureString(value, returnedValue, elementType),
						true);
			}
			break;

		case "Check if Field is Enabled":
			checkIfFieldIsEnabled(xpath, value, elementLabel, elementType);
			break;

		case "Check if Field is Disabled":
			checkIfFieldIsDisabled(xpath, value, elementLabel, elementType);
			break;

		case "Click and Expect Message":
			we = elementManager.waitAndFindElement(xpath, elementLabel);
			clickOnWebElement(elementLabel, elementType, we, MessageType.MESSAGE, value);
			break;

		case "Click and Expect Warning":
			we = elementManager.waitAndFindElement(xpath, elementLabel);
			clickOnWebElement(elementLabel, elementType, we, MessageType.WARNING, value);
			break;

		case "Click and Expect Error":
			we = elementManager.waitAndFindElement(xpath, elementLabel);
			clickOnWebElement(elementLabel, elementType, we, MessageType.ERROR, value);
			break;

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

		default:
			reportingManager.updateTestLog(Status.WARNING, getInvalidActionTypeString(actionType, elementType), false);
			break;

		}

	}

	public void clickOnWebElement(String elementLabel, String elementType, WebElement we, MessageType msgType,
			String expectedMsg) {
		if (elementManager.isElementPresent(we)) {

			if (we.isEnabled()) {
				startTimer();

				reportingManager.updateTestLog(Status.PASS, getClickedString(elementLabel, elementType), true);

				we.click();

				applicationSync.waitForApplicationToLoad();

				reportingManager.updateTestLog(Status.INFO, getLoadTimeString(elementLabel, elementType), true);

				msgValidator.checkForDisplayedMessages(msgType, expectedMsg);
			} else {
				reportingManager.updateTestLog(Status.FATAL, getElementDisabledString(elementLabel), true);
			}
		} else {
			reportingManager.updateTestLog(Status.FATAL, getElementNotFoundString(elementLabel, elementType), true);
		}
	}

	public void clickOnWebElementAndHandleWarning(String elementLabel, String elementType, WebElement we,
			MessageType msgType, String expectedMsg) {

		String buttonId = null;

		if (elementManager.isElementPresent(we)) {

			buttonId = we.getAttribute("id");

			if (we.isEnabled()) {
				startTimer();

				reportingManager.updateTestLog(Status.PASS, getClickedString(elementLabel, elementType), true);

				we.click();

				applicationSync.waitForApplicationToLoad();

				reportingManager.updateTestLog(Status.INFO, getLoadTimeString(elementLabel, elementType), true);

				List<MessageType> displayedMessages = msgValidator.checkForDisplayedMessages(msgType, expectedMsg);

				if (!msgType.equals(MessageType.WARNING) && displayedMessages.contains(MessageType.WARNING)) {
					try {
						we = driver.findElement(By.xpath(".//*[@id='" + buttonId + "']"));
					} catch (NoSuchElementException e) {
						we = null;
					}

					if (null != we) {
						reportingManager.updateTestLog(Status.PASS, getClickedString(elementLabel, elementType), true);

						we.click();

						applicationSync.waitForApplicationToLoad();

						reportingManager.updateTestLog(Status.INFO, getLoadTimeString(elementLabel, elementType), true);

						msgValidator.checkForDisplayedMessages(msgType, expectedMsg);
					}
				}

			} else {
				reportingManager.updateTestLog(Status.FATAL, getElementDisabledString(elementLabel), true);
			}
		} else {
			reportingManager.updateTestLog(Status.FATAL, getElementNotFoundString(elementLabel, elementType), true);
		}
	}

	private void checkIfFieldIsEnabled(String xpath, String value, String elementLabel, String elementType) {
		WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

		if (we.isDisplayed() && we.isEnabled()) {
			reportingManager.updateTestLog(Status.PASS, getElementEnabledString(elementLabel), true);
		} else if (we.isDisplayed() && !we.isEnabled()) {
			reportingManager.updateTestLog(Status.FAIL, getElementDisabledString(elementLabel), true);
		} else {
			reportingManager.updateTestLog(Status.FAIL, getElementNotFoundString(elementLabel, elementType), true);
		}
	}

	private void checkIfFieldIsDisabled(String xpath, String value, String elementLabel, String elementType) {
		WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

		if (we.isDisplayed() && we.isEnabled()) {
			reportingManager.updateTestLog(Status.FAIL, getElementEnabledString(elementLabel), true);
		} else if (we.isDisplayed() && !we.isEnabled()) {
			reportingManager.updateTestLog(Status.PASS, getElementDisabledString(elementLabel), true);
		} else {
			reportingManager.updateTestLog(Status.FAIL, getElementNotFoundString(elementLabel, elementType), true);
		}
	}

	public String getElementDisabledString(String elementLabel) {
		return elementLabel + " is displayed but is not enabled!";
	}

	private String getElementEnabledString(String elementLabel) {
		return elementLabel + " is enabled!";
	}

	private String getInvalidActionTypeString(String actionType, String elementType) {
		return actionType + " is an invalid action for " + elementType + "!";
	}

	private String getAssertFailureString(String value, String returnedValue, String elementType) {
		return "Value of " + elementType + " was: '" + TextHTMLWrapper.wrapElementLabel(value)
				+ "' and it did not match the returned value: " + TextHTMLWrapper.wrapValue(returnedValue) + "'!";
	}

	private String getAssertSuccessString(String value, String returnedValue, String elementType) {
		return "Value of " + elementType + " was: '" + TextHTMLWrapper.wrapElementLabel(value)
				+ "' and it matched the returned value: '" + TextHTMLWrapper.wrapValue(returnedValue) + "'.";
	}

	public String getElementNotFoundString(String elementLabel, String elementType) {
		return elementType + " '" + TextHTMLWrapper.wrapElementLabel(elementLabel)
				+ "' could not be located on the screen!";
	}

	private String getLoadTimeString(String elementLabel, String elementType) {
		return "Load time after clicking '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' " + elementType
				+ " was " + TextHTMLWrapper.wrapValue(stopTimerAndReturnElapsedTime()) + ".";
	}

	public String getClickedString(String elementLabel, String elementType) {
		return "User clicked on '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' " + elementType + ".";
	}

	private String getUsingElementNameForGlobalVarString() {
		return USE_ELEM_NAME_NO_VAR_NAME_STRING;
	}

}
