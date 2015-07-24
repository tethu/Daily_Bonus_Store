package com.example.db_store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static android.provider.BaseColumns._ID;
import static com.example.db_store.DbConstants.DATE;
import static com.example.db_store.DbConstants.MATH;
import static com.example.db_store.DbConstants.MISTAKE;
import static com.example.db_store.DbConstants.TABLE_NAME;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Tab_1 extends Activity {
	int time_sec;
	boolean write_f = false;
	String close_bill_math = ""; // 黑盒子累積數量
	String close_bill_server_math = ""; // server累積數量
	String math = ""; // 黑盒子累積數量
	String server_math = ""; // server累積數量
	String test_Instruction = "s,et,01,rct,ffff,e";
	String clear_Instruction = "s,et,01,cct,ffff,e";
	final String URL = "http://healthifenas.synology.me/php_web/beacon/android/search.php";// 要加上"http://"，否則會連線失敗
	boolean now_state = false;
	String Branch_id = "";
	String system_time = "";
	Context context;
	File dir_Internal;
	TextView box_times_tv;
	TextView server_times_tv;
	TextView search_time_tv;
	TextView time_limit_tv;
	ImageButton switch_im_but;
	ImageButton check_im_but;
	String search_time = "";
	File search_log;
	LinearLayout time_limit_layout;
	LinearLayout user_input_layout;
	EditText user_times_etv;
	boolean close_bill = false;
	private DBHelper dbhelper = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_1);
		ble_basic_set();
		context = this;
		dir_Internal = context.getFilesDir();
		box_times_tv = (TextView) findViewById(R.id.box_times_tv);
		server_times_tv = (TextView) findViewById(R.id.server_times_tv);
		search_time_tv = (TextView) findViewById(R.id.search_time_tv);
		time_limit_tv = (TextView) findViewById(R.id.time_limit_tv);
		user_times_etv = (EditText) findViewById(R.id.user_times_etv);
		check_im_but = (ImageButton) findViewById(R.id.check_but);
		switch_im_but = (ImageButton) findViewById(R.id.switch_im_but);
		time_limit_layout = (LinearLayout) findViewById(R.id.time_limit_layout);
		user_input_layout = (LinearLayout) findViewById(R.id.user_input_layout);
		search_log = new File(dir_Internal, "search_log.txt");
		search_log_data(readFromFile(search_log));
		File new_data_status = new File(dir_Internal, "Branch_data.txt");
		Branch_id = readFromFile(new_data_status);
	}

	public void check_but(View v) {
		try {

			if (!now_state) // 查詢
				ble_enable();
			else if (close_bill) {// 關帳
				if (user_times_etv.length() != 0) {
					if (getCursor(system_time))
						close_bill_close();
					else {
						close_bill_end();
						close_bill_server_math = "";
						close_bill_math = "";
						user_times_etv.setText("");
						Toast.makeText(getApplicationContext(),
								"今日已經核帳過了，請明日再核帳。", Toast.LENGTH_SHORT).show();
					}
				} else
					Toast.makeText(getApplicationContext(), "請確實的填入資料",
							Toast.LENGTH_SHORT).show();
			} else
				ble_enable();

		} catch (Exception e) {
			Log.d("check_but_error", "" + e.getMessage());
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT)
					.show();
		}

	}

	public void search_log_data(String data) {
		if (data.length() != 0) {
			int address_f = 0;
			int address[] = new int[2];
			for (int i = 0; i < data.length(); i++) {
				char ch;
				ch = data.charAt(i);
				if (ch == ',') {
					address[address_f] = i;
					address_f++;
				}
			}
			math = data.substring(0, address[0]);
			server_math = data.substring(address[0] + 1, address[1]);
			search_time = data.substring(address[1] + 1, data.length());
			server_times_tv.setText(server_math);
			box_times_tv.setText(Integer.valueOf(math) + "");
			search_time_tv.setText(search_time);
		}
	}

	public void switch_but(View v) {
		try {

			if (now_state) { // 切到查詢
				switch_im_but.setBackgroundResource(R.drawable.switch_search);
				check_im_but.setBackgroundResource(R.drawable.button_activate);
				now_state = false;
				clear_tv();
				search_log_data(readFromFile(search_log));
				user_input_layout.setVisibility(View.GONE);
				time_limit_layout.setVisibility(View.GONE);
			} else { // 切到關帳
				switch_im_but.setBackgroundResource(R.drawable.switch_close);

				now_state = true;
				user_input_layout.setVisibility(View.VISIBLE);
				clear_tv();
				if (close_bill) {
					user_times_etv.setVisibility(View.VISIBLE);
					box_times_tv.setText(close_bill_math);
					server_times_tv.setText(close_bill_server_math);
					check_im_but
							.setBackgroundResource(R.drawable.button_confirm);
					server_times_tv.setText(close_bill_server_math);
					box_times_tv.setText(Integer.valueOf(close_bill_math) + "");
				} else {
					user_times_etv.setVisibility(View.INVISIBLE);
					check_im_but
							.setBackgroundResource(R.drawable.button_activate_check);
				}
				time_limit_layout.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			Log.d("worry_but", "error");
		}

	}

	public void search(String data) {
		try {
			// clear_write();
			String save_data = "";
			success();
			if ((data.length() != 0) && (data != null)) {
				server_math = data;
				server_times_tv.setText(data);
			} else {
				server_math = "0";
				server_times_tv.setText("0");
			}
			box_times_tv.setText(Integer.valueOf(math) + "");
			search_time_tv.setText(search_time);
			time();
			save_data = math + "," + data + "," + search_time;
			writeToFile(search_log, save_data);
			stop_scan = false;
			Handler handler = new Handler();
			timer.cancel();
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			handler.removeCallbacks(SCAN_BLE_Device);
			mLeDeviceListAdapter.clear();
		} catch (Exception e) {
			Log.d("search_check", "search");
			Toast.makeText(getApplicationContext(), "search_error",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void check(String data) {
		try {
			sec_time.cancel();
		} catch (Exception e) {
		}
		try {
			// clear_write();
			success();
			if ((data.length() != 0) && (data != null)) {
				close_bill_server_math = data;
				server_times_tv.setText(data);
			} else {
				close_bill_server_math = "0";
				server_times_tv.setText("0");
			}
			box_times_tv.setText(Integer.valueOf(close_bill_math) + "");
			time();
			close_bill = true;
			check_im_but.setBackgroundResource(R.drawable.button_confirm);
			user_times_etv.setVisibility(View.VISIBLE);
			stop_scan = false;
			Handler handler = new Handler();
			timer.cancel();
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			handler.removeCallbacks(SCAN_BLE_Device);
			mLeDeviceListAdapter.clear();
			sec_60();
		} catch (Exception e) {
			Log.d("search_check", "check");
			Toast.makeText(getApplicationContext(), "search_error",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void clear_tv() {
		server_times_tv.setText("");
		box_times_tv.setText("");
		search_time_tv.setText("");
	}

	public void clear_write() {
		try {
			write(clear_Instruction);
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close_bill_end() {
		final Handler handler = new Handler();
		sec_time.cancel();
		close_bill = false;
		if (now_state) {
			server_times_tv.setText("");
			box_times_tv.setText("");
			check_im_but
					.setBackgroundResource(R.drawable.button_activate_check);
		}
		time_limit_tv.setText("");
		user_times_etv.setVisibility(View.GONE);
		handler.removeCallbacks(sec_60);
	}

	public void close_bill_close() {
		time();
		close_bill_end();
		Log.d("check_but_error", "1");
		int mistake = Integer.valueOf(close_bill_server_math)
				- Integer.valueOf(user_times_etv.getText().toString());
		Log.d("check_but_error", "2");
		add(system_time, user_times_etv.getText().toString(), "" + mistake);
		Log.d("check_but_error", "3");
		close_bill_server_math = "";
		close_bill_math = "";
		user_times_etv.setText("");
		Toast.makeText(getApplicationContext(), "successful_close_bill",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindService(mServiceConnection);
			mBluetoothLeService = null;
			File new_data_status = new File(dir_Internal, "Branch_data.txt");
			writeToFile(new_data_status, "");
			closeDatabase();
		} catch (Exception e) {
			Log.d("onDestroy_error", "" + e.getMessage());
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		openDatabase();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == REQUEST_ENABLE_BT
					&& resultCode == Activity.RESULT_CANCELED) {
				finish();
				return;
			}
		} catch (Exception e) {
			Log.d("onActivityResult", "" + e.getMessage());
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
			mGattCharacteristics.clear();
			getApplicationContext().unbindService(mServiceConnection);
			mLeDeviceListAdapter.clear();
			stop_scan = false;
			scanLeDevice(false);
			Thread.sleep(500);
		} catch (Exception e) {
			Log.d("onPause_error", "" + e.getMessage());
		}

	}

	/*************************************** sql ***************************************/

	private boolean getCursor(String date) {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String[] columns = { _ID, DATE, MATH, MISTAKE };
		int list_f = 0;

		Cursor cursor = db.query(TABLE_NAME, columns, DATE + "=" + "?",
				new String[] { date }, null, null, null);
		if (cursor.getCount() > 0)
			return false;
		startManagingCursor(cursor);
		return true;
	}

	public void add(String date, String math, String mistake) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DATE, date);
		values.put(MATH, math);
		values.put(MISTAKE, mistake);
		db.insert(TABLE_NAME, null, values);
	}

	private void openDatabase() {
		dbhelper = new DBHelper(this);
	}

	private void closeDatabase() {
		dbhelper.close();
	}

	/*************************************** sql ***************************************/

	/*************************************** 抓取系統時間&計時 ***************************************/

	Date curDate;
	Timer sec_time;
	Runnable sec_60;

	public void time() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatter2 = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		curDate = new Date(System.currentTimeMillis());
		system_time = formatter.format(curDate);
		search_time = formatter2.format(curDate);
	}

	public void sec_60() {
		try {
			sec_time.cancel();
		} catch (Exception e) {
		}
		stop_scan = true;
		time_sec = 60;
		sec_60 = new Runnable() {
			@Override
			public void run() {
				try {
					final Handler handler = new Handler();
					time_limit_tv.setText("" + time_sec);
					time_sec--;
					if (time_sec == 0) {
						close_bill_end();
						close_bill_server_math = "";
						close_bill_math = "";
						user_times_etv.setText("");
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "error2",
							Toast.LENGTH_SHORT).show();
				}

			}
		};
		long period = 1000;
		final Handler handler = new Handler();
		sec_time = new Timer();
		sec_time.schedule(new TimerTask() {
			@Override
			public void run() {
				if (time_sec > 0) {
					handler.removeCallbacks(sec_60);
					handler.post(sec_60);
				} else
					handler.removeCallbacks(sec_60);
			}
		}, 0, period);
	}

	/*************************************** 抓取系統時間 ***************************************/

	/******************************************* 檔案建立 *******************************************/
	// 讀取檔案資料
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

	/***************************************** 伺服器累積次數 ****************************************/

	private Handler handler_net = new Handler();
	Runnable Accumulate = new Runnable() {
		@Override
		public void run() {
			Message msg = new Message();
			Bundle data = new Bundle();
			msg.setData(data);
			try {
				// 連線到 url網址
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost method = new HttpPost(URL);

				// 傳值給PHP
				List<NameValuePair> vars = new ArrayList<NameValuePair>();
				time();

				vars.add(new BasicNameValuePair("Branch_Id", Branch_id));
				vars.add(new BasicNameValuePair("TIME", system_time));
				method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

				// 接收PHP回傳的資料
				HttpResponse response = httpclient.execute(method);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					data.putString("key", EntityUtils.toString(entity, "utf-8"));// 如果成功將網頁內容存入key
					Accumulate_handler_Success.sendMessage(msg);

				} else {
					data.putString("key", "無資料");
					Accumulate_handler_Nodata.sendMessage(msg);
				}
			} catch (Exception e) {
				data.putString("key", "連線失敗，請檢查連線。");
				Accumulate_handler_Error.sendMessage(msg);
				handler_net.removeCallbacks(Accumulate);
			}

		}
	};

	Handler Accumulate_handler_Error = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_SHORT)
					.show();
		}
	};

	Handler Accumulate_handler_Nodata = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			System.out.print(val);
			Log.d("data", val);
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_SHORT)
					.show();
		}
	};

	Handler Accumulate_handler_Success = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");// 取出key中的字串存入val
			String company_data = val.substring(1, val.length());
			if (now_state)
				check(company_data);// 關帳模式
			else
				search(company_data); // 查詢模式
		}
	};

	/***************************************** 伺服器累積次數 ****************************************/

	/*************************************** BLE ***************************************/

	boolean sucess_flag = false;

	public boolean check_Instruction(String data) {
		int address = 0;
		String check_data = "";
		for (int i = 0; i < data.length(); i++) {
			char ch;
			ch = data.charAt(i);
			if (ch == ',')
				address = i;
		}
		if (now_state) {
			close_bill_math = data.substring(address - 4, address);
			check_data = close_bill_math;
		} else {
			math = data.substring(address - 4, address);
			check_data = math;
		}
		if (check_data.equals("ffff"))
			return false;
		else
			return true;
	}

	public void ble_enable() {
		try {
			handler_times.removeCallbacks(updateTimer);
		} catch (Exception e) {
		}
		try {
			handler_times.removeCallbacks(scan_time_runnable);
		} catch (Exception e) {
		}
		try {
			action_in = new Queue_rssi(-70, 0);
			sucess_flag = false;
			Thread.sleep(500);
			TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
			svan_tv.setText("掃描裝置中...");
			mLeDeviceListAdapter = new LeDeviceListAdapter();
			scan_list.setAdapter(mLeDeviceListAdapter);
			scanLeDevice(true);
			success_flag = false;
			use_offsetting_dialog();
			stop_scan = true;
			Trade_in = true;
			device_name = "";
			times(scan_time_runnable);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "讀取錯誤", Toast.LENGTH_SHORT)
					.show();
		}
	}

	String UUID = "";
	boolean ble_status_f = false;
	String device_name = "";
	/**************************** ble_connect_read&write ***************************/
	Runnable SCAN_BLE_Device;
	String now_connect_address = "";
	String uuid, ble_rssi;
	String[] ble_rssi_array = new String[1000];
	String[] ble_uuid_array = new String[1000];
	int ble_rssi_f = 0;
	int check_f = 0;
	int check_uuid_status = 0;
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	int uuid_f = 0;
	boolean Trade_in = true;
	private TextView mConnectionState;
	private TextView mDataField;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	public BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	ListView scan_list;
	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	int updata_database_data = 0;
	int tag = 0;
	Timer timer;
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	ListView database_list;
	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 1500;

	// Code to manage Service lifecycle.
	public final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e("BLE_connect", "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Toast.makeText(getApplicationContext(), "connect_error",
					Toast.LENGTH_SHORT).show();
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
				write_f = true;
				Toast.makeText(getApplicationContext(), "連接成功，請稍待資料更新",
						Toast.LENGTH_SHORT).show();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {

				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				String data = intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA);

				try {
					Log.d("Data2:", "2:" + data);
					if (check_Instruction(data)) {
						Log.d("Data_error:", "1:" + data);
						if (Trade_in) {
							Log.d("Data_error:", "2:" + data);
							handler_times.removeCallbacks(updateTimer);
							Log.d("Data_error:", "3:" + data);
							success_flag = true;
							Trade_in = false;
							Log.d("Data_error:", "4:" + data);
							new Thread(Accumulate).start();// 啟動執行序runnable
							Log.d("Data_error:", "5:" + data);
						}
						Log.d("Data_error:", "1:" + data);
					}
				} catch (Exception e) {
					dialog_rescan();
					Toast.makeText(getApplicationContext(), "廣播錯誤",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	public void ble_basic_set() {

		/**************************** ble_connect_read&write ***************************/
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);

		/**************************** ble_connect_read&write ***************************/

		scan_list = (ListView) findViewById(R.id.scan_list);
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
	}

	public void write(String write_data) {

		try {
			Notifications();
			Thread.sleep(200);
			if (mGattCharacteristics != null) {
				int add = 0;
				if (check_uuid_status == 2)
					add = 0;
				if (check_uuid_status == 4)
					add = 1;

				BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(uuid_f - 1).get(add);

				byte[] value = new byte[1];
				value = ascll(write_data);
				characteristic.setValue(value);
				mBluetoothLeService.writeCharacteristic(characteristic);

			}

		} catch (Exception e) {
			Log.d("worry", "write1");
			// TODO Auto-generated catch block
			dialog_rescan();
			Toast.makeText(getApplicationContext(), "error_write",
					Toast.LENGTH_SHORT).show();
		}

	}

	private byte[] ascll(String data) {
		byte[] byte_data;
		byte[] byte_data_check;
		byte_data = data.getBytes();
		byte_data_check = new byte[byte_data.length + 2];
		byte_data_check[0] = 0x13;
		for (int i = 0; i < byte_data.length; i++) {
			byte_data_check[i + 1] = byte_data[i];
		}
		byte_data_check[byte_data_check.length - 1] = 0x0A;
		Log.d("Length", "" + byte_data.length);
		return byte_data_check;
	}

	public void Notifications() {
		try {
			Thread.sleep(100);
			int add = 1;
			if (check_uuid_status == 2)
				add = 1;
			if (check_uuid_status == 4)
				add = 0;
			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(uuid_f - 1).get(add);

				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					mBluetoothLeService.setCharacteristicNotification(
							characteristic, true);
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		int f = 0;
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);
		String unknownCharaString = getResources().getString(
				R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			f = f + 1;
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			Log.d("UUID_information", uuid);

			currentServiceData.put(LIST_NAME,
					SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				uuid = gattCharacteristic.getUuid().toString();
				if (uuid.equals("0000fff3-0000-1000-8000-00805f9b34fb")) {// 將此uuid以外的空間隱掉

					charas.add(gattCharacteristic);
					HashMap<String, String> currentCharaData = new HashMap<String, String>();
					uuid_f = f;
					currentCharaData.put(LIST_NAME, SampleGattAttributes
							.lookup(uuid, unknownCharaString));
					currentCharaData.put(LIST_UUID, uuid);
					gattCharacteristicGroupData.add(currentCharaData);
					if (check_f == 0)
						check_f = 2;
				}

				if (uuid.equals("0000fff4-0000-1000-8000-00805f9b34fb")) {
					charas.add(gattCharacteristic);
					HashMap<String, String> currentCharaData = new HashMap<String, String>();
					uuid_f = f;
					currentCharaData.put(LIST_NAME, SampleGattAttributes
							.lookup(uuid, unknownCharaString));
					currentCharaData.put(LIST_UUID, uuid);
					gattCharacteristicGroupData.add(currentCharaData);
					if (check_f == 0)
						check_f = 4;
				}

			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
			check_uuid_status = check_f;

		}

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				this, gattServiceData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 }, gattCharacteristicData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		mGattServicesList.setAdapter(gattServiceAdapter);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	/**************************** ble_connect_read&write ***************************/

	boolean stop_scan = false;

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			SCAN_BLE_Device = new Runnable() {
				@Override
				public void run() {
					Log.d("scan_device", "out");
					try {
						if (stop_scan) {
							Log.d("scan_device", "in");
							// Stops scanning after a pre-defined scan period.
							mScanning = true;
							ble_rssi_f = 0;
							for (int i = 0; i < ble_rssi_array.length; i++)
								ble_rssi_array[i] = "";
							for (int i = 0; i < ble_uuid_array.length; i++)
								ble_uuid_array[i] = "";
							mBluetoothAdapter.startLeScan(mLeScanCallback);
							invalidateOptionsMenu();
						}
					} catch (Exception e) {
						Log.d("SCAN_BLE_DEVICE", "" + e.getMessage());
					}
				}
			};
			long period = 1000;
			final Handler handler = new Handler();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					handler.removeCallbacks(SCAN_BLE_Device);
					handler.post(SCAN_BLE_Device);
					invalidateOptionsMenu();
					TextView status = (TextView) findViewById(R.id.connection_state);
					if (status.getText().toString().equals("Connected")
							&& write_f) {
						try {
							Thread.sleep(500);
							write(test_Instruction);
							write_f = false;
							Log.d("write", "write");
							timer.cancel();
							mScanning = false;
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							handler.removeCallbacks(SCAN_BLE_Device);
							mLeDeviceListAdapter.clear();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Log.d("worry", "write3");
							// Toast.makeText(getApplicationContext(),
							// "write_error", Toast.LENGTH_SHORT).show();
						}

					} else {

					}
				}
			}, 0, period);
		} else {
			mScanning = false;
			final Handler handler = new Handler();
			handler.removeCallbacks(SCAN_BLE_Device);
			timer.cancel();
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}

	}

	// Adapter for holding devices found through scanning.
	public class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = Tab_1.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			Log.d("BLE_connect", "Connect request result=");
			ViewHolder viewHolder;
			try {
				// General ListView optimization code.
				if (view == null) {
					ble_rssi = "";
					view = mInflator
							.inflate(R.layout.listitem_device_ble, null);
					viewHolder = new ViewHolder();
					viewHolder.deviceAddress = (TextView) view
							.findViewById(R.id.device_address);
					viewHolder.deviceName = (TextView) view
							.findViewById(R.id.device_name);
					viewHolder.devicerssi = (TextView) view
							.findViewById(R.id.device_rssi);
					view.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) view.getTag();
				}

				BluetoothDevice device = mLeDevices.get(i);
				final String deviceName = device.getName();
				if (deviceName != null && deviceName.length() > 0)
					viewHolder.deviceName.setText(deviceName);
				else
					viewHolder.deviceName.setText(R.string.unknown_device);
				viewHolder.deviceAddress.setText(device.getAddress());
				viewHolder.devicerssi.setText("rssi:" + ble_rssi_array[i]);
				int check_rssi = Integer.parseInt(ble_rssi_array[i]);

				Log.d("BLE_RSSI", "" + check_rssi);

				if (device.getName().toString().equals("null")) {
					rescan();
					Log.d("error_scan", "-1" + device.getName());
				}

				if (device.getName().length() != 0 && device.getName() != null) {
					if (ble_uuid_array[i]
							.equals("f510ffdb-0001-0001-0001-000000000000")) {
						boolean in;
						in = action_in.Queue_function(check_rssi);

						if (in) {
							handler_times.removeCallbacks(scan_time_runnable);
							UUID = ble_uuid_array[i];
							device_name = device.getAddress();
							tag = 2;
							stop_scan = false;
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
							svan_tv.setText("掃描到裝置，連接中...");
							mDeviceAddress = device.getAddress();
							registerReceiver(mGattUpdateReceiver,
									makeGattUpdateIntentFilter());
							Intent gattServiceIntent = new Intent(Tab_1.this,
									BluetoothLeService.class);
							getApplicationContext().bindService(
									gattServiceIntent, mServiceConnection,
									BIND_AUTO_CREATE);
							if (svan_tv.getText().toString()
									.equals("掃描到裝置，連接中..."))
								times(updateTimer);
							else
								handler_times.removeCallbacks(updateTimer);

						}
					}
				}
			} catch (Exception e) {
				Log.d("SCAN_error", "" + e.getMessage());
			}
			return view;
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			int startByte = 2;

			boolean patternFound = false;
			// 寻找ibeacon

			while (startByte <= 5) {
				if (((int) scanRecord[startByte + 2] & 0xff) == 0x02
						&& ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
					patternFound = true;
					break;
				}
				startByte++;
			}
			if (patternFound) {
			} else {

				ble_rssi = "" + rssi;
				ble_rssi_array[ble_rssi_f] = ble_rssi;
				String msg = "";
				int i = 0;
				for (byte b : scanRecord) {
					i++;
					if (i >= 6 && i <= 21)
						msg = String.format("%02x", b) + msg;
					if (i == 11)
						msg = "-" + msg;
					else if (i == 13)
						msg = "-" + msg;
					else if (i == 15)
						msg = "-" + msg;
					else if (i == 17)
						msg = "-" + msg;
				}
				ble_uuid_array[ble_rssi_f] = "" + msg;
				ble_rssi_f = ble_rssi_f + 1;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						mLeDeviceListAdapter.addDevice(device);
						mLeDeviceListAdapter.notifyDataSetChanged();

					}
				});

			}
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView devicerssi;
	}

	String[] company_name_list_SQLdata;
	String[] trade_in_list_SQLdata;
	String[] status_list_SQLdata;

	/******************************************* dialog *******************************************/

	AlertDialog dialog;

	public void use_offsetting_dialog() {
		AlertDialog.Builder use_offsetting_dialog_builder = new AlertDialog.Builder(
				Tab_1.this);
		LayoutInflater inflater = LayoutInflater.from(Tab_1.this);
		final View v = inflater.inflate(R.layout.scan_device_dialog, null);
		v.setLayerType(1, null);
		use_offsetting_dialog_builder.setView(v).setCancelable(false)
				.setPositiveButton("中斷", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							sucess_flag = true;
							handler_times.removeCallbacks(updateTimer);
							handler_times.removeCallbacks(scan_time_runnable);
							TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
							svan_tv.setText("");
							mBluetoothLeService.disconnect();
							mBluetoothLeService.close();
							mGattCharacteristics.clear();
							getApplicationContext().unbindService(
									mServiceConnection);
							Trade_in = true;
							dialog.dismiss();
							stop_scan = false;
							scanLeDevice(false);
							mLeDeviceListAdapter.clear();
							Thread.sleep(500);
						} catch (Exception e) {
							Log.d("worry", "中斷失敗");
						}

					}
				});
		dialog = use_offsetting_dialog_builder.show();
	}

	public void success() {
		try {
			handler_times.removeCallbacks(updateTimer);
		} catch (Exception e) {
		}
		try {
			handler_times.removeCallbacks(scan_time_runnable);
		} catch (Exception e) {
		}
		
		try {
			TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
			svan_tv.setText("");
			Trade_in = false;
			dialog.dismiss();
			stop_scan = false;
			scanLeDevice(false);
			final Handler handler = new Handler();
			timer.cancel();
			handler.removeCallbacks(SCAN_BLE_Device);
			Thread.sleep(350);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "updata_error",
					Toast.LENGTH_SHORT).show();
		}

		try {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
			mGattCharacteristics.clear();
			getApplicationContext().unbindService(mServiceConnection);
			Thread.sleep(500);
			Log.d("stop_scan_disconnect", "successful");
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "無連接中的裝置可關閉",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void dialog_rescan() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("連接失敗");
		builder.setMessage("可以再次讀取或中斷");
		builder.setCancelable(false);
		builder.setNeutralButton("取消中斷", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					sucess_flag = true;
					TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
					svan_tv.setText("");
					dialog.dismiss();
					handler_times.removeCallbacks(updateTimer);
					handler_times.removeCallbacks(scan_time_runnable);
					mBluetoothLeService.disconnect();
					mBluetoothLeService.close();
					mGattCharacteristics.clear();
					getApplicationContext().unbindService(mServiceConnection);
					Trade_in = true;
					dialog.dismiss();
					stop_scan = false;
					scanLeDevice(false);
					mLeDeviceListAdapter.clear();
					Thread.sleep(500);
				} catch (Exception e) {
					Log.d("worry", "中斷讀取失敗");
				}

			}
		});
		DialogInterface.OnClickListener onclick1 = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					rescan();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Connect_Error",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		builder.setPositiveButton("繼續讀取", onclick1);
		builder.show();
	}

	Queue_rssi action_in;

	public void rescan() {
		action_in = new Queue_rssi(-70, 0);
		if (!sucess_flag) {
			TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
			// LinearLayout beacon_layout = (LinearLayout)
			// findViewById(R.id.beacon_layout);
			// LinearLayout connect_layout = (LinearLayout)
			// findViewById(R.id.connect_layout);
			try {
				svan_tv.setText("");
				// beacon_layout.setVisibility(View.VISIBLE);
				// connect_layout.setVisibility(View.GONE);
				scanLeDevice(false);
				mBluetoothLeService.disconnect();
				mBluetoothLeService.close();
				mGattCharacteristics.clear();
				getApplicationContext().unbindService(mServiceConnection);
				Trade_in = true;
				Thread.sleep(500);
			} catch (Exception e) {
				Log.d("rescan_disconnect_error", "");
			}
			try {
				svan_tv.setText("掃描裝置中...");
				mLeDeviceListAdapter = new LeDeviceListAdapter();
				scan_list.setAdapter(mLeDeviceListAdapter);
				scanLeDevice(true);
				times(scan_time_runnable);
				// beacon_layout.setVisibility(View.GONE);
				// connect_layout.setVisibility(View.VISIBLE);
				success_flag = false;
				stop_scan = true;
				device_name = "";
				write_f = true;
				try {
					handler_times.removeCallbacks(updateTimer);
				} catch (Exception x) {
				}
				Thread.sleep(500);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Connect_Error",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/******************************************* dialog *******************************************/

	/******************************************* time *******************************************/

	private Long startTime;
	private Handler handler_times = new Handler();
	private boolean success_flag = false;
	int rescan_flag = 0;
	int updateTimer_time = 0;
	int scan_time_runnable_time = 0;

	// 計時
	public void times(Runnable time) {
		rescan_flag = 0;
		updateTimer_time = 0;
		scan_time_runnable_time = 0;
		// 取得目前時間
		startTime = System.currentTimeMillis();
		// 設定定時要執行的方法
		handler_times.removeCallbacks(time);
		// 設定Delay的時間
		handler_times.postDelayed(time, 1000);

	}

	// 計時開始
	private Runnable updateTimer = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startTime;
			// 計算目前已過秒數
			Long seconds = (spentTime / 1000) % 60;
			int updateTimer_time = Integer.valueOf("" + seconds);

			if (updateTimer_time == 5) {

				TextView status = (TextView) findViewById(R.id.connection_state);
				if (!(status.getText().toString().equals("Connected") || success_flag)) {
					rescan_flag++;
					if (rescan_flag == 3)
						dialog_rescan();
					else
						rescan();
				} else {
					handler_times.removeCallbacks(updateTimer);
					if (!success_flag) {
						try {
							Thread.sleep(500);
							write(test_Instruction);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Log.d("worry", "write3");
						}
						Toast.makeText(getApplicationContext(), "re_write",
								Toast.LENGTH_SHORT).show();
					}
				}
				Log.d("worry", "time_" + seconds);
			} else
				handler_times.postDelayed(this, 1000);

			Log.d("worry", "time_" + seconds);
		}
	};

	// 計時開始
	private Runnable scan_time_runnable = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startTime;
			// 計算目前已過秒數
			Long seconds = (spentTime / 1000) % 60;
			int scan_time_runnable_time = Integer.valueOf("" + seconds);
			if (scan_time_runnable_time == 2) {
				handler_times.removeCallbacks(scan_time_runnable);
				rescan();
			} else
				handler_times.postDelayed(this, 1000);
			Log.d("worry", "time_" + seconds);
		}
	};

	/******************************************* time *******************************************/

	/*************************************** BLE ***************************************/
}
