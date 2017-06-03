package calendar.lme.com.calendardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.pink.pinkutils.CalendarUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private CalendarMonthView calendarMonthView;
    private TextView dateTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();
        initList();
    }
    private void initView(){
        dateTitle= (TextView) findViewById(R.id.dateTitle);
        calendarMonthView= (CalendarMonthView) findViewById(R.id.calendarMonthView);
        calendarMonthView.setDate(CalendarUtils.getDate(Calendar.getInstance()));
        dateTitle.setText(CalendarUtils.getDate(Calendar.getInstance(),"yyyy-MM-dd"));
        calendarMonthView.setOnDatePickedListener(new OnDatePickedListener() {
            @Override
            public void onDatePicked(Calendar calendar) {
                DateFormat sdf=SimpleDateFormat.getDateTimeInstance();
                dateTitle.setText(sdf.format(calendar.getTime())+"");
            }
        });
        calendarMonthView.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageChangeListener(Calendar calendar) {
                dateTitle.setText(CalendarUtils.getDate(calendar,"yyyy-MM-dd"));
            }
        });
        dateTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarMonthView.setDate("20170602");
            }
        });
    }
    private void initList() {
        ListView mList = (ListView) findViewById(R.id.list);
        mList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 100;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, null);
                }

                TextView textView = (TextView) convertView;
                textView.setText("position:" + position);

                return convertView;
            }
        });
    }

}
