package frameworkextensions.screenelementhandlers.utilities;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import frameworkcore.testthread.TestThread;
import frameworkcore.testthread.ThreadEntities;

/**
 * @author Rahul Patidar (rahul.b.patidar)
 */
public class JSUtilities extends TestThread {

    public JSUtilities(ThreadEntities threadEntities) {

        super(threadEntities);
    }

    /**
     * This method is used to highlight the element present on the screen.
     * 
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     */
    public void flash(WebElement element, WebDriver driver) {
        String bgcolor = element.getCssValue("backgroundColor");
        for (int i = 0; i < 10; i++) {
            changeColor("rgb(0,200,0)", element, driver);
            changeColor(bgcolor, element, driver);
        }
    }

    /**
     * This method is used to change the color of the element present on the screen.
     * 
     * @param color - This parameter is of String data type
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     */
    public void changeColor(String color, WebElement element, WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.backgroundColor = '" + color + "'", new Object[] { element });
        try {
            Thread.sleep(20L);
        } catch (InterruptedException localInterruptedException) {
        }
    }

    /**
     * This method is used to draw the border on element present on the screen.
     * 
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     */
    public void drawBorder(WebElement element, WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border='3px solid red'", new Object[] { element });
    }

    /**
     * This method is used to generate the Alert on element present on the screen.
     * 
     * @param driver - This parameter is of WebDriver type
     * @param dmessageriver - This parameter is of String data type
     */
    public void generateAlert(WebDriver driver, String message) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("alert('" + message + "')", new Object[0]);
    }

    /**
     * This method is used to click on the element present on the screen.
     * 
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     */
    public void clickElementByJS(WebElement element, WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", new Object[] { element });
    }

    /**
     * This method is used to select value present on the screen.
     * 
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     * @param value - This parameter is of String type
     */
    public void setValueByJS(WebElement element, WebDriver driver, String value) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);arguments[0].setAttribute('value', '" + value + "');",
                new Object[] { element });
    }

    /**
     * This method is used to refresh browser.
     * 
     * @params Argument : driver - This parameter is of WebDriver type
     */
    public void refreshBrowserByJS(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("history.go(0)", new Object[0]);
    }

    /**
     * This method is used to get the title of the screen.
     * 
     * @params Argument : driver - This parameter is of WebDriver type
     */
    public String getTitleByJS(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String title = js.executeScript("return document.title;", new Object[0]).toString();
        return title;
    }

    /**
     * This method is used to get the innertext of the element present on the
     * screen.
     * 
     * @params Argument : driver - This parameter is of WebDriver type
     */
    public String getPageInnerText(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String pageText = js.executeScript("return document.documentElement.innerText;", new Object[0]).toString();
        return pageText;
    }

    /**
     * This method is used to scroll page.
     * 
     * @params Argument : driver - This parameter is of WebDriver type
     */
    public void scrollPageDown(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0,document.body.scrollHeight)", new Object[0]);
    }

    /**
     * This method is used to scroll into view.
     * 
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     */
    public void scrollIntoView(WebElement element, WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", new Object[] { element });
    }

    /**
     * This method is used to select date by JS.
     * 
     * @param element - This parameter is of WebElement type
     * @param driver - This parameter is of WebDriver type
     * @param dateValue - This parameter is of String data type
     */
    public void selectDateByJS(WebElement element, WebDriver driver, String dateValue) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].setAttribute('value','" + dateValue + "');", new Object[] { element });
    }

    /**
     * This method is used to scroll down.
     */
    public void scroll_drown() {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    /**
     * This method is used to scroll down using length.
     */
    public void scroll_drown_using_length() {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, 300)");
    }

    /**
     * This method is used to scroll up.
     */
    public void scroll_up() {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, -document.body.scrollHeight);");
    }

    /**
     * This method is used to scroll up using length.
     */
    public void scroll_up_using_length() {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, -300)");
    }

    /**
     * This method is used to click on element by JS.
     * 
     * @param element - This parameter is of WebElement type
     */
    public void click(WebElement we) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", we);
    }

    /**
     * This method is used to focus on element by JS.
     */
    public void focusOnElement() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.focus();");

    }

}
