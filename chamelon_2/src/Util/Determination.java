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
        //�㷨��˼·��ֻ��rc��������cell�����޸�������frontier�е�cell����ֻ�޸�˳��ǰ��
        List<Assignment> assignments = new ArrayList<>();
        PriorityQueue<String> mvc = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                //��������
                return CH.get(s2).edges.size()-CH.get(s1).edges.size();
            }
        });//�Զ���Ƚ���
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
                String confliVal = tools.getValueByCell(indexSet, confliCell,indexSet.fileReaderByParams.fileParams.table);//��ó�ͻԪ���ֵ
                int operator = edge.conflictNodes.get(0).operator;
                if(operator==2||operator==3){
                    //val>confliVal,�޸�Ϊval<confliVal
                    upperBound = Math.min(upperBound,Integer.parseInt(confliVal));
                }else if(operator==4||operator==5){
                    lowerBound = Math.max(lowerBound,Integer.parseInt(confliVal));
                }
            }
            //�������½�������ֵ��ѡ�������ֵ
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
        //��������ѡ����֮ǰֵ�����ֵ
        if(lowerBound>=curValInt){//��һ�����ڵ���lowerBound��λ��,
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
        if(upperBound<=Integer.parseInt(curVal)){//��һ������upperBound
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
        //�޸����ʽΪ!=��������Ǵ� = ת������ģ����޸�
        String attr = cell.split(",")[1];//�������
        //���repair exp��ͻ ֱ�ӽ���
        boolean satisfy = true;
        String val = "";
        if(satisfy){
            //���ȵ�ֵ��ѡ����ִ������ġ�
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
        //��������޸ĵ�cell,ֱ����������
        //https://www.gurobi.com/documentation/8.0/refman/java_api_overview.html#sec:Java
        //��С��Ϊ���򹹽�����ͼ������ͼ�޻��������޸����ʽ���Ա�����
        Map<String,List<String>> graph = new HashMap<>();
        Map<String, Pair<String,Integer>> repair = new HashMap<>();
        buildExpGraph(graph,rc);
        //�Ķ���cell��������
        //�޸����е�ĳЩcell ʹ���ܹ���������
        // c1>c2>c3 ԭ����ֵ 5 4 3
        // c1<c2<c3 ���ͻ��cell��Ϊ 1 2 3
        // c2<c3
        // c2>c4
        //����һ�������޸�
        //����ͼ�����˷�������������������޸ģ���5 4 3 �޸�Ϊ 6 7 �޸�Ϊ��һ������һ�������˵Ĺ����м�¼ǰһ�����Ľ������
        return repairByTuopu(indexSet,graph,repair);
    }

    private List<Assignment> repairByTuopu(IndexSet indexSet, Map<String, List<String>> graph, Map<String, Pair<String, Integer>> repair) {
        //�����ж�ͼ�Ľڵ�
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
                int pos = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attr),attr,val,table);//��һ�����ڵ���Ԫ�ص�λ��
                repair.put(key,new Pair<>(val,pos));
            }
        }
        while(!queue.isEmpty()){
            for(int i = 0;i<queue.size();i++){
                String cell = queue.poll();
                cnt++;
                if(!graph.containsKey(cell))continue;//���һ���ڵ�һ����û��next��
                for(String next:graph.get(cell)){
                    //����next��Ӧ��
                    in.put(next,in.get(next)-1);
                    //�޸����ʽΪc1<c2�ģ�����c2Ϊc1+1
                    Pair<String,Integer> nextPair = getNextVal(indexSet,cell,repair.get(cell),table);//cell����һ��ֵ
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
//            if(rc.frontier.contains(entry.getKey()))//ֻ����frontier�е�cell��version 11.30��repairExp�е�cell���п����޸�
            assignments.add(new Assignment(entry.getKey(),entry.getValue().getKey(),true));
        }
        if(cnt <= graph.size()){//����graph�����һ��ڵ�û�д洢������cnt�ش���graph.size�����<=.˵���л�
            //�л�ʱ�����ؿ�
            System.out.println("QP find assignments has circle");
            assignments.clear();
        }
//        System.out.println("assignments size"+assignments.size());
        return assignments;//�ڵ�����ͷ��ʸ���һ��ʱ���޻�
    }

    private void initSortedList(IndexSet indexSet, String attr) {
        if(indexSet.sorted_list.containsKey(attr))return ;//���� ��������
        //û�еĻ�����
        indexSet.buildSortIndex(attr);
    }

    private Pair<String, Integer> getNextVal(IndexSet indexSet, String cell, Pair<String, Integer> curPair, String table) {
        String attr = cell.split(",")[1];
        //��һ������key��ֵ����������
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
