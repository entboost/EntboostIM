package com.entboost.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.entboost.Log4jLog;
import com.entboost.im.global.OtherUtils;
import com.entboost.im.service.MainService;

public class BootReceiver extends BroadcastReceiver {

	private static String LONG_TAG = BootReceiver.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//启动Activity
		   //Intent nIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
		   //nIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		   //context.startActivity(nIntent);
		
//			Intent nIntent=new Intent(context, WelcomeActivity.class);
//			nIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(nIntent);
		
		//启动服务
		boolean isWork = OtherUtils.checkServiceWork(OtherUtils.MAIN_SERVICE_NAME, 0, false, "BootReceiver");
		if (!isWork) {
			Intent nIntent = new Intent(context, MainService.class);
			context.startService(nIntent);
		} else {
			Log4jLog.i(LONG_TAG, "main service is already started, miss it");
		}
	}
}
