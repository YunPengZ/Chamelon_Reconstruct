package Detection;

import Bean.Violation;
import FileOption.FileReaderByParams;
import Util.AttrComparator;
import Util.TreeNodeComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static Detection.TreeArrayTools.lowBit;
import static Util.Tools.*;

public class TreeArrayInDeltaSingle {
    private Set<String> otherAtomSet;
    private String attrA,attrB,attrC,attrD,table;
    private boolean isAscA,isAscB,isAscC,isAscD;
    private long BuildIndexTime = 0;

    public TreeArrayInDeltaSingle() {

    }


    public TreeArrayInDeltaSingle(Set<String> otherAtomSet, AttributeType attributeType, String table) {
        this.otherAtomSet = otherAtomSet;
        this.attrA = attributeType.attrA;
        this.attrB = attributeType.attrB;
        this.attrC = attributeType.attrC;
        this.attrD = attributeType.attrD;
        this.isAscA = attributeType.isAscA;
        this.isAscB = attributeType.isAscB;
        this.isAscC = attributeType.isAscC;
        this.isAscD = attributeType.isAscD;
        this.table = table;
    }


    public long getBuildIndexTime() {
        return BuildIndexTime;
    }

    public void detectInGeneral(FileReaderByParams fileReaderByParams, List<Violation> violationList, List<Integer> incIdList, int dcId) {
        long start = System.currentTimeMillis();
        List<Integer> La,Lb;
        La =new ArrayList<>(incIdList);
        Lb= new ArrayList<>(incIdList);

        AttrComparator attrComparatorA = new AttrComparator(fileReaderByParams.incList,attrA,table,isAscA);
        AttrComparator attrComparatorB = new AttrComparator(fileReaderByParams.incList,attrB,table,isAscB);
//        System.out.println("A is asc:"+isAscA);
        La.sort(attrComparatorA);
        Lb.sort(attrComparatorB);
        List<Integer> offsetAB = new ArrayList<>(incIdList.size());
        initOffsetList(fileReaderByParams.incList,La,Lb,offsetAB,attrA,attrB,table,isAscA);
        List<TreeNode> nodes = new ArrayList<>(incIdList.size()+1);
        buildTreeArray(nodes,fileReaderByParams.incList,Lb,attrD,table,isAscD);
        long end = System.currentTimeMillis();
        this.BuildIndexTime+=(end-start);
        //计算offset
        //之后要比较一下计算offset和直接做二分查找的时间差异
        for (int i =0;i<incIdList.size();i++){
            int k = offsetAB.get(i);
//            int left = Integer.parseInt(getValueByAttr(fileReaderByParams.incList.get(La.get(i)),attrA,table));
//            int right = Integer.parseInt(getValueByAttr(fileReaderByParams.incList.get(Lb.get(k)),attrB,table));
            int index = k+1;//在树状数组中对应的下标
            if(index>=nodes.size()){
                index = nodes.size()-1;
            }
            while(index>0){//在ids中搜索满足条件的元组
                String value = getValueByAttr(fileReaderByParams.incList.get(La.get(i)),attrC,table);
                int j = lowwerBoundByAttr(fileReaderByParams.incList,nodes.get(index).ids,attrD,value,table,isAscC);
                for(int m = 0;m<j;m++){
                    if(checkTuplePair(fileReaderByParams.incList,La.get(i),nodes.get(index).ids.get(m),otherAtomSet,table)){
                        violationList.add(new Violation(La.get(i)+fileReaderByParams.originIdList.size(),nodes.get(index).ids.get(m)+fileReaderByParams.originIdList.size(),dcId));
                    }
                }
                index-=lowBit(index);
            }
        }
    }
    //对应paper中buildTree方法
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

    public void detectInParticular(FileReaderByParams fileReaderByParams, List<Violation> violationList, List<Integer> incIdList, int dcId, long start) {
    }
}
