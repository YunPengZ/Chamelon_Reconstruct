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
                String cell = mvc.poll();//������ ͬʱɾ��
//                Iterator<HyperEdge> iterator = this.CH.get(cell).edges.iterator();
                RepairContext rc = new RepairContext(cell);
//                while(iterator.hasNext()){//���ʹ��ñ�ɾ�����������ֻ��ɾ���Ļ����ﵽЧ������Ϊ������ͼ��
//                    //Ϊʲô����鲻�����
//                    LookUp(indexSet,cell,iterator,rc,0);//rc�����ʱ���������
//                }
//                System.out.println("once  lookup end");
                assignments  = determination.run(indexSet,CH, cell,rc);//�ֱ����d��delta d �е�Ԫ��
                updateResult(assignments,tempRepair);
                updateResult(assignments,result);
                assignments.clear();
            }
            RepairResult repairResult = new RepairResult();
            repairResult.setResult(new ArrayList<>(tempRepair.entrySet()));
            System.out.println("once repair size:"+tempRepair.size());
//            System.out.println(tempRepair);
            updateData(indexSet,repairResult);
            //���ø��º��d list ��inc list �������� ���¼��
            //֮���ع���ͻ��ͼ
            for(int i = 0;i<80;i++) System.out.print("#");
            System.out.println();
            resetGraph(indexSet,violationList);//�ؽ���ͻ��ͼ
            if(CH.size()==0){
                return ;//�޸����
            }
            processedCells.addAll(CH.keySet());
            int sizeAfter = processedCells.size();
            System.out.println("After repair,Conflict HyperGraph size is:"+CH.size());
            System.out.println("processedCells size before:"+sizeBefore+",after:"+sizeAfter);
            if(sizeBefore>=sizeAfter)break;//�²����ĳ�ͻcell���Ѿ�������һ��ʱ��������ԭ���Ĵ˴�����
            tempRepair.clear();//��������һ��ѭ���������tempRepair�������
        }
        //����ʱ�������
        dataPostProcess(indexSet);//����dataset�Լ���ָ��
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
                //�����������ֵ�����
                tupleId -= indexSet.fileReaderByParams.originIdList.size();
                tools.updateCellVal(indexSet.fileReaderByParams.incList.get(tupleId),attr,val,indexSet.fileReaderByParams.fileParams.table);
            }else{
                tools.updateCellVal(indexSet.fileReaderByParams.originList.get(tupleId),attr,val,indexSet.fileReaderByParams.fileParams.table);
            }
        }
    }

    private void dataPostProcess(IndexSet indexSet) {
       for(String cell: this.MVC){//ֻ����δ�޸�����cell
           if(result.containsKey(cell))continue;
           result.put(cell,"-1");
       }
    }

    private void resetGraph(IndexSet indexSet,List<Violation> violationList) {
        //���¼��
        System.out.println("rebuild conflict graph starting...");
        indexSet.setIndex(indexSet.fileReaderByParams,true,true);
        violationList.clear();
        IncDetection incDetection = new IncDetection();
        incDetection.detect(violationList,indexSet,3,true, false,false);//�ڲ�Ĭ��ʹ����״����
        CH.clear();
        MVC.clear();
        ConflictHyper conflictHyper = new ConflictHyper();
        conflictHyper.buildCH(violationList,CH,indexSet, new HashSet<>());
    }

    private void LookUp(IndexSet indexSet, String cell, Iterator<HyperEdge> iterator,RepairContext rc,int level) {
        /**
         * �����������ʹ�� �����������ȽϺ� ���㷨������while edges is not empty��仰��ʼִ��һ�����ѣ��Է��ʹ��ó��߽���ɾ��
         */
        HyperEdge edge = iterator.next();
        if(level>=3)return ;//����ջ�������30
        // �����Ĺ��̻����ջ���
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
        //����cell���������෴���޸����ʽ
        RepairExp repairExp = DenialRepair(indexSet,edge,cell);
        rc.repairExpList.add(repairExp);//�������漰�������޸����ʽ
        //frontier��ʾ��cell��ص�����cell
        //�� �޸�t2.cʱ������mvc�еĶ��㣬�Լ�������صĶ���t1.c t3.c t4.c����Ϊfrontier���һԱ
        //��ǰ�޸����ʽ���������cell��Ϊfrontier
//        System.out.println("level:"+level);
//        System.out.println("conflict size:"+edge.conflictNodes.size());
        for(ConflictNode frontier:edge.conflictNodes){
            String key = frontier.confliId+","+frontier.confliAttr;
            if(rc.frontier.contains(key))continue;
            rc.frontier.add(key);
            //��ͻ��ͼ��cell����ó�ͻcell�ĳ�ͻ���ߣ�
            Iterator<HyperEdge> iterator_2= this.CH.get(key).edges.iterator();
//            System.out.println("lookup continue");
            while(iterator_2.hasNext()){
//                System.out.println("lookup continue");
                LookUp(indexSet,key,iterator_2,rc,level+1);//����һ��ִ��һ��iterator.next��
            }
        }
    }

    private boolean isNumeric(HyperEdge edge) {
        int exp = edge.conflictNodes.get(0).operator;
        return exp<6&&exp>1;
    }

    private RepairExp DenialRepair(IndexSet indexSet, HyperEdge edge,String cell) {
        //������෴ֵ��Ҫ֪��ԭ�����ͻ��cell
        //��ʱ������һ�������а���cell��������ͻ�ڵ�
        int confliId = edge.conflictNodes.get(0).confliId;
        String confliAttr = edge.conflictNodes.get(0).confliAttr;
        String confliCell = confliId+","+confliAttr;
        String confliVal  = tools.getValueByCell(indexSet, confliCell,indexSet.fileReaderByParams.fileParams.table);
        RepairExp repairExp = new RepairExp(tools.getReverseOperator(edge.conflictNodes.get(0).operator),confliVal,cell,confliCell);
        return repairExp;
    }
}
