package uk.co.senab.photoview.sample.test;

/**
 * Created by Administrator on 2016/8/13 0013.
 */
public class Line {
    Coordinate a;
    Coordinate b;

    public Line() {
        this.a = new Coordinate();
        this.b = new Coordinate();
    }
    //求两直线的交点，斜率相同的话res=u.a
    public static Coordinate intersection(Line u,Line v){
        Coordinate res = u.a;
        double t = ((u.a.x-v.a.x)*(v.b.y-v.a.y)-(u.a.y-v.a.y)*(v.b.x-v.a.x))
                /((u.a.x-u.b.x)*(v.b.y-v.a.y)-(u.a.y-u.b.y)*(v.b.x-v.a.x));
        res.x += (u.b.x-u.a.x)*t;
        res.y += (u.b.y-u.a.y)*t;
        return res;
    }
}
