package io.github.danielcampossantos.hydrationtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HydrationView extends View {

    private float progress;
    private int percentage;
    private int exceededPercentage;

    private int primaryColor = Color.BLUE;
    private int containerColor = Color.LTGRAY;
    private int exceededColor = Color.RED;

    private final Paint waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint containerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint percentagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint percentSymbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public HydrationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        waterPaint.setStyle(Paint.Style.FILL);
        containerPaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1.5f));
        borderPaint.setColor(Color.rgb(55, 55, 55));

        percentagePaint.setTextAlign(Paint.Align.LEFT);
        percentagePaint.setTextSize(dp(24));
        percentagePaint.setFakeBoldText(true);

        percentSymbolPaint.setTextAlign(Paint.Align.LEFT);
        percentSymbolPaint.setTextSize(dp(11));
        percentSymbolPaint.setFakeBoldText(true);

        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setTextSize(dp(11));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2f;

        Paint.FontMetrics percentageMetrics = percentagePaint.getFontMetrics();
        Paint.FontMetrics statusMetrics = statusPaint.getFontMetrics();

        float percentageHeight = percentageMetrics.descent - percentageMetrics.ascent;
        float statusHeight = statusMetrics.descent - statusMetrics.ascent;

        float cupTextGap = dp(8);
        float textGap = dp(2);
        float cupHeight = height * 0.58f;

        float totalHeight = cupHeight + cupTextGap + percentageHeight + textGap + statusHeight;
        float top = (height - totalHeight) / 2f;
        float bottom = top + cupHeight;

        float leftTop = centerX - width * 0.25f;
        float rightTop = centerX + width * 0.25f;
        float leftBottom = centerX - width * 0.19f;
        float rightBottom = centerX + width * 0.19f;

        Path cup = new Path();

        cup.moveTo(leftTop, top);
        cup.lineTo(rightTop, top);
        cup.lineTo(rightBottom, bottom - dp(4));
        cup.quadTo(centerX, bottom + dp(4), leftBottom, bottom - dp(4));
        cup.close();

        waterPaint.setColor(primaryColor);
        containerPaint.setColor(containerColor);

        canvas.save();
        canvas.clipPath(cup);
        canvas.drawPath(cup, containerPaint);

        float waterHeight = (bottom - top) * progress;
        float waterTop = bottom - waterHeight;

        canvas.drawRect(0, waterTop, width, bottom, waterPaint);

        canvas.restore();

        canvas.drawPath(cup, borderPaint);

        boolean exceeded = percentage > 100;

        percentagePaint.setColor(exceeded ? exceededColor : primaryColor);
        percentSymbolPaint.setColor(exceeded ? exceededColor : primaryColor);
        statusPaint.setColor(exceeded ? exceededColor : Color.rgb(112, 120, 131));

        String numberText = String.valueOf(percentage);
        String statusText = exceeded ? exceededPercentage + "% acima da meta" : "da meta";

        float numberWidth = percentagePaint.measureText(numberText);
        float percentWidth = percentSymbolPaint.measureText("%");
        float percentGap = dp(1);
        float percentageGroupWidth = numberWidth + percentGap + percentWidth;
        float percentageStartX = centerX - percentageGroupWidth / 2f;

        float percentageTop = bottom + cupTextGap;
        float percentageY = percentageTop - percentageMetrics.ascent;

        canvas.drawText(numberText, percentageStartX, percentageY, percentagePaint);
        canvas.drawText("%", percentageStartX + numberWidth + percentGap, percentageY - dp(8), percentSymbolPaint);

        float statusY = percentageTop + percentageHeight + textGap - statusMetrics.ascent;

        canvas.drawText(statusText, centerX, statusY, statusPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(progress, 1f));
        invalidate();
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
        invalidate();
    }

    public void setExceededPercentage(int exceededPercentage) {
        this.exceededPercentage = exceededPercentage;
        invalidate();
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
        invalidate();
    }

    public void setContainerColor(int containerColor) {
        this.containerColor = containerColor;
        invalidate();
    }

    public void setExceededColor(int exceededColor) {
        this.exceededColor = exceededColor;
        invalidate();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}