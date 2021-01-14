package FileOption;
import Util.Tools;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class FileReaderByParams implements Serializable {
    public FileParams fileParams;
    public List<Object> originList,incList,cleanList,dirtyList;
    private List<Object> totalList;
    public List<Integer> originIdList,incIdList;
    private List<Integer> totalIdList;
    public List<String> dcs;
    public Map<String,Integer> dcMap;
    public Tools tools;
    public FileReaderByParams(FileParams fileParams,List<String>dcs) {
        this.fileParams = fileParams;
        this.dcs = dcs;
        this.dcMap = new HashMap<>();
        buildDCMap();

        tools = new Tools();
    }

    public void buildDCMap() {
        for(int i = 0;i<dcs.size();i++){
            dcMap.put(dcs.get(i),i);
        }
    }

    public void readFile(){
        FileOptionUtil fileread = new FileOptionUtil();

        this.originList = fileread.readList(new File(fileParams.originDir),fileParams.table,0);
        System.out.println("origin read end s");
        int originSz = this.originList.size();
        this.incList = fileread.readList(new File(fileParams.incDir),fileParams.table,originSz);
        // 更新id
        this.originIdList = tools.get_id_list(originList,fileParams.table,0);
        this.incIdList = tools.get_id_list(incList,fileParams.table,originSz);
//        buildTotalList();
    }

    public void readCleanFile(){
        FileOptionUtil fileread = new FileOptionUtil();
        int originSz = this.originIdList.size();
        this.cleanList = fileread.readList(new File(fileParams.cleanDir),fileParams.table,originSz);
        this.dirtyList = fileread.readList(new File(fileParams.dirtyDir),fileParams.table,originSz);
    }

    private void buildTotalList() {
        this.totalList = new ArrayList<>(originList.size()+incList.size());
        this.totalList.addAll(originList);
        this.totalList.addAll(incList);
        this.totalIdList = new ArrayList<>(totalList.size());
        this.totalIdList.addAll(originIdList);
        this.totalIdList.addAll(incIdList);
    }

    @Override
    public String toString() {
        return "FileReaderByParams{" +
                "fileParams=" + fileParams +
                ", originList=" + originList +
                ", incList=" + incList +
                ", totalList=" + totalList +
                ", cleanList=" + cleanList +
                ", dirtyList=" + dirtyList +
                ", originIdList=" + originIdList +
                ", incIdList=" + incIdList +
                ", totalIdList=" + totalIdList +
                ", dcs=" + dcs +
                '}';
    }
}
