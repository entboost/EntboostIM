package com.entboost.im.push;

import java.util.Map;
import java.util.Map.Entry;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.listener.RequestPushPlatformListener;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.global.MyApplication;
import com.entboost.im.push.HuaweiPushApiAccessor.HuaweiPushApiAccessorListener;
import com.entboost.utils.PhoneInfo;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * 
 * 接入第三方推送平台支持类
 *
 */
public class ThirdPartyPushHelper {
	
	private static String LONG_TAG = ThirdPartyPushHelper.class.getName();
	
	/**
	 * 华为推送平台的证书编号
	 */
	public static long SSLID_HUAWEI = 100;
	/**
	 * 小米推送平台的证书编号
	 */
	public static long SSLID_XIAOMI = 200;
	
	//当前客户端使用的SSLId
	private static long currentSSLId = 0;
	//是否使用第三方推送
	private static boolean enablePush = false;
	
	//第三方推送令牌
	public static String pushToken;
	
	/**
	 * 小米推送平台申请的APPID
	 */
	public static String XIAOMI_PushAppId = "22222"; //注意：填入你自己应用所申请的APPID
	/**
	 * 小米推送平台申请的APPKey
	 */
	public static String XIAOMI_PushAppKey = "33333"; //注意：填入你自己应用所申请的APPKey
	
	/**
	 * 准备接入第三方推送平台
	 * @param context 应用上下文
	 */
	public static void preparePushEnvironment(Context context) {
		String manufacturer = PhoneInfo.getManufacturerName(); //识别手机厂商
		if (manufacturer==null) {
			Log4jLog.e(LONG_TAG, "can not read manufacturer");
			return;
		}
		
		//与IM服务端约定的SSLID进行手机尝试配对
		//其它待后续支持：manufacturer.equalsIgnoreCase("samsung")
		if (manufacturer.equalsIgnoreCase("HUAWEI")) {
			currentSSLId = SSLID_HUAWEI;
		}
		if (manufacturer.equalsIgnoreCase("Xiaomi")) {
			currentSSLId = SSLID_XIAOMI;
		}
		
		//for test
//		enablePush = true;
//		currentSSLId = SSLID_XIAOMI;
		
		//获取IM服务端支持的第三方推送列表
		Map<Long, Integer> pushSslIds = EntboostCache.getPushSslIds();
		Log4jLog.i(LONG_TAG, "all pushSslIds:" + pushSslIds);
		//精准匹配
		if (pushSslIds.get(currentSSLId)!=null)
			enablePush = true;
		//否则选择默认推送平台
		if (!enablePush) {
			for (Entry<Long,Integer> e : pushSslIds.entrySet()) {
				if (e.getValue() == 1) {
					enablePush = true;
					currentSSLId = e.getKey();
					break;
				}
			}
		}
		
		if (enablePush) {
			//接入华为Push
			if (currentSSLId == SSLID_HUAWEI) {
				final HuaweiPushApiAccessor hwpAccessor = HuaweiPushApiAccessor.getInstance(context);
				hwpAccessor.setListener(new HuaweiPushApiAccessorListener() {
					@Override
					public void onConnected() {
						hwpAccessor.requestTokenFormHuaweiServer();
						hwpAccessor.setPassByMsg(false);
					}
				});
				
				hwpAccessor.connect();
			}
			//接入小米Push
			else if (currentSSLId == SSLID_XIAOMI) {
				//打开日志
		        LoggerInterface newLogger = new LoggerInterface() {
		            @Override
		            public void setTag(String tag) {
		                // ignore
		            }
		            @Override
		            public void log(String content, Throwable t) {
		                Log4jLog.d(LONG_TAG, content, t);
		            }
		            @Override
		            public void log(String content) {
		            	Log4jLog.d(LONG_TAG, content);
		            }
		        };
		        Logger.setLogger(context, newLogger);
		        //初始化推送服务
				MiPushClient.registerPush(context, XIAOMI_PushAppId, XIAOMI_PushAppKey);
			}
		}
	}
	
	/**
	 * 上传推送平台Token到IM服务端
	 * @param waittingLogin 是否等待登录状态
	 */
	public static void setPushToken(final boolean waittingLogin) {
		if (StringUtils.isBlank(pushToken)) {
			return;
		}
		
		//实现事件监听实例
		final RequestPushPlatformListener listener = new RequestPushPlatformListener() {
			@Override
			public void onFailure(int code, String errMsg) {
				Log4jLog.e(LONG_TAG, errMsg);
			}
			@Override
			public void onRequestSuccess() {
				Log4jLog.i(LONG_TAG, "requestPushPlatform success, currentSSLId = " + currentSSLId);
			}
		};
		
		//在主线程执行
		HandlerToolKit.runOnMainThreadAsync(new Runnable() {
			@Override
			public void run() {
				final MyApplication app = MyApplication.getInstance();
				
				if (app.isLogin()) {
					//上传Token到恩布IM服务端
					EntboostUM.requestPushPlatform(currentSSLId, pushToken, listener);
				} else if (waittingLogin) {
					//在子线程等待登录成功后(超过60秒忽略)，上传Token到恩布IM服务端
					new Thread(new Runnable() {
						@Override
						public void run() {
							int times = 0;
							while(times<60) {
								if (app.isLogin()) {
									EntboostUM.requestPushPlatform(currentSSLId, pushToken, listener);
									break;
								}
								
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									Log4jLog.e(LONG_TAG, e);
								}
								
								times++;
							}
							
							Log4jLog.i(LONG_TAG, "waitting for requestPushPlatform end, times:" + times);
						}
					}).start();
				}
			}
		});
	}
}
