package com.luboganev.handdrawn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements RangeSeekBar.OnRangeSeekBarChangeListener {
    @InjectView(R.id.drawingView) DrawingView drawingView;
    @InjectView(R.id.rangebar) RangeSeekBar<Integer> rangeBar;
    @InjectView(R.id.action_clear) ImageButton clearImageButton;
    @InjectView(R.id.action_set_draw_mode) ImageButton drawImageButton;
    @InjectView(R.id.action_set_present_mode) ImageButton presentImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        rangeBar.setOnRangeSeekBarChangeListener(this);
        rangeBar.setNotifyWhileDragging(true);
        updateControlPanelButtons();
        updateRangeBar(0);
    }

    private void updateControlPanelButtons() {
        if (drawingView.getMode() == DrawingView.MODE_DRAW) {
            clearImageButton.setVisibility(View.VISIBLE);
            drawImageButton.setVisibility(View.GONE);
            presentImageButton.setVisibility(View.VISIBLE);
        } else {
            clearImageButton.setVisibility(View.GONE);
            drawImageButton.setVisibility(View.VISIBLE);
            presentImageButton.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.action_clear)
    void onActionClear() {
        if (drawingView.getMode() != DrawingView.MODE_DRAW) {
            drawingView.setMode(DrawingView.MODE_DRAW);
        } else {
            drawingView.clearCanvas();
        }
        rangeBar.setVisibility(View.GONE);
    }

    @OnClick(R.id.action_set_draw_mode)
    void onActionSetDrawMode() {
        drawingView.setMode(DrawingView.MODE_DRAW);
        rangeBar.setVisibility(View.GONE);
        updateControlPanelButtons();
    }

    @OnClick(R.id.action_set_present_mode)
    void onActionSetPresentMode() {
        List<TimedPoint> points = drawingView.getTimedPointsCopy();
        drawingView.setMode(DrawingView.MODE_PRESENT);
        updateRangeBar(points.size() - 1);
        rangeBar.setVisibility(View.VISIBLE);
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
