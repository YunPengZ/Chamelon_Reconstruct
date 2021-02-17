package Entrance;

import Bean.Violation;
import Detection.IncDetection;
import FileOption.FileParams;
import FileOption.FileReaderByParams;
import IndexOption.IndexReader;
import IndexOption.IndexSet;
import Repair.Repair;
import Util.Tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static Util.Tools.*;

public class Inc {
    String rootPath;
    String incSize,errorRate,dSize,table;
    String originDir,increDir,dirtyDir,cleanDir,detectionType;
    int type,repairType,errorAttrNum;
    boolean test,isChoosed,byIndex,onlyDelta,print;//���Ϊ�棬�����Ĵ���������ֵģ���Ȼ����������ʱ�������

    public Inc(String incSz, String erroreRt, String dSz, String table, String rootPath, int type, int repairType, int errorAttrNum,
               boolean test, boolean isChoosed, boolean byIndex, boolean onlyDelta, boolean print){
        incSize = incSz;
        errorRate = erroreRt;
        dSize = dSz;
        this.repairType = repairType;
        this.rootPath = rootPath;
        this.table = table;
        this.type = type;
        this.test = test;
        this.isChoosed = isChoosed;
        this.byIndex = byIndex;
        this.onlyDelta = onlyDelta;
        this.errorAttrNum = errorAttrNum;
        this.print = print;
    }

    public void holistic(String dType){
        Tools tools = new Tools();
        List<Violation> violationList = new ArrayList<>();
        System.out.println("table sz:"+table);
        List<String> dcs = readDC(rootPath,table);
        setDir(dType);
        FileParams fileParams = new FileParams(originDir,increDir,cleanDir,dirtyDir,dSize,errorRate,incSize,table,detectionType);
        FileReaderByParams fileReaderByParams = new FileReaderByParams(fileParams,dcs);
        long start = System.currentTimeMillis();
        fileReaderByParams.readFile();//��ȡ����
        long readEnd = System.currentTimeMillis();
        tools.printRunTime(start,readEnd,"readFile ");
        //holistic �㷨�����������ṹʵ�ּ�ⲿ�ֹ���
        IndexSet indexSet = new IndexSet(test);
        IndexReader indexReader = new IndexReader();
        indexSet = indexReader.readIndex(indexSet,fileReaderByParams,rootPath,table,dSize,isChoosed,byIndex,onlyDelta);
        System.out.println("choosed set:"+indexSet.choosedSet);
        System.out.println("inc sz"+indexSet.fileReaderByParams.incList.size());
        IncDetection incDetection = new IncDetection();
        incDetection.detect(violationList,indexSet,type,byIndex,onlyDelta,print);
        int dSZInt = fileReaderByParams.originIdList.size();
        int i = 0;
        for(Violation violation:violationList){
            String dc = fileReaderByParams.dcs.get(violation.getDc());
            int left = violation.getFirst()-dSZInt;
            int right = violation.getSecond()-dSZInt;
            for(String atom:dc.split("&")){
                String firstAttr = getAttrByPre(atom,1);
                String secondAttr = getAttrByPre(atom,2);
                String firstVal = getValueByAttr(fileReaderByParams.incList.get(left),firstAttr,table);
                String secondVal = getValueByAttr(fileReaderByParams.incList.get(right),secondAttr,table);
                if (!satisfyAtom(firstVal,secondVal,atom)){
                    System.out.println(firstVal+",right:"+secondVal+",dc:"+dc+",atom"+atom);
                    i++;

                }
            }
        }
        System.out.println(i);
        Repair repair = new Repair(Integer.parseInt(errorRate),errorAttrNum);
        if(repairType!=0)repair.run(indexSet,violationList,repairType);
        //holistic �㷨
//        holistic(list,increList,d_id_list,incre_id_list,sub_violations_map,index_params_map,params_map,index_type,tree_params,
//                dcs,listsz,total_lilst,cellRepairs,equivalence_index,iter,table,dirty_list,clean_list,error_rate);
    }

    private void checkViolation(List<Violation> violationList, List<String> dcMap) {

    }

    private List<String> readDC(String rootPath, String table) {
        List<String> res = new ArrayList<>();
        String dcPath = rootPath+"dc\\"+table;
        File file = new File(dcPath);
        InputStreamReader isr = null;
        String lineText = null;
        try {
            isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader bfReader = new BufferedReader(isr);
            while ((lineText = bfReader.readLine()) != null) {
                res.add(lineText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private void setDir(String dType) {
        originDir = rootPath+"dataSet\\source_d\\"+table+dSize+".txt";
//        fileDir = "D:\\scientic_project\\chameleon\\dataSet\\source_d\\" + table+"100.txt";
        increDir = rootPath+"dataSet\\delta_d\\"+table +"\\"+table+"_incre_"+incSize+"k_error_rate_"+errorRate+".txt";
        dirtyDir = rootPath+"dataSet\\delta_d\\"+table+"\\"+table+"_incre_"+incSize+"k_mis_error_rate_"+errorRate+".txt";
        cleanDir = rootPath+"dataSet\\delta_d\\"+table+"\\"+table+"_incre_"+incSize+"k_clean.txt";
        detectionType = dType;
        System.out.println(originDir + increDir);

    }
}
// naive
// 1016084
//2039354
//iejoin
// 1031123
//2066210

//1021717
//2051417
//3028601

