package Bean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


//邻接表的实现
//所有顶点用一维的表表示，可以用hashmap
//一个顶点链接到与其有边的所有节点
public class HyperEdge {
    public int vioId;
    public List<ConflictNode> conflictNodes;
    //String表示节点在hashmap中的key值
    public HyperEdge(Collection<ConflictNode> conflictNodes,int vioId) {
        this.conflictNodes = new ArrayList<>(conflictNodes);
        this.vioId = vioId;
    }
}
