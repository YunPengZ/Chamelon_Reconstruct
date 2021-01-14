package Util;

import Detection.TreeNode;

import java.util.Comparator;
import java.util.List;

public class TreeNodeComparator implements Comparator<TreeNode> {
    List<Object> objs;
    String attr;
    String table;
    boolean isAsc;
    Tools tools;
    public TreeNodeComparator(List<Object> objs,String attr,String table,boolean isAsc) {
        this.objs = objs;
        this.attr = attr;
        this.table = table;
        this.isAsc = isAsc;
        tools = new Tools();
    }

    @Override
    public int compare(TreeNode t1, TreeNode t2) {
            if(t1.index==-1||t2.index==-1)return 0;
            String firstValue = tools.getValueByAttr(objs.get(t1.tupleId),attr,table);
            String secondValue = tools.getValueByAttr(objs.get(t2.tupleId),attr,table);
            if(isAsc){
                return Integer.parseInt(firstValue)-Integer.parseInt(secondValue);
            }else{
                return Integer.parseInt(secondValue)-Integer.parseInt(firstValue);

            }

    }
}
