package hlh.palace9;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * 作者： Created by Ying on 2018/7/25.
 * 描述：
 */

public class Palace9View extends View {
    //以下根据需求改变
    int radius = 20; //圆半径
    int roundColor = Color.BLACK;
    int slideColor = Color.GREEN;
    int errorColor = Color.RED;
    int paintWidth = 10;  //画笔粗细
    int time = 1000; //重置时间 单位ms

    private Paint mPaint;
    private int screenWidth;
    private int screenHeight;
    private Context mContext;
    ArrayList<RoundBean> initial = new ArrayList();  //初始的状态值
    private String mPw = "";
    private RoundInterface mRoundInterface;
    private Handler mHandler = new Handler();
    boolean binit;
    private Runnable mR = new Runnable() {
        @Override
        public void run() {
            mRoundBeans.clear();
            for (RoundBean roundBean : initial) {
                mRoundBeans.add((RoundBean) roundBean.clone());
            }
            startPoint[0] = startPoint[1] = 0;
            points = new float[9 * 4];
            iRound = 0;
            mString = "";
            bError = false;
            invalidate();
        }
    };

    //完成需要重置以下状态
    ArrayList<RoundBean> mRoundBeans = new ArrayList();
    int[] startPoint = {0, 0};//0 x  1  y
    float[] points = new float[9 * 4];//至少4个值，即能够绘制一条直线
    int iRound = 0;
    String mString = "";
    boolean bError;

    public Palace9View(Context context) {
        super(context);
        init(context);
    }

    public Palace9View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Palace9View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setColor(roundColor);//设置颜色
        mPaint.setStyle(Paint.Style.FILL);//  Paint.Style.STROKE：描边  Paint.Style.FILL_AND_STROKE：描边并填充  Paint.Style.FILL：填充
        mPaint.setStrokeWidth(paintWidth);//设置画笔的粗细

        //  获取屏幕的宽高
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = getMeasuredWidth(); //获取控件宽高
        screenHeight = getMeasuredHeight();

        //把圆bean 初始化出来  随着android:layout_width 变化
        if (!binit) {
            for (int i = 1; i < 4; i++) {
                for (int j = 1; j < 4; j++) {
                    int i1 = screenWidth / 4 * j;
                    int i2 = screenHeight / 4 * i;
                    mRoundBeans.add(new RoundBean(i1, i2));
                    initial.add(new RoundBean(i1, i2));
                }
            }
            binit = true;
        }
    }

    //进行绘画   invalidate(); 重新绘画
    // canvas了解下  https://www.jianshu.com/p/afa06f716ca6
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RoundBean roundBean : mRoundBeans) {
            int color = roundColor;
            if (roundBean.status == 1) {
                color = slideColor;
                if (bError) color = errorColor;
            }
            mPaint.setColor(color);
            // 绘制圆       圆心的XY坐标    半径  画笔
            canvas.drawCircle(roundBean.x, roundBean.y, radius, mPaint);
        }
        mPaint.setColor(bError ? errorColor : slideColor);
        canvas.drawLines(points, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bError) return true;
        if (event.getAction() == MotionEvent.ACTION_UP) { //手指离开
            bError = true;
            if (!TextUtils.isEmpty(mPw) && mPw.equals(mString)) {
                if (mRoundInterface != null) mRoundInterface.success();
                return true;
            }
            if (mRoundInterface != null) mRoundInterface.failure(mString);
            mHandler.postDelayed(mR, time);
        } else {
            float x = event.getX();
            float y = event.getY();
            for (int i = 0; i < mRoundBeans.size(); i++) {
                RoundBean roundBean = mRoundBeans.get(i);
                if (roundBean.status == 1) continue;
                if (x < roundBean.x + radius && x > roundBean.x - radius &&
                        y < roundBean.y + radius && y > roundBean.y - radius) {
                    startPoint[0] = roundBean.x;
                    startPoint[1] = roundBean.y;
                    roundBean.status = 1;
                    mString += i;
                    if (iRound == 0) {
                        points[0] = roundBean.x;
                        points[1] = roundBean.y;
                    } else {
                        points[iRound * 4 - 2] = roundBean.x;
                        points[iRound * 4 - 1] = roundBean.y;
                        points[iRound * 4] = roundBean.x;
                        points[iRound * 4 + 1] = roundBean.y;
                    }
                    iRound++;
                }
            }
            if (iRound > 0) {
                points[iRound * 4 - 2] = x;
                points[iRound * 4 - 1] = y;
            }
        }

        if (bError) {
            points[iRound * 4 - 2] = points[iRound * 4 - 4];
            points[iRound * 4 - 1] = points[iRound * 4 - 3];
        }
        invalidate();
        return true;
    }

    //设置密码
    public void setPW(String pw) {
        mPw = pw;
    }

    public void setInterface(RoundInterface roundInterface) {
        mRoundInterface = roundInterface;
    }


    class RoundBean implements Cloneable {
        int x;
        int y;
        int status; //状态 0未滑动  1滑动中 2 错误 3 成功状态

        public RoundBean(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    interface RoundInterface {
        void failure(String string);

        void success();
    }
}
