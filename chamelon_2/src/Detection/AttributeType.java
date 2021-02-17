package Detection;

import static Util.Tools.getAttrByPre;

public class AttributeType {
    String attrA,attrB,attrC,attrD;
    boolean isAscA,isAscB,isAscC,isAscD;

    public AttributeType(String attrA, String attrB, String attrC, String attrD, boolean isAscA, boolean isAscB, boolean isAscC, boolean isAscD) {
        this.attrA = attrA;
        this.attrB = attrB;
        this.attrC = attrC;
        this.attrD = attrD;
        this.isAscA = isAscA;
        this.isAscB = isAscB;
        this.isAscC = isAscC;
        this.isAscD = isAscD;
    }

    public AttributeType() {

    }

    public void setAttribute(String firstAtom, String secondAtom) {
         attrA = getAttrByPre(firstAtom,1);
         attrB = getAttrByPre(firstAtom,2);
         attrC = getAttrByPre(secondAtom,1);
         attrD = getAttrByPre(secondAtom,2);
        if(firstAtom.contains(">")){
            isAscA = isAscB  = true;
        }else isAscA = isAscB  = false;
        if(secondAtom.contains(">")){
            isAscC = isAscD = true;
        }else isAscC = isAscD = false;
    }
}
