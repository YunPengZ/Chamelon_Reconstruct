package Bean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


//�ڽӱ��ʵ��
//���ж�����һά�ı��ʾ��������hashmap
//һ���������ӵ������бߵ����нڵ�
public class HyperEdge {
    public int vioId;
    public List<ConflictNode> conflictNodes;
    //String��ʾ�ڵ���hashmap�е�keyֵ
    public HyperEdge(Collection<ConflictNode> conflictNodes,int vioId) {
        this.conflictNodes = new ArrayList<>(conflictNodes);
        this.vioId = vioId;
    }
}
