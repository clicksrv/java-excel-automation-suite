package frameworkcore.errormanagers;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class TestException extends RuntimeException {

    private static final long serialVersionUID = -3270845278455949905L;
    public String errorName;

    public TestException(String errorDescription) {
        super(errorDescription);

        errorName = "Test Exception";
    }

    /**
     * This method is used to handle test exception.
     * 
     * @param errorName - This parameter is of String data type.
     * @param errorDescription - This parameter is of String data type.
     */
    public TestException(String errorName, String errorDescription) {
        super(errorDescription);
        this.errorName = errorName;
    }

    public TestException(Exception e, String errorDescription) {
        super(errorDescription);
        super.addSuppressed(e);
        super.setStackTrace(e.getStackTrace());
    }
}
