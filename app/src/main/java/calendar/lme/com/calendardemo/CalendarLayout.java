package calendar.lme.com.calendardemo;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * Created by codbking on 2016/12/18.
 * email:codbking@gmail.com
 * github:https://github.com/codbking
 * blog:http://www.jianshu.com/users/49d47538a2dd/latest_articles
 */

public class CalendarLayout extends FrameLayout {


    private CalendarMonthView view1;
    private ViewGroup view2;
    //展开
    public static final int TYPE_OPEN = 0;
    //折叠
    public static final int TYPE_FOLD = 1;
    public int type = TYPE_OPEN;

    //是否处于滑动中
    private boolean isSilde = false;

    private int topHeigth;
    private int itemHeight;
    private int bottomViewTopHeight;
    private int maxDistance;

    private ScrollerCompat mScroller;
    private float mMaxVelocity;
    private int activitPotionerId;
    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public CalendarLayout(Context context) {
        super(context);
        init();
    }

    public CalendarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        view1 = (CalendarMonthView) getChildAt(0);
        view2 = (ViewGroup) getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        itemHeight=view1.getCellHeight();
        topHeigth=view1.getMeasuredHeight();
        maxDistance = topHeigth - itemHeight;
        switch (type) {
            case TYPE_FOLD:
                bottomViewTopHeight = itemHeight;
                break;
            case TYPE_OPEN:
                bottomViewTopHeight = topHeigth;
                break;
        }
        view2.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - view1.getCellHeight(), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        view2.offsetTopAndBottom(bottomViewTopHeight);
        if(type==TYPE_FOLD){
            view1.offsetTopAndBottom(-view1.getSelectPosition());
        }
    }

    private void init() {

        final ViewConfiguration vc = ViewConfiguration.get(getContext());

        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mScroller = ScrollerCompat.create(getContext(), sInterpolator);
    }

    float oy, ox;
    boolean isClickBtottomView = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (view1.isSlide()){
           return super.onInterceptTouchEvent(ev);
        }
        boolean isflag = false;

        //上下运动进行拦截
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY=ev.getY();
                oy = ev.getY();
                ox = ev.getX();
                isClickBtottomView = isClickView(view2, ev);
                cancel();
                activitPotionerId = ev.getPointerId(0);
                int top = view2.getTop();
                if (top < topHeigth) {
                    type = TYPE_FOLD;
                } else {
                    type = TYPE_OPEN;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float x = ev.getX();

                float xdiff = x - ox;
                float ydiff = y - oy;

                if (Math.abs(ydiff) > 5 && Math.abs(ydiff) > Math.abs(xdiff)) {
                    isflag = true;
                    if (isClickBtottomView) {
                        boolean isScroll = isScroll(view2);
                        if (ydiff > 0) {
                            //向下
                            if (type == TYPE_OPEN) {
                                return super.onInterceptTouchEvent(ev);
                            } else {
                                if (isScroll) {
                                    return super.onInterceptTouchEvent(ev);
                                }

                            }
                        } else {
                            //向上
                            if (type == TYPE_FOLD) {
                                return super.onInterceptTouchEvent(ev);
                            } else {
                                if (isScroll) {
                                    return super.onInterceptTouchEvent(ev);
                                }
                            }
                        }

                    }
                }
                ox = x;
                oy = y;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return isSilde || isflag || super.onInterceptTouchEvent(ev);
    }

    private boolean isScroll(ViewGroup view2) {
        View fistChildView = view2.getChildAt(0);
        if (fistChildView == null) {
            return false;
        }

        if (view2 instanceof ListView) {
            AbsListView list = (AbsListView) view2;
            if (fistChildView.getTop() != 0) {
                return true;
            } else {
                if (list.getPositionForView(fistChildView) != 0) {
                    return true;
                }
            }
        }

        return false;
    }


    public boolean isClickView(View view, MotionEvent ev) {
        Rect rect = new Rect();
        view.getHitRect(rect);
        boolean isClick = rect.contains((int) ev.getX(), (int) ev.getY());
        return isClick;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        processTouchEvent(event);
        return true;
    }

    private VelocityTracker mVelocityTracker;

    float startY,endY;
    public void processTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSilde) {
                    return;
                }
                float cy = event.getY();
                int dy = (int) (cy - oy);
                if (dy == 0) {
                    return;
                }
                oy = cy;
                move(dy);

                break;
            case MotionEvent.ACTION_UP:
                view1.invalidateView();
                if (isSilde) {
                    cancel();
                    return;
                }

                //判断速度
                final int pointerId = activitPotionerId;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                float crrentV = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);

                if (Math.abs(crrentV) > 2000) {
                    if (crrentV > 0) {
                        open();
                    } else {
                        flod();
                    }
                    cancel();
                    return;
                }

                endY=event.getY();
                if (endY>startY){//下滑
                    if (Math.abs(endY-startY)>=view1.getCellHeight()){
                        open();
                    }else {
                        flod();
                    }
                }else {
                    if (Math.abs(endY-startY)>=view1.getCellHeight()){
                        flod();
                    }else {
                        open();
                    }
                }
                cancel();

                break;
            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
        }
    }

    public void open() {
        startScroll(view2.getTop(), topHeigth);
    }

    public void flod() {
        startScroll(view2.getTop(), topHeigth - maxDistance);
    }

    private void move(int dy) {
        int itemHeight=view1.getCellHeight();

        int dy1 = getAreaValue(view1.getTop(), dy, -view1.getSelectPosition(), 0);
        int dy2 = getAreaValue(view2.getTop() - topHeigth, dy, -(topHeigth - itemHeight), 0);

        if (dy1 != 0) {
            ViewCompat.offsetTopAndBottom(view1, dy1);
        }

        if (dy2 != 0) {
            ViewCompat.offsetTopAndBottom(view2, dy2);
        }

    }

    private int getAreaValue(int top, int dy, int minValue, int maxValue) {

        if (top + dy < minValue) {
            return minValue - top;
        }

        if (top + dy > maxValue) {
            return maxValue - top;
        }
        return dy;
    }

    private void startScroll(int starty, int endY) {

        float distance = endY - starty;
        float t = distance / maxDistance * 1000;

        mScroller.startScroll(0, 0, 0, endY - starty, (int) Math.abs(t));
        postInvalidate();
    }

    int oldY = 0;

    @Override
    public void computeScroll() {
        super.computeScroll();

        bottomViewTopHeight = view2.getTop();
        if (mScroller.computeScrollOffset()) {
            isSilde = true;
            int cy = mScroller.getCurrY();
            int dy = cy - oldY;
            move(dy);
            oldY = cy;
            postInvalidate();
        } else {
            oldY = 0;
            isSilde = false;
        }
    }

    public void cancel() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
