package frameworkcore;

import java.text.SimpleDateFormat;

import frameworkcore.FrameworkStatics.FrameworkEntities;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo), Surbhi Sinha
 *         (surbhi.b.sinha)
 */
public class FrameworkConstants {

    static {
        StandardFormats.defaultDateFormat.setLenient(false);
        StandardFormats.defaultTimeFormat.setLenient(false);
        StandardFormats.defaultDateTimeFormat.setLenient(false);
    }

    public static class FrameworkPaths {

        public static final String RUN_MANAGER = "Run Manager.xlsm";
        public static final String LIBRARY = "Library.xlsx";
        public static final String BATCH_DATASHEET = "Batch_Datasheets" + FrameworkEntities.fileSeparator;
        public static final String PRE_DEFINED_FLOWS = "Scripts" + FrameworkEntities.fileSeparator
                + "PreDefinedFlows.xlsx";
    }

    public static class StandardFormats {
        public static final String DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss.SSS";
        public static final String TIME_FORMAT = "HH:mm:ss.SSS";
        public static final String DATE_FORMAT = "MM/dd/yyyy";
        public static SimpleDateFormat defaultTimeFormat = new SimpleDateFormat(StandardFormats.TIME_FORMAT);
		public static SimpleDateFormat defaultDateFormat = new SimpleDateFormat(StandardFormats.DATE_FORMAT);
		public static SimpleDateFormat defaultDateTimeFormat = new SimpleDateFormat(StandardFormats.DATE_TIME_FORMAT);
    }

    public static class HTMLFormat {
        public static final String VALUE = "<font color=\"Aqua\"><b>";
        public static final String FIELD = "<font color=\"SandyBrown\"><b>";
        public static final String STEP = "<font color=\"#fff44f\"><b>";
        public static final String CLOSE = "</font></b>";
    }

}
