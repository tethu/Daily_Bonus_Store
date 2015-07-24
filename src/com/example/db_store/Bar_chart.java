package com.example.db_store;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import android.widget.LinearLayout;

public class Bar_chart {

	Context context;
	LinearLayout bar_layout;
	
	Bar_chart(Context context,LinearLayout bar_layout){
		this.context=context;
		this.bar_layout=bar_layout;
	}

	public void create_bar_horizontal(){
		bar_layout.removeAllViews();
		String[] titles = new String[] { "2007"};
	    //String[] titles = new String[] { "2007", "2008" };
	    List<double[]> values = new ArrayList<double[]>();
	    values.add(new double[] { 100, 101, 102, 103, 104, 105});
	    /*values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500,
	        11600, 13500 });
	    /*values.add(new double[] { 14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500,
	        12600, 14000 });*/
	    int[] colors = new int[] { Color.CYAN};
	    //int[] colors = new int[] { Color.CYAN, Color.BLUE };
	    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        
        
        /*********background**********/
        renderer.setApplyBackgroundColor(false);
        renderer.setBackgroundColor(Color.parseColor("#EEE9BF"));
        renderer.setMarginsColor(Color.parseColor("#EEE9BF"));
        /*********background**********/
	    renderer.setOrientation(Orientation.VERTICAL);
	    setChartSettings(renderer, "", "", "", 0, 12.5, 0, 240, Color.RED, Color.YELLOW);
	    renderer.setXLabels(0);
	    renderer.setYLabels(10);
        renderer.setBarSpacing(1);  
        renderer.setShowGrid(true);   
	    int length = renderer.getSeriesRendererCount();
	    for (int i = 0; i < length; i++) {
	      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
	      seriesRenderer.setDisplayChartValues(true);
	    }
	    View view=ChartFactory.getBarChartView(context,  buildBarDataset(titles, values), renderer, Type.DEFAULT);
	    view.setBackgroundColor(Color.parseColor("#FFFFFF"));
	    bar_layout.addView(view);
	}
	
	protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    int length = colors.length;
	    for (int i = 0; i < length; i++) {
	      SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	      r.setColor(colors[i]);
	      renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	  }

	  protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    int length = titles.length;
	    for (int i = 0; i < length; i++) {
	      CategorySeries series = new CategorySeries(titles[i]);
	      double[] v = values.get(i);
	      int seriesLength = v.length;
	      for (int k = 0; k < seriesLength; k++) {
	        series.add(v[k]);
	      }
	      dataset.addSeries(series.toXYSeries());
	    }
	    return dataset;
	  }
	  

	   
	  protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
	      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
	      int labelsColor) {
	    renderer.setChartTitle(title);
	    renderer.setXTitle(xTitle);
	    renderer.setYTitle(yTitle); 
	    renderer.setXAxisMin(xMin);
	    renderer.setXAxisMax(xMax);
	    renderer.setYAxisMin(yMin);
	    renderer.setYAxisMax(yMax);
	    renderer.setAxesColor(axesColor);
	    renderer.setLabelsColor(labelsColor);
	  }
}
