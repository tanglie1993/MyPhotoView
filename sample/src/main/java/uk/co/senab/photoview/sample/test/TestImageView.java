package uk.co.senab.photoview.sample.test;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/8/7 0007.
 */
public class TestImageView extends ImageView {

    private ScaleGestureDetector detector;

    private Coordinate lastCoordinate = new Coordinate();

    private boolean isDragging;

    public TestImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public TestImageView(Context context) {
        super(context);
        init(context);
    }

    public TestImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TestImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        setImageMatrix(matrix);
        detector = new ScaleGestureDetector(context.getApplicationContext(), mScaleListener);
    }




    public void onScale(float scaleFactor, float focusX, float focusY) {
        Matrix matrix = new Matrix(getImageMatrix());
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        setImageMatrix(matrix);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(canDrag()){
                    isDragging = true;
                    lastCoordinate.x = ev.getX();
                    lastCoordinate.y = ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isDragging){
                    float dx = ev.getX() - lastCoordinate.x;
                    float dy = ev.getY() - lastCoordinate.y;
                    scrollBy((int) -dx, (int) -dy);
                    lastCoordinate.x = ev.getX();
                    lastCoordinate.x = ev.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }
        return true;
    }

    private boolean canDrag() {
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        return values[0] > 1;
    }

    private ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return false;


            Coordinate scaleCenter = getScaleCenter();

            int centerX = (int) scaleCenter.x;
            int centerY = (int) scaleCenter.y;
            TestImageView.this.onScale(scaleFactor, centerX, centerY);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // NO-OP
        }
    };

    private Coordinate getScaleCenter() {

        RectF displayRect = new RectF();
        displayRect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        getImageMatrix().mapRect(displayRect);

        Line lineA = new Line();
        lineA.a = new Coordinate(0, 0);
        lineA.b = new Coordinate(displayRect.left, displayRect.top);

        Line lineB = new Line();
        lineB.a = new Coordinate(getRight() - getLeft(), 0);
        lineB.b = new Coordinate(displayRect.right, displayRect.top);

        System.out.println("lineA.a.x: " + lineA.a.x + " lineA.a.y " + lineA.a.y);
        System.out.println("lineA.b.x: " + lineA.b.x + " lineA.b.y " + lineA.b.y);
        System.out.println("lineB.a.x: " + lineB.a.x + " lineB.a.y " + lineB.a.y);
        System.out.println("lineB.b.x: " + lineB.b.x + " lineB.b.y " + lineB.b.y);
        System.out.println("Coordinate.distance(lineA.a, lineA.b) " + Coordinate.distance(lineA.a, lineA.b));
        System.out.println("Coordinate.distance(lineB.a, lineB.b) " +   Coordinate.distance(lineB.a, lineB.b));

        if(Coordinate.distance(lineA.a, lineA.b) < 1 && Coordinate.distance(lineB.a, lineB.b) < 1){
            System.out.println("return A: " + (getRight() - getLeft()) / 2 + " " + (getBottom() - getTop()) / 2);
            return new Coordinate((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
        }
        if(Coordinate.distance(lineA.a, lineA.b) < 1){
            System.out.println("return B: " + lineA.a.x + " " + lineA.a.y);
            return lineA.a;
        }
        if(Coordinate.distance(lineB.a, lineB.b) < 1){
            System.out.println("return C: " + lineB.a.x + " " + lineB.a.y);
            return lineB.a;
        }
        System.out.println("return D: " + Line.intersection(lineA, lineB).x + " " + Line.intersection(lineA, lineB).y);
        return Line.intersection(lineA, lineB);
    }

}
