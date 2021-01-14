package Repair;

import Bean.*;
import Detection.IncDetection;
import IndexOption.IndexReader;
import IndexOption.IndexSet;
import MVC.MinimalCover;
import Util.Determination;
import Util.Tools;

import java.util.*;

public class HolisticRepair {
    private Tools tools;
    private Map<String, Node> CH;
    private Set<HyperEdge> edgesVisited;
    private Set<String> processedCells;
    private List<String> MVC;
    private Map<String,String> tempRepair;
    private Set<String> visitedCells;
    public Map<String,String>  result;
    IndexReader indexReader;

    HolisticRepair(){
        tools = new Tools();
        CH = new HashMap<>();
        processedCells = new HashSet<>();
        edgesVisited = new HashSet<>();
        MVC = new ArrayList<>();
        tempRepair = new HashMap<>();
        result = new HashMap<>();
        visitedCells = new HashSet<>();
        indexReader =  new IndexReader();
    }
    public void repair(IndexSet indexSet, List<Violation> violationList) {
        ConflictHyper conflictHyper = new ConflictHyper();
        conflictHyper.buildCH(violationList,CH,indexSet, new HashSet<>());
        getCHSize();
        repeat(indexSet,violationList);
    }

    private void getCHSize() {
        long res = 0L;
        for(Map.Entry<String,Node> entry:CH.entrySet()){
            res+=indexReader.getObject(entry.getKey());
            res+=getNodeSize(entry.getValue());
        }
        System.out.println(indexReader.printSize(res));
    }

    private long getNodeSize(Node node) {
        long res = 0;
        res+=indexReader.getObject(node.key);
        for(HyperEdge edge:node.edges){
            res+=getEdgeSize(edge);
        }
        return res;
    }

    private long getEdgeSize(HyperEdge edge) {
        long res = 4L;
        for(ConflictNode conflictNode:edge.conflictNodes){
            res+=getConfliNodeSize(conflictNode);
        }
        return res;
    }

    private long getConfliNodeSize(ConflictNode conflictNode) {
        return 8L+indexReader.getObject(conflictNode.confliAttr);
    }


    private void repeat(IndexSet indexSet, List<Violation> violationList) {
        Determination determination = new Determination();
        List<Assignment> assignments = new ArrayList<>();
        processedCells.addAll(this.CH.keySet());
        while(true){
            MinimalCover mvcCalc = new MinimalCover(this.CH.size(),indexSet.fileReaderByParams.originIdList.size(),false);
            PriorityQueue<String> mvc = mvcCalc.priorityMvc(this.MVC,this.CH);
            int sizeBefore = processedCells.size();
            int mvcSize = mvc.size();
            int cnt = 1;
            while(!mvc.isEmpty()){
                if((cnt++)%(mvcSize/100)==0){
                    System.out.println("percent "+(100*cnt/mvcSize));
                }
                String cell = mvc.poll();//出队列 同时删除
//                Iterator<HyperEdge> iterator = this.CH.get(cell).edges.iterator();
                RepairContext rc = new RepairContext(cell);
//                while(iterator.hasNext()){//访问过得边删除，但是如果只是删除的话不达到效果，因为是无向图，
//                    //为什么这个查不出结果
//                    LookUp(indexSet,cell,iterator,rc,0);//rc过大的时候就有问题
//                }
//                System.out.println("once  lookup end");
                assignments  = determination.run(indexSet,CH, cell,rc);//分别更新d和delta d 中的元素
                updateResult(assignments,tempRepair);
                updateResult(assignments,result);
                assignments.clear();
            }
            RepairResult repairResult = new RepairResult();
            repairResult.setResult(new ArrayList<>(tempRepair.entrySet()));
            System.out.println("once repair size:"+tempRepair.size());
//            System.out.println(tempRepair);
            updateData(indexSet,repairResult);
            //利用更新后的d list 和inc list 更新索引 重新检测
            //之后重构冲突超图
            for(int i = 0;i<80;i++) System.out.print("#");
            System.out.println();
            resetGraph(indexSet,violationList);//重建冲突超图
            if(CH.size()==0){
                return ;//修复完成
            }
            processedCells.addAll(CH.keySet());
            int sizeAfter = processedCells.size();
            System.out.println("After repair,Conflict HyperGraph size is:"+CH.size());
            System.out.println("processedCells size before:"+sizeBefore+",after:"+sizeAfter);
            if(sizeBefore>=sizeAfter)break;//新产生的冲突cell都已经产生过一遍时，结束，原论文此处错误
            tempRepair.clear();//如果是最后一次循环，不清除tempRepair里的内容
        }
        //结束时候产生的
        dataPostProcess(indexSet);//更新dataset以计算指标
}

    private void updateResult(List<Assignment> assignments,Map<String,String> repair) {
        if (assignments.size()==0)return;
        for (Assignment assignment:assignments){
            if(!assignment.satisfy)continue;
            if(assignment.val.equals("")) System.out.println("update empty val");;
            repair.put(assignment.cell,assignment.val);
        }
    }
    private void updateData(IndexSet indexSet, RepairResult result) {
        for(Map.Entry<String,String> entry:result.result){
            String cell  = entry.getKey();
            String val = entry.getValue();
//            if(val.equals("")) System.out.println("empty val");
            int tupleId = Integer.parseInt(cell.split(",")[0]);
            String attr = cell.split(",")[1];
            if(tupleId>=indexSet.fileReaderByParams.originIdList.size()){
                //属于增量部分的数据
                tupleId -= indexSet.fileReaderByParams.originIdList.size();
                tools.updateCellVal(indexSet.fileReaderByParams.incList.get(tupleId),attr,val,indexSet.fileReaderByParams.fileParams.table);
            }else{
                tools.updateCellVal(indexSet.fileReaderByParams.originList.get(tupleId),attr,val,indexSet.fileReaderByParams.fileParams.table);
            }
        }
    }

    private void dataPostProcess(IndexSet indexSet) {
       for(String cell: this.MVC){//只考虑未修复过的cell
           if(result.containsKey(cell))continue;
           result.put(cell,"-1");
       }
    }

    private void resetGraph(IndexSet indexSet,List<Violation> violationList) {
        //重新检测
        System.out.println("rebuild conflict graph starting...");
        indexSet.setIndex(indexSet.fileReaderByParams,true,true);
        violationList.clear();
        IncDetection incDetection = new IncDetection();
        incDetection.detect(violationList,indexSet,3,true, false,false);//内部默认使用树状数组
        CH.clear();
        MVC.clear();
        ConflictHyper conflictHyper = new ConflictHyper();
        conflictHyper.buildCH(violationList,CH,indexSet, new HashSet<>());
    }

    private void LookUp(IndexSet indexSet, String cell, Iterator<HyperEdge> iterator,RepairContext rc,int level) {
        /**
         * 这个理解过程真痛苦 看着例子理解比较好 从算法流程中while edges is not empty这句话开始执行一个深搜，对访问过得超边进行删除
         */
        HyperEdge edge = iterator.next();
        if(level>=3)return ;//调用栈层数最大30
        // 迭代的过程会造成栈溢出
//        iterator.remove();
//        System.out.println(edgesVisited.size()+"rc.repairList"+rc.repairExpList.size()+rc.frontier.size()+"conlifct nodes size"+edge.conflictNodes.size());
        if(edgesVisited.contains(edge)){
//            System.out.println(edge);
            return ;
        }
        edgesVisited.add(edge);
        boolean isNumeric = isNumeric(edge);
        if(isNumeric){
            return ;
        }
        //对于cell会生成其相反的修复表达式
        RepairExp repairExp = DenialRepair(indexSet,edge,cell);
        rc.repairExpList.add(repairExp);//过程中涉及的所有修复表达式
        //frontier表示与cell相关的其他cell
        //如 修复t2.c时，包括mvc中的顶点，以及其他相关的顶点t1.c t3.c t4.c都作为frontier里的一员
        //当前修复表达式可能引入的cell作为frontier
//        System.out.println("level:"+level);
//        System.out.println("conflict size:"+edge.conflictNodes.size());
        for(ConflictNode frontier:edge.conflictNodes){
            String key = frontier.confliId+","+frontier.confliAttr;
            if(rc.frontier.contains(key))continue;
            rc.frontier.add(key);
            //冲突超图的cell，获得冲突cell的冲突超边，
            Iterator<HyperEdge> iterator_2= this.CH.get(key).edges.iterator();
//            System.out.println("lookup continue");
            while(iterator_2.hasNext()){
//                System.out.println("lookup continue");
                LookUp(indexSet,key,iterator_2,rc,level+1);//调用一次执行一次iterator.next，
            }
        }
    }

    private boolean isNumeric(HyperEdge edge) {
        int exp = edge.conflictNodes.get(0).operator;
        return exp<6&&exp>1;
    }

    private RepairExp DenialRepair(IndexSet indexSet, HyperEdge edge,String cell) {
        //获得其相反值需要知道原来其冲突的cell
        //暂时不考虑一个超边中包含cell的两个冲突节点
        int confliId = edge.conflictNodes.get(0).confliId;
        String confliAttr = edge.conflictNodes.get(0).confliAttr;
        String confliCell = confliId+","+confliAttr;
        String confliVal  = tools.getValueByCell(indexSet, confliCell,indexSet.fileReaderByParams.fileParams.table);
        RepairExp repairExp = new RepairExp(tools.getReverseOperator(edge.conflictNodes.get(0).operator),confliVal,cell,confliCell);
        return repairExp;
    }
}
