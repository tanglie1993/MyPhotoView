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

import uk.co.senab.photoview.sample.test.entity.Coordinate;
import uk.co.senab.photoview.sample.test.entity.Line;
import uk.co.senab.photoview.sample.test.listener.DefaultOnDoubleTapListener;
import uk.co.senab.photoview.sample.test.listener.DefaultOnGestureListener;

/**
 * Created by Administrator on 2016/8/7 0007.
 */
public class TestImageView extends ImageView {

    private int originalImageBottom;
    private int originalImageTop;
    private int originalImageLeft;
    private int originalImageRight;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private Coordinate lastCoordinate = new Coordinate();

    private int currentDegree;
    private boolean isDragging;
    private boolean isAutomaticScaling;

    private RectF displayRect = new RectF();

    private int imageHeightUpperLimit;

    private float cumulativeScalefactor = 1;

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

        scaleDetector = new ScaleGestureDetector(context.getApplicationContext(), scaleListener);
        gestureDetector = new GestureDetector(context, new DefaultOnGestureListener());
        gestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener() {
            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (isAutomaticScaling) {
                    return false;
                }
                startScaleAnimation();
                return false;
            }
        });
        rotate(20);

        post(new Runnable() {
            @Override
            public void run() {
                originalImageBottom = getTop() + getDrawable().getIntrinsicHeight();
                originalImageTop = getTop();
                originalImageLeft = getLeft();
                originalImageRight = getLeft() + getDrawable().getIntrinsicWidth();
                imageHeightUpperLimit = (getOriginalImageBottom() - getOriginalImageTop()) * 3;
            }
        });
    }

    private void startScaleAnimation() {
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
        anim.addListener(animatorListener);
        anim.start();
    }

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
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
    };

    private void scale(float scaleFactor, float focusX, float focusY) {
//        System.out.println("scaleFactor: " + scaleFactor + " focuX: " + focusX + "focusY: " + focusY);
        cumulativeScalefactor *= scaleFactor;
        if(cumulativeScalefactor < 0.99999){
            cumulativeScalefactor = 1;
            return;
        }
        adjustDisplayRect();
        if(displayRect.bottom - displayRect.top > imageHeightUpperLimit && isExpanding(scaleFactor)){
            return;
        }
        Matrix matrix = new Matrix(getImageMatrix());
        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
//            System.out.println("!hasExceededBounds");
        if(!hasExceededBounds(matrix)){
            setImageMatrix(matrix);
            invalidate();
        }
    }

    private void translate(float dx, float dy) {
        System.out.println("displayRect.right: " + displayRect.right);
        Matrix matrix = new Matrix(getImageMatrix());
        matrix.postTranslate(dx, dy);
        if(!hasExceededBounds(matrix)){
            setImageMatrix(matrix);
            invalidate();
        }
    }

    private void rotate(int degree){
        currentDegree += degree;
        if(currentDegree >= 360){
            currentDegree %= 360;
        }
        Matrix imageMatrix = getImageMatrix();
        adjustDisplayRect();
        imageMatrix.postRotate(degree, (displayRect.left + displayRect.right) / 2, (displayRect.top + displayRect.bottom) / 2);
        setImageMatrix(imageMatrix);
        System.out.println("dislayRect: " + displayRect.left + " " + displayRect.top + " " + displayRect.right + " " + displayRect.bottom);
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
                adjustDisplayRect();
                if(canDrag(displayRect)){
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

    private boolean canDrag(RectF rect) {
        if(rect.bottom > getOriginalImageBottom() - getOriginalImageTop() && rect.top < 0){
            if(rect.left > 0 && rect.right < getOriginalImageRight() - getOriginalImageLeft()){
                return true;
            }else if(rect.left < 0 && rect.right > getOriginalImageRight() - getOriginalImageLeft()) {
                return true;
            }else{
                return false;
            }
        }else if(rect.left < 0 && rect.right > getOriginalImageRight() - getOriginalImageLeft()){
            if (rect.top > 0 && rect.bottom < getOriginalImageBottom() - getOriginalImageTop()){
                return true;
            }else if (rect.top < 0 && rect.bottom > getOriginalImageBottom() - getOriginalImageTop()){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    private ScaleGestureDetector.OnScaleGestureListener scaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

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
            if(currentDegree == 0){
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
            }else{
                if(getFixedAngleCount() == 1){
                    Coordinate center = getTiltedImageZoomOutCenter();
                    scale(scaleFactor, center.x, center.y);
                }else{
                    adjustDisplayRect();
                    float centerX = (displayRect.left + displayRect.right) / 2;
                    float centerY = (displayRect.top + displayRect.bottom) / 2;
                    scale(scaleFactor, centerX, centerY);
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

    private int getFixedAngleCount() {
        adjustDisplayRect();
        int fixedAngleCount = 0;
        if(displayRect.bottom > getOriginalImageTop() - getOriginalImageBottom() && displayRect.bottom < getOriginalImageTop() - getOriginalImageBottom() + 1){
            fixedAngleCount++;
        }
        if(displayRect.top < 0 && displayRect.top > -1){
            fixedAngleCount++;
        }
        if(displayRect.right > getOriginalImageRight() - getOriginalImageLeft() && displayRect.right < getOriginalImageRight() - getOriginalImageLeft() + 1){
            fixedAngleCount++;
        }
        if(displayRect.left < 0 && displayRect.left > -1){
            fixedAngleCount++;
        }
        return fixedAngleCount;
    }

    private Coordinate getTiltedImageZoomOutCenter() {
        adjustDisplayRect();
        int tempDegree = currentDegree % 90;
        double tan = Math.tan(tempDegree);
        if(displayRect.bottom > getOriginalImageTop() - getOriginalImageBottom() && displayRect.bottom < getOriginalImageTop() - getOriginalImageBottom() + 1){
            float x = (float) (displayRect.left + (displayRect.right - displayRect.left) * tan / (1 + tan));
            float y = displayRect.bottom;
            return new Coordinate(x, y);
        }
        if(displayRect.top < 0 && displayRect.top > -1){
            float x = (float) (displayRect.left + (displayRect.right - displayRect.left) * 1 / (1 + tan));
            float y = displayRect.top;
            return new Coordinate(x, y);
        }
        if(displayRect.right > getOriginalImageRight() - getOriginalImageLeft() && displayRect.right < getOriginalImageRight() - getOriginalImageLeft() + 1){
            float x = displayRect.right;
            float y = (float) (displayRect.top + (displayRect.bottom - displayRect.top) * 1 / (1 + tan));
            return new Coordinate(x, y);
        }
        if(displayRect.left < 0 && displayRect.left > -1){
            float x = displayRect.left;
            float y = (float) (displayRect.top + (displayRect.bottom - displayRect.top) * tan / (1 + tan));
            return new Coordinate(x, y);
        }
        float centerX = (displayRect.left + displayRect.right) / 2;
        float centerY = (displayRect.top + displayRect.bottom) / 2;
        return new Coordinate(centerX, centerY);
    }

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
        if(displayRect.bottom - displayRect.top > getOriginalImageBottom() - getOriginalImageTop()){
            return true;
        }else{
            return false;
        }
    }

    private boolean hasExceededBounds(Matrix matrix){
        RectF rect = new RectF();
        rect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(rect);

//        System.out.println("displayRect.bottom: " + displayRect.bottom);
//        System.out.println("displayRect.top: " + displayRect.top);
//        System.out.println("displayRect.left: " + displayRect.left);
//        System.out.println("displayRect.right: " + displayRect.right);
        if(canDrag(rect)){
            return false;
        }else{
            return true;
        }
    }

    private void adjustDisplayRect() {
        Matrix matrix = getImageMatrix();
        displayRect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(displayRect);
    }

    private Coordinate getTowardOriginalScaleCenter() {
//        adjustDisplayRect();

        Line lineA = new Line();
        lineA.a = new Coordinate(0, 0);
        lineA.b = new Coordinate(displayRect.left, displayRect.top);

        Line lineB = new Line();
        lineB.a = new Coordinate(getOriginalImageRight() - getOriginalImageLeft(), 0);
        lineB.b = new Coordinate(displayRect.right, displayRect.top);

//        System.out.println("lineA.a.x: " + lineA.a.x + " lineA.a.y " + lineA.a.y);
//        System.out.println("lineA.b.x: " + lineA.b.x + " lineA.b.y " + lineA.b.y);
//        System.out.println("lineB.a.x: " + lineB.a.x + " lineB.a.y " + lineB.a.y);
//        System.out.println("lineB.b.x: " + lineB.b.x + " lineB.b.y " + lineB.b.y);
//        System.out.println("Coordinate.distance(lineA.a, lineA.b) " + Coordinate.distance(lineA.a, lineA.b));
//        System.out.println("Coordinate.distance(lineB.a, lineB.b) " +   Coordinate.distance(lineB.a, lineB.b));

        if(Coordinate.distance(lineA.a, lineA.b) < 1 && Coordinate.distance(lineB.a, lineB.b) < 1){
//            System.out.println("return A: " + (getOriginalImageRight() - getOriginalImageLeft()) / 2 + " " + (getOriginalImageBottom() - getOriginalImageTop()) / 2);
            return new Coordinate((getOriginalImageRight() - getOriginalImageLeft()) / 2, (getOriginalImageBottom() - getOriginalImageTop()) / 2);
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
    
    private int getOriginalImageBottom(){
        return originalImageBottom;
    }

    private int getOriginalImageTop(){
        return originalImageTop;
    }

    private int getOriginalImageLeft(){
        return originalImageLeft;
    }

    private int getOriginalImageRight(){
        return originalImageRight;
    }
}
