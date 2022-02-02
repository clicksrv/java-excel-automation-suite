package frameworkcore.datablocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class Metadata {

    private ApplicationMetadata applicationMetadata;
    private BatchJobStreamMetadata batchJobStreamMetadata;

    public Metadata() {
        applicationMetadata = new ApplicationMetadata();
        batchJobStreamMetadata = new BatchJobStreamMetadata();
    }

    public ApplicationMetadata getApplicationMetadata() {
        return applicationMetadata;
    }

    public BatchJobStreamMetadata getBatchJobStreamMetadata() {
        return batchJobStreamMetadata;
    }

    /**
     * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
     */
    public class ApplicationMetadata {

        private HashMap<String, ScreenMetadata> screens = new HashMap<>();

        /**
         * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
         */
        public class ScreenMetadata {

            private String elementName;
            private String elementType;
            private String xpath;

            private HashMap<String, DatatableMetadata> datatables = new HashMap<>();
            private HashMap<String, TreetableMetadata> treetables = new HashMap<>();

            /**
             * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
             */
            public class DatatableMetadata {

                private String elementName;
                private String elementType;
                private String xpath;

                @Override
                public String toString() {
                    return "[Element Name: " + elementName + ", Element Type: " + elementType + ", XPath: " + xpath
                            + "]";
                }
            }

            /**
             * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
             */
            public class TreetableMetadata {

                private String elementName;
                private String elementType;
                private String xpath;
                private int level;

                @Override
                public String toString() {
                    return "[Element Name: " + elementName + ", Element Type: " + elementType + ", XPath: " + xpath
                            + ", Level: " + level + "]";
                }
            }

            @Override
            public String toString() {
                return "[Element Name: " + elementName + ", Element Type: " + elementType + ", XPath: " + xpath + "]";
            }

        }
    }

    /**
     * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
     */
    public class BatchJobStreamMetadata {

        private HashMap<String, List<BatchJob>> batchJobStreams = new HashMap<>();

        private final Comparator<BatchJob> compareByOrdinalAscending = new Comparator<Metadata.BatchJobStreamMetadata.BatchJob>() {

            @Override
            public int compare(BatchJob bj1, BatchJob bj2) {
                return (bj1.getOrdinal() > bj2.getOrdinal()) ? 1 : ((bj2.getOrdinal() > bj1.getOrdinal()) ? -1 : 0);
            }
        };

        /**
         * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
         */
        public class BatchJob {

            private double ordinal;
            private String batchJobName;
            private String fileName;
            private String jobPath;

            public BatchJob(double ordinal, String batchJobName, String fileName, String jobPath) {
                this.ordinal = ordinal;
                this.batchJobName = batchJobName;
                this.fileName = StringUtils.isBlank(fileName) ? null : fileName;
                this.jobPath = jobPath;
            }

            @Override
            public String toString() {
                return "[Job Name = " + batchJobName + ", Ordinal = " + ordinal + ", Job Path = " + jobPath
                        + (null == fileName ? "" : ", IFM Key = " + fileName + "]");
            }

            public boolean equals(BatchJob batchJob) {
                return batchJobName.equals(batchJob.batchJobName);
            }

            public double getOrdinal() {
                return ordinal;
            }

            public String getBatchJobName() {
                return batchJobName;
            }

            public String getFileName() {
                return fileName;
            }

            public String getJobPath() {
                return jobPath;
            }
        }

        public void addBatchJobStream(String streamName, String jobPath, String ordinal, String jobName,
                String fileName) {
            if (StringUtils.isNotBlank(streamName)) {
                if (batchJobStreamExists(streamName)) {
                    batchJobStreams.get(streamName)
                            .add(new BatchJob(Double.parseDouble(ordinal), jobName, fileName, jobPath));
                } else {
                    batchJobStreams.put(streamName, new ArrayList<BatchJob>());
                    batchJobStreams.get(streamName)
                            .add(new BatchJob(Double.parseDouble(ordinal), jobName, fileName, jobPath));
                }
            }
        }

        public List<BatchJob> getBatchJobsInStream(String streamName) {
            List<BatchJob> batchJobStream = batchJobStreams.get(streamName);
            batchJobStream.sort(compareByOrdinalAscending);
            return batchJobStream;
        }

        private boolean batchJobStreamExists(String streamName) {
            return batchJobStreams.containsKey(streamName);
        }

        public boolean isMetadataLoaded() {
            return batchJobStreams.size() > 0;
        }
    }

}
