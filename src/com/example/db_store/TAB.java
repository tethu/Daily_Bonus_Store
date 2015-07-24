package com.example.db_store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class TAB extends TabActivity {
	String company;
	String Branch;
	String company_id;
	String Branch_id;
	TabHost tabHost;
	Context context;
	File dir_Internal;
	public List<ImageView> imageList = new ArrayList<ImageView>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tabhost_layout);
		Intent intent = getIntent();
		company = intent.getExtras().getString("company");
	    Branch = intent.getExtras().getString("Branch");
	    company_id = intent.getExtras().getString("company_id");
	    Branch_id = intent.getExtras().getString("Branch_id");
	    context = this;
		dir_Internal = context.getFilesDir();
    	TextView title_tv=(TextView)findViewById(R.id.title_tv);
    	title_tv.setText("公司:"+company+"\n店名:"+Branch);
    	File new_data_status = new File(dir_Internal, "Branch_data.txt");
    	writeToFile(new_data_status,Branch_id);
    	Tab("核帳", R.drawable.icon_1_on, Tab_1.class);
		Tab("紀錄", R.drawable.icon_2_off, Tab_2.class);
		Tab("統計", R.drawable.icon_3_off, Tab_3.class);
		Tab("設置", R.drawable.icon_4_off, Tab_4.class);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				imageList.get(0).setImageDrawable(
						getResources().getDrawable(R.drawable.icon_1_off));
				imageList.get(1).setImageDrawable(
						getResources().getDrawable(R.drawable.icon_2_off));
				imageList.get(2)
						.setImageDrawable(
								getResources().getDrawable(
										R.drawable.icon_3_off));
				imageList.get(3)
				.setImageDrawable(
						getResources().getDrawable(
								R.drawable.icon_4_off));
				
				if (tabId.equals("核帳")) {
					imageList.get(0).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_1_on));
				}
				if (tabId.equals("紀錄")) {
					imageList.get(1).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_2_on));
				}
				if (tabId.equals("統計")) {
					imageList.get(2).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_3_on));
				}
				if (tabId.equals("設置")) {
					imageList.get(3).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_4_on));
				}
			}
		});
		
	}

	

	public void Tab(String name, int image,
			@SuppressWarnings("rawtypes") Class c) {
		tabHost = getTabHost();
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec(name);
		// 建立tab先做一tab將所需的屬性用layoyt完成-如標題、圖像等....
		View tabIndicator = LayoutInflater.from(this).inflate(
				R.layout.tab_layout_set, getTabWidget(), false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.question_tv);
		title.setText(name);// 標題匯入
		//textList.add(title);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.imageView1);
		imageList.add(icon);
		icon.setImageResource(image);// 匯入圖
		spec.setIndicator(tabIndicator);// 將完成的layout匯入tab
		spec.setContent(intent);// 將對應標題(按鈕)跟對應class合再一起
		tabHost.addTab(spec);// 將完成的tab加進物件
	}

	/******************************************* 檔案建立 *******************************************/
	//讀取檔案資料
	  private String readFromFile(File fin) {
	  StringBuilder data = new StringBuilder();
	  BufferedReader reader = null;
	  try {
	      reader = new BufferedReader(new InputStreamReader(
	               new FileInputStream(fin), "utf-8"));
	      String line;
	      while ((line = reader.readLine()) != null) {
	          data.append(line);
	      }
	  } catch (Exception e) {
	      ;
	  } finally {
	      try {
	          reader.close();
	      } catch (Exception e) {
	          ;
	      }
	  }
	  return data.toString();
	}
	// 寫入資料
	private void writeToFile(File fout, String data) {
		FileOutputStream osw = null;
		try {
			osw = new FileOutputStream(fout);
			osw.write(data.getBytes());
			osw.flush();
		} catch (Exception e) {
			;
		} finally {
			try {
				osw.close();
			} catch (Exception e) {
				;
			}
		}
	}

	/******************************************* 檔案建立 *******************************************/

}
