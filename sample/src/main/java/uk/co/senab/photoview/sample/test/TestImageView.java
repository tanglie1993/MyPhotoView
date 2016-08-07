package uk.co.senab.photoview.sample.test;

import android.content.Context;
import android.graphics.Matrix;
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
        System.out.println(String.format("onScale: scale: %.2f. fX: %.2f. fY: %.2f",
                scaleFactor, focusX, focusY));
        Matrix matrix = new Matrix(getImageMatrix());
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        setImageMatrix(matrix);
        System.out.println(matrix.toShortString());
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(canDrag()){
                    isDragging = true;
                    lastCoordinate.setX(ev.getX());
                    lastCoordinate.setY(ev.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isDragging){
                    float dx = ev.getX() - lastCoordinate.getX();
                    float dy = ev.getY() - lastCoordinate.getY();
                    float distance = (float) Math.sqrt(dx*dx + dy*dy);
                    scrollBy((int) -dx, (int) -dy);
                    lastCoordinate.setX(ev.getX());
                    lastCoordinate.setY(ev.getY());
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

            int centerX = (getWidth()) / 2;
            int centerY = (getHeight()) / 2;
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
}
