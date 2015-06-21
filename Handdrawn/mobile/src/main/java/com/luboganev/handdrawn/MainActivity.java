package com.luboganev.handdrawn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luboganev.handdrawn.data.Sensor;
import com.luboganev.handdrawn.data.SensorDataPoint;
import com.luboganev.handdrawn.events.BusProvider;
import com.luboganev.handdrawn.events.NewSensorEvent;
import com.luboganev.handdrawn.events.SensorRangeEvent;
import com.luboganev.handdrawn.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements RangeSeekBar.OnRangeSeekBarChangeListener {
    @InjectView(R.id.drawingView) DrawingView drawingView;
    @InjectView(R.id.rangebar) RangeSeekBar<Integer> rangeBar;
    @InjectView(R.id.action_clear) ImageView clearImageView;
    @InjectView(R.id.action_set_draw_mode) ImageView drawImageView;
    @InjectView(R.id.action_set_present_mode) ImageView presentImageView;
    @InjectView(R.id.tutorialOverlay) View tutorialOverlayView;
    @InjectView(R.id.logo) ImageView logoImageView;
    @InjectView(R.id.sensorGraphView) SensorGraphView sensorGraphView;

    private static final long SENSOR_ID = 1;
    private Sensor sensor;
    private float spread;

    private boolean[] drawSensors = new boolean[6];

    private RemoteSensorManager remoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        remoteSensorManager = RemoteSensorManager.getInstance(this);

        updateCurrentSensor();

        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int messageResourceId = -1;
                switch (v.getId()) {
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

        rangeBar.setOnRangeSeekBarChangeListener(this);
        rangeBar.setNotifyWhileDragging(true);
        updateControlPanelButtons();
        updateRangeBar(0);
    }

    private void updateCurrentSensor() {
        sensor = RemoteSensorManager.getInstance(this).getSensor(SENSOR_ID);
        if (sensor != null) {
            remoteSensorManager.filterBySensorId((int) sensor.getId());
            initialiseSensorData();
        }
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
            tutorialOverlayView.setVisibility(View.INVISIBLE);
        }
    };

    private void hideTutorialNow() {
        tutorialOverlayView.removeCallbacks(mHideTutorialRunnable);
        tutorialOverlayView.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.logo)
    void onActionInfo() {
        tutorialOverlayView.setVisibility(View.VISIBLE);
        tutorialOverlayView.removeCallbacks(mHideTutorialRunnable);
        tutorialOverlayView.postDelayed(mHideTutorialRunnable, 3000);
    }

    @OnClick(R.id.action_clear)
    void onActionClear() {
        hideTutorialNow();
        if (drawingView.getMode() != DrawingView.MODE_DRAW) {
            drawingView.setMode(DrawingView.MODE_DRAW);
        } else {
            drawingView.clearCanvas();
        }
    }

    @OnClick(R.id.action_set_draw_mode)
    void onActionSetDrawMode() {
        remoteSensorManager.startMeasurement();
        drawingView.setMode(DrawingView.MODE_DRAW);
        rangeBar.setVisibility(View.INVISIBLE);
        logoImageView.setVisibility(View.VISIBLE);
        updateControlPanelButtons();
    }

    @OnClick(R.id.action_set_present_mode)
    void onActionSetPresentMode() {
        List<TimedPoint> points = drawingView.getTimedPointsCopy();
        remoteSensorManager.stopMeasurement();
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

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        initialiseSensorData();
        if (drawingView.getMode() == DrawingView.MODE_DRAW) {
            remoteSensorManager.startMeasurement();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        remoteSensorManager.stopMeasurement();
    }

    @Subscribe
    public void onSensorUpdatedEvent(SensorUpdatedEvent event) {
        Timber.d(" ========= SensorUpdatedEvent ========= ");
        if (sensor == null) {
            return;
        }
        if (event.getSensor().getId() == this.sensor.getId()) {

            for (int i = 0; i < event.getDataPoint().getValues().length; ++i) {
                float normalised = (event.getDataPoint().getValues()[i] - sensor.getMinValue()) / spread;
                sensorGraphView.addNewDataPoint(normalised, event.getDataPoint().getAccuracy(), i);
            }
        }
    }

    @Subscribe
    public void onSensorRangeEvent(SensorRangeEvent event) {
        Timber.d(" ========= SensorRangeEvent ========= ");
        if (sensor == null) {
            return;
        }
        if (event.getSensor().getId() == this.sensor.getId()) {
            initialiseSensorData();
        }
    }

    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
//        sensor = event.getSensor();
        Timber.d(" ========= New sensor ========= ");
//        sensorEmptyState.setVisibility(View.GONE);
        updateCurrentSensor();
    }

    private void initialiseSensorData() {
        if (sensor == null) {
            return;
        }

        sensorGraphView.setVisibility(View.VISIBLE);

        spread = sensor.getMaxValue() - sensor.getMinValue();
        LinkedList<SensorDataPoint> dataPoints = sensor.getDataPoints();

        if (dataPoints == null || dataPoints.isEmpty()) {
            Log.w("sensor data", "no data found for sensor " + sensor.getId() + " " + sensor.getName());
            return;
        }


        LinkedList<Float>[] normalisedValues = new LinkedList[dataPoints.getFirst().getValues().length];
        LinkedList<Integer>[] accuracyValues = new LinkedList[dataPoints.getFirst().getValues().length];

        for (int i = 0; i < normalisedValues.length; ++i) {
            normalisedValues[i] = new LinkedList<Float>();
            accuracyValues[i] = new LinkedList<Integer>();
        }


        for (SensorDataPoint dataPoint : dataPoints) {

            for (int i = 0; i < dataPoint.getValues().length; ++i) {
                float normalised = (dataPoint.getValues()[i] - sensor.getMinValue()) / spread;
                normalisedValues[i].add(normalised);
                accuracyValues[i].add(dataPoint.getAccuracy());
            }
        }


        sensorGraphView.setNormalisedDataPoints(normalisedValues, accuracyValues);
        sensorGraphView.setZeroLine((0 - sensor.getMinValue()) / spread);

        sensorGraphView.setMaxValueLabel(MessageFormat.format("{0,number,#}", sensor.getMaxValue()));
        sensorGraphView.setMinValueLabel(MessageFormat.format("{0,number,#}", sensor.getMinValue()));

    }
}
