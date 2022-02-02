package frameworkextensions.screenelementhandlers.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.syslogcapturetools.LogCaptureManager;
import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ApplicationMessageValidator extends TestThread {

	public ApplicationMessageValidator(ThreadEntities threadEntities) {
		super(threadEntities);
	}

	public enum MessageType {
		NO_SPECIFIC, MESSAGE, ERROR, WARNING
	}

	static final String PORTLET_UNAVAILABLE_XPATH = "//span[contains(@class,'wpthemeAccess')]//following-sibling::div[1]";

	static final String MESSAGES_HEADER_XPATH = "//div[@class='messageheading' and text()='Messages']";

	public static final String ERRORS_HEADER_XPATH = "//div[@class='messageheading' and text()='Errors']";

	public static final String WARNINGS_HEADER_XPATH = "//div[@class='messageheading' and text()='Warnings']";

	static final String SYSTEM_ERRORS_HEADER_XPATH = "//div[@class='messageheading' and text()='System Errors']";

	static final String BUSINESS_EXCEPTIONS_HEADER_XPATH = "//div[@class='messageheading' and text()='Business Exceptions']";

	public static final String MESSAGE_LIST_XPATH = "//following-sibling::div[@class='message']";

	public static final String LIST_XPATH = "//li";

	public List<MessageType> checkForDisplayedMessages(MessageType expectedMessageType, String expectedMessage) {

		driver.manage().timeouts().implicitlyWait(3, TimeUnit.MILLISECONDS);

		List<WebElement> portletMessages = driver.findElements(By.xpath(PORTLET_UNAVAILABLE_XPATH));

		List<MessageType> displayedMsgTypes = new ArrayList<ApplicationMessageValidator.MessageType>();

		for (WebElement webElement : portletMessages) {
			String elementText = webElement.getText().toLowerCase();
			if (StringUtils.contains(elementText, "portlet is unavailable")) {
				reportingManager.updateTestLog(Status.FATAL, "The portlet is unavailable!", true);
			} else if (StringUtils.contains(elementText, "portlet is disabled")) {
				reportingManager.updateTestLog(Status.FATAL, "The portlet is disabled!", true);
			}
		}

		if (isMessageDisplayed(MESSAGES_HEADER_XPATH)) {
			List<String> messages = getMessages(MESSAGES_HEADER_XPATH);

			boolean correctMsgDisplayed = false;

			for (String message : messages) {

				if (StringUtils.isNotBlank(expectedMessage)) {
					if (expectedMessageType.equals(MessageType.MESSAGE)
							&& StringUtils.containsIgnoreCase(message, expectedMessage)) {
						correctMsgDisplayed = true;
					}
				} else {
					correctMsgDisplayed = true;
				}

				reportingManager.updateTestLog(Status.INFO, "Message displayed: " + message, false);
			}

			if (expectedMessageType.equals(MessageType.MESSAGE)) {

				if (correctMsgDisplayed) {
					reportingManager.updateTestLog(Status.PASS,
							TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
									+ " was displayed!",
							true);
				} else {
					reportingManager.updateTestLog(Status.FAIL,
							TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
									+ " was not displayed!",
							true);
				}
			}

			displayedMsgTypes.add(MessageType.MESSAGE);
		}

		if (isMessageDisplayed(WARNINGS_HEADER_XPATH)) {
			List<String> messages = getMessages(WARNINGS_HEADER_XPATH);

			boolean correctMsgDisplayed = false;

			for (String message : messages) {

				if (StringUtils.isNotBlank(expectedMessage)) {
					if (expectedMessageType.equals(MessageType.WARNING)
							&& StringUtils.containsIgnoreCase(message, expectedMessage)) {
						correctMsgDisplayed = true;
					}
				} else {
					correctMsgDisplayed = true;
				}

				reportingManager.updateTestLog(Status.INFO, "Warning displayed: " + message, false);
			}

			if (expectedMessageType.equals(MessageType.WARNING)) {

				if (correctMsgDisplayed) {
					reportingManager.updateTestLog(Status.PASS,
							TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
									+ " was displayed!",
							true);
				} else {
					reportingManager.updateTestLog(Status.FAIL,
							TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
									+ " was not displayed!",
							true);
				}
			}

			displayedMsgTypes.add(MessageType.WARNING);
		}

		boolean errorDisplayed = false;
		boolean correctErrDisplayed = false;

		if (isMessageDisplayed(ERRORS_HEADER_XPATH)) {
			List<String> messages = getMessages(ERRORS_HEADER_XPATH);

			for (String message : messages) {

				if (StringUtils.isNotBlank(expectedMessage)) {
					if (expectedMessageType.equals(MessageType.ERROR)
							&& StringUtils.containsIgnoreCase(message, expectedMessage)) {
						correctErrDisplayed = true;
					}
				} else {
					correctErrDisplayed = true;
				}

				if (!expectedMessageType.equals(MessageType.ERROR)) {
					reportingManager.updateTestLog(Status.FAIL, "Error displayed: " + message, false);
				} else {
					reportingManager.updateTestLog(Status.INFO, "Error displayed: " + message, false);
				}
			}

			errorDisplayed = true;
		}

		if (isMessageDisplayed(SYSTEM_ERRORS_HEADER_XPATH)) {
			List<String> messages = getMessages(SYSTEM_ERRORS_HEADER_XPATH);

			for (String message : messages) {

				if (StringUtils.isNotBlank(expectedMessage)) {
					if (expectedMessageType.equals(MessageType.ERROR)
							&& StringUtils.containsIgnoreCase(message, expectedMessage)) {
						correctErrDisplayed = true;
					}
				} else {
					correctErrDisplayed = true;
				}

				if (!expectedMessageType.equals(MessageType.ERROR)) {
					reportingManager.updateTestLog(Status.FAIL, "System Error displayed: " + message, false);
				} else {
					reportingManager.updateTestLog(Status.INFO, "System Error displayed: " + message, false);
				}

				LogCaptureManager logCaptureManager = new LogCaptureManager();

				Pattern MY_PATTERN = Pattern.compile("\\[(.*?)\\]");

				Matcher m = MY_PATTERN.matcher(message);

				String errCd = null;

				while (m.find()) {
					errCd = m.group(1);
				}

				String logs = logCaptureManager.captureLogsForScreen(errCd);

				reportingManager.updateTestLog(Status.INFO, logs, false);

			}

			errorDisplayed = true;
		}

		if (isMessageDisplayed(BUSINESS_EXCEPTIONS_HEADER_XPATH)) {
			List<String> messages = getMessages(BUSINESS_EXCEPTIONS_HEADER_XPATH);

			for (String message : messages) {

				if (StringUtils.isNotBlank(expectedMessage)) {
					if (expectedMessageType.equals(MessageType.ERROR)
							&& StringUtils.containsIgnoreCase(message, expectedMessage)) {
						correctErrDisplayed = true;
					}
				} else {
					correctErrDisplayed = true;
				}

				if (!expectedMessageType.equals(MessageType.ERROR)) {
					reportingManager.updateTestLog(Status.FAIL, "Business Exception displayed: " + message, false);
				} else {
					reportingManager.updateTestLog(Status.INFO, "Business Exception displayed: " + message, false);
				}
			}
			errorDisplayed = true;
		}

		if (errorDisplayed) {
			displayedMsgTypes.add(MessageType.ERROR);
		}

		if (errorDisplayed && !expectedMessageType.equals(MessageType.ERROR)) {
			reportingManager.updateTestLog(TestSettings.adaTestMode ? Status.FAIL : Status.FATAL,
					"Errors were displayed!", true);
		} else if (errorDisplayed && expectedMessageType.equals(MessageType.ERROR)) {

			if (correctErrDisplayed) {
				reportingManager.updateTestLog(Status.PASS,
						TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
								+ " was displayed!",
						true);
			} else {
				reportingManager.updateTestLog(Status.FAIL,
						TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
								+ " was not displayed!",
						true);
			}
		} else if (!errorDisplayed && expectedMessageType.equals(MessageType.ERROR)) {

			reportingManager.updateTestLog(Status.FAIL,
					TextHTMLWrapper.wrapValue(getCorrectMsg(expectedMessage, expectedMessageType))
							+ " was not displayed!",
					true);
		}

		driver.manage().timeouts().implicitlyWait(FrameworkTimeouts.pageLoadTimeOut, TimeUnit.MILLISECONDS);

		return displayedMsgTypes;
	}

	private String getCorrectMsg(String expectedMessage, MessageType expectedMessageType) {
		String correctMsg;
		if (StringUtils.isNotBlank(expectedMessage)) {
			correctMsg = "'" + expectedMessage + "'";
		} else {
			switch (expectedMessageType) {
			case ERROR:
				correctMsg = "Error";
				break;
			case WARNING:
				correctMsg = "Warning";
				break;
			case MESSAGE:
			default:
				correctMsg = "Message";
				break;
			}
		}
		return correctMsg;
	}

	public boolean isMessageDisplayed(String messageHeadingXpath) {

		List<WebElement> messageHeadings = driver.findElements(By.xpath(messageHeadingXpath));

		for (WebElement messageHeading : messageHeadings) {
			if (messageHeading.isDisplayed()) {
				return true;
			}
		}

		return false;
	}

	public List<String> getMessages(String xpathExpression) {
		List<String> messages = new ArrayList<String>();

		List<WebElement> messageListElements = driver
				.findElements(By.xpath(xpathExpression + MESSAGE_LIST_XPATH + LIST_XPATH));

		if (messageListElements.size() == 0) {
			messageListElements = driver.findElements(By.xpath(xpathExpression + MESSAGE_LIST_XPATH));
		}

		for (WebElement webElement : messageListElements) {
			String msgText = webElement.getText();

			if (StringUtils.isNotBlank(msgText)) {
				messages.add(msgText);
			}
		}

		return messages;
	}

	public void messageTest(WebElement we, List<WebElement> lists) {

		ArrayList<String> errors = new ArrayList<>();

		for (int i = 0; i < lists.size(); i++) {
			String errTxt = lists.get(i).getText().trim().toLowerCase();
			if (errTxt.contains("error") || errTxt.contains("bs-") || errTxt.contains("sc-")
					|| errTxt.contains("invalid") || errTxt.contains("is required. (go to")
					|| errTxt.contains("incorrect") || errTxt.contains("failed") || errTxt.contains("sd-")) {

				errors.add(lists.get(i).getText());
			} else {
				reportingManager.updateTestLog(Status.INFO, "Message displayed: " + lists.get(i).getText(), true);
			}
		}

		if (errors.size() > 0) {
			for (String error : errors) {
				reportingManager.updateTestLog(Status.FAIL, "Error displayed: " + error, true);
			}

			LogCaptureManager logCaptureManager = new LogCaptureManager();

			String logs = logCaptureManager.captureLogsForScreen(testCaseVariables.currentPageOrBJStreamKeyword);

			if (StringUtils.isNotBlank(logs)) {
				reportingManager.updateTestLog(Status.FATAL,
						"Test Application threw errors! Log excerpt is as below: " + logs, true);
			} else {
				reportingManager.updateTestLog(Status.FATAL, "Test Application threw errors!", true);
			}

		}

	}

	public boolean isMessagePresent(WebElement message) {

		try {
			if (message.isDisplayed()) {
				return true;
			}
		} catch (Exception e) {
		}

		return false;

	}
}
