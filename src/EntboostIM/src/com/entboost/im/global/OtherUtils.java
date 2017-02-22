package com.entboost.im.global;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import com.entboost.Log4jLog;

public class OtherUtils {
	private static String LONG_TAG = OtherUtils.class.getName();
    
    /**
     * IM API通讯服务
     */
    public static String EB_CLIENT_SERVICE_NAME = "net.yunim.service.EbClientService";
    /**
     * IM主服务
     */
    public static String MAIN_SERVICE_NAME	= "com.entboost.im.service.MainService"; 
    
    /** 
     * 判断某个服务是否正在运行的方法 
     * @param mContext 
     * @param serviceName 包名+服务的类名（例如：net.yunim.service.EBClientService)
     * @param mark 标记，仅用于打印日志
     * @return true代表正在运行，false代表服务没有正在运行 
     */
    public static boolean isServiceWork(Context mContext, String serviceName, String mark) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList.size() <= 0) {  
            return false;
        }
        
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
            	Log4jLog.d(LONG_TAG, "service '" + serviceName +"' is found, mark = " + mark);
                isWork = true;
                break;
            }
        }
        
        if (!isWork)
        	Log4jLog.e(LONG_TAG, "service '" + serviceName +"' is not exist, mark = " + mark);
        return isWork;  
    }	
	
	/**
	 * 检测服务是否正在运行
	 * @param times 次数
	 * @param waitting 如非正在运行，是否延时(标准单位500毫秒)
	 * @param mark 标记
	 * @return
	 */
	public static boolean checkServiceWork(String serviceName, int times, boolean waitting, String mark) {
		//String serviceName = "net.yunim.service.EbClientService";
		
		boolean isWork = OtherUtils.isServiceWork(MyApplication.getInstance(), serviceName, mark);
		if (!isWork && waitting) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return isWork;
	}
	
}
