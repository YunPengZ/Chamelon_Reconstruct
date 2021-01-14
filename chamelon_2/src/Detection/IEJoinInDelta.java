package Detection;

import Bean.Violation;
import FileOption.FileReaderByParams;
import Util.AttrComparator;
import Util.Tools;
import javafx.util.Pair;

import java.util.*;

public class IEJoinInDelta {
    Tools tools;
    long buildIndexTime = 0;
    IEJoinInDelta(){
        tools = new Tools();
    }


    public void detect(List<Violation> violationList, FileReaderByParams fileReaderByParams, List<Integer> incIdList, Set<String> atomSet, String table,int dcId) {
            Set<String> otherAtomSet = new HashSet<>();
        //来到此处，必定包含两个或两个以上的不等于谓词
        Pair<String,String> pair= tools.getTwoInEqualAtoms(new ArrayList<>(atomSet),otherAtomSet);
        IEJoin(violationList,fileReaderByParams,incIdList,pair.getKey(),pair.getValue(),otherAtomSet,table,dcId);
    }

    private void IEJoin(List<Violation> violationList,FileReaderByParams fileReaderByParams, List<Integer> incIdList, String firstAtom, String secondAtom, Set<String> otherAtomSet,
                        String table,int dcId) {
        long start = System.currentTimeMillis();
        String X = tools.getAttrByPre(firstAtom,1);
        String Xb = tools.getAttrByPre(firstAtom,2);
        String Y = tools.getAttrByPre(secondAtom,1);
        String Yb = tools.getAttrByPre(secondAtom,2);
        List<Integer> L1,L1b,L2,L2b;
        L1 = new ArrayList<>(incIdList);
        L1b = new ArrayList<>(incIdList);
        L2 =  new ArrayList<>(incIdList);
        L2b = new ArrayList<>(incIdList);
        int m = L1.size(),n = L2.size();
        initSortList(fileReaderByParams.incList,L1,L1b,L2,L2b,X,Xb,Y,Yb,firstAtom,secondAtom,table);
        List<Integer> permuXY,permuXYb,offsetX,offsetY;
        permuXY = new ArrayList<>(incIdList.size());
        permuXYb = new ArrayList<>(incIdList.size());
        offsetX = new ArrayList<>(incIdList.size());
        offsetY= new ArrayList<>(incIdList.size());
        initPermuList(L1,L2,permuXY);
        initPermuList(L1b,L2b,permuXYb);
        tools.initOffsetList(fileReaderByParams.incList,L1,L1b,offsetX,X,Xb,table,firstAtom.contains("<"));//包含小于是升序
        tools.initOffsetList(fileReaderByParams.incList,L2,L2b,offsetY,Y,Yb,table,secondAtom.contains(">"));//第二个atom包含大于是升序
        long end = System.currentTimeMillis();
        buildIndexTime+=(end-start);
        boolean visited[] = new boolean[incIdList.size()];
        int equalOff;
        if(firstAtom.contains("=")&&secondAtom.contains("="))equalOff = 0;
        else equalOff = 1;
        for(int i = 0;i<m;i++){
            //C在D中的位置
            //C<D
            for(int j = 0;j<Math.min(offsetY.get(i),L2.size());j++){
//                System.out.println(incList.get(L2.get(i)).toString()+incList.get(L2b.get(j))+secondAtom);
                visited[permuXYb.get(j)] = true;
            }
            //A>B
            for(int k = offsetX.get(permuXY.get(i))+equalOff;k<n;k++){
                if(visited[k]){
                    if(tools.checkTuplePair(fileReaderByParams.incList,L2.get(i),L2b.get(k),otherAtomSet,table)){
//                        System.out.println(incList.get(L2.get(i)).toString()+incList.get(L2b.get(k)).toString()+dc);
                        violationList.add(new Violation(L2.get(i)+fileReaderByParams.originIdList.size(),L2b.get(k)+fileReaderByParams.originIdList.size(),dcId));
                    }
                }
            }
        }
    }



    private void initPermuList(List<Integer> L1, List<Integer> L2, List<Integer> permuXY) {
        //得到L2在L1中的位置
        for(Integer id:L2){
            permuXY.add(L1.indexOf(id));
        }
    }

    private void initSortList(List<Object> incList, List<Integer> L1, List<Integer> L1b, List<Integer> L2, List<Integer> L2b, String X, String Xb, String Y, String Yb, String firstAtom, String secondAtom, String table) {
        AttrComparator attrComparatorX,attrComparatorXb,attrComparatorY,attrComparatorYb;
        if (firstAtom.contains(">")) {
            attrComparatorX = new AttrComparator(incList,X,table,false);
            attrComparatorXb = new AttrComparator(incList,X,table,false);
        }else{
            attrComparatorX = new AttrComparator(incList,X,table,true);
            attrComparatorXb = new AttrComparator(incList,Xb,table,true);
        }
        if(secondAtom.contains(">")){
            attrComparatorY = new AttrComparator(incList,Y,table,true);
            attrComparatorYb = new AttrComparator(incList,Yb,table,true);
        }else{
            attrComparatorY = new AttrComparator(incList,Y,table,false);
            attrComparatorYb = new AttrComparator(incList,Yb,table,false);
        }
        L1.sort(attrComparatorX);
        L1b.sort(attrComparatorXb);
        L2.sort(attrComparatorY);
        L2b.sort(attrComparatorYb);
    }

    public long getBuildIndexTime() {
        return this.buildIndexTime;
    }
}
