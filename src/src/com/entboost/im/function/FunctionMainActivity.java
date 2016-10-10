package com.entboost.im.function;

import java.util.Vector;

import net.yunim.service.EntboostUM;
import net.yunim.service.entity.CardInfo;
import net.yunim.service.entity.FnavInfo;
import net.yunim.service.entity.FuncInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.listener.FnavArgsListener;
import net.yunim.service.listener.LoadFnavListener;
import net.yunim.utils.UIUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.lidroid.xutils.util.LogUtils;

@SuppressLint("SetJavaScriptEnabled")
public class FunctionMainActivity extends EbActivity {

	private WebView mWebView;
	private FuncInfo funcInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_function_main);
		funcInfo = (FuncInfo) getIntent().getSerializableExtra("funcInfo");
		String tab_type = getIntent().getStringExtra("tab_type");
		AbTitleBar titleBar = this.getTitleBar();
		titleBar.setTitleText(funcInfo.getFunc_name());
		mWebView = (WebView) findViewById(R.id.funcWebView);
		WebSettings s = mWebView.getSettings();
		s.setJavaScriptEnabled(true);
		s.setDomStorageEnabled(true);
		mWebView.setBackgroundResource(R.color.mainPageBG);
		mWebView.requestFocus();
		mWebView.setScrollBarStyle(0);
		//设置WebView可触摸放大缩小
		mWebView.getSettings().setBuiltInZoomControls(true);  
		String url = funcInfo.getFunc_url(tab_type);
		mWebView.loadUrl(url);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				showProgressDialog("正在努力加载，请等待");
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
				UIUtils.showToast(FunctionMainActivity.this,
						"无法打开扩展应用，请检查网络是否通畅！");
				finish();
			}

			public boolean shouldOverrideUrlLoading(final WebView view,
					final String url) {
				EntboostUM.fnavArgs(url, new FnavArgsListener() {

					@Override
					public void closeWindow() {
						FunctionMainActivity.this.finish();
					}

					@Override
					public void send_group_success(Long depCode, GroupInfo group) {
						removeProgressDialog();
						Intent intent = new Intent(FunctionMainActivity.this,
								ChatActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_SINGLE_TOP
								| Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(ChatActivity.INTENT_TITLE,
								group.getDep_name());
						intent.putExtra(ChatActivity.INTENT_UID, depCode);
						intent.putExtra(ChatActivity.INTENT_CHATTYPE,
								ChatActivity.CHATTYPE_GROUP);
						FunctionMainActivity.this.startActivity(intent);
					}

					@Override
					public void onStart() {
						showProgressDialog("正在执行操作，请等待");
					}

					@Override
					public void send_account_success(Long uid, CardInfo cardInfo) {
						removeProgressDialog();
						Intent intent = new Intent(FunctionMainActivity.this,
								ChatActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_SINGLE_TOP
								| Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(ChatActivity.INTENT_TITLE,
								cardInfo.getNa());
						intent.putExtra(ChatActivity.INTENT_UID, uid);
						FunctionMainActivity.this.startActivity(intent);
					}

					@Override
					public void defaultLink() {
						view.loadUrl(url);
					}

					@Override
					public void onFailure(String errMsg) {
						removeProgressDialog();
						UIUtils.showToast(FunctionMainActivity.this, errMsg);
					}

				});

				return true;
			}
		});
		EntboostUM.loadFnav(Long.valueOf(funcInfo.getSub_id()),
				new LoadFnavListener() {

					@Override
					public void onFailure(String errMsg) {
						UIUtils.showToast(FunctionMainActivity.this, errMsg);
					}

					@Override
					public void onLoadFuncSuccess(
							final Vector<FnavInfo> fnavInfos) {
						// 获取一级菜单
						Vector<FnavInfo> fnavInfos1 = new Vector<FnavInfo>();
						for (FnavInfo fi : fnavInfos) {
							if (fi.getParent_navid() == null
									|| fi.getParent_navid() == 0) {
								fnavInfos1.add(fi);
							}
						}
						LinearLayout layout = (LinearLayout) findViewById(R.id.menu_Layout);
						int size = layout.getChildCount();
						if (size > fnavInfos1.size()) {
							size = fnavInfos1.size();
						}
						if (fnavInfos.size() != 0) {
							layout.setVisibility(View.VISIBLE);
						}
						for (int i = 0; i < size; i++) {
							View child = layout.getChildAt(i);
							child.setVisibility(View.INVISIBLE);
							final FnavInfo fnavInfo = fnavInfos1.get(i);
							final Vector<FnavInfo> fnavInfos2 = new Vector<FnavInfo>();
							for (FnavInfo fi : fnavInfos) {
								if (fi.getParent_navid() - fnavInfo.getNav_id() == 0) {
									fnavInfos2.add(fi);
								}
							}
							initMenu((ViewGroup) child, fnavInfo, fnavInfos2);
							child.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (fnavInfos2.size() == 0) {
										mWebView.loadUrl(fnavInfo.getUrl());
									} else {
										View popView = mInflater.inflate(
												R.layout.list_pop, null);
										ListView popListView = (ListView) popView
												.findViewById(R.id.pop_list);
										final ListPopAdapter mListPopAdapter = new ListPopAdapter(
												FunctionMainActivity.this,
												fnavInfos2,
												R.layout.item2_list_pop);
										popListView.setAdapter(mListPopAdapter);
										UIUtils.showWindow(v, popListView,
												new OnItemClickListener() {

													@Override
													public void onItemClick(
															AdapterView<?> arg0,
															View arg1,
															int position,
															long arg3) {
														FnavInfo fnavInfo = (FnavInfo) mListPopAdapter
																.getItem(position);
														mWebView.loadUrl(fnavInfo
																.getUrl());
													}
												});
									}
								}
							});
						}
					}
				});
	}

	private void initMenu(ViewGroup layout, FnavInfo fnavInfo,
			Vector<FnavInfo> fnavInfos2) {
		for (int i = 0, j = layout.getChildCount(); i < j; i++) {
			View child = layout.getChildAt(i);
			if (child instanceof TextView) {
				((TextView) child).setText(fnavInfo.getName());
			} else if (child instanceof ImageView) {
				if (fnavInfos2.size() != 0) {
					child.setVisibility(View.VISIBLE);
				} else {
					child.setVisibility(View.GONE);
				}
			}
		}
	}

}
