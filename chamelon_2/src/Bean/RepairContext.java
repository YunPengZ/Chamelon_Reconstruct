package Bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepairContext {
    public List<RepairExp> repairExpList;
    public Set<String> frontier;

    public RepairContext(String startCell) {
        this.frontier = new HashSet<>();
        this.frontier.add(startCell);
        this.repairExpList = new ArrayList<>();
    }
}
