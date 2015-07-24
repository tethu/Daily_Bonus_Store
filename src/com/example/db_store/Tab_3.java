package com.example.db_store;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

public class Tab_3 extends Activity {
	Pie_Chart pie_chart;
	Bar_chart bar_chart;
	LinearLayout pie_chart_layout;
	LinearLayout layout;
	LinearLayout strip_chart_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_3);
		layout = (LinearLayout) findViewById(R.id.layout);
		pie_chart_layout = (LinearLayout) findViewById(R.id.pie_chart);
		strip_chart_layout = (LinearLayout) findViewById(R.id.strip_chart);
		pie_chart=new Pie_Chart(this,pie_chart_layout);
        pie_chart.create_pie();
        bar_chart=new Bar_chart(this,strip_chart_layout);
    	bar_chart.create_bar_horizontal();
	}

}
