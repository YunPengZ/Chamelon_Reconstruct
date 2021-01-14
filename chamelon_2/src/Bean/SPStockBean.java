package Bean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SPStockBean implements Serializable {
    private int id;
    private String Date;
    private String Ticker;
    private int Open;
    private int High;
    private int Low;
    private int Close;
    private int Volume;

    public SPStockBean(int id,String args[]){
        this.id = id;
        this.Date = args[0];
        this.Ticker = args[1];
        this.Open = (int)(Double.parseDouble(args[2])*100);
        this.High = (int)(Double.parseDouble(args[3])*100);
        this.Low = (int)(Double.parseDouble(args[4])*100);
        this.Close = (int)(Double.parseDouble(args[5])*100);
        this.Volume = Integer.parseInt(args[6]);
    }

    public String get(String para){
        String value= null;
        Method method = null;
        char[] chars = para.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char)(chars[0] - 32);
        }
        para = new String(chars);
        try {
            if (para.equals("Close")) {
                return Integer.toString(this.getClose());
            }
            if (para.equals("Open")) {
                return Integer.toString(this.getOpen());
            }
            if (para.equals("High")) {
                return Integer.toString(this.getHigh());
            }

            if (para.equals("Id")) {
                return Integer.toString(this.getId());
            }
            if (para.equals("Date")) {
                return this.getDate();
            }
            if (para.equals("Ticker")) {
                return this.getTicker();
            }
            if (para.equals("Low")) {
                return Integer.toString(this.getLow());
            }
            if (para.equals("Volume")) {
                return Integer.toString(this.getVolume());
            }

            System.out.println("SPStock unknown param: " + para);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return value;
    }
    @Override
    public String toString() {
        return "SPStockBean{" +
                "id=" + id +
                ", Date='" + Date + '\'' +
                ", Ticker='" + Ticker + '\'' +
                ", Open=" + Open +
                ", High=" + High +
                ", Low=" + Low +
                ", Close=" + Close +
                ", Volumn=" + Volume +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTicker() {
        return Ticker;
    }

    public void setTicker(String ticker) {
        Ticker = ticker;
    }

    public int getOpen() {
        return Open;
    }

    public void setOpen(int open) {
        Open = open;
    }

    public int getHigh() {
        return High;
    }

    public void setHigh(int high) {
        High = high;
    }

    public int getLow() {
        return Low;
    }

    public void setLow(int low) {
        this.Low = low;
    }

    public int getClose() {
        return Close;
    }

    public void setClose(int close) {
        Close = close;
    }

    public int getVolume() {
        return Volume;
    }

    public void setVolume(int volumn) {
        Volume = volumn;
    }
}
