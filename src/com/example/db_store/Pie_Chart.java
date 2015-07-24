package com.example.db_store;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Pie_Chart {
	Context context;
	LinearLayout pie_chart;

	Pie_Chart(Context context, LinearLayout pie_chart) {
		this.context = context;
		this.pie_chart = pie_chart;
	}

	// 中間挖空的圓餅圖
	public void create_pie() {
		pie_chart.removeAllViews();
		List<double[]> values = new ArrayList<double[]>();
		// values.add(new double[] { 12, 14, 11, 10, 19 });
		// values.add(new double[] { 10, 9, 14, 20, 11 });
		values.add(new double[] { 80.1, 19.9 });
		int[] colors = new int[] { Color.GREEN, Color.parseColor("#FF9900") };
		List<String[]> titles = new ArrayList<String[]>();
		titles.add(new String[] { "P1", "P2" });
		// titles.add(new String[] { "P1", "P2", "P3", "P4", "P5" });
		// titles.add(new String[] { "Project1", "Project2", "Project3",
		// "Project4", "Project5" });
		// int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA,
		// Color.YELLOW, Color.CYAN };

		DefaultRenderer renderer = buildCategoryRenderer(colors);
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.parseColor("#FFFFEE"));
		renderer.setLabelsColor(Color.BLACK);
		View view = ChartFactory.getDoughnutChartView(context,
				buildMultipleCategoryDataset("Project budget", titles, values),
				renderer);
		view.setBackgroundColor(Color.parseColor("#FFFFCC"));
		pie_chart.addView(view);

	}

	// 普通圓餅圖
	public void create_pie_chart() {
		pie_chart.removeAllViews();
		double[] values = new double[] { 12, 14, 11, 10, 19 };// Integer.valueOf("#FF9900")
		int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA,
				Color.YELLOW, Color.CYAN };
		// double[] values = new double[] { 19.9, 80.1 };
		// int[] colors = new int[] { Color.parseColor("#FF9900"),Color.GREEN};
		DefaultRenderer renderer = buildCategoryRenderer(colors);
		renderer.setZoomButtonsVisible(true);
		// renderer.setZoomEnabled(true);
		renderer.setPanEnabled(false);
		renderer.setChartTitleTextSize(20);
		View view = ChartFactory.getPieChartView(context,
				buildCategoryDataset("Project budget", values), renderer);
		view.setBackgroundColor(Color.parseColor("#FFFFCC"));
		pie_chart.addView(view);
	}

	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	protected MultipleCategorySeries buildMultipleCategoryDataset(String title,
			List<String[]> titles, List<double[]> values) {
		MultipleCategorySeries series = new MultipleCategorySeries(title);
		int k = 0;
		for (double[] value : values) {
			series.add("兌獎人數統計圖", titles.get(k), value);
			k++;
		}
		return series;
	}

	protected CategorySeries buildCategoryDataset(String title, double[] values) {
		CategorySeries series = new CategorySeries(title);
		int k = 0;
		for (double value : values) {
			series.add("Project " + ++k + " (" + value + ")", value);
		}
		return series;
	}
}
