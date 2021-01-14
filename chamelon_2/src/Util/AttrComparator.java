package Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AttrComparator implements Comparator<Integer> {
    List<Object> objs;
    String attr,table;
    boolean asc;
    Tools tools;
    public AttrComparator( List<Object> objs,String attr,String table,boolean isAsc){
        this.objs = objs;
        this.attr = attr;
        this.table = table;
        this.asc = isAsc;
        tools = new Tools();
    }

    @Override
    public int compare(Integer id1, Integer id2) {
        String val1 = tools.getValueByAttr(objs.get(id1),attr,table);
        String val2 = tools.getValueByAttr(objs.get(id2),attr,table);
        int valInt1 = Integer.parseInt(val1);
        int valInt2 = Integer.parseInt(val2);
        if(asc){
            return valInt1-valInt2;
        }else{
            return valInt2-valInt1;
        }
    }
}
