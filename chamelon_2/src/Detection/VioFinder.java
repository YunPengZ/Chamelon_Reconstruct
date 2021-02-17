package Detection;

import Bean.Violation;
import Entrance.Inc;
import FileOption.FileReaderByParams;
import IndexOption.IndexSet;
import Util.Tools;

import java.util.*;

public class VioFinder {
    Tools tools;
    long buildIndexTimeCost = 0;
    public VioFinder(){
        tools = new Tools();
    }
    public void detect(List<Violation> violationList, IndexSet indexSet, List<Integer> incIdList, List<String> otherAtomSet, boolean print, int dcId) {
        //这里处理的直接是不等于
        //嵌套处理，多个谓词join的情况如何处理
        dfs(violationList,indexSet,incIdList,incIdList,otherAtomSet,print,0,dcId);
    }

    private void dfs(List<Violation> violationList,IndexSet indexSet, List<Integer> leftCluster,List<Integer> rightCluster, List<String> otherAtomSet,boolean print,int index,int dcId) {
        Map<Integer,List<Integer>> sortedMapA = new TreeMap<>();
        Map<Integer,List<Integer>> sortedMapB = new TreeMap<>();
//        System.out.println("index "+index+"left size"+leftCluster.size()+"right size "+rightCluster.size());
//        System.out.println(otherAtomSet.get(index));
//        System.out.println(otherAtomSet.size());
        if(index==otherAtomSet.size()){
            int dSz = indexSet.fileReaderByParams.originIdList.size();
            for (Integer left:leftCluster){
                for(Integer right:rightCluster){
//                    if(!tools.checkTuplePair(indexSet.fileReaderByParams.incList,left,right,new HashSet<>(otherAtomSet),indexSet.fileReaderByParams.fileParams.table)) {
////                        System.out.println(indexSet.fileReaderByParams.incList.get(left).toString()+indexSet.fileReaderByParams.incList.get(right));
//                    }else{
                        violationList.add(new Violation(left+dSz,right+dSz,dcId));
                    }

//                }
            }
            return ;
        }
        //检查当前层，同时处理下一层
        int atomType = tools.getOperator(otherAtomSet.get(index));
        if(atomType<6&&atomType>1){
            //数值型的
            String firstAttr = tools.getAttrByPre(otherAtomSet.get(index),1);
            String secondAttr = tools.getAttrByPre(otherAtomSet.get(index),2);
            buildSortedMap(sortedMapA,indexSet.fileReaderByParams,leftCluster,firstAttr);
            buildSortedMap(sortedMapB,indexSet.fileReaderByParams,rightCluster,secondAttr);
            if(print){
                System.out.println("mapa"+sortedMapA.keySet());
                System.out.println("mapb"+sortedMapB.keySet());
            }
            if(atomType==2||atomType==3){
                processLargeThan(violationList,indexSet,sortedMapA,sortedMapB,otherAtomSet,print,index,dcId,false);
            }else{
                //处理t1.A<t2.B,当作t2.B>t1.A处理即可，记得反转即可
                processLargeThan(violationList,indexSet,sortedMapB,sortedMapA,otherAtomSet,print,index,dcId,true);
            }
            sortedMapA.clear();
            sortedMapB.clear();
        }

    }

    private void processLargeThan(List<Violation> violationList, IndexSet indexSet, Map<Integer, List<Integer>> sortedMapA, Map<Integer, List<Integer>> sortedMapB,
                                  List<String> otherAtomSet,boolean print, int index, int dcId,boolean reverse) {
        Iterator itA = sortedMapA.entrySet().iterator();
        Iterator itB = sortedMapB.entrySet().iterator();
        int BVal = ((Map.Entry<Integer,List<Integer>>)itB.next()).getKey();
        int AVal = ((Map.Entry<Integer,List<Integer>>)itA.next()).getKey();
        while(itA.hasNext()&&AVal<=BVal){
            if(index==1&&print) System.out.println(String.valueOf(AVal)+BVal);
            AVal = ((Map.Entry<Integer,List<Integer>>)itA.next()).getKey();
        }
        if(AVal<=BVal&&!itA.hasNext())return ;
        //当前的Aval>Bval
        if(print&&index==1) System.out.println("arrive center"+AVal+" "+BVal);
        List<Integer> LHS = new ArrayList<>(sortedMapA.get(AVal));
        List<Integer> RHS = new ArrayList<>(sortedMapB.get(BVal));
        if(itA.hasNext()||itB.hasNext()){
            while(itB.hasNext()){
                BVal = ((Map.Entry<Integer,List<Integer>>)itB.next()).getKey();
                if(AVal>BVal){
                    RHS.addAll(sortedMapB.get(BVal));
                }else{
                    while(itA.hasNext()){
                        AVal =  ((Map.Entry<Integer,List<Integer>>)itA.next()).getKey();
                        if(AVal<BVal){//原文中未处理等于的情况，
                            LHS.addAll(sortedMapA.get(AVal));
                        }else if(AVal>BVal){
                            //如果当前是反的 ，下一轮输入调整一下
                            List<Integer> nextInputL = new ArrayList<>(LHS);
                            List<Integer> nextInputR = new ArrayList<>(RHS);
                            if(reverse)dfs(violationList,indexSet,nextInputR,nextInputL,otherAtomSet,print,index+1,dcId);
                            else dfs(violationList,indexSet,nextInputL,nextInputR,otherAtomSet,print,index+1,dcId);
                            List<Integer> temp = new ArrayList<>(RHS);
                            LHS.clear();
                            RHS.clear();
                            LHS.addAll(sortedMapA.get(AVal));
                            RHS.addAll(temp);
                            RHS.addAll(sortedMapB.get(BVal));
                            break;
                        }
                    }
                }
            }
        }
        while(itA.hasNext()){
            AVal =  ((Map.Entry<Integer,List<Integer>>)itA.next()).getKey();
            LHS.addAll(sortedMapA.get(AVal));
        }
        if(reverse)dfs(violationList,indexSet,RHS,LHS,otherAtomSet,print,index+1,dcId);
        else dfs(violationList,indexSet,LHS,RHS,otherAtomSet,print,index+1,dcId);
    }

    private void buildSortedMap(Map<Integer,List<Integer>> sortedMap,FileReaderByParams fileReaderByParams,List<Integer> incIdList, String attr) {
        //long start = System.currentTimeMillis();
        long start, end;
        // System.out.println(incIdList.size()+":"+incIdList.toString());
        //System.out.println(fileReaderByParams.incList.size());
        //System.out.println(fileReaderByParams.incList.get(0));
        for(Integer tupleId:incIdList){
            start = System.currentTimeMillis();
            int val = Integer.parseInt(tools.getValueByAttr(fileReaderByParams.incList.get(tupleId),attr,fileReaderByParams.fileParams.table));
            // int val = tools.getIntValueByAttr(fileReaderByParams.incList.get(tupleId),attr,fileReaderByParams.fileParams.table);
            // start = System.currentTimeMillis();
            if(sortedMap.containsKey(val)){
                sortedMap.get(val).add(tupleId);
            }else{
                List<Integer> temp = new ArrayList<>();
                temp.add(tupleId);
                sortedMap.put(val,temp);
            }
            end = System.currentTimeMillis();
            this.buildIndexTimeCost +=(end-start);
        }
        //long end = System.currentTimeMillis();
        //this.buildIndexTimeCost +=(end-start);
    }

    public long getBuildIndexTime() {
        return this.buildIndexTimeCost;
    }
}
