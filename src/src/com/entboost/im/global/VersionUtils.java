package com.entboost.im.global;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_STATE_CODE;
import net.yunim.service.entity.ClientVer;
import net.yunim.service.listener.CheckClientVerListener;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;

public class VersionUtils {
	
	private static String TAG = VersionUtils.class.getSimpleName();
	private static String LONG_TAG = VersionUtils.class.getName();

	/**
	 * 检测应用版本，如必要就执行更新程序
	 * @param localClientVer 本地应用程序版本号
	 * @param context 上下文
	 * @param listener 事件监视器
	 */
	public static void checkApkVer(final String localClientVer, final Context context, final CheckClientVerListener listener) {
		EntboostUM.checkClientVer(localClientVer, new CheckClientVerListener() {

			@Override
			public void onFailure(int code, String errMsg) {
				if (listener != null) {
					listener.onFailure(code, errMsg);
				} else {
					UIUtils.showToast(context, errMsg);
				}
			}
			
			@Override
			public void onCheckVerSuccess(final ClientVer cVer) {
				String newVer = cVer.getServer_ver();
				try {
					Log4jLog.d(LONG_TAG, "localClientVer:" + localClientVer + ", newVer:" + newVer);
					
					//非数字类型的版本号
					if ( (!isNumeric(newVer) || !isNumeric(localClientVer)) ) {
						//采用字符串比较的方式
						if (newVer.compareTo(localClientVer)<=0) {
							handleNoUpdate(localClientVer, context, listener, false);
							return;
						}
					} else { //纯数字类型的版本号
						//分拆后，再逐段转为数字后比较
						String[] newVers = newVer.split("[.]");
						String[] localVers = localClientVer.split("[.]");
						
						// 比较版本号
						boolean needUpdated = false;
						int i=0;
						for (; (i<localVers.length && i<newVers.length); i++) {
							int localV = Integer.valueOf(localVers[i]);
							int newV = Integer.valueOf(newVers[i]);
							//本地版本更高，退出更新
							if (localV>newV) {
								handleNoUpdate(localClientVer, context, listener, false);
								return;
							} else if (localV<newV) { //本地版本更低，需要更新
								needUpdated = true;
								break;
							}
						}
						
						//两个版本号分段数不相等的情况
						if (!needUpdated && localVers.length>=newVers.length) {
							handleNoUpdate(localClientVer, context, listener, false);
							return;
						}
					}
				} catch (Exception e) {
					handleNoUpdate(localClientVer, context, listener, true);
					return;
				}
				
				//主线程执行
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						//显示更新程序对话框
						showUpdataDialog(context, cVer, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (listener != null) {
									listener.onFailure(EB_STATE_CODE.EB_STATE_ERROR.getValue(), "取消更新");
								}
							}
						}, listener);
					}
				});
			}
		});
	}
	
	//处理不更新的情况
	private static void handleNoUpdate(final String localClientVer, final Context context, final CheckClientVerListener listener , boolean isError) {
		if(listener!=null){
			listener.onFailure(EB_STATE_CODE.EB_STATE_ERROR.getValue(), (isError?"校验版本号无效:":"当前是最新版本:") + localClientVer);
		}else{
			UIUtils.showToast(context, (isError?"校验版本号无效:":"当前是最新版本:") + localClientVer);
		}
	}
	
	// 判断字符串是否数字(包括"."点号)
	private static boolean isNumeric(String str) {
	   Pattern pattern = Pattern.compile("[0-9.]*"); 
	   Matcher isNum = pattern.matcher(str);
	   if(isNum.matches())
	      return true; 
	   return false;
	}
	
	//弹出对话框通知用户更新程序
	private static void showUpdataDialog(final Context context, final ClientVer cVer, OnClickListener listener, final CheckClientVerListener chkVerListener) {
		try {
			AlertDialog.Builder builer = new Builder(context);
			builer.setTitle("版本升级");
			builer.setMessage("检测到最新版本(" + cVer.getServer_ver() + ")，请及时更新！");
			// 当点确定按钮时从服务器上下载 新的apk 然后安装
			builer.setPositiveButton("确定", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					downLoadApk(context, cVer.getDownload_url(), chkVerListener);
				}
			});
			builer.setCancelable(false);
			// 当点取消按钮时进行登录
			builer.setNegativeButton("取消", listener);
			AlertDialog dialog = builer.create();
			dialog.show();
		} catch (Exception e) {
			Log4jLog.e(LONG_TAG, "showUpdataDialog error", e);
		}
	}

	//从服务器中下载APK
	private static void downLoadApk(final Context context, final String url, final CheckClientVerListener chkVerListener) {
		if (StringUtils.isBlank(url)) {
			Log4jLog.e(LONG_TAG, "url is invalid");
			//显示错误提示
			Toast.makeText(context, "未能识别下载地址", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//sdcard是否可用
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log4jLog.e(LONG_TAG, "sdcard is invalid");
			//显示错误提示
			Toast.makeText(context, "手机存储空间不可用", Toast.LENGTH_SHORT).show();
			return;
		}
		
		final ProgressDialog pd; // 进度条对话框
		pd = new ProgressDialog(context);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		pd.setCancelable(false);
		pd.show();
		
		final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/entboost_update.apk";
		
		//异步执行下载任务
		DownloadHttpFileTask dTask =new DownloadHttpFileTask(pd, filePath);
		dTask.setListener(new DownloadHttpFileTaskListener() {
			
			@Override
			public void onFinished() {
				//延迟3秒后执行
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						//关闭进度条对话框
						pd.dismiss();
						
						//安装apk
						installApk(context, filePath);
						//退出应用程序
						IMStepExecutor.getInstance().exitApplication();
					}
				}, 2000);
			}
			
			@Override
			public void onFailure() {
				//关闭进度条对话框
				pd.dismiss();
				//显示错误提示
				Toast.makeText(context, "下载升级文件失败", Toast.LENGTH_LONG).show();
				
				if (chkVerListener!=null)
					chkVerListener.onFailure(EB_STATE_CODE.EB_STATE_ERROR.getValue(), "下载升级文件失败");
			}
			
			@Override
			public void onCancelled() {
				
			}
		});
		dTask.execute(url);
	}
	
	/**
	 * 安装apk
	 * @param context
	 * @param filePath
	 */
	public static void installApk(Context context, String filePath) {
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

}
