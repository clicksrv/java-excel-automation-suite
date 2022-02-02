package frameworkextensions.screenelementhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.executors.PageUtility;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testdatamanagement.TestDataManager;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.InstructionParser;
import frameworkextensions.screenelementhandlers.utilities.TextHTMLWrapper;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator.MessageType;

/**
 * @author Ashish Vishwakarma (ashish.b.vishwakarma), Vedhanth Reddy
 *         (v.reddy.monajigari), Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class DataTableHandler extends TestThread implements IWebElementHandler {

	public DataTableHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator applicationMessageValidator = new ApplicationMessageValidator(threadEntities);
	PageUtility pageUtility = new PageUtility(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);
	String dataTableName = null;

	/**
	 * @param xpath
	 * @param instruction
	 * @throws InterruptedException
	 */

	@Override
	public void handleElement(ScriptActionBlock actionBlock) {
		String xpath = actionBlock.getElementXpath();
		String instruction = actionBlock.getValue();
		String dataTableName = actionBlock.getElementName();

		if (instruction.equals("")) {
			@SuppressWarnings("unused")
			String debugString = instruction;
		}

		this.dataTableName = dataTableName;

		String noRecordsXpath = xpath + "//*[contains(text(),'No records found')]";
		List<WebElement> blankDatatable = driver.findElements(By.xpath(noRecordsXpath));
		if (blankDatatable.size() > 0) {
			reportingManager.updateTestLog(Status.FATAL, dataTableName + " has no records!", true);
		}

		InstructionParser parser = new InstructionParser(threadEntities);
		Map<Integer, String> mapping = new HashMap<Integer, String>();

		Map<String, List<String>> map = parser.parseInstruction(instruction);
		List<String> keyList = new ArrayList<String>();
		List<Integer> idxList = new ArrayList<Integer>();
		TestDataManager tdm = new TestDataManager(threadEntities);
		String nextPage = "//span[@title='Next Page' or text()='N']";
		boolean continueFlag = true;
		int clickIdx = 0;
		int assertIdx = 0;

		for (String key : map.keySet()) {
			keyList.add(key);
		}

		createHeaderColMapping(mapping, keyList, idxList, xpath);

		if (!StringUtils.containsIgnoreCase(instruction, "where")) {

			List<Integer> index = new ArrayList<>();
			index.add(idxList.get(0));

			conditionLessAction(mapping, map, index, xpath);

		} else {
			while (continueFlag) {
				List<WebElement> dataTableRows = elementManager.waitAndFindElements(xpath + "/tbody/tr");
				String key = null;
				int rowIndex = 0;
				List<Integer> clickIdxList = new ArrayList<Integer>();

				for (WebElement rowElement : dataTableRows) {
					boolean isConditionMatch = false;
					for (int i = 0; i < idxList.size(); i++) {

						List<String> tempList = map.get(mapping.get(idxList.get(i)));

						WebElement we = null;

						if (map.containsKey("ConfirmRow#Set")) {
							we = rowElement.findElement(By.xpath("td[" + (idxList.get(i) + 1) + "]" + tempList.get(1)));
						} else {
							we = rowElement.findElements(By.tagName("td")).get(idxList.get(i));
						}

						if (tempList.contains("Click")) {
							clickIdx = idxList.get(i);
						} else if (tempList.contains("Assert Selected Dropdown Value")
								|| tempList.contains("Assert Selected Radio Button")
								|| tempList.contains("Assert Field Value")
								|| tempList.contains("Assert Selected Checkboxes")) {
							assertIdx = idxList.get(i);
						} else if (tempList.contains("Set Field Value")
								|| tempList.contains("Clear and Set Field Value")
								|| tempList.contains("Select Dropdown By Value")
								|| tempList.contains("Select Dropdown By Index")
								|| tempList.contains("Select Dropdowns By Value")
								|| tempList.contains("Select Dropdowns By Index")
								|| tempList.contains("Select Random Value") || tempList.contains("Select Checkboxes")
								|| tempList.contains("Select Radio Button")) {
							key = mapping.get(idxList.get(i));
						} else if ((tempList.get(tempList.size() - 1)
								.equalsIgnoreCase(tdm.elementManager.getTextFromWebElement(we)))) {
							isConditionMatch = true;
						} else {
							isConditionMatch = false;
							break;
						}
					}

					if (isConditionMatch) {
						if (map.containsKey("ConfirmRow#Set")) {
							processConfirmAction(map, keyList, xpath, rowIndex, mapping);
						} else if (map.containsKey("EditRow") || map.containsKey("DeleteRow")) {
							processEditOrDeleteAction(map, keyList, xpath, (rowIndex + 1));
						} else if (StringUtils.isNotBlank(key) && (map.get(key).contains("Set Field Value")
								|| map.get(key).contains("Set Field Value")
								|| map.get(key).contains("Clear and Set Field Value")
								|| map.get(key).contains("Select Dropdown By Value")
								|| map.get(key).contains("Select Dropdown By Index")
								|| map.get(key).contains("Select Dropdowns By Value")
								|| map.get(key).contains("Select Dropdowns By Index")
								|| map.get(key).contains("Select Random Value")
								|| map.get(key).contains("Select Checkboxes")
								|| map.get(key).contains("Select Radio Button") || map.get(key).contains("Select"))) {
							PageUtility utility = new PageUtility(threadEntities);
							List<String> actionList = map.get(key);
							utility.handlePageElement(new ScriptActionBlock(key, actionList.get(0), actionList.get(2),
									xpath + "/tbody/tr[" + (rowIndex + 1) + "]/td" + actionList.get(1),
									actionList.get(actionList.size() - 1)));
						} else {
							clickIdxList.add(clickIdx);
							processOtherDatatableActions(mapping, map, clickIdxList, assertIdx, rowElement);

						}
						continueFlag = false;
						break;
					} else {
						rowIndex = rowIndex + 1;
					}
				}
				if (rowIndex != 0 && (rowIndex) % 5 == 0 && rowIndex >= dataTableRows.size()) {
					driver.findElement(By.xpath(nextPage)).click();
					applicationSync.waitForApplicationToLoad();
				} else if (continueFlag) {
					continueFlag = false;
					for (int i = 0; i < idxList.size(); i++) {
						List<String> tempList = map.get(mapping.get(idxList.get(i)));
						reportingManager.updateTestLog(Status.FAIL, "'" + dataTableName + "' did not contain value '"
								+ tempList.get(tempList.size() - 1) + "'", true);
						;
					}
					reportingManager.updateTestLog(Status.FATAL, "Datatable selection failed!", true);
				}
			}
		}
	}

	/**
	 * @param mapping
	 * @param map
	 * @param clickIdxList
	 * @param assertIdx
	 * @param rowElement
	 */
	private void processOtherDatatableActions(Map<Integer, String> mapping, Map<String, List<String>> map,
			List<Integer> clickIdxList, int assertIdx, WebElement rowElement) {

		if (mapping.get(clickIdxList.get(0)) != null && map.get(mapping.get(clickIdxList.get(0))) != null
				&& map.get(mapping.get(clickIdxList.get(0))).contains("Click")) {
			performClickActionOnDatatable(mapping, map, clickIdxList, rowElement);
		} else {
			// method call for assertion in datatable
			performAssertActionOnDatatable(mapping, map, assertIdx, rowElement);
		}
	}

	/**
	 * @param mapping
	 * @param map
	 * @param assertIdx
	 * @param rowElement
	 */
	private void performAssertActionOnDatatable(Map<Integer, String> mapping, Map<String, List<String>> map,
			int assertIdx, WebElement rowElement) {
		WebElement we = rowElement.findElements(By.tagName("td")).get(assertIdx);
		List<String> tempList = map.get(mapping.get(assertIdx));

		if (StringUtils.equals(tempList.get(tempList.size() - 2), "Assert Field Value")
				&& StringUtils.equalsIgnoreCase(tempList.get(tempList.size() - 1), we.getText())) {
			reportingManager.updateTestLog(Status.PASS, "Assertion Pass --> " + mapping.get(assertIdx) + " = "
					+ we.getText() + ", value asserted successfully", false);
		} else {
			reportingManager.updateTestLog(Status.FAIL,
					"Assertion Fail --> " + mapping.get(assertIdx) + " != " + we.getText() + ", value assertion failed",
					true);
		}
	}

	/**
	 * @param mapping
	 * @param map
	 * @param clickIdxList
	 * @param rowElement
	 */
	private void performClickActionOnDatatable(Map<Integer, String> mapping, Map<String, List<String>> map,
			List<Integer> clickIdxList, WebElement rowElement) {

		List<WebElement> link = rowElement.findElements(
				By.xpath("td[" + (clickIdxList.get(0) + 1) + "]" + map.get(mapping.get(clickIdxList.get(0))).get(1)));

		if (link.size() == 1) {
			if (link.get(0).isEnabled()) {
				startTimer();

				reportingManager.updateTestLog(Status.PASS,
						getClickedString(mapping.get(clickIdxList.get(0)) + " in DataTable " + dataTableName), false);

				link.get(0).click();

				applicationSync.waitForApplicationToLoad();

				reportingManager.updateTestLog(Status.INFO,
						getLoadTimeString(mapping.get(clickIdxList.get(0)) + " in DataTable " + dataTableName), true);

				applicationMessageValidator.checkForDisplayedMessages(MessageType.NO_SPECIFIC, null);

			} else {
				reportingManager.updateTestLog(Status.FATAL,
						getElementDisabledString(mapping.get(clickIdxList.get(0)) + " in DataTable " + dataTableName),
						true);
			}

		} else {
			for (int i = 0; i < link.size(); i++) {
				if (validateLinkOrButton(link.get(i))) {
					if (link.get(i).isEnabled()) {
						startTimer();

						reportingManager.updateTestLog(Status.PASS,
								getClickedString(mapping.get(clickIdxList.get(0)) + " in DataTable " + dataTableName),
								true);

						link.get(i).click();

						applicationSync.waitForApplicationToLoad();

						reportingManager.updateTestLog(Status.INFO,
								getLoadTimeString(mapping.get(clickIdxList.get(0)) + " in DataTable " + dataTableName),
								true);

						applicationMessageValidator.checkForDisplayedMessages(MessageType.NO_SPECIFIC, null);

					} else {
						reportingManager.updateTestLog(Status.FATAL, getElementDisabledString(
								mapping.get(clickIdxList.get(0)) + " in DataTable " + dataTableName), true);
					}
				}
			}
		}
	}

	private String getElementDisabledString(String elementLabel) {
		return elementLabel + " is displayed but is not enabled!";
	}

	private String getLoadTimeString(String elementLabel) {
		return "Load time after clicking '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' was "
				+ TextHTMLWrapper.wrapValue(stopTimerAndReturnElapsedTime()) + ".";
	}

	private String getClickedString(String elementLabel) {
		return "User clicked on '" + TextHTMLWrapper.wrapElementLabel(elementLabel) + "' button.";
	}

	/**
	 * @param link
	 * @return
	 */
	private boolean validateLinkOrButton(WebElement link) {
		return link.getTagName().equals("a") || link.getTagName().equalsIgnoreCase("button")
				|| (link.getTagName().equalsIgnoreCase("input") && (link.getAttribute("type").equalsIgnoreCase("submit")
						|| link.getAttribute("type").equalsIgnoreCase("button")
						|| link.getAttribute("type").equalsIgnoreCase("reset")));
	}

	/**
	 * @param map
	 * @param keyList
	 * @param xpath
	 * @param rowIndex
	 */
	private void processConfirmAction(Map<String, List<String>> map, List<String> keyList, String xpath, int rowIndex,
			Map<Integer, String> mapping) {
		String confirmBtnXpath = xpath + "/tbody/tr[" + (rowIndex + 1) + "]/td";
		int index = 0;
		for (String s : keyList) {
			if (s.contains("#")) {
				String key = s.substring(0, s.indexOf("#"));
				for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
					if (key.equals(entry.getValue())) {
						index = entry.getKey();
						break; // breaking because its one to one map
					}
				}
				List<String> newColumnsList = map.get(s);
				if (newColumnsList.contains("Click")) {
					confirmBtnXpath = confirmBtnXpath + newColumnsList.get(1) + "[2]";
				} else {
					PageUtility pageUtil = new PageUtility(threadEntities);

					pageUtil.handlePageElement(new ScriptActionBlock(s, newColumnsList.get(0), newColumnsList.get(2),
							confirmBtnXpath + "[" + (index + 1) + "]" + newColumnsList.get(1), newColumnsList.get(3)));

				}
			}
		}

		WebElement confirmButtonWE = elementManager.waitAndFindElement(confirmBtnXpath);

		if (elementManager.isElementPresent(confirmButtonWE)) {
			applicationSync.waitForApplicationToLoad();
			driver.findElement(By.xpath(confirmBtnXpath)).click();
			reportingManager.updateTestLog(Status.PASS, "Confirm button clicked successfully !!", true);
		} else {
			updateFailureLog("Unable to click on Confirm Button");
		}
	}

	/**
	 * @param map
	 * @param keyList
	 * @param xpath
	 * @param rowIndex
	 */
	private void processEditOrDeleteAction(Map<String, List<String>> map, List<String> keyList, String xpath,
			int rowIndex) {
		String confirmBtnXpath = xpath + "/tbody/tr[" + rowIndex + "]/td";
		for (String s : keyList) {
			if (StringUtils.equals(s, "EditRow")) {
				performEditClickAction(map, confirmBtnXpath, s);
			} else if (StringUtils.equals(s, "DeleteRow")) {
				performDeleteClickAction(map, confirmBtnXpath, s, rowIndex);
			}
		}

	}

	/**
	 * @param map
	 * @param confirmBtnXpath
	 * @param s
	 */
	private void performEditClickAction(Map<String, List<String>> map, String confirmBtnXpath, String s) {
		List<String> newColumnsList = map.get(s);
		if (newColumnsList.contains("Click")) {
			confirmBtnXpath = confirmBtnXpath + newColumnsList.get(1) + "[1]";
			WebElement confirmButtonWE = elementManager.waitAndFindElement(confirmBtnXpath);
			if (elementManager.isElementPresent(confirmButtonWE)) {
				applicationSync.waitForApplicationToLoad();

				confirmButtonWE.click();

				applicationSync.waitForApplicationToLoad();
				reportingManager.updateTestLog(Status.PASS, "Edit button clicked successfully !!", true);
			} else {
				updateFailureLog("Unable to click on Confirm Button");
			}
		}
	}

	/**
	 * @param map
	 * @param confirmBtnXpath
	 * @param s
	 */
	private void performDeleteClickAction(Map<String, List<String>> map, String confirmBtnXpath, String s,
			int rowIndex) {
		List<String> newColumnsList = map.get(s);
		String popupConfirmBtn = getPopupBtnXpath(confirmBtnXpath, rowIndex);
		if (newColumnsList.contains("Click")) {
			confirmBtnXpath = confirmBtnXpath + newColumnsList.get(1) + "[2]";
			WebElement we = elementManager.waitAndFindElement(confirmBtnXpath);
			if (elementManager.isElementPresent(we)) {
				applicationSync.waitForApplicationToLoad();
				we.click();
				applicationSync.waitForApplicationToLoad();
				WebElement element = elementManager.waitAndFindElement(popupConfirmBtn, true);
				if (null != element) {
					if (element.isDisplayed()) {
						element.click();
						applicationSync.waitForApplicationToLoad();
						reportingManager.updateTestLog(Status.PASS, "Delete button clicked successfully !!", true);
					}
				} else {
					reportingManager.updateTestLog(Status.PASS, "Delete button clicked successfully !!", true);
				}

			} else {
				updateFailureLog("Unable to click on Confirm Button");
			}
		}
	}

	private String getPopupBtnXpath(String confirmBtnXpath, int rowIndex) {
		String popupConfirmBtn = null;
		popupConfirmBtn = confirmBtnXpath.substring(confirmBtnXpath.indexOf("["), confirmBtnXpath.lastIndexOf("'"));
		popupConfirmBtn = "//button" + popupConfirmBtn + ":" + (rowIndex - 1) + "')]/span[text()='Confirm']";
		return popupConfirmBtn;
	}

	/**
	 * @param elementLabel
	 */
	private void updateFailureLog(String elementLabel) {

		reportingManager.updateTestLog(Status.FAIL, elementLabel, true);
	}

	/**
	 * @param mapping
	 * @param keyList
	 * @param idxList
	 * @param xpath
	 */
	private void createHeaderColMapping(Map<Integer, String> mapping, List<String> keyList, List<Integer> idxList,
			String xpath) {
		List<WebElement> dataTableHeader = driver.findElements(By.xpath(xpath + "/thead/tr[1]/th"));

		if (dataTableHeader.size() == 0) {
			reportingManager.updateTestLog(Status.FATAL, "'" + dataTableName + "' was not found on the screen!", true);
		}

		List<WebElement> dataTableHeader2 = driver.findElements(By.xpath(xpath + "/thead/tr[2]/th"));
		List<String> keyListWoBracket = new ArrayList<>();

		int headerColIdx = 0;
		int header2ColIdx = 0;

		for (String key : keyList) {
			keyListWoBracket.add(key.replaceAll("\\s*\\({1}.*\\){1}", "").trim());
		}

		for (WebElement headerCols : dataTableHeader) {

			String headerText = headerCols.getText().trim().split("\n")[0];

			if (keyList.contains(headerText)) {
				mapping.put(headerColIdx, headerText);
			} else if (StringUtils.isEmpty(headerText)) {
				if (!(headerColIdx > keyList.size())) {
					mapping.put(headerColIdx, keyList.get(headerColIdx));
				}
			} else if (keyListWoBracket.contains(headerText)) {
				mapping.put(headerColIdx, headerText);
			}

			int noOfColumns = 1;

			try {
				noOfColumns = Integer.parseInt(headerCols.getAttribute("colspan"));
			} catch (Exception e) {
			}

			if (noOfColumns > 1) {

				if (dataTableHeader2.size() > 0) {

					int limit = header2ColIdx + noOfColumns;

					for (int h2idx = header2ColIdx; h2idx < limit; h2idx++) {

						String header2Text = dataTableHeader2.get(header2ColIdx).getText().trim();

						if (keyList.contains(header2Text)) {
							mapping.put(headerColIdx, header2Text);
						} else if (keyListWoBracket.contains(header2Text)) {
							mapping.put(headerColIdx, header2Text);
						}

						headerColIdx++;
						header2ColIdx++;
					}
				} else {
					headerColIdx++;
				}
			} else {
				headerColIdx++;
			}

		}
		for (Integer key : mapping.keySet()) {
			idxList.add(key);
		}
	}

	private void conditionLessAction(Map<Integer, String> mapping, Map<String, List<String>> map, List<Integer> index,
			String xpath) {

		WebElement row = elementManager.waitAndFindElement(xpath + "/tbody/tr[1]");
		int i = 0;
		String action = map.get(mapping.get(index.get(0))).get(2);

		List<String> validClickActions = Arrays.asList("Click", "Select Checkboxes", "Select Radio Button");
		List<String> validAssertActions = Arrays.asList("Assert Field Value");

		if (validClickActions.contains(action)) {
			performClickActionOnDatatable(mapping, map, index, row);
		} else if (validAssertActions.contains(action)) {
			i = index.get(0);
			performAssertActionOnDatatable(mapping, map, i, row);
		} else {
			reportingManager.updateTestLog(Status.FATAL, action + " is not supported on condition-less actions!",
					false);
		}

	}
}
