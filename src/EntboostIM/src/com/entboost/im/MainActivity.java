package com.entboost.im;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.EntboostUM;
import net.yunim.service.cache.EbCache;
import net.yunim.service.entity.DynamicNews;
import net.yunim.service.entity.FuncInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.listener.EditGroupListener;
import net.yunim.utils.YINetworkUtils;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.OtherUtils;
import com.entboost.im.global.UIUtils;
import com.entboost.im.group.PersonGroupEditActivity;
import com.entboost.im.message.MessageListFragment;
import com.entboost.im.receiver.NotificationReceiver;
import com.entboost.im.setting.SettingFragment;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.activity.MyActivityManager;
import com.entboost.ui.base.view.popmenu.PopMenuConfig;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;
import com.entboost.utils.PhoneInfo;

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
		//===实现Home键效果===
		
		//返回桌面上次停留的页面
		moveTaskToBack(true);
		//返回桌面的默认页面
//		Intent i = new Intent(Intent.ACTION_MAIN);
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		i.addCategory(Intent.CATEGORY_HOME);
//		startActivity(i);
	}
	
	@Override
	public void online_another(int type) {
		if (type==0)
			goBackLoginView("您的帐号已经在其它地方登录！");
		else
			goBackLoginView("您的密码已被修改，请重新登录！");
	}
	
	@Override
	public void onInvalidAccPassword() {
		goBackLoginView("密码错误，请重新登录!");
	}
	
	//用户登出并跳转到登录界面
	private void goBackLoginView(String message) {
		//返回到主界面
		MyActivityManager.getInstance().popToActivity(MainActivity.class.getName());
		
		AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
		ab.setIcon(android.R.drawable.ic_dialog_alert);
		ab.setTitle("提示");
		ab.setMessage(message);
		
		ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
				
				//执行用户登出
				EntboostLC.logout();
				MyApplication.getInstance().setLogin(false);
				
				//退出所有Activity
				MyActivityManager.getInstance().clearAllActivity();
				
				//跳转到登录界面
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
			friendMainFragment.refreshPage(false, FriendMainFragment.NotifyChangeAll);
		}
		super.onUserStateChange(uid);
	}
	
	@Override
	public void onUserHeadChange(Long uid, Long resId, String cmServer, String httpServer, String md5) {
		//更新聊天会话列表界面
		if (messageListFragment != null) {
			messageListFragment.refreshPage();
		}
		//更新联系人界面
		if (friendMainFragment != null) {
			friendMainFragment.refreshPage(false, FriendMainFragment.NotifyChangeAll);
		}
		//刷新显示当前登录用户的头像
		if (settingFragment!=null && EntboostCache.getUser().getUid()-uid==0) {
			settingFragment.refreshUserHead();
		}
		super.onUserHeadChange(uid, resId, cmServer, httpServer, md5);
	}

	@Override
	public void onLoadContactsLineState() {
		if (friendMainFragment != null) {
			friendMainFragment.refreshPage(false, FriendMainFragment.NotifyChangeContact);
		}
		super.onLoadContactsLineState();
	}

	public static final String EXTRA_BUNDLE = "main_activity_extra_bundle";
	
	public static final String INTENT_DYNAMIC_NEWS_TYPE = "dynamic_news_type";
	
	// 处理接收消息通知事件
	public static void handleReceiveDynamicNews(Context context, DynamicNews news) {
		// 接收到消息后，如果程序位于后台运行或手机锁屏状态，需要发送系统通知
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		if (MyApplication.getInstance().isShowNotificationMsg() || km.inKeyguardRestrictedInputMode()) {
			//检查配置是否允许通知提醒
			SharedPreferences preferences = context.getSharedPreferences("notificationSetting", Context.MODE_PRIVATE);
			boolean enableNotify = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_NEW), true);
			if (!enableNotify) {
				Log4jLog.d(LONG_TAG, "miss a new message notification");
				return;
			}
			
			boolean enableDetails = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_DETAILS), true);
			
			//解析事件内容并创建Intent
			boolean typeMatched = true; //是否发送状态栏通知
			Intent intent = new Intent(context, NotificationReceiver.class);
			Bundle bundle = new Bundle();
			bundle.putInt(INTENT_DYNAMIC_NEWS_TYPE, news.getType());
			
			//不同的消息类型
			switch(news.getType()) {
			case DynamicNews.TYPE_GROUPCHAT:
			case DynamicNews.TYPE_USERCHAT:
				if (news.getType() == DynamicNews.TYPE_GROUPCHAT) {
					bundle.putInt(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
				}
				bundle.putString(ChatActivity.INTENT_TITLE, news.getTitle());
				bundle.putLong(ChatActivity.INTENT_TOID, news.getSender());
				break;
			case DynamicNews.TYPE_CALL:
				break;
			case DynamicNews.TYPE_MYMESSAGE:
				break;
			case DynamicNews.TYPE_BMESSAGE:
				break;
			case DynamicNews.TYPE_MYSYSTEMMESSAGE:
				typeMatched = false;
				break;
			default:
				typeMatched = false;
				Log4jLog.e(LONG_TAG, "unhandle DynamicNews type " + news.getType());
			break;
			}
			
			//发送状态栏通知
			if (typeMatched) {
				intent.putExtra(EXTRA_BUNDLE, bundle);
				UIUtils.sendNotificationMsg(context, R.drawable.notify, enableDetails?news.getTitle():"您有一条未读通知", enableDetails?news.getContentText():"", 
						EntboostCache.getUnreadNumDynamicNews(), intent, UIUtils.PENDINGINTENT_TYPE_BROADCASE);
			}
		}
	}
	
	@Override
	public void onReceiveDynamicNews(DynamicNews news) {
		super.onReceiveDynamicNews(news);
		
		MainActivity.handleReceiveDynamicNews(this, news);
		// 更新主菜单的未读数量标记
		mBottomTabView.getItem(0).showTip(EntboostCache.getUnreadNumDynamicNews());
	}

	//处理特殊的Intent
	private void handleIntent() {
		Bundle bundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
		
        if(bundle != null) {
        	int dynamicNewsType = bundle.getInt(INTENT_DYNAMIC_NEWS_TYPE, -1);
        	if (dynamicNewsType>-1) {
        		Intent intent = null;
        		
				//不同的消息类型
				switch(dynamicNewsType) {
				case DynamicNews.TYPE_GROUPCHAT:
				case DynamicNews.TYPE_USERCHAT:
					intent = new Intent(this, ChatActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					//intent.setData(Uri.parse("custom://" + System.currentTimeMillis()));
					if (bundle.getInt(ChatActivity.INTENT_CHATTYPE) == DynamicNews.TYPE_GROUPCHAT) {
						intent.putExtra(ChatActivity.INTENT_CHATTYPE, bundle.getInt(ChatActivity.INTENT_CHATTYPE));
					}
					intent.putExtra(ChatActivity.INTENT_TITLE, bundle.getString(ChatActivity.INTENT_TITLE));
					intent.putExtra(ChatActivity.INTENT_TOID, bundle.getLong(ChatActivity.INTENT_TOID));
					break;
				case DynamicNews.TYPE_CALL:
					intent = new Intent(this, CallListActivity.class);
					break;
				case DynamicNews.TYPE_MYMESSAGE:
				case DynamicNews.TYPE_BMESSAGE:
					FuncInfo funcInfo = EntboostCache.getMessageFuncInfo();
					if (funcInfo != null) {
						intent = new Intent(this, FunctionMainActivity.class);
						intent.putExtra("funcInfo", funcInfo);
						intent.putExtra("tab_type", dynamicNewsType==DynamicNews.TYPE_MYMESSAGE?FuncInfo.SYS_MSG:FuncInfo.BC_MSG);
					}
					break;
				default:
					Log4jLog.e(LONG_TAG, "unhandle intent, DynamicNews type " + dynamicNewsType);
				break;
				}
				
				//跳转页面
				if (intent!=null) {
					startActivity(intent);
				}
        	}
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Log4jLog.i(LONG_TAG, "onNewIntent");
		super.onNewIntent(intent);
		
		MyApplication application = (MyApplication)getApplication();
		application.setInInterface(true);
		
		setIntent(intent);
		
		//处理特殊的Intent
		handleIntent();
	}
	
	private static String ENABLE_IBO_WIZARD = "enableIBOWizard";
	
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
		addView("我", settingFragment, this.getResources().getDrawable(R.drawable.menu4), this.getResources().getDrawable(R.drawable.menu4_n));
		
		mBottomTabView.initItemsTip(R.drawable.tab_red_circle);
		initMenu();
		
		//手机基本信息
		String manufacturer = PhoneInfo.getManufacturerName(); //手机厂商
		String model = PhoneInfo.getModelName(); //手机型号
		Log4jLog.d(LONG_TAG, "MANUFACTURER:" + manufacturer + ", MODEL:" + model);
		
		PowerManager pm =(PowerManager)getSystemService(Context.POWER_SERVICE);
		
		//请求电池管理白名单
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			String packageName = this.getPackageName();
			try {
				if (!pm.isIgnoringBatteryOptimizations(packageName)) {
					if (manufacturer!=null && manufacturer.equalsIgnoreCase("HUAWEI")) { //华为手机
						//检查是否允许显示向导
						SharedPreferences preferences = getSharedPreferences("first", Context.MODE_PRIVATE);
						boolean enableIBOWizard = preferences.getBoolean(ENABLE_IBO_WIZARD, true);
						if (enableIBOWizard)
							showIBOWizardDialog();
					} else {
						//直接请求允许忽略电池优化
						Intent intent = new Intent();
						intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					    intent.setData(Uri.parse("package:" + packageName));
					    startActivityForResult(intent, 1);
					}
				}
			} catch (Exception e) {
				Log4jLog.e(LONG_TAG, "ignoringBatteryOptimizations error", e);
			}
		} else {
			//阻止CPU休眠
			wakeLock =pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "entboost_cpu_lock");
			wakeLock.acquire();
		}
		
		MyApplication application = (MyApplication)getApplication();
		application.setInInterface(true);
		
		//处理特殊的Intent
		handleIntent();
	}
	
	/**
	 * 忽略电池优化向导对话框
	 */
	@SuppressLint("NewApi")
	private void showIBOWizardDialog(){
	    AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
	    normalDialog.setTitle("请忽略电池优化").setMessage("为了更容易接收到消息，请允许本应用忽略电池优化(就是设置为'允许忽略')");
	    
	    normalDialog.setPositiveButton("进入设置", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	//进入忽略电池优化配置页面
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
				startActivity(intent);
	        }
	    });
	    normalDialog.setNeutralButton("不再提示", 
	        new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	SharedPreferences preferences = getSharedPreferences("first", Context.MODE_PRIVATE);
	    		SharedPreferences.Editor editor = preferences.edit();
	    		editor.putBoolean(ENABLE_IBO_WIZARD, false);
	    		editor.commit();
	        }
	    });
	    normalDialog.setNegativeButton("取消", 
		        new DialogInterface.OnClickListener() {
		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		        	//do nothing
		        }
		    });
	    // 创建实例并显示
	    normalDialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1){
			    
			}
		} else if (resultCode == RESULT_CANCELED){
			if (requestCode == 1){
				//ToastUtils.show(getActivity(), "请开启忽略电池优化~");
			}
		}
	}
	
	//处理服务异常和未登陆状态
	//切换至登录页面并执行登录流程
	private void handleNotWork() {
		if (YINetworkUtils.isNetworkConnected(this)) {
			//退出当前页
			MainActivity.this.finish();
			
			MyApplication application = (MyApplication)getApplication();
			application.initEbConfig("MainActivity");
			application.setInInterface(true);
			application.setLogin(false);
			//IMStepExecutor.getInstance().executeTryLogon();//application.getWelcomeActivity());
			
			//切换至新的欢迎页
			Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else {
			Log4jLog.e(LONG_TAG, "network is disconnected");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		final String mark = getClass().getSimpleName();
		boolean isWork = OtherUtils.checkServiceWork(OtherUtils.MAIN_SERVICE_NAME, 0, false, mark);
		//service未启动
		if (!isWork) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int times = 0;
					boolean isWork = OtherUtils.checkServiceWork(OtherUtils.MAIN_SERVICE_NAME, times, true, mark);
					while (!isWork && times<2) {
						Log4jLog.d(LONG_TAG, "check service work times = " + times);
						times ++ ;
						isWork = OtherUtils.checkServiceWork(OtherUtils.MAIN_SERVICE_NAME, times, true, mark);
					}
					
					//1秒以后再次在主线程检测
					HandlerToolKit.runOnMainThreadSync(new Runnable() {
						@Override
						public void run() {
							boolean isWork = OtherUtils.checkServiceWork(OtherUtils.MAIN_SERVICE_NAME, 0, false, mark);
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
		} else {
			//清除通知栏消息
			UIUtils.cancelNotificationMsg(this);
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
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
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
												friendMainFragment.refreshPage(false, FriendMainFragment.NotifyChangeContact);
												//friendMainFragment.refreshPage(true);
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
		
		if (messageListFragment!=null) {
			messageListFragment.refreshPage();
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
				intent.putExtra(ChatActivity.INTENT_TOID, group.getDep_code());
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
