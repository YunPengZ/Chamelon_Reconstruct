package Bean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TaxBean implements Serializable {
    private int id;
    private String FName;
    private String LName;
    private String Gender;
    private String AreaCode;
    private String Phone;
    private String City;
    private String State;
    private String Zip;
    private String MaritalStatus;
    private String HasChild;
    private int Salary;
    private int Rate;//数据库中为Double类型
    private int SingleExemp;
    private int MarriedExemp;
    private int ChildExemp;

    @Override
    public String toString() {
        return "TaxBean{" +
                "id=" + id +
                ", FName='" + FName + '\'' +
                ", LName='" + LName + '\'' +
                ", Gender='" + Gender + '\'' +
                ", AreaCode='" + AreaCode + '\'' +
                ", Phone='" + Phone + '\'' +
                ", City='" + City + '\'' +
                ", State='" + State + '\'' +
                ", Zip='" + Zip + '\'' +
                ", MaritalStatus='" + MaritalStatus + '\'' +
                ", HasChild='" + HasChild + '\'' +
                ", Salary=" + Salary +
                ", Rate=" + Rate +
                ", SingleExemp=" + SingleExemp +
                ", MarriedExemp=" + MarriedExemp +
                ", ChildExemp=" + ChildExemp +
                '}';
    }

    public TaxBean(int id, String args[]){
        this.id = id;
        this.FName = args[0];
        this.LName = args[1];
        this.Gender = args[2];
        this.AreaCode = args[3];
        this.Phone = args[4];
        this.City = args[5];
        this.State = args[6];
        this.Zip = args[7];
        this.MaritalStatus = args[8];
        this.HasChild = args[9];
        this.Salary = Integer.parseInt(args[10]);
        this.Rate = (int)(Double.parseDouble(args[11])*100);
        this.SingleExemp = Integer.parseInt(args[12]);
        this.MarriedExemp = Integer.parseInt(args[13]);
        this.ChildExemp = (int)Double.parseDouble(args[14]);
    }

    public String get(String para){
        char[] chars = para.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char)(chars[0] - 32);
        }
        para = new String(chars);
        String value= null;
        Method method = null;
        try {
            if (para.equals("Id")){
                return Integer.toString(this.getId());
            }
            if (para.equals("FName")){
                return this.getFName();
            }
            if (para.equals("LName")){
                return this.getLName();
            }
            if (para.equals("Gender")){
                return this.getGender();
            }
            if (para.equals("AreaCode")){
                return this.getAreaCode();
            }
            if (para.equals("Phone")){
                return this.getPhone();
            }
            if (para.equals("City")){
                return this.getCity();
            }
            if (para.equals("State")){
                return this.getState();
            }
            if (para.equals("Zip")){
                return this.getZip();
            }
            if (para.equals("MaritalStatus")){
                return this.getMaritalStatus();
            }
            if (para.equals("HasChild")){
                return this.getHasChild();
            }
            if (para.equals("Salary")){
                return Integer.toString(this.getSalary());
            }
            if (para.equals("Rate")){
                return Integer.toString(this.getRate());
            }
            if (para.equals("SingleExemp")){
                return Integer.toString(this.getSingleExemp());
            }
            if (para.equals("MarriedExemp")){
                return Integer.toString(this.getMarriedExemp());
            }
            if (para.equals("ChildExemp")){
                return Integer.toString(this.getChildExemp());
            }
            System.out.println("Tax unknown param: " + para);


        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFName() {
        return FName;
    }

    public void setFName(String FName) {
        this.FName = FName;
    }

    public String getLName() {
        return LName;
    }

    public void setLName(String LName) {
        this.LName = LName;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getAreaCode() {
        return AreaCode;
    }

    public void setAreaCode(String areaCode) {
        AreaCode = areaCode;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
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

    public String getMaritalStatus() {
        return MaritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        MaritalStatus = maritalStatus;
    }

    public String getHasChild() {
        return HasChild;
    }

    public void setHasChild(String hasChild) {
        this.HasChild = hasChild;
    }

    public int getSalary() {
        return Salary;
    }

    public void setSalary(int salary) {
        Salary = salary;
    }

    public int getRate() {
        return Rate;
    }

    public void setRate(int rate) {
        Rate = rate;
    }

    public int getSingleExemp() {
        return SingleExemp;
    }

    public void setSingleExemp(int singleExemp) {
        SingleExemp = singleExemp;
    }

    public int getMarriedExemp() {
        return MarriedExemp;
    }

    public void setMarriedExemp(int marriedExemp) {
        MarriedExemp = marriedExemp;
    }

    public int getChildExemp() {
        return ChildExemp;
    }

    public void setChildExemp(int childExemp) {
        ChildExemp = childExemp;
    }
}
