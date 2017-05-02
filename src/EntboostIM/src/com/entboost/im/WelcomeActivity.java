package com.entboost.im;

import net.yunim.service.EntboostCache;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import com.entboost.Log4jLog;
import com.entboost.im.global.IMStepExecutor;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.OtherUtils;
import com.entboost.im.service.MainService;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.activity.MyActivityManager;

public class WelcomeActivity extends Activity {

	/** The tag. */
	private static String TAG = WelcomeActivity.class.getSimpleName();
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
        Log4jLog.i(LONG_TAG, this.getClass().getName()+", activity pop");
        MyActivityManager.getInstance().popOneActivity(this);
		
        MyApplication application = (MyApplication)getApplication();
//        application.setWelcomeActivity(null);
		IMStepExecutor.getInstance().exitWelcome();
		
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log4jLog.i(LONG_TAG, "onNewIntent");
		setIntent(intent);
		
		MyApplication application = (MyApplication)getApplication();
		application.setInInterface(true);
		
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log4jLog.i(LONG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		final MyApplication application = (MyApplication)getApplication();
		application.setInInterface(true);
		
	    if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
	        finish();
	        return;  
	    }
		
		setContentView(R.layout.activity_welcome);
		
		//加入管理栈
		Log4jLog.i(LONG_TAG, this.getClass().getName()+", activity push (in onCreate())");
		MyActivityManager.getInstance().pushOneActivity(this);
		
		// 首次开启程序，添加桌面快捷图标
		SharedPreferences preferences = getSharedPreferences("first", Context.MODE_PRIVATE);
		boolean isFirst = preferences.getBoolean("isfrist", true);
		if (isFirst) {
			createDeskShortCut();
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isfrist", false);
		editor.commit();
		
//		application.setWelcomeActivity(this);
		
//		boolean isWork = OtherUtils.checkServiceWork(OtherUtils.EB_CLIENT_SERVICE_NAME, 0, false, "WelcomeActivity");
//		if (!isWork) {
//			application.initEbConfig("WelcomeActivity");
//		}
		
		//启动服务
		boolean isWork = OtherUtils.checkServiceWork(OtherUtils.MAIN_SERVICE_NAME, 0, false, TAG);
		if (!isWork) {
			Intent nIntent = new Intent(this, MainService.class);
			startService(nIntent);
		} else if (!application.isLogin() || !EntboostCache.getDefaultLogon()) {
			//延迟3秒钟后执行
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					//关闭欢迎页
					WelcomeActivity.this.finish();
					
					Intent intent = null;
					if (application.isLogin()) {
						//跳转到主界面
						intent = new Intent(WelcomeActivity.this, MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						
						Bundle bundle = intent.getBundleExtra(MainActivity.EXTRA_BUNDLE);
						if(bundle != null)
							intent.putExtra(MainActivity.EXTRA_BUNDLE, bundle);
					} else {
						//跳转到登录界面
						intent = new Intent(WelcomeActivity.this, LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					}
					
					startActivity(intent);
				}
			}, 3000);
		} else if (application.isLogin()){
			//延迟1秒钟后执行
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					//关闭欢迎页
					WelcomeActivity.this.finish();
					//跳转到主界面
					Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					
					Bundle bundle = intent.getBundleExtra(MainActivity.EXTRA_BUNDLE);
					if(bundle != null)
						intent.putExtra(MainActivity.EXTRA_BUNDLE, bundle);
					
					startActivity(intent);
				}
			}, 1000);
		} else {
			Log4jLog.e(LONG_TAG, "nothing to do");
		}
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
