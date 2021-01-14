import java.io.*;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

class Main
{
    public int getOperator(String atom) { //取逆等于7-operator
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


    public boolean isNumeric(String cleanVal) {
        try {
            Integer.parseInt(cleanVal);
        }catch (Exception e){
//            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static void main (String[] args) throws java.lang.Exception
    {
        Main t = new Main();
        Set<String> otherAtomSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int atomType1 = t.getOperator(o1);
                int atomType2 =  t.getOperator(o2);
                return (atomType2<6&&atomType2>1)?1:-1;
            }
        });
        otherAtomSet.add("t1.State!=t2.State");
        otherAtomSet.add("t1.Rate>t2.Rate");
        for(String atom:otherAtomSet){
            System.out.println(atom);
        }
        t.isNumeric("9000012345");
        System.out.println("hi");
    }
}