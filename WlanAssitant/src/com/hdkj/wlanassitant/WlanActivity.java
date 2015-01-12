package com.hdkj.wlanassitant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hdkj.vlinkfunc.VlinkFunc;
import com.hdkj.wificlass.WifiClass;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class WlanActivity extends Activity {

	private WifiClass mWifiClass;
	/*******three buttons*******/
	private Button mOpenButton;
	private Button mCloseButton;
	private Button mScanButton;
	private Button mAddButton;
	private Button mLinkedButton;
	/************ListView*******/
	private ListView mListView;
	/**********save the scan result**********/
	private List<ScanResult> mScanResults;
	/**
	 * init activity modules
	 */
	private void initModules()
	{
		mListView = (ListView)findViewById(R.id.listView1);
		mListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id)
			{
				// TODO 自动生成的方法存根
				ScanResult srResult = mScanResults.get(pos);
				String string = srResult.capabilities;
				if (string.contains("WPA"))
				{
					wifiLink(srResult.SSID,WifiClass.TYPE_WPA);
				}
				else if (string.contains("WEP")) {
					wifiLink(srResult.SSID,WifiClass.TYPE_WEP);
				}
				else {
					if (mWifiClass!= null)
					{
						if(mWifiClass.addNetwork(srResult.SSID, "", WifiClass.TYPE_NO_PASSWD))
						{
							mHandler.sendEmptyMessage(MSG_CONNECTED);
						}
						else {
							mHandler.sendEmptyMessage(MSG_UNCONNECTED);
						}
					}
				}
			}
		});
		mOpenButton = (Button)findViewById(R.id.button_open);
		mOpenButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				if (mWifiClass != null)
				{
					mWifiClass.openWifi();
				}
			}
			
		});
		mCloseButton= (Button)findViewById(R.id.button_close);
		mCloseButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				if (mWifiClass!= null)
				{
					mWifiClass.closeWifi();
				}
			}
			
		});
		mScanButton = (Button)findViewById(R.id.button_scan);
		mScanButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				ScanWlan();
			}
		});
		mAddButton = (Button)findViewById(R.id.button_add);
		mAddButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//添加未广播的无线网络
				linkUnbroadcastWlan();
			}
		});
		mLinkedButton = (Button)findViewById(R.id.button_assitant);
		mLinkedButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				startActivity(new Intent(getApplicationContext(), AssitantActivity.class));
			}
		});

	}
    /*********记录密码编辑框*********/
	private EditText mEditInputKey;
	/**
	 * 点击listview进行wifi连接
	 * @param ssid
	 * @param keyType
	 */
	private void wifiLink(final String ssid,final int keyType)
	{
    	LinearLayout layout = 
    	(LinearLayout)getLayoutInflater().inflate(R.layout.key_input,null);
    	    	
    	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	    	builder.setTitle("Wifi连接");
    	    	builder.setView(layout);
    	mEditInputKey = (EditText)layout.findViewById(R.id.etInputKey);
    	//设置确定和取消按钮
    	builder.setPositiveButton("确定",new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String sPassWord = mEditInputKey.getText().toString();
				if (sPassWord.equals(""))
				{
					return;
				}
				if (mWifiClass!=null)
				{
					if(mWifiClass.addNetwork(ssid, sPassWord, keyType))
					{
						//notify handler
						mHandler.sendEmptyMessage(MSG_CONNECTED);
					}
					else {
						mHandler.sendEmptyMessage(MSG_UNCONNECTED);
					}
				}
			}
		});
    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO 自动生成的方法存根
				
			}
		});
    	builder.create().show();
	}
	/**
	 * 添加未广播的无线网络
	 * @param ssid
	 * @param passWord
	 * @param keyType
	 * @return
	 */
	private void linkUnbroadcastWlan()
	{
    	RelativeLayout layout = 
    	(RelativeLayout)getLayoutInflater().inflate(R.layout.unbroadcast_layout,null);
    	    	
    	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	    	builder.setTitle("添加无线网络");
    	    	builder.setView(layout);
    	final EditText mEditName = (EditText)layout.findViewById(R.id.etWlanName);
    	final EditText mEditPassword = (EditText)layout.findViewById(R.id.etPassWord);
    	//设置确定和取消按钮
    	builder.setPositiveButton("确定",new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String strName = mEditName.getText().toString();
				if(strName.equals("")) return;
				String strWord = mEditPassword.getText().toString();
				if (!mWifiClass.addNetwork(strName, strWord, WifiClass.TYPE_NO_PASSWD))
				{
					if (!mWifiClass.addNetwork(strName, strWord, WifiClass.TYPE_WPA))
					{
						if (!mWifiClass.addNetwork(strName, strWord, WifiClass.TYPE_WEP))
						{
							mHandler.sendEmptyMessage(MSG_UNCONNECTED);
						}
						else {
							mHandler.sendEmptyMessage(MSG_CONNECTED);
						}
					}
					else {
						mHandler.sendEmptyMessage(MSG_CONNECTED);
					}
				}
				else {
					mHandler.sendEmptyMessage(MSG_CONNECTED);
				}
			}
		});
    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO 自动生成的方法存根
				
			}
		});
    	builder.create().show();
    	
	}
	/**
	 * get wifi network
	 * @return
	 */
	private List<ScanResult> getNearWlan()
	{
		mWifiClass.startScan();
		return mWifiClass.getWifiList();
	}
	/**
	 * 扫描无线网络
	 */
	private void ScanWlan()
	{
		mScanResults = getNearWlan();
		int len = mScanResults.size();
		String[] strWlanName = new String[len];
		String[] strBSSID = new String[len];
		String[] strKeyDesc = new String[len];
		int[] wlanSignal = new int[len];
		int[] imageId = new int[]{R.drawable.signal_4,
								  R.drawable.signal_3,
								  R.drawable.signal_2,
								  R.drawable.signal_1,
								  R.drawable.signal_0};
		int[] imageKeyId = new int[]{R.drawable.signal_lock_4,
									  R.drawable.signal_lock_3,
									  R.drawable.signal_lock_2,
									  R.drawable.signal_lock_1,
									  R.drawable.signal_lock_0};
		for (int i = 0; i < len; i++)
		{
			strWlanName[i] = mScanResults.get(i).SSID;
			strBSSID[i] = mScanResults.get(i).BSSID;
			strKeyDesc[i] = mScanResults.get(i).capabilities;
			wlanSignal[i] = WifiClass.calculateSignalLevel(mScanResults.get(i).level);
			//VlinkFunc.Debug(sResults.get(i).capabilities);
		}
		//sResults.get(0).capabilities;
		List<Map<String, Object>> listItems = new ArrayList<Map<String,Object>>();
		for (int j = 0; j < strWlanName.length; j++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			if (strKeyDesc[j].contains("WPA") || strKeyDesc[j].contains("WEP"))
			{
				listItem.put("signal", imageKeyId[wlanSignal[j]]);	
			}
			else {
				listItem.put("signal", imageId[wlanSignal[j]]);	
			}
			listItem.put("wlanname", strWlanName[j]);
			listItem.put("bssid", strBSSID[j]);
			listItems.add(listItem);
		}
		SimpleAdapter sAdapter = new SimpleAdapter(this,listItems,
				R.layout.layout_list_item,
				new String[]{"wlanname","signal","bssid"},
				new int[]{R.id.tvWlanName,R.id.ivSignalView,R.id.tvNetworkId});
		mListView.setAdapter(sAdapter);
		
	}
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlan);
        /******create a wificlass instance******/
        mWifiClass = new WifiClass(this);
        initModules();
        /******default open wifi******/
        mWifiClass.openWifi();
    }
	@Override
	protected void onResume()
	{
		// TODO 自动生成的方法存根
		super.onResume();
		ScanWlan();
	}
	/**********消息处理事件处理***********/
	private EventHandler mHandler = new EventHandler();
	/******定义消息常量******/
	private final int MSG_CONNECTED = 0x6661;
	private final int MSG_UNCONNECTED = 0x6662;
	class EventHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			// TODO 自动生成的方法存根
			switch (msg.what)
			{
			case MSG_CONNECTED:
				VlinkFunc.vLinkToast(getApplication(), "connect success");
				startActivity(new Intent(getApplicationContext(),AssitantActivity.class));
				break;
			case MSG_UNCONNECTED:
				VlinkFunc.vLinkToast(getApplication(), "connect fail");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
	
}
