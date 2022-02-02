package frameworkextensions.methods;

import com.aventstack.extentreports.Status;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import frameworkcore.FrameworkConstants;
import frameworkcore.FrameworkStatics.FrameworkPropertyFiles;
import frameworkcore.FrameworkStatics.FrameworkSettings;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.datablocks.ScriptActionBlock;
import frameworkcore.executors.PageUtility;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;
import frameworkextensions.screenelementhandlers.utilities.ApplicationMessageValidator;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class URLLoaders extends TestThread {

	public URLLoaders(ThreadEntities threadEntities) {
		super(threadEntities);
	}

	ApplicationMessageValidator generic = new ApplicationMessageValidator(threadEntities);

	String baseURL = null;
	String loadedURL = null;

	public void loadApplicationURL() {

		logout();

		reportingManager.createReportNode("Loading Application URL on Browser");

		baseURL = FrameworkPropertyFiles.urlProperties.getProperty("Application_" + TestSettings.currentEnvironment);

		if (StringUtils.isBlank(baseURL)) {
			reportingManager.updateTestLog(Status.FATAL, "No URL available for " + FrameworkSettings.projectName
					+ " on Environment: " + TestSettings.currentEnvironment + " in application-url-config.properties",
					false);
		}

		startTimer();

		driver.get(baseURL);
		loadedURL = driver.getCurrentUrl();

		reportingManager.updateTestLog(Status.INFO,
				"URL Loaded for environment '" + FrameworkConstants.HTMLFormat.VALUE + testParameters.getEnvironment()
						+ FrameworkConstants.HTMLFormat.CLOSE + "' is: " + FrameworkConstants.HTMLFormat.FIELD
						+ loadedURL + FrameworkConstants.HTMLFormat.CLOSE,
				true);

		reportingManager.updateTestLog(Status.INFO, "Time taken to load URL was " + FrameworkConstants.HTMLFormat.VALUE
				+ stopTimerAndReturnElapsedTime() + FrameworkConstants.HTMLFormat.CLOSE, false);

		checkIfPortalIsDown();
	}

	private void checkIfPortalIsDown() {
		if (driver
				.findElements(By.xpath(
						FrameworkPropertyFiles.frameworkProperties.getProperty("OnLoadUniquePortalElementXpath")))
				.size() == 0) {
			reportingManager.updateTestLog(Status.FATAL, "Portal is down!", false);
		}
	}

	public void logout() {

		try {
			if (driver
					.findElement(By.xpath(FrameworkPropertyFiles.frameworkProperties.getProperty("LogOutElementXpath")))
					.isDisplayed()) {

				reportingManager.createReportNode("Logging out of application");

				PageUtility pageUtility = new PageUtility(threadEntities);
				pageUtility.handlePageElement(new ScriptActionBlock("Logout Link", "Link", "Click",
						FrameworkPropertyFiles.frameworkProperties.getProperty("LogOutElementXpath"), null));

			}
		} catch (NoSuchElementException e) {
		}
	}
}
