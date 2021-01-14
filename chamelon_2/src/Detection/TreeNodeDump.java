package Detection;

import java.util.ArrayList;
import java.util.List;

public class TreeNodeDump {
    public int index;//在La中的下标

    private int tupleId;//
    List<Integer> rootTupleIds;
    List<Integer> tupleIDs;//树原本的排列顺序对应的元组id
    List<Integer> ids; //树对应的元组id
    public TreeNodeDump(int index){
        this.index = index;
        this.ids = new ArrayList<>();
        this.tupleIDs = new ArrayList<>();
        this.rootTupleIds = new ArrayList<>();
    }
    //对于这部分数组，处理方式是
}
