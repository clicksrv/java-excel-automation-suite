package frameworkcore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;
import frameworkcore.FrameworkStatics.FrameworkTimeouts;
import frameworkcore.FrameworkStatics.TestSettings;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class WebDriverFactory {

    static RemoteWebDriver driver = null;
    static String os = System.getProperty("os.name").toLowerCase();
    static Platform platform = null;

    static {
        if (os.contains("win")) {
            platform = Platform.WINDOWS;
        } else if (os.contains("nix") || os.contains("aix") || os.contains("nux")) {
            platform = Platform.LINUX;
        } else {
            ErrorHandler.frameworkError(ErrLvl.FATAL, null,
                    "Framework is not configured for current operating system!");
        }
    }

    public enum Browsers {
        CHROME, FIREFOX, IE, EDGE, SAFARI
    }

    /**
     * This method is used to get the driver of the browser on which we wants to
     * execute our scripts
     * 
     * @param browser - This parameter is of Browsers data type
     * @param forTestCase - This parameter carries the name of the test case
     * @return driver of the respective browser
     */
    public static synchronized RemoteWebDriver getDriver(Browsers browser, String forTestCase) {

        new File(FrameworkPaths.preFinalReportPath + FrameworkEntities.fileSeparator + "Downloads"
                + FrameworkEntities.fileSeparator).mkdirs();

        driver = Main.getWebDriverFromPool(browser, forTestCase);

        if (null == driver) {
            try {
                driver = getNewDriver(browser);
            } catch (UnreachableBrowserException e) {
                ErrorHandler.frameworkError(ErrLvl.FATAL, e,
                        "Could not reach GRID Hub or Node! Check if GRID Hub is up and running and Nodes are registered to the GRID!");
            } catch (WebDriverException e) {
                ErrorHandler.frameworkError(ErrLvl.FATAL, e, "Failed to instantiate " + browser.toString() + "!");
            }

            Main.addWebDriverToPool(browser, driver, forTestCase);
        }

        return driver;
    }

    private static RemoteWebDriver getNewDriver(Browsers browser)
            throws WebDriverException, UnreachableBrowserException {

        switch (browser) {
        case FIREFOX:
            FirefoxOptions firefoxOptions = getFirefoxOptions();

            if (TestSettings.gridMode) {
                driver = new RemoteWebDriver(firefoxOptions);
            } else {
                WebDriverManager.firefoxdriver().setup();
                System.out.println("GeckoDriver being used: "+System.getProperty("webdriver.gecko.driver"));
                driver = new FirefoxDriver(firefoxOptions);
            }
            break;
            
        case CHROME:
            ChromeOptions chromeOptions = getChromeOptions();

            if (TestSettings.gridMode) {
                driver = new RemoteWebDriver(chromeOptions);
            } else {
                WebDriverManager.chromedriver().setup();
                System.out.println("ChromeDriver being used: "+System.getProperty("webdriver.chrome.driver"));
                driver = new ChromeDriver();
                if (TestSettings.adaTestMode) {
                    // getADATestExtension();
                }
            }
            break;
        case EDGE:
            EdgeOptions edgeOptions = getEdgeOptions();

            if (TestSettings.gridMode) {
                driver = new RemoteWebDriver(edgeOptions);
            } else {
                WebDriverManager.edgedriver().setup();
                System.out.println("EdgeDriver being used: "+System.getProperty("webdriver.edge.driver"));
                driver = new EdgeDriver(edgeOptions);
            }
            break;
            
        case IE:
            InternetExplorerOptions ieOptions = getIEOptions();

            if (TestSettings.gridMode) {
                driver = new RemoteWebDriver(ieOptions);
            } else {
                WebDriverManager.iedriver().setup();
                System.out.println("IEDriver being used: "+System.getProperty("webdriver.ie.driver"));
                driver = new InternetExplorerDriver(ieOptions);
            }
            break;
        default:
            ErrorHandler.frameworkError(ErrLvl.FATAL, null, browser.toString() + " is currently not supported!");
            break;
        }

        if (TestSettings.adaTestMode) {
            driver.manage().window().maximize();
        } else {
            if (TestSettings.hideBrowser) {
                driver.manage().window().setPosition(new Point(-2000, 0));
            } else {
                driver.manage().window().setPosition(new Point(0, 0));
            }
        }

        driver.manage().timeouts().pageLoadTimeout(FrameworkTimeouts.pageLoadTimeOut, TimeUnit.SECONDS);

        return driver;
    }

    private static EdgeOptions getEdgeOptions() {
		EdgeOptions options = new EdgeOptions();
		options.setCapability("ms:inPrivate", true);
		return options;
	}

	private static FirefoxOptions getFirefoxOptions() {
        FirefoxProfile profile = new FirefoxProfile();

        profile.setPreference("network.proxy.type", 0);
        profile.setPreference("browser.privatebrowsing.autostart", true);
        profile.setPreference("browser.download.dir", FrameworkPaths.preFinalReportPath
                + FrameworkEntities.fileSeparator + "Downloads" + FrameworkEntities.fileSeparator);
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/octet-stream;application/csv;text/csv");
        profile.setPreference("pdfjs.disabled", true);

        profile.setAcceptUntrustedCertificates(true);

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        capabilities.setJavascriptEnabled(true);
        capabilities.acceptInsecureCerts();

        return new FirefoxOptions(capabilities);
    }

    private static ChromeOptions getChromeOptions() {

        Map<String, Object> preferences = new HashMap<String, Object>();

        preferences.put("profile.content_settings.pattern_pairs.*.multiple-automatic-downloads", 1);
        preferences.put("download.prompt_for_download", "false");
        preferences.put("profile.default_content_settings.popups", 0);
        preferences.put("download.default_directory", FrameworkPaths.preFinalReportPath
                + FrameworkEntities.fileSeparator + "Downloads" + FrameworkEntities.fileSeparator);
        preferences.put("download.extensions_to_open",
                "application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/octet-stream");
        preferences.put("download.extensions_to_open", "text/csv,application/pdf");

        ChromeOptions options = new ChromeOptions();

        if (TestSettings.adaTestMode) {
           // Code to load extension for ADA testing
        } else {
            options.addArguments("--disable-extensions", "--incognito"); // disabling extensions
        }

        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("prefs", preferences);

        if (platform.equals(Platform.WINDOWS)) {
            if (TestSettings.hideBrowser) {
                options.addArguments("--disable-gpu"); // applicable to windows os only
            }
        } else if (platform.equals(Platform.LINUX)) {
            options.addArguments("--headless", "--disable-dev-shm-usage");
        }
        options.addArguments("test-type", "start-maximized", "--disable-popup-blocking", "--disable-default-apps",
                "--auto-launch-at-startup", "--always-authorize-plugins", "--disable-infobars",
                "--disable-infobar-for-protected-media-identifier", "--safebrowsing-disable-download-protection",
                "--ignore-certificate-errors", "--no-sandbox");

        return options;
    }

    private static InternetExplorerOptions getIEOptions() {

        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

        capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        capabilities.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
        capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
        capabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, false);
        capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
        capabilities.setCapability(InternetExplorerDriver.ELEMENT_SCROLL_BEHAVIOR, false);
        capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
        capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "accept");
        capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        capabilities.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
        capabilities.setCapability(InternetExplorerDriver.SILENT, true);

        capabilities.setJavascriptEnabled(true);
        capabilities.acceptInsecureCerts();

        return new InternetExplorerOptions(capabilities);
    }

}
