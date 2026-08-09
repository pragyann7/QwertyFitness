package com.ps.qwertyfitness.ui.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ps.qwertyfitness.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrendGraphView extends View {

    public static class DataPoint {
        public long timestamp;
        public float value;

        public DataPoint(long timestamp, float value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private List<DataPoint> data = new ArrayList<>();
    private Paint linePaint, pointPaint, areaPaint, gridPaint, textPaint;
    private Path linePath, areaPath;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private String unit = "";

    public TrendGraphView(Context context) {
        super(context);
        init();
    }

    public TrendGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.accent_electric_lime));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.accent_electric_lime));
        pointPaint.setStyle(Paint.Style.FILL);

        areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaPaint.setColor(ContextCompat.getColor(getContext(), R.color.accent_electric_lime));
        areaPaint.setAlpha(20);
        areaPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.divider_slate));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        textPaint.setTextSize(24f);

        linePath = new Path();
        areaPath = new Path();
    }

    public void setData(List<DataPoint> data, String unit) {
        this.data = data;
        this.unit = unit != null ? unit : "";
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        float paddingLeft = 100f;
        float paddingRight = 40f;
        float paddingTop = 40f;
        float paddingBottom = 60f;

        float effectiveWidth = width - paddingLeft - paddingRight;
        float effectiveHeight = height - paddingTop - paddingBottom;

        if (data == null || data.isEmpty()) {
            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                float y = paddingTop + effectiveHeight - (i * effectiveHeight / gridLines);
                canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
            }
            canvas.drawText("No data for this period", width / 2 - 120, height / 2, textPaint);
            return;
        }

        if (data.size() < 2) {
            DataPoint entry = data.get(0);
            canvas.drawCircle(paddingLeft + effectiveWidth / 2, paddingTop + effectiveHeight / 2, 8f, pointPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%.1f %s", entry.value, unit), paddingLeft + effectiveWidth / 2 + 15, paddingTop + effectiveHeight / 2, textPaint);
            return;
        }

        float maxValue = -Float.MAX_VALUE;
        float minValue = Float.MAX_VALUE;
        long minTime = Long.MAX_VALUE;
        long maxTime = -Long.MAX_VALUE;

        for (DataPoint point : data) {
            if (point.value > maxValue) maxValue = point.value;
            if (point.value < minValue) minValue = point.value;
            if (point.timestamp > maxTime) maxTime = point.timestamp;
            if (point.timestamp < minTime) minTime = point.timestamp;
        }

        float range = maxValue - minValue;
        if (range == 0) range = 1;
        maxValue += range * 0.1f;
        minValue -= range * 0.1f;
        range = maxValue - minValue;
        
        long timeRange = maxTime - minTime;
        if (timeRange == 0) timeRange = 1;

        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = paddingTop + effectiveHeight - (i * effectiveHeight / gridLines);
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
            float labelVal = minValue + (i * range / gridLines);
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", labelVal), 10, y + 10, textPaint);
        }

        linePath.reset();
        areaPath.reset();

        for (int i = 0; i < data.size(); i++) {
            DataPoint point = data.get(i);
            float x = paddingLeft + (point.timestamp - minTime) * effectiveWidth / timeRange;
            float y = paddingTop + effectiveHeight - (point.value - minValue) * effectiveHeight / range;

            if (i == 0) {
                linePath.moveTo(x, y);
                areaPath.moveTo(x, paddingTop + effectiveHeight);
                areaPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                areaPath.lineTo(x, y);
            }
            
            if (i == data.size() - 1) {
                areaPath.lineTo(x, paddingTop + effectiveHeight);
                areaPath.close();
                canvas.drawText(dateFormat.format(new Date(minTime)), paddingLeft, height - 10, textPaint);
                String lastDate = dateFormat.format(new Date(maxTime));
                float dateWidth = textPaint.measureText(lastDate);
                canvas.drawText(lastDate, width - paddingRight - dateWidth, height - 10, textPaint);
            }
        }

        canvas.drawPath(areaPath, areaPaint);
        canvas.drawPath(linePath, linePaint);

        for (DataPoint point : data) {
            float x = paddingLeft + (point.timestamp - minTime) * effectiveWidth / timeRange;
            float y = paddingTop + effectiveHeight - (point.value - minValue) * effectiveHeight / range;
            canvas.drawCircle(x, y, 8f, pointPaint);
        }
    }
}
