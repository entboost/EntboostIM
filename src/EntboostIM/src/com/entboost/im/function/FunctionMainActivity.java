package com.entboost.im.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.CardInfo;
import net.yunim.service.entity.FnavInfo;
import net.yunim.service.entity.FuncInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.listener.FnavArgsListener;
import net.yunim.service.listener.LoadFnavListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.global.UIUtils;
import com.entboost.ui.base.activity.MyActivityManager;
import com.entboost.ui.base.view.titlebar.AbTitleBar;

@SuppressLint("SetJavaScriptEnabled")
public class FunctionMainActivity extends EbActivity {

	private static String LONG_TAG = FunctionMainActivity.class.getName();
	
	private WebView mWebView;
	private FuncInfo funcInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_function_main);
		
		funcInfo = (FuncInfo) getIntent().getSerializableExtra("funcInfo");
		String tab_type = getIntent().getStringExtra("tab_type");
		HashMap<String, Object> eParams = (HashMap<String, Object>)getIntent().getSerializableExtra("eParams");
		
		AbTitleBar titleBar = this.getTitleBar();
		titleBar.setTitleText(funcInfo.getFunc_name());
		
		mWebView = (WebView) findViewById(R.id.funcWebView);
		WebSettings s = mWebView.getSettings();
		//支持JS
		s.setJavaScriptEnabled(true);
		//设置WebView可触摸放大缩小
		s.setSupportZoom(true);
		s.setBuiltInZoomControls(true);
		s.setDisplayZoomControls(false);
		//支持缓存
		s.setDomStorageEnabled(true);
		//允许js弹出窗口
		s.setJavaScriptCanOpenWindowsAutomatically(true);
		
		//加载页面自适应手机屏幕 
	    s.setUseWideViewPort(true);
	    s.setLoadWithOverviewMode(true);
		
	    //辅助WebView处理js的对话框，网站图标，网站title，加载进度等 
	    mWebView.setWebChromeClient(new WebChromeClient());
	    
		mWebView.setBackgroundResource(R.color.mainPageBG);
		mWebView.requestFocus();
		mWebView.setScrollBarStyle(0);
		
		//加载页面
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
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				removeProgressDialog();
				UIUtils.showToast(FunctionMainActivity.this, "无法打开扩展应用，请检查网络是否通畅！");
				finish();
			}

			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				EntboostUM.fnavArgs(url, new FnavArgsListener() {
					@Override
					public void closeWindow() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								FunctionMainActivity.this.finish();
							}
						});
					}

					@Override
					public void onStart() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								showProgressDialog("正在执行操作，请稍后");
							}
						});
					}					
					
					@Override
					public void call_group(final Long depCode, final GroupInfo group) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								//退出已经弹出的聊天窗口
								Activity existActivity =MyActivityManager.getInstance().popToActivity(ChatActivity.class.getName());
								if (existActivity!=null)
									MyActivityManager.getInstance().popOneActivity(existActivity);
								
								//关闭进度条								
								removeProgressDialog();
								
								//进入新的聊天窗口
								Intent intent = new Intent(FunctionMainActivity.this, ChatActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_SINGLE_TOP
										| Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.putExtra(ChatActivity.INTENT_TITLE, group.getDep_name());
								intent.putExtra(ChatActivity.INTENT_TOID, depCode);
								intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
								FunctionMainActivity.this.startActivity(intent);
							}
						});
					}

					@Override
					public void call_account(final Long uid, final CardInfo cardInfo) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								//退出已经弹出的聊天窗口
								Activity existActivity =MyActivityManager.getInstance().popToActivity(ChatActivity.class.getName());
								if (existActivity!=null)
									MyActivityManager.getInstance().popOneActivity(existActivity);
								
								//关闭进度条
								removeProgressDialog();
								
								//进入新的聊天窗口
								Intent intent = new Intent(FunctionMainActivity.this, ChatActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_SINGLE_TOP
										| Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.putExtra(ChatActivity.INTENT_TITLE, cardInfo.getNa());
								intent.putExtra(ChatActivity.INTENT_TOID, uid);
								FunctionMainActivity.this.startActivity(intent);
							}
						});
					}

					@Override
					public void download_resource(int type, long resId, String fileName) {
						//关闭界面并返回下载资源文件的选择结果
						Intent intent = getIntent();
						intent.putExtra("resId", resId);
						intent.putExtra("dlType", type);
						
						setResult(RESULT_OK, intent);
						FunctionMainActivity.this.finish();
					}

					@Override
					public void open_subid(long subid, boolean newWindow, String otherParams) {
						FuncInfo funcInfo = EntboostCache.getFuncInfo(subid);
						if (funcInfo!=null) {
							Intent intent = new Intent(FunctionMainActivity.this, FunctionMainActivity.class);
							intent.putExtra("funcInfo", funcInfo);
							startActivity(intent);
						}
					}

					@Override
					public void defaultLink() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								view.loadUrl(url);
							}
						});
					}

					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
								UIUtils.showToast(FunctionMainActivity.this, errMsg);
							}
						});
					}
				});

				return true;
			}
		});
		
		String url = funcInfo.getFunc_url(tab_type);
		if (eParams!=null && eParams.size()>0) {
			for (Entry<String, Object> entry : eParams.entrySet()) {
				url = url + "&" + entry.getKey() + "=" + entry.getValue();
			}
		}
		Log4jLog.i(LONG_TAG, url);
		mWebView.loadUrl(url);
		
		//导航菜单
		EntboostUM.loadFnav(Long.valueOf(funcInfo.getSub_id()), new LoadFnavListener() {
			@Override
			public void onFailure(int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						UIUtils.showToast(FunctionMainActivity.this, errMsg);
					}
				});
			}
			
			@Override
			public void onLoadFuncSuccess(final List<FnavInfo> fnavInfos) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						// 获取一级菜单
						List<FnavInfo> fnavInfos1 = new ArrayList<FnavInfo>();
						for (FnavInfo fi : fnavInfos) {
							if (fi.getParent_navid() == null || fi.getParent_navid() == 0) {
								fnavInfos1.add(fi);
							}
						}
						
						LinearLayout layout = (LinearLayout) findViewById(R.id.menu_Layout);
						int size = layout.getChildCount();
						if (size > fnavInfos1.size()) {
							size = fnavInfos1.size();
						}
						
						if (fnavInfos.size() > 0) {
							layout.setVisibility(View.VISIBLE);
						}
						
						for (int i = 0; i < size; i++) {
							View child = layout.getChildAt(i);
							//child.setVisibility(View.INVISIBLE);
							final FnavInfo fnavInfo = fnavInfos1.get(i);
							
							final List<FnavInfo> fnavInfos2 = new ArrayList<FnavInfo>();
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
										View popView = mInflater.inflate(R.layout.list_pop, null);
										ListView popListView = (ListView) popView.findViewById(R.id.pop_list);
										
										final ListPopAdapter mListPopAdapter = new ListPopAdapter(FunctionMainActivity.this, fnavInfos2, R.layout.item2_list_pop);
										popListView.setAdapter(mListPopAdapter);
										UIUtils.showWindow(v, popListView, new OnItemClickListener() {
											@Override
											public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
												FnavInfo fnavInfo = (FnavInfo) mListPopAdapter.getItem(position);
												mWebView.loadUrl(fnavInfo.getUrl());
											}
										});
									}
								}
							});
						}
					}
				});
			}
		});
	}

	private void initMenu(ViewGroup layout, FnavInfo fnavInfo, List<FnavInfo> fnavInfos2) {
		for (int i = 0, j = layout.getChildCount(); i < j; i++) {
			View child = layout.getChildAt(i);
			if (child instanceof TextView) {
				child.setVisibility(View.VISIBLE);
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
