package uk.co.senab.photoview.sample.test;

/**
 * Created by Administrator on 2016/8/7 0007.
 */
public class Coordinate {

    public Coordinate(){

    }

    public Coordinate(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float x;
    public float y;

    public static float distance(Coordinate a, Coordinate b){
        float deltaX = Math.abs(a.x - b.x);
        float deltaY = Math.abs(a.y - b.y);
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
