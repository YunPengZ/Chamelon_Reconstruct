package Bean;

import java.util.List;

public class RepairExp {
    public int exp;
    public String val;
    public String confliCell;
    public String cell;
    public RepairExp(int exp, String val,String cell,String confliCell) {
        this.exp = exp;
        this.val = val;
        this.confliCell = confliCell;
        this.cell = cell;
    }

    @Override
    public String toString() {
        return "RepairExp{" +
                "exp=" + exp +
                ", val='" + val + '\'' +
                ", confliCell='" + confliCell + '\'' +
                ", cell='" + cell + '\'' +
                '}';
    }
}
