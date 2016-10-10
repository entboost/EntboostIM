package com.entboost.im;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.DynamicNews;
import net.yunim.service.entity.FuncInfo;
import net.yunim.service.entity.SearchResultInfo;
import net.yunim.service.listener.EditGroupListener;
import net.yunim.utils.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

import com.entboost.Log4jLog;
import com.entboost.im.base.EbMainActivity;
import com.entboost.im.chat.CallListActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.chat.FileListActivity;
import com.entboost.im.contact.FriendMainFragment;
import com.entboost.im.contact.SearchContactActivity;
import com.entboost.im.function.FunctionListFragment;
import com.entboost.im.function.FunctionMainActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.im.message.BroadcastMessageListActivity;
import com.entboost.im.message.MessageListFragment;
import com.entboost.im.persongroup.PersonGroupEditActivity;
import com.entboost.im.setting.SetLogonServiceAddrActivity;
import com.entboost.im.setting.SettingFragment;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.view.pupmenu.PopMenuConfig;
import com.entboost.ui.base.view.pupmenu.PopMenuItem;
import com.entboost.ui.base.view.pupmenu.PopMenuItemOnClickListener;
import com.entboost.voice.RealTimeAudioRecorder;
import com.lidroid.xutils.util.LogUtils;

public class MainActivity extends EbMainActivity {
	
	/** The tag. */
	private static String TAG = MainActivity.class.getSimpleName();
	private static String LONG_TAG = MainActivity.class.getName();
	
	private MessageListFragment messageListFragment;
	private FriendMainFragment friendMainFragment;
	private FunctionListFragment functionListFragment;
	private SettingFragment settingFragment;

	@Override
	public void onBackPressed() {
		// 实现Home键效果
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);
	}

	@Override
	public void online_another() {
		AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
		ab.setTitle("提示");
		ab.setMessage("您的帐号已经在其它地方登录！");
		ab.setIcon(android.R.drawable.ic_dialog_alert);
		ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				EntboostLC.logout();
				finish();
				Intent intent = new Intent(MainActivity.this,
						LoginActivity.class);
				startActivity(intent);
			}
		});
		ab.setCancelable(false);
		ab.show();
	}

	@Override
	public void onUserStateChange(Long uid) {
		if (friendMainFragment != null) {
			friendMainFragment.refreshPage();
		}
		super.onUserStateChange(uid);
	}

	@Override
	public void onReceiveDynamicNews(DynamicNews news) {
		super.onReceiveDynamicNews(news);
		// 接收到消息后，如果程序位于后台运行，需要发送系统通知
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		if (MyApplication.getInstance().isShowNotificationMsg()
				|| km.inKeyguardRestrictedInputMode()) {
			Intent intent = null;
			if (news.getType() == DynamicNews.TYPE_GROUPCHAT
					|| news.getType() == DynamicNews.TYPE_USERCHAT) {
				intent = new Intent(this, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setData(Uri.parse("custom://"
						+ System.currentTimeMillis()));
				if (news.getType() == DynamicNews.TYPE_GROUPCHAT) {
					intent.putExtra(ChatActivity.INTENT_CHATTYPE,
							ChatActivity.CHATTYPE_GROUP);
				}
				intent.putExtra(ChatActivity.INTENT_TITLE, news.getTitle());
				intent.putExtra(ChatActivity.INTENT_UID, news.getSender());
			} else if (news.getType() == DynamicNews.TYPE_CALL) {
				intent = new Intent(this, CallListActivity.class);
			} else if (news.getType() == DynamicNews.TYPE_MYMESSAGE) {
				FuncInfo funcInfo = EntboostCache.getMessageFuncInfo();
				if (funcInfo != null) {
					intent = new Intent(this, FunctionMainActivity.class);
					intent.putExtra("funcInfo", funcInfo);
					intent.putExtra("tab_type", FuncInfo.SYS_MSG);
				}
			} else if (news.getType() == DynamicNews.TYPE_BMESSAGE) {
				FuncInfo funcInfo = EntboostCache.getMessageFuncInfo();
				if (funcInfo != null) {
					intent = new Intent(this, FunctionMainActivity.class);
					intent.putExtra("funcInfo", funcInfo);
					intent.putExtra("tab_type", FuncInfo.BC_MSG);
				}
				// intent = new Intent(this,
				// BroadcastMessageListActivity.class);
			}
			if (intent != null) {
				UIUtils.sendNotificationMsg(this, R.drawable.notify,
						news.getTitle(), news.getContentText(),
						EntboostCache.getUnreadNumDynamicNews(), intent);
			}
		}
		// 更新主菜单的未读数量标记
		mBottomTabView.getItem(0).showTip(
				EntboostCache.getUnreadNumDynamicNews());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log4jLog.d(LONG_TAG, this.getClass().getName()+", activity push");
		
		super.onCreate(savedInstanceState);
		messageListFragment = new MessageListFragment();
		addView("聊天", messageListFragment,
				this.getResources().getDrawable(R.drawable.menu1), this
						.getResources().getDrawable(R.drawable.menu1_n));
		friendMainFragment = new FriendMainFragment();
		addView("联系人", friendMainFragment,
				this.getResources().getDrawable(R.drawable.menu2), this
						.getResources().getDrawable(R.drawable.menu2_n));
		functionListFragment = new FunctionListFragment();
		addView("应用", functionListFragment,
				this.getResources().getDrawable(R.drawable.menu3), this
						.getResources().getDrawable(R.drawable.menu3_n));
		settingFragment = new SettingFragment();
		addView("设置", settingFragment,
				this.getResources().getDrawable(R.drawable.menu4), this
						.getResources().getDrawable(R.drawable.menu4_n));
		mBottomTabView.initItemsTip(R.drawable.tab_red_circle);
		initMenu();

	}

	public void initMenu() {
		PopMenuConfig config = new PopMenuConfig();
		config.setBackground_resId(R.drawable.popmenu);
		config.setTextColor(Color.WHITE);
		config.setShowAsDropDownYoff(28);
		this.getTitleBar().addRightImageButton(R.drawable.main_search, config,
				new PopMenuItem("查找用户", new PopMenuItemOnClickListener() {

					@Override
					public void onItemClick() {
						Intent intent = new Intent(MainActivity.this,
								SearchContactActivity.class);
						startActivity(intent);
					}

				}));
		this.getTitleBar().addRightImageButton(
				R.drawable.main_add,
				config,
				new PopMenuItem("添加联系人", 
						R.drawable.menu_add_contact, R.layout.item_menu2,
						new PopMenuItemOnClickListener() {

							@Override
							public void onItemClick() {
								// Intent intent = new Intent(MainActivity.this,
								// AddContactActivity.class);
								// startActivity(intent);
								FuncInfo funcInfo = EntboostCache
										.getFindFuncInfo();
								if (funcInfo != null) {
									Intent intent = new Intent(
											MainActivity.this,
											FunctionMainActivity.class);
									intent.putExtra("funcInfo", funcInfo);
									startActivity(intent);
								}
							}

						}),
				new PopMenuItem("添加联系人分组", 
						R.drawable.menu_add_tempgroup, R.layout.item_menu2,
						new PopMenuItemOnClickListener() {

							@Override
							public void onItemClick() {
								final EditText input = new EditText(
										MainActivity.this);
								showDialog("添加分组", input,
										new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												String value = input.getText()
														.toString();
												if (StringUtils.isBlank(value)) {
													showToast("分组名称不能为空！");
													return;
												}
												showProgressDialog("正在添加分组！");
												EntboostUM
														.addContactGroup(
																value,
																new EditGroupListener() {

																	@Override
																	public void onFailure(
																			String errMsg) {
																		removeProgressDialog();
																		showToast(errMsg);
																	}

																	@Override
																	public void onEditGroupSuccess(
																			Long dep_code) {
																		removeProgressDialog();
																		if (friendMainFragment != null) {
																			friendMainFragment
																					.refreshPage();
																		}
																	}
																});
											}
										});
							}

						}),
				new PopMenuItem("添加个人群组",R.drawable.menu_add_tempgroup, R.layout.item_menu2,
						new PopMenuItemOnClickListener() {

							@Override
							public void onItemClick() {
								Intent intent = new Intent(MainActivity.this,
										PersonGroupEditActivity.class);
								startActivity(intent);
							}

						}),
				new PopMenuItem("文件目录", 
						R.drawable.menu_add_tempgroup, R.layout.item_menu2,
						new PopMenuItemOnClickListener() {

							@Override
							public void onItemClick() {
								Intent intent = new Intent(MainActivity.this,
										FileListActivity.class);
								startActivity(intent);
							}
						}));

	}

}
