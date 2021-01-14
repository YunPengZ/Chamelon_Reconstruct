package Repair;

public class RepairBound {
    public boolean isNumeric;
    public int uppBound;
    public int lowBound;
    public String val;

    public RepairBound(boolean isNumeric) {
        this.isNumeric = isNumeric;
    }

    @Override
    public String toString() {
        return "RepairBound{" +
                "isNumeric=" + isNumeric +
                ", uppBound=" + uppBound +
                ", lowBound=" + lowBound +
                ", val='" + val + '\'' +
                '}';
    }
}
