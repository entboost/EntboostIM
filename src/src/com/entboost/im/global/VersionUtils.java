package com.entboost.im.global;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.yunim.eb.constants.EBConstants;
import net.yunim.service.EntboostUM;
import net.yunim.service.cache.EbCache;
import net.yunim.service.constants.EB_STATE_CODE;
import net.yunim.service.entity.ClientVer;
import net.yunim.service.listener.CheckClientVerListener;
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
import android.os.Message;
import android.widget.Toast;

import com.entboost.Log4jLog;

public class VersionUtils {
	
	/** The tag. */
	private static String TAG = VersionUtils.class.getSimpleName();
	private static String LONG_TAG = VersionUtils.class.getName();
	
	protected static final int DOWN_ERROR = 0;

	public static void checkApkVer(final String clientVer, final Context context, final CheckClientVerListener listener) {
		EntboostUM.checkClientVer(clientVer, new CheckClientVerListener() {

			@Override
			public void onFailure(int code, String errMsg) {
				if (listener != null) {
					listener.onFailure(code, errMsg);
				} else {
					UIUtils.showToast(context, errMsg);
				}
			}

			@Override
			public void onCheckVerSuccess(ClientVer cVer) {
				String cver = cVer.getServer_ver();
				try {
					//if (Integer.valueOf(StringUtils.substringAfterLast(cver, ".")) <= Integer.valueOf(StringUtils.substringAfterLast(clientVer, "."))) {
					if (cver.compareTo(clientVer)<=0) {
						if(listener!=null){
							listener.onFailure(EB_STATE_CODE.EB_STATE_ERROR.getValue(), "当前是最新版本:"+clientVer);
						}else{
							UIUtils.showToast(context, "当前是最新版本:"+clientVer);
						}
						return;
					}
				} catch (Exception e) {
					if(listener!=null){
						listener.onFailure(EB_STATE_CODE.EB_STATE_ERROR.getValue(), "当前是最新版本:"+clientVer);
					}else{
						UIUtils.showToast(context, "当前是最新版本:"+clientVer);
					}
					return;
				}
				showUpdataDialog(context, cVer, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (listener != null) {
							listener.onFailure(EB_STATE_CODE.EB_STATE_ERROR.getValue(), "取消更新");
						}
					}
				});
			}
		});
	}

	public static File getFileFromServer(String path, ProgressDialog pd)
			throws Exception {
		// 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Accept-Encoding", ""); //默认方式，不支持gzip
			conn.setConnectTimeout(5000);
			
			// 获取到文件的大小
			Log4jLog.i(LONG_TAG, "新版本文件大小(Byte)：" + conn.getContentLength());
			pd.setMax(conn.getContentLength()<0?2:conn.getContentLength());
			double all = Double.valueOf(pd.getMax() + "") / 1024 / 1024;
			//Log4jLog.i(LONG_TAG, "计算后的大小(MB)：" + all);
			
			InputStream is = conn.getInputStream();
			File file = new File(Environment.getExternalStorageDirectory(), "entboost_update.apk");
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				total += len;
				// 获取当前下载量
				pd.setProgress(conn.getContentLength()<0?1:total);
				double percent = Double.valueOf(total + "") / 1024 / 1024;
				pd.setProgressNumberFormat(conn.getContentLength()<0?String.format("%.2fM/未知", percent):String.format("%.2fM/%.2fM", percent, all));
			}
			fos.close();
			bis.close();
			is.close();
			return file;
		} else {
			return null;
		}
	}

	/*
	 * 
	 * 弹出对话框通知用户更新程序
	 */
	private static void showUpdataDialog(final Context context, final ClientVer cVer, OnClickListener listener) {
		try {
			AlertDialog.Builder builer = new Builder(context);
			builer.setTitle("版本升级");
			builer.setMessage("检测到最新版本(" + cVer.getServer_ver() + ")，请及时更新！");
			// 当点确定按钮时从服务器上下载 新的apk 然后安装
			builer.setPositiveButton("确定", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					downLoadApk(context, cVer.getDownload_url());
				}
			});
			builer.setCancelable(false);
			// 当点取消按钮时进行登录
			builer.setNegativeButton("取消", listener);
			AlertDialog dialog = builer.create();
			dialog.show();
		} catch (NullPointerException e) {
			Log4jLog.e(LONG_TAG, "showUpdataDialog error", e);
		}
	}

	/*
	 * 从服务器中下载APK
	 */
	private static void downLoadApk(final Context context, final String url) {
		final ProgressDialog pd; // 进度条对话框
		pd = new ProgressDialog(context);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		pd.setCancelable(false);
		pd.show();
		new Thread() {
			@Override
			public void run() {
				try {
					File file = getFileFromServer(url, pd);
					sleep(3000);
					installApk(context, file);
					pd.dismiss(); // 结束掉进度条对话框
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = DOWN_ERROR;
					handler.sendMessage(msg);
					e.printStackTrace();
				}
			}
		}.start();
	}

	// 安装apk
	private static void installApk(Context context, File file) {
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DOWN_ERROR:
				// 下载apk失败
				Toast.makeText(EbCache.getInstance().getContext(), "下载新版本失败", 1)
						.show();
				break;
			}
		}
	};
}
