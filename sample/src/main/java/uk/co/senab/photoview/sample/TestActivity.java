package uk.co.senab.photoview.sample;

import android.graphics.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;


public class TestActivity extends Activity {

    private ScaleGestureDetector detector;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Matrix matrix = new Matrix();
        imageView = (ImageView) findViewById(R.id.imageView);
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                    return false;

                int centerX = (imageView.getWidth()) / 2;
                int centerY = (imageView.getHeight()) / 2;
                TestActivity.this.onScale(scaleFactor, centerX, centerY);
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
        detector = new ScaleGestureDetector(this, mScaleListener);
        imageView.setImageMatrix(matrix);
    }


    public void onScale(float scaleFactor, float focusX, float focusY) {
        System.out.println(String.format("onScale: scale: %.2f. fX: %.2f. fY: %.2f",
                scaleFactor, focusX, focusY));
//        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        Matrix matrix = new Matrix (imageView.getImageMatrix());
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        imageView.setImageMatrix(matrix);
        imageView.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
}
