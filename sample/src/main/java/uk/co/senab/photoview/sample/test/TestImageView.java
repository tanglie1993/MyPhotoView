package uk.co.senab.photoview.sample.test;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/8/7 0007.
 */
public class TestImageView extends ImageView {

    private ScaleGestureDetector detector;

    public TestImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TestImageView(Context context) {
        super(context);
    }

    public TestImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context) {
        Matrix matrix = new Matrix();
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

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
        detector = new ScaleGestureDetector(context, mScaleListener);
        setImageMatrix(matrix);
    }


    public void onScale(float scaleFactor, float focusX, float focusY) {
        System.out.println(String.format("onScale: scale: %.2f. fX: %.2f. fY: %.2f",
                scaleFactor, focusX, focusY));
//        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        Matrix matrix = new Matrix (getImageMatrix());
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        setImageMatrix(matrix);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
}
