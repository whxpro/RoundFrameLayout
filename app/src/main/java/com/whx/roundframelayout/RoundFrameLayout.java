package com.whx.roundframelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

public class RoundFrameLayout extends FrameLayout {

    public RoundFrameLayout(Context context) {
        this(context, null);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private float mRadius;
    private float mTopLeftRadius;
    private float mTopRightRadius;
    private float mBottomLeftRadius;
    private float mBottomRightRadius;

    private Path mPath;
    private Paint mPaint;
    private final RectF mLayer = new RectF();
    private boolean isRadiusSame;

    private void init(AttributeSet attrs) {
        TypedArray tpa = getContext().obtainStyledAttributes(attrs, R.styleable.RoundFrameLayout);
        mRadius = tpa.getDimension(R.styleable.RoundFrameLayout_rfl_radius, 0);
        mTopLeftRadius = tpa.getDimension(R.styleable.RoundFrameLayout_rfl_topLeft_radius, 0);
        mTopRightRadius = tpa.getDimension(R.styleable.RoundFrameLayout_rfl_topRight_radius, 0);
        mBottomLeftRadius = tpa.getDimension(R.styleable.RoundFrameLayout_rfl_bottomLeft_radius, 0);
        mBottomRightRadius = tpa.getDimension(R.styleable.RoundFrameLayout_rfl_bottomRight_radius, 0);
        tpa.recycle();

        setArgs();
    }

    private void setArgs() {
        isRadiusSame = mTopLeftRadius == mTopRightRadius && mTopLeftRadius == mBottomLeftRadius && mTopLeftRadius == mBottomRightRadius;
        if (isRadiusSame) {
            if (mTopLeftRadius != 0) {
                mRadius = mTopLeftRadius;
            } else {
                mTopLeftRadius = mRadius;
                mTopRightRadius = mRadius;
                mBottomLeftRadius = mRadius;
                mBottomRightRadius = mRadius;
            }
        }

        if (isRadiusSame && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mRadius != 0) {
                setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0,
                                0,
                                view.getWidth(),
                                view.getHeight(),
                                mRadius);
                    }
                });
                setClipToOutline(true);
                Log.e("--------", "set clip outline");
            }
        } else {
            mPath = new Path();

            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);

            Log.e("--------", "set path and paint");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mLayer.set(0, 0, w, h);
        if (mPath != null) {
            mPath.reset();
            float[] radii = new float[]{
                    mTopLeftRadius, mTopLeftRadius,
                    mTopRightRadius, mTopRightRadius,
                    mBottomLeftRadius, mBottomLeftRadius,
                    mBottomRightRadius, mBottomRightRadius
            };
            mPath.addRoundRect(new RectF(0, 0, w, h), radii, Path.Direction.CW);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        long start = System.currentTimeMillis();
        if (isRadiusSame) {
            super.dispatchDraw(canvas);

            Log.e("---------", "super dispatch draw, cost: " + (System.currentTimeMillis() - start));
        } else {
            // Android P 之后 Xfermode 行为变化，所以分情况处理，见bug https://issuetracker.google.com/issues/111819103?pli=1
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                canvas.saveLayer(mLayer, null, Canvas.ALL_SAVE_FLAG);
                super.dispatchDraw(canvas);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                canvas.drawPath(mPath, mPaint);
                canvas.restore();

                Log.e("---------", "< P, dispatch draw cost: " + (System.currentTimeMillis() - start));
            } else {
//                canvas.save();
//                canvas.clipPath(mPath);
//                super.dispatchDraw(canvas);
//                canvas.restore();

                canvas.saveLayer(mLayer, null);
                super.dispatchDraw(canvas);

                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                Path path = new Path();
                path.addRect(new RectF(0, 0, mLayer.width() + 1, mLayer.height() + 1), Path.Direction.CW);
                path.op(mPath, Path.Op.DIFFERENCE);
                canvas.drawPath(path, mPaint);
                canvas.restore();

                Log.e("---------", ">= P, dispatch draw cost: " + (System.currentTimeMillis() - start));
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mPath != null && getBackground() != null) {
            canvas.save();
            canvas.clipPath(mPath);
            super.draw(canvas);
            canvas.restore();

            Log.e("---------", "clip background");
        } else {
            super.draw(canvas);
        }
    }

    public void setRadius(float radius) {
        if (mRadius != radius) {
            mRadius = radius;
            setArgs();
            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            invalidate();
        }
    }

    public void setTopLeftRadius(float topLeftRadius) {
        if (mTopLeftRadius != topLeftRadius) {
            mTopLeftRadius = topLeftRadius;
            setArgs();
            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            invalidate();
        }
    }

    public void setTopRightRadius(float topRightRadius) {
        if (mTopRightRadius != topRightRadius) {
            mTopRightRadius = topRightRadius;
            setArgs();
            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            invalidate();
        }
    }

    public void setBottomLeftRadius(float bottomLeftRadius) {
        if (mBottomLeftRadius != bottomLeftRadius) {
            mBottomLeftRadius = bottomLeftRadius;
            setArgs();
            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            invalidate();
        }
    }

    public void setBottomRightRadius(float bottomRightRadius) {
        if (mBottomRightRadius != bottomRightRadius) {
            mBottomRightRadius = bottomRightRadius;
            setArgs();
            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            invalidate();
        }
    }
}
