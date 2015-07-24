package com.example.db_store;

import static android.provider.BaseColumns._ID;
import static com.example.db_store.DbConstants.DATE;
import static com.example.db_store.DbConstants.MATH;
import static com.example.db_store.DbConstants.MISTAKE;
import static com.example.db_store.DbConstants.TABLE_NAME;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
public class Tab_2 extends Activity {
	ListView database_list;
	private DBHelper dbhelper = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_2);
		database_list=(ListView)findViewById(R.id.database_list);
		openDatabase();
		initView();
	}
	

	/*************************************** sql  ***************************************/

	private void initView() {
		database_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});
	}

	private Cursor getCursor() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String[] columns = { _ID, DATE, MATH, MISTAKE};
		int list_f = 0;

		Cursor cursor = db.query(TABLE_NAME, columns, null,
				null, null, null, null);
		if (cursor.getCount() > 0) {
			
			
			while (cursor.moveToNext()) {
			}
		}
		startManagingCursor(cursor);
		return cursor;
	}

	// 顯示資料庫所有的資料
	private void showInList() {
		Cursor cursor = getCursor();
		String[] from = { DATE,MATH,MISTAKE };
		int[] to = { R.id.txtDate,R.id.txtMath,R.id.txtMistake};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.sqlite_listview, cursor, from, to);
		database_list.setAdapter(adapter);
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDatabase();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		showInList();
	}


	private void openDatabase() {
		dbhelper = new DBHelper(this);
	}

	private void closeDatabase() {
		dbhelper.close();
	}

	/*************************************** sql  ***************************************/

}
