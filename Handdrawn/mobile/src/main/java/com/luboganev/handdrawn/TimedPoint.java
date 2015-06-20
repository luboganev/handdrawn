package com.luboganev.handdrawn;

/**
 * Created by luboganev on 20/06/15.
 */
public class TimedPoint {
    private final float mTouchX;
    private final float mTouchY;
    private final long mTouchTimestamp;
    private final int mTouchEventAction;

    public TimedPoint(float touchX, float touchY, int touchEventAction, long touchTimestamp) {
        mTouchX = touchX;
        mTouchY = touchY;
        mTouchTimestamp = touchTimestamp;
        mTouchEventAction = touchEventAction;
    }

    public float getTouchX() {
        return mTouchX;
    }

    public float getTouchY() {
        return mTouchY;
    }

    public long getTouchTimestamp() {
        return mTouchTimestamp;
    }

    public int getTouchEventAction() {
        return mTouchEventAction;
    }

    public long timeTo(TimedPoint point) {
        return mTouchTimestamp - point.mTouchTimestamp;
    }

    public String formattedTimeTo(TimedPoint point) {
        long totalMillis = timeTo(point);

        long seconds = totalMillis / 1000L;
        long millis = totalMillis % 1000L;

        return seconds + "." + millis;
    }
}
