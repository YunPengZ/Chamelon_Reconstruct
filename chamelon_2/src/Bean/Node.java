package Bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Node {
    public String key;
    public List<HyperEdge> edges;

    public Node(String key) {
        this.key = key;
        this.edges = new ArrayList<>();
    }
    public void addEdge(Collection<ConflictNode> confliNodes,int vioId){
        this.edges.add(new HyperEdge(confliNodes,vioId));
    }
}
