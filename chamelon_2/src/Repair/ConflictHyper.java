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
     * 构建冲突超图 start
     */
//    private void buildCH_2(List<Violation> violationList, List<String> dcs) {
//        Set<String> nodes = new HashSet<>();
//        List<String> temp = new ArrayList<>();
//        for (Violation violation:violationList){//通过这种方式避免同一个冲突中涉及到的两个cell是相同的
//            buildNodeSet(nodes,violation,dcs.get(violation.getDc()));
//            temp = new ArrayList<>(nodes);
//            for(String node:temp){
//               buildNode(nodes,violation,node);
//            }
//            nodes.clear();
//            temp.clear();
//        }
//    }
    //对于t1.a>t2.b&t1.a>t2.c这种类型的dc 暂不考虑吧
    public void buildCH(List<Violation> violationList, Map<String, Node> CH, IndexSet indexSet, Set<String> cells) {

        Long start = System.currentTimeMillis();
        Map<String,List<ConflictNode>> conflictNodes = new HashMap<>();//避免频繁创建对象
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
//            if(gapSec>5){//gc占据大量时间 内存处在崩溃边缘 返回当前的冲突超图 不再继续计算
////                CH.clear();
//                return;
//            }
            Violation violation = iterator.next();
            if(((i++)*100)%totalSz==0){
                System.out.println("percentile "+i*100/totalSz);
            }
            //需要记录具体冲突的cell
            for(String atom:indexSet.fileReaderByParams.dcs.get(violation.getDc()).split("&")){
                String firstAttr = tools.getAttrByPre(atom,1);
                String secondAttr = tools.getAttrByPre(atom,2);
                if(!cellsAttr.equals("")&&!firstAttr.equals(cellsAttr)&&!secondAttr.equals(cellsAttr)){
                    //如果冲突里的cell和要构建冲突超图的属性没有关系，跳过
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
                    //同一个dc 不考虑一个cell与多个cell冲突
                    continue;
//                    conflictNodes.get(rightKey).add(new ConflictNode(violation.getFirst(),firstAttr,reverseOperator));
                }else {
                    List<ConflictNode> conflictNodeList = new ArrayList<>();
                    conflictNodeList.add(new ConflictNode(violation.getFirst(),firstAttr,reverseOperator));
                    conflictNodes.put(rightKey,conflictNodeList);
                }
            }
            for(String key:conflictNodes.keySet()){
                if(cells==null||cells.size()==0||cells.contains(key))//cell未指定或者key==指定的cell
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
     * 构建冲突超图 end
     */
}
