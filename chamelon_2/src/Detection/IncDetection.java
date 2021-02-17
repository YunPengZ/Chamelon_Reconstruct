package Detection;

import Bean.EqualMapBean;
import Bean.Violation;
import FileOption.FileReaderByParams;
import IndexOption.IndexReader;
import IndexOption.IndexSet;
import Util.SIzeofTools;
import Util.Tools;

import java.util.*;

/**
 * @author zyp
 * @time 2020.10
 */

public class IncDetection {
    long buildIndexTimeCost = 0;
    Tools tools = new Tools();
    public void detect(List<Violation> violationList, IndexSet indexSet, int type, boolean byIndex, boolean onlyDelta,boolean print) {
        //�����������ļ�������Ԫ���ڲ��ļ��
//        List<Violation> violationList = new ArrayList<>();
        Long start = System.currentTimeMillis();
        if(!onlyDelta) detectByIndex(violationList,indexSet,byIndex,print);
        tools.printType(type);
        detectInDelta(violationList,indexSet,type,print);
        getViolationSize(violationList);
        Long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"detect");
    }

    private void getViolationSize(List<Violation> violationList) {
        long res = 0L;
        for(Violation violation:violationList){
            res+=getSingleViolationSize(violation);
        }
        IndexReader indexReader = new IndexReader();
        System.out.println(indexReader.printSize(res));
    }

    private long getSingleViolationSize(Violation violation) {
        return 12L;//����12L���㼴��
    }

    private void detectInDelta(List<Violation> violationList, IndexSet indexSet,int type,boolean print) {
        /**
         * naive��IEJoin����״�������ּ�ⷽʽ
         */
        long start = System.currentTimeMillis();
        String table = indexSet.fileReaderByParams.fileParams.table;
        for(String dc:indexSet.fileReaderByParams.dcs){
            System.out.println(dc);
            int dcId = indexSet.fileReaderByParams.dcMap.get(dc);
            String firstEqualAttr = "",seconEqualAttr = "";
            Set<String> otherAtomSet = new HashSet<>();
            for(String atom:dc.split("&")){
                if(tools.getOperator(atom)==6){
                    //���ڵȼ���Ĵ���
                    if(firstEqualAttr.equals(""))firstEqualAttr = tools.getAttrByPre(atom,1);
                    else if(seconEqualAttr.equals(""))seconEqualAttr = tools.getAttrByPre(atom,1);
                    else otherAtomSet.add(atom);
                }else otherAtomSet.add(atom);
            }
            if(firstEqualAttr.equals("")){
                //����������ν�ʣ�ֱ��ִ�м���㷨
                detectInDeltaInEqual(violationList,indexSet,indexSet.fileReaderByParams.incIdList,otherAtomSet,table, print, dcId,type);
            }else{
                for(Map.Entry<String,List<Integer>> entry:tools.getEquiMap(indexSet.fileReaderByParams.incList,indexSet.fileReaderByParams.incIdList,firstEqualAttr,table).entrySet()){
                    if(seconEqualAttr.equals("")){
                        //ֻ����һ�����ڵ�
                        if(print)System.out.println("euqal map key is:"+entry.getKey());
                        detectInDeltaInEqual(violationList,indexSet,entry.getValue(),otherAtomSet,table,print, dcId,type);
                    }else{
                        //�����������������ϵ��ںţ�����ֻ���������ں������
                        for(Map.Entry<String,List<Integer>> entry2:tools.getEquiMap(indexSet.fileReaderByParams.incList,entry.getValue(),seconEqualAttr,table).entrySet()){
                            detectInDeltaInEqual(violationList,indexSet,entry2.getValue(),otherAtomSet,table,print,dcId,type);
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        tools.printRunTime(0,buildIndexTimeCost,"build Index in detal d");
        tools.printRunTime(start,end,"detect in detlta d");
        System.out.println("violation size:"+violationList.size());

    }

    /**
     *
     * type = 1:naive
     * type = 2:IEJoin
     * type = 3:treeArray
     * type = 4:vioFinder
     * type = 5��treeArray-dump
     */
    private void detectInDeltaInEqual(List<Violation> violationList, IndexSet indexSet, List<Integer> incIdList, Set<String> otherAtomSet, String table, boolean print, int dcId, int type) {
        int numericCnt = 0;
        String oneTuple = "";
        for(String atom:otherAtomSet){
            int atomType = tools.getOperator(atom);
            if(atomType>1&&atomType<6){
                numericCnt++;
            }else if(atomType==8){
                oneTuple = atom;
            }
        }
        if(!oneTuple.equals("")){
            processOneTupleAtom(indexSet,incIdList,oneTuple);
        }
        otherAtomSet.remove(oneTuple);
        if(type == 1){//ʣ���
            NaiveDetectionInDetal naiveDetectionInDetal = new NaiveDetectionInDetal();
            naiveDetectionInDetal.detect(violationList,indexSet.fileReaderByParams,incIdList,otherAtomSet,table,dcId);
        }else if(numericCnt<2||type==4){
            VioFinder vioFinder = new VioFinder();
            vioFinder.detect(violationList,indexSet,incIdList,new ArrayList<>(otherAtomSet),print,dcId);
            buildIndexTimeCost += vioFinder.getBuildIndexTime();
//            processSingleAtom(violationList,indexSet.fileReaderByParams,incIdList,otherAtomSet,table,dcId);
        }
        else if(type==2){
            IEJoinInDelta ieJoinInDelta = new IEJoinInDelta();
            ieJoinInDelta.detect(violationList,indexSet.fileReaderByParams,incIdList,otherAtomSet,table,dcId);
            buildIndexTimeCost += ieJoinInDelta.getBuildIndexTime();
        }else if(type==3||type==5){
            boolean useDump = false;
            if(type==5){
                useDump = true;
            }
            TreeArrayInDetal treeArrayInDetal = new TreeArrayInDetal(print,useDump);

            treeArrayInDetal.detect(violationList,indexSet.fileReaderByParams,incIdList,otherAtomSet,table,dcId);
            buildIndexTimeCost += treeArrayInDetal.getBuildIndexTime();
        }
//        for(Violation violation:violationList){
//            System.out.println(tools.getObj(indexSet,violation.getFirst()).toString()+tools.getObj(indexSet,violation.getSecond()));
//        }
        if(print)System.out.println("current violations in delta d sz:"+violationList.size());
    }

    private void processOneTupleAtom(IndexSet indexSet, List<Integer> incIdList, String oneTuple) {
        Iterator<Integer> idIterator = incIdList.iterator();
        String firstAttr = tools.getAttrByPre(oneTuple,1);
        String secondAttr = tools.getAttrByPre(oneTuple,2);
        while(idIterator.hasNext()){
            int tupleId = idIterator.next();
            String firstVal = tools.getValueByCell(indexSet, tupleId+","+firstAttr,indexSet.fileReaderByParams.fileParams.table);
            String secondVal = tools.getValueByCell(indexSet, tupleId+","+secondAttr,indexSet.fileReaderByParams.fileParams.table);
            if(!tools.satisfyAtom(firstVal,secondVal,oneTuple)){
                idIterator.remove();
            }
        }
    }

    private void processSingleAtom(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList, Set<String> otherAtomSet, String table, int dcId) {
//        String InequalAtom = "";
//        for(String atom:otherAtomSet){
//            int atomType = tools.getOperator(atom);
//           if(atomType==1){
//
//           }else if(atomType<6&&atomType>1){
//               InequalAtom = atom;
//           }
//        }
//        otherAtomSet.remove(InequalAtom);
//        if(!InequalAtom.equals("")){
//            processInEqual(violationList,fileReaderByParams,incIdList,InequalAtom,otherAtomSet,table,dcId);
//        }else{
//            NaiveDetectionInDetal naiveDetectionInDetal = new NaiveDetectionInDetal();
//            naiveDetectionInDetal.detect(violationList,fileReaderByParams,incIdList,otherAtomSet,table,dcId);
//        }
    }

    private void processInEqual(List<Violation> violationList, IndexSet indexSet, List<Integer> incIdList, String atom, Set<String> otherAtomSet, String table, int dcId) {
        String attr = tools.getAttrByPre(atom,1);
        Map<String,List<Integer>> equiMap = tools.getEquiMap(indexSet.fileReaderByParams.incList,incIdList,attr,table);
        for (String key:equiMap.keySet()){
            for(String secondKey:equiMap.keySet()){
                if(!key.equals(secondKey)){
                    getJoinResutl(violationList,indexSet,equiMap.get(key),equiMap.get(secondKey),otherAtomSet,dcId);
                }
            }
        }
    }

    private void getJoinResutl(List<Violation> violationList, IndexSet indexSet, List<Integer> left, List<Integer> right, Set<String> otherAtomSet, int dcId) {
//        if(otherAtomSet.size()==0){
//            Violation violation = new Violation(true,dcId);
//            violation.lefts = left;
//            violation.rights = right;
//            violationList.add(violation);
//        }
        for(Integer tupleId:left){
            for(Integer tupleId2:right){
                boolean satisfy = true;
                for (String atom:otherAtomSet){
                    if(!satisfy)break;
                    satisfy = tools.satisfyIdAtom(indexSet,tupleId,tupleId2,atom,dcId);
                }
                if(satisfy)violationList.add(new Violation(tupleId,tupleId2,dcId));
            }
        }
    }

    private void detectByIndex(List<Violation> violationList, IndexSet indexSet, boolean byIndex, boolean print) {
        //dc�ܶ�ʱ����������ѡ��������������������ø���dc����غ�
        long start = System.currentTimeMillis();
        int percentile = indexSet.fileReaderByParams.incIdList.size()/100;
        Long last = System.currentTimeMillis();
        for(int i = 0;i<indexSet.fileReaderByParams.incIdList.size();i++){
            int incId = indexSet.fileReaderByParams.incIdList.get(i);
            if(i%percentile==0&&print){
                Long cur = System.currentTimeMillis();
                tools.printRunTime(last,cur,"percentile"+i/percentile+" by index/noindex detection");
                last = cur;
                System.out.println(violationList.size());
            }
            if(byIndex){
                detectByEqualMap(violationList,incId,indexSet);
//                detectByEquiIndex(violationList,incId,indexSet.fileReaderByParams,indexSet.equivalenceIndex,indexSet.choosedSet);
                detectByIEJoinIndex(violationList,incId,indexSet);
            }else{
                detectByNaive(violationList,incId,indexSet.fileReaderByParams);
            }
        }
        long end = System.currentTimeMillis();
        tools.printRunTime(start,end,"detect by Index ");
    }

    private void detectByEqualMap(List<Violation> violationList, int incId, IndexSet indexSet) {
        for(String dc:indexSet.fileReaderByParams.dcs){
            if(dc.contains("=")){
                EqualMapBean equalMapBean = indexSet.equalMapBean.get(dc);
                dfs(indexSet,violationList,equalMapBean,incId,dc);
            }
        }
    }

    private void dfs(IndexSet indexSet, List<Violation> violationList, EqualMapBean equalMapBean, int incId, String dc) {
        if(equalMapBean==null)return ;
        int dcId = indexSet.fileReaderByParams.dcMap.get(dc);
        String incVal = tools.getValueByAttr(indexSet.fileReaderByParams.incList.get(incId),equalMapBean.attr,indexSet.fileReaderByParams.fileParams.table);
        if(incVal.equals("-1"))return ;
        if(!equalMapBean.hasNextMap){
            if(equalMapBean.isEqual){
                checkOtherLists(violationList,indexSet.fileReaderByParams,equalMapBean.mapCur.getOrDefault(incVal,null),incId,dc,dcId);
            }else{
                for(Map.Entry<String,List<Integer>> entry:equalMapBean.mapCur.entrySet()){
                    if(!entry.getKey().equals(incVal)){
                        checkOtherLists(violationList, indexSet.fileReaderByParams, entry.getValue(),incId, dc, dcId);
                    }
                }
            }
        }else{
            if(equalMapBean.isEqual){
                //map����keyֵ��ʱ��ᱨ��
                dfs(indexSet,violationList,equalMapBean.mapNext.getOrDefault(incVal,null),incId, dc);
            }else{
                for(Map.Entry<String,EqualMapBean> entry:equalMapBean.mapNext.entrySet()){
                    if(!entry.getKey().equals(incVal)){
                        dfs(indexSet,violationList,entry.getValue(),incId, dc);
                    }
                }
            }
        }
    }

    private void checkOtherLists(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> integers, int incId, String dc, int dcId) {
        if(integers==null) return ;
        Set<String> otherAtoms = new HashSet<>();
        List<String> equalAtoms = new ArrayList<>();
        for(String atom:dc.split("&")){
            int atomType = tools.getOperator(atom);
            if(atomType==1||atomType==6){
                equalAtoms.add(atom);
            }else{
                otherAtoms.add(atom);
            }
        }
        checkOtherAtoms(violationList,fileReaderByParams,integers,incId,String.join("&",equalAtoms),dc,dcId,true);
        checkOtherAtoms(violationList,fileReaderByParams,integers,incId,String.join("&",equalAtoms),dc,dcId,false);
    }

    /**
     * ��ʹ��������⵽�ĳ�ͻ
     * @param violationList
     * @param incId
     * @param fileReaderByParams
     */
    private void detectByNaive(List<Violation> violationList, Integer incId, FileReaderByParams fileReaderByParams) {
        String table = fileReaderByParams.fileParams.table;
        for(int i = 0;i<fileReaderByParams.originIdList.size();i++){
            int tupleId = fileReaderByParams.originIdList.get(i);
            for(String dc:fileReaderByParams.dcs){
                boolean incLeft,incRight;
                int dcId = fileReaderByParams.dcMap.get(dc);
                incLeft = incRight = true;
                for(String atom:dc.split("&")){
                    if(!incLeft&&!incRight)break;
                    String firstAttr = tools.getAttrByPre(atom,1);
                    String secondAttr = tools.getAttrByPre(atom,2);
                    String incFirstVal = tools.getValueByAttr(fileReaderByParams.incList.get(incId),firstAttr,table);
                    String incSecondVal = tools.getValueByAttr(fileReaderByParams.incList.get(incId),secondAttr,table);
                    String oriFirstVal = tools.getValueByAttr(fileReaderByParams.originList.get(tupleId),firstAttr,table);
                    String oriSecondVal = tools.getValueByAttr(fileReaderByParams.originList.get(tupleId),secondAttr,table);
                    if(!tools.satisfyAtom(incFirstVal,oriSecondVal,atom)){
                        incLeft = false;
                        //��break��ͬʱδ�����Ҳ��Ԫ��
                    }
                    if(!tools.satisfyAtom(oriFirstVal,incSecondVal,atom)){
                        incRight = false;
                    }
                }
                if(incLeft){
//                    System.out.println(dc);
//                    System.out.println(fileReaderByParams.incList.get(incId).toString()+fileReaderByParams.originList.get(tupleId));
                    violationList.add(new Violation(incId+fileReaderByParams.originIdList.size(),tupleId,dcId));
                }
                if(incRight){
//                    System.out.println(dc);
//
//                    System.out.println(fileReaderByParams.originList.get(tupleId)+fileReaderByParams.incList.get(incId).toString());
                    violationList.add(new Violation(tupleId,incId+fileReaderByParams.originIdList.size(),dcId));
                }
            }
        }
    }

    /***
     * ����IEJoin��������ͻ
     *
     */
    private void detectByIEJoinIndex(List<Violation> violationList, Integer incId, IndexSet indexSet) {
        Set<String> visitedDC = new HashSet<>();
        List<Integer> leftId = new ArrayList<>();
        List<Integer> rightId = new ArrayList<>();
//        System.out.println(indexSet.choosedSet);

//        for(String dc:indexSet.fileReaderByParams.dcs){
//            boolean hasEqual = false;
//            for(String atom:dc.split("&")){
//                int atomType = tools.getOperator(atom);
//                if(atomType==1||atomType==6){
//                    hasEqual = true;
//                    break;
//                }
//            }
//            if(!hasEqual){
//
//            }
//        }
        for(String ind:indexSet.choosedSet){
            //�������ѡ�� �����޷����ǵ�����dc
            if(ind.contains(">")||ind.contains("<")){
                satisfyIEJoinInd(leftId,rightId,indexSet,incId,ind);
                for(String dc:indexSet.fileReaderByParams.dcs){
                    int dcId = indexSet.fileReaderByParams.dcMap.get(dc);
                    if(visitedDC.contains(dc))continue;
                    if(dc.contains(ind)){
                        visitedDC.add(dc);
                        checkOtherAtoms(violationList,indexSet.fileReaderByParams,leftId,incId,ind,dc,dcId,true);
                        checkOtherAtoms(violationList,indexSet.fileReaderByParams,rightId,incId,ind,dc,dcId,false);
                    }
                }
            }
            leftId.clear();
            rightId.clear();
        }
//        System.out.println(visitedDC.size());
    }

    /**
     *
     * @param leftId ��inc��Ϊ���Ԫ�س���ʱ������ν�ʵ�Ԫ��id
     * @param rightId
     * @param incId
     * @param ind
     * @return
     */
    //���IEJoin����ô�������㵥��ν�ʵ�Ԫ����LB�е��±��ס��ͬʱ�������±���λ�������ʽ���档
    //���ڵڶ���ν�ʣ����㵥��ν�ʵ�Ԫ����Lb�е��±�Ҳ����ס����������ֱ���뼴�ɣ����ַ�ʽ�����ڿռ�ѹ�����ռ�Ч�ʺܸߣ�����ʱ�仹��һ��
    //���ڵ���Ԫ��Ĳ�ѯ��˵������O(n)
    private void  satisfyIEJoinInd(List<Integer> leftId, List<Integer> rightId, IndexSet indexSet, Integer incId, String ind) {
        String firstIne = ind.split("&")[0],secondIne = ind.split("&")[1];
        String attrA = tools.getAttrByPre(firstIne,1),attrB = tools.getAttrByPre(firstIne,2);
        String attrC = tools.getAttrByPre(secondIne,1),attrD = tools.getAttrByPre(secondIne,2);
        String table = indexSet.fileReaderByParams.fileParams.table;
        // inc������ʱ������������Ԫ����ӵ�leftId��
        Object incObj = indexSet.fileReaderByParams.incList.get(incId);
        String incValueA = tools.getValueByAttr(incObj,attrA,table);
        String incValueB = tools.getValueByAttr(incObj,attrB,table);
        String incValueC = tools.getValueByAttr(incObj,attrC,table);
        String incValueD = tools.getValueByAttr(incObj,attrD,table);
        if(incValueA.equals("-1")||incValueB.equals("-1")||incValueC.equals("-1")||incValueD.equals("-1"))return ;
        /***
         * inc��Ϊleft��right�������һ����
         * �������ֵ�һ��ν��
         * ��ν����>ʱ��
         */
        int dSz = indexSet.fileReaderByParams.originIdList.size();
        boolean visitedLeft[] = new boolean[dSz];
        boolean visitedRight[] = new boolean[dSz];
        String permuBD = "permutation_"+attrB+"_"+attrD;
        String permuAC = "permutation_"+attrA+"_"+attrC;

        if(firstIne.contains(">")){
            int indexLeftB = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrB),attrB,incValueA,table);
            //����inc��Ϊ�Ҳ�Ԫ��
            int indexRightA = tools.upperBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrA),attrA,incValueB,table);
            //������indexLeft
            setVisitedArray(visitedLeft,indexSet.permutation_list.get(permuBD),0,indexLeftB);
            setVisitedArray(visitedRight,indexSet.permutation_list.get(permuAC),indexRightA,dSz);

        }else if(firstIne.contains("<")){
            int indexLeftB = tools.upperBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrB),attrB,incValueA,table);
//            System.out.println("�߽�Ԫ��"+fileReaderByParams.originList.get(sorted_list.get(attrB).get(indexLeftB)));
            setVisitedArray(visitedLeft,indexSet.permutation_list.get(permuBD),indexLeftB,dSz);
            int indexRightA = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrA),attrA,incValueB,table);
            setVisitedArray(visitedRight,indexSet.permutation_list.get(permuAC),0,indexRightA);
        }
        //�ڶ���ν�ʵĴ������һ��ν����ʲô�޹�
        processSecondIne(indexSet,secondIne,leftId,rightId,visitedLeft,visitedRight,attrC,attrD,incValueC,incValueD,table,dSz);
    }

    private void processSecondIne(IndexSet indexSet, String secondIne, List<Integer> leftId, List<Integer> rightId,
                                  boolean[] visitedLeft, boolean[] visitedRight, String attrC, String attrD, String incValueC, String incValueD, String table,int dSz) {
        if(secondIne.contains(">")){
            int indexLeftD = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrD),attrD,incValueC,table);
            checkVisitedArray(leftId,visitedLeft,indexSet.sorted_list.get(attrD),0,indexLeftD);
            int indexRightC = tools.upperBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrC),attrC,incValueD,table);
            checkVisitedArray(rightId,visitedRight,indexSet.sorted_list.get(attrC),indexRightC,dSz);
        }else if(secondIne.contains("<")){
            int indexLeftD = tools.upperBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrD),attrD,incValueC,table);
            checkVisitedArray(leftId,visitedLeft,indexSet.sorted_list.get(attrD),indexLeftD,dSz);
            int indexRightC = tools.lowwerBoundByAttr(indexSet.fileReaderByParams.originList,indexSet.sorted_list.get(attrC),attrC,incValueD,table);
            checkVisitedArray(rightId,visitedRight,indexSet.sorted_list.get(attrC),0,indexRightC);
        }
    }

    private void setVisitedArray(boolean[] visited, List<Integer> permu, int left, int right) {
        for(int i = left;i<right;i++){
            visited[permu.get(i)] = true;
        }
    }

    private void checkVisitedArray(List<Integer> ids, boolean[] visited, List<Integer> list, int left, int right) {
        for(int i = left;i<right;i++){
            if(visited[i]){
                ids.add(list.get(i));
            }
        }
    }


    /***
     * ���õȼ�����������ͻ
     */
    private void detectByEquiIndex(List<Violation> violationList, Integer incId, FileReaderByParams fileReaderByParams, Map<String, Map<String, List<Integer>>> equivalenceIndex, Set<String> choosedSet) {
        //����һ��ʵ��ʱ���Ǽ���� �����Ե���ν�ʲ����ǿ��е����
        Set<String> visitedDC = new HashSet<>();
        for(String ind:choosedSet){
            if(ind.contains("=")){
                String attr = tools.getAttrByPre(ind,1);
                String val = tools.getValueByAttr(fileReaderByParams.incList.get(incId),attr,fileReaderByParams.fileParams.table);
                if(equivalenceIndex.get(attr).containsKey(val)){
                    for(String dc:fileReaderByParams.dcs){//�������ֵ��������ֵ�����
                        int dcId = fileReaderByParams.dcMap.get(dc);
                        if(visitedDC.contains(dc))continue;
                        if(dc.contains(ind)){
                            //���incid������ȼ����Ԫ���Ƿ���������atom
                            //�ֱ����������������
                            visitedDC.add(dc);
                            checkOtherAtoms(violationList,fileReaderByParams,equivalenceIndex.get(attr).get(val),incId,ind,dc,dcId ,true);
                            checkOtherAtoms(violationList,fileReaderByParams,equivalenceIndex.get(attr).get(val),incId,ind,dc,dcId,false);
                        }
                    }
                }

            }
        }
    }

    /**
     * ��������ɸѡ�������ν�ʣ�����Ƿ�����
     */

    private void checkOtherAtoms(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> originIds, Integer incId, String ind,String dc,int dcId,boolean isLeft) {
        for(Integer originId:originIds){
            boolean satisfy = satisfyOtherAtoms(fileReaderByParams,dc,ind,isLeft,originId,incId,fileReaderByParams.fileParams.table);
            if(satisfy){
                if(isLeft)violationList.add(new Violation(incId+fileReaderByParams.originIdList.size(),originId,dcId));
                else {
//                    System.out.println(fileReaderByParams.originList.get(originId).toString()+fileReaderByParams.incList.get(incId)+dc);
                    violationList.add(new Violation(originId,incId+fileReaderByParams.originIdList.size(),dcId));
                }
            }
        }
    }

    private boolean satisfyOtherAtoms(FileReaderByParams fileReaderByParams, String dc, String ind, boolean isLeft, Integer originId, Integer incId, String table) {
        boolean satisfy = true;
        Object originObj = fileReaderByParams.originList.get(originId);
        Object incObj = fileReaderByParams.incList.get(incId);
        for(String atom:dc.split("&")){
            if(!satisfy)break;
            if(ind.contains(atom))continue;
            String firstAttr = tools.getAttrByPre(atom,1);
            String secondAttr = tools.getAttrByPre(atom,2);
            if(isLeft){
                //������Ϊ���Ԫ�س���
                satisfy = tools.satisfyAtom(tools.getValueByAttr(incObj,firstAttr,table),tools.getValueByAttr(originObj,secondAttr,table),atom);
            }else{
                satisfy = tools.satisfyAtom(tools.getValueByAttr(originObj,firstAttr,table),tools.getValueByAttr(incObj,firstAttr,table),atom);
            }
        }
        return satisfy;
    }

}
