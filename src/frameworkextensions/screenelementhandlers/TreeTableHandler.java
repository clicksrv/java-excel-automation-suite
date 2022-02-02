package frameworkextensions.screenelementhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aventstack.extentreports.Status;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.datablocks.TreeTableDTO;
import frameworkcore.executors.PageUtility;
import frameworkcore.interfaces.IWebElementHandler;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.TreeTableInstructionParser;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Ashish Vishwakarma (ashish.b.vishwakarma)
 */
public class TreeTableHandler extends TestThread implements IWebElementHandler {

	public TreeTableHandler(ThreadEntities threadEntities) {

		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);
	PageUtility pageUtility = new PageUtility(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);
	ApplicationSync applicationSync = new ApplicationSync(threadEntities);

	/**
	 * This method is used to handle Tree table available on the screem
	 * 
	 * @param xpath       : This parameter is of String data type
	 * @param instruction : This parameter is of String data type
	 */
	@Override
	public void handleElement(ScriptActionBlock actionBlock) {

		String xpath = actionBlock.getElementXpath();
		String instruction = actionBlock.getValue();

		TreeTableInstructionParser parser = new TreeTableInstructionParser(threadEntities);
		Map<Integer, String> mapping = new HashMap<Integer, String>();
		TreeTableDTO treetable = parser.parseTreeTableInstruction(instruction);
		List<String> keyList = new ArrayList<String>();
		List<Integer> idxList = new ArrayList<Integer>();
		List<String> nodeList = new ArrayList<String>();
		for (String key : treetable.getNodePathMap().keySet()) {
			nodeList.add(key);
		}

		for (String key : treetable.getBeforeWhereMap().keySet()) {
			keyList.add(key);
		}
		createHeaderColMapping(mapping, keyList, idxList, xpath);

		List<WebElement> dataTableRows = driver.findElements(By.xpath(xpath + "/tbody/tr"));
		int nodeLevelCounter = 0;
		int expRowIndex = 0;
		int rowIndex = 1;
		int nodeLevel = nodeList.size();
		boolean isClick = false;
		boolean isExpandable = true;
		while (isExpandable) {
			if (!isClick) {
				for (WebElement rowElement : dataTableRows) {
					int colCount = rowElement.findElements(By.tagName("td")).size();
					for (int i = 0; i < colCount; i++) {

						WebElement we = rowElement.findElements(By.tagName("td")).get(i);

						if (StringUtils.isNotBlank(we.getText())
								&& StringUtils.equalsIgnoreCase(nodeList.get(0), we.getText())) {
							if (treetable.getNodePathMap().get(nodeList.get(0)).get(1).contains("span")) {
								we = rowElement.findElement(By.xpath("td[" + (i + 1) + "]/"
										+ treetable.getNodePathMap().get(nodeList.get(0)).get(1)));
								we.click();
								applicationSync.waitForApplicationToLoad();
								dataTableRows = driver.findElements(By.xpath(xpath + "/tbody/tr"));
								nodeLevelCounter++;
								expRowIndex = rowIndex;
								isClick = true;
								nodeList.remove(i);
								break;
							} else {
								if (nodeList.size() == 1
										&& treetable.getBeforeWhereMap().get("selectionnode").contains("Click")) {
									we = rowElement.findElement(By.xpath("td[" + (i + 1) + "]/"
											+ treetable.getNodePathMap().get(nodeList.get(0)).get(1)));
									we.click();
									applicationSync.waitForApplicationToLoad();
								}
							}
						}
					}
					rowIndex++;
					if (isClick) {
						break;
					}
				}
			} else {

				WebElement rowElement = dataTableRows.get(expRowIndex);
				int colCount = rowElement.findElements(By.tagName("td")).size();
				for (int i = 0; i < colCount; i++) {
					WebElement we = rowElement.findElements(By.tagName("td")).get(i);
					if (StringUtils.isNotBlank(we.getText())
							&& StringUtils.equalsIgnoreCase(nodeList.get(0), we.getText())) {
						we = rowElement.findElement(By.xpath(
								"td[" + (i + 1) + "]/" + treetable.getNodePathMap().get(nodeList.get(0)).get(1)));
						if (we.isDisplayed()) {
							we.click();
							applicationSync.waitForApplicationToLoad();
						}
						dataTableRows = driver.findElements(By.xpath(xpath + "/tbody/tr"));
						nodeLevelCounter++;
						isClick = true;
						nodeList.remove(i);
						break;
					}
				}
				expRowIndex++;
			}
			if (nodeLevelCounter == nodeLevel) {
				isExpandable = false;
			}
		}

		List<WebElement> actionRow = driver.findElements(By.xpath(xpath + "/tbody/tr[" + expRowIndex + "]"));
		for (int i = 0; i < idxList.size(); i++) {
			for (WebElement actionCol : actionRow) {
				List<String> actionList = treetable.getBeforeWhereMap().get(mapping.get(idxList.get(i)));
				String value = actionList.get(actionList.size() - 1);
				if (actionList.get(2).equals("AssertValue") && actionCol
						.findElement(By.xpath("td[" + (idxList.get(i) + 1) + "]")).getText().equalsIgnoreCase(value)) {
					reportingManager.updateTestLog(Status.PASS, value + " Asserted Successfully !!", false);
					break;
				}
			}
		}

	}

	/**
	 * This method is used to create maapping b/w header and column of Tree table
	 * 
	 * @param mapping : This parameter is Map of Integer,String data type
	 * @param keyList : This parameter is List of String data type
	 * @param idxList : This parameter is List of Integer data type
	 * @param xpath   : This parameter is of String data type
	 */
	private void createHeaderColMapping(Map<Integer, String> mapping, List<String> keyList, List<Integer> idxList,
			String xpath) {
		List<WebElement> dataTableHeader = driver.findElements(By.xpath(xpath + "/thead/tr/th"));
		int headerColIdx = 0;
		for (WebElement headerCols : dataTableHeader) {
			if (keyList.contains(headerCols.getText())) {
				mapping.put(headerColIdx, headerCols.getText());
			}
			headerColIdx++;
		}
		for (Integer key : mapping.keySet()) {
			idxList.add(key);
		}
	}
}
