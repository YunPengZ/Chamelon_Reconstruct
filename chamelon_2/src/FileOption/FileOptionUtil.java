package FileOption;

import Bean.*;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileOptionUtil {
    public List<Object> readList(File file, String table, int size) {
        /***
         * size表示increlist中id起始的数字
         * id:0-list.size:=list,id>=list.size:=incremental list
         */
        List<Object> list = new ArrayList<>();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader bfReader = new BufferedReader(isr);
            String lineText = null;
            int count = size;
            bfReader.readLine();//跳过第一行 即跳过行头
            switch (table) {
                case "hospital":
                    while ((lineText = bfReader.readLine()) != null) {
                        String arrs[] = lineText.split(",");
                        HospitalBean hospitalBean = new HospitalBean(count++,arrs);
                        list.add(hospitalBean);
                    }
                    // tableType = 1;
                    break;
                case "tax":
                    while ((lineText = bfReader.readLine()) != null) {
                        String arrs[] = lineText.split(",");
                        TaxBean taxBean = new TaxBean(count++,arrs);
                        list.add(taxBean);
                    }
                    break;
                case "spstock":
                    while ((lineText = bfReader.readLine())!=null){
                        String args[] = lineText.split(",");
                        SPStockBean spStockBean = new SPStockBean(count++,args);
                        list.add(spStockBean);
                    }
                    break;
                case "flight":
                    while ((lineText = bfReader.readLine())!=null){
                        String args[] = lineText.split(",");
                        FlightBean flightBean = new FlightBean(count++,args);
                        list.add(flightBean);
                    }
                    break;
                default:
                    break;
            }//对初处理的数据 获得
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void writeToFile(File resultFile, List<Violation> resultObj){
        try {
            if (!resultFile.exists()) {    //文件不存在则创建文件，先创建目录
                //File dir = new File(resultFile.getParent());
                resultFile.getParentFile().mkdirs();
                resultFile.createNewFile();
            }else{
                resultFile.delete();
            }
            FileWriter fw = new FileWriter(resultFile.getAbsoluteFile());
            BufferedWriter bfw = new BufferedWriter(fw);
            for(Violation violation:resultObj){
                StringBuilder sb = new StringBuilder();
                sb.append(violation);
                sb.append("\n");
                bfw.write(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Pair<String,HashMap<String,Integer>>> readRange(File file, String table_1) {
        InputStreamReader isr = null;
        List<Pair<String,HashMap<String,Integer>>> mapList = new ArrayList<>();
        try {
            isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf  = new BufferedReader(isr);
            String line = null;
            line = bf.readLine();
            for(String attr:line.split(",")){
                Pair<String,HashMap<String,Integer>> map = new Pair(attr,new HashMap<>());
                mapList.add(map);
            }
            while((line=bf.readLine())!=null){
                String[] lineSplit = line.split(",");
                for(int i = 0;i<lineSplit.length;i++){
                    int value  = 0;
                    if(mapList.get(i).getValue().containsKey(lineSplit[i])){
                        value = mapList.get(i).getValue().get(lineSplit[i])+1;
                        // mapList.get(i).getValue().put(lineSplit[i],);
                    }
                    mapList.get(i).getValue().put(lineSplit[i],value);
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapList;
    }

    public void writeRepairToFile(File resultFile, List<Pair<String, String>> cellRepairs) {
        System.out.println("repairs size:"+cellRepairs.size());
        try {
            if (!resultFile.exists()) {    //文件不存在则创建文件，先创建目录
                //File dir = new File(resultFile.getParent());
                resultFile.getParentFile().mkdirs();
                resultFile.createNewFile();
            }else{
                resultFile.delete();
            }
            FileWriter fw = new FileWriter(resultFile.getAbsoluteFile());
            BufferedWriter bfw = new BufferedWriter(fw);
            for(Pair<String,String> pair:cellRepairs){
                StringBuilder sb = new StringBuilder();
                sb.append(pair);
                sb.append("\n");
                bfw.write(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_temp_result(List<Violation> violations, File resultFile, List<Pair<String, String>> cellRepairs, int iter) {
        File file1 = new File( "D:\\scientic_project\\chameleon\\out\\result_1st_20k(temp).txt");
        File file2 = new File( "D:\\scientic_project\\chameleon\\out\\result_2nd_20k(temp).txt");
        if(iter==1){
            writeToFile(file1,violations);
        }
        if(iter==2){
            writeRepairToFile(resultFile,cellRepairs);
            writeToFile(file2,violations);
            System.out.println(cellRepairs);
        }
    }
}
