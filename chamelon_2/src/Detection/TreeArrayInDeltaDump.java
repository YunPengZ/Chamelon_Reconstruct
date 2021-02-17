package Detection;


import Bean.Violation;
import FileOption.FileReaderByParams;
import javafx.util.Pair;
import org.omg.CORBA.INTERNAL;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;

import java.util.*;

import static Detection.TreeArrayTools.lowBit;
import static Util.Tools.*;

public class TreeArrayInDeltaDump {
    Set<String> otherAtomSet;
    private String attrA,attrB,attrC,attrD,table;
    private boolean isAscA,isAscB,isAscC,isAscD,print,isParticular;
    private long BuildIndexTime = 0;
    Map<Integer,List<Integer>> orderMapB;
    TreeMap<Integer,Map<Integer,List<Integer>>> clusterAC;
    List<TreeNodeDump> nodes;
    List<Integer> offSetAB;

    public TreeArrayInDeltaDump(Set<String> otherAtomSet, String attrA, String attrB, String table, boolean isAscA, boolean isAscB, boolean print) {
        this.attrB = attrA;
        this.attrD = attrB;
        this.table = table;
        this.isAscB = isAscA;
        this.isAscD = isAscB;
        this.print = print;
        this.otherAtomSet = otherAtomSet;
        this.isParticular = true;
    }


    public TreeArrayInDeltaDump(Set<String> otherAtomSet, AttributeType attributeType, String table, boolean isParticular) {
        this.attrA = attributeType.attrA;
        this.attrB = attributeType.attrB;
        this.attrC = attributeType.attrC;
        this.attrD = attributeType.attrD;
        this.isAscA = attributeType.isAscA;
        this.isAscB = attributeType.isAscB;
        this.isAscC = attributeType.isAscC;
        this.isAscD = attributeType.isAscD;
        this.otherAtomSet = otherAtomSet;
        this.isParticular = isParticular;
        this.table =table;
    }


    public void detectionInParticular(FileReaderByParams fileReaderByParams, List<Violation> violationList, List<Integer> incIdList,int dcId,long start) {
        //以t1.B>t2.B&t1.D>t2.D为例
        orderMapB = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer k1, Integer k2) {
                if(isAscB)return k1-k2;
                else return k2-k1;
            }
        });
        nodes= new ArrayList<>(fileReaderByParams.incList.size()+1);
        initDumpTreeArray(fileReaderByParams.incList,incIdList,orderMapB);
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        if(print)System.out.println("init dump tree array end ");
        getViolationInPar(violationList,fileReaderByParams,dcId);
    }

    private void initDumpTreeArray(List<Object> incList, List<Integer> incIdList, Map<Integer, List<Integer>> orderMapA) {
        for(Integer id:incIdList){
            int val = Integer.parseInt(getValueByAttr(incList.get(id),attrB,table));
            if(orderMapA.containsKey(val)){
                orderMapA.get(val).add(id);
            }else{
                List<Integer> temp = new ArrayList<>();
                temp.add(id);
                orderMapA.put(val,temp);
            }
        }
        initTreeArray(orderMapA);
    }

    private void initTreeArray(Map<Integer,List<Integer>> orderMapA) {
        nodes.add(new TreeNodeDump(-1));
        int index = 1;
        for(Map.Entry<Integer,List<Integer>> entry:orderMapA.entrySet()){
            TreeNodeDump treeNodeDump = new TreeNodeDump(index++);
            treeNodeDump.tupleIDs = entry.getValue();
            nodes.add(treeNodeDump);
        }
    }

    private void getViolationInPar(List<Violation> violationList, FileReaderByParams fileReaderByParams, int dcId) {
        TreeMap<Integer,List<Pair<Integer,Integer>>> orderMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer k1, Integer k2) {
                if(isAscD)return k1-k2;
                else return k2-k1;
            }
        });//将多个重复值放在一起处理的map
        long start = System.currentTimeMillis();
        processSecondOrderMap(orderMap,fileReaderByParams.incList);
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        int i = 0;
        for(Map.Entry<Integer,List<Pair<Integer,Integer>>> entry:orderMap.entrySet()){
            if(print&&(i*100)%orderMap.size()==0){
                System.out.println("tree array percentil:"+((i*100)/orderMap.size()));
            }
            i++;
            if(isParticular){
                for(Pair<Integer,Integer> pair:entry.getValue()){//处理了一下重复值
                    int index = nodes.get(pair.getKey()).index;//在A中的index
                    if(index==-1)continue;
                    //检测冲突放再更新元素之前，不然会添加到自身的元素作为重复
                    backtrace(violationList,fileReaderByParams,nodes.get(pair.getKey()),pair.getValue(),dcId);
                }
            }
            long nodeStart = System.currentTimeMillis();
            for(Pair<Integer, Integer> pair:entry.getValue()){
                int index = nodes.get(pair.getKey()).index;//在A中的index
                if(index==-1)continue;
                nodes.get(index).rootTupleIds.add(pair.getValue());
                //将元组对应的节点单独记录，后面只更新父节点的ids变量
                while(index<nodes.size()){
                    index+=lowBit(index);
                    if(index>=nodes.size())break;
                    nodes.get(index).ids.add(pair.getValue());//
                }
            }
            long nodeEnd = System.currentTimeMillis();
            this.BuildIndexTime+=(nodeEnd-nodeStart);
        }
    }

    private void processSecondOrderMap(TreeMap<Integer, List<Pair<Integer, Integer>>> orderMap, List<Object> incList) {
        //跳过第一个空node
        for(int i = 1;i<nodes.size();i++){
            for(Integer tupleId:nodes.get(i).tupleIDs){
                //list中存储的元素表明，该值所代表的元组，在原数组中的对应下标
                int val = Integer.parseInt(getValueByAttr(incList.get(tupleId),attrD,table));
                if(orderMap.containsKey(val)){
                    orderMap.get(val).add(new Pair<>(i,tupleId));
                }else{
                    List<Pair<Integer,Integer>> temp= new ArrayList<>();
                    temp.add(new Pair<>(i,tupleId));
                    orderMap.put(val,temp);
                }
            }//key 值是在树状数组中对应的几点，value是元组id
        }
    }

    private void backtrace(List<Violation> violationList,FileReaderByParams fileReaderByParams,
                           TreeNodeDump node,Integer tupleId,int dcId) {
        int index = node.index;
        String tupleVal = getValueByAttr(fileReaderByParams.incList.get(tupleId),attrB,table);
        while (index > 0) {
            if(index!=node.index){
                for(Integer id:nodes.get(index).rootTupleIds){
                    String val = getValueByAttr(fileReaderByParams.incList.get(id),attrB,table);
                    if (!tupleVal.equals(val)&&checkTuplePair(fileReaderByParams.incList, tupleId, id, otherAtomSet, table)) {
                        violationList.add(new Violation(tupleId+fileReaderByParams.originIdList.size(),id+fileReaderByParams.originIdList.size(),dcId));
                    }
                }
            }
            for (Integer id : nodes.get(index).ids) {// id 中的元素 在Lb插入顺序中早于node.tuple id 如果lb升序，那么node.tuple id>  id
                if (checkTuplePair(fileReaderByParams.incList, tupleId, id, otherAtomSet, table)) {
                    violationList.add(new Violation(tupleId+fileReaderByParams.originIdList.size(),id+fileReaderByParams.originIdList.size(),dcId));
                }
            }
            index -= lowBit(index);
        }
    }

    public long getBuildIndexTime() {
        return this.BuildIndexTime;
    }

    public void detectionInGeneral(FileReaderByParams fileReaderByParams, List<Violation> violationList, List<Integer> incIdList, int dcId, long start) {
        clusterAC = new TreeMap<>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        if(isAscA) return o1-o2;
                        else return o2-o1;
                    }
                }
        );
        detectionInParticular(fileReaderByParams,violationList,incIdList,dcId,start);
        initClusterAC(fileReaderByParams,incIdList);

        initOffSet();
        int pos = 0;
        int dSz = fileReaderByParams.originIdList.size();
        for(Map.Entry<Integer,Map<Integer, List<Integer>>> entry:clusterAC.entrySet()){
            int index = offSetAB.get(pos++)+1;//在树状数组中的下标
            while(index>0){
                for(Map.Entry<Integer,List<Integer>> entry2:entry.getValue().entrySet()){
                    int j = lowwerBoundByAttr(fileReaderByParams.incList,nodes.get(index).ids,attrD,entry2.getKey().toString(),table,isAscD);
                    for(int m = 0;m<j;m++){
                        for(Integer tupleId:entry2.getValue()){
                            if(checkTuplePair(fileReaderByParams.incList,tupleId,nodes.get(index).ids.get(m),otherAtomSet,table)){
                                violationList.add(new Violation(tupleId+dSz,nodes.get(index).ids.get(m)+dSz,dcId));
                            }
                        }
                    }
                }

                index-=lowBit(index);
            }
        }
    }

    private void initOffSet() {
        offSetAB = new ArrayList<>(clusterAC.size());
        List<Integer> La = new ArrayList<>(clusterAC.keySet());
        List<Integer> Lb = new ArrayList<>(orderMapB.keySet());
        int pos = 0;
        for(Integer val:La){
            if(isAscA){
                while (pos<Lb.size()&&Lb.get(pos)<val)pos++;
            }else{
                while (pos<Lb.size()&&Lb.get(pos)>val)pos++;
            }
            offSetAB.add(pos);
        }
    }

    private void initClusterAC(FileReaderByParams fileReaderByParams,List<Integer> incIdList) {
        for(Integer id:incIdList){
            int valA = Integer.parseInt(getValueByAttr(fileReaderByParams.incList.get(id),attrA,table));
            int valC = Integer.parseInt(getValueByAttr(fileReaderByParams.incList.get(id),attrC,table));
            if(clusterAC.containsKey(valA)){
                if(clusterAC.get(valA).containsKey(valC)){
                    clusterAC.get(valA).get(valC).add(id);
                }else{
                    List<Integer> temp = new ArrayList<>();
                    temp.add(id);
                    clusterAC.get(valA).put(valC,temp);
                }
            }else{
                Map<Integer,List<Integer>> clusterC = new HashMap<>();
                List<Integer> temp = new ArrayList<>();
                temp.add(id);
                clusterC.put(valC,temp);
                clusterAC.put(valA,clusterC);
            }
        }
    }
}
