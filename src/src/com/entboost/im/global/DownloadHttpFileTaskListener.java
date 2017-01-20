package com.entboost.im.global;

public interface DownloadHttpFileTaskListener {
	
	/**
	 * 下载完成
	 */
	public void onFinished();
	/**
	 * 下载失败
	 */
	public void onFailure();
	/**
	 * 取消下载
	 */
	public void onCancelled();
}
