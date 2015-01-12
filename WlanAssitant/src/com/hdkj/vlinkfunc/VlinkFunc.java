/**
 * 
 */
package com.hdkj.vlinkfunc;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author vLink
 *
 */
public class VlinkFunc
{
	//
	private static final String strPrint = "WifiClass";
	public static void Debug(String strInfo)
	{
		Log.d(strPrint, strInfo);
	}
	public static void Error(String strInfo)
	{
		Log.e(strPrint, strInfo);
	}
	/**
	 * Toast
	 * @param context
	 * @param text
	 */
	public static void vLinkToast(Context context,String text)
	{
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
}