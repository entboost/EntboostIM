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
import net.yunim.utils.YINetworkUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.MainActivity;
import com.entboost.im.WelcomeActivity;
import com.entboost.im.push.ThirdPartyPushHelper;
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
	
	private MyApplication getApplication() {
		return MyApplication.getInstance();
	}
	
	private Activity getActivity() {
		return MyActivityManager.getInstance().getTopActivity();
	}
	
	//判断是否在界面模式
	private boolean isInInterface() {
		return MyApplication.getInstance().isInInterface();
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
	
	//登录环节
	private void tryLogon() {
		final Context context = getApplication();
		
		// 判断是否默认登录，如果是默认登录就直接登录到主界面，如果不是就打开登录界面
		boolean defaulLogon = EntboostCache.getDefaultLogon();
		Log4jLog.d(LONG_TAG, "defaulLogon: "+defaulLogon);
		if (!defaulLogon) {
			if (isInInterface()) {
				Activity welcomeActivity = MyActivityManager.getInstance().getActivity(WelcomeActivity.class.getName());
				if (welcomeActivity!=null) {
					welcomeActivity.finish();
				}
				
				Intent intent = new Intent(context, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			}
		} else {
			// 登录当前的默认用户到Entboost系统，默认用户是最后一次登录的用户
			EntboostLC.logon(context, new LogonAccountListener() {
				@Override
				public void onFailure(int code, String errMsg) {
					if (isInInterface()) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								Activity welcomeActivity = MyActivityManager.getInstance().getActivity(WelcomeActivity.class.getName());
								if (welcomeActivity!=null) {
									welcomeActivity.finish();
								}
								
								Intent intent = new Intent(context, LoginActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
								context.startActivity(intent);
							}
						});
					}
				}
				
				@Override
				public void onLogonSuccess(AccountInfo pAccountInfo) {
					HandlerToolKit.runOnMainThreadAsync(new Runnable() {
						@Override
						public void run() {
							//标记登录状态
							getApplication().setLogin(true);
							
							//上传第三方平台推送令牌到IM服务端
							ThirdPartyPushHelper.setPushToken(false);
							
							//切换至主界面
							if (isInInterface()) {
								Activity welcomeActivity = MyActivityManager.getInstance().getActivity(WelcomeActivity.class.getName());
								if (welcomeActivity!=null) {
									welcomeActivity.finish();
								}
								
								Intent intent = new Intent(context, MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
								context.startActivity(intent);
							}
						}
					});
				}
			});
		}
	}
	
	/**
	 * 执行登录步骤
	 */
	public void executeTryLogon() {
		Log4jLog.i(LONG_TAG, "executeTryLogon===============================================");
		
		//注册事件监听器
		if (listener==null) {
			listener = new EntboostIMListener() {
				@Override
				public void disConnect(final String err) {
					//主线程中执行
					HandlerToolKit.runOnMainThreadAsync(new Runnable() {
						public void run() {
							if (dialogs.size() > 0) {
								return;
							}
							
							if (isInInterface()) {
								try {
									AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("提示").setMessage(err).setPositiveButton("确定", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
//											if (context instanceof Activity)
//												((Activity)context).finish();
											
											IMStepExecutor.getInstance().exitApplication();
										}
									}).show();
									dialogs.add(dialog);
								} catch (NullPointerException e) {
									Log4jLog.e(LONG_TAG, "AlertDialog error", e);
//									if (context instanceof Activity)
//										((Activity)context).finish();
									
									IMStepExecutor.getInstance().exitApplication();
								}
							} else {
								Log4jLog.e(LONG_TAG, "executeTryLogon miss disConnect event");
								//退出应用程序
								System.exit(0);
							}
						}
					});
				}
			};
			
			Entboost.addListener(listener);
		}
		
		//子线程执行，防止阻塞主线程
		new Thread(new Runnable() {
			@Override
			public void run() {
				//检测网络是否畅通
				boolean isConnected = false;
				int times = 0;
				while(times < 20) {
					isConnected = YINetworkUtils.isNetworkConnected(getApplication());
					if (isConnected)
						break;
					
					times ++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Log4jLog.e(LONG_TAG, "", e);
					}
				}
				Log4jLog.d(LONG_TAG, "isNetworkConnected = " + isConnected + ", times = " + times);
				
				//设置LC服务端地址
				//EntboostCache.setLogonCenterAddr("entboost.entboost.com:18012");
				
				//初始化应用
				EntboostLC.initAPPKey(MyApplication.appid, MyApplication.appkey, new InitAppKeyListener() {
					@Override
					public void onFailure(int code, String err) {
						if (isInInterface()) {
							HandlerToolKit.runOnMainThreadAsync(new Runnable() {
								@Override
								public void run() {
									showSelectService(getActivity());
								}
							});
						}
					}
					
					@Override
					public void onInitAppKeySuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								//清除下载目录的临时文件
								EntboostCache.clearTempFiles();
								
								//准备接入手机厂商推送平台
								ThirdPartyPushHelper.preparePushEnvironment(getApplication());
								
								//进入登录步骤
								if (isInInterface()) {
									// 检查应用程序版本
									String clientVer = AppUtils.getVersion(getActivity());
									VersionUtils.checkApkVer(clientVer, getActivity(), new CheckClientVerListener() {
										@Override
										public void onFailure(int code, String errMsg) {
											Log4jLog.i(LONG_TAG, errMsg + ", code(" + code + ")");
											
											HandlerToolKit.runOnMainThreadAsync(new Runnable() {
												@Override
												public void run() {
													tryLogon();
												}
											});
										}
										
										@Override
										public void onCheckVerSuccess(ClientVer cVer) {
											
										}
									});
								} else {
									tryLogon();
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	/**
	 * 执行退出欢迎页
	 */
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
	
	/**
	 * 显示输入服务端访问地址的界面
	 * @param context
	 */
	private void showSelectService(final Context context) {
		try {
			AlertDialog.Builder ab = new AlertDialog.Builder(context);
			ab.setTitle("提示");
			// ab.setMessage(LogonCenter.getInstance().getSharedLogonCenterAddr());
			ab.setMessage("注册appKey失败，请重新设置服务器地址！");
			ab.setIcon(android.R.drawable.ic_dialog_alert);
			ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					if (context instanceof Activity)
						((Activity)context).finish();
					
					Intent intent = new Intent(context, SetLogonServiceAddrActivity.class);
					context.startActivity(intent);
				}
			});
			
			ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					dialog.dismiss();
					if (context instanceof Activity)
						((Activity)context).finish();
					
					IMStepExecutor.getInstance().exitApplication();
				}
			});
			
			ab.create();
			AlertDialog dialog = ab.show();
			dialogs.add(dialog);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 退出应用程序
	 */
	public void exitApplication() {
		//退出所有Activity
		MyActivityManager.getInstance().clearAllActivity();
		
		//程序退出
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);		
	}
}
