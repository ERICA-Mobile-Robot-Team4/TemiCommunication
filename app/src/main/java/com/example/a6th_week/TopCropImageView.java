package com.example.a6th_week;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 이미지를 칸에 꽉 채우되, 상단(얼굴)이 잘리지 않게 위쪽 기준으로 크롭합니다.
 */
public class TopCropImageView extends AppCompatImageView {

    public TopCropImageView(Context context) {
        super(context);
        init();
    }

    public TopCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        applyTopCropMatrix();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        applyTopCropMatrix();
    }

    private void applyTopCropMatrix() {
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        int dWidth  = drawable.getIntrinsicWidth();
        int dHeight = drawable.getIntrinsicHeight();
        int vWidth  = getWidth();
        int vHeight = getHeight();

        if (dWidth <= 0 || dHeight <= 0 || vWidth <= 0 || vHeight <= 0) return;

        // 가로, 세로 중 더 큰 비율로 스케일 → 칸을 꽉 채움
        float scale = Math.max((float) vWidth / dWidth, (float) vHeight / dHeight);

        // 가로는 가운데 정렬, 세로는 위쪽(0) 고정
        float dx = (vWidth  - dWidth  * scale) / 2f;
        float dy = 0f;

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
        setImageMatrix(matrix);
    }
}
