package Util;

import Bean.FlightBean;
import Bean.HospitalBean;
import Bean.SPStockBean;
import Bean.TaxBean;
import FileOption.FileReaderByParams;
import IndexOption.IndexSet;
import javafx.util.Pair;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools implements Serializable {
    public int getTupleID(Object object, String table) {
        int tuple_id = -1;
        switch (table){
            case "hospital":
                tuple_id = ((HospitalBean)object).getId();
                break;
            case "spstock":
                tuple_id = ((SPStockBean)object).getId();
                break;
            case "tax":
                tuple_id = ((TaxBean)object).getId();
                break;
            case "flight":
                tuple_id = ((FlightBean)object).getId();
                break;
        }
        return tuple_id;
    }
    public List<Integer> get_id_list(List<Object> list, String tableType,int originSz) {
        List<Integer> id_list = new ArrayList<>();
        for (Object object:list){
            id_list.add(getTupleID(object,tableType)-originSz);
        }
        return id_list;
    }
    public boolean satisfyAtom(String firstVal,String secondVal, String atom) {
        if(atom.contains("!=")){
            return !firstVal.equals(secondVal);
        } else if(atom.contains(">")||atom.contains("<")){
            return satisfyInEqualAtom(firstVal,secondVal,atom);
        }else if(atom.contains("=")){
            return firstVal.equals(secondVal);
        }
        return false;
    }


    public boolean satisfyInEqualAtom(String firstVal, String secondVal, String atom) {
        int first_val_int = Integer.parseInt(firstVal);
        int second_val_int = Integer.parseInt(secondVal);
        if(atom.contains("=")&&firstVal.equals(secondVal))return true;
        if (atom.contains(">")&&first_val_int>second_val_int){
            return true;
        }else if(atom.contains("<")&&first_val_int<second_val_int){
            return true;
        }
        return false;
    }

    public String getAttrByPre(String key,int attrPos) {//attrPos=1 那么匹配t1的属性
        String attr = null;
        String regx = "\\.[a-zA-Z_]+";//会匹配四次 attr_a是第一次 attr_b是第2次
        Pattern r = Pattern.compile(regx);
        Matcher matcher =r.matcher(key);
        while(matcher.find()&&attrPos!=0){
            attr = matcher.group().substring(1);
            attrPos--;
        }
        return attr;
    }

    public Map<String, Map<String, Integer>> getCountMap(List<Object> object_list,List<Integer> list, String table) {
        Map<String,Map<String,Integer>> map = new HashMap<>();
        Tools tools = new Tools();
        List<String> fields = new ArrayList<>();
        if(list.size()>0){
            for(Field field:object_list.get(list.get(0)).getClass().getDeclaredFields()){
                fields.add(field.getName());
            }
        }
        for(String field:fields){
            Map<String,Integer> field_map = new HashMap<>();
            for(Integer index:list){
                String temp = tools.getValueByAttr(object_list.get(index),field,table);
                Integer count = field_map.get(temp);
                field_map.put(temp,(count==null)?1:count+1);
            }
            if(!map.containsKey(field)){
                map.put(field,field_map);
            }
        }
        return map;
    }

    public String getValueByAttr(Object object, String attr, String table) {
        String res = null;
        switch (table){
            case "hospital":
                res = ((HospitalBean)object).get(attr);
                break;
            case "spstock":
                res = ((SPStockBean)object).get(attr);
                break;
            case "tax":
                res = ((TaxBean)object).get(attr);
                break;
            case "flight":
                res = ((FlightBean)object).get(attr);
                break;
        }
        return res;
    }

    public Map<String, List<Integer>> getEquiMap(List<Object> list, List<Integer> ids, String attr, String table) {
        Map<String,List<Integer>> mp = new HashMap<>();
//        System.out.println(attr);
        for(Integer id:ids){
            String val = getValueByAttr(list.get(id),attr,table);
            if(mp.containsKey(val)){
                mp.get(val).add(id);
            }else{
                List<Integer> temp = new ArrayList<>();
                temp.add(id);
                mp.put(val,temp);
            }
        }
        return mp;
    }

    /**
     * 适合于非递减序列，lowwerbound表示第一个大于等于的元素位置
     * 寻找非递增序列的 第一个小于等于value的元素位置
     * 两个方法，适用于重载
     */
    public int lowwerBoundByAttr(List<Object> list, List<Integer> ids, String attrD, String value, String table) {
        int target = Integer.parseInt(value);
        int l = 0,r = ids.size();
        while(l<r){
            int m = l+((r-l)>>1);
            int midValue = Integer.parseInt(getValueByAttr(list.get(ids.get(m)),attrD,table));
                if(midValue>=target)r = m;
                else l = m+1;
        }

        return l;
    }
    public int lowwerBoundByAttr(List<Object> list, List<Integer> ids, String attrD, String value, String table,boolean isAsc) {
        int target = Integer.parseInt(value);
        int l = 0,r = ids.size();
        while(l<r){
            int m = l+((r-l)>>1);
            int midValue = Integer.parseInt(getValueByAttr(list.get(ids.get(m)),attrD,table));
            if(isAsc){
                if(midValue>=target)r = m;
                else l = m+1;
            }else{
                if(midValue<=target)r = m;
                else l = m+1;
            }
        }
        return l;
    }

    public int upperBoundByAttr(List<Object> list, List<Integer> ids, String attrD, String value, String table) {
        int target = Integer.parseInt(value);
        int l = 0,r = ids.size();
        while(l<r){
            int m = l+((r-l)>>1);
            int midValue = Integer.parseInt(getValueByAttr(list.get(ids.get(m)),attrD,table));
            if(midValue<=target)l = m+1;
            else r=m;
        }
        return l;
    }

    public int lowwerBound(List<Integer> nums, String target) {
        int value = Integer.parseInt(target);
        int l = 0,r = nums.size();
        while(l<r){
            int m = l+((r-l)>>1);
            if(nums.get(m)<=value)r = m;
            else l = m+1;
        }
        return l;
    }

    public int upperBound(List<Integer> nums, String target) {
        int value = Integer.parseInt(target);
        int l = 0,r = nums.size();
        while(l<r){
            int m = (l+r)/2;
            if(nums.get(m)<=value) l = m+1;
            else    r = m;
        }
        return l;
    }

    public Pair<String,String> getTwoInEqualAtoms(List<String> atomSet, Set<String> otherAtomSet) {
        String firstAtom = "",secondAtom = "";
        for(String atom:atomSet){
            if(atom.contains(">")||atom.contains("<")){
                if(firstAtom.equals("")){
                    firstAtom = atom;
                }else if(secondAtom.equals(""))secondAtom = atom;
                else otherAtomSet.add(atom);
            }else otherAtomSet.add(atom);
        }

        return new Pair<String,String>(firstAtom,secondAtom);
    }
    public boolean checkTuplePair(List<Object> incList, Integer idLeft, Integer idRight, Set<String> otherAtomSet, String table) {
        for(String atom:otherAtomSet){
            String firstAttr = getAttrByPre(atom,1);
            String secondAttr = getAttrByPre(atom,2);
            String firstValue = getValueByAttr(incList.get(idLeft),firstAttr,table);
            String secondValue = getValueByAttr(incList.get(idRight),secondAttr,table);
            if(!satisfyAtom(firstValue,secondValue,atom)){
                return false;
            }
        }
        return true;
    }
    /**
     * IEJoin中并未指定计算offset的方式，只是提出是以线性时间计算
     */
    public void initOffsetList(List<Object> incList, List<Integer> L1, List<Integer> L1b, List<Integer> offsetX, String X, String Xb,String table,boolean isAsc) {
        //L1和L1b要么同为升序要么同为降序
        int pos = 0;
        for(Integer id:L1){
            int val = Integer.parseInt(getValueByAttr(incList.get(id),X,table));
//            System.out.println(incList.get(id).toString()+incList.get(L1b.get(pos))+X+Xb);
            //第一个大于的元素
            if(isAsc){
                /**
                 * offset是第一个大于元素的元组下标，所以可能会存在pos == L1b.size()
                 */
                while(pos<L1b.size()&&Integer.parseInt(getValueByAttr(incList.get(L1b.get(pos)),Xb,table))<val){
                    pos++;
                }
            }else{
                /**
                 * 对于降序的
                 * 如
                 * 10 5 4  ---》 11  10 6 4
                 * 映射数组应为
                 * 2 4 4
                 */
                while(pos<L1b.size()&&Integer.parseInt(getValueByAttr(incList.get(L1b.get(pos)),Xb,table))>val){
                    pos++;
                }
            }
            offsetX.add(pos);
        }
    }

    public void printRunTime(long start, long end,String prefix) {
        int s = 1000;
        int min = s*60;
        System.out.println(String.format(prefix+" progress costs %dmin%ds%dms,total %d",(end-start)/min,((end-start)/s)%60,(end-start)%s,(end-start)));
    }


    public void printType(int type) {
        if(type==1){
            System.out.println("naive detect in delta d ");
        }else if(type==2){
            System.out.println("IEJoin detect in delta d ");
        }else if(type==3){
            System.out.println("TreeArray detect in delta d");
        }else if(type==4){
            System.out.println("VioFinder detect in delta d");
        }else{
            System.err.println("Error Detection Type");
        }
    }

    public int getOperator(String atom) { //取逆等于7-operator
        if(!atom.contains("t2"))return 8;
        if(atom.contains("!")){
            return 1;//不等于
        }else if(atom.contains(">")){
            if(atom.contains("=")) return 2;
            return 3;//大于
        }else if(atom.contains("<")){
            if(atom.contains("=")) return 4;
            return 5;

        }else if(atom.contains("=")){
            return 6;
        }
        return 7;
    }

    public int getReverseOperator(int operator) {
        return 7-operator;
    }


    public void updateCellVal(Object obj, String attr,String val, String table) {
        //这部分代码写的是真的屎，重构的时候记得用多态
        String upperAttr = attr.substring(0,1).toUpperCase()+attr.substring(1);
        String setter = "set"+attr.substring(0,1).toUpperCase()+attr.substring(1);
        try {
            if(table=="hospital") {
                if (obj.getClass().getDeclaredField(upperAttr).getType() == String.class) {
                    ((HospitalBean) obj).getClass().getDeclaredMethod(setter, String.class).invoke(obj, val);
                } else if (obj.getClass().getDeclaredField(upperAttr).getType() == int.class) {
                    ((HospitalBean) obj).getClass().getDeclaredMethod(setter, int.class).invoke(obj, Integer.parseInt(val));
                }
            }
            else if(table=="spstock"){
                if (obj.getClass().getDeclaredField(upperAttr).getType() == String.class) {
                    ((SPStockBean) obj).getClass().getDeclaredMethod(setter, String.class).invoke(obj, val);
                } else if (obj.getClass().getDeclaredField(upperAttr).getType() == int.class) {
                    ((SPStockBean) obj).getClass().getDeclaredMethod(setter, int.class).invoke(obj, Integer.parseInt(val));
                }
            }else if(table=="tax"){
                if (obj.getClass().getDeclaredField(upperAttr).getType() == String.class) {
                    ((TaxBean) obj).getClass().getDeclaredMethod(setter, String.class).invoke(obj, val);
                } else if (obj.getClass().getDeclaredField(upperAttr).getType() == int.class) {
                    ((TaxBean) obj).getClass().getDeclaredMethod(setter, int.class).invoke(obj, Integer.parseInt(val));
                }
            }else if(table=="flight"){
                if (obj.getClass().getDeclaredField(upperAttr).getType() == String.class) {
                    ((FlightBean) obj).getClass().getDeclaredMethod(setter, String.class).invoke(obj, val);
                } else if (obj.getClass().getDeclaredField(upperAttr).getType() == int.class) {
                    ((FlightBean) obj).getClass().getDeclaredMethod(setter, int.class).invoke(obj, Integer.parseInt(val));
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public String getValueByCell(IndexSet indexSet, String key, String table) {
        int tupleId = Integer.parseInt(key.split(",")[0]);
        String attr = key.split(",")[1];
        int dSz = indexSet.fileReaderByParams.originList.size();
        if(tupleId<dSz){
            return getValueByAttr(indexSet.fileReaderByParams.originList.get(tupleId),attr,table);
        }else{
            return getValueByAttr(indexSet.fileReaderByParams.incList.get(tupleId-dSz),attr,table);
        }

    }

    public Object getObj(IndexSet indexSet, int tupleId) {
        int dSz = indexSet.fileReaderByParams.originIdList.size();
        if(tupleId>=dSz){
            tupleId-=dSz;
            return indexSet.fileReaderByParams.incList.get(tupleId);
        }else{
            return indexSet.fileReaderByParams.originList.get(tupleId);
        }
    }

    public boolean isNumeric(String cleanVal) {
        try {
            Integer.parseInt(cleanVal);
        }catch (Exception e){
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String concatDCWithReverse(String dc, String reverAtom) {
        List<String> reverse = new ArrayList<>();
        for(String atom:dc.split("&")){
            if(atom.equals(reverAtom)){
                reverse.add(reverseAtom(reverAtom));
            }else{
                reverse.add(atom);
            }
        }
        return String.join("&",reverse);
    }

    private String reverseAtom(String reverAtom) {
        Map<Integer,String> operatorMap = new HashMap<>();
        operatorMap.put(1,"!=");
        operatorMap.put(2,">");
        operatorMap.put(3,">=");
        operatorMap.put(4,"<");
        operatorMap.put(5,"<=");
        operatorMap.put(6,"=");
        int operator = getOperator(reverAtom);
        return reverAtom.replace(operatorMap.get(operator),operatorMap.get(7-operator));
    }

    public boolean isOrigin(String cell,int dSz) {
        int tupleId  = Integer.parseInt(cell.split(",")[0]);
        if(tupleId>=dSz)return false;
        return true;
    }

    public boolean satisfyIdAtom(IndexSet indexSet, Integer tupleId, Integer tupleId2, String atom, int dcId) {
        String firstAttr = getAttrByPre(atom,1);
        String secondAttr  = getAttrByPre(atom,2);
        String firstCell = tupleId+","+firstAttr;
        String secondCell = tupleId2+","+secondAttr;
        String firstVal = getValueByCell(indexSet, firstCell,indexSet.fileReaderByParams.fileParams.table);
        String secondVal = getValueByCell(indexSet, secondCell,indexSet.fileReaderByParams.fileParams.table);
        return satisfyAtom(firstVal,secondVal,atom);
    }
}
