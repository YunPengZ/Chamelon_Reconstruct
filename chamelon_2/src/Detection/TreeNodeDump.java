package Detection;

import java.util.ArrayList;
import java.util.List;

public class TreeNodeDump {
    public int index;//��La�е��±�

    private int tupleId;//
    List<Integer> rootTupleIds;
    List<Integer> tupleIDs;//��ԭ��������˳���Ӧ��Ԫ��id
    List<Integer> ids; //����Ӧ��Ԫ��id
    public TreeNodeDump(int index){
        this.index = index;
        this.ids = new ArrayList<>();
        this.tupleIDs = new ArrayList<>();
        this.rootTupleIds = new ArrayList<>();
    }
    //�����ⲿ�����飬����ʽ��
}
