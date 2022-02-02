/**
 * 
 */
package frameworkextensions.screenelementhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

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
 * @author Surbhi Sinha (surbhi.b.sinha)
 */
public class CheckboxHandler extends TestThread implements IWebElementHandler {

    private static final String LABEL_PREFIX = "//label[contains(@for,";
    private static final String COLON_TOKEN = ":";
    private static final String LABEL_SUFFIX = "')]";

    public CheckboxHandler(ThreadEntities threadEntities) {
        super(threadEntities);
    }

    ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
    WebElementManager elementManager = new WebElementManager(threadEntities);
    ApplicationSync applicationSync = new ApplicationSync(threadEntities);

    /**
     * This method is use to handle regular checkboxes available on the screen.
     * 
     * @param actionType - This parameter is of String data type
     * @param value - This parameter is of String data type
     * @param xpath - This parameter is of String data type
     * @param elementLabel - This parameter is of String data type
     */
    @Override
    public void handleElement(ScriptActionBlock actionBlock) {

        String actionType = actionBlock.getElementType();
        String value = actionBlock.getValue();
        String xpath = actionBlock.getElementXpath();
        String elementLabel = actionBlock.getElementName();

        WebElement we = null;
        switch (actionType) {
        case "Select Checkboxes":
            if (StringUtils.isBlank(value)) {
                we = elementManager.waitAndFindElement(xpath, elementLabel);
                if (null == we) {
                    break;
                }
                we.click();
            } else {
                setRegularCheckbox(xpath, value, elementLabel);
            }
            break;
        case "Assert Selected Checkboxes":
            assertRegularCheckbox(value, xpath, elementLabel);

            break;

        case "Check if Field is Enabled":

            // Check if Field is Enabled assertRegularCheckbox(value, xpath, elementLabel);

            break;

        case "Check if Field is Disabled":
            // Check if Field is Disabled assertRegularCheckbox(value, xpath, elementLabel);

            break;

        default:
            reportingManager.updateTestLog(Status.WARNING, "Action type for " + elementLabel + "is invalid.", false);

            break;
        }

    }

    /**
     * This method is used to set regular checkboxes which is available on the
     * screen.
     * 
     * @param xpath - This parameter is of String data type
     * @param selectedCheckValues - This parameter is of String data
     *         type
     * @param elementLabel - This parameter is of String data type
     */
    private void setRegularCheckbox(String xpath, String selectedCheckValues, String elementLabel) {
        List<WebElement> we = driver.findElements(By.xpath(xpath));

        String identifier = xpath.substring(xpath.indexOf(",") + 1, xpath.indexOf(")") - 1);

        if (StringUtils.isNotEmpty(selectedCheckValues)) {

            String[] selectedValuesArray = selectedCheckValues.split(",");

            if (we.size() == 1) {
                String checkOneElementIdentifier = LABEL_PREFIX + identifier + LABEL_SUFFIX;

                String singleCheckboxLabelText = elementManager.waitAndFindElement(checkOneElementIdentifier).getText()
                        .trim();

                if (singleCheckboxLabelText.equals(selectedCheckValues.trim())) {
                    WebElement singleCheckboxWebElement = elementManager.waitAndFindElement(checkOneElementIdentifier);
                    if (elementManager.isElementPresent_quickCheck(singleCheckboxWebElement)) {

                        if (!we.get(0).isSelected()) {
                            singleCheckboxWebElement.click();
                            applicationSync.waitForApplicationToLoad();
                        }

                        reportingManager.updateTestLog(Status.PASS,
                                "User was able to select: '" + TextHTMLWrapper.wrapValue(selectedCheckValues)
                                        + "' from the checkbox group: '"
                                        + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.",
                                false);

                    } else {
                        reportingManager.updateTestLog(Status.FAIL,
                                "User could not select: '" + TextHTMLWrapper.wrapValue(selectedCheckValues)
                                        + "' from the checkbox group: '"
                                        + TextHTMLWrapper.wrapElementLabel(elementLabel)
                                        + "' as the element was not found on the screen with the xpath: '"
                                        + TextHTMLWrapper.wrapValue(xpath) + "'!",
                                true);
                    }

                }

            } else if (we.size() > 1) {

                List<String> selectionValueList = new ArrayList<String>();
                List<String> uncommonValueList = new ArrayList<String>();
                List<String> allValueList = new ArrayList<String>();

                for (String selectedValue : selectedValuesArray) {
                    selectionValueList.add(selectedValue.trim());
                }

                for (WebElement webElement : we) {
                    allValueList.add(webElement.getText().trim());
                }

                uncommonValueList = (List<String>) CollectionUtils.removeAll(allValueList, selectionValueList);

                for (int i = 0; i < we.size(); i++) {

                    String xpathIdentifier = LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX;

                    String labelText = driver.findElement(By.xpath(xpathIdentifier)).getText();

                    for (int j = 0; j < selectionValueList.size(); j++) {
                        if (labelText.equals(selectionValueList.get(j))) {

                            WebElement checkboxWebElement = elementManager.waitAndFindElement(xpathIdentifier);
                            if (elementManager.isElementPresent_quickCheck(checkboxWebElement)) {

                                if (!we.get(i).isSelected()) {
                                    JavascriptExecutor ex = (JavascriptExecutor) driver;
                                    ex.executeScript("arguments[0].click();", we.get(i));
                                    applicationSync.waitForApplicationToLoad();
                                }

                                reportingManager.updateTestLog(Status.PASS,
                                        "User was able to select: '" + TextHTMLWrapper.wrapValue(selectedCheckValues)
                                                + "' from the checkbox group: '"
                                                + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.",
                                        false);
                            } else {
                                reportingManager.updateTestLog(Status.FAIL,
                                        "User could not select: '" + TextHTMLWrapper.wrapValue(selectedCheckValues)
                                                + "' from the checkbox group: '"
                                                + TextHTMLWrapper.wrapElementLabel(elementLabel)
                                                + "' as the element was not found on the screen with the xpath: '"
                                                + TextHTMLWrapper.wrapValue(xpath) + "'!",
                                        true);
                            }
                        }
                    }

                }

                for (int i = 0; i < we.size(); i++) {

                    String xpathIdentifierUnchekValue = LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX;

                    String labelText = elementManager.waitAndFindElement(xpathIdentifierUnchekValue).getText();

                    for (int j = 0; j < uncommonValueList.size(); j++) {
                        if (labelText.equals(uncommonValueList.get(j))) {

                            WebElement checkboxWebElement = elementManager
                                    .waitAndFindElement(xpathIdentifierUnchekValue);
                            if (elementManager.isElementPresent_quickCheck(checkboxWebElement)) {

                                if (we.get(i).isSelected()) {

                                    JavascriptExecutor ex = (JavascriptExecutor) driver;
                                    ex.executeScript("arguments[0].click();", we.get(i));
                                    applicationSync.waitForApplicationToLoad();

                                }

                                reportingManager.updateTestLog(Status.PASS,
                                        "User was able to deselect all values from the checkbox group: '"
                                                + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.",
                                        false);
                            } else {
                                reportingManager.updateTestLog(Status.FAIL,
                                        "User was unable to deselect values from the checkbox group: '"
                                                + TextHTMLWrapper.wrapElementLabel(elementLabel)
                                                + "' as the element was not found on the screen with the xpath: '"
                                                + TextHTMLWrapper.wrapValue(xpath) + "'!",
                                        true);
                            }
                        }
                    }

                }

            }

        } else {

            for (int i = 0; i < we.size(); i++) {
                if (elementManager.isElementPresent_quickCheck(we.get(i)) && we.get(i).isSelected()) {

                    applicationSync.waitForApplicationToLoad();
                    JavascriptExecutor ex = (JavascriptExecutor) driver;
                    ex.executeScript("arguments[0].click();", we.get(i));
                    applicationSync.waitForApplicationToLoad();
                    reportingManager.updateTestLog(Status.PASS,
                            "User was able to deselect all values from the checkbox group: '"
                                    + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.",
                            false);
                }
            }

        }
    }

    /**
     * This method is use to assert regular checkboxes available on the screen.
     * 
     * @param value - This parameter is of String data type
     * @param xpath - This parameter is of String data type
     * @param elementLabel - This parameter is of String data type
     */

    private void assertRegularCheckbox(String value, String xpath, String elementLabel) {
        List<WebElement> we = elementManager.waitAndFindElements(xpath);
        boolean isChecked = true;
        String[] selectedValuesArray = value.split(",");
        String identifier = xpath.substring(xpath.indexOf(",") + 1, xpath.indexOf(")") - 1);
        if (we.size() == 1) {

            isChecked = elementManager.waitAndFindElement(xpath).isSelected();

        } else if (we.size() > 1) {
            for (int i = 0; i < we.size(); i++) {
                String xpathIdentifier = LABEL_PREFIX + identifier + COLON_TOKEN + i + LABEL_SUFFIX;

                String labelText = elementManager.waitAndFindElement(xpathIdentifier).getText();

                for (int j = 0; j < selectedValuesArray.length; j++) {
                    if (labelText.equals(value.trim())) {
                        isChecked = we.get(i).isSelected();
                        if (!isChecked) {
                            break;
                        }

                    }
                }

            }
        }

        if (isChecked) {

            reportingManager.updateTestLog(Status.PASS,
                    "User was able to verify the selected values: " + TextHTMLWrapper.wrapValue(value)
                            + " from the checkbox group: '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.",
                    true);
        } else {
            List<String> selectedList = Arrays.asList(selectedValuesArray);

            reportingManager.updateTestLog(Status.FAIL,
                    "User failed to verify the selected values: " + TextHTMLWrapper.wrapValue(value)
                            + " from the checkbox group: '" + TextHTMLWrapper.wrapElementLabel(elementLabel)
                            + "' as the user found the selected values to be: '"
                            + TextHTMLWrapper.wrapValue(selectedList.toString()) + "'!",
                    true);
        }
    }

}
