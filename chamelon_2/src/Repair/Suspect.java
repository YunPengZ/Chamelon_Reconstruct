package Repair;

import java.io.Serializable;

public class Suspect implements Serializable {
    //suspect ֻ��������¼���½磬
    public boolean isNumeric;
    public int uppBound;
    public int lowBound;
    public String val;

    public Suspect(boolean isNumeric) {
        this.isNumeric = isNumeric;
    }

    @Override
    public String toString() {
        return "Suspect{" +
                "isNumeric=" + isNumeric +
                ", uppBound=" + uppBound +
                ", lowBound=" + lowBound +
                ", val='" + val + '\'' +
                '}';
    }
}
