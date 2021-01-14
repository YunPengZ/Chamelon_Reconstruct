package Bean;

public class ConflictNode {
    public int confliId;
    public String confliAttr;
//    public String confliCell;
    public int operator;//³åÍ»µÄ²Ù×÷·û

    public ConflictNode(int confliId, String confliAttr, int operator) {
        this.confliId = confliId;
        this.confliAttr = confliAttr;
        this.operator = operator;
    }
}
