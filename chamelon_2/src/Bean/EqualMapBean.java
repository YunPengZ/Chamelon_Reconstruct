package Bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqualMapBean implements Serializable {
    public String attr;
    public Map<String,EqualMapBean> mapNext;
    public Map<String, List<Integer>> mapCur;
    public boolean hasNextMap;
    public boolean isEqual;
    public EqualMapBean(String attr,boolean hasNextMap,boolean isEqual){
        this.hasNextMap = hasNextMap;
        this.attr = attr;
        this.isEqual = isEqual;
        if(hasNextMap){
            mapNext = new HashMap<>();
        }else{
            mapCur = new HashMap<>();
        }
    }

    @Override
    public String toString() {
        return "EqualMapBean{" +
                "attr='" + attr + '\'' +
                ", mapNext=" + mapNext +
                ", mapCur=" + mapCur +
                ", hasNextMap=" + hasNextMap +
                ", isEqual=" + isEqual +
                '}';
    }
}
