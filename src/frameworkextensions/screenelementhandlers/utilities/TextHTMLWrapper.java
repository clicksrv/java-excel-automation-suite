package frameworkextensions.screenelementhandlers.utilities;

import frameworkcore.FrameworkConstants.HTMLFormat;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TextHTMLWrapper {

    public static synchronized String wrapValue(String value) {
        return (HTMLFormat.VALUE + value + HTMLFormat.CLOSE);
    }

    public static synchronized String wrapElementLabel(String elementLabel) {
        return (HTMLFormat.FIELD + elementLabel + HTMLFormat.CLOSE);
    }

}
