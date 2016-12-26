package com.entboost.im.global;

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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.MainActivity;
import com.entboost.im.setting.SetLogonServiceAddrActivity;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.activity.MyActivityManager;

public class IMStepExecutor {
	private static String LONG_TAG = IMStepExecutor.class.getName();
	
	private static IMStepExecutor instance = null;
	
	private Vector<AlertDialog> dialogs = new Vector<AlertDialog>();
	private EntboostIMListener listener;
	
	private IMStepExecutor() {
		
	}
	
	/**
	 * 获取单实例
	 * @return
	 */
	public synchronized static IMStepExecutor getInstance() {
		if (instance==null) {
			instance = new IMStepExecutor();
		}
		return instance;
	}
	
	/**
	 * 执行登录步骤
	 * @param activity
	 */
	public void executeTryLogon(final Activity activity) {
		//注册事件监听器
		if (listener==null) {
			listener = new EntboostIMListener() {
				@Override
				public void disConnect(String err) {
					if (dialogs.size() > 0) {
						return;
					}
					
					try {
						AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("提示").setMessage(err).setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								activity.finish();
								IMStepExecutor.getInstance().exitApplication();
							}
						}).show();
						dialogs.add(dialog);
					} catch (NullPointerException e) {
						Log4jLog.e(LONG_TAG, "AlertDialog error", e);
						activity.finish();
						IMStepExecutor.getInstance().exitApplication();
					}
				}
			};
			
			Entboost.addListener(listener);
		}
		
		// 注册开发者appkey，appid的数据类型为Long,数字后面要加上字母l，例如874562130982+l
		EntboostLC.initAPPKey(MyApplication.appid, MyApplication.appkey, new InitAppKeyListener() {
			@Override
			public void onFailure(int code, String err) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						showSelectService(activity);
					}
				});
			}
			
			@Override
			public void onInitAppKeySuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						// 检查应用程序版本
						String clientVer = AppUtils.getVersion(activity/*WelcomeActivity.this*/);
						VersionUtils.checkApkVer(clientVer, activity/*WelcomeActivity.this*/, new CheckClientVerListener() {
							@Override
							public void onFailure(int code, String errMsg) {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										// 判断是否默认登录，如果是默认登录就直接登录到主界面，如果不是就打开登录界面
										boolean defaulLogon = EntboostCache.getDefaultLogon();
										if (!defaulLogon) {
											//finish();
											Intent intent = new Intent(activity/*WelcomeActivity.this*/, LoginActivity.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
											activity.startActivity(intent);
										} else {
											// 登录当前的默认用户到Entboost系统，默认用户是最后一次登录的用户
											EntboostLC.logon(new LogonAccountListener() {
												@Override
												public void onFailure(int code, String errMsg) {
													HandlerToolKit.runOnMainThreadAsync(new Runnable() {
														@Override
														public void run() {
															//finish();
															Intent intent = new Intent(activity/*WelcomeActivity.this*/, LoginActivity.class);
															intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
															activity.startActivity(intent);
														}
													});
												}

												@Override
												public void onLogonSuccess(AccountInfo pAccountInfo) {
													HandlerToolKit.runOnMainThreadAsync(new Runnable() {
														@Override
														public void run() {
															//finish();
															Intent intent = new Intent(activity/*WelcomeActivity.this*/, MainActivity.class);
															intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
															activity.startActivity(intent);
														}
													});
												}
											});
										}
									}
								});
							}

							@Override
							public void onCheckVerSuccess(ClientVer cVer) {
								// TODO Auto-generated method stub
								
							}
						});
					}
				});
			}
		});
	}
	
	//执行退出欢迎页
	public void exitWelcome() {
		// 移除系统监听
		Entboost.removeListener(listener);
		listener = null;
		//关闭对话框
		if (dialogs != null) {
			for (AlertDialog dialog : dialogs) {
				dialog.dismiss();
			}
			dialogs.clear();
		}
	}
	
	//显示输入服务端访问地址的界面
	private void showSelectService(final Activity activity) {
		try {
			AlertDialog.Builder ab = new AlertDialog.Builder(activity/*WelcomeActivity.this*/);
			ab.setTitle("提示");
			// ab.setMessage(LogonCenter.getInstance().getSharedLogonCenterAddr());
			ab.setMessage("注册appKey失败，请重新设置服务器地址！");
			ab.setIcon(android.R.drawable.ic_dialog_alert);
			ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					activity.finish();
					
					Intent intent = new Intent(activity/*WelcomeActivity.this*/, SetLogonServiceAddrActivity.class);
					activity.startActivity(intent);
				}
			});
			
			ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					dialog.dismiss();
					activity.finish();
					IMStepExecutor.getInstance().exitApplication();
				}
			});
			
			ab.create();
			AlertDialog dialog = ab.show();
			dialogs.add(dialog);
		} catch (Exception e) {
		}
	}
	
	//退出应用程序
	public void exitApplication() {
		//退出所有Activity
		MyActivityManager.getInstance().clearAllActivity();
		
		//程序退出
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);		
	}
}
