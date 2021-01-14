package Bean;

import java.util.ArrayList;
import java.util.List;

public class Violation<Object> {
    static int id;//新建冲突 则自增
    private int first_id;
    private int second_id;
    private int dc;
    private boolean isFirst;//表示要修改的元素是不是第一个 true则表明是第一个 false表明不是第一个

    public Violation(){
        super();
    }
//    public Violation(int left, int right){
//        this.first_id  = left;
//        this.second_id = right;
//        id++;
//        //System.err.println("执行到错误运行程序 可能导致答案不正确 请检查构建冲突过程");
//    }

    public Violation(int left, int right, int dc){
        this.first_id  = left;
        this.second_id = right;
        this.dc = dc;
        id++;
        //System.err.println("执行到错误运行程序 可能导致答案不正确 请检查构建冲突过程");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSecond() {
        return second_id;
    }

    public void setSecond(int second) {
        this.second_id = second;
    }

    public int getFirst() {
        return first_id;
    }

    public void setFirst(int first) {
        this.first_id = first;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setIsFirst(boolean isFirst) {
        this.isFirst= isFirst;
    }

    public int getDc() {
        return dc;
    }

    public void setDc(int dc) {
        this.dc = dc;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "first=" + first_id +
                ", second=" + second_id +
                ",isFirst="+isFirst+
                ",dc="+dc+
                '}';
    }
}
/***

public class Violation<T>{
    private T first;
    private T second;

    public Violation(T left,T right){
        this.first  = left;
        this.second = right;
    }

    public T getSecond() {
        return second;
    }

    public void setSecond(T second) {
        this.second = second;
    }

    public Object getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "first=" + first.toString() +
                ", second=" + second.toString() +
                '}';
    }
}
 */