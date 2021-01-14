package MVC;

import Bean.ConflictNode;
import Bean.HyperEdge;
import Bean.Node;
import Util.Tools;

import java.util.*;

public class MinimalCover {
    // ref:https://blog.csdn.net/did_you/article/details/79038061
    Set<String> del,visited;
    Map<String,Point> points;
    List<Point> sortedPoints;
    Tools tools = new Tools();
    int v_cnt,Vv_cnt,e_cnt,dSz;
    boolean onlyDelta;//����mvcʱ���Ƿ�ֻ����inc�еĶ���
    public MinimalCover(int sz,int dSz,boolean onlyDelta){
        del = new HashSet<>(sz);
        visited = new HashSet<>(sz);
        points = new HashMap<>(sz);
        this.dSz = dSz;
        this.onlyDelta = onlyDelta;
    }
    public PriorityQueue<String> priorityMvc(List<String> MVC,Map<String, Node> CH){
        calcMVC(MVC,CH);
        PriorityQueue<String> mvc = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                //��������
                return CH.get(s2).edges.size()-CH.get(s1).edges.size();
            }
        });//�Զ���Ƚ���
        for(String cell:MVC){
            mvc.add(cell);
        }
        return mvc;
    }
    public void calcMVC(List<String> MVC, Map<String, Node> CH) {
        Long start = System.currentTimeMillis();
        pretreat(CH);
        MinVC_MGA(CH,MVC);
        //�����޸��㷨ʱ����С���㸲��ʱ��ֻ���ǵ�����Ԫ�ؼ���
        Long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"calc mvc ");
        System.out.println("mvc size "+MVC.size());
    }

    private void MinVC_MGA(Map<String, Node> CH, List<String> MVC) {
        for(Point point:sortedPoints){
            if(!del.contains(point.cell))Vv_cnt++;
        }
        for(Point point:sortedPoints){
            if(del.contains(point.cell))continue;
            if(visited.contains(point.cell))continue;
            if(onlyDelta&&tools.isOrigin(point.cell,dSz))continue;//����һ������d�е����ݹ���
            MVC.add(point.cell);
            del.add(point.cell);
            Vv_cnt--;
            for(HyperEdge hyperEdge:CH.get(point.cell).edges){
                String confliCell = getCellByConfliNodes(hyperEdge.conflictNodes.get(0));
                if(del.contains(confliCell)){
                    continue;
                }
                e_cnt++;
                if(!visited.contains(confliCell)){
                    visited.add(confliCell);
                    v_cnt++;
                }
            }//�ѷ��ʹ���δ��ɾ���ĵ�>=ʣ���δ��ɾ���ĵ�
            if(v_cnt>=Vv_cnt)break;//��ͼ�еĵ�ȫ������
        }
    }

    private void pretreat(Map<String, Node> CH) {//Ԥ��������ÿ������ĶȺ��ڽӶ���
        for(String key:CH.keySet()){
            Point point = new Point(key);
            if(del.contains(key))continue;
            for(HyperEdge hyperEdge:CH.get(key).edges){
                if(!del.contains(getCellByConfliNodes(hyperEdge.conflictNodes.get(0))))point.degree++;
            }
            if(point.degree==0)del.add(key);
            points.put(key,point);
        }
        for(String key:CH.keySet()){
            if(del.contains(key)){
                points.get(key).adj_degree=0;
                continue;
            }
            points.get(key).adj_degree = points.get(key).degree;
            for(HyperEdge hyperEdge:CH.get(key).edges){
                points.get(key).adj_degree+=points.get(getCellByConfliNodes(hyperEdge.conflictNodes.get(0))).degree;
            }
        }
        sortedPoints = new ArrayList<>(points.values());
        sortedPoints.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o2.adj_degree-o1.adj_degree;
            }
        });
    }

    private String getCellByConfliNodes(ConflictNode conflictNode) {
        int confliId = conflictNode.confliId;
        String confliAttr = conflictNode.confliAttr;
        String confliCell = confliId+","+confliAttr;
        return confliCell;
    }


}
