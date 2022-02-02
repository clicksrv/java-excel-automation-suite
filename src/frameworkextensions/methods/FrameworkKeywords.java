package frameworkextensions.methods;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.ButtonAndLinksHandler;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator.MessageType;
import frameworkextensions.screenelementhandlers.utilities.ApplicationSync;
import frameworkextensions.screenelementhandlers.utilities.WebElementManager;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class FrameworkKeywords extends TestThread {

	public FrameworkKeywords(ThreadEntities threadEntities) {
		super(threadEntities);
	}

	ApplicationSync applicationSync = new ApplicationSync(threadEntities);
	WebElementManager elementManager = new WebElementManager(threadEntities);

	public void navigate(String CSVlinkNames) {

		testCaseVariables.excelReporter.createNode("Navigation through " + CSVlinkNames);

		String[] path = CSVlinkNames.split("\\s*,\\s*");

		List<String> pathList = new ArrayList<>();

		for (int i = 0; i < path.length; i++) {

			String currentPath = path[i];

			int counter = 1;
			while (pathList.contains(currentPath)) {

				if (Character.isDigit(currentPath.charAt(0))) {
					currentPath = currentPath.substring(1);
				}

				currentPath = counter + currentPath;
				counter++;
			}

			pathList.add(currentPath);
		}

		for (String linkText : pathList) {

			List<WebElement> weList = null;

			applicationSync.waitForApplicationToLoad();

			if (Character.isDigit(linkText.charAt(0))) {
				weList = driver.findElements(By.partialLinkText(linkText.substring(1)));
			} else {
				weList = driver.findElements(By.partialLinkText(linkText));
			}

			WebElement we = null;

			WebDriverWait wait = new WebDriverWait(driver, FrameworkTimeouts.elementTimeOut);

			if (Character.isDigit(linkText.charAt(0))) {
				int index = Integer.parseInt(linkText.substring(0, 1));
				we = weList.get(index);
			} else {
				we = weList.get(0);
			}

			wait.until(ExpectedConditions.visibilityOf(we));

			ButtonAndLinksHandler handler = new ButtonAndLinksHandler(threadEntities);
			handler.clickOnWebElement(Character.isDigit(linkText.charAt(0)) ? linkText.substring(0, 1) : linkText,
					"Link", we, MessageType.NO_SPECIFIC, null);

			if (Character.isDigit(linkText.charAt(0))) {
				linkText = linkText.substring(1);
			}
		}
	}

	public void testScreenForADACompliance() {
		if (TestSettings.adaTestMode) {
		}

	}

}
