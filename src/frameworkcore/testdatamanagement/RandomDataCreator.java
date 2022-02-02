package frameworkcore.testdatamanagement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;

import frameworkcore.FrameworkStatics;
import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;
import frameworkcore.datablocks.AddressBlock;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public final class RandomDataCreator {

    private Properties lists = new Properties();

    private static String[] namesList;
    private static String[] companyTypesList;
    private static String[] streetTypesList;
    private static String[] buildingTypesList;

    private static AddressBlock[] texasAddressList;
    private static AddressBlock[] nonTexasAddressList;

    enum State {
        TEXAS, NON_TEXAS
    }

    public RandomDataCreator() {

        try (ZipFile zipFile = new ZipFile((new StringBuilder(String.valueOf(FrameworkStatics.getRelativePath())))
                .append(FrameworkEntities.fileSeparator).append("lib").append(FrameworkEntities.fileSeparator)
                .append("data").append(FrameworkEntities.fileSeparator).append("lists.zip").toString())) {
            ZipEntry entry = zipFile.entries().nextElement();
            InputStream stream = zipFile.getInputStream(entry);
            lists.load(stream);
        } catch (FileNotFoundException fe) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, fe, "Data Lists Zip file not found!");
        } catch (IOException ie) {
            ErrorHandler.frameworkError(ErrLvl.FATAL, ie, "Data Lists Zip could not be read!");
        }

        namesList = lists.getProperty("Names").split(",");
        companyTypesList = lists.getProperty("CompanyTypes").split(",");
        streetTypesList = lists.getProperty("StreetTypes").split(",");
        buildingTypesList = lists.getProperty("BuildingTypes").split(",");

        texasAddressList = transformJSONToAddressBlockArray(lists.getProperty("TexasAddresses"));
        nonTexasAddressList = transformJSONToAddressBlockArray(lists.getProperty("NonTexasAddresses"));

        lists.clear();
        lists = null;
    }

    /*
     * public static void main(String[] args) {
     * 
     * String[] a = "CORP.,L.L.C.,INC.,CO.,LTD.".split(",");
     * 
     * System.out.println(a); }
     */

    private Object randomObjectFromList(Object[] list) {
        int randomIdx = randomNumberBetween(0, list.length - 1);
        return list[randomIdx];
    }

    private AddressBlock[] transformJSONToAddressBlockArray(String jsonContent) {
        Gson gson = new Gson();
        return gson.fromJson(jsonContent, AddressBlock[].class);
    }

    public String randomName() {
        return (String) randomObjectFromList(namesList);
    }

    public String randomEmail(String domainName) {
        String email = randomName() + "." + randomName() + domainName;
        return email;
    }

    public String randomDateBetween(int startYear, int endYear) {

        GregorianCalendar gc = new GregorianCalendar();

        int year = randomNumberBetween(startYear, endYear);

        gc.set(Calendar.YEAR, year);

        int dayOfYear = randomNumberBetween(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR));

        gc.set(Calendar.DAY_OF_YEAR, dayOfYear);

        String mm = String.valueOf(gc.get(Calendar.MONTH) + 1);
        if (mm.length() == 1) {
            mm = "0" + mm;
        }

        String dd = String.valueOf(gc.get(Calendar.DAY_OF_MONTH));
        if (dd.length() == 1) {
            dd = "0" + dd;
        }

        String yyyy = String.valueOf(gc.get(Calendar.YEAR));

        return (mm + "/" + dd + "/" + yyyy);
    }

    public String randomSSN() {
        return randomNumberBetween(111, 665).toString() + "-" + randomNumberBetween(11, 99).toString() + "-"
                + randomNumberBetween(1111, 9999).toString();
    }

    public String randomStreetName() {
        return (randomNumberBetween(10, 9999) + " " + randomName() + " "
                + (String) randomObjectFromList(streetTypesList));
    }

    public String randomAptHouseOthNumber() {
        return ((String) randomObjectFromList(buildingTypesList) + " " + randomNumberBetween(1, 999));
    }

    public AddressBlock randomAddressBlock(State state) {
        if (state.equals(State.TEXAS)) {
            return (AddressBlock) randomObjectFromList(texasAddressList);
        } else {
            return (AddressBlock) randomObjectFromList(nonTexasAddressList);
        }
    }

    public String randomEmployerOrOrganizationName() {
        return (randomName() + " " + (String) randomObjectFromList(companyTypesList));
    }

    public Long randomNumberBetween(long min, long max) {
        return min + Math.round(Math.random() * ((max - min)));
    }

    public Integer randomNumberBetween(int min, int max) {
        return min + (int) Math.round(Math.random() * ((max - min)));
    }

}


