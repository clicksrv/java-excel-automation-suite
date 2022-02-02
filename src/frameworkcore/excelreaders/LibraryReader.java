package frameworkcore.excelreaders;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.FrameworkStatics.FrameworkPaths;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class LibraryReader extends ExcelReader {

    public LibraryReader() {
        super(FrameworkPaths.libraryPath, null);
    }

    public void loadBatchMetadata() {
        loadWorksheet("Batch Job Metadata");

        int lastRow = getRowCount();

        String streamName = null;
        String jobPath = null;
        String jobName = null;
        String ordinal = null;
        String fileName = null;

        for (int i = 1; i <= lastRow; i++) {
            streamName = getCellValue(i, 0);
            ordinal = getCellValue(i, 1);
            jobPath = getCellValue(i, 2);
            jobName = getCellValue(i, 3);
            fileName = getCellValue(i, 4);

            FrameworkEntities.metadata.getBatchJobStreamMetadata().addBatchJobStream(streamName, jobPath, ordinal,
                    jobName, fileName);
        }

    }
}
