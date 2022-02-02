package frameworkextensions.screenelementhandlers;

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
 * @author Vedhanth Reddy (v.reddy.monajigari), Surbhi Sinha (surbhi.b.sinha),
 *         Rahul Patidar (rahul.b.patidar)
 */
public class CheckBoxAndRadioWithoutLabelHandler extends TestThread implements IWebElementHandler {

	public CheckBoxAndRadioWithoutLabelHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
	JSUtilities jsUtilities = new JSUtilities(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	/**
	 * This method is used to handle button or links available on the screen using
	 * javascript.
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

		switch (actionType) {
		case "Select Checkboxes":
			we = elementManager.waitAndFindElement(xpath);
			if (elementManager.isElementPresent(we)) {
				jsUtilities.clickElementByJS(we, driver);
				applicationSync.waitForApplicationToLoad();
				reportingManager
						.updateTestLog(Status.PASS,
								"User selected the following checkboxes: " + TextHTMLWrapper.wrapValue(value)
										+ " for the checkbox group: " + TextHTMLWrapper.wrapElementLabel(elementLabel),
								false);
				break;
			} else {
				reportingManager
						.updateTestLog(Status.FAIL,
								"The checkbox group: " + TextHTMLWrapper.wrapElementLabel(elementLabel)
										+ " with the xpath: " + TextHTMLWrapper.wrapValue(xpath) + " was not found!",
								true);
				break;
			}

		case "Assert Selected Checkboxes":
			// assert()
			break;

		case "Select Radio Button":
			we = elementManager.waitAndFindElement(xpath);
			if (elementManager.isElementPresent(we)) {
				jsUtilities.clickElementByJS(we, driver);
				applicationSync.waitForApplicationToLoad();
				reportingManager.updateTestLog(Status.PASS,
						"User selected the following radio button: " + TextHTMLWrapper.wrapValue(value)
								+ " for the radio button group: " + TextHTMLWrapper.wrapElementLabel(elementLabel),
						false);
				break;
			} else {
				reportingManager
						.updateTestLog(Status.FAIL,
								"The radio button group: " + TextHTMLWrapper.wrapElementLabel(elementLabel)
										+ " with the xpath: " + TextHTMLWrapper.wrapValue(xpath) + " was not found!",
								true);
				break;
			}

		case "Assert Selected Radio Button":
			// assert()
			break;
		case "Check if Field is Enabled":
			// checkIfEnabled()
			break;
		case "Check if Field is Disabled":
			// checkIfDisabled()
			break;

		default:
			reportingManager.updateTestLog(Status.WARNING, "Invalid action: " + TextHTMLWrapper.wrapValue(actionType)
					+ " for: " + TextHTMLWrapper.wrapElementLabel(elementLabel) + "!", false);
			break;
		}
	}
}
