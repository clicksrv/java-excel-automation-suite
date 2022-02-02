package frameworkcore.datablocks;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public class AddressBlock {

    private String zip;
    private String countyCode;
    private String countyDecode;
    private String city;
    private String stateCode;
    private String stateDecode;

    public String getStateDecode() {
        return stateDecode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountyDecode() {
        return countyDecode;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public String getZip() {
        return zip;
    }
}
