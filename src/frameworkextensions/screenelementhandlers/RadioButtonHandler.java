/**
 * 
 */
package frameworkextensions.screenelementhandlers;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.JSUtilities;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Surbhi Sinha (surbhi.b.sinha)
 */
public class RadioButtonHandler extends TestThread implements IWebElementHandler {

	public RadioButtonHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	private static final String LABEL_PREFIX = "//label[contains(@for,";
	private static final String COLON_TOKEN = ":";
	private static final String LABEL_SUFFIX = "')]";

	JSUtilities jsUtilities = new JSUtilities(threadEntities);

	/**
	 * This method is used to handle radio button available on the screen.
	 * 
	 * @param actionType - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	@Override
	public void handleElement(ScriptActionBlock actionBlock) {

		String actionType = actionBlock.getAction();
		String xpath = actionBlock.getElementXpath();
		String value = actionBlock.getValue();
		String elementLabel = actionBlock.getElementName();

		switch (actionType) {

		case "Select Radio Button":

			setRadioButton(xpath, value.trim(), elementLabel);
			break;

		case "Assert Selected Radio Button":

			assertRadioButton(xpath, value, elementLabel);
			break;

		case "Check if Field is Enabled":

			// checkIfEnabled(xpath, value, elementLabel);
			break;
		case "Check if Field is Disabled":

			// checkIfDisabled(xpath, value, elementLabel);
			break;

		default:
			reportingManager.updateTestLog(Status.WARNING, "Action type for " + elementLabel + "is invalid.", false);
			break;

		}

	}

	/**
	 * This method is used to verify field is disabled or not which is available on
	 * the screen.
	 * 
	 * @param xpath - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void checkIfDisabled(String xpath, String value, String elementLabel) {

		WebElement we = elementManager.waitAndFindElement(xpath);
		if (!we.isEnabled()) {
			// pass
		} else {
			// fail
		}
	}

	/**
	 * This method is used to set radio button which is available on the screen.
	 * 
	 * @param xpath - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void setRadioButton(String xpath, String value, String elementLabel) {
		boolean isSelected = false;
		List<WebElement> we = driver.findElements(By.xpath(xpath));

		String identifier = xpath.substring(xpath.indexOf(",") + 1, xpath.indexOf(")") - 1);

		for (int i = 0; i < we.size(); i++) {
			String labelText = elementManager
					.waitAndFindElement(LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX).getText().trim();

			WebElement radio = elementManager
					.waitAndFindElement(LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX);

			if (labelText.equals(value) && elementManager.isElementPresent(radio)) {
				applicationSync.waitForApplicationToLoad();
				jsUtilities.clickElementByJS(radio, driver);
				applicationSync.waitForApplicationToLoad();
				isSelected = true;
			}
		}

		if (isSelected) {
			reportingManager.updateTestLog(Status.PASS, TextHTMLWrapper.wrapElementLabel(elementLabel)
					+ ": Element is Selected with " + TextHTMLWrapper.wrapValue(value), false);
		} else {
			reportingManager.updateTestLog(Status.FAIL,
					"Unable to Select " + TextHTMLWrapper.wrapElementLabel(elementLabel) + ": Element with "
							+ TextHTMLWrapper.wrapValue(value),
					true);
		}

	}

	/**
	 * This method is used to assert radio button available on the screen.
	 * 
	 * @param xpath - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void assertRadioButton(String xpath, String value, String elementLabel) {

		List<WebElement> we = elementManager.waitAndFindElements(xpath);

		String identifier = xpath.substring(xpath.indexOf(",") + 1, xpath.indexOf(")") - 1);

		for (int i = 0; i < we.size(); i++) {
			String labelText = elementManager
					.waitAndFindElement(LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX).getText();

			if (labelText.equals(value) && we.get(i).isSelected()) {

				reportingManager.updateTestLog(Status.PASS, TextHTMLWrapper.wrapElementLabel(elementLabel)
						+ " Radio Button is selected with " + TextHTMLWrapper.wrapValue(value), false);
			} else if (labelText.equals(value) && !we.get(i).isSelected()) {

				reportingManager.updateTestLog(Status.FAIL, elementLabel + " Radio Button is selected with "
						+ TextHTMLWrapper.wrapValue(labelText) + "instead of" + TextHTMLWrapper.wrapElementLabel(value),
						true);
			}

		}

	}

}
