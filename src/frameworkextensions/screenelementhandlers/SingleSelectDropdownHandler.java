/**
 * 
 */
package frameworkextensions.screenelementhandlers;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.aventstack.extentreports.Status;

import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Surbhi Sinha (surbhi.b.sinha), Saurav Kumar Sahoo
 *         (saurav.kumar.sahoo)
 */
public class SingleSelectDropdownHandler extends TestThread implements IWebElementHandler {

	public SingleSelectDropdownHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	/**
	 * This method is used to select value from dropdown list available on the
	 * screen.
	 * 
	 * @param actionType - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	public void handleElement(ScriptActionBlock actionBlock) {
		String actionType = actionBlock.getAction();
		String value = actionBlock.getValue();
		String xpath = actionBlock.getElementXpath();
		String elementLabel = actionBlock.getElementName();

		WebElement we = null;

		switch (actionType) {
		case "Select Dropdown By Value":
			we = elementManager.waitAndFindElement(xpath);
			if (null == we) {
				reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
				break;
			}
			setSingleDropdown(value.trim(), we, elementLabel);
			break;
		case "Select Dropdown By Index":
			we = elementManager.waitAndFindElement(xpath);
			if (null == we) {
				reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
				break;
			}
			setSingleDropdownByIndex(value, we, elementLabel, xpath);
			break;
		case "Select Random Value":
			we = elementManager.waitAndFindElement(xpath);
			if (null == we) {
				reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
				break;
			}
			setRandomValue(we, elementLabel, xpath);
			break;
		case "Assert Selected Dropdown Value":
			we = elementManager.waitAndFindElement(xpath);
			if (null == we) {
				reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
				break;
			}
			assertSingleDropdown(value, we, elementLabel);

			break;

		case "Click":
			we = elementManager.waitAndFindElement(xpath);

			if (null == we) {
				reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
				break;
			}

			we.click();

			break;
		case "Check if Field is Enabled":
			checkIfFieldIsEnabled(xpath, value, elementLabel);

			break;

		case "Check if Field is Disabled":
			checkIfFieldIsDisabled(xpath, value, elementLabel);

			break;

		default:
			reportingManager.updateTestLog(Status.WARNING, "Action type for " + elementLabel + "is invalid.", false);
			break;

		}

	}

	private void setSingleDropdownByIndex(String value, WebElement we, String elementLabel, String xpath) {

		int i = Integer.parseInt(value);
		String selectedDropdownValue = null;
		applicationSync.waitForApplicationToLoad();
		Select selectDropdown = new Select(we);
		if (selectDropdown.getOptions().size() > i && i > -1) {
			selectDropdown.selectByIndex(i);
			applicationSync.waitForApplicationToLoad();

			boolean elementStale = false;
			do {
				if (elementStale) {
					selectDropdown = new Select(elementManager.waitAndFindElement(xpath));
				}

				WebElement selectedWe = null;
				try {
					selectedWe = selectDropdown.getFirstSelectedOption();
					elementStale = false;
				} catch (StaleElementReferenceException e) {
					elementStale = true;
				}

				if (!elementStale) {
					try {
						selectedDropdownValue = elementManager.getTextFromWebElement(selectedWe);
						elementStale = false;
					} catch (StaleElementReferenceException e) {
						elementStale = true;
					}
				}

			} while (elementStale);

			reportingManager.updateTestLog(Status.PASS,
					getIndexSelectionPassString(value, elementLabel, selectedDropdownValue), false);
		} else if (i < 0) {
			reportingManager.updateTestLog(Status.ERROR, getInvalidIndexString(value), false);
		} else {
			String[] dropdowns = getdropdownlist(we);
			List<String> dropdownList = Arrays.asList(dropdowns);

			reportingManager.updateTestLog(Status.FAIL,
					getIndexSelectionFailString(value, elementLabel, selectDropdown, dropdownList), true);
		}
	}

	private void setRandomValue(WebElement we, String elementLabel, String xpath) {

		String selectedDropdownValue = null;
		applicationSync.waitForApplicationToLoad();
		Select selectDropdown = new Select(we);
		int i = rdc.randomNumberBetween(1, selectDropdown.getOptions().size() - 1);
		String value = String.valueOf(i);
		if (selectDropdown.getOptions().size() > i && i > -1) {
			selectDropdown.selectByIndex(i);
			applicationSync.waitForApplicationToLoad();

			boolean elementStale = false;
			do {
				if (elementStale) {
					selectDropdown = new Select(elementManager.waitAndFindElement(xpath));
				}

				WebElement selectedWe = null;
				try {
					selectedWe = selectDropdown.getFirstSelectedOption();
					elementStale = false;
				} catch (StaleElementReferenceException e) {
					elementStale = true;
				}

				if (!elementStale) {
					try {
						selectedDropdownValue = elementManager.getTextFromWebElement(selectedWe);
						elementStale = false;
					} catch (StaleElementReferenceException e) {
						elementStale = true;
					}
				}

			} while (elementStale);

			reportingManager.updateTestLog(Status.PASS,
					getIndexSelectionPassString(value, elementLabel, selectedDropdownValue), false);
		} else if (i < 0) {
			reportingManager.updateTestLog(Status.ERROR, getInvalidIndexString(value), false);
		} else {
			String[] dropdowns = getdropdownlist(we);
			List<String> dropdownList = Arrays.asList(dropdowns);

			reportingManager.updateTestLog(Status.FAIL,
					getIndexSelectionFailString(value, elementLabel, selectDropdown, dropdownList), true);
		}
	}

	/**
	 * This method is used to set dropdown value which is available on the screen.
	 * 
	 * @param value - This parameter is of String data type
	 * @param we - This parameter is of WebElement data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void setSingleDropdown(String value, WebElement we, String elementLabel) {

		boolean isDropdownSelected = false;

		applicationSync.waitForApplicationToLoad();

		String[] dropdownStringList = getdropdownlist(we);
		for (int i = 0; i < dropdownStringList.length; i++) {

			if (dropdownStringList[i].trim().equalsIgnoreCase(value)) {
				applicationSync.waitForApplicationToLoad();
				Select selectDropdown = new Select(we);
				selectDropdown.selectByIndex(i);
				isDropdownSelected = true;
				applicationSync.waitForApplicationToLoad();
				break;
			}
		}

		applicationSync.waitForApplicationToLoad();

		if (isDropdownSelected) {
			reportingManager.updateTestLog(Status.PASS, getValueSelectionSuccessString(value, elementLabel), false);
		} else {
			String[] dropdowns = getdropdownlist(we);
			List<String> dropdownList = Arrays.asList(dropdowns);

			reportingManager.updateTestLog(Status.FAIL, getValueSelectionFailString(value, elementLabel, dropdownList),
					true);
		}

	}

	/**
	 * This method is used to assert value of dropdown list which is available on
	 * the screen.
	 * 
	 * @params Argument : value - This parameter is of String data type
	 * @params Argument : we - This parameter is of WebElement data type
	 * @params Argument : elementLabel - This parameter is of String data type
	 */
	private void assertSingleDropdown(String value, WebElement we, String elementLabel) {

		boolean isDropdownValueSelected = false;

		Select selectDropdown = new Select(we);

		List<WebElement> allSelectedOptions = selectDropdown.getAllSelectedOptions();

		String selectedDropdownText = null;

		for (WebElement webElement : allSelectedOptions) {
			selectedDropdownText = webElement.getText().trim();

			isDropdownValueSelected = selectedDropdownText.equals(value.trim());
		}

		if (isDropdownValueSelected) {
			reportingManager.updateTestLog(Status.PASS, getAssertSuccessString(value, elementLabel), false);
		} else {
			reportingManager.updateTestLog(Status.FAIL,
					getAssertFailureString(value, elementLabel, selectedDropdownText), true);
		}

	}

	private void checkIfFieldIsEnabled(String xpath, String value, String elementLabel) {
		WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

		if (null == we) {
			reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
			return;
		}

		if (we.isDisplayed() && we.isEnabled()) {
			reportingManager.updateTestLog(Status.PASS, getElementEnabledString(elementLabel), false);
		} else if (we.isDisplayed() && !we.isEnabled()) {
			reportingManager.updateTestLog(Status.FAIL, getElementDisabledString(elementLabel), true);
		} else {
			reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
		}
	}

	private void checkIfFieldIsDisabled(String xpath, String value, String elementLabel) {
		WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

		if (null == we) {
			reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
			return;
		}

		if (we.isDisplayed() && we.isEnabled()) {
			reportingManager.updateTestLog(Status.FAIL, getElementEnabledString(elementLabel), true);
		} else if (we.isDisplayed() && !we.isEnabled()) {
			reportingManager.updateTestLog(Status.PASS, getElementDisabledString(elementLabel), false);
		} else {
			reportingManager.updateTestLog(Status.FAIL, getElementMissingString(xpath, elementLabel), true);
		}
	}

	private String[] getdropdownlist(WebElement we) {
		applicationSync.waitForApplicationToLoad();

		Select select = new Select(we);
		List<WebElement> options = select.getOptions();
		Object[] optList = options.toArray();
		String[] optArr = new String[optList.length];

		for (int i = 0; i < optList.length; i++) {
			optArr[i] = ((WebElement) optList[i]).getText();
		}
		return optArr;
	}

	/**
	 * This method is used to get all the values from dropdown list which is
	 * available on the screen.
	 * 
	 * @params Argument : we - This parameter is of WebElement data type
	 * @return : This method will return array of String
	 */
	private String getValueSelectionFailString(String value, String elementLabel, List<String> dropdownList) {
		return "User could not select '" + TextHTMLWrapper.wrapValue(value) + "' from the dropdown '"
				+ TextHTMLWrapper.wrapElementLabel(elementLabel) + "'. Available values were: "
				+ TextHTMLWrapper.wrapValue(dropdownList.toString()) + ".";
	}

	private String getValueSelectionSuccessString(String value, String elementLabel) {
		return "User selected '" + TextHTMLWrapper.wrapValue(value) + "' from the dropdown '"
				+ TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.";
	};

	private String getElementDisabledString(String elementLabel) {
		return TextHTMLWrapper.wrapElementLabel(elementLabel) + " is displayed but is not enabled!";
	}

	private String getElementEnabledString(String elementLabel) {
		return TextHTMLWrapper.wrapElementLabel(elementLabel) + " is enabled!";
	}

	private String getIndexSelectionFailString(String value, String elementLabel, Select selectDropdown,
			List<String> dropdownList) {
		return "User could not select index '" + TextHTMLWrapper.wrapValue(value) + "' from the dropdown '"
				+ TextHTMLWrapper.wrapElementLabel(elementLabel) + "'. Available values were: (Index 0 through "
				+ (selectDropdown.getOptions().size() - 1) + ") " + TextHTMLWrapper.wrapValue(dropdownList.toString())
				+ ".";
	}

	private String getIndexSelectionPassString(String value, String elementLabel, String selectedDropdownValue) {
		return "User selected index '" + TextHTMLWrapper.wrapValue(value + ": " + selectedDropdownValue)
				+ "' from the dropdown '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.";
	}

	private String getInvalidIndexString(String value) {
		return value + " is an invalid index selection!";
	}

	private String getAssertFailureString(String value, String elementLabel, String selectedDropdownText) {
		return "Selected dropdown value in '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' is '"
				+ TextHTMLWrapper.wrapValue(selectedDropdownText) + "' and not '" + TextHTMLWrapper.wrapValue(value)
				+ "'.";
	}

	private String getAssertSuccessString(String value, String elementLabel) {
		return "Selected dropdown value in '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' is '"
				+ TextHTMLWrapper.wrapValue(value) + "'.";
	}

	private String getElementMissingString(String xpath, String elementLabel) {
		return "Web Element with Xpath " + TextHTMLWrapper.wrapValue(xpath) + " for "
				+ TextHTMLWrapper.wrapElementLabel(elementLabel) + " is not present!";
	}
}
