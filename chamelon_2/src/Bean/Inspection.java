package Bean;

import javafx.util.Pair;
public class Inspection {
    int InspectionID;
    String DBAName;
    String AKANmae;
    int License;
    String FacilityType;
    String Rish;
    String Address;
    String City;
    String State;
    String Zip;//Zip虽然是数字类型，但是以字符串类型存储
    String InspectionData;
    String InspectionType;
    String Results;
    String Violations;
    Double Latitude;
    Double Longitude;
    Pair<Double,Double> Location;

    public Inspection(String []args) {
        InspectionID = Integer.parseInt(args[1]);
        this.DBAName = args[2];
        this.AKANmae = args[3];
        License = Integer.parseInt(args[4]);
        FacilityType = args[5];
        Rish = args[6];
        Address = args[7];
        City = args[8];
        State = args[9];
        Zip = args[10];
        InspectionData = args[11];
        InspectionType = args[12];
        Results = args[13];
        Violations = args[14];
        Latitude = Double.parseDouble(args[15]);
        Longitude = Double.parseDouble(args[16]);
        double left = Double.parseDouble(args[17].substring(2));
        double right = Double.parseDouble(args[18].substring(0,args[18].length()-2));
        Location = new Pair<>(left,right);
    }

    public int getInspectionID() {
        return InspectionID;
    }

    public void setInspectionID(int inspectionID) {
        InspectionID = inspectionID;
    }

    public String getDBAName() {
        return DBAName;
    }

    public void setDBAName(String DBAName) {
        this.DBAName = DBAName;
    }

    public String getAKANmae() {
        return AKANmae;
    }

    public void setAKANmae(String AKANmae) {
        this.AKANmae = AKANmae;
    }

    public int getLicense() {
        return License;
    }

    public void setLicense(int license) {
        License = license;
    }

    public String getFacilityType() {
        return FacilityType;
    }

    public void setFacilityType(String facilityType) {
        FacilityType = facilityType;
    }

    public String getRish() {
        return Rish;
    }

    public void setRish(String rish) {
        Rish = rish;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getZip() {
        return Zip;
    }

    public void setZip(String zip) {
        Zip = zip;
    }

    public String getInspectionData() {
        return InspectionData;
    }

    public void setInspectionData(String inspectionData) {
        InspectionData = inspectionData;
    }

    public String getInspectionType() {
        return InspectionType;
    }

    public void setInspectionType(String inspectionType) {
        InspectionType = inspectionType;
    }

    public String getResults() {
        return Results;
    }

    public void setResults(String results) {
        Results = results;
    }

    public String getViolations() {
        return Violations;
    }

    public void setViolations(String violations) {
        Violations = violations;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }

    public Pair<Double, Double> getLocation() {
        return Location;
    }

    public void setLocation(Pair<Double, Double> location) {
        Location = location;
    }
}
