package calendar.lme.com.calendardemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import net.pink.pinkutils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;

/**
 * 月视图
 *
 * 邵前前
 */
public class CalendarMonthView extends View {
    private final Region[][] MONTH_REGIONS= new Region[6][7];
    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
            Paint.LINEAR_TEXT_FLAG);
    private Scroller mScroller;
    private OnPageChangeListener onPageChangeListener;
    private OnDatePickedListener onDatePickedListener;

    private int circleRadius;
    private int indexYear, indexMonth;
    private int centerYear=CalendarUtils.getYear(), centerMonth=CalendarUtils.getMonth();
    private int leftYear, leftMonth;
    private int rightYear, rightMonth;
    private int width, height;
    private int lastPointX;
    private int lastPointY;
    private int lastMoveX;
    private int criticalWidth;

    private float sizeTextGregorian, sizeTextFestival;
    private float offsetYFestival1, offsetYFestival2,offsetYFestival;
    private boolean isNewEvent;

    private Calendar today = Calendar.getInstance(Locale.getDefault());
    private Map<Integer,SparseArray> monthCellDescriptorMap=new HashMap<>();
    private String selectedCalendarDay;
    private int mTouchSlop;
    private int selectPosition;
    private int cellHeight;
    private String ymd;
    private boolean isSlide;//是否处于滑动中
    public CalendarMonthView(Context context) {
        super(context);
        init(context);
    }

    public CalendarMonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context){
        mScroller = new Scroller(context);
        mPaint.setTextAlign(Paint.Align.CENTER);
        selectedCalendarDay= CalendarUtils.getDate(today);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public void computeScroll() {
        Boolean flag=mScroller.computeScrollOffset();
        if (flag) {
            isSlide=true;
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }else {
            isSlide = false;
        }
    }
    public void invalidateView(){
        scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                isNewEvent = true;
                lastPointX = (int) event.getX();
                lastPointY= (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float offsetX=Math.abs(lastPointX - event.getX());
                if(Math.abs(lastPointY - event.getY())>offsetX){
                    break;
                }
                if (isNewEvent) {
                    if (offsetX > mTouchSlop) {
                        isNewEvent = false;
                    }
                }
                int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
                smoothScrollTo(totalMoveX, indexYear * height);
                break;
            case MotionEvent.ACTION_UP:
                float offsetX1=Math.abs(lastPointX - event.getX());
                if(Math.abs(lastPointY - event.getY())>offsetX1){
                    //break;
                }
                Calendar calendar=CalendarUtils.getCalendar(selectedCalendarDay);
                if (offsetX1 > mTouchSlop) {
                    if (lastPointX > event.getX() &&
                            Math.abs(lastPointX - event.getX()) >= criticalWidth) {
                        indexMonth++;
                        centerMonth = (centerMonth + 1) % 13;
                        if (centerMonth == 0) {
                            centerMonth = 1;
                            centerYear++;
                        }
                        calendar.add(MONTH,1);
                    } else if (lastPointX < event.getX() &&
                            Math.abs(lastPointX - event.getX()) >= criticalWidth) {
                        indexMonth--;
                        centerMonth = (centerMonth - 1) % 12;
                        if (centerMonth == 0) {
                            centerMonth = 12;
                            centerYear--;
                        }
                        calendar.add(MONTH,-1);
                    }
                    selectedCalendarDay=CalendarUtils.getDate(calendar);
                    computeDate();
                    smoothScrollTo(width * indexMonth, indexYear * height);
                    lastMoveX = width * indexMonth;
                } else {
                    defineRegion((int) event.getX(), (int) event.getY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                invalidateView();
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(measureWidth, (int) (measureWidth * 6F / 7F));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;

        criticalWidth = (int) (1F / 5F * width);
        int cellW = (int) (w / 7F);
        int cellH = (int) (h / 6F);

        circleRadius = cellW*19/20;
        sizeTextGregorian = width / 20F;
        mPaint.setTextSize(sizeTextGregorian);
        float heightGregorian = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;
        sizeTextFestival = width / 40F;
        mPaint.setTextSize(sizeTextFestival);

        float heightFestival = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;
        offsetYFestival1 = (((Math.abs(mPaint.ascent() + mPaint.descent())) / 1.7F) +
                heightFestival / 1.7F + heightGregorian / 1.7F) / 1.7F;
        offsetYFestival=offsetYFestival1-(((Math.abs(mPaint.ascent() + mPaint.descent())) / 2F) +
                heightFestival / 2F + heightGregorian / 2F) / 2F;
        offsetYFestival2 = offsetYFestival1 * 1.2F;
        cellHeight=cellH;
        for (int i = 0; i < MONTH_REGIONS.length; i++) {
            for (int j = 0; j < MONTH_REGIONS[i].length; j++) {
                Region region = new Region();
                region.set((j * cellW), (i * cellH), cellW + (j * cellW),
                        cellW + (i * cellH));
                MONTH_REGIONS[i][j] = region;
            }
        }
        selectPosition=MONTH_REGIONS[getMonthCellRow(ymd)][1].getBounds().top;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        draw(canvas, width * (indexMonth - 1), height * indexYear, leftYear, leftMonth);//上一个月
        draw(canvas, width * indexMonth, indexYear * height, centerYear, centerMonth);//当前月
        draw(canvas, width * (indexMonth + 1), height * indexYear, rightYear, rightMonth);//下一个月
    }
    private void draw(Canvas canvas, int x, int y, int year, int month) {
        canvas.save();
        canvas.translate(x, y);
        List<List<MonthCellDescriptor>> cells=getMonthCells(year,month);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                drawCell(canvas, MONTH_REGIONS[i][j].getBounds(), cells.get(i).get(j));
            }
        }
        canvas.restore();
    }
    private void drawCell(Canvas canvas, Rect rect, MonthCellDescriptor monthCellDescriptor){
        //是否被选中，每个月都有一天是被选中的（当前月才会绘制）
        if(monthCellDescriptor.isSelected){
            mPaint.setColor(getResources().getColor(R.color.colorAccent));
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
            canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius / 2F, mPaint);
        }
        //阳历月字体颜色
        int color=monthCellDescriptor.isCurrentMonth ? getResources().getColor(R.color.colorPrimaryDark): getResources().getColor(R.color.colorPrimary);
        mPaint.setColor(color);
        mPaint.setTextSize(sizeTextGregorian);
        canvas.drawText(monthCellDescriptor.value + "", rect.centerX(), rect.centerY()+offsetYFestival, mPaint);
    }
    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }
    public void setOnDatePickedListener(OnDatePickedListener onDatePickedListener) {
        this.onDatePickedListener = onDatePickedListener;
    }
    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }
    public void setDate(String ymd) {
        this.ymd=ymd;
        centerYear = CalendarUtils.getYear(CalendarUtils.getCalendar(ymd));
        centerMonth = CalendarUtils.getMonth(CalendarUtils.getCalendar(ymd));
        indexYear = 0;
        indexMonth = 0;
        computeDate();
        requestLayout();
        invalidate();
        moveToToday(ymd);
    }

    public void moveToToday(String ymd){
        selectedCalendarDay = ymd;
        if (null != onDatePickedListener) {
            onDatePickedListener.onDatePicked(CalendarUtils.getCalendar(selectedCalendarDay));
        }
        int dx = 0 - mScroller.getFinalX();
        smoothScrollBy(dx, 0);
        lastMoveX = width * indexMonth;
        requestLayout();
        invalidate();
    }

    /**
     *
     * @param dx x轴移动的位移 正直表示向左移动
     * @param dy y轴移动的位移 垂直方向不移动dy=0
     */
    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, 500);
        invalidate();
    }
    private void defineRegion(int x, int y) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                Region region = MONTH_REGIONS[i][j];
                if (!region.contains(x,y)) {
                    continue;
                }
                Calendar selectedCalendar=getMonthCellValue(centerYear, centerMonth, i, j);
                selectedCalendarDay=CalendarUtils.getDate(selectedCalendar);
                invalidate();
                selectPosition = region.getBounds().top;
                if (null != onDatePickedListener) {
                    onDatePickedListener.onDatePicked(selectedCalendar);
                }
                scrollOppositeMonth();
            }
        }
    }

    /**
     * 如果选中的不是当前月，就对应滑到哪一个月
     */
    private void scrollOppositeMonth(){
        int ym=Integer.parseInt(CalendarUtils.getDate(CalendarUtils.getCalendar(selectedCalendarDay),"yyyyMM"));
        int temp=Integer.parseInt(centerYear+""+(centerMonth>9?centerMonth:"0"+centerMonth));
        if (ym>temp){
            indexMonth++;
            centerMonth = (centerMonth + 1) % 13;
            if (centerMonth == 0) {
                centerMonth = 1;
                centerYear++;
            }
        }else if (ym<temp){
            indexMonth--;
            centerMonth = (centerMonth - 1) % 12;
            if (centerMonth == 0) {
                centerMonth = 12;
                centerYear--;
            }
        }
        computeDate();
        smoothScrollTo(width * indexMonth, indexYear * height);
        lastMoveX = width * indexMonth;
    }
    private void computeDate() {
        rightYear = leftYear = centerYear;
        rightMonth = centerMonth + 1;
        leftMonth = centerMonth - 1;

        if (centerMonth == 12) {
            rightYear++;
            rightMonth = 1;
        }
        if (centerMonth == 1) {
            leftYear--;
            leftMonth = 12;
        }
        if(null!=onPageChangeListener){
            onPageChangeListener.onPageChangeListener(CalendarUtils.getCalendar(selectedCalendarDay));
        }
        //滑动完成之后，再次获取所选位置到顶部的距离
        Region region=MONTH_REGIONS[getMonthCellRow(selectedCalendarDay)][1];
        if (null!=region){
            selectPosition=region.getBounds().top;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    List<List<MonthCellDescriptor>> getMonthCells(int year,int month) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(DAY_OF_MONTH, 1);
        //取得按月保存的数据
        int ym=Integer.parseInt(CalendarUtils.getDate(cal.getTime(),"yyyyMM"));
        SparseArray sparseArray=monthCellDescriptorMap.get(ym);
        List<List<MonthCellDescriptor>> cells = new ArrayList<>();
        cal.set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        if (offset > 0) {
            offset -= 7;
        }
        cal.add(Calendar.DATE, offset);
        while (cells.size() < 6) {// 保证每个月都是现实42天
            List<MonthCellDescriptor> weekCells = new ArrayList<>();
            cells.add(weekCells);
            for (int c = 0; c < 7; c++) {
                Date date = cal.getTime();
                int date_value=Integer.parseInt(CalendarUtils.getDate(date));
                if(null==sparseArray){
                    sparseArray=new SparseArray();
                }
                MonthCellDescriptor monthCellDescriptor=(MonthCellDescriptor)sparseArray.get(date_value);
                if(null==monthCellDescriptor){
                    monthCellDescriptor=new MonthCellDescriptor();
                    monthCellDescriptor.isCurrentMonth=(cal.get(MONTH)+1 == month);
                    int value = cal.get(DAY_OF_MONTH);
                    monthCellDescriptor.value=value;
                }
                monthCellDescriptor.isSelected=false;
                if (monthCellDescriptor.isCurrentMonth){
                    monthCellDescriptor.isSelected=(selectedCalendarDay.equals(CalendarUtils.getDate(cal)));
                }
                //保存每天的数据
                sparseArray.put(date_value, monthCellDescriptor);
                weekCells.add(monthCellDescriptor);
                cal.add(DATE, 1);
            }
        }
        //保存每个月的数据
        monthCellDescriptorMap.put(ym,sparseArray);
        return cells;
    }
    Calendar getMonthCellValue(int year,int month,int i,int j) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        if (offset > 0) {
            offset -= 7;
        }
        cal.add(Calendar.DATE, offset);
        cal.add(DATE,i*7+j);
        return cal;
    }

    /**
     * 通过所选天数获取是第几行
     * @param ymd
     * @return
     */
    int getMonthCellRow(String ymd) {
        Calendar cal=CalendarUtils.getCalendar(ymd);
        cal.set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        int diffDay=Math.abs(offset)+CalendarUtils.getDay(CalendarUtils.getCalendar(ymd));
        //返回的行数减一
        return diffDay%7==0?diffDay/7-1:diffDay/7;
    }
    public int getSelectPosition() {
        return selectPosition;
    }
    public int getCellHeight(){
        return cellHeight;
    }

    public boolean isSlide() {
        return isSlide;
    }
}
