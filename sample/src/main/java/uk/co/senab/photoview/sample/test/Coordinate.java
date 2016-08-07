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

    private float x;
    private float y;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
