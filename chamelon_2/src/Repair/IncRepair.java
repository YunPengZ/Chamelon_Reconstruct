package Repair;

import Bean.Assignment;
import Bean.Node;
import Bean.Violation;
import IndexOption.IndexSet;
import MVC.MinimalCover;
import Util.Determination;
import Util.Tools;

import java.util.*;

public class IncRepair {
    public List<Map.Entry<String, String>> result;
    private Map<String, Node> CH;
    private List<String> MVC;
    private Map<String,String> tempResult;
    private Tools tools;

    IncRepair(){
        result = new ArrayList<>();
        CH = new HashMap<>();
        tempResult = new HashMap<>();
        MVC = new ArrayList<>();
        tools = new Tools();
    }

    public void repair(IndexSet indexSet, List<Violation> violationList) {
        List<Assignment> assignments = new ArrayList<>();
        ConflictHyper conflictHyper = new ConflictHyper();
        conflictHyper.buildCH(violationList,CH,indexSet, new HashSet<>());
        incRepair(indexSet,violationList,assignments);
    }

    private void incRepair(IndexSet indexSet, List<Violation> violationList, List<Assignment> assignments) {
        Long start = System.currentTimeMillis();
        MinimalCover mvcCalc = new MinimalCover(this.CH.size(),indexSet.fileReaderByParams.originIdList.size(),true);
        //mvc只包含inc中的cell
        List<String>orderedCell = new ArrayList<>();
        PriorityQueue<String> mvc = mvcCalc.priorityMvc(MVC,CH);
        Map<String,RepairBound> repairBoundInOrigin = new HashMap<String, RepairBound>();
        Map<String,RepairBound> repairBoundInDelta = new HashMap<String, RepairBound>();
        Map<String,RepairBound> repairBoundBySuspect = new HashMap<String, RepairBound>();
        Map<String,RepairBound> repairBoundByDeltaSuspect = new HashMap<String, RepairBound>();
        while (!mvc.isEmpty()){
            String cell = mvc.poll();
            orderedCell.add(cell);
        }
        VFreeRepair vFreeRepair = new VFreeRepair();
        for(String cell:orderedCell){
            //分别获得cell在d中的修复上下文和在delta d 中的修复上下文以及suspect s
            //suspect需要
            repairBoundInOrigin.put(cell,vFreeRepair.getSuspectByCH(indexSet,CH,cell,false,true,false));
            repairBoundInDelta.put(cell,vFreeRepair.getSuspectByCH(indexSet,CH,cell,false,false,true));
        }
        vFreeRepair.calcSuspect(indexSet,violationList,CH,orderedCell,repairBoundBySuspect,true,false);
        vFreeRepair.calcSuspect(indexSet,violationList,CH,orderedCell,repairBoundByDeltaSuspect,false,true);
        //获得suspect的冲突需要重构冲突超图
        for(String cell:orderedCell){
            //不满足suspect 直接跳过
            if(!satisfy(repairBoundBySuspect.get(cell)))continue;
            if(!satisfy(repairBoundInOrigin.get(cell)))continue;
            //d中的、/
//            System.out.println(repairBoundBySuspect.get(cell)+"In origin:"+repairBoundInOrigin.get(cell));
            RepairBound repairBoundInOriginAndSuspect = vFreeRepair.MergeTwoBound(repairBoundInOrigin.get(cell),repairBoundBySuspect.get(cell));
            if(!satisfy(repairBoundInOriginAndSuspect))continue;
            RepairBound repairBoundDeltaWithSuspect = vFreeRepair.MergeTwoBound(repairBoundByDeltaSuspect.get(cell),repairBoundInDelta.get(cell));
            RepairBound repairBoundWithDelta = vFreeRepair.MergeTwoBound(repairBoundInOriginAndSuspect,repairBoundDeltaWithSuspect);
            determination(indexSet,assignments,cell,repairBoundInOriginAndSuspect,repairBoundWithDelta);
        }
        result = vFreeRepair.postProcess(assignments,orderedCell,tempResult);
        Long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"inc repair");
    }

    private void determination(IndexSet indexSet, List<Assignment> assignments, String cell, RepairBound repairBoundInOriginAndSuspect, RepairBound repairBoundWithDelta) {
        String curVal = tools.getValueByCell(indexSet, cell,indexSet.fileReaderByParams.fileParams.table);
        Determination determination = new Determination();
        if(!repairBoundInOriginAndSuspect.isNumeric){
            assignments.add(new Assignment(cell,repairBoundInOriginAndSuspect.val,true));
        }else{
            if(satisfy(repairBoundWithDelta)){
                determination.getRepairByBound(indexSet,assignments,cell,repairBoundWithDelta.lowBound,repairBoundWithDelta.uppBound,curVal);
            }else{
                determination.getRepairByBound(indexSet,assignments,cell,repairBoundInOriginAndSuspect.lowBound,repairBoundInOriginAndSuspect.uppBound,curVal);
            }
        }

    }

    private boolean satisfy(RepairBound repairBound) {
        if(repairBound.isNumeric){
            return repairBound.uppBound>=repairBound.lowBound;
        }else{
            return !repairBound.val.equals("-1");//是否等于-1,不等于-1是对
        }
    }
}
