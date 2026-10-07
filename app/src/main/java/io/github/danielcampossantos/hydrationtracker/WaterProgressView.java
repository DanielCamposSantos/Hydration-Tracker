package io.github.danielcampossantos.hydrationtracker;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WaterProgressView extends View implements View.OnClickListener {

    private static final int DEFAULT_MAX_VALUE = 2000;
    private static final long ANIMATION_DURATION = 500;
    private static final String DEFAULT_TITLE = "Consumo diário de água";

    private float progress;
    private float animatedProgress;

    private int percentage;
    private int exceededPercentage;
    private int currentIntake;
    private int maxValue = DEFAULT_MAX_VALUE;

    private boolean showPercentage = true;
    private String titleText = DEFAULT_TITLE;

    private int progressColor = Color.BLUE;
    private int containerColor = Color.LTGRAY;
    private int textColor = Color.BLUE;
    private int exceededTextColor = Color.RED;

    private ValueAnimator progressAnimator;

    private final Paint waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint containerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint percentagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint percentSymbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint absolutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WaterProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public WaterProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaterProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        readAttributes(context, attrs);
        configurePaints();
        configureInteraction();
        updateProgress(false);
    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterProgressView);

        progressColor = typedArray.getColor(R.styleable.WaterProgressView_progressColor, Color.BLUE);
        textColor = typedArray.getColor(R.styleable.WaterProgressView_textColor, progressColor);
        exceededTextColor = typedArray.getColor(R.styleable.WaterProgressView_exceededTextColor, Color.RED);
        containerColor = typedArray.getColor(R.styleable.WaterProgressView_containerColor, Color.LTGRAY);
        maxValue = Math.max(1, typedArray.getInt(R.styleable.WaterProgressView_maxValue, DEFAULT_MAX_VALUE));

        String customTitle = typedArray.getString(R.styleable.WaterProgressView_titleText);

        if (customTitle != null) {
            titleText = customTitle;
        }

        typedArray.recycle();
    }

    private void configurePaints() {
        waterPaint.setStyle(Paint.Style.FILL);
        containerPaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1.5f));
        borderPaint.setColor(Color.rgb(55, 55, 55));

        configureTextPaint(titlePaint, 11, Paint.Align.CENTER, true);
        configureTextPaint(percentagePaint, 24, Paint.Align.LEFT, true);
        configureTextPaint(percentSymbolPaint, 11, Paint.Align.LEFT, true);
        configureTextPaint(absolutePaint, 17, Paint.Align.LEFT, true);
        configureTextPaint(unitPaint, 9, Paint.Align.LEFT, true);
        configureTextPaint(statusPaint, 11, Paint.Align.CENTER, false);
    }

    private void configureTextPaint(Paint paint, float size, Paint.Align align, boolean bold) {
        paint.setTextAlign(align);
        paint.setTextSize(dp(size));
        paint.setFakeBoldText(bold);
    }

    private void configureInteraction() {
        setOnClickListener(this);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2f;

        updatePaintColors();

        float titleBottom = drawTitle(canvas, centerX);
        float cupTop = titleBottom + dp(8);
        float cupBottom = getCupBottom(height);

        Path cup = createCup(width, centerX, cupTop, cupBottom);

        drawCup(canvas, cup, width, cupTop, cupBottom);
        drawProgressInformation(canvas, centerX, cupBottom);
    }

    private void updatePaintColors() {
        boolean exceeded = isGoalExceeded();
        int currentTextColor = exceeded ? exceededTextColor : textColor;

        waterPaint.setColor(progressColor);
        containerPaint.setColor(containerColor);

        titlePaint.setColor(currentTextColor);
        percentagePaint.setColor(currentTextColor);
        percentSymbolPaint.setColor(currentTextColor);
        absolutePaint.setColor(currentTextColor);
        unitPaint.setColor(currentTextColor);
        statusPaint.setColor(currentTextColor);
    }

    private float drawTitle(Canvas canvas, float centerX) {
        Paint.FontMetrics titleMetrics = titlePaint.getFontMetrics();

        float titleTop = dp(4);
        float titleY = titleTop - titleMetrics.ascent;

        canvas.drawText(titleText, centerX, titleY, titlePaint);

        return titleY + titleMetrics.descent;
    }

    private float getCupBottom(float height) {
        Paint mainPaint = getMainPaint();

        Paint.FontMetrics mainMetrics = mainPaint.getFontMetrics();
        Paint.FontMetrics statusMetrics = statusPaint.getFontMetrics();

        float mainHeight = mainMetrics.descent - mainMetrics.ascent;
        float statusHeight = statusMetrics.descent - statusMetrics.ascent;
        float textHeight = mainHeight + dp(2) + statusHeight;

        return height - textHeight - dp(12);
    }

    private Path createCup(float width, float centerX, float top, float bottom) {
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

        return cup;
    }

    private void drawCup(Canvas canvas, Path cup, float width, float top, float bottom) {
        canvas.save();
        canvas.clipPath(cup);

        canvas.drawPath(cup, containerPaint);

        float visualProgress = Math.min(animatedProgress, 1f);
        float waterHeight = (bottom - top) * visualProgress;
        float waterTop = bottom - waterHeight;

        canvas.drawRect(0, waterTop, width, bottom, waterPaint);

        canvas.restore();

        canvas.drawPath(cup, borderPaint);
    }

    private void drawProgressInformation(Canvas canvas, float centerX, float cupBottom) {
        Paint mainPaint = getMainPaint();

        Paint.FontMetrics mainMetrics = mainPaint.getFontMetrics();
        Paint.FontMetrics statusMetrics = statusPaint.getFontMetrics();

        float mainHeight = mainMetrics.descent - mainMetrics.ascent;
        float mainTop = cupBottom + dp(8);
        float mainY = mainTop - mainMetrics.ascent;

        if (showPercentage) {
            drawPercentage(canvas, centerX, mainY);
        } else {
            drawAbsoluteValue(canvas, centerX, mainY);
        }

        String statusText = getStatusText();
        float statusY = mainTop + mainHeight + dp(2) - statusMetrics.ascent;

        canvas.drawText(statusText, centerX, statusY, statusPaint);
    }

    private void drawPercentage(Canvas canvas, float centerX, float y) {
        String numberText = String.valueOf(percentage);

        float numberWidth = percentagePaint.measureText(numberText);
        float symbolWidth = percentSymbolPaint.measureText("%");
        float gap = dp(1);
        float totalWidth = numberWidth + gap + symbolWidth;
        float startX = centerX - totalWidth / 2f;

        canvas.drawText(numberText, startX, y, percentagePaint);
        canvas.drawText("%", startX + numberWidth + gap, y - dp(8), percentSymbolPaint);
    }

    private void drawAbsoluteValue(Canvas canvas, float centerX, float y) {
        String valueText = currentIntake + "/" + maxValue;
        String unitText = "ml";

        float valueWidth = absolutePaint.measureText(valueText);
        float unitWidth = unitPaint.measureText(unitText);
        float gap = dp(2);
        float totalWidth = valueWidth + gap + unitWidth;
        float startX = centerX - totalWidth / 2f;

        canvas.drawText(valueText, startX, y, absolutePaint);
        canvas.drawText(unitText, startX + valueWidth + gap, y - dp(5), unitPaint);
    }

    private Paint getMainPaint() {
        return showPercentage ? percentagePaint : absolutePaint;
    }

    private String getStatusText() {
        if (isGoalExceeded()) {
            return exceededPercentage + "% acima da meta";
        }

        return "da meta";
    }

    private boolean isGoalExceeded() {
        return currentIntake > maxValue;
    }

    @Override
    public void onClick(View v) {
        showPercentage = !showPercentage;
        invalidate();
    }

    private void updateProgress(boolean animate) {
        if (maxValue <= 0) {
            resetProgress();
            return;
        }

        progress = Math.max(0f, currentIntake / (float) maxValue);
        percentage = Math.round(progress * 100);
        exceededPercentage = isGoalExceeded() ? Math.max(1, percentage - 100) : 0;

        if (animate) {
            animateProgress();
            return;
        }

        animatedProgress = progress;
        invalidate();
    }

    private void resetProgress() {
        progress = 0f;
        animatedProgress = 0f;
        percentage = 0;
        exceededPercentage = 0;
        invalidate();
    }

    private void animateProgress() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        progressAnimator = ValueAnimator.ofFloat(animatedProgress, progress);
        progressAnimator.setDuration(ANIMATION_DURATION);
        progressAnimator.setInterpolator(new DecelerateInterpolator());

        progressAnimator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });

        progressAnimator.start();
    }

    public void setProgress(int value) {
        percentage = Math.max(0, value);
        progress = percentage / 100f;
        currentIntake = Math.round(maxValue * progress);
        exceededPercentage = isGoalExceeded() ? Math.max(1, percentage - 100) : 0;

        animateProgress();
    }

    public void setHydrationData(int currentIntake, int maxValue) {
        this.currentIntake = Math.max(0, currentIntake);
        this.maxValue = Math.max(1, maxValue);

        updateProgress(true);
    }

    public void setCurrentIntake(int currentIntake) {
        this.currentIntake = Math.max(0, currentIntake);
        updateProgress(true);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = Math.max(1, maxValue);
        updateProgress(true);
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    public void setExceededTextColor(int exceededTextColor) {
        this.exceededTextColor = exceededTextColor;
        invalidate();
    }

    public void setContainerColor(int containerColor) {
        this.containerColor = containerColor;
        invalidate();
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText == null ? DEFAULT_TITLE : titleText;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        super.onDetachedFromWindow();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}