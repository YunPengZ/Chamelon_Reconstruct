package Detection;

import javafx.util.Pair;

import java.util.*;

import static Util.Tools.getAttrByPre;
import static Util.Tools.getTwoInEqualAtoms;

public class TreeArrayTools {
    public static Pair<String,String> getTwoAtoms(Set<String> atomSet, Set<String> otherAtomSet){
        List<String> atoms = new ArrayList<>(atomSet);
        atoms.sort(new Comparator<String>() {
            @Override
            public int compare(String atom1, String atom2) {
                if(noOtherAttr(atom1))return -1;
                else if(noOtherAttr(atom2))return 1;
                return 0;
            }
        });
        return getTwoInEqualAtoms(atoms,otherAtomSet);

    }
    public static boolean noOtherAttr(String atom) {
        String firstAttr = getAttrByPre(atom,1);
        String secondAttr = getAttrByPre(atom,2);
        if(firstAttr.equals(secondAttr)){
            return true;
        }
        return false;
    }

    public static int lowBit(int index) {
        return index&(-index);
    }

}
