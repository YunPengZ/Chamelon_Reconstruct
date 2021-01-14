package Bean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HospitalBean implements Serializable {
    //此处应该使用多态实现
    private int id;
    private String ProviderId;
    private String HospitalName;
    //private String Address;
    private String City;
    private String State;
    private String ZipCode;
    private String CountryName;
    private String PhoneNumber;
    private String HospitalType;
    private String HospitalOwner;
    private String EmergencyService;
    private String Condition;
    private String MeasureCode;
    private String MeasureName;
    private String Sample;
    private String StateAvg;

    public HospitalBean(int id,String[] args) {
        this.id = id;
        this.ProviderId = args[0];
        this.HospitalName = args[1];
        //this.Address = args[2];
        this.City = args[2];
        this.State = args[3];
        this.ZipCode = args[4];
        this.CountryName = args[5];
        this.PhoneNumber = args[6];
        this.HospitalType = args[7];
        this.HospitalOwner = args[8];
        this.EmergencyService = args[9];
        this.Condition = args[10];
        this.MeasureCode = args[11];
        this.MeasureName = args[12];
        this.Sample = args[13];
        this.StateAvg = args[14];
    }

    public String get(String para){

        String value= null;
        Method method = null;
        char[] chars = para.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char)(chars[0] - 32);
        }
        para = new String(chars);
        try {
            if (para.equals("Id")){
                return Integer.toString(this.getId());
            }
            if (para.equals("ProviderId")){
                return this.getProviderId();
            }
            if (para.equals("HospitalName")){
                return this.getHospitalName();
            }
            if (para.equals("City")){
                return this.getCity();
            }
            if (para.equals("State")){
                return this.getState();
            }
            if (para.equals("PhoneNumber")){
                return this.getPhoneNumber();
            }
            if (para.equals("HospitalType")){
                return this.getHospitalType();
            }
            if (para.equals("HospitalOwner")){
                return this.getHospitalOwner();
            }
            if (para.equals("EmergencyService")){
                return this.getEmergencyService();
            }
            if (para.equals("Condition")){
                return this.getCondition();
            }
            if (para.equals("MeasureCode")){
                return this.getMeasureCode();
            }
            if (para.equals("MeasureName")){
                return this.getMeasureName();
            }
            if (para.equals("Sample")){
                return this.getSample();
            }
            if (para.equals("StateAvg")){
                return this.getStateAvg();
            }
            if (para.equals("ZipCode")){
                return this.getZipCode();
            }
            if (para.equals("CountryName")){
                return this.getCountryName();
            }
            System.out.println("Hospital unknown param: " + para);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return value;
    }
    @Override
    public String toString() {
        return "HospitalBean{" +
                "ProviderId='" + ProviderId + '\'' +
                ", HospitalName='" + HospitalName + '\'' +
                ", City='" + City + '\'' +
                ", State='" + State + '\'' +
                ", Zipcode=" + ZipCode +
                ", Countryname='" + CountryName + '\'' +
                ", PhoneNumber='" + PhoneNumber + '\'' +
                ", HospitalType='" + HospitalType + '\'' +
                ", HospitalOwner='" + HospitalOwner + '\'' +
                ", EmergencyService='" + EmergencyService + '\'' +
                ", Condition='" + Condition + '\'' +
                ", MeasureCode='" + MeasureCode + '\'' +
                ", MeasureName='" + MeasureName + '\'' +
                ", Sample='" + Sample + '\'' +
                ", StateAvg='" + StateAvg + '\'' +
                '}';
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getProviderId() {
        return ProviderId;
    }

    public void setProviderId(String providerId) {
        ProviderId = providerId;
    }

    public String getHospitalName() {
        return HospitalName;
    }

    public void setHospitalName(String hospitalName) {
        HospitalName = hospitalName;
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
    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getHospitalType() {
        return HospitalType;
    }

    public void setHospitalType(String hospitalType) {
        HospitalType = hospitalType;
    }

    public String getHospitalOwner() {
        return HospitalOwner;
    }

    public void setHospitalOwner(String hospitalOwner) {
        HospitalOwner = hospitalOwner;
    }

    public String getEmergencyService() {
        return EmergencyService;
    }

    public void setEmergencyService(String emergencyService) {
        EmergencyService = emergencyService;
    }

    public String getCondition() {
        return Condition;
    }

    public void setCondition(String condition) {
        Condition = condition;
    }

    public String getMeasureCode() {
        return MeasureCode;
    }

    public void setMeasureCode(String measureCode) {
        MeasureCode = measureCode;
    }

    public String getMeasureName() {
        return MeasureName;
    }

    public void setMeasureName(String measureName) {
        MeasureName = measureName;
    }

    public String getSample() {
        return Sample;
    }

    public void setSample(String Sample) {
        Sample = Sample;
    }

    public String getStateAvg() {
        return StateAvg;
    }

    public void setStateAvg(String stateAvg) {
        StateAvg = stateAvg;
    }

    public String getZipCode() {
        return ZipCode;
    }

    public void setZipCode(String zipCode) {
        ZipCode = zipCode;
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String countryName) {
        CountryName = countryName;
    }

    public String get_all_params(){
        Field[] fields = this.getClass().getDeclaredFields();
        List<String>  list = new ArrayList<>();
        for(Field field: fields){
            list.add(field.getName());
        }
        return String.join(",",list);
    }
}

