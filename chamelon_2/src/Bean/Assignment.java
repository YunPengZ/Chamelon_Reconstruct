package Bean;

import java.io.Serializable;

public class Assignment implements Serializable {
    public  boolean satisfy;
    public String cell;
    public String val;

    public Assignment( String cell, String val,boolean satisfy) {
        this.satisfy = satisfy;
        this.cell = cell;
        this.val = val;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "satisfy=" + satisfy +
                ", cell='" + cell + '\'' +
                ", val='" + val + '\'' +
                '}';
    }
}
