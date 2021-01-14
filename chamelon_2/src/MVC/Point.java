package MVC;

public class Point {
    public String cell;
    public int degree,adj_degree;

    public Point(String cell) {
        this.cell = cell;
        this.degree = 0;
        this.adj_degree = 0;
    }
}
