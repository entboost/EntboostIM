package com.entboost.im.service;

import net.yunim.utils.YINetworkUtils;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.entboost.Log4jLog;
import com.entboost.im.global.IMStepExecutor;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.OtherUtils;

public class MainService extends Service {
	
	private static String TAG = MainService.class.getSimpleName();
	private static String LONG_TAG = MainService.class.getName();
	
	private boolean alive = true;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 执行业务逻辑
	 */
	private void processTask() {
		MyApplication application = (MyApplication)this.getApplication();
		
		boolean isWork = OtherUtils.isServiceWork(this, OtherUtils.EB_CLIENT_SERVICE_NAME, "MainService");
		if (!isWork)
			application.initEbConfig(TAG);
		
		if (!application.isLogin()) {
			IMStepExecutor executor = IMStepExecutor.getInstance();
			executor.executeTryLogon();
		}
	}
	
	@Override
	public void onCreate() {
		synchronized(MainService.class) {
			Log4jLog.i(LONG_TAG, "onCreate1");
			
			if (!YINetworkUtils.isNetworkConnected(getApplication())) {
				Log4jLog.e(LONG_TAG, "network is disconnect");
				//子线程中等待网络开通
				new Thread(new Runnable() {
					@Override
					public void run() {
						Log4jLog.i(LONG_TAG, "waitting for connected network");
						boolean isNetworkConnected = YINetworkUtils.isNetworkConnected(getApplication());
						while(alive && !isNetworkConnected) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								Log4jLog.e(LONG_TAG, "thread Interrupted", e);
							}
							
							isNetworkConnected = YINetworkUtils.isNetworkConnected(getApplication());
						}
						
						Log4jLog.i(LONG_TAG, "end waitting for connected network, result = " + isNetworkConnected);
						
						if (isNetworkConnected)
							processTask();
					}
				}).start();
			} else {
				processTask();
			}
			
			Log4jLog.i(LONG_TAG, "onCreate2");
		}
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		this.alive = false;
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
		//return super.onStartCommand(intent, flags, startId);
	}
}
