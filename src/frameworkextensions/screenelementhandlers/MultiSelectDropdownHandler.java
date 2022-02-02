package frameworkextensions.screenelementhandlers;

import java.util.Arrays;
import java.util.List;

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
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class MultiSelectDropdownHandler extends TestThread implements IWebElementHandler {

    public MultiSelectDropdownHandler(ThreadEntities threadEntities) {

        super(threadEntities);
    }

    ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
    WebElementManager elementManager = new WebElementManager(threadEntities);
    ApplicationSync applicationSync = new ApplicationSync(threadEntities);

    /**
     * This method is used to select mutiple values from dropdown available on the
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

        WebElement we = elementManager.waitAndFindElement(xpath);

        if (null == we) {
            reportingManager.updateTestLog(Status.FAIL, "Web Element with Xpath " + TextHTMLWrapper.wrapValue(xpath)
                    + " for " + TextHTMLWrapper.wrapElementLabel(elementLabel) + " is not present!", true);
            return;
        }

        switch (actionType) {
        case "Select Dropdowns By Value":
            setMultiDropdown(value, we, elementLabel);
            break;

        case "Assert Selected Dropdown Values":
            assertMultiDropdown(value, we, elementLabel);
            break;

        case "Check if Field is Enable":
            checkIfFieldIsEnabled(xpath, value, elementLabel);
            break;

        case "Check if Field is Disable":
            checkIfFieldIsDisabled(xpath, value, elementLabel);
            break;

        case "Select Dropdowns By Index":
            //
            break;

        default:
            reportingManager.updateTestLog(Status.WARNING, "Action type for " + elementLabel + "is invalid.", false);
            break;

        }
    }

    /**
     * This method is used to set dropdown values which is available on the screen.
     * 
     * @param value - This parameter is of String data type
     * @param xpath - This parameter is of String data type
     * @param elementLabel - This parameter is of String data type
     */
    private void setMultiDropdown(String value, WebElement we, String elementLabel) {
        boolean isDropdownSelected = false;

        String[] listOfValues = value.split(",");

        applicationSync.waitForApplicationToLoad();
        Select selectDropdown = new Select(we);

        try {
            for (String string : listOfValues) {
                string = string.trim();
                selectDropdown.selectByVisibleText(string.trim());
                isDropdownSelected = true;
            }
        } catch (Exception e) {
            isDropdownSelected = false;
        }

        applicationSync.waitForApplicationToLoad();

        if (isDropdownSelected) {
            reportingManager.updateTestLog(Status.PASS, "User selected '" + TextHTMLWrapper.wrapValue(value)
                    + "' from the dropdown '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'.", false);
        } else {
            String[] dropdowns = getdropdownlist(we);
            List<String> dropdownList = Arrays.asList(dropdowns);

            reportingManager.updateTestLog(Status.FAIL,
                    "User could not select '" + TextHTMLWrapper.wrapValue(value) + "' from the dropdown '"
                            + TextHTMLWrapper.wrapElementLabel(elementLabel) + "'. Available values were: "
                            + TextHTMLWrapper.wrapValue(dropdownList.toString()) + ".",
                    true);
        }

    };

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

    private void assertMultiDropdown(String value, WebElement we, String elementLabel) {
        // TODO
        reportingManager.updateTestLog(Status.WARNING,
                "assertMultipleDropdown method is not implemented for MultiDropdown Element Type", false);
    }

    private void checkIfFieldIsEnabled(String xpath, String value, String elementLabel) {
        WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

        if (null == we) {
            reportingManager.updateTestLog(Status.FAIL, "Web Element with Xpath " + TextHTMLWrapper.wrapValue(xpath)
                    + " for " + TextHTMLWrapper.wrapElementLabel(elementLabel) + " is not present!", true);
            return;
        }

        if (we.isDisplayed() && we.isEnabled()) {
            reportingManager.updateTestLog(Status.PASS, elementLabel + " is Enabled!", false);
        } else if (we.isDisplayed() && !we.isEnabled()) {
            reportingManager.updateTestLog(Status.FAIL, elementLabel + " is displayed but is not enabled!", true);
        } else {
            reportingManager.updateTestLog(Status.FAIL, elementLabel + " is not displayed!", true);
        }
    }

    private void checkIfFieldIsDisabled(String xpath, String value, String elementLabel) {
        WebElement we = elementManager.waitAndFindElement(xpath, elementLabel);

        if (null == we) {
            reportingManager.updateTestLog(Status.FAIL, "Web Element with Xpath " + TextHTMLWrapper.wrapValue(xpath)
                    + " for " + TextHTMLWrapper.wrapElementLabel(elementLabel) + " is not present!", true);
            return;
        }

        if (we.isDisplayed() && we.isEnabled()) {
            reportingManager.updateTestLog(Status.FAIL, elementLabel + " is Enabled!", true);
        } else if (we.isDisplayed() && !we.isEnabled()) {
            reportingManager.updateTestLog(Status.PASS, elementLabel + " is displayed but is not enabled!", false);
        } else {
            reportingManager.updateTestLog(Status.FAIL, elementLabel + " is not displayed!", true);
        }
    }

}
