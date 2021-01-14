package Repair;

import Bean.ConflictNode;
import Bean.Node;
import Bean.Violation;
import IndexOption.IndexSet;
import Util.Tools;

import java.util.*;

public class ConflictHyper {
    Tools tools = new Tools();
    /**
     * ������ͻ��ͼ start
     */
//    private void buildCH_2(List<Violation> violationList, List<String> dcs) {
//        Set<String> nodes = new HashSet<>();
//        List<String> temp = new ArrayList<>();
//        for (Violation violation:violationList){//ͨ�����ַ�ʽ����ͬһ����ͻ���漰��������cell����ͬ��
//            buildNodeSet(nodes,violation,dcs.get(violation.getDc()));
//            temp = new ArrayList<>(nodes);
//            for(String node:temp){
//               buildNode(nodes,violation,node);
//            }
//            nodes.clear();
//            temp.clear();
//        }
//    }
    //����t1.a>t2.b&t1.a>t2.c�������͵�dc �ݲ����ǰ�
    public void buildCH(List<Violation> violationList, Map<String, Node> CH, IndexSet indexSet, Set<String> cells) {

        Long start = System.currentTimeMillis();
        Map<String,List<ConflictNode>> conflictNodes = new HashMap<>();//����Ƶ����������
        String table = indexSet.fileReaderByParams.fileParams.table;
//        if(violationList.size()>1e7){
//            System.err.println("violations size large than 10 million,can not build CH,process terminal");
//            return ;
//        }
        String cellsAttr = "";
        if(!cells.isEmpty()){
            for(String cell:cells){
                cellsAttr = cell.split(",")[1];
                break;
            }
        }
        Iterator<Violation> iterator = violationList.iterator();
        int i = 0;
        int totalSz = violationList.size();
        long lastTime = System.currentTimeMillis();
        while (iterator.hasNext()){
            long curTime = System.currentTimeMillis();
            long gapSec = (curTime-lastTime)/1000;
//            if(gapSec>5){//gcռ�ݴ���ʱ�� �ڴ洦�ڱ�����Ե ���ص�ǰ�ĳ�ͻ��ͼ ���ټ�������
////                CH.clear();
//                return;
//            }
            Violation violation = iterator.next();
            if(((i++)*100)%totalSz==0){
                System.out.println("percentile "+i*100/totalSz);
            }
            //��Ҫ��¼�����ͻ��cell
            for(String atom:indexSet.fileReaderByParams.dcs.get(violation.getDc()).split("&")){
                String firstAttr = tools.getAttrByPre(atom,1);
                String secondAttr = tools.getAttrByPre(atom,2);
                if(!cellsAttr.equals("")&&!firstAttr.equals(cellsAttr)&&!secondAttr.equals(cellsAttr)){
                    //�����ͻ���cell��Ҫ������ͻ��ͼ������û�й�ϵ������
                    continue;
                }
                String leftKey = violation.getFirst()+","+firstAttr;
                String rightKey = violation.getSecond()+","+secondAttr;
                int operator = tools.getOperator(atom);
                String leftVal = tools.getValueByCell(indexSet,leftKey,table);
                String rightVal = tools.getValueByCell(indexSet, rightKey,table);
                if(leftVal.equals("-1")||rightVal.equals("-1"))continue;
                if(conflictNodes.containsKey(leftKey)){
                    continue;
//                    conflictNodes.get(leftKey).add(new ConflictNode(violation.getSecond(),secondAttr,operator));
                }else {
                    List<ConflictNode> conflictNodeList = new ArrayList<>();
                    conflictNodeList.add(new ConflictNode(violation.getSecond(),secondAttr,operator));
                    conflictNodes.put(leftKey,conflictNodeList);
                }
                int reverseOperator = tools.getReverseOperator(operator);
                if(operator ==1||operator==6) reverseOperator = operator;
                if(conflictNodes.containsKey(rightKey)){
                    //ͬһ��dc ������һ��cell����cell��ͻ
                    continue;
//                    conflictNodes.get(rightKey).add(new ConflictNode(violation.getFirst(),firstAttr,reverseOperator));
                }else {
                    List<ConflictNode> conflictNodeList = new ArrayList<>();
                    conflictNodeList.add(new ConflictNode(violation.getFirst(),firstAttr,reverseOperator));
                    conflictNodes.put(rightKey,conflictNodeList);
                }
            }
            for(String key:conflictNodes.keySet()){
                if(cells==null||cells.size()==0||cells.contains(key))//cellδָ������key==ָ����cell
                    buildNode(conflictNodes.get(key),CH,violation.getId(),key);
            }
            conflictNodes.clear();
//            iterator.remove();
            lastTime = curTime;
        }
        Long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"build ConflictHyper");
    }
    private void buildNode(List<ConflictNode> conflictNodes,Map<String, Node> CH, int vioId, String key) {
        if(!CH.containsKey(key)){
            Node node = new Node(key);
            node.addEdge(conflictNodes,vioId);
            CH.put(key,node);
        }else{
            CH.get(key).addEdge(conflictNodes,vioId);
        }
    }
    /**
     * ������ͻ��ͼ end
     */
}
