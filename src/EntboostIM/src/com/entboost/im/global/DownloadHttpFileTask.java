package com.entboost.im.global;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.entboost.Log4jLog;
import com.entboost.utils.AbFileUtil;

/**
 * 自定义异步下载Http文件类，继承AsyncTask类
 */
public class DownloadHttpFileTask extends AsyncTask<String, Integer, Boolean> {
	private static String LONG_TAG = DownloadHttpFileTask.class.getName();
	
	// 进度条对话框
	private ProgressDialog pd;
	// 保存路径
	private String savedFilePath;
	// 事件监听器
	private DownloadHttpFileTaskListener listener;
	
	public static final int SIZE_UNKNOWN = 1;
	public static final int SIZE_KNOWN = 0;

	public void setListener(DownloadHttpFileTaskListener listener) {
		this.listener = listener;
	}

	public DownloadHttpFileTask(ProgressDialog pd, String savedFilePath) {
		super();
		
		this.pd = pd;
		this.savedFilePath = savedFilePath;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		
		String urlPath = params[0];
		HttpURLConnection conn = null;
		int code = -1;
		
		try {
			URL url = new URL(urlPath);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Accept-Encoding", ""); //默认方式，不支持gzip
			conn.setConnectTimeout(5000);
			
			code = conn.getResponseCode();
		} catch (Exception e) {
			Log4jLog.e(LONG_TAG, "connect error url:" + urlPath, e);
			return false;
		}
		
		if (code!=200) {
			Log4jLog.e(LONG_TAG, "response error code("+ code +"), url:" + urlPath);
			return false;
		}
		
		// 获取到文件大小
		int length = conn.getContentLength();
		Log4jLog.i(LONG_TAG, "文件大小(Byte)：" + length);
		
		// 是否未知文件大小：1=未知，0=已知
		int unknow = length<0?SIZE_UNKNOWN:SIZE_KNOWN;
		
		//更新进度条
		publishProgress(unknow, 0, length);
		
		InputStream is = null;
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		
		try {
			File file = new File(savedFilePath);
			
			is = conn.getInputStream();
			fos = new FileOutputStream(file);
			bis = new BufferedInputStream(is);
			
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			while (!isCancelled() && (len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				total += len;
				
				//更新进度条
				publishProgress(unknow, total);
			}
			
			if (isCancelled()) {
				Log4jLog.i(LONG_TAG, "cancel downloadTask");
				return false;
			} else
				return true;
		} catch(Exception e) {
			Log4jLog.e(LONG_TAG, "downloadTask error", e);
		} finally {
			AbFileUtil.closeOutputStream(fos);
			AbFileUtil.closeInputStream(bis);
			AbFileUtil.closeInputStream(is);
		}
		
		return false;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(final Boolean result) {
		if (result) {
			if (listener!=null)
				listener.onFinished();
		} else {
			if (listener!=null)
				listener.onFailure();
		}
		super.onPostExecute(result);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		//类型
		int unknow = values[0];
		//总长度
		if (values.length>2)
			pd.setMax((unknow==SIZE_UNKNOWN)?2:values[2].intValue());
		//当前长度
		pd.setProgress((unknow==SIZE_UNKNOWN)?1:values[1].intValue());
		
		//全部容量(MB)
		double all = Double.valueOf(pd.getMax()) / 1024 / 1024;
		//已下载容量(MB)
		double downloaded = Double.valueOf(values[1].intValue()) / 1024 / 1024;
		pd.setProgressNumberFormat((unknow==SIZE_UNKNOWN)?String.format("%.2fM/未知", downloaded):String.format("%.2fM/%.2fM", downloaded, all));
		
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onCancelled(Boolean result) {
		if (listener!=null)
			listener.onCancelled();
		super.onCancelled(result);
	}
}