package uk.co.senab.photoview.sample.test;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.Toast;

import uk.co.senab.photoview.sample.test.entity.Coordinate;
import uk.co.senab.photoview.sample.test.entity.Line;
import uk.co.senab.photoview.sample.test.listener.DefaultOnDoubleTapListener;
import uk.co.senab.photoview.sample.test.listener.DefaultOnGestureListener;

/**
 * Created by Administrator on 2016/8/7 0007.
 */
public class TestImageView extends ImageView {

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private Coordinate lastCoordinate = new Coordinate();

    private boolean isDragging;
    private boolean isAutomaticScaling;

    private RectF displayRect = new RectF();

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

    private void init(final Context context) {
        setScaleType(ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        setImageMatrix(matrix);
        scaleDetector = new ScaleGestureDetector(context.getApplicationContext(), mScaleListener);
        gestureDetector = new GestureDetector(context, new DefaultOnGestureListener());
        gestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener() {
            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {

                if (isAutomaticScaling) {
                    return false;
                }

                ValueAnimator anim = ValueAnimator.ofFloat(1f, 1.5f);
                anim.setDuration(200);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    private float lastAnimatedValue = 1;

                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        scale((float) valueAnimator.getAnimatedValue() / lastAnimatedValue, (displayRect.left + displayRect.right) / 2, (displayRect.top + displayRect.bottom) / 2);
                        lastAnimatedValue = (float) valueAnimator.getAnimatedValue();
                    }
                });
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        isAutomaticScaling = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isAutomaticScaling = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        isAutomaticScaling = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                anim.start();
                return false;
            }
        });

    }

    private void scale(float scaleFactor, float focusX, float focusY) {
//        System.out.println("scaleFactor: " + scaleFactor + " focuX: " + focusX + "focusY: " + focusY);
        Matrix matrix = new Matrix(getImageMatrix());
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        if(!hasExceededBounds(matrix)){
//            System.out.println("!hasExceededBounds");
            setImageMatrix(matrix);
            invalidate();
        }
    }

    private void translate(float dx, float dy) {
        Matrix matrix = new Matrix(getImageMatrix());
        matrix.postTranslate(dx, dy);
        if(!hasExceededBounds(matrix)){
            setImageMatrix(matrix);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isAutomaticScaling){
            return true;
        }
        scaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
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
                    translate((int) dx, (int) dy);
                    lastCoordinate.x = ev.getX();
                    lastCoordinate.y = ev.getY();
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
            if(isAutomaticScaling){
                return false;
            }

            float scaleFactor = detector.getScaleFactor();

            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)){
                return false;
            }
            System.out.println("scaleFactor: " + scaleFactor);
            if(isExpanded()){
                if(isExpanding(scaleFactor)){
                    scale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                }else{
                    scaleTowardOriginal(scaleFactor);
                }
            }else{
                if(isExpanding(scaleFactor)){
                    scaleTowardOriginal(scaleFactor);
                }else{
                    scale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                }
            }

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

    private void scaleTowardOriginal(float scaleFactor) {
        Coordinate scaleCenter = getTowardOriginalScaleCenter();
        int centerX = (int) scaleCenter.x;
        int centerY = (int) scaleCenter.y;
        TestImageView.this.scale(scaleFactor, centerX, centerY);
    }

    private boolean isExpanding(float scaleFactor) {
        return scaleFactor > 1;
    }

    private boolean isExpanded() {
        if(displayRect.bottom - displayRect.top > getBottom() - getTop()){
            return true;
        }else{
            return false;
        }
    }

    private boolean hasExceededBounds(Matrix matrix){
        displayRect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(displayRect);
//        System.out.println("displayRect.bottom: " + displayRect.bottom);
//        System.out.println("displayRect.top: " + displayRect.top);
//        System.out.println("displayRect.left: " + displayRect.left);
//        System.out.println("displayRect.right: " + displayRect.right);
        if(displayRect.left > 0 || displayRect.right < getRight() - getLeft()
                || displayRect.top > 0 || displayRect.bottom < getBottom() - getTop()){
            return true;
        }else{
            return false;
        }
    }

    private Coordinate getTowardOriginalScaleCenter() {
//        adjustDisplayRect();

        Line lineA = new Line();
        lineA.a = new Coordinate(0, 0);
        lineA.b = new Coordinate(displayRect.left, displayRect.top);

        Line lineB = new Line();
        lineB.a = new Coordinate(getRight() - getLeft(), 0);
        lineB.b = new Coordinate(displayRect.right, displayRect.top);

//        System.out.println("lineA.a.x: " + lineA.a.x + " lineA.a.y " + lineA.a.y);
//        System.out.println("lineA.b.x: " + lineA.b.x + " lineA.b.y " + lineA.b.y);
//        System.out.println("lineB.a.x: " + lineB.a.x + " lineB.a.y " + lineB.a.y);
//        System.out.println("lineB.b.x: " + lineB.b.x + " lineB.b.y " + lineB.b.y);
//        System.out.println("Coordinate.distance(lineA.a, lineA.b) " + Coordinate.distance(lineA.a, lineA.b));
//        System.out.println("Coordinate.distance(lineB.a, lineB.b) " +   Coordinate.distance(lineB.a, lineB.b));

        if(Coordinate.distance(lineA.a, lineA.b) < 1 && Coordinate.distance(lineB.a, lineB.b) < 1){
//            System.out.println("return A: " + (getRight() - getLeft()) / 2 + " " + (getBottom() - getTop()) / 2);
            return new Coordinate((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
        }
        if(Coordinate.distance(lineA.a, lineA.b) < 1){
//            System.out.println("return B: " + lineA.a.x + " " + lineA.a.y);
            return lineA.a;
        }
        if(Coordinate.distance(lineB.a, lineB.b) < 1){
//            System.out.println("return C: " + lineB.a.x + " " + lineB.a.y);
            return lineB.a;
        }
//        System.out.println("return D: " + Line.intersection(lineA, lineB).x + " " + Line.intersection(lineA, lineB).y);
        return Line.intersection(lineA, lineB);
    }

}
