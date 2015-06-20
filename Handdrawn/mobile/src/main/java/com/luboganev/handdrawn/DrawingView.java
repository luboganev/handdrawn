package com.luboganev.handdrawn;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DrawingView extends View {
    private List<TimedPoint> mTimedPoints = new LinkedList<>();
    private Path mDrawPath;
    private Paint mDrawPaint, mCanvasPaint;

    // If path should be reset in onDraw
    private boolean mResetPath = false;
    // If canvas should be cleared in onDraw
    private boolean mClear = false;

    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;

    public static final int MODE_DRAW = 1;
    public static final int MODE_PRESENT = 2;

    private int mMode = MODE_DRAW;

    /**
     *  Changes the mode of the view between touch
     *  drawing and presenting saved timedpoints
     *
     * @param mode
     */
    public void setMode(int mode) {
        if (mMode == mode) {
            return;
        }
        switch (mode) {
            case MODE_DRAW:
                clearCanvas();
                mMode = mode;
                break;
            case MODE_PRESENT:
                mMode = mode;
                break;
        }
    }

    public void setTimedPoints(List<TimedPoint> timedPoints) {
        if (mMode == MODE_PRESENT) {
            mTimedPoints = timedPoints;
            presentPointsRange(0, timedPoints.size() - 1);
        }
    }

    public List<TimedPoint> getTimedPointsCopy() {
        ArrayList<TimedPoint> copyPoints = new ArrayList<>();
        copyPoints.addAll(mTimedPoints);
        return copyPoints;
    }

    public DrawingView(Context context) {
        super(context);
        initView();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mDrawPath = new Path();
        mDrawPaint = new Paint();
        int mPaintColor = 0xFF000000;
        mDrawPaint.setColor(mPaintColor);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(20);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mMode != MODE_DRAW) {
            return false;
        }

        float touchX = event.getX();
        float touchY = event.getY();

        mTimedPoints.add(new TimedPoint(touchX, touchY, event.getAction(), System.currentTimeMillis()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
                mDrawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mClear) {
            mDrawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mClear = false;
        }

        if (mResetPath) {
            mDrawPath.reset();
            mResetPath = false;
        }

        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mDrawPath, mDrawPaint);
    }

    /**
     *  Clears the canvas and all saved timed points
     */
    public void clearCanvas() {
        mClear = true;
        mResetPath = true;
        mTimedPoints.clear();
        invalidate();
    }

    /**
     *  Drawns a range of the saved timed points on the screen. The view needs to be in
     *  present mode to do this.
     *
     * @param startIndex
     * @param endIndex
     */
    public void presentPointsRange(int startIndex, int endIndex) {
        if (mMode != MODE_PRESENT) {
            return;
        }

        if (startIndex >= endIndex) {
            return;
        }
        if (startIndex < 0) {
            return;
        }

        if (endIndex >= mTimedPoints.size()) {
            return;
        }

        mClear = true;

        Path path = new Path();
        for (int i = startIndex; i < endIndex; i++) {
            TimedPoint p = mTimedPoints.get(i);

            if (i == startIndex) {
                path.moveTo(p.getTouchX(), p.getTouchY());
            }

            switch (p.getTouchEventAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(p.getTouchX(), p.getTouchY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(p.getTouchX(), p.getTouchY());
                    break;
            }
        }
        mDrawPath = path;
        invalidate();
    }

    public int getMode() {
        return mMode;
    }
}
