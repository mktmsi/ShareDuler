package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoResizeTextView extends TextView {

    private  final float MIN_TEXT_SIZE = 10f;

    public AutoResizeTextView(Context context) {
        super(context);
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        resize();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        resize();
    }

    private void resize() {

        Paint paint = new Paint();

        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();

        //    適当に大きめの数値からスタート
        float textSize = 300f;

        paint.setTextSize(textSize);

        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = (float)(Math.abs(fm.top)) + (Math.abs(fm.descent));

        float textWidth = paint.measureText(this.getText().toString());
        while (viewWidth < textWidth | viewHeight < textHeight){
            if (MIN_TEXT_SIZE >= textSize){
                textSize = MIN_TEXT_SIZE;
                break;
            }

            textSize--;

            paint.setTextSize(textSize);

            fm = paint.getFontMetrics();
            textHeight = (float)(Math.abs(fm.top)) + (Math.abs(fm.descent));
            textWidth = paint.measureText(this.getText().toString());
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

}