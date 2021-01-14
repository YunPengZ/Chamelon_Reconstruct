package FileOption;

import java.io.Serializable;

public class FileParams implements Serializable {
    public String originDir,incDir,cleanDir,dirtyDir;
    public String dSize,errorRate,incSize,table,detectionType;
    //此类直接定义为公开变量

    public FileParams(String originDir, String incDir, String cleanDir, String dirtyDir, String dSize, String errorRate, String incSize, String table,String detectionType) {
        this.originDir = originDir;
        this.incDir = incDir;
        this.cleanDir = cleanDir;
        this.dirtyDir = dirtyDir;
        this.dSize = dSize;
        this.errorRate = errorRate;
        this.incSize = incSize;
        this.table = table;
        this.detectionType = detectionType;
    }

    @Override
    public String toString() {
        return "FileParams{" +
                "originDir='" + originDir + '\'' +
                ", incDir='" + incDir + '\'' +
                ", cleanDir='" + cleanDir + '\'' +
                ", dirtyDir='" + dirtyDir + '\'' +
                ", dSize='" + dSize + '\'' +
                ", errorRate='" + errorRate + '\'' +
                ", incSize='" + incSize + '\'' +
                ", table='" + table + '\'' +
                ", detectionType='" + detectionType + '\'' +
                '}';
    }
}
