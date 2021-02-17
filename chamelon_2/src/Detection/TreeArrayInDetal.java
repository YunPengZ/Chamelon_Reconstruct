package Detection;

import Bean.Violation;
import FileOption.FileReaderByParams;
import Util.AttrComparator;
import Util.Tools;
import Util.TreeNodeComparator;
import javafx.util.Pair;
import java.util.*;

import static Detection.TreeArrayTools.*;
import static Util.Tools.getAttrByPre;

public class TreeArrayInDetal {
    Tools tools;
    boolean print,useDump;
    long BuildIndexTime = 0;
    TreeArrayInDetal(boolean print, boolean useDump){
        tools = new Tools();
        this.print = print;
        this.useDump = useDump;
    }

    public void detect(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList, Set<String> atomSet, String table, int dcId) {
        //先判断谓词是否跨列
        //先按照谓词是否跨列对谓词分别排序
        long start = System.currentTimeMillis();
        Set<String> otherAtomSet = new HashSet<>();

        Pair<String,String> twoAtoms = getTwoAtoms(atomSet,otherAtomSet);
        String firstAtom = twoAtoms.getKey();
        String secondAtom = twoAtoms.getValue();
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        if(print)System.out.println("dc"+firstAtom+secondAtom);
        if(noOtherAttr(secondAtom)){
            //特殊情况的否定约束，利用特殊情况的解法来做。
            detectTreeArrayInParticular(violationList,fileReaderByParams,incIdList,otherAtomSet,firstAtom,secondAtom,table,dcId);
        }else{
            detectTreeArrayInGeneral(violationList,fileReaderByParams,incIdList,otherAtomSet,firstAtom,secondAtom,table,dcId);
        }
    }

    /***
     * 不同于IEJoin，atom包含>时候，LA，LB按照升学排列，LC,LD也按照升序排列，
     * 当atom包含<时候，LA按照降序排列
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
        //需要先维护树状数组的结构
        long start = System.currentTimeMillis();
        AttributeType attributeType = new AttributeType();
        attributeType.setAttribute(firstAtom,secondAtom);

        if(useDump){
            TreeArrayInDeltaDump treeArrayInDeltaDump = new TreeArrayInDeltaDump(otherAtomSet,attributeType,table,false);
            treeArrayInDeltaDump.detectionInGeneral(fileReaderByParams,violationList,incIdList,dcId,start);
            this.BuildIndexTime = treeArrayInDeltaDump.getBuildIndexTime();
        }else{
            TreeArrayInDeltaSingle treeArrayInDeltaSingle = new TreeArrayInDeltaSingle(otherAtomSet,attributeType,table);
            treeArrayInDeltaSingle.detectInGeneral(fileReaderByParams,violationList,incIdList,dcId);
            this.BuildIndexTime = treeArrayInDeltaSingle.getBuildIndexTime();
        }

    }


    private void detectTreeArrayInParticular(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList,
                                             Set<String> otherAtomSet, String firstAtom, String secondAtom,String table,int dcId) {
        //对列的排序方式和IEJoin一致
        if(print)System.out.println("particular tree array");
//        System.out.println(firstAtom);
        long start = System.currentTimeMillis();

        String attrA = getAttrByPre(firstAtom,1);
        String attrB = getAttrByPre(secondAtom,1);
        boolean isAscA = firstAtom.contains(">");
        boolean isAscB = secondAtom.contains(">");
        if(useDump){
            TreeArrayInDeltaDump treeArrayInDeltaDump = new TreeArrayInDeltaDump(otherAtomSet,attrA,attrB,table,isAscA,isAscB,print);
            treeArrayInDeltaDump.detectionInParticular(fileReaderByParams,violationList,incIdList,dcId,start);
            this.BuildIndexTime = treeArrayInDeltaDump.getBuildIndexTime();
        }else{
            TreeArrayInDeltaSingle treeArrayInDeltaSingle = new TreeArrayInDeltaSingle();
            treeArrayInDeltaSingle.detectInParticular(fileReaderByParams,violationList,incIdList,dcId,start);
            this.BuildIndexTime = treeArrayInDeltaSingle.getBuildIndexTime();
        }

    }

    public long getBuildIndexTime() {
        return this.BuildIndexTime;
    }
}
