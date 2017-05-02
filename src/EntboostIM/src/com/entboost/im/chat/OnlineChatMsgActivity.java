package com.entboost.im.chat;

import com.entboost.im.global.UIUtils;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;

public class OnlineChatMsgActivity extends EbActivity {
	private WebView mWebView;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_onlie_chat_msg);
		mWebView = (WebView) findViewById(R.id.webView);
		WebSettings s = mWebView.getSettings();
		s.setJavaScriptEnabled(true);
		s.setDomStorageEnabled(true);
		mWebView.setBackgroundResource(R.color.mainPageBG);
		mWebView.requestFocus();
		mWebView.setScrollBarStyle(0);
		String url=getIntent().getStringExtra("onlineChatUrl");
		mWebView.loadUrl(url);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				showProgressDialog("正在努力加载，请稍后");
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				removeProgressDialog();
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				UIUtils.showToast(OnlineChatMsgActivity.this,
						"无法打开扩展应用，请检查网络是否通畅！");
				finish();
			}

		});
	}

}
