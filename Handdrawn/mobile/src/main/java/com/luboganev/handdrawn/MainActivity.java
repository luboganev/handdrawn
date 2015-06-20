package com.luboganev.handdrawn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity implements RangeSeekBar.OnRangeSeekBarChangeListener {
    @InjectView(R.id.drawingView) DrawingView drawingView;
    @InjectView(R.id.rangebar) RangeSeekBar<Integer> rangeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        rangeBar.setOnRangeSeekBarChangeListener(this);
        rangeBar.setNotifyWhileDragging(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (drawingView != null) {
            if (drawingView.getMode() == DrawingView.MODE_DRAW) {
                menu.findItem(R.id.action_clear).setVisible(true);
                menu.findItem(R.id.action_set_draw_mode).setVisible(false);
                menu.findItem(R.id.action_set_present_mode).setVisible(true);
            } else {
                menu.findItem(R.id.action_clear).setVisible(false);
                menu.findItem(R.id.action_set_draw_mode).setVisible(true);
                menu.findItem(R.id.action_set_present_mode).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                if (drawingView.getMode() != DrawingView.MODE_DRAW) {
                    drawingView.setMode(DrawingView.MODE_DRAW);
                } else {
                    drawingView.clearCanvas();
                }
                rangeBar.setVisibility(View.GONE);
                return true;
            case R.id.action_set_draw_mode:
                drawingView.setMode(DrawingView.MODE_DRAW);
                rangeBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
                return true;
            case R.id.action_set_present_mode:
                List<TimedPoint> points = drawingView.getTimedPointsCopy();
                drawingView.setMode(DrawingView.MODE_PRESENT);
                updateRangeBar(points.size() - 1);
                rangeBar.setVisibility(View.VISIBLE);
                drawingView.setTimedPoints(points);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
