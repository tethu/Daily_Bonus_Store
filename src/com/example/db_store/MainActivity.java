package com.example.db_store;


import java.util.ArrayList;
import java.util.List;

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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {
	final String URL = "http://healthifenas.synology.me/php_web/beacon/android/Login_app.php";// 要加上"http://"，否則會連線失敗
	String password="";
	String account="";
	EditText account_edtv;
	EditText password_edtv;
	String company="";
	String company_id="";
	String Branch_id="";
	String Branch="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        account_edtv=(EditText)findViewById(R.id.accout_etv);
        password_edtv=(EditText)findViewById(R.id.password_etv);
    }
    
    public void Login(View v){
    	check_account();
    }
    
    public void check_account(){
    	account=account_edtv.getText().toString();
    	password=password_edtv.getText().toString();
    	if((account.length()==0) || (password.length()==0)){
    		Toast.makeText(getApplicationContext(), "請確實的輸入帳號及密碼", Toast.LENGTH_SHORT).show();
    	}else{
    		try {
    			new Thread(Comparison_account).start();// 啟動執行序runnable
    		} catch (Exception e) {
    			Log.d("LOGIN_ERROR", "登入失敗");
    		}
    	}
    } 
    
    public boolean check_state(String state){
    	for (int i = 0; i < state.length(); i++) {
			char ch;
			ch = state.charAt(i);
			if (ch == '，')
				return true;
		}
    	return false;
    }
    
    public void company_Branch(String data){
    	int address=0;
    	for (int i = 0; i < data.length(); i++) {
			char ch;
			ch = data.charAt(i);
			if (ch == '-')
				address=i;
		}
    	company = data.substring(0, address);
    	Branch = data.substring(address+1, data.length());
    } 
    
    public void id(String data){
    	int address=0;
    	for (int i = 0; i < data.length(); i++) {
			char ch;
			ch = data.charAt(i);
			if (ch == '-')
				address=i;
		}
    	company_id = data.substring(0, address);
    	Branch_id = data.substring(address+1, data.length());
    }
    
    public void cut(String data){
    	String company_data="";
    	String id_data="";
    	int address=0;
    	for (int i = 0; i < data.length(); i++) {
			char ch;
			ch = data.charAt(i);
			if (ch == ';')
				address=i;
		}
    	company_data= data.substring(0, address);
    	id_data = data.substring(address+1,data.length());
    	company_Branch(company_data);
    	id(id_data);
    }
    
    

    public void Login_success(String data){
    	try{
    		cut(data);
        	Intent intent=new Intent();
        	intent.setClass(getApplicationContext(), TAB.class);
        	intent.putExtra("company",company);
        	intent.putExtra("Branch",Branch);
        	intent.putExtra("company_id",company_id);
        	intent.putExtra("Branch_id",Branch_id);
        	startActivity(intent);
        	MainActivity.this.finish();
    	}catch(Exception e){
    		ed_clear();
    		Toast.makeText(getApplicationContext(), "登入失敗，請再嘗試一次。", Toast.LENGTH_SHORT).show();
    	}  	
    }
    
    public void ed_clear(){
        account_edtv.setText("");
        password_edtv.setText("");
    }
	/***************************************** account_check ****************************************/

	private Handler handler_net = new Handler();
	Runnable Comparison_account = new Runnable() {
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

				vars.add(new BasicNameValuePair("ACCOUNT", account));
				vars.add(new BasicNameValuePair("PASSWORD", password));
				method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

				// 接收PHP回傳的資料
				HttpResponse response = httpclient.execute(method);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					data.putString("key", EntityUtils.toString(entity, "utf-8"));// 如果成功將網頁內容存入key
					Comparison_account_handler_Success.sendMessage(msg);

				} else {
					data.putString("key", "無資料");
					Comparison_account_handler_Nodata.sendMessage(msg);
				}
			} catch (Exception e) {
				data.putString("key", "連線失敗，請檢查連線。");
				Comparison_account_handler_Error.sendMessage(msg);
				handler_net.removeCallbacks(Comparison_account);
			}

		}
	};


	Handler Comparison_account_handler_Error = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG)
					.show();
		}
	};

	Handler Comparison_account_handler_Nodata = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			System.out.print(val);
			Log.d("data", val);
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG)
					.show();
		}
	};

	Handler Comparison_account_handler_Success = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");// 取出key中的字串存入val
			String company_data = val.substring(1, val.length());
			
			if(check_state(company_data)){
				Toast.makeText(getApplicationContext(), company_data, Toast.LENGTH_SHORT).show();
				password="";
				account="";
				ed_clear();
			}else
				Login_success(company_data);
			
		}
	};
	/***************************************** account_check ****************************************/

 
}
