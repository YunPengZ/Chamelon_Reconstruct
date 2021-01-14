package IndexOption;

import Bean.EqualMapBean;
import Bean.HospitalBean;
import FileOption.FileReaderByParams;
import Util.SIzeofTools;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IndexReader {

    public IndexSet readIndex(IndexSet indexSet, FileReaderByParams fileReaderByParams, String rootPath, String table, String dSize,
                              boolean isChoosed, boolean byIndex, boolean onlyDelta) {
        String indexPath =String.format(rootPath+"dataSet\\source_d\\index_dsz%s_of_%s.db",dSize,table);
        System.out.println(indexPath);
        File indexFile = new File(indexPath);
        try {
            if(indexFile.exists()){
                //索引已经写入到文件中，直接读取，否则重新建立了索引
                System.out.println("index exists,readIndex");
                indexSet = (IndexSet) readObject(indexPath);
                indexSet.fileReaderByParams = fileReaderByParams;
            }else{
                if(!onlyDelta)indexSet.setIndex(fileReaderByParams,isChoosed,byIndex);
                indexSet.fileReaderByParams = null;
                writeObject(indexPath,indexSet);
                indexSet.fileReaderByParams = fileReaderByParams;
                //不写入原始数据
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(indexSet.fileReaderByParams.incList.size());
        try{
//            long indexSz = SIzeofTools.fullSizeOf(indexSet);
            long equalSz = calcEqualSize(indexSet.equalMapBean);
//            long equivSz = calcMapSize(indexSet.equivalenceIndex.values());
            long sortedListSz = calcMapSize(indexSet.sorted_list);
            long permuSz = calcMapSize(indexSet.permutation_list);
//            System.out.println("permutation"+indexSet.equivalenceIndex.values().size());
//            System.out.println("equivlence index size "+printSize(indexSet.fileReaderByParams.originList.size()*4L));
//            long dSz = SIzeofTools.getObjectSize(indexSet.fileReaderByParams.originList);
//            long incSz = SIzeofTools.getObjectSize(indexSet.fileReaderByParams.incList);
            long dSz = calcListSize(indexSet.fileReaderByParams.originList);//需要指定字节数
            long incSz = calcListSize(indexSet.fileReaderByParams.incList);
//            System.out.println("dsz:"+dSz+"equivsz"+equivSz);
//            System.out.println("index set size:"+printSize(indexSz));
            System.out.println("equivlence index size "+printSize(equalSz));
            System.out.println("iejoin index size"+printSize(sortedListSz+permuSz));
            System.out.println("total index size "+printSize(equalSz+sortedListSz+permuSz));
            System.out.println("d dataset size "+printSize(dSz));
            System.out.println("inc dataset size "+printSize(incSz));
        }catch (Exception e){
            e.printStackTrace();
        }
        return indexSet;
    }

    private long calcEqualSize(Map<String, EqualMapBean> equalMapBean) {
        long res = 0;
        for(Map.Entry<String,EqualMapBean> equalMapBeanEntry:equalMapBean.entrySet()){
            res+=calcEqualMapBean(equalMapBeanEntry.getValue());
            res+=getObject(equalMapBeanEntry.getKey());
        }
        return res;
    }

    public long getObject(String key) {
        try {
            return SIzeofTools.fullSizeOf(key);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private long calcEqualMapBean(EqualMapBean equalMapBean) {
        long key = getObject(equalMapBean.attr);
        if(equalMapBean.hasNextMap){
            return calcEqualSize(equalMapBean.mapNext)+key;
        }else{
            return calcMapSize(equalMapBean.mapCur)+key;
        }
    }


    private long calcListSize(List<Object> incList) {
        long res = 0;
        for(Object o:incList){
            try {
                res+=SIzeofTools.fullSizeOf(o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private long calcMapSize(Collection<Map<String, List<Integer>>> equivalenceIndex) {
        if(equivalenceIndex.isEmpty())return 0L;
        long res = 0;
        for (Map<String,List<Integer>> map:equivalenceIndex){
            res+=calcMapSize(map);
        }
        return res;
    }

    private long calcMapSize(Map<String, List<Integer>> map) {
        if(map.isEmpty())return 0L;
       long res = 0L;
       for(Map.Entry<String,List<Integer>> entry:map.entrySet()){
           res+=4*entry.getValue().size();
           try {
               res+=SIzeofTools.fullSizeOf(entry.getKey());
           } catch (IllegalAccessException e) {
               e.printStackTrace();
           }
       }
        return res;
//        return RamUsageEstimator.sizeOfCollection(map.values(),4L);
    }

    public String printSize(long byteCount) {
        if (byteCount == 0) {
            return new String("0KB");
        }
        long oneKb = 1 * 1024 ;
        long oneMb = 1*1024*1024;
        long v = byteCount / oneMb;
        double remain = (byteCount%oneMb)/oneKb;
        return v+"MB"+remain+"KB";
    }

    public  void writeObject(String path,Object map) throws IOException {
        File f=new File(path);
        FileOutputStream out=new FileOutputStream(f);
        ObjectOutputStream objwrite=new ObjectOutputStream(out);
        objwrite.writeObject(map);
        objwrite.flush();
        objwrite.close();
    }

    // read the object from the file
    public Object readObject(String path) throws IOException, ClassNotFoundException{
        FileInputStream in=new FileInputStream(path);
        ObjectInputStream objread=new ObjectInputStream(in);
        Object map=objread.readObject();
        objread.close();
        return map;
    }
}
