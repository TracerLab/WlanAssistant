/**
 * 
 */
package com.hdkj.wlanassitant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.hdkj.vlinkfunc.VlinkFunc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author vLink
 *
 */
public class AssitantActivity extends Activity
{

	//各种组件
	private TextView mTextTitle;
	private TextView mTextRecv;
	private EditText mSendText;
	private EditText mIpText1;
	private EditText mIpText2;
	private EditText mIpText3;
	private EditText mIpText4;
	
	private CheckBox mCheckBox;
	private EditText mTargetPort;
	private Button mSendButton;
	private Button mClearButton;
	private Button mRetButton;
	//private WifiClass mWifiClass;// = new WifiClass(getParent());
	private SendThread mSendThread = new SendThread();
	private RecvThread mRecvThread;
	//
	private void returnToWlan()
	{
		finish();
	}
	/**
	 * 初始化各个组件
	 */
	private void init()
	{
		mTextTitle = (TextView)findViewById(R.id.textView0);
		mTextRecv = (TextView)findViewById(R.id.textView1);
		mSendText = (EditText)findViewById(R.id.etSendEdit);
		mIpText1 = (EditText)findViewById(R.id.etIp1);
		mIpText2 = (EditText)findViewById(R.id.etIp2);
		mIpText3 = (EditText)findViewById(R.id.etIp3);
		mIpText4 = (EditText)findViewById(R.id.etIp4);
		mCheckBox = (CheckBox)findViewById(R.id.checkBox1);
		mTargetPort = (EditText)findViewById(R.id.etTargetPort);
		mSendButton = (Button)findViewById(R.id.button_send);
		mClearButton = (Button)findViewById(R.id.button_clear);
		mSendButton.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				String szIp = mIpText1.getText().toString() + "." + 
							  mIpText2.getText().toString() + "." +
						      mIpText3.getText().toString() + "." + 
							  mIpText4.getText().toString();
				String szInfo = mSendText.getText().toString();
				String szTarPort = mTargetPort.getText().toString();
				if (mSendThread.getState() == Thread.State.NEW)
				{
					//mSendThread = new SendThread();
					mSendThread.start();
					mSendThread.setUdpInfo(szIp, szInfo, Integer.parseInt(szTarPort));
					mSendThread.enableSend(true);
				}
				mSendThread.setUdpInfo(szIp, szInfo, Integer.parseInt(szTarPort));
				mSendThread.enableSend(true);				
			}
		});
		mClearButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				mTextRecv.setText("");
				mSendText.setText("");
				//mSendText.setTextColor(Color.argb(0xff, 0xc0, 0xc0, 0xc0));
			}
		});
		mRetButton = (Button)findViewById(R.id.button_ret);
		mRetButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				returnToWlan();
			}
		});
	
	}
	/**
	 * 获取本地IP地址
	 * @return
	 */
	private String GetLocalIpAddress()
	{
		//WifiManager
		WifiManager mWifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager == null)
		{
			return "未连接";
		}
		WifiInfo mWifiInfo=mWifiManager.getConnectionInfo();
		if (mWifiInfo == null)
		{
			return "未连接";
		}
		int ipAddr = mWifiInfo.getIpAddress();
		//return android.text.format.Formatter.formatIpAddress(ipAddr);
		VlinkFunc.Debug(String.valueOf(ipAddr));
		StringBuilder szIp = new StringBuilder(); 
		szIp.append(String.valueOf((ipAddr & 0x000000ff)) + ".");
		szIp.append(String.valueOf((ipAddr & 0x0000ffff)>>>8)+".");
		szIp.append(String.valueOf((ipAddr & 0x00ffffff)>>>16)+".");
		szIp.append(String.valueOf(((ipAddr >>> 24))));
		return szIp.toString();
	}
	/**
	 * 动态设置各个组件的高度和宽度
	 */
	private void initWidthAndHeight()
	{
		WindowManager wmManager = getWindowManager();
		DisplayMetrics dMetrics = new DisplayMetrics();
		wmManager.getDefaultDisplay().getMetrics(dMetrics);
		int height = dMetrics.heightPixels;
		int width = dMetrics.widthPixels;
		//int sendHeight = mSendText.getHeight();
		//Log.d("WifiClass", String.valueOf(sendHeight));
		mTextRecv.setHeight((int)(height*0.4));
		mTextRecv.setMovementMethod(ScrollingMovementMethod.getInstance());
		mSendText.setWidth((int)(width*0.4));
		//mSendButton.setWidth((int)(width*0.2));
		//mSendText.setHeight((int)(height*0.1));
		//mIpText1.setHeight((int)(height*0.1));
		//mIpText2.setHeight((int)(height*0.1));
		//mIpText3.setHeight((int)(height*0.1));
		//mIpText4.setHeight((int)(height*0.1));
		
		//mTargetPort.setHeight((int)(height*0.1));
		//mSendButton.setHeight((int)(height*0.1));
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_assitant);
		mRecvThread = new RecvThread(mSendThread.getDatagramSocket());
		mRecvThread.start();
		init();
		
		mTextTitle.append("本机: IP:"+GetLocalIpAddress()+"  端口:"+mSendThread.getLocalPort());
		registerReceiver(mBroadcastReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
	}


	
	/**
	 * 广播，监听wifi变化
	 */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {  
    	  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            // TODO Auto-generated method stub  
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) { 
            	NetworkInfo netInfo = 
            (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
	            if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)){
	            	VlinkFunc.Debug("wifi is connected");
	            	mTextTitle.setText("本机: IP:"+GetLocalIpAddress()+
	            	"  端口:"+mSendThread.getLocalPort());
				}
	            else if(netInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
					VlinkFunc.Debug("wifi is disconnected");
	            	mTextTitle.setText("本机: IP:未连接"+
	            	"  端口:无");
				}
            }
        }  
    };  
	@Override
	protected void onResume()
	{
		// TODO 自动生成的方法存根
		super.onResume();
		//初始化发送区的高度
		initWidthAndHeight();
		
	}
	@Override
	protected void onDestroy()
	{
		// TODO 自动生成的方法存根
		super.onDestroy();
		//关闭UDP连接，销毁发送线程
		if (mSendThread != null)
		{
			mSendThread.closeUdpSocket();
			if (!mSendThread.isInterrupted())
			{
				mSendThread.interrupt();
			}
			mSendThread = null;
		}
		unregisterReceiver(mBroadcastReceiver);
	}
	/**
	 * 将接收的内容显示到textview上去
	 */
	private void showRecvBuffer()
	{
		if (mCheckBox.isChecked())
		{
			StringBuilder szRecv= new StringBuilder();
			for(int i = 0; i<mRealDataCount;i++)
			{
				szRecv.append(Integer.toString((mDataBuffer[i] & 0xff)+0x100,16).substring(1)+
						" ");  
			}
			szRecv.append("\n");
			mTextRecv.append(szRecv.toString());
		}
		else {
			mTextRecv.append(mStringBuffer+"\n");
		}
	}
	/********消息处理类*************/
	private MyHandler mHandler = new MyHandler();
	private final int MSG_SHOWDATA = 0x4821;
	class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			// TODO 自动生成的方法存根
			switch (msg.what)
			{
			case MSG_SHOWDATA:
				showRecvBuffer();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
	/**********数据接收缓存区，包括字节型和字符串型************/
	private byte[] mDataBuffer = new byte[1024*1024];//1M
	private int mRealDataCount = 0;
	private String mStringBuffer;
	/************************************************************/
	class RecvThread extends Thread{

		private DatagramSocket mDatagramSocket;
		public RecvThread(DatagramSocket ds)
		{
			mDatagramSocket= ds;
		}

		@Override
		public void run()
		{
			// TODO 自动生成的方法存根
			while(true)
			{
				try
				{
					sleep(200);
				} catch (InterruptedException e1)
				{
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
					continue;
				}
				//
				for(int i = 0; i < mDataBuffer.length;i++)
				{
					mDataBuffer[i] = 0;
				}
				DatagramPacket datagramPacket = new DatagramPacket(mDataBuffer, mDataBuffer.length);
				try
				{
					//datagramPacket.
					mDatagramSocket.receive(datagramPacket);
					//
					int byteCount = datagramPacket.getLength();
					mRealDataCount = byteCount;
					mStringBuffer = new String(mDataBuffer, 0, byteCount, "UTF-8");
					//向activity发送消息
					mHandler.sendEmptyMessage(MSG_SHOWDATA);
					VlinkFunc.Debug(mStringBuffer);
				} catch (IOException e)
				{
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}

	}
	/************************************************************/
    class SendThread extends Thread
    {
    	private boolean bWait = true;
    	public void enableSend(boolean bEnabled)
		{
			synchronized (this)
			{
				bWait = !bEnabled;
			}
		}
    	//
        //private  String mLocalAddress;//默认都是8080
        private  int mLocalPort = 8080;
        public int getLocalPort()
		{
			return mLocalPort;
		}
    	private String mSendIp;
    	private String mSendInfo;
    	private int mSendPort;
    	//
    	private DatagramSocket mDatagramSocket;// = new DatagramSocket();
    	public DatagramSocket getDatagramSocket()
		{
			return mDatagramSocket;
		}
    	//
    	public  SendThread()
		{
    		try
			{
    			//初始化数据发送socket
				mDatagramSocket = new DatagramSocket();
				mLocalPort = mDatagramSocket.getLocalPort();
				VlinkFunc.Debug("LocalPort:" +String.valueOf(mLocalPort));
				//mLocalAddress = mDatagramSocket.getLocalAddress().getHostAddress();
				//VlinkFunc.Debug(mLocalAddress);
			} catch (SocketException e)
			{
				mDatagramSocket = null;
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
    	//关闭UDP连接
    	public void closeUdpSocket()
		{
			if (mDatagramSocket != null)
			{
				mDatagramSocket.close();
			}
		}
    	//供外部调用的初始化UDP info的函数
		public synchronized void setUdpInfo(String szIpAddress,String szInfo,int TargetPort)
		{
			mSendInfo = szInfo;
			mSendIp = szIpAddress;
			mSendPort = TargetPort;
		}
		@Override
		public void run()
		{
			// TODO 自动生成的方法存根
			while (true)
			{
				if (bWait){
					try
					{
						sleep(500);
					} catch (InterruptedException e)
					{
						// TODO 自动生成的 catch 块
						e.printStackTrace();
						synchronized (this){
							bWait = true;	
						}
					}
					continue;
				}else {
					try
					{
						InetAddress ipAddress = InetAddress.getByName(mSendIp);
						//VlinkFunc.Debug(ipAddress.toString());
						//将字符串转换为字节型数组
						byte[] data = mSendInfo.getBytes();
						DatagramPacket dataPacket = new DatagramPacket(data,data.length , ipAddress, mSendPort);
						
						if (mDatagramSocket != null)
						{
							mDatagramSocket.send(dataPacket);
							VlinkFunc.Debug("send success");
						}
						synchronized (this){
							bWait = true;	
						}
					} catch (UnknownHostException e){
						// TODO 自动生成的 catch 块
						synchronized (this){
							bWait = true;	
						}
						e.printStackTrace();
					} catch (IOException e){
						// TODO 自动生成的 catch 块
						synchronized (this){
							bWait = true;	
						}
						e.printStackTrace();
					}
				}
			}
		}
    }
}
