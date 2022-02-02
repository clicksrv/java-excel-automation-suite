package frameworkextensions.screenelementhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testdatamanagement.TestDataManager;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Surbhi Sinha (surbhi.b.sinha)
 */
public class PrimefacesCheckboxHandler extends TestThread implements IWebElementHandler {

	public PrimefacesCheckboxHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	private static final String CHECKBOX_LABEL_XPATH_SUFFIX = "/tbody/tr/td/label";
	private static final String CHECKBOX_VALUE_XPATH_SUFFIX = "/tbody//div/div[2]/span";

	/**
	 * This method is used to handle prime faces checkboxes available on the screen.
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
		case "Select Checkboxes":
			setPrimeFacesCheckBox(value, xpath, elementLabel);
			break;

		case "SetGlobal":
			TestDataManager tdm = new TestDataManager(threadEntities);
			if (StringUtils.isNotBlank(value)) {
				tdm.setTestGlobal(elementLabel, value);
			} else {
				tdm.setTestGlobal(elementLabel, xpath);
			}
			break;
		case "Assert Selected Checkboxes":
			assertPrimeFacesCheckBox(value, xpath, elementLabel);
			break;

		case "Check if Field is Enabled":
			// Check if Field is Enabled
			break;
		case "Check if Field is Disabled":
			// Check if Field is Disabled
			break;

		default:
			updateFailureLog(elementLabel);
			break;
		}

	}

	/**
	 * This method is used to verify field is enabled or not which is available on
	 * the screen.
	 * 
	 * @param xpath - This parameter is of String data type
	 * @param value - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void checkIfEnabled(String xpath, String value, String elementLabel) {
		WebElement we = elementManager.waitAndFindElement(xpath);
		if (we.isEnabled()) {
			// pass
		} else {
			// fail
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
	 * This method is used to update the failure logs
	 * 
	 * @params Argument : elementLabel - This parameter is of String data type
	 */
	private void updateFailureLog(String elementLabel) {
		reportingManager.updateTestLog(Status.WARNING,
				"Action type for " + elementLabel + "is not valid for PrimeFaces CheckBox Element", false);
	}

	/**
	 * This method is used to set prime faces checkboxes which is available on the
	 * screen.
	 * 
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void setPrimeFacesCheckBox(String value, String xpath, String elementLabel) {
		String checkLabelLocator = null;
		String checkValueLocator = null;
		if (StringUtils.isNotEmpty(value)) {

			String[] selectionValuesArray = value.split(",");

			List<String> selectionValueList = new ArrayList<String>();
			List<String> uncommonValueList = new ArrayList<String>();
			List<String> allValueList = new ArrayList<String>();

			for (String selectedValue : selectionValuesArray) {
				selectionValueList.add(selectedValue.trim());
			}

			checkLabelLocator = xpath + CHECKBOX_LABEL_XPATH_SUFFIX;
			checkValueLocator = xpath + CHECKBOX_VALUE_XPATH_SUFFIX;

			List<WebElement> checkLabelLocatorList = driver.findElements(By.xpath(checkLabelLocator));

			for (WebElement webElement : checkLabelLocatorList) {
				allValueList.add(webElement.getText().trim());
			}

			uncommonValueList = (List<String>) CollectionUtils.removeAll(allValueList, selectionValueList);

			for (int i = 0; i < checkLabelLocatorList.size(); i++) {

				for (int j = 0; j < selectionValueList.size(); j++) {

					List<WebElement> checkValueLocatorList_new = driver.findElements(By.xpath(checkValueLocator));

					List<WebElement> checkLabelLocatorList_new = elementManager.waitAndFindElements(checkLabelLocator);

					if (checkLabelLocatorList_new.get(i).getText().trim().equals(selectionValueList.get(j))
							&& !isValueChecked(checkValueLocatorList_new.get(i))) {
						applicationSync.waitForApplicationToLoad();
						JavascriptExecutor ex = (JavascriptExecutor) driver;
						ex.executeScript("arguments[0].click();", checkValueLocatorList_new.get(i));

						applicationSync.waitForApplicationToLoad();

						reportingManager.updateTestLog(Status.PASS, selectionValueList.get(j)
								+ " are selected for the field " + TextHTMLWrapper.wrapElementLabel(elementLabel),
								false);

						break;
					}

				}

			}

			for (int i = 0; i < checkLabelLocatorList.size(); i++) {

				for (int j = 0; j < uncommonValueList.size(); j++) {

					List<WebElement> checkValueLocatorList_new2 = driver.findElements(By.xpath(checkValueLocator));

					List<WebElement> checkLabelLocatorList_new2 = driver.findElements(By.xpath(checkLabelLocator));

					if (checkLabelLocatorList_new2.get(i).getText().trim().equals(uncommonValueList.get(j))
							&& isValueChecked(checkValueLocatorList_new2.get(i))) {

						JavascriptExecutor ex = (JavascriptExecutor) driver;
						ex.executeScript("arguments[0].click();", checkValueLocatorList_new2.get(i));
						applicationSync.waitForApplicationToLoad();

						reportingManager.updateTestLog(Status.PASS, uncommonValueList.get(j)
								+ "are Deselected for the feild " + TextHTMLWrapper.wrapElementLabel(elementLabel),
								false);

						break;
					}

				}

			}

		}

	}

	/**
	 * This method is use to assert prime faces checkboxes available on the screen.
	 * 
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 * @param elementLabel - This parameter is of String data type
	 */
	private void assertPrimeFacesCheckBox(String value, String xpath, String elementLabel) {

		String checkLabelLocator = null;
		String checkValueLocator = null;
		if (StringUtils.isNotEmpty(value)) {

			String[] selectionValuesArray = value.split(",");
			List<String> selectionValueList = new ArrayList<String>();
			List<String> uncommonValueList = new ArrayList<String>();
			List<String> allValueList = new ArrayList<String>();

			for (String selectedValue : selectionValuesArray) {
				selectionValueList.add(selectedValue.trim());
			}

			Map<String, Boolean> actualAssertionMap = new HashMap<String, Boolean>();
			Map<String, Boolean> expectedAssertionMap = new HashMap<String, Boolean>();

			checkLabelLocator = xpath + CHECKBOX_LABEL_XPATH_SUFFIX;
			checkValueLocator = xpath + CHECKBOX_VALUE_XPATH_SUFFIX;

			List<WebElement> checkLabelLocatorList = driver.findElements(By.xpath(checkLabelLocator));
			/* actual values mapping present on screen */
			for (int i = 0; i < checkLabelLocatorList.size(); i++) {
				List<WebElement> checkValueLocatorList = driver.findElements(By.xpath(checkValueLocator));
				allValueList.add(checkLabelLocatorList.get(i).getText().trim());
				actualAssertionMap.put(checkLabelLocatorList.get(i).getText().trim(),
						isValueChecked(checkValueLocatorList.get(i)));
			}

			/*
			 * prepare uncommon value list : all the values which are present on screen but
			 * not in the datasheet
			 */
			uncommonValueList = (List<String>) CollectionUtils.removeAll(allValueList, selectionValueList);
			/*
			 * expected values mapping present on screen for values provided in datasheet
			 */

			for (String seletionValueKey : selectionValueList) {
				expectedAssertionMap.put(seletionValueKey, true);

			}

			/*
			 * expected values mapping present on screen for values not provided in
			 * datasheet
			 */

			for (String deseletionValueKey : uncommonValueList) {
				expectedAssertionMap.put(deseletionValueKey, false);

			}

			for (Map.Entry<String, Boolean> actualValue : actualAssertionMap.entrySet()) {
				if (expectedAssertionMap.containsKey(actualValue.getKey())) {

					String actualValueString = BooleanUtils.toStringTrueFalse(actualValue.getValue());

					String expectedValueString = BooleanUtils
							.toStringTrueFalse(expectedAssertionMap.get(actualValue.getKey()));

					if (StringUtils.equals(actualValueString, expectedValueString)) {
						reportingManager.updateTestLog(Status.PASS, "User is able to verify " + actualValue.getKey()
								+ " for the feild " + TextHTMLWrapper.wrapElementLabel(elementLabel), false);
						continue;

					} else {

						reportingManager.updateTestLog(Status.FAIL,
								" User failed to verify the selected values: "
										+ TextHTMLWrapper.wrapValue(actualValue.getKey())
										+ " from the checkbox group: '" + TextHTMLWrapper.wrapElementLabel(elementLabel)
										+ "' as the user found the selected values to be: '"
										+ TextHTMLWrapper.wrapValue(actualValueString.toString()) + "'!",
								true);

						break;
					}

				}
			}

		}

	}

	/**
	 * This method is used to verify that checkboxes is already selected or not
	 * available on the screen.
	 * 
	 * @params Argument : element - This parameter is of String data type
	 */
	public boolean isValueChecked(WebElement element) {
		boolean isChecked = false;

		if (element.getAttribute("class").contains("ui-icon-check")) {
			isChecked = true;
		}

		return isChecked;
	}

}
