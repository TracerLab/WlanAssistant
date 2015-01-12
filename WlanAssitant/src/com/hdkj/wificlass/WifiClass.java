/**
 * 
 */
package com.hdkj.wificlass;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.hdkj.vlinkfunc.VlinkFunc;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

/**
 * @author vLink
 *
 *	01.
	02. <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"></uses-permission>    
	03. <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>    
	04. <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>    
	05. <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>    
 */
public class WifiClass
{
	//获取WifiManager
	private WifiManager mWifiManager;  
	//获取WifiInfo  
	private WifiInfo mWifiInfo;  
	//保存扫描结果mWifiList
	private List<ScanResult> mWifiList;  
	//已配置好的wifi列表
	private List<WifiConfiguration> mWifiConfigurations;  
	//wifi lock
	private WifiLock mWifiLock;
	//连接wifi的密码，没有则无
	private String mPasswd = "";
	//连接wifi的名称
	private String mSSID = "";  
	//运行context
	private Context mContext;

	//WifiClass
	public WifiClass(Context context){
		//保存context
		mContext = context;
		//WifiManager
		mWifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		//获取mWifiConfigurations
		mWifiConfigurations=mWifiManager.getConfiguredNetworks();  
		//
		mWifiInfo=mWifiManager.getConnectionInfo();
		}  
	
	//创建一个新的Configuration
	public WifiConfiguration createWifiInfo(String SSID, String password, int type) {  
        
        //Log.v(TAG, "SSID = " + SSID + "## Password = " + password + "## Type = " + type);  
          
        WifiConfiguration config = new WifiConfiguration();  
        config.allowedAuthAlgorithms.clear();  
        config.allowedGroupCiphers.clear();  
        config.allowedKeyManagement.clear();  
        config.allowedPairwiseCiphers.clear();  
        config.allowedProtocols.clear();  
        config.SSID = "\"" + SSID + "\"";  
  
        WifiConfiguration tempConfig = this.IsExsits(SSID);  
        if (tempConfig != null) {  
            mWifiManager.removeNetwork(tempConfig.networkId);  
        }  
          
        // 分为三种情况：1没有密码2用wep加密3用wpa加密  
        if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS
        	config.hiddenSSID = true;
            config.wepKeys[0] = "";  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
            config.wepTxKeyIndex = 0;  
              
        } else if (type == TYPE_WEP) {  //  WIFICIPHER_WEP   
            config.hiddenSSID = true;  
            config.wepKeys[0] = "\"" + password + "\"";  
            config.allowedAuthAlgorithms  
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);  
            config.allowedGroupCiphers  
                    .set(WifiConfiguration.GroupCipher.WEP104);  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
            config.wepTxKeyIndex = 0;  
        } else if (type == TYPE_WPA) {   // WIFICIPHER_WPA  
            config.preSharedKey = "\"" + password + "\"";  
            config.hiddenSSID = true;  
            config.allowedAuthAlgorithms  
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
            config.allowedPairwiseCiphers  
                    .set(WifiConfiguration.PairwiseCipher.TKIP);  
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
            config.allowedPairwiseCiphers  
                    .set(WifiConfiguration.PairwiseCipher.CCMP);  
            config.status = WifiConfiguration.Status.ENABLED;  
        }   
          
        return config;  
    }  
 
    //判断要连接的wifi是否已在配置列表中
    private WifiConfiguration IsExsits(String SSID) {  
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();  
        for (WifiConfiguration existingConfig : existingConfigs) {  
            if (existingConfig.SSID.equals("\"" + SSID + "\"")
            		/*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {  
                return existingConfig;  
            }  
        }  
        return null;  
    }  
	
    public static final int WIFI_UNKNOWNERROR = 0x00;  
    public static final int WIFI_CONNECTED = 0x01;  
    public static final int WIFI_CONNECT_FAILED = 0x02;  
    public static final int WIFI_CONNECTING = 0x03; 
    public static final int WIFI_DISCONNECTING = 0x04; 
    public static final int WIFI_DISCONNECTED = 0x05; 
    /** 
     * 判断wifi是否连接成功,不是network 
     *  
     * @param context 
     * @return 
     */  
    public static int isWifiConnected(Context context) {  
        ConnectivityManager connectivityManager = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo wifiNetworkInfo = connectivityManager  
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
          
//        Log.v(TAG, "isConnectedOrConnecting = " + wifiNetworkInfo.isConnectedOrConnecting());  
//        Log.d(TAG, "wifiNetworkInfo.getDetailedState() = " + wifiNetworkInfo.getDetailedState());  
        if (wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR  
                || wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {  
            return WIFI_CONNECTING;  
        } else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {  
            return WIFI_CONNECTED;  
        } else if(wifiNetworkInfo.getDetailedState() == DetailedState.DISCONNECTING) {
			return WIFI_DISCONNECTING;
		}else if (wifiNetworkInfo.getDetailedState() == DetailedState.DISCONNECTED){
			return WIFI_DISCONNECTED;
		}
        else if(wifiNetworkInfo.getDetailedState() == DetailedState.FAILED){  
            //Log.d(TAG, "getDetailedState() == " + wifiNetworkInfo.getDetailedState());  
            return WIFI_CONNECT_FAILED;  
        }
        else {
			return WIFI_UNKNOWNERROR;
		}
    }  
    //
    public void removeNetwork(String ssid)
	{
    	WifiConfiguration configuration = IsExsits(ssid);
		if (configuration != null)
		{
			mWifiManager.removeNetwork(configuration.networkId);
		}
	}
    /**
     * 添加一个网络但不连接
     * @param ssid
     */
    public void addNetwork(String ssid)
	{
    	WifiConfiguration cfg = null;
    	cfg = IsExsits(ssid);
    	if (cfg != null)
		{
    		mWifiManager.removeNetwork(cfg.networkId);
		}
        WifiConfiguration config = new WifiConfiguration();// =  createWifiInfo(ssid, "", TYPE_NO_PASSWD);
        config.allowedAuthAlgorithms.clear();  
        config.allowedGroupCiphers.clear();  
        config.allowedKeyManagement.clear();  
        config.allowedPairwiseCiphers.clear();  
        config.allowedProtocols.clear();  
        config.SSID = "\"" + ssid + "\"";  
        config.hiddenSSID = true;
        //config.wepKeys[0] = "";  
        //config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
        //config.wepTxKeyIndex = 0;  
        mWifiManager.addNetwork(config);
        mWifiManager.saveConfiguration();
	}
    /**
     * 添加一个已配置的网络并连接
     * @param wcg
     * @return
     */
    public boolean addNetwork(WifiConfiguration wcg) {  
              
        int wcgID = mWifiManager.addNetwork(wcg);  
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        String szInfo = b ? "connect success" : "connect fail";
        VlinkFunc.Debug(szInfo);
        return b;
    }  
      
    public static final int TYPE_NO_PASSWD = 0x11;  
    public static final int TYPE_WEP = 0x12;  
    public static final int TYPE_WPA = 0x13;  
      
    public boolean addNetwork(String ssid, String passwd, int type) {  
        if (ssid == null || passwd == null || ssid.equals("")) {  
            VlinkFunc.Debug("addNetwork() ## nullpointer error!");  
            return false;  
        }  
          
        if (type != TYPE_NO_PASSWD && type != TYPE_WEP && type != TYPE_WPA) {  
            VlinkFunc.Debug("addNetwork() ## unknown type = " + type);
        }  
        startTimer();   
        //unRegister();  
        if(addNetwork(createWifiInfo(ssid, passwd, type)))
        {
            stopTimer(); 
            return true;
        }
        return false;
    }  

	/**
	 * 打开wifi设备  
	 */
	public void openWifi(){
		if(!mWifiManager.isWifiEnabled()){
			if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING){
				VlinkFunc.Debug("openWifi");
				mWifiManager.setWifiEnabled(true);	
				VlinkFunc.vLinkToast(mContext, "Open WLAN");
			}
		}  
	}
	//关闭wifi
	public void closeWifi(){
		if (mWifiManager.isWifiEnabled()){
			if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLING){
				VlinkFunc.Debug("closeWifi");
				mWifiManager.setWifiEnabled(false);
				VlinkFunc.vLinkToast(mContext, "Close WLAN");
			}
		}
	}
	/**
	 * 关闭wifi设备  
	 */
	public int checkWifiState() {    
		return mWifiManager.getWifiState();    
		}    
	//wifiLock  
	public void createWifiLock(){  
		mWifiLock = mWifiManager.createWifiLock("hdkj");  
	}
	//wifiLock  
	public void acquireWifiLock(){  
		mWifiLock.acquire();  
		}  
	//wifiLock  
	public void releaseWifiLock(){  
	//
		if(mWifiLock.isHeld()){
			mWifiLock.release();  
			}  
		}  
    /**
     * 断开指定ID的网络  
     * @param netId
     */
    public void disconnectWifi(int netId) {  
        mWifiManager.disableNetwork(netId);  
        mWifiManager.disconnect();  
    }  
	/**
	 * 启动wifi扫描，获取周围可用的wifi设备
	 */
	public void startScan(){  
		mWifiManager.startScan();  
		//得到扫描结果
		mWifiList=mWifiManager.getScanResults(); 
		}
	//
	public List<WifiConfiguration> getWifiConfigurations()
	{
		return mWifiManager.getConfiguredNetworks(); 
	}
	//
	/** Anything worse than or equal to this will show 0 bars. */  
	private static final int MIN_RSSI = -100;  
	/** Anything better than or equal to this will show the max bars. */  
	private static final int MAX_RSSI = -55;  
	/**
	 * 对信号强度进行分级，一般为0,1,2,3,4
	 * @param rssi
	 * @return
	 */
	public static int calculateSignalLevel(int rssi)
	{  
				/* in general, numLevels is 4  */  
		final int numLevels = 5;
		if (rssi <= MIN_RSSI) 
		{  
			return 0;  
		} 
		else if (rssi >= MAX_RSSI) 
		{
			return numLevels - 1;
		} else 
		{
			float inputRange = (MAX_RSSI - MIN_RSSI);
			float outputRange = (numLevels - 1);
			return (int)((float)(rssi - MIN_RSSI) * outputRange / inputRange);
		}
	}  
	/**
	 * 获取刚刚扫描完的可用wifi列表
	 * @return
	 */
	public List<ScanResult> getWifiList(){  
		return mWifiList;  
		}  
    //  
    public void connetionConfiguration(int index){  
        if(index>mWifiConfigurations.size()){  
            return ;  
        }  
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);  
        //mWifiManager.
    }  
    // 得到MAC地址  
    public String getMacAddress() {  
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();  
    }  
  
    // 得到接入点的BSSID  
    public String getBSSID() {  
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();  
    }  
  
    // 得到IP地址  
    public int getIPAddress() {  
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();  
    }  
  
    // 得到连接的ID  
    public int getNetworkId() {  
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();  
    }  
  
    // 得到WifiInfo的所有信息包  
    public String getWifiInfo() {  
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();  
    }  
    
    /**
     * 在进程销毁的时候需要调用这个函数
     */
    public void clearWifiConfiguration()
	{
		//unRegister();
	}
    private Timer mTimer = null;  
    private void startTimer() {  
        if (mTimer != null) {  
            stopTimer();  
        }  
          
        mTimer = new Timer(true);  
//      mTimer.schedule(mTimerTask, 0, 20 * 1000);// 20s  
        mTimer.schedule(new TimerTask()
		{	
			@Override
			public void run()
			{
				// TODO 自动生成的方法存根
	            // TODO Auto-generated method stub  
	            VlinkFunc.Error("timer out!");  
	            //onNotifyWifiConnectFailed(); 
	            VlinkFunc.Debug("connect wifi failed");
	            //VlinkFunc.vLinkToast(mContext, "网络连接超时");
	            //unRegister();  
			}
		}, 20 * 1000);  
    }  
    private void stopTimer() {  
        if (mTimer != null) {  
            mTimer.cancel();  
            mTimer = null;  
        }  
    }  
}