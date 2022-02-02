package frameworkcore.executors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkConstants;
import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.excelreaders.ScriptReader;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.ButtonAndLinksHandler;
import frameworkextensions.screenelementhandlers.CheckBoxAndRadioWithoutLabelHandler;
import frameworkextensions.screenelementhandlers.CheckboxHandler;
import frameworkextensions.screenelementhandlers.DataTableHandler;
import frameworkextensions.screenelementhandlers.MultiSelectDropdownHandler;
import frameworkextensions.screenelementhandlers.PrimefacesCheckboxHandler;
import frameworkextensions.screenelementhandlers.PrimefacesRadioButtonHandler;
import frameworkextensions.screenelementhandlers.RadioButtonHandler;
import frameworkextensions.screenelementhandlers.SingleSelectDropdownHandler;
import frameworkextensions.screenelementhandlers.TextBoxHandler;
import frameworkextensions.screenelementhandlers.TreeTableHandler;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;

/**
 * @author Ashish Vishwakarma (ashish.b.vishwakarma), Saurav Kumar Sahoo
 *         (saurav.kumar.sahoo), Rahul Patidar (rahul.b.patidar)
 */
public class PageUtility extends TestThread {

	public PageUtility(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	String workbookPath = null;
	String workbookName = null;
	ScriptReader reader = null;
	ApplicationMessageValidator messageValidator = new ApplicationMessageValidator(threadEntities);

	/**
	 * This method is used to execute custom code
	 * 
	 * @param value - This parameter is of String data type
	 * @param xpath - This parameter is of String data type
	 */
	public void executeCustomCode(String value, String xpath) {

		String tempArray[] = value.split("#");

		String className = tempArray[0];

		String methodName = tempArray[1].substring(0, tempArray[1].indexOf("("));

		String arguments = tempArray[1].substring(tempArray[1].indexOf("(") + 1, tempArray[1].lastIndexOf(")"));

		try {
			Class<?> customCodeClass = Class.forName("frameworkextensions.screenelementhandlers.custom." + className);
			Method executeComponent;

			executeComponent = customCodeClass.getMethod(methodName, ScriptActionBlock.class);

			Constructor<?> ctor = customCodeClass.getDeclaredConstructors()[0];
			Object businessComponent;

			businessComponent = ctor.newInstance(threadEntities);

			executeComponent.invoke(businessComponent, xpath, arguments);
		} catch (ClassNotFoundException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e, "Class " + FrameworkConstants.HTMLFormat.VALUE + className
					+ FrameworkConstants.HTMLFormat.CLOSE + " was not found!", testCaseVariables);
		} catch (InstantiationException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e, "Class " + className + " could not be instantiated!",
					testCaseVariables);
		} catch (IllegalAccessException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e, "Illegal Access, method " + methodName + " is not public!",
					testCaseVariables);
		} catch (NoSuchMethodException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e, "Method " + methodName + " was not found!", testCaseVariables);
		} catch (SecurityException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e, "Security Exception caught for " + methodName + "!",
					testCaseVariables);
		} catch (IllegalArgumentException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e, "Arguments " + arguments + " are not valid!", testCaseVariables);
		} catch (InvocationTargetException e) {
			ErrorHandler.testError(ErrLvl.ERROR, e,
					"Unable to invoke target class  " + className + " and method " + methodName + "!",
					testCaseVariables);
		}
	}

	/**
	 * This method is used to handle all the page elements
	 * 
	 * @param actionBlock - populate new ScriptActionBlock with required
	 *                 String arguments or call with script step number and offset
	 *                 to auto-read from Script Sheet.
	 */
	public void handlePageElement(ScriptActionBlock actionBlock) {

		ApplicationSync applicationSync = new ApplicationSync(threadEntities);
		applicationSync.waitForApplicationToLoad();

		switch (actionBlock.getElementType()) {
		case "Text":
		case "TextBox":
		case "Textbox":
		case "Span":
		case "A":
		case "a":
			TextBoxHandler textBoxHandler = new TextBoxHandler(threadEntities);
			textBoxHandler.handleElement(actionBlock);
			break;

		case "Button":
		case "Link":
			ButtonAndLinksHandler buttonAndLinksHandler = new ButtonAndLinksHandler(threadEntities);
			buttonAndLinksHandler.handleElement(actionBlock);
			break;

		case "CheckboxWithoutLabel":
		case "RadioWithoutLabel":
			CheckBoxAndRadioWithoutLabelHandler customElementHandler = new CheckBoxAndRadioWithoutLabelHandler(
					threadEntities);
			customElementHandler.handleElement(actionBlock);
			break;

		case "PrimeFacesDatatable":
			DataTableHandler dataTableHandler = new DataTableHandler(threadEntities);
			dataTableHandler.handleElement(actionBlock);
			break;

		case "PrimeFacesTreeTable":
			TreeTableHandler treeTableHandler = new TreeTableHandler(threadEntities);
			treeTableHandler.handleElement(actionBlock);
			break;

		case "PrimeFacesCheckBox":
			PrimefacesCheckboxHandler primeFacesCheckboxHandler = new PrimefacesCheckboxHandler(threadEntities);
			primeFacesCheckboxHandler.handleElement(actionBlock);
			break;

		case "PrimeFacesRadio":
			PrimefacesRadioButtonHandler primeFacesRadioButtonHandler = new PrimefacesRadioButtonHandler(
					threadEntities);
			primeFacesRadioButtonHandler.handleElement(actionBlock);
			break;
		case "Radio":
		case "RadioButton":
		case "Radiobutton":
			RadioButtonHandler radioButtonHandler = new RadioButtonHandler(threadEntities);
			radioButtonHandler.handleElement(actionBlock);
			break;
		case "Checkbox":
		case "CheckBox":
			CheckboxHandler checkboxHandler = new CheckboxHandler(threadEntities);
			checkboxHandler.handleElement(actionBlock);
			break;
		case "SingleSelectDropDown":
			SingleSelectDropdownHandler selectSingleDropdownHandler = new SingleSelectDropdownHandler(threadEntities);
			selectSingleDropdownHandler.handleElement(actionBlock);
			break;
		case "MultiSelectDropDown":
			MultiSelectDropdownHandler selectMultipleDropdownHandler = new MultiSelectDropdownHandler(threadEntities);
			selectMultipleDropdownHandler.handleElement(actionBlock);
			break;
		default:
			reportingManager.updateTestLog(Status.WARNING,
					"Element Type " + actionBlock.getElementType() + " is an invalid value!", false);
			break;
		}
	}
}
