package Repair;

import Bean.*;
import Detection.IncDetection;
import IndexOption.IndexSet;
import MVC.MinimalCover;
import Util.Determination;
import Util.Tools;

import java.util.*;

public class VFreeRepair {
    private Map<String, Node> CH;
    private List<String> MVC;
    private Map<String,String> tempResult;
    private Set<HyperEdge> edgesVisited;
    public List<Map.Entry<String,String>> result;
    Tools tools ;


    public VFreeRepair(){
        tools = new Tools();
        CH = new HashMap<>();
        MVC = new ArrayList<>();
        tempResult = new HashMap<>();
        edgesVisited = new HashSet<>();
    }

    public void repair(IndexSet indexSet, List<Violation> violationList) {
        //前方也是通过冲突图构建MVC，但是在构建rc时候，引入suspect
        List<Assignment> assignments = new ArrayList<>();
        ConflictHyper conflictHyper = new ConflictHyper();
        conflictHyper.buildCH(violationList,CH,indexSet, new HashSet<>());
        singleRepair(indexSet,violationList,assignments);
    }

    private void singleRepair(IndexSet indexSet, List<Violation> violationList, List<Assignment> assignments) {
        MinimalCover mvcCalc = new MinimalCover(this.CH.size(),indexSet.fileReaderByParams.originIdList.size(),false);
        List<String>orderedCell = new ArrayList<>();
        PriorityQueue<String> mvc = mvcCalc.priorityMvc(MVC,CH);
        Map<String, RepairBound>  repairWithOutSuspect = new HashMap<String, RepairBound>();
        Map<String,RepairBound> repairOnlySuspect = new HashMap<String, RepairBound>();
        Long start = System.currentTimeMillis();
        while (!mvc.isEmpty()){
            String cell = mvc.poll();
            orderedCell.add(cell);
        }
        //对mvc中的cell涉及到的id进行记录
        for(String cell:orderedCell){
            repairWithOutSuspect.put(cell,getSuspectByCH(indexSet,CH,cell,false, false, false));
        }
        Long boundEnd = System.currentTimeMillis();
        tools.printRunTime(start,boundEnd,"getBoundWithOutSuspect ");
        calcSuspect(indexSet,violationList, CH, orderedCell,repairOnlySuspect,false, false);
        int i = 0;
        for(String cell:orderedCell){
            if((i++)*100%orderedCell.size()==0){
                System.out.println("process suspect percentile "+i*100/orderedCell.size());
            }
            updateTempResult(indexSet,repairWithOutSuspect,repairOnlySuspect,tempResult,assignments,cell);
        }
        result = postProcess(assignments,orderedCell,tempResult);
        //添加suspect，之后还是通过已有的方法处理
        Long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"vfree repair ");
    }

    public List<Map.Entry<String, String>> postProcess(List<Assignment> assignments, List<String> mvc, Map<String, String> tempResult) {
        for(Assignment assignment:assignments){
            if (assignment.satisfy){
                tempResult.put(assignment.cell,assignment.val);
            }
        }
        for (String cell:mvc){
            if(tempResult.containsKey(cell))continue;
            tempResult.put(cell,"-1");
        }
        List<Map.Entry<String, String>>  result = new ArrayList<>(tempResult.entrySet());
        return result;
    }

    public void updateTempResult(IndexSet indexSet, Map<String, RepairBound> repairWithOutSuspect, Map<String, RepairBound> repairOnlySuspect, Map<String, String> tempResult, List<Assignment> assignments, String cell) {
        String curVal = tools.getValueByCell(indexSet, cell,indexSet.fileReaderByParams.fileParams.table);
        //统一suspect与原始的修复
        RepairBound repairBound = MergeTwoBound(repairOnlySuspect.get(cell),repairWithOutSuspect.get(cell));
        if(repairBound.isNumeric){
            Determination determination = new Determination();
            determination.getRepairByBound(indexSet,assignments,cell,repairBound.lowBound,repairBound.uppBound,curVal);
        }else{
            tempResult.put(cell,repairBound.val);
        }
    }

    public RepairBound MergeTwoBound(RepairBound suspect, RepairBound withOutSuspect) {
        int withOutSuspectUpper =Integer.MAX_VALUE;
        int withOutSuspectLower = Integer.MIN_VALUE;
        int suspectUpper = Integer.MAX_VALUE;
        int suspectLower = Integer.MIN_VALUE;
        if(suspect.isNumeric){
            suspectLower = Math.max(suspectLower,suspect.lowBound);
            suspectUpper = Math.min(suspectUpper,suspect.uppBound);
        }
        if(withOutSuspect.isNumeric){
            withOutSuspectLower = Math.max(withOutSuspectLower,withOutSuspect.lowBound);
            withOutSuspectUpper = Math.min(withOutSuspectUpper,withOutSuspect.uppBound);
        }
        boolean isNumeric = suspect.isNumeric||withOutSuspect.isNumeric;
        RepairBound result = new RepairBound(isNumeric);
        if(isNumeric){
            result.uppBound = Math.min(suspectUpper,withOutSuspectUpper);
            result.lowBound = Math.max(suspectLower,withOutSuspectLower);
        }else{
            result.val = suspect.val.equals(withOutSuspect.val)?suspect.val:"-1";
        }
        return result;
    }

    public void calcSuspect(IndexSet indexSet, List<Violation> violationList, Map<String, Node> CH, List<String> orderedCell, Map<String, RepairBound> repairOnlySuspect, boolean onlyOrigin, boolean onlyDelta) {
        Map<String,Set<String>> attrCellsList = new HashMap<>();
        for(String cell:orderedCell){
            String attr = cell.split(",")[1];
            if(attrCellsList.containsKey(attr)){
                attrCellsList.get(attr).add(cell);
            }else{
                Set<String> temp = new HashSet<>();
                temp.add(cell);
                attrCellsList.put(attr,temp);//某个属性值涉及到哪些cells
            }
        }
        for(Map.Entry<String,Set<String>>entry:attrCellsList.entrySet()){
            setReverseDCAndCH(indexSet,violationList,CH,entry.getValue(),entry.getKey());
            for(String cell:entry.getValue()){
                repairOnlySuspect.put(cell,getSuspectByCH(indexSet, CH, cell,true, onlyOrigin, onlyDelta));
            }
        }
    }


//    private void LookUpWithSuspect(IndexSet indexSet, String cell, Iterator<HyperEdge> iterator, RepairContext rc, int level) {
//        //获得相关的修复上下文
//        if(!iterator.hasNext())return ;
//        if(level>=30)return ;
//        // 迭代的过程会造成栈溢出
//        HyperEdge edge = iterator.next();
//        if(edgesVisited.contains(edge)){
//            return ;
//        }
//        edgesVisited.add(edge);
//        //对于cell会生成其相反的修复表达式
//        RepairExp repairExp = DenialRepair(indexSet,edge,cell);
//        rc.repairExpList.add(repairExp);//过程中涉及的所有修复表达式
//        //对于每一个cell都获得其suspect，但是这个suspect涉及的cell不添加到frontier
//        //suspect不添加cell，只添加值到修复表达式中,相当于对于cell设置一个上下界
//        Suspect suspect=  getSuspectByCell(indexSet,cell);
//        for(ConflictNode frontier:edge.conflictNodes){
//            String key = frontier.confliId+","+frontier.confliAttr;
//            if(rc.frontier.contains(key))continue;
//            rc.frontier.add(key);
//            //冲突超图的cell，获得冲突cell的冲突超边，
//            Iterator<HyperEdge> iterator_2= this.CH.get(key).edges.iterator();
//            while(iterator_2.hasNext()){
//                LookUpWithSuspect(indexSet,key,iterator_2,rc,level+1);//调用一次执行一次iterator.next，
//            }
//        }
//    }

    public RepairBound getSuspectByCH(IndexSet indexSet, Map<String, Node> CH, String cell, boolean isSuspect, boolean onlyOrigin, boolean onlyDelta) {
        RepairBound repairBound = new RepairBound(false);
        String table = indexSet.fileReaderByParams.fileParams.table;
        String val = "-1";
        int cnt = 0;int lowwerBound  = Integer.MIN_VALUE;int upperBound = Integer.MAX_VALUE;
        boolean isNumeric = false;
        int dSz = indexSet.fileReaderByParams.originIdList.size();
        if(!CH.containsKey(cell)){
            repairBound.val = "-1";
            return repairBound;
        }
        for(HyperEdge edge: CH.get(cell).edges){
            int confliId = edge.conflictNodes.get(0).confliId;
            if(onlyOrigin&&confliId>=dSz)continue;
            if(onlyDelta&&confliId<dSz)continue;
            String curCell = confliId+","+edge.conflictNodes.get(0).confliAttr;
            String curVal = tools.getValueByCell(indexSet, curCell,table);
            int operator = edge.conflictNodes.get(0).operator;
            if(isSuspect)operator = 7-operator;//suspect的话获得suspect需要对操作符取反
            if (operator==6){
                repairBound.val = "-1";
                break;
            }else if(operator==1){//原来为！=,常规修复为=,而suspect情况下，仍然修复为!=
                if(val.equals(curVal))cnt++;
                else{
                    if(cnt==0){
                        val = curVal;
                    }else cnt--;
                }//最后剩下的就是出现次数较多的那个
            }else{
                if(operator==2||operator==3){
                    //suspecet:cell.X>conflictCell.X--->suspect修复位cell.X>conflictCell.X
                    //常规情况修复为 cell.X<conflicell.X
                    upperBound = Math.min(upperBound,Integer.parseInt(curVal));//修复为
                }else{
                    lowwerBound = Math.max(lowwerBound,Integer.parseInt(curVal));
                }
                isNumeric = true;
            }
        }
        if(!isNumeric)repairBound.val= val;
        else{
            repairBound.isNumeric = true;
            repairBound.lowBound = lowwerBound;
            repairBound.uppBound = upperBound;
        }
        return repairBound;
    }
    private void setReverseDCAndCH(IndexSet indexSet, List<Violation> violationList, Map<String, Node> CH, Set<String> cells, String attr) {
        CH.clear();//此次构建的冲突超图清空
        List<String> temp = indexSet.fileReaderByParams.dcs;
        reDetectionByAttr(indexSet,violationList,attr);
        //构建冲突图，获得某个cell的冲突超边
        ConflictHyper conflictHyper = new ConflictHyper();
        conflictHyper.buildCH(violationList, CH,indexSet,cells);
        indexSet.fileReaderByParams.dcs = temp;//更新回原有的dc，避免影响下一个使用
        indexSet.fileReaderByParams.buildDCMap();
    }

    private void reDetectionByAttr(IndexSet indexSet, List<Violation> violationList, String attr) {
        IncDetection detection = new IncDetection();
        List<String> reverseDC = new ArrayList<>();
        for(String dc:indexSet.fileReaderByParams.dcs){
            if(dc.contains(attr)){
                //dc包含属性，下一步
                for(String atom:dc.split("&")){
                    if(atom.contains(attr)){
                        reverseDC.add(tools.concatDCWithReverse(dc,atom));
                        break;
                    }
                }
            }
        }
        indexSet.fileReaderByParams.dcs = reverseDC;//只使用相关的dc，不包含的属性不使用，只使用暴力的算法，
        indexSet.fileReaderByParams.buildDCMap();//更新DCMap;
        violationList.clear();
        detection.detect(violationList,indexSet,3,true,false,false);//根据冲突计算得到cell的suspect
    }

    private RepairExp DenialRepair(IndexSet indexSet, HyperEdge edge, String cell) {
        String confliAttr = edge.conflictNodes.get(0).confliAttr;
        int confliId = edge.conflictNodes.get(0).confliId;
        String confliCell = confliId+","+confliAttr;
        String confliVal = tools.getValueByCell(indexSet, confliCell,indexSet.fileReaderByParams.fileParams.table);
        return new RepairExp(tools.getReverseOperator(edge.conflictNodes.get(0).operator),confliVal,cell,confliCell);
    }
}
