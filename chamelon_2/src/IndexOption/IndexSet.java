package IndexOption;

import Bean.EqualMapBean;
import FileOption.FileReaderByParams;
import Util.Tools;
import javafx.util.Pair;
import org.omg.CORBA.INTERNAL;

import java.io.*;
import java.util.*;

public class IndexSet implements Serializable {
    //所有索引集合
    Tools tools = new Tools();
    public FileReaderByParams fileReaderByParams;
    public Map<String, Map<String,Integer>> countMap;//统计不同列中不同属性值的出现次数
    public Map<String, Map<String,List<Integer>>> equivalenceIndex;
    public Map<String,List<Integer>> sorted_list;
    public Map<String,List<Integer>> permutation_list;
    public Set<String> choosedSet;
    public Map<String,EqualMapBean> equalMapBean;
    boolean test = false;
    public IndexSet(boolean test){
        this.test = test;
        this.sorted_list = new HashMap<>();
        this.permutation_list = new HashMap<>();
        this.countMap = new HashMap<>();
        this.equivalenceIndex = new HashMap<>();
        this.equalMapBean = new HashMap<>();
    }

    private static Comparator<Pair<String, Double>> scoreComparator = new Comparator<Pair<String, Double>>(){
        @Override
        public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
            return p1.getValue().compareTo(p2.getValue());//p1>p2 返回正数 升序排列
        }
    };

    private Set<String>indexSelect(List<String> dcs, boolean isChoosed){
        //输入不同的dc，返回应该再哪个dc上构建索引以及对应dc中哪些谓词
        Set<String> indSet = new HashSet<>();
        Set<String> equSet = new HashSet<>();
        Set<String> ineSet = new HashSet<>();
        Set<String> dc_1 = new HashSet<>();
        Set<String> dc_2 = new HashSet<>();
        for(String dc:dcs){
            boolean has_equal = false;
            for(String pre:dc.split("&")){//对于等值的谓词 直接拆分为多个等于号的拼接
                if(pre.contains("=")&&!pre.contains("!")){
                    equSet.add(pre);
                    has_equal = true;
                }
            }
            if(has_equal)dc_1.add(dc);
            else dc_2.add(dc);
        }
//        indSet.addAll(dc_1);
        processEquiDC(indSet,equSet,dc_1,isChoosed);
        processNotEquiDC(indSet,ineSet,dc_2,isChoosed);
        return indSet;
    }

    private void processNotEquiDC(Set<String> indSet, Set<String> ineSet, Set<String> dc_2, boolean isChoosed) {
        if(!isChoosed){
            for(String dc:dc_2){
                String firstAtom = "";
                String secondAtom = "";
                for(String atom:dc.split("&")){
                    if(firstAtom.equals(""))firstAtom = atom;
                    else if(secondAtom.equals(""))secondAtom = atom;
                    else break;
                }
                indSet.add(firstAtom+"&"+secondAtom);
            }
            return;
        }
        PriorityQueue<Pair<String,Double>> priorityQueue = new PriorityQueue<>(scoreComparator);
        for(String dc:dc_2){
            //dc的谓词组合
            List<String> inePre = new ArrayList<>();
            for(String pre:dc.split("&")){
                inePre.add(pre);
            }
            inePre.sort(new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.compareTo(t1);
                }
            });
            for (int i = 0;i<inePre.size();i++){
                for(int j = i+1;j<inePre.size();j++){
                    String ine_1 = inePre.get(i);
                    String ine_2 = inePre.get(j);
                    if(test){
                        priorityQueue.add(new Pair<>(ine_1+"&"+ine_2,i*1.0/(i+j)));
                    }else{
                        priorityQueue.add(new Pair<>(ine_1+"&"+ine_2,getNotEquiScore(ine_1,ine_2)));
                    }
                }
            }
        }
        chooseTopInd(indSet,dc_2,priorityQueue);
    }

    private void processEquiDC(Set<String> indSet, Set<String> equSet, Set<String> dc_1, boolean isChoosed) {
        if(equSet.size()==0)return ;
        if(isChoosed){
            PriorityQueue<Pair<String,Double>> priorityQueue = new PriorityQueue<>(equSet.size(),scoreComparator);
            for(String equ:equSet){
                priorityQueue.add(new Pair<>(equ,getEquiPreScore(equ)));
            }
            //获取最小分数的索引，并
            chooseTopInd(indSet,dc_1,priorityQueue);
        }else{
            indSet.addAll(equSet);
        }
    }


    private Double getEquiPreScore(String equ) {
        String attrA = tools.getAttrByPre(equ,1);
        String attrB = tools.getAttrByPre(equ,2);
        double selectivity = 0.0;
        int originSz = fileReaderByParams.originIdList.size();
        int cnt = 0;
        if(attrA==attrB){
            for(Map.Entry<String,Integer> entry:countMap.get(attrA).entrySet()){
                cnt +=(entry.getValue())*entry.getValue();
            }
        }else{
            for(Map.Entry<String,Integer> entryA:countMap.get(attrA).entrySet()) {
                for(Map.Entry<String,Integer> entryB:countMap.get(attrA).entrySet()) {
                    if(entryA.getKey().equals(entryB.getKey())){
                        cnt+=(entryA.getValue()*entryB.getValue());
                    }
                }
            }
        }
        selectivity = 1-((cnt)/(originSz*originSz));
        double cover = calcCoverage(equ);
        return (1-selectivity)/cover;
    }

    private Double getNotEquiScore(String ine_1, String ine_2) {
        //两个谓词组成的索引 计算满足谓词对的元组个数
        int cnt = 0;
        int originSz = fileReaderByParams.originIdList.size();
        for(int i =0;i<originSz;i++){
            for(int j = 0;j<originSz;j++){
                /***
                 * 由于dc涉及到跨列，因此可能存在(i,j)满足dc,但是(j,i)不满足dc的情况，所以所有元组对都要考虑，这个时间怕是要爆炸
                 * 但是对于两个元组对相等的情况暂不考虑
                 */
                if(i==j)continue;
                if(isSatisfy(i,j,ine_1)&&isSatisfy(i,j,ine_2))cnt++;
            }
        }
        double selectivity = 1-((cnt)/(originSz*originSz));
        double cover = calcCoverage(ine_1,ine_2);
        return (1-selectivity)/cover;
    }

    private boolean isSatisfy(int i, int j, String ine1) {
        //两个谓词组成的形式是 A\theta B,C\theta D
        //一定是不等谓词
        int id1 = fileReaderByParams.originIdList.get(i);
        int id2 = fileReaderByParams.originIdList.get(j);
        String attrA = tools.getAttrByPre(ine1,1);
        String attrB = tools.getAttrByPre(ine1,2);
        String valueA = tools.getValueByAttr(fileReaderByParams.originList.get(id1),attrA,fileReaderByParams.fileParams.table);
        String valueB = tools.getValueByAttr(fileReaderByParams.originList.get(id2),attrB,fileReaderByParams.fileParams.table);
        return tools.satisfyInEqualAtom(valueA,valueB,ine1);
    }

    private double calcCoverage(String pre_1,String pre_2) {
        int sz = fileReaderByParams.dcs.size()+1;//加上平滑项
        int cnt = 1;
        for(String dc:fileReaderByParams.dcs){
            if(dc.contains(pre_1)&&dc.contains(pre_2))cnt++;
        }
        return cnt/sz;
    }
    private double calcCoverage(String pre) {
        int sz = fileReaderByParams.dcs.size()+1;//加上平滑项
        int cnt = 1;
        for(String dc:fileReaderByParams.dcs){
            if(dc.contains(pre))cnt++;
        }
        return cnt/sz;
    }

    //每次获取分数最小的索引，并将索引对应的约束删除
    public void chooseTopInd(Set<String> indSet, Set<String> dcs, PriorityQueue<Pair<String, Double>> priorityQueue) {
        while(!dcs.isEmpty()&&!priorityQueue.isEmpty()){
            String ind = priorityQueue.poll().getKey();//移除最小分数对应的索引
//            priorityQueue.poll();
            for(Iterator<String> it = dcs.iterator();it.hasNext();){
                boolean flag  = true;
                String dc = it.next();
                for(String atom:ind.split("&")){
                    if(!dc.contains(atom)){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    indSet.add(ind);
                    it.remove();
                }
            }
        }
    }

    /***
     * 设置索引
     * @param fileReaderByParams
     * @param byIndex
     */
    public void setIndex(FileReaderByParams fileReaderByParams, boolean isChoosed, boolean byIndex) {
        Long start = System.currentTimeMillis();
        this.fileReaderByParams = fileReaderByParams;//不写入fileReaderByParams
        countMap = tools.getCountMap(fileReaderByParams.originList,fileReaderByParams.originIdList,fileReaderByParams.fileParams.table);
        assert countMap.size()==0;
        if(byIndex){//是否需要构建索引，不需要的话直接结束
            for(String dc:fileReaderByParams.dcs){
                List<String> equalAtom = new ArrayList<>();
                List<String> ineAtom = new ArrayList<>();
                for(String atom:dc.split("&")){
                    int atomType = tools.getOperator(atom);
                    if(atomType==1||atomType==6){
                        equalAtom.add(atom);
                    }else if(ineAtom.size()<2){
                        ineAtom.add(atom);
                    }
                }
                if(equalAtom.size()==0){
//                    buildIEjoinIndex(String.join("&",ineAtom));
                }else{
                    buildEquiIndex2(String.join("&",equalAtom),dc);
                }
            }
            choosedSet = indexSelect(fileReaderByParams.dcs,isChoosed);//返回的是被选择到的谓词
            for(String ind:choosedSet){
                if(ind.contains("&")){
                    buildIEjoinIndex(ind);
                }
//                else buildEquiIndex(ind);
            }
        }else{
            System.out.println("no index");
        }
        Long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"build index ");
    }

    private void buildEquiIndex2(String equalJoin,String dc) {
        equalMapBean.put(dc,dfs(equalJoin.split("&"),0,fileReaderByParams.fileParams.table,fileReaderByParams.originIdList));
    }

    private void buildEquiIndex(String ind) {

        //建立一个三级的映射
//        List<Integer> tupleIds = fileReaderByParams.originIdList;
//        dfs(equalJoin.split("&"),0,fileReaderByParams.fileParams.table);

        String attrA = tools.getAttrByPre(ind,1);
        String attrB = tools.getAttrByPre(ind,2);
        System.out.println(attrA);
        equivalenceIndex.put(attrA,tools.getEquiMap(fileReaderByParams.originList, fileReaderByParams.originIdList, attrA,fileReaderByParams.fileParams.table));
        if(!attrA.equals(attrB)){
            equivalenceIndex.put(attrB,tools.getEquiMap(fileReaderByParams.originList, fileReaderByParams.originIdList, attrB,fileReaderByParams.fileParams.table));
        }
    }

    private EqualMapBean dfs(String[] split, int i, String table,List<Integer> tupleIds) {
        if(i>=split.length) return null;
        String atom = split[i];
        String attr = tools.getAttrByPre(atom,1);
        boolean hasNext = i<split.length-1;
        int atomType = tools.getOperator(atom);
        EqualMapBean equalMapBean = new EqualMapBean(attr,hasNext,atomType==6);
        if(!hasNext){
            equalMapBean.mapCur = tools.getEquiMap(fileReaderByParams.originList,tupleIds,attr,table);
        }else{
            for(Map.Entry<String,List<Integer>> entry:tools.getEquiMap(fileReaderByParams.originList,tupleIds,attr,table).entrySet()){
                equalMapBean.mapNext.put(entry.getKey(),dfs(split,i+1,table,entry.getValue()));
            }
        }
        return equalMapBean;
    }

    private void buildIEjoinIndex(String ind) {
        String firstIne = ind.split("&")[0];
        String secondIne = ind.split("&")[1];
        String attrA = tools.getAttrByPre(firstIne,1);
        String attrB = tools.getAttrByPre(firstIne,2);
        String attrC = tools.getAttrByPre(secondIne,1);
        String attrD = tools.getAttrByPre(secondIne,2);

        //不需要像IEjoin 一样根据操作符的不同变化排序顺序
        buildSortIndex(attrA);
        buildSortIndex(attrB);
        buildSortIndex(attrC);
        buildSortIndex(attrD);
        buildPermutationIndex(attrA,attrC);
        buildPermutationIndex(attrB,attrD);
    }

    private void buildPermutationIndex(String attrA, String attrC) {
        List<Integer> permutation = new ArrayList<>();
        for(Integer id:sorted_list.get(attrA)){
            int pos = sorted_list.get(attrC).indexOf(id);
            permutation.add(pos);
        }
        permutation_list.put("permutation_"+attrA+"_"+attrC,permutation);
    }

    //按照属性值升序排列
    public void buildSortIndex(String attr) {
        if(sorted_list.containsKey(attr))return ;
        List<Integer> temp = new ArrayList<>(fileReaderByParams.originIdList);
        temp.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer id1, Integer id2) {
                String val1 = tools.getValueByAttr(fileReaderByParams.originList.get(id1),attr,fileReaderByParams.fileParams.table);
                String val2 = tools.getValueByAttr(fileReaderByParams.originList.get(id2),attr,fileReaderByParams.fileParams.table);
                int valInt1 = Integer.parseInt(val1);
                int valInt2 = Integer.parseInt(val2);
                return valInt1-valInt2;
            }
        });
        sorted_list.put(attr,temp);
    }

    @Override
    public String toString() {
        return "IndexSet{" +
                "tools=" + tools +
                ", fileReaderByParams=" + fileReaderByParams +
                ", countMap=" + countMap +
                ", equivalenceIndex=" + equivalenceIndex +
                ", sorted_list=" + sorted_list +
                ", permutation_list=" + permutation_list +
                ", choosedSet=" + choosedSet +
                ", test=" + test +
                '}';
    }

}
