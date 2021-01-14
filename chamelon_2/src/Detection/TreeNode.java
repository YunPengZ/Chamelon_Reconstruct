package Detection;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    public int index;//在La中的下标
    public int tupleId;//
    List<Integer> ids;
    TreeNode(int index,int tupleId){
        this.index = index;
        this.tupleId = tupleId;
        this.ids = new ArrayList<>();
    }
}
