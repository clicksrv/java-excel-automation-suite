package frameworkcore.reportutilities;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.Status;

import frameworkcore.FrameworkConstants.HTMLFormat;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.testthread.TestCaseVariables;
import frameworkcore.testthread.TestParameters;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class ReportingManager {

    TestCaseVariables testCaseVariables;
    WebDriver driver;
    TestParameters testParameters;

    public ReportingManager(TestCaseVariables testCaseVariables, WebDriver driver, TestParameters parameters) {
        this.testCaseVariables = testCaseVariables;
        this.driver = driver;
        testParameters = parameters;
    }

    public void createReportNode(String str) {
    	testCaseVariables.excelReporter.createNode(str);
    }
    
    
    public void updateTestLog(Status status, String text, boolean captureScreenshot) {

        String statusText = status.toString().toUpperCase();

        if (!captureScreenshot) {
            if (status.equals(Status.FAIL) || status.equals(Status.FATAL) || status.equals(Status.ERROR)) {
                printErrorText(text, statusText);
            } else {
                printText(text, statusText);
            }
            testCaseVariables.excelReporter.createStep(status,
                    HTMLFormat.STEP + getCurrentStepNumber() + HTMLFormat.CLOSE + text);
        } else {
            if (status.equals(Status.FAIL) || status.equals(Status.FATAL) || status.equals(Status.ERROR)) {
                printErrorText(text, statusText);
                testCaseVariables.excelReporter.createStepWithScreenshot(status,
                        HTMLFormat.STEP + getCurrentStepNumber() + HTMLFormat.CLOSE + text,
                        createScreenshot(ScreenshotStyle.FULL, status));
            } else {
                printText(text, statusText);
                testCaseVariables.excelReporter.createStepWithScreenshot(status,
                        HTMLFormat.STEP + getCurrentStepNumber() + HTMLFormat.CLOSE + text,
                        createScreenshot(text.contains("Load time after clicking") ? ScreenshotStyle.PARTIAL
                                : ScreenshotStyle.FULL, status));
            }
        }

        if (status.equals(Status.FATAL)) {
            ErrorHandler.testError(ErrLvl.FATAL, null, text, testCaseVariables);
        }
    }

    private String stripHTML(String text) {
        return Jsoup.parse(text.replaceAll("\n", "@#").replaceAll("\t", "#@")).text().replaceAll("@#", "\n")
                .replaceAll("#@", "\t");
    }

    private void printText(String text, String statusText) {

        String writingString = "[" + getCurrentStepNumber() + statusText + "] " + stripHTML(text) + "\n";

        if (TestSettings.writeTestConsoleToFile) {
            testCaseVariables.logWriter.append(writingString);

        } else {
            System.out.print(writingString);
        }
    }

    private void printErrorText(String text, String statusText) {

        String writingString = "[" + getCurrentStepNumber() + statusText + "] " + stripHTML(text)
                + (TestSettings.writeTestConsoleToFile ? "\t<<<!" : "") + "\n";

        if (TestSettings.writeTestConsoleToFile) {
            testCaseVariables.logWriter.append(writingString);

        } else {
            System.err.print(writingString);
        }

    }

    private String getCurrentStepNumber() {
        if (testCaseVariables.currentStepNumber > 0) {
            String step = "Step " + String.valueOf(testCaseVariables.currentStepNumber + 1) + ":";
            if (testCaseVariables.currentPreDefinedFlowLevel > 0) {

                for (int i : testCaseVariables.currentPreDefinedFlowStepNumber) {
                    step = step + String.valueOf(i) + ":";
                }
                return step + " ";
            }

            return step + " ";
        } else {
            return "";
        }
    }

    public void debug(String text, boolean captureScreenshot) {

        Status status = Status.DEBUG;
        String statusText = status.toString().toUpperCase();

        if (!captureScreenshot) {
            printText(text, statusText);
            if (TestSettings.writeDebugToReport) {
                testCaseVariables.excelReporter.createStep(status, text);
            }

        } else {
            printText(text, statusText);
            if (TestSettings.writeDebugToReport) {
                testCaseVariables.excelReporter.createStepWithScreenshot(status, text,
                        createScreenshot(ScreenshotStyle.PARTIAL, status));
            }
        }
    }

    public void updateTestLogFatalWithException(Exception e, String text, boolean captureScreenshot) {

        Status status = Status.FATAL;
        String statusText = status.toString().toUpperCase();

        if (!captureScreenshot) {
            printErrorText(text, statusText);
        } else {
            printErrorText(text, statusText);
            testCaseVariables.excelReporter.createStepWithScreenshot(status, text,
                    createScreenshot(ScreenshotStyle.FULL, status));
        }

        ErrorHandler.testError(ErrLvl.FATAL, e, text, testCaseVariables);

    }

    private enum ScreenshotStyle {
        FULL, PARTIAL;
    }

    private String createScreenshot(ScreenshotStyle style, Status status) {

        String shotName = null;

        if (status.equals(Status.FAIL) || status.equals(Status.FATAL)) {
            shotName = "Screenshot_" + testParameters.getCurrentTestCaseOutput() + "_"
                    + (++testCaseVariables.screenshotCount) + "_" + status.toString().toUpperCase();
        } else {
            shotName = "Screenshot_" + testParameters.getCurrentTestCaseOutput() + "_"
                    + (++testCaseVariables.screenshotCount);
        }

        if (TestSettings.fastExecutionMode && !(status.equals(Status.FAIL) || status.equals(Status.FATAL))) {
            style = ScreenshotStyle.PARTIAL;
        }

        if (style.equals(ScreenshotStyle.FULL)) {
            Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
                    .takeScreenshot(driver);
            try {
                ImageIO.write(screenshot.getImage(), "PNG",
                        new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator
                                + FrameworkPaths.screenshotPath + FrameworkEntities.fileSeparator + shotName + ".png"));
            } catch (IOException e) {
                ErrorHandler.testError(ErrLvl.ERROR, e, "Failed to create Screenshot!", testCaseVariables);
            }
        } else if (style.equals(ScreenshotStyle.PARTIAL)) {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                FileUtils.copyFile(scrFile, new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator
                        + FrameworkPaths.screenshotPath + FrameworkEntities.fileSeparator + shotName + ".png"));
            } catch (IOException e) {
                ErrorHandler.testError(ErrLvl.ERROR, e, "Failed to create Screenshot!", testCaseVariables);

            }
        }
        return FrameworkPaths.screenshotPath + shotName + ".png";
    }

}
