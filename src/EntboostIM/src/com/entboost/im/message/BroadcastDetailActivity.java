package com.entboost.im.message;

import net.yunim.service.entity.BroadcastMessage;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.utils.AbDateUtil;

public class BroadcastDetailActivity extends EbActivity {

	private BroadcastMessage msg;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_broadcast_detail);
		msg = (BroadcastMessage) getIntent().getSerializableExtra(
				"broadcastMessage");
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.setBackgroundColor(0x00000000); // 透明背景
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		if (msg != null) {
			((TextView) findViewById(R.id.msg_name)).setText(msg.getMsg_name());
			((TextView) findViewById(R.id.msg_time)).setText(AbDateUtil
					.formatDateStr2Desc(AbDateUtil.getStringByFormat(
							msg.getSendTime(), AbDateUtil.dateFormatYMDHMS),
							AbDateUtil.dateFormatYMDHMS));
			webView.loadDataWithBaseURL(null, msg.getMsg_content(), "text/html",
					"utf-8", null);
		}

	}

}
