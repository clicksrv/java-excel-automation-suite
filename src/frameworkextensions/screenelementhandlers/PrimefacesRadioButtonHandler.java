/**
 * 
 */
package frameworkextensions.screenelementhandlers;

import java.util.List;

import org.apache.commons.lang.StringUtils;
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
public class PrimefacesRadioButtonHandler extends TestThread implements IWebElementHandler {

	public PrimefacesRadioButtonHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	private static final String CHECKBOX_LABEL_XPATH_SUFFIX = "/tbody/tr/td/label";
	private static final String LABEL_PREFIX = "//label[contains(@for,";
	private static final String COLON_TOKEN = ":";
	private static final String LABEL_SUFFIX = "')]";

	JSUtilities jsUtilities = new JSUtilities(threadEntities);

	/**
	 * This method is used to handle prime faces radio button available on the
	 * screen.
	 * 
	 * @param actionType - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	public void handleElement(ScriptActionBlock actionBlock) {

		String actionType = actionBlock.getAction();
		String xpath = actionBlock.getElementXpath();
		String value = actionBlock.getValue();
		String elementLabel = actionBlock.getElementName();

		switch (actionType) {
		case "Select Radio Button":
			setPrimeFacesRadioButton(value, xpath, elementLabel);
			break;

		case "Assert Selected Radio Button":
			assertPrimeFacesRadioButton(value, xpath, elementLabel);

			break;

		case "Check if Enabled":
			// checkIfEnabled(value, xpath, elementLabel);
			break;
		case "Check if Field is Disabled":
			// checkIfDisabled(value, xpath, elementLabel);
			break;

		default:
			reportingManager.updateTestLog(Status.WARNING, "Action type for " + elementLabel + "is invalid.", false);

			break;

		}

	}

	/**
	 * This method is used to verify field is enabled or not which is available on
	 * the screen.
	 * 
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void checkIfEnabled(String value, String xpath, String elementLabel) {
		// TODO Auto-generated method stub
		WebElement we = elementManager.waitAndFindElement(xpath);
		if (we.isEnabled()) {
			// pass
		} else {
			// fail
		}

	}

	/**
	 * This method is used to set prime faces radio button which is available on the
	 * screen.
	 * 
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void setPrimeFacesRadioButton(String value, String xpath, String elementLabel) {
		String checkLabelLocator = null;

		if (StringUtils.isNotEmpty(value)) {

			checkLabelLocator = xpath + CHECKBOX_LABEL_XPATH_SUFFIX;

			List<WebElement> we = driver.findElements(By.xpath(checkLabelLocator));

			String identifier = xpath.substring(xpath.indexOf(",") + 1, xpath.indexOf(")") - 1);

			for (int i = 0; i < we.size(); i++) {
				String labelText = elementManager
						.waitAndFindElement(LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX).getText();

				if (labelText.equals(value) && !we.get(i).isSelected()) {
					applicationSync.waitForApplicationToLoad();
					jsUtilities.clickElementByJS(we.get(i), driver);

					applicationSync.waitForApplicationToLoad();

					reportingManager.updateTestLog(Status.PASS, TextHTMLWrapper.wrapElementLabel(elementLabel)
							+ ": Element is Selected with " + TextHTMLWrapper.wrapValue(value), false);

				}

			}

		}

	}

	/**
	 * This method is used to assert prime faces radio button available on the
	 * screen.
	 * 
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void assertPrimeFacesRadioButton(String value, String xpath, String elementLabel) {

		String checkLabelLocator = null;

		if (StringUtils.isNotEmpty(value)) {

			checkLabelLocator = xpath + CHECKBOX_LABEL_XPATH_SUFFIX;

			List<WebElement> we = driver.findElements(By.xpath(checkLabelLocator));

			String identifier = xpath.substring(xpath.indexOf(",") + 1, xpath.indexOf(")") - 1);

			for (int i = 0; i < we.size(); i++) {
				String labelText = driver
						.findElement(By.xpath(LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX)).getText();

				if (labelText.equals(value) && we.get(i).isSelected()) {

					reportingManager.updateTestLog(Status.PASS, TextHTMLWrapper.wrapElementLabel(elementLabel)
							+ ": Element is Selected with " + TextHTMLWrapper.wrapValue(value), false);

				} else if (labelText.equals(value) && !we.get(i).isSelected()) {
					reportingManager.updateTestLog(Status.FAIL,
							TextHTMLWrapper.wrapElementLabel(elementLabel) + ": Element is Selected with "
									+ TextHTMLWrapper.wrapValue(labelText) + " Insted of :"
									+ TextHTMLWrapper.wrapValue(value),
							true);
				}

			}

		}

	}

}
