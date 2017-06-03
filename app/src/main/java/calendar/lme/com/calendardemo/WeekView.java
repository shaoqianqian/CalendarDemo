package calendar.lme.com.calendardemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pink.pinkutils.DensityUtils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Created by Administrator on 2017/5/31.
 */

public class WeekView extends LinearLayout {
    String weekDayNames[] = { "日", "一", "二", "三", "四", "五", "六" };
    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 设置排列方向为竖向
        setOrientation(VERTICAL);
        LayoutParams llParams = new LayoutParams(LayoutParams.MATCH_PARENT, DensityUtils.dp2px(context,20));
        // 周视图根布局
        LinearLayout llWeek = new LinearLayout(context);
        llWeek.setOrientation(HORIZONTAL);
        LayoutParams lpWeek = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lpWeek.weight = 1;
        // --------------------------------------------------------------------------------周视图
        for (int i = 0; i < 7; i++) {
            TextView tvWeek = new TextView(context);
            tvWeek.setText(weekDayNames[i]);
            tvWeek.setGravity(Gravity.CENTER);
            tvWeek.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            llWeek.addView(tvWeek, lpWeek);
        }
        addView(llWeek, llParams);
    }
}
