package com.luboganev.handdrawn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements RangeSeekBar.OnRangeSeekBarChangeListener {
    @InjectView(R.id.drawingView) DrawingView drawingView;
    @InjectView(R.id.rangebar) RangeSeekBar<Integer> rangeBar;
    @InjectView(R.id.action_clear) ImageView clearImageView;
    @InjectView(R.id.action_set_draw_mode) ImageView drawImageView;
    @InjectView(R.id.action_set_present_mode) ImageView presentImageView;
    @InjectView(R.id.action_info) ImageView showInfoImageView;
    @InjectView(R.id.logo) ImageView logoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int messageResourceId = -1;
                switch (v.getId()) {
                    case R.id.action_info:
                        messageResourceId = R.string.hint_toast_info;
                        break;
                    case R.id.action_clear:
                        messageResourceId = R.string.hint_toast_clear;
                        break;
                    case R.id.action_set_present_mode:
                        messageResourceId = R.string.hint_toast_present_mode;
                        break;
                    case R.id.action_set_draw_mode:
                        messageResourceId = R.string.hint_toast_draw_mode;
                        break;
                }
                if (messageResourceId >= 0) {
                    Toast.makeText(getApplicationContext(), messageResourceId, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        };

        clearImageView.setOnLongClickListener(listener);
        drawImageView.setOnLongClickListener(listener);
        presentImageView.setOnLongClickListener(listener);
        showInfoImageView.setOnLongClickListener(listener);

        rangeBar.setOnRangeSeekBarChangeListener(this);
        rangeBar.setNotifyWhileDragging(true);
        updateControlPanelButtons();
        updateRangeBar(0);
    }

    private void updateControlPanelButtons() {
        if (drawingView.getMode() == DrawingView.MODE_DRAW) {
            clearImageView.setVisibility(View.VISIBLE);
            drawImageView.setVisibility(View.GONE);
            presentImageView.setVisibility(View.VISIBLE);
        } else {
            clearImageView.setVisibility(View.GONE);
            drawImageView.setVisibility(View.VISIBLE);
            presentImageView.setVisibility(View.GONE);
        }
    }

    private Runnable mHideTutorialRunnable = new Runnable() {
        @Override
        public void run() {
            drawingView.setBackground(null);
        }
    };

    @OnClick({R.id.action_info, R.id.logo})
    void onActionInfo() {
        drawingView.setBackgroundResource(R.drawable.tutorial_background);
        drawingView.removeCallbacks(mHideTutorialRunnable);
        drawingView.postDelayed(mHideTutorialRunnable, 3000);
    }

    @OnClick(R.id.action_clear)
    void onActionClear() {
        if (drawingView.getMode() != DrawingView.MODE_DRAW) {
            drawingView.setMode(DrawingView.MODE_DRAW);
        } else {
            drawingView.clearCanvas();
        }
    }

    @OnClick(R.id.action_set_draw_mode)
    void onActionSetDrawMode() {
        drawingView.setMode(DrawingView.MODE_DRAW);
        rangeBar.setVisibility(View.INVISIBLE);
        logoImageView.setVisibility(View.VISIBLE);
        updateControlPanelButtons();
    }

    @OnClick(R.id.action_set_present_mode)
    void onActionSetPresentMode() {
        List<TimedPoint> points = drawingView.getTimedPointsCopy();
        drawingView.setMode(DrawingView.MODE_PRESENT);
        updateRangeBar(points.size() - 1);
        rangeBar.setVisibility(View.VISIBLE);
        logoImageView.setVisibility(View.INVISIBLE);
        drawingView.setTimedPoints(points);
        updateControlPanelButtons();
    }

    public void updateRangeBar(int maxValue) {
        if(rangeBar != null) {
            rangeBar.setOnRangeSeekBarChangeListener(null);
            rangeBar.setRangeValues(0, maxValue);
            rangeBar.setSelectedMinValue(0);
            rangeBar.setSelectedMaxValue(maxValue);
            rangeBar.setOnRangeSeekBarChangeListener(this);
        }
    }

    @Override
    public void onRangeSeekBarValuesChanged(RangeSeekBar rangeSeekBar, Object o, Object t1) {
        drawingView.presentPointsRange(rangeSeekBar.getSelectedMinValue().intValue(),
                rangeSeekBar.getSelectedMaxValue().intValue());
    }
}
