package com.entboost.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;

import com.entboost.Log4jLog;
import com.entboost.im.global.IMStepExecutor;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.OtherUtils;
import com.entboost.ui.base.activity.MyActivityManager;

public class WelcomeActivity extends Activity {

	/** The tag. */
	//private static String TAG = WelcomeActivity.class.getSimpleName();
	private static String LONG_TAG = WelcomeActivity.class.getName();

	@Override
	protected void onStart() {
		Log4jLog.i(LONG_TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Log4jLog.i(LONG_TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log4jLog.i(LONG_TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		Log4jLog.i(LONG_TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log4jLog.i(LONG_TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onResume() {
		Log4jLog.i(LONG_TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Log4jLog.i(LONG_TAG, "onDestroy");
		
    	// Activity退出时出栈
        Log4jLog.d(LONG_TAG, this.getClass().getName()+", activity pop");
        MyActivityManager.getInstance().popOneActivity(this);
		
		IMStepExecutor.getInstance().exitWelcome();
		
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log4jLog.i(LONG_TAG, "onNewIntent");
		
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log4jLog.i(LONG_TAG, "onCreate");
		
		MyApplication application = (MyApplication)getApplication();
		application.setWelcomeActivity(this);
		
		boolean isWork = OtherUtils.checkServiceWork(0, false, "WelcomeActivity");
		if (!isWork) {
			application.initEbConfig("WelcomeActivity");
		}
		
		//加入管理栈
		Log4jLog.d(LONG_TAG, this.getClass().getName()+", activity push (in onCreate())");
		MyActivityManager.getInstance().pushOneActivity(this);
		
		setContentView(R.layout.activity_welcome);
		
		// 首次开启程序，添加桌面快捷图标
		SharedPreferences preferences = getSharedPreferences("first", Context.MODE_PRIVATE);
		boolean isFirst = preferences.getBoolean("isfrist", true);
		if (isFirst) {
			createDeskShortCut();
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isfrist", false);
		editor.commit();
		
		IMStepExecutor.getInstance().executeTryLogon(this);
	}
	
	/**
	 * 创建快捷方式
	 */
	public void createDeskShortCut() {
		// 创建快捷方式的Intent
		Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重复创建
		shortcutIntent.putExtra("duplicate", false);
		// 需要现实的名称
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));

		// 快捷图片
		Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
		// 下面两个属性是为了当应用程序卸载时桌面上的快捷方式会删除
		//intent.setAction("android.intent.action.MAIN");
		intent.setAction(Intent.ACTION_MAIN);
		//intent.addCategory("android.intent.category.LAUNCHER");
		intent.addCategory(Intent.CATEGORY_LAUNCHER); 
		// 点击快捷图片，运行的程序主入口
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		
		// 发送广播。OK
		sendBroadcast(shortcutIntent);
	}
}
