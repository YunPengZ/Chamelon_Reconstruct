package Bean;

public class ConflictNode {
    public int confliId;
    public String confliAttr;
//    public String confliCell;
    public int operator;//��ͻ�Ĳ�����

    public ConflictNode(int confliId, String confliAttr, int operator) {
        this.confliId = confliId;
        this.confliAttr = confliAttr;
        this.operator = operator;
    }
}
