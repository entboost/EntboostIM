package com.entboost.im;

import java.util.Vector;

import net.yunim.eb.signlistener.EntboostIMListener;
import net.yunim.service.Entboost;
import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.entity.AccountInfo;
import net.yunim.service.entity.ClientVer;
import net.yunim.service.listener.CheckClientVerListener;
import net.yunim.service.listener.InitAppKeyListener;
import net.yunim.service.listener.LogonAccountListener;
import net.yunim.utils.VersionUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;

import com.entboost.Log4jLog;
import com.entboost.im.global.AppUtils;
import com.entboost.im.global.MyApplication;
import com.entboost.im.setting.SetLogonServiceAddrActivity;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.activity.MyActivityManager;

public class WelcomeActivity extends Activity {

	/** The tag. */
	private static String TAG = WelcomeActivity.class.getSimpleName();
	private static String LONG_TAG = WelcomeActivity.class.getName();
	
	private Vector<AlertDialog> dialogs = new Vector<AlertDialog>();
	private EntboostIMListener listener;

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
    	// Activity退出时出栈
        Log4jLog.d(LONG_TAG, this.getClass().getName()+", activity pop");
        MyActivityManager.getInstance().popOneActivity(this);
        
		// 移除系统监听
		Entboost.removeListener(listener);
		if (dialogs != null) {
			for (AlertDialog dialog : dialogs) {
				dialog.dismiss();
			}
		}
		super.onDestroy();
	}

	private void showSelectService() {
		try {
			AlertDialog.Builder ab = new AlertDialog.Builder(
					WelcomeActivity.this);
			ab.setTitle("提示");
			// ab.setMessage(LogonCenter.getInstance().getSharedLogonCenterAddr());
			ab.setMessage("注册appKey失败，请重新设置服务器地址！");
			ab.setIcon(android.R.drawable.ic_dialog_alert);
			ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
					Intent intent = new Intent(WelcomeActivity.this,
							SetLogonServiceAddrActivity.class);
					startActivity(intent);
				}
			});
			ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					dialog.dismiss();
					finish();
				}
			});
			ab.create();
			AlertDialog dialog = ab.show();
			dialogs.add(dialog);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//加入管理栈
		Log4jLog.d(LONG_TAG, this.getClass().getName()+", activity push");
		MyActivityManager.getInstance().pushOneActivity(this);
		
		setContentView(R.layout.activity_welcome);
		// 首次开启程序，添加桌面快捷图标
		SharedPreferences preferences = getSharedPreferences("first",
				Context.MODE_PRIVATE);
		boolean isFirst = preferences.getBoolean("isfrist", true);
		if (isFirst) {
			createDeskShortCut();
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isfrist", false);
		editor.commit();
		listener = new EntboostIMListener() {

			@Override
			public void disConnect(String err) {
				if (dialogs.size() > 0) {
					return;
				}
				AlertDialog dialog = new AlertDialog.Builder(
						WelcomeActivity.this)
						.setTitle("提示")
						.setMessage(err)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).show();
				dialogs.add(dialog);
			}

		};
		Entboost.addListener(listener);
		// 注册开发者appkey，appid的数据类型为Long,数字后面要加上字母l，例如874562130982+l
		EntboostLC.initAPPKey(MyApplication.appid, MyApplication.appkey, new InitAppKeyListener() {
			@Override
			public void onFailure(String err) {
				showSelectService();
			}

			@Override
			public void onInitAppKeySuccess() {
				// 检查应用程序版本
				String clientVer = AppUtils.getVersion(WelcomeActivity.this);
				VersionUtils.checkApkVer(clientVer, WelcomeActivity.this, new CheckClientVerListener() {
					@Override
					public void onFailure(String errMsg) {
						// 判断是否默认登录，如果是默认登录就直接登录到主界面，如果不是就打开登录界面
						boolean defaulLogon = EntboostCache.getDefaultLogon();
						if (!defaulLogon) {
							finish();
							Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						} else {
							// 登录当前的默认用户到Entboost系统，默认用户是最后一次登录的用户
							EntboostLC.logon(new LogonAccountListener() {
								@Override
								public void onFailure(String errMsg) {
									finish();
									Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
								}

								@Override
								public void onLogonSuccess(AccountInfo pAccountInfo) {
									finish();
									Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
								}
							});
						}
					}

					@Override
					public void onCheckVerSuccess(
							ClientVer cVer) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		});
	}

	/**
	 * 创建快捷方式
	 */
	public void createDeskShortCut() {
		// 创建快捷方式的Intent
		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重复创建
		shortcutIntent.putExtra("duplicate", false);
		// 需要现实的名称
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));

		// 快捷图片
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				getApplicationContext(), R.drawable.ic_launcher);

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
		// 下面两个属性是为了当应用程序卸载时桌面 上的快捷方式会删除
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		// 点击快捷图片，运行的程序主入口
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// 发送广播。OK
		sendBroadcast(shortcutIntent);
	}
}
