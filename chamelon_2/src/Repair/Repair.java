package Repair;

        import Bean.Assignment;
        import Bean.Violation;
        import IndexOption.IndexSet;
        import Util.Tools;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Map;
        import java.util.Objects;

public class Repair
{
    Tools tools = new Tools();
    int error_rate;
    int errorAttrNums;

    public Repair(int error_rate, int errorAttrNums) {
        this.error_rate = error_rate;
        this.errorAttrNums = errorAttrNums;
    }

    public void run(IndexSet indexSet, List<Violation> violationList, int repairType) {
        //执行修复算法
        long start = System.currentTimeMillis();
        //计算修复结果 precision、recall、和f1
        RepairResult result = new RepairResult();
        if(repairType==1){
            //holistic
            HolisticRepair holisticRepair = new HolisticRepair();
            holisticRepair.repair(indexSet,violationList);
            result.setResult(new ArrayList<>(holisticRepair.result.entrySet()));
        }else if(repairType==2){
            VFreeRepair vFreeRepair = new VFreeRepair();
            vFreeRepair.repair(indexSet,violationList);
            result.setResult(vFreeRepair.result);
        }else if(repairType==3){
            IncRepair incRepair = new IncRepair();
            incRepair.repair(indexSet,violationList);
            result.setResult(incRepair.result);
        }
        long end = System.currentTimeMillis();
        indexSet.fileReaderByParams.readCleanFile();
        calcZB(indexSet,result);
        tools.printRunTime(start,end,"repair");

        long updateEnd = System.currentTimeMillis();
        tools.printRunTime(end,updateEnd,"update data");
    }

    //holistic 修复是以全局为单位，但是由于我们错误的cell只属于增量数据集，因此在计算时候，只考虑增量中的cell
    private void calcZB(IndexSet indexSet, RepairResult result) {
        int repairSz = result.result.size();
        int incSz = indexSet.fileReaderByParams.incList.size();
        int dSz = indexSet.fileReaderByParams.originIdList.size();
        int repairInOrigin = 0;
        double rightRepairInDelta = 0.0;
        int repairTruthDistance = 0;
        int repairNoiseDistance = 0;
        int truthNoiseDistance  = 0;
        int squareDistance = 0;
        int numericChangedNums = 0;
        String table = indexSet.fileReaderByParams.fileParams.table;
        for (Map.Entry<String,String> entry:result.result){
            int tupleId = Integer.parseInt(entry.getKey().split(",")[0]);
            String attr = entry.getKey().split(",")[1];
            String cleanVal = "";
            String dirtyVal = "";
            if(tupleId<dSz){
                cleanVal = tools.getValueByAttr(indexSet.fileReaderByParams.originList.get(tupleId),attr,table);
                dirtyVal = cleanVal;//d中的clean 和dirty 两值相同
            }else{
                tupleId -=dSz;
                cleanVal= tools.getValueByAttr(indexSet.fileReaderByParams.cleanList.get(tupleId),attr,table);
                dirtyVal = tools.getValueByAttr(indexSet.fileReaderByParams.dirtyList.get(tupleId),attr,table);
            }
            boolean calcDistance = true;
            if(entry.getValue().equals("-1")||entry.getValue().equals("")){
                calcDistance = false;
            }
            if(!tools.isNumeric(dirtyVal))calcDistance = false;
            //无论是否修复正确都要计算mnad 只有赋fv时 不计算
            if(tupleId<dSz){
                repairInOrigin++;
                //d中的cell
            }else{
                if(!cleanVal.equals(dirtyVal)){
                    if(cleanVal.equals(entry.getValue()))rightRepairInDelta++;
                        //发现并被赋新值的都计分
                    else if(entry.getValue().equals("-1")||entry.getValue().equals("")){
                        rightRepairInDelta+=0.5;
                    }
                }
            }
            if(calcDistance){
                int cleanValInt = Integer.parseInt(cleanVal);
                int repairValInt = Integer.parseInt(entry.getValue());
                int noiseValInt = Integer.parseInt(dirtyVal);
                int  base = cleanValInt-repairValInt;
                repairTruthDistance+=Math.abs(cleanValInt-repairValInt);
                repairNoiseDistance+=Math.abs(noiseValInt-repairValInt);
                truthNoiseDistance +=Math.abs(noiseValInt-cleanValInt);
                squareDistance +=(base*base);
            }
        }
        for(int i = 0;i<80;i++) System.out.print("#");
        System.out.println();
        System.out.println("right repair nums:"+rightRepairInDelta);
        System.out.println("changed cells num (in delta d):"+(repairSz-repairInOrigin));
        System.out.println("changed cells num (total):"+repairSz);
        if (rightRepairInDelta==0){
            System.out.println("div equals 0,end..");
        }else{
            double precision = rightRepairInDelta*1.0/repairSz;
            double recall = rightRepairInDelta*1.0/(incSz*error_rate*0.01*errorAttrNums);///3*3/
            double f1 = 2*precision*recall/(precision+recall);
            System.out.println("precision is "+precision+" recall is:"+recall+" f1"+f1);
            System.out.println("relative accuracy(song2016):"+(1-repairTruthDistance*1.0/(repairNoiseDistance+truthNoiseDistance)));

        }
        System.out.println("distance is:"+repairTruthDistance+" squared distance is： "+squareDistance);
        System.out.println("mean normalized absolute distance(MNAD) is:"+repairTruthDistance*1.0/(repairSz-repairInOrigin));
    }


}
