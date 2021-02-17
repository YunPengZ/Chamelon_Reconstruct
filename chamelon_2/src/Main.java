import Entrance.Inc;
import IndexOption.IndexSet;
import Util.Tools;
import javafx.util.Pair;

import java.util.*;

class Test{
    Map<String,List<Integer>> dfs(List<Integer> nums){
        Map<String,List<Integer>> mp = new HashMap<>();
        for(int i = 0;i<10;i++){
            if(mp.containsKey("2"))mp.get("2").add(i);
            else{
                List<Integer> temp = new ArrayList<>();
                temp.add(i);
                mp.put("2",temp);
            }
        }
        return mp;
    }
    List<Integer> comparatorSort(List<Integer>nums){
        nums.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return t1-integer;
            }
        });
        return nums;
    }
    boolean noOtherAttr(String atom) {
        Tools tools = new Tools();
        String firstAttr = tools.getAttrByPre(atom, 1);
        String secondAttr = tools.getAttrByPre(atom, 2);
        if (firstAttr.equals(secondAttr)) {
            return true;
        }
        return false;
    }
    void strsSort(List<String> strs){
        strs.sort(new Comparator<String>() {
            @Override
            public int compare(String atom1, String atom2) {
                if(noOtherAttr(atom1))return -1;
                else if(noOtherAttr(atom2))return 1;
                return 0;
            }
        });
        for(String str:strs){
            System.out.println(str);
        }
    }
}

public class Main {
public static void main(String[] args) {

        String stockDCs[] = {
        "t1.High>t2.High&t1.High<t2.Close", "t1.Open<t2.Close&t1.Open>t2.High", "t1.Low<t2.Low&t1.Low>t2.Close"//, "t1.High<t1.Low",
//                "t1.Low>t1.Close", "t1.Low>t1.Close", "t1.High<t1.Close","t1.High<t1.Open"
        };
        String taxDcs[] = {
        "t1.State=t2.State&t1.Salary<t2.Salary&t1.Rate>t2.Rate"
        };
        String incSize = "5",errorRate = "3",dSize = "20",table="spstock";
        int type = 2;
        int errorAttrNum = 5;//hospital:3,tax:5,spstock
        boolean test = true;
        String rootPath = "D:\\scientic_project\\chameleon_2\\";
        boolean isChoosed = false;
        int repairType = 0;
        boolean byIndex = true;
        boolean onlyDelta = true;//
        boolean print  = false;
        System.out.println("A bin's modify");
//        String incSize = args[0],errorRate = args[1],dSize = args[2],table=args[3];
//        int type = Integer.parseInt(args[4]);
//        boolean test = Boolean.parseBoolean(args[5]);
//        String rootPath = args[6];
//        boolean isChoosed = Boolean.parseBoolean(args[7]);
//        boolean byIndex = Boolean.parseBoolean(args[8]);
//        boolean onlyDelta = Boolean.parseBoolean(args[9]);
//        int repairType = Integer.parseInt(args[10]);
////        dc 应该是从文件当中读取的
//        int errorAttrNum = Integer.parseInt(args[11]);
//        boolean print = Boolean.parseBoolean(args[12]);
        Inc inc = new Inc(incSize,errorRate,dSize,table,rootPath,type,repairType,errorAttrNum,test,isChoosed,byIndex,onlyDelta,print);
        inc.holistic("null");
        }
}
