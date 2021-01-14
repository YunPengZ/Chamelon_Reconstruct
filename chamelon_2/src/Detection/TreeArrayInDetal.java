package Detection;

import Bean.Violation;
import FileOption.FileReaderByParams;
import Util.AttrComparator;
import Util.Tools;
import Util.TreeNodeComparator;
import javafx.util.Pair;
import java.util.*;

public class TreeArrayInDetal {
    Tools tools;
    boolean print;
    long BuildIndexTime = 0;
    TreeArrayInDetal(boolean print){
        tools = new Tools();
        this.print = print;
    }

    public void detect(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList, Set<String> atomSet, String table, int dcId) {
        //���ж�ν���Ƿ����
        //�Ȱ���ν���Ƿ���ж�ν�ʷֱ�����
        long start = System.currentTimeMillis();
        List<String> atoms = new ArrayList<>(atomSet);
        atoms.sort(new Comparator<String>() {
            @Override
            public int compare(String atom1, String atom2) {
                if(noOtherAttr(atom1))return -1;
                else if(noOtherAttr(atom2))return 1;
                return 0;
            }
        });
        Set<String> otherAtomSet = new HashSet<>();
        Pair<String,String> pair = tools.getTwoInEqualAtoms(atoms,otherAtomSet);
        String firstAtom = pair.getKey();
        String secondAtom = pair.getValue();
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        if(print)System.out.println("dc"+firstAtom+secondAtom);
        if(noOtherAttr(secondAtom)){
            //��������ķ�Լ����������������Ľⷨ������
            detectTreeArrayInParticular(violationList,fileReaderByParams,incIdList,otherAtomSet,firstAtom,secondAtom,table,dcId);
        }else{
            detectTreeArrayInGeneral(violationList,fileReaderByParams,incIdList,otherAtomSet,firstAtom,secondAtom,table,dcId);
        }
    }

    /***
     * ��ͬ��IEJoin��atom����>ʱ��LA��LB������ѧ���У�LC,LDҲ�����������У�
     * ��atom����<ʱ��LA���ս�������
     * @param violationList
     * @param incIdList
     * @param otherAtomSet
     * @param firstAtom
     * @param secondAtom
     * @param table
     * @param dcId
     */
    private void detectTreeArrayInGeneral(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList, Set<String> otherAtomSet,
                                          String firstAtom, String secondAtom, String table,int dcId) {
        //todo
        //��Ҫ��ά����״����Ľṹ
        long start = System.currentTimeMillis();
        List<Integer> La,Lb;
        La =new ArrayList<>(incIdList);
        Lb= new ArrayList<>(incIdList);
        String attrA = tools.getAttrByPre(firstAtom,1);
        String attrB = tools.getAttrByPre(firstAtom,2);
        String attrC = tools.getAttrByPre(secondAtom,1);
        String attrD = tools.getAttrByPre(secondAtom,2);
        boolean isAscA,isAscB,isAscC,isAscD;
        if(firstAtom.contains(">")){
            isAscA = isAscB  = true;
        }else isAscA = isAscB  = false;
        if(secondAtom.contains(">")){
            isAscC = isAscD = true;
        }else isAscC = isAscD = false;
        AttrComparator attrComparatorA = new AttrComparator(fileReaderByParams.incList,attrA,table,isAscA);
        AttrComparator attrComparatorB = new AttrComparator(fileReaderByParams.incList,attrB,table,isAscB);
//        System.out.println("A is asc:"+isAscA);
        La.sort(attrComparatorA);
        Lb.sort(attrComparatorB);
        List<Integer> offsetAB = new ArrayList<>(incIdList.size());
        tools.initOffsetList(fileReaderByParams.incList,La,Lb,offsetAB,attrA,attrB,table,isAscA);
        List<TreeNode> nodes = new ArrayList<>(incIdList.size()+1);
        buildTreeArray(nodes,fileReaderByParams.incList,Lb,attrD,table,isAscD);
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        int equalOff;
        if(firstAtom.contains("=")&&secondAtom.contains("="))equalOff = 0;
        else equalOff = 1;
        //����offset
        //֮��Ҫ�Ƚ�һ�¼���offset��ֱ�������ֲ��ҵ�ʱ�����
        for (int i =0;i<incIdList.size();i++){
            int k = offsetAB.get(i);
            int index = k+1;//����״�����ж�Ӧ���±�
            if(index>=nodes.size()){
                index = nodes.size()-1;
            }
            while(index>0){//��ids����������������Ԫ��
                String value = tools.getValueByAttr(fileReaderByParams.incList.get(La.get(i)),attrC,table);
                int j = tools.lowwerBoundByAttr(fileReaderByParams.incList,nodes.get(index).ids,attrD,value,table,isAscC);
                for(int m = 0;m<j;m++){
                    if(tools.checkTuplePair(fileReaderByParams.incList,La.get(i),nodes.get(index).ids.get(m),otherAtomSet,table)){
                        violationList.add(new Violation(La.get(i)+fileReaderByParams.originIdList.size(),nodes.get(index).ids.get(m)+fileReaderByParams.originIdList.size(),dcId));
                    }
                }
                index-=lowBit(index);
            }
        }
    }
    //��Ӧpaper��buildTree����
    private void buildTreeArray(List<TreeNode> nodes, List<Object> incList, List<Integer> lb, String attrD,String table, boolean isAscD) {
        nodes.add(new TreeNode(-1,-1));
        for(int i = 1;i<=lb.size();i++){
            nodes.add(new TreeNode(i,lb.get(i-1)));
        }
        List<TreeNode> nodesD = new ArrayList<>(nodes);
        nodesD.sort(new TreeNodeComparator(incList,attrD,table,isAscD));
        for(TreeNode node:nodesD){
            int index = node.index;
            if(index==-1)continue;
            while(index<nodes.size()){
                nodes.get(index).ids.add(node.tupleId);
                index+=lowBit(index);
            }
        }
    }

    private void detectTreeArrayInParticular(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList,
                                             Set<String> otherAtomSet, String firstAtom, String secondAtom,String table,int dcId) {
        //���е�����ʽ��IEJoinһ��
        if(print)System.out.println("particular tree array");
//        System.out.println(firstAtom);
        long start = System.currentTimeMillis();

        String attrA = tools.getAttrByPre(firstAtom,1);
        String attrB = tools.getAttrByPre(secondAtom,1);
        boolean isAscA = firstAtom.contains(">");
        boolean isAscB = secondAtom.contains(">");
        Map<Integer,List<Integer>> orderMapA = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer k1, Integer k2) {
                if(isAscA)return k1-k2;
                else return k2-k1;
            }
        });
        List<TreeNodeDump> nodes = new ArrayList<>(fileReaderByParams.incList.size()+1);
        initDumpTreeArray(fileReaderByParams.incList,incIdList,nodes,orderMapA,attrA,table);
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        if(print)System.out.println("init dump tree array end ");
        getViolationInPar(violationList,nodes,fileReaderByParams,otherAtomSet,attrA,attrB,table,dcId,isAscB);
    }

    private void initDumpTreeArray(List<Object> incList, List<Integer> incIdList, List<TreeNodeDump> nodes, Map<Integer, List<Integer>> orderMapA, String attrA, String table) {
        for(Integer id:incIdList){
            int val = Integer.parseInt(tools.getValueByAttr(incList.get(id),attrA,table));
            if(orderMapA.containsKey(val)){
                orderMapA.get(val).add(id);
            }else{
                List<Integer> temp = new ArrayList<>();
                temp.add(id);
                orderMapA.put(val,temp);
            }
        }
        initTreeArray(nodes,orderMapA);
    }

    private void getViolationInPar(List<Violation> violationList, List<TreeNodeDump> nodes, FileReaderByParams fileReaderByParams, Set<String> otherAtomSet,String attrA,
                                   String attrB, String table,int dcId, boolean isAscB) {
        TreeMap<Integer,List<Pair<Integer,Integer>>> orderMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer k1, Integer k2) {
                if(isAscB)return k1-k2;
                else return k2-k1;
            }
        });//������ظ�ֵ����һ�����map
        long start = System.currentTimeMillis();
        processSecondOrderMap(orderMap,fileReaderByParams.incList,nodes,attrB,table);
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        int i = 0;
        for(Map.Entry<Integer,List<Pair<Integer,Integer>>> entry:orderMap.entrySet()){
            if(print&&(i*100)%orderMap.size()==0){
                System.out.println("tree array percentil:"+((i*100)/orderMap.size()));
            }
            i++;
            for(Pair<Integer,Integer> pair:entry.getValue()){//������һ���ظ�ֵ
                int index = nodes.get(pair.getKey()).index;//��A�е�index
                if(index==-1)continue;
                //����ͻ���ٸ���Ԫ��֮ǰ����Ȼ����ӵ������Ԫ����Ϊ�ظ�
//                System.out.println("backtrace")
                backtrace(violationList,fileReaderByParams,nodes,otherAtomSet,nodes.get(pair.getKey()),pair.getValue(),attrA,table,dcId);
            }
            long nodeStart = System.currentTimeMillis();
            for(Pair<Integer, Integer> pair:entry.getValue()){
                int index = nodes.get(pair.getKey()).index;//��A�е�index
                if(index==-1)continue;
                nodes.get(index).rootTupleIds.add(pair.getValue());
                //��Ԫ���Ӧ�Ľڵ㵥����¼������ֻ���¸��ڵ��ids����
                while(index<nodes.size()){
                    index+=lowBit(index);
                    if(index>=nodes.size())break;
                    nodes.get(index).ids.add(pair.getValue());//
                }
            }
            long nodeEnd = System.currentTimeMillis();
            this.BuildIndexTime+=(nodeEnd-nodeStart);
        }
//        for(TreeNode node:nodesB){//����B��˳�����Ԫ��
//            int index = node.index;//��A�е�index
//            if(index==-1)continue;
////            System.out.println(attrB+incList.get(node.tupleId).toString());
//            //����ͻ���ٸ���Ԫ��֮ǰ����Ȼ����ӵ������Ԫ����Ϊ�ظ�
//            backtrace(violationList,incList,nodes,otherAtomSet,node,table,dc);
//
//            while(index<nodes.size()){
//                nodes.get(index).ids.add(node.tupleId);//
//                index+=lowBit(index);
//            }
//
//        }
    }

    private void processSecondOrderMap(TreeMap<Integer, List<Pair<Integer, Integer>>> orderMap, List<Object> incList, List<TreeNodeDump> nodes, String attrB, String table) {
        //������һ����node
        for(int i = 1;i<nodes.size();i++){
            for(Integer tupleId:nodes.get(i).tupleIDs){
                //list�д洢��Ԫ�ر�������ֵ�������Ԫ�飬��ԭ�����еĶ�Ӧ�±�
                int val = Integer.parseInt(tools.getValueByAttr(incList.get(tupleId),attrB,table));
                if(orderMap.containsKey(val)){
                    orderMap.get(val).add(new Pair<>(i,tupleId));
                }else{
                    List<Pair<Integer,Integer>> temp= new ArrayList<>();
                    temp.add(new Pair<>(i,tupleId));
                    orderMap.put(val,temp);
                }
            }//key ֵ������״�����ж�Ӧ�ļ��㣬value��Ԫ��id
        }
    }

    private void initTreeArray(List<TreeNodeDump> nodes, Map<Integer,List<Integer>> orderMapA) {
        nodes.add(new TreeNodeDump(-1));
        int index = 1;
        for(Map.Entry<Integer,List<Integer>> entry:orderMapA.entrySet()){
            TreeNodeDump treeNodeDump = new TreeNodeDump(index++);
            treeNodeDump.tupleIDs = entry.getValue();
            nodes.add(treeNodeDump);
        }
    }

    private void backtrace(List<Violation> violationList,FileReaderByParams fileReaderByParams, List<TreeNodeDump> nodes, Set<String> otherAtomSet,
                           TreeNodeDump node,Integer tupleId,String attrA,String table,int dcId) {
        int index = node.index;
        String tupleVal = tools.getValueByAttr(fileReaderByParams.incList.get(tupleId),attrA,table);
        while (index > 0) {
            if(index!=node.index){
                for(Integer id:nodes.get(index).rootTupleIds){
                    String val = tools.getValueByAttr(fileReaderByParams.incList.get(id),attrA,table);
                    if (!tupleVal.equals(val)&&tools.checkTuplePair(fileReaderByParams.incList, tupleId, id, otherAtomSet, table)) {
                        violationList.add(new Violation(tupleId+fileReaderByParams.originIdList.size(),id+fileReaderByParams.originIdList.size(),dcId));
                    }
                }
            }
            for (Integer id : nodes.get(index).ids) {// id �е�Ԫ�� ��Lb����˳��������node.tuple id ���lb������ônode.tuple id>  id
                if (tools.checkTuplePair(fileReaderByParams.incList, tupleId, id, otherAtomSet, table)) {
                    violationList.add(new Violation(tupleId+fileReaderByParams.originIdList.size(),id+fileReaderByParams.originIdList.size(),dcId));
                }
            }
            index -= lowBit(index);
        }
    }

    private int lowBit(int index) {
        return index&(-index);
    }

    private boolean noOtherAttr(String atom) {
        String firstAttr = tools.getAttrByPre(atom,1);
        String secondAttr = tools.getAttrByPre(atom,2);
        if(firstAttr.equals(secondAttr)){
            return true;
        }
        return false;
    }

    public long getBuildIndexTime() {
        return this.BuildIndexTime;
    }
}
