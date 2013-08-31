package com.kulerz.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.kulerz.app.helpers.SystemHelper;

public class ColorView extends LinearLayout
{

    private boolean isSelected = false;
    private int color;
    private Paint paint = new Paint();

    public ColorView(Context context) {
        super(context);
        initialize();
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        setClickable(true);
        setOrientation(HORIZONTAL);
        setWillNotDraw(false);
        paint.setARGB(255, 255, 255, 255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(SystemHelper.convertDpToPixel(5, getContext()));
        paint.setAntiAlias(true);
    }

    public void setColorIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        this.invalidate();
    }

    public void setColor(int color) {
        this.color = color;
        setBackgroundColor(color);
    }

    public int getColor() {
        return color;
    }

    public boolean isColorSelected() {
        return isSelected;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isSelected) {
            Rect r = canvas.getClipBounds();
            Rect outline = new Rect(1, 1, r.right - 1, r.bottom - 1);
            canvas.drawRect(outline, paint);
        }
    }

}
