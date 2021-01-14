package Detection;

import Bean.Violation;
import FileOption.FileReaderByParams;
import Util.Tools;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaiveDetectionInDetal {

    public void detect(List<Violation> violationList,FileReaderByParams fileReaderByParams, List<Integer> incIdList, Set<String> otherAtomSet,String table,int dcId) {
        if(otherAtomSet.size()==0)return ;
        Tools tools = new Tools();

        for(Integer incId1:incIdList){
            for(Integer incId2:incIdList){
                Object obj1 = fileReaderByParams.incList.get(incId1);
                Object obj2 = fileReaderByParams.incList.get(incId2);
                if(incId1.equals(incId2))continue;
                //两个元组对
                boolean satisfy = true;
                for(String atom:otherAtomSet){
                    if(!satisfy)break;
                    String attr1 = tools.getAttrByPre(atom,1);
                    String attr2 = tools.getAttrByPre(atom,2) ;
                    satisfy = tools.satisfyAtom(tools.getValueByAttr(obj1,attr1,table),tools.getValueByAttr(obj2,attr2,table),atom);
                }
                if(satisfy)violationList.add(new Violation(incId1+fileReaderByParams.originIdList.size(),incId2+fileReaderByParams.originIdList.size(),dcId));
            }

        }
    }
}
