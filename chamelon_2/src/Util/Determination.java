package Util;

        import Bean.*;
        import IndexOption.IndexSet;
        import Repair.RepairBound;
        import Repair.VFreeRepair;
        import javafx.util.Pair;

        import java.util.*;

public class Determination {
    Tools tools;
    public Determination(){
        tools = new Tools();
    }

    public boolean isNumeric(int exp) {
        if(exp<6&&exp>1)return true;
        return false;
    }
    public List<Assignment> run(IndexSet indexSet, Map<String, Node> CH, String cell, RepairContext rc) {
//        long start = System.currentTimeMillis();
//        boolean isNumeric = false;
        List<Assignment> assignments;
//        for(RepairExp exp:rc.repairExpList){
//            isNumeric = isNumeric(exp.exp);
//            if(isNumeric)break;
//        }
//        if(isNumeric){
            assignments = getQPAssignments_2(indexSet,CH,rc,cell);
//        }else assignments = getVFMAssignments(rc,indexSet.countMap,cell);
//        long end = System.currentTimeMillis();
//        long gap = end-start;
        return assignments;
    }

    private List<Assignment> getQPAssignments_2(IndexSet indexSet, Map<String, Node> CH, RepairContext rc, String cell) {
        List<Assignment> assignments = new ArrayList<>();
        VFreeRepair vFreeRepair = new VFreeRepair();
        String curVal = tools.getValueByCell(indexSet,cell,indexSet.fileReaderByParams.fileParams.table);
        RepairBound repairBound = vFreeRepair.getSuspectByCH(indexSet,CH,cell,false,false,false);
        if(repairBound.isNumeric)
            getRepairByBound(indexSet,assignments,cell,repairBound.lowBound,repairBound.uppBound,curVal);
        else
            assignments.add(new Assignment(cell,repairBound.val,true));
        return  assignments;
    }

    private List<Assignment> getSelfAssignments(IndexSet indexSet, Map<String, Node> CH, RepairContext rc, String cell) {
        //算法的思路是只对rc中少量的cell做出修复，对于frontier中的cell排序，只修复顺序考前的
        List<Assignment> assignments = new ArrayList<>();
        PriorityQueue<String> mvc = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                //降序排列
                return CH.get(s2).edges.size()-CH.get(s1).edges.size();
            }
        });//自定义比较器
        int cnt = 0;
        while(!mvc.isEmpty()){
            String curCell = mvc.poll();
            String curVal = tools.getValueByCell(indexSet, curCell,indexSet.fileReaderByParams.fileParams.table);
            String attr = curCell.split(",")[1];
            if(cnt++>3)break;
            int upperBound = Integer.MAX_VALUE;
            int lowerBound = Integer.MIN_VALUE;
            for(HyperEdge edge:CH.get(curCell).edges){
                String confliCell = edge.conflictNodes.get(0).confliId+","+edge.conflictNodes.get(0).confliAttr;
                String confliVal = tools.getValueByCell(indexSet, confliCell,indexSet.fileReaderByParams.fileParams.table);//获得冲突元组的值
                int operator = edge.conflictNodes.get(0).operator;
                if(operator==2||operator==3){
                    //val>confliVal,修复为val<confliVal
                    upperBound = Math.min(upperBound,Integer.parseInt(confliVal));
                }else if(operator==4||operator==5){
                    lowerBound = Math.max(lowerBound,Integer.parseInt(confliVal));
                }
            }
            //根据上下界在已有值里选择最靠近的值
            getRepairByBound(indexSet,assignments,cell,lowerBound,upperBound,curVal);
        }
        return  assignments;
    }

    public void getRepairByBound(IndexSet indexSet, List<Assignment> assignments, String cell, int lowerBound, int upperBound, String curVal) {
//        int cellTupleId = Integer.parseInt(cell.split(",")[0]);
        String attr = cell.split(",")[1];
        int curValInt = Integer.parseInt(curVal);
        int tupleId = -1;
        int closestVal = -1;
        int closestGap = Integer.MAX_VALUE;
        initSortedList(indexSet,attr);
//        int dSz = indexSet.fileReaderByParams.originIdList.size();
//        boolean isOrigin = false;
//        if(cellTupleId>=dSz)cellTupleId-=dSz;
//        else isOrigin = true;
//        System.out.println("true upper bound"+upperBound+"low"+lowerBound+"curVal"+curVal);
//        String cleanVal = tools.getValueByAttr(indexSet.fileReaderByParams.cleanList.get(cellTupleId),attr,indexSet.fileReaderByParams.fileParams.table);
//        String dirtyVal = tools.getValueByAttr(indexSet.fileReaderByParams.dirtyList.get(cellTupleId),attr,indexSet.fileReaderByParams.fileParams.table);
//            if(!cleanVal.equals(dirtyVal))
        //从区间里选择离之前值最近的值
        if(lowerBound>=curValInt){//第一个大于等于lowerBound的位置,
            int closet2LowerIndex = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attr),attr,String.valueOf(lowerBound),indexSet.fileReaderByParams.fileParams.table);
            closet2LowerIndex = checkBound(indexSet,closet2LowerIndex);
            tupleId = indexSet.sorted_list.get(attr).get(closet2LowerIndex);
            String val = tools.getValueByAttr(indexSet.fileReaderByParams.originList.get(tupleId),attr,indexSet.fileReaderByParams.fileParams.table);
            int valInt = Integer.parseInt(val);
            if(Math.abs(valInt-curValInt)<closestGap){
                closestGap = Math.abs(valInt-curValInt);
                closestVal = valInt;
            }
        }
        if(upperBound<=Integer.parseInt(curVal)){//第一个大于upperBound
            int close2UpperIndex = tools.upperBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attr),attr,String.valueOf(upperBound),indexSet.fileReaderByParams.fileParams.table);
            if (close2UpperIndex==0)close2UpperIndex=1;
            tupleId = indexSet.sorted_list.get(attr).get(close2UpperIndex-1);
            String val = tools.getValueByAttr(indexSet.fileReaderByParams.originList.get(tupleId),attr,indexSet.fileReaderByParams.fileParams.table);
            int valInt = Integer.parseInt(val);
            if(Math.abs(valInt-curValInt)<closestGap){
                closestVal = valInt;
            }
        }
//        System.out.println("tupleId"+tupleId);
        if(tupleId!=-1){
//            System.out.println("lower"+lowerBound+"upper:"+upperBound+"curVal"+curVal+"repair val"+closestVal+"cleanVal"+cleanVal+"dirtyVal"+dirtyVal);
            assignments.add(new Assignment(cell,String.valueOf(closestVal),true));
        }else{
            assignments.add(new Assignment(cell,"-1",false));
        }
    }

    private int checkBound(IndexSet indexSet, int index) {
        if(index>=indexSet.fileReaderByParams.originIdList.size())
            return indexSet.fileReaderByParams.originIdList.size()-1;
        if(index<0)return 0;
        return index;
    }

    private List<Assignment> getVFMAssignments(RepairContext rc, Map<String, Map<String, Integer>> countMap,String cell) {
        //修复表达式为!=的情况都是从 = 转变过来的，不修复
        String attr = cell.split(",")[1];//获得属性
        //如果repair exp冲突 直接结束
        boolean satisfy = true;
        String val = "";
        if(satisfy){
            //连等的值，选择出现次数最多的。
            int max_cnt= 0 ;
            for(RepairExp repairExp:rc.repairExpList){
                if(repairExp.exp==6){
                    int temp = countMap.get(attr).getOrDefault(repairExp.val,0);
                    if(temp>max_cnt){
                        max_cnt = temp;
                        val = repairExp.val;
                    }
                }
            }
        }
        List<Assignment> assignments = new ArrayList<>();
        for(String frontier:rc.frontier){
            assignments.add(new Assignment(frontier,val,satisfy&&!val.equals("")));
        }
        return assignments;
    }

    //cardinality
    private List<Assignment> getQPAssignments(IndexSet indexSet, RepairContext rc) {
        //todo
        //逐个增加修改的cell,直到满足条件
        //https://www.gurobi.com/documentation/8.0/refman/java_api_overview.html#sec:Java
        //以小于为方向构建有向图，有向图无环，表明修复表达式可以被满足
        Map<String,List<String>> graph = new HashMap<>();
        Map<String, Pair<String,Integer>> repair = new HashMap<>();
        buildExpGraph(graph,rc);
        //改动的cell尽可能少
        //修改其中的某些cell 使得能够满足条件
        // c1>c2>c3 原来的值 5 4 3
        // c1<c2<c3 则冲突的cell改为 1 2 3
        // c2<c3
        // c2>c4
        //沿着一个方向修改
        //沿着图的拓扑方向遍历，遍历过程中修改，如5 4 3 修复为 6 7 修复为上一个数加一，在拓扑的过程中记录前一个数的结果即可
        return repairByTuopu(indexSet,graph,repair);
    }

    private List<Assignment> repairByTuopu(IndexSet indexSet, Map<String, List<String>> graph, Map<String, Pair<String, Integer>> repair) {
        //拓扑判断图的节点
        int cnt = 0;
        List<Assignment> assignments = new ArrayList<>();
        String table = indexSet.fileReaderByParams.fileParams.table;
        Map<String,Integer> in = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        for(String key:graph.keySet()){
            in.put(key,0);
            for(String val:graph.get(key)){
                in.put(val,in.getOrDefault(val,0)+1);
            }
        }
        for(String key:graph.keySet()){
            if(in.get(key)==0){
                queue.add(key);
                String attr = key.split(",")[1];
                String val = tools.getValueByCell(indexSet, key,table);
                initSortedList(indexSet,attr);
                int pos = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attr),attr,val,table);//第一个大于等于元素的位置
                repair.put(key,new Pair<>(val,pos));
            }
        }
        while(!queue.isEmpty()){
            for(int i = 0;i<queue.size();i++){
                String cell = queue.poll();
                cnt++;
                if(!graph.containsKey(cell))continue;//最后一个节点一定是没有next的
                for(String next:graph.get(cell)){
                    //更新next对应的
                    in.put(next,in.get(next)-1);
                    //修复表达式为c1<c2的，更新c2为c1+1
                    Pair<String,Integer> nextPair = getNextVal(indexSet,cell,repair.get(cell),table);//cell的下一个值
                    if(repair.containsKey(next)){
                        if(Integer.parseInt(repair.get(next).getKey())<Integer.parseInt(nextPair.getKey()))repair.put(next,nextPair);
                    }else{
                        repair.put(next,nextPair);
                    }
                    if(in.get(next)==0){
                        queue.add(next);
                    }
                }
            }
        }
        for(Map.Entry<String,Pair<String,Integer>> entry:repair.entrySet()){
//            if(rc.frontier.contains(entry.getKey()))//只更改frontier中的cell，version 11.30：repairExp中的cell都有可能修改
            assignments.add(new Assignment(entry.getKey(),entry.getValue().getKey(),true));
        }
        if(cnt <= graph.size()){//由于graph中最后一层节点没有存储，所以cnt必大于graph.size，如果<=.说明有环
            //有环时，返回空
            System.out.println("QP find assignments has circle");
            assignments.clear();
        }
//        System.out.println("assignments size"+assignments.size());
        return assignments;//节点个数和访问个数一致时，无环
    }

    private void initSortedList(IndexSet indexSet, String attr) {
        if(indexSet.sorted_list.containsKey(attr))return ;//已有 跳过即可
        //没有的话创建
        indexSet.buildSortIndex(attr);
    }

    private Pair<String, Integer> getNextVal(IndexSet indexSet, String cell, Pair<String, Integer> curPair, String table) {
        String attr = cell.split(",")[1];
        //第一个大于key的值，正是所求
        int i = tools.upperBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attr),attr,curPair.getKey(),table);
//        int i = curPair.getValue();
//        for(;i<indexSet.sorted_list.get(attr).size();i++){
//            if(!curPair.getKey().equals(tools.getValueByAttr(indexSet.fileReaderByParams.originList.get(indexSet.sorted_list.get(attr).get(i)),attr,table)))break;
//        }
        if(i==indexSet.sorted_list.get(attr).size())i-=1;
        return new Pair<>(tools.getValueByAttr(indexSet.fileReaderByParams.originList.get(indexSet.sorted_list.get(attr).get(i)),attr,table),i);
    }

    private void buildExpGraph(Map<String, List<String>> graph, RepairContext rc) {
        for(RepairExp repairExp:rc.repairExpList){
            if(repairExp.exp==4||repairExp.exp==5){
                if(graph.containsKey(repairExp.cell)){
                    graph.get(repairExp.cell).add(repairExp.confliCell);
                }else{
                    List<String> temp = new ArrayList<>();
                    temp.add(repairExp.confliCell);
                    graph.put(repairExp.cell,temp);
                }
            }else if(repairExp.exp==2||repairExp.exp==3){
                if(graph.containsKey(repairExp.confliCell)){
                    graph.get(repairExp.confliCell).add(repairExp.cell);
                }else{
                    List<String> temp = new ArrayList<>();
                    temp.add(repairExp.cell);
                    graph.put(repairExp.confliCell,temp);
                }
            }
        }
    }
}
