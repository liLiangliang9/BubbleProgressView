package com.lomon.bubble.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.lomon.bubble.util.DimenUtils;

/**
 * 带刻度带气泡的助力进度条
 */
public class BubbleProgressView extends View {
    private Paint mPaintProgress, mPaintBubble, mPaintProgressStr, mPaintMark, mPaintMarkText;
    private Paint mPaintBottomCircle, mPainTopCircle;
    private PathMeasure mPathMeasure;
    private Path mPathSrc, mPathDst, mPathBubble;
    private int mColorProgressBg;//进度条的背景颜色
    private int mColorProgress;//进度条的进度颜色
    private int mColorProgressStr = Color.WHITE;//进度条的进度文字的颜色
    private float mProgressHeight = DimenUtils.dp2px(getContext(), 10);
    private float mProgress = 0;//进度条的进度
    private float mBubbleTriangleHeight = DimenUtils.dp2px(getContext(), 8);//气泡底部小三角高度
    private float mBubbleRectRound = 0;//气泡的圆角
    private String mProgressStr = "已有0人助力";//显示进度的字符串
    private float mTextSize = DimenUtils.sp2px(getContext(), 11);//进度条文字大小
    private Paint.FontMetricsInt mFontMetricsInt;
    private float mProgressStrMarginLeftRight = DimenUtils.dp2px(getContext(), 0);//气泡的边距
    private float mProgressStrMarginTopDown = DimenUtils.dp2px(getContext(), 10);//气泡的边距

    private float mMarkHeight = DimenUtils.dp2px(getContext(), 5);//刻度高度
    private float mMarkWidth = DimenUtils.dp2px(getContext(), 2);//刻度宽度
    private String last;
    private String third;
    private String second;
    private String first;

    private float mMarkProgress1 = 0;//刻度（人数）进度条
    private float mMarkProgress2 = 0;//刻度（人数）进度条
    private float mMarkProgress3 = 0;//刻度（人数）进度条

    public BubbleProgressView(Context context) {
        this(context, null);
    }

    public BubbleProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintProgress.setStrokeCap(Paint.Cap.ROUND);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setStrokeWidth(mProgressHeight);

        mPaintBottomCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBottomCircle.setStyle(Paint.Style.FILL);
        mPaintBottomCircle.setAntiAlias(true);
        //颜色理应动态设置，这里省事了一下，有需要自己封装
        mPaintBottomCircle.setColor(Color.parseColor("#00FBFF"));

        mPainTopCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPainTopCircle.setStyle(Paint.Style.FILL);
        mPainTopCircle.setAntiAlias(true);
        mPainTopCircle.setColor(Color.parseColor("#ffffff"));

        mPaintBubble = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBubble.setStrokeCap(Paint.Cap.ROUND);//设置线头为圆角
        mPaintBubble.setStyle(Paint.Style.FILL);
        mPaintBubble.setAntiAlias(true);
        mPaintBubble.setStrokeJoin(Paint.Join.ROUND);//设置拐角为圆角

        mPaintProgressStr = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintProgressStr.setStyle(Paint.Style.FILL);
        mPaintProgressStr.setColor(mColorProgressStr);
        mPaintProgressStr.setAntiAlias(true);
        mPaintProgressStr.setTextSize(mTextSize);//设置字体大小
        mPaintProgressStr.setTextAlign(Paint.Align.CENTER);//将文字水平居中

        // 刻度人数（N人）
        mPaintMarkText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMarkText.setStyle(Paint.Style.FILL);
        mPaintMarkText.setAntiAlias(true);
        mPaintMarkText.setColor(Color.parseColor("#333333"));
        mPaintMarkText.setTextSize(DimenUtils.sp2px(getContext(), 12));//设置字体大小
        mPaintMarkText.setTextAlign(Paint.Align.CENTER);//将文字水平居中

        //刻度
        mPaintMark = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMark.setStyle(Paint.Style.FILL);
        mPaintMark.setAntiAlias(true);
        mPaintMark.setColor(Color.parseColor("#666666"));

        mPathSrc = new Path();
        mPathDst = new Path();
        mPathBubble = new Path();
        mPathMeasure = new PathMeasure();

        mColorProgressBg = Color.parseColor("#ECECEC");
        mColorProgress = Color.parseColor("#00FBFF");
        mPaintBubble.setColor(Color.parseColor("#FFD035"));//设置气泡的颜色

        mFontMetricsInt = mPaintProgressStr.getFontMetricsInt();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPathSrc.moveTo(30, h - mProgressHeight * 4);
        mPathSrc.lineTo(w - 30, h - mProgressHeight * 4);//进度条位置在控件整体底部，且距离控件左边和右边各30像素
        mPathMeasure.setPath(mPathSrc, false);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画进度条
        drawProgress(canvas);
        //画气泡
        drawBubble(canvas);
    }

    private void drawBubble(Canvas canvas) {
        Rect rect = new Rect();
        mPaintProgressStr.getTextBounds(mProgressStr, 0, mProgressStr.length(), rect);//返回包围整个字符串的最小的一个Rect区域，以此计算出文字的高度和宽度
        int width = (int) (rect.width() + mProgressStrMarginLeftRight);//计算字符串宽度(加上设置的边距)
        int height = (int) (rect.height() + mProgressStrMarginTopDown);//计算字符串高度(加上设置的边距)
        mPathBubble.reset();
        float p[] = new float[2];//用于存储点坐标的数组
        float t[] = new float[2];
        float stop = mPathMeasure.getLength() * mProgress;//计算进度条的进度
        mPathMeasure.getPosTan(stop, p, t);//获取进度所对应点的左边

        //这里是计算文字所在矩形的位置及大小，大于等于一半就始终显示右侧效果，否则始终显示左侧效果
        RectF rectF;
        if (mProgress < 0.5f) {
            // 画左侧的三角形
            mPathBubble.moveTo(p[0], p[1] - mProgressHeight);
            mPathBubble.lineTo(p[0] + mBubbleTriangleHeight, p[1] - mBubbleTriangleHeight - mProgressHeight);//假设底部小三角为等腰直角三角形，那么三角形的高度就等于底边长度的1/2
            mPathBubble.lineTo(p[0], p[1] - mBubbleTriangleHeight - mProgressHeight);
            mPathBubble.close();//使路径闭合从而形成三角形

            //left:始终为进度所在的横坐标，
            //top:进度所在的高度 - 底部三角形高度 - 进度条高度 - 矩形高度
            //right:进度所在的横坐标+矩形宽度
            //bottom:与top相比小了一个矩形的高度
            rectF = new RectF(
                    p[0] - mBubbleRectRound / 2,
                    p[1] - mBubbleTriangleHeight - mProgressHeight - height,
                    p[0] + mBubbleTriangleHeight + mBubbleRectRound / 2 + width,
                    p[1] - mBubbleTriangleHeight - mProgressHeight);
        } else {

            // 画右侧的三角形
            mPathBubble.moveTo(p[0], p[1] - mProgressHeight);
            mPathBubble.lineTo(p[0] - mBubbleTriangleHeight, p[1] - mBubbleTriangleHeight - mProgressHeight);//假设底部小三角为等腰直角三角形，那么三角形的高度就等于底边长度的1/2
            mPathBubble.lineTo(p[0], p[1] - mBubbleTriangleHeight - mProgressHeight);
            mPathBubble.close();//使路径闭合从而形成三角形

            //left:始终为进度所在的横坐标-矩形宽度，
            //top:进度所在的高度 - 底部三角形高度 - 进度条高度 - 矩形高度
            //right:始终为进度所在的横坐标
            //bottom:与top相比小了一个矩形的高度
            rectF = new RectF(
                    p[0] - mBubbleTriangleHeight - mBubbleRectRound / 2 - width,
                    p[1] - mBubbleTriangleHeight - mProgressHeight - height,
                    p[0] + mBubbleRectRound / 2,
                    p[1] - mBubbleTriangleHeight - mProgressHeight);

        }

        mPathBubble.addRoundRect(rectF, mBubbleRectRound, mBubbleRectRound, Path.Direction.CW);//添加矩形路径
        canvas.drawPath(mPathBubble, mPaintBubble);//绘制气泡
        int i = (mFontMetricsInt.bottom - mFontMetricsInt.ascent) / 2 - mFontMetricsInt.bottom;//让文字垂直居中
        canvas.drawText(mProgressStr, rectF.centerX(), rectF.centerY() + i, mPaintProgressStr);//绘制文字（将文字绘制在气泡矩形的中心点位置）
    }

    private void drawProgress(Canvas canvas) {
        mPathDst.reset();
        mPaintProgress.setColor(mColorProgressBg);
        canvas.drawPath(mPathSrc, mPaintProgress);//绘制进度背景（灰色部分）
        float stop = mPathMeasure.getLength() * mProgress;//计算进度条的进度
        mPathMeasure.getSegment(0, stop, mPathDst, true);//得到与进度对应的路径
        mPaintProgress.setColor(mColorProgress);
        canvas.drawPath(mPathDst, mPaintProgress);//绘制进度

        // 绘制thumb圆
        canvas.drawCircle(stop + 30, getHeight() - mProgressHeight * 4, DimenUtils.dp2px(getContext(), 8), mPaintBottomCircle);
        canvas.drawCircle(stop + 30, getHeight() - mProgressHeight * 4, DimenUtils.dp2px(getContext(), 5), mPainTopCircle);


        // 绘制刻度
        canvas.drawRect(15 + mMarkWidth / 2, getHeight() - mProgressHeight * 2f, mMarkWidth + 15 + mMarkWidth / 2, getHeight() - mProgressHeight * 2f - mMarkHeight, mPaintMark);

        // 绘制人数
        canvas.drawText("0人", 40, getHeight() - mProgressHeight / 2, mPaintMarkText);

        if (!TextUtils.isEmpty(first) && mMarkProgress1 > 0) {
            float result = mPathMeasure.getLength() * mMarkProgress1;
            canvas.drawRect(result + 30 - mMarkWidth / 2, getHeight() - mProgressHeight * 2f, mMarkWidth / 2 + 30 + result, getHeight() - mProgressHeight * 2f - mMarkHeight, mPaintMark);
            canvas.drawText(first + "人", result + 30, getHeight() - mProgressHeight / 2, mPaintMarkText);
        }

        if (!TextUtils.isEmpty(second) && mMarkProgress2 > 0) {
            float result = mPathMeasure.getLength() * mMarkProgress2;
            canvas.drawRect(result + 30 - mMarkWidth / 2, getHeight() - mProgressHeight * 2f, mMarkWidth / 2 + 30 + result, getHeight() - mProgressHeight * 2f - mMarkHeight, mPaintMark);
            canvas.drawText(second + "人", result + 30, getHeight() - mProgressHeight / 2, mPaintMarkText);
        }

        if (!TextUtils.isEmpty(third) && mMarkProgress3 > 0) {
            float result = mPathMeasure.getLength() * mMarkProgress3;
            canvas.drawRect(result + 30 - mMarkWidth / 2, getHeight() - mProgressHeight * 2f, mMarkWidth / 2 + 30 + result, getHeight() - mProgressHeight * 2f - mMarkHeight, mPaintMark);
            canvas.drawText(third + "人", result + 30, getHeight() - mProgressHeight / 2, mPaintMarkText);
        }

        if (!TextUtils.isEmpty(last)) {
            float result = mPathMeasure.getLength();
            canvas.drawRect(result + 40 - mMarkWidth / 2, getHeight() - mProgressHeight * 2f, mMarkWidth / 2 + 40 + result, getHeight() - mProgressHeight * 2f - mMarkHeight, mPaintMark);
            canvas.drawText(last + "人", result + 20, getHeight() - mProgressHeight / 2, mPaintMarkText);
        }

    }

    /**
     * 设置进度
     *
     * @param assistPeople 助力人数
     * @param allPeople    总人数
     */
    public void setProgress(int assistPeople, float allPeople) {
        mProgress = assistPeople / allPeople;
        mProgressStr = "已有" + assistPeople + "人助力";
        invalidate();//设置完进度进行重绘
    }

    /**
     * 设置刻度和人数
     *
     * @param assistPeople
     */
    public void setAssistInfo(String assistPeople) {

        String[] strs = assistPeople.split("-");
        if (strs.length > 0) {
            if (strs.length == 1) {
                last = strs[0];
            } else if (strs.length == 2) {
                first = strs[0];
                last = strs[1];
                mMarkProgress1 = Float.parseFloat(first) / Float.parseFloat(last);
            } else if (strs.length == 3) {
                first = strs[0];
                second = strs[1];
                last = strs[2];
                mMarkProgress1 = Float.parseFloat(first) / Float.parseFloat(last);
                mMarkProgress2 = Float.parseFloat(second) / Float.parseFloat(last);
            } else if (strs.length == 4) {
                first = strs[0];
                second = strs[1];
                third = strs[2];
                last = strs[3];
                mMarkProgress1 = Float.parseFloat(first) / Float.parseFloat(last);
                mMarkProgress2 = Float.parseFloat(second) / Float.parseFloat(last);
                mMarkProgress3 = Float.parseFloat(third) / Float.parseFloat(last);
            }
        }
        invalidate();
    }
}
