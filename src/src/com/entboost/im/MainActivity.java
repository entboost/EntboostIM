package com.entboost.im;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.EntboostUM;
import net.yunim.service.cache.EbCache;
import net.yunim.service.entity.DynamicNews;
import net.yunim.service.entity.FuncInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.listener.EditGroupListener;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.widget.EditText;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.base.EbMainActivity;
import com.entboost.im.chat.CallListActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.chat.FileListActivity;
import com.entboost.im.contact.FriendMainFragment;
import com.entboost.im.contact.SearchContactActivity;
import com.entboost.im.function.FunctionListFragment;
import com.entboost.im.function.FunctionMainActivity;
import com.entboost.im.global.IMStepExecutor;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.OtherUtils;
import com.entboost.im.global.UIUtils;
import com.entboost.im.group.PersonGroupEditActivity;
import com.entboost.im.message.MessageListFragment;
import com.entboost.im.setting.SettingFragment;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.activity.MyActivityManager;
import com.entboost.ui.base.view.popmenu.PopMenuConfig;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;

public class MainActivity extends EbMainActivity {
	
	/** The tag. */
	private static String TAG = MainActivity.class.getSimpleName();
	private static String LONG_TAG = MainActivity.class.getName();
	
	private WakeLock wakeLock; //休眠管理对象
	
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
				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});
		ab.setCancelable(false);
		ab.show();
	}

	@Override
	public void onUserStateChange(Long uid) {
		if (friendMainFragment != null) {
			friendMainFragment.refreshPage(false);
		}
		super.onUserStateChange(uid);
	}

	@Override
	public void onReceiveDynamicNews(DynamicNews news) {
		super.onReceiveDynamicNews(news);
		
		// 接收到消息后，如果程序位于后台运行，需要发送系统通知
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		if (MyApplication.getInstance().isShowNotificationMsg() || km.inKeyguardRestrictedInputMode()) {
			Intent intent = null;
			if (news.getType() == DynamicNews.TYPE_GROUPCHAT || news.getType() == DynamicNews.TYPE_USERCHAT) {
				intent = new Intent(this, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setData(Uri.parse("custom://" + System.currentTimeMillis()));
				if (news.getType() == DynamicNews.TYPE_GROUPCHAT) {
					intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
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
				UIUtils.sendNotificationMsg(this, R.drawable.notify, news.getTitle(), news.getContentText(), EntboostCache.getUnreadNumDynamicNews(), intent);
			}
		}
		// 更新主菜单的未读数量标记
		mBottomTabView.getItem(0).showTip(EntboostCache.getUnreadNumDynamicNews());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log4jLog.d(LONG_TAG, "onNewIntent");
		
		super.onNewIntent(intent);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log4jLog.d(LONG_TAG, this.getClass().getName()+", activity push (in onCreate())");
		
		super.onCreate(savedInstanceState);
		
		messageListFragment = new MessageListFragment();
		addView("聊天", messageListFragment, this.getResources().getDrawable(R.drawable.menu1), this.getResources().getDrawable(R.drawable.menu1_n));
		
		friendMainFragment = new FriendMainFragment();
		addView("联系人", friendMainFragment, this.getResources().getDrawable(R.drawable.menu2), this.getResources().getDrawable(R.drawable.menu2_n));
		
		functionListFragment = new FunctionListFragment();
		addView("应用", functionListFragment, this.getResources().getDrawable(R.drawable.menu3), this.getResources().getDrawable(R.drawable.menu3_n));
		
		settingFragment = new SettingFragment();
		addView("设置", settingFragment, this.getResources().getDrawable(R.drawable.menu4), this.getResources().getDrawable(R.drawable.menu4_n));
		
		mBottomTabView.initItemsTip(R.drawable.tab_red_circle);
		initMenu();
		
		//阻止CPU休眠
		PowerManager pm =(PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock =pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "entboost_cpu_lock");
		wakeLock.acquire();
		
		//请求电池管理白名单
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			String packageName = this.getPackageName();
			if (!pm.isIgnoringBatteryOptimizations(packageName)) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
			    intent.setData(Uri.parse("package:" + packageName));
			    startActivity(intent);
			}
//			else {
//				intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//				startActivity(intent);
//			}
		}
		
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(packageName)) {
//			Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//			intent.setData(Uri.parse(packageName));
//			startActivity(intent);
//		}
	}
	
	//处理服务异常和未登陆状态
	//切换至登录页面并执行登录流程
	private void handleNotWork() {
		//退出当前页
		MainActivity.this.finish();
		
		MyApplication application = (MyApplication)getApplication();
		
		//退出欢迎页
//		if (application.getWelcomeActivity()!=null) {
//			application.getWelcomeActivity().finish();
//			application.setWelcomeActivity(null);
//		}
		
		application.initEbConfig("MainActivity");
		IMStepExecutor.getInstance().executeTryLogon(application.getWelcomeActivity());
		
		//切换至新的欢迎页
		Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		final String mark = getClass().getSimpleName();
		boolean isWork = OtherUtils.checkServiceWork(0, false, mark);
		//service未启动
		if (!isWork) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int times = 0;
					boolean isWork = OtherUtils.checkServiceWork(times, true, mark);
					while (!isWork && times<2) {
						Log4jLog.d(LONG_TAG, "check service work times = " + times);
						times ++ ;
						isWork = OtherUtils.checkServiceWork(times, true, mark);
					}
					
					//2秒以后再次在主线程检测
					HandlerToolKit.runOnMainThreadSync(new Runnable() {
						@Override
						public void run() {
							boolean isWork = OtherUtils.checkServiceWork(0, false, mark);
							Log4jLog.e(LONG_TAG, "the servie is not running, exit MainActivity and try to start it"); 
							
							if (!isWork) {
								handleNotWork();
							}
						}
					});

				}
			}).start();
		} else if (EbCache.getInstance().getSysDataCache().getAppInfo()==null){
			Log4jLog.e(LONG_TAG, "AppAccountInfo为空, 当前状态未登录，exit MainActivity and try to start it");
			handleNotWork();
		}
	}

	@Override
	protected void onPause() {
//		//让CPU允许休眠
//		if (wakeLock!=null) {
//			wakeLock.release();
//			wakeLock = null;
//		}
				
		super.onPause();
	}
	
	//初始化右上角菜单
	public void initMenu() {
		PopMenuConfig config = new PopMenuConfig();
		config.setBackground_resId(R.drawable.popmenu);
		config.setTextColor(Color.WHITE);
		config.setShowAsDropDownYoff(28);
		
		//主按钮一(查找用户)
		this.getTitleBar().addRightImageButton(R.drawable.main_search, config, new PopMenuItem("查找用户", 
			new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					Intent intent = new Intent(MainActivity.this, SearchContactActivity.class);
					startActivity(intent);
				}

			}));
		
		//主按钮二(弹出子菜单)
		this.getTitleBar().addRightImageButton(R.drawable.main_add, config,
			new PopMenuItem("添加联系人", R.drawable.menu_add_contact, R.layout.item_menu2,
					new PopMenuItemOnClickListener() {
						
						@Override
						public void onItemClick() {
							FuncInfo funcInfo = EntboostCache.getFindFuncInfo();
							if (funcInfo != null) {
								Intent intent = new Intent(MainActivity.this, FunctionMainActivity.class);
								intent.putExtra("funcInfo", funcInfo);
								startActivity(intent);
							}
						}
					}),
			new PopMenuItem("添加联系人分组",  R.drawable.menu_add_tempgroup, R.layout.item_menu2, new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					final EditText input = new EditText(MainActivity.this);
					
					showDialog("添加分组", input, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String value = input.getText().toString();
							if (StringUtils.isBlank(value)) {
								showToast("分组名称不能为空！");
								return;
							}
							
							showProgressDialog("正在添加分组！");
							EntboostUM.addContactGroup(value, new EditGroupListener() {
								@Override
								public void onFailure(int code, final String errMsg) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											removeProgressDialog();
											showToast(errMsg);
										}
									});
								}

								@Override
								public void onEditGroupSuccess(Long dep_code) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											removeProgressDialog();
											if (friendMainFragment != null) {
												friendMainFragment.refreshPage(true);
											}
										}
									});
								}
							});
						}
					});
				}
			}),
			new PopMenuItem("添加个人群组",R.drawable.menu_add_tempgroup, R.layout.item_menu2, new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					Intent intent = new Intent(MainActivity.this, PersonGroupEditActivity.class);
					startActivity(intent);
				}
			}), new PopMenuItem("文件目录",  R.drawable.menu_add_tempgroup, R.layout.item_menu2, new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					Intent intent = new Intent(MainActivity.this, FileListActivity.class);
					startActivity(intent);
				}
			}));
	}

	@Override
	public void onAddMember(Long uid, Long empid, Long depCode) {
		Log4jLog.d(LONG_TAG, "onAddMember");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyDepartmentChanged(false, depCode);
			friendMainFragment.notifyGroupChanged(false, depCode);
			friendMainFragment.notifyEntChanged(false, depCode, false, false, true);
		}
		
		super.onAddMember(uid, empid, depCode);
	}
	
	@Override
	public void onExitMember(Long uid, Long depCode) {
		Log4jLog.d(LONG_TAG, "onExitMember");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyDepartmentChanged(false, depCode);
			friendMainFragment.notifyGroupChanged(false, depCode);
			friendMainFragment.notifyEntChanged(false, depCode, false, false, true);
		}
		
		super.onExitMember(uid, depCode);
	}

	@Override
	public void onUpdateMember(Long uid, Long empid, Long depCode) {
		Log4jLog.d(LONG_TAG, "onUpdateMember");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyDepartmentChanged(false, depCode);
			friendMainFragment.notifyGroupChanged(false, depCode);
			friendMainFragment.notifyEntChanged(false, depCode, false, false, true);
		}
		
		super.onUpdateMember(uid, empid, depCode);
	}

	@Override
	public void onUpdateGroup(Long depCode) {
		Log4jLog.d(LONG_TAG, "onUpdateGroup");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyDepartmentChanged(false, depCode);
			friendMainFragment.notifyGroupChanged(false, depCode);
			friendMainFragment.notifyEntChanged(false, depCode, false, true, false);
		}
		
		super.onUpdateGroup(depCode);
	}

	@Override
	public void onDeleteGroup(Long depCode) {
		Log4jLog.d(LONG_TAG, "onDeleteGroup");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyDepartmentChanged(false, depCode);
			friendMainFragment.notifyGroupChanged(false, depCode);
			friendMainFragment.notifyEntChanged(false, depCode, true, false, false);
		}
		
		super.onDeleteGroup(depCode);
	}
	
	@Override
	public void onCall2Group(Long depCode, Long fromUid) {
		Log4jLog.d(LONG_TAG, "onCall2Group"); 
		
		//发起者才需要处理该事件
		if (fromUid - EntboostCache.getUid() == 0) {
			GroupInfo group = EntboostCache.getGroup(depCode);
			
			if (group != null) {
				//如果已存在聊天界面，关闭它
				Activity existActivity = MyActivityManager.getInstance().getActivity(ChatActivity.class.getName());
				if (existActivity!=null)
					existActivity.finish();
				
				//切换至新的聊天会话界面
				Intent intent = new Intent(this, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(ChatActivity.INTENT_TITLE, group.getDep_name());
				intent.putExtra(ChatActivity.INTENT_UID, group.getDep_code());
				intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
				startActivity(intent);
			}
		}
		
		super.onCall2Group(depCode, fromUid);
	}
	
	@Override
	public void onAddContactRequest(Long uid, String remark) {
		Log4jLog.d(LONG_TAG, "onAddContactRequest");
		
		super.onAddContactRequest(uid, remark);
	}

	@Override
	public void onAddContactAccept(Long uid, Long contactId) {
		Log4jLog.d(LONG_TAG, "onAddContactAccept");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyContactChanged(false);
		}
		super.onAddContactAccept(uid, contactId);
	}

	@Override
	public void onAddContactReject(Long uid, String remark) {
		Log4jLog.d(LONG_TAG, "onAddContactReject");
		
		super.onAddContactReject(uid, remark);
	}

	@Override
	public void onDeleteContact(Long uid, Long contactId, boolean remove) {
		Log4jLog.d(LONG_TAG, "onDeleteContact");

		if (friendMainFragment!=null) {
			friendMainFragment.notifyContactChanged(false);
		}
		super.onDeleteContact(uid, contactId, remove);
	}

	@Override
	public void onUpdateContact(Long uid, Long contactId) {
		Log4jLog.d(LONG_TAG, "onUpdateContact");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyContactChanged(false);
		}
		super.onUpdateContact(uid, contactId);
	}

	@Override
	public void onUpdateContactGroup(Long contactGroupId, String contactGroupName) {
		Log4jLog.d(LONG_TAG, "onUpdateContactGroup");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyContactChanged(false);
		}
		super.onUpdateContactGroup(contactGroupId, contactGroupName);
	}

	@Override
	public void onDeleteContactGroup(Long contactGroupId) {
		Log4jLog.d(LONG_TAG, "onDeleteContactGroup");
		
		if (friendMainFragment!=null) {
			friendMainFragment.notifyContactChanged(false);
		}
		super.onDeleteContactGroup(contactGroupId);
	}
	
}
