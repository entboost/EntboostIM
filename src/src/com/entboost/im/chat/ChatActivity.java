package com.entboost.im.chat;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.yunim.service.EntboostCM;
import net.yunim.service.EntboostCache;
import net.yunim.service.cache.EbCache;
import net.yunim.service.cache.FileCacheUtils;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.ChatRoomRichMsg;
import net.yunim.service.entity.FileCache;
import net.yunim.service.entity.FuncInfo;
import net.yunim.service.entity.Resource;
import net.yunim.service.listener.CallUserListener;
import net.yunim.service.listener.SendFileListener;
import net.yunim.utils.YINetworkUtils;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.MainActivity;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.contact.DefaultUserInfoActivity;
import com.entboost.im.function.FunctionMainActivity;
import com.entboost.im.global.OtherUtils;
import com.entboost.im.global.UIUtils;
import com.entboost.im.group.MemberListActivity;
import com.entboost.im.group.MemberSelectActivity;
import com.entboost.ui.base.activity.MyActivityManager;
import com.entboost.ui.base.listener.KeyboardChangeListener;
import com.entboost.ui.base.listener.KeyboardChangeListener.KeyBoardListener;
import com.entboost.ui.base.view.popmenu.PopMenu;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.entboost.voice.ExtAudioRecorder;
import com.entboost.voice.VoiceCallback;

public class ChatActivity extends EbActivity implements KeyBoardListener{

	/** The tag. */
	private static String LONG_TAG = ChatActivity.class.getName();
	
	public static final String INTENT_TITLE = "intent_title";
	public static final String INTENT_UID = "intent_uid";
	public static final String INTENT_CHATTYPE = "intent_chattype";
	public static final String INTENT_TO_MSG_ID = "intent_to_msgid";

	public static final int CHATTYPE_PERSON = 0;
	public static final int CHATTYPE_GROUP = 1;

	private int chattype = CHATTYPE_PERSON;
	private Long uid; //用户编号或群组编号(依赖chattype配合判断)
	private EditText mContentEdit;
	private ChatMsgViewAdapter mChatMsgViewAdapter;
	private ListView mMsgListView;
	private LinearLayout emotionsAppPanel;
	private GridView expressionGriView;
	private EmotionsImageAdapter emotionsImageAdapter;
	private Button picBtn;
	private Button sendBtn;
	private ImageView voiceImg;
	private String title;
	private LinearLayout morePanel;
	private ImageButton moreBtn;
	private Button fileBtn;
	
	//键盘弹出/隐藏事件监听器
	private KeyboardChangeListener mKeyboardChangeListener;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onSendFileIng(ChatRoomRichMsg msg) {
		if (mChatMsgViewAdapter != null) {
			mChatMsgViewAdapter.refreshFileProgressBar(msg);
		}
	}

	@Override
	public void onReceiveFileIncoming(FileCache fileCache) {
		if (mChatMsgViewAdapter != null) {
			mChatMsgViewAdapter.refreshFileProgressBar(fileCache);
		}
	}

	@Override
	public void onReceiveUserMessage(ChatRoomRichMsg msg) {
		// 启动会话界面，接收消息自动设置已读
		if (msg.getChatType() == ChatRoomRichMsg.CHATTYPE_GROUP) {
			if (msg.getDepCode() - uid == 0) {
				//EntboostCache.readMsg(msg.getDepCode());
				EntboostCache.markReadDynamicNewsBySender(msg.getDepCode());
			} else {
				pageInfo.showInfo(msg.getSendName() + "[" + msg.getDepName()+ "]:" + UIUtils.getTipCharSequence(getResources(), msg.getTipHtml(), true), 5);
			}
		} else {
			if (msg.getSender() - uid == 0 || msg.getSender() - EntboostCache.getUid() == 0) {
				EntboostCache.markReadDynamicNewsBySender(msg.getSender());
			} else {
				pageInfo.showInfo(msg.getSendName() + ":" + UIUtils.getTipCharSequence(getResources(), msg.getTipHtml(), true), 5);
			}
		}
		refreshPage(true);
	}

	/**
	 * 刷新会话页面
	 * @param srcollToBottom 是否滚动到视图底部
	 */
	private void refreshPage(boolean srcollToBottom) {
		if (uid != null) {
			mChatMsgViewAdapter.initChat(EntboostCache.getChatMsgs(uid));
		}
		mChatMsgViewAdapter.notifyDataSetChanged();
		//mMsgListView.setAdapter(mChatMsgViewAdapter);
		if (srcollToBottom)
			mMsgListView.setSelection(mMsgListView.getBottom());
	}

	/**
	 * 发送消息的状态发生改变
	 */
	@Override
	public void onSendStatusChanged(ChatRoomRichMsg msg) {
		refreshPage(true);
	}

	//处理服务异常和未登陆状态
	private void handleNotWork() {
		//退出当前页
		finish();
		
		if (!MyActivityManager.getInstance().isActivityExist(MainActivity.class.getName())) {
			Log4jLog.e(LONG_TAG, "MainActivity instance is not exist, switch to it");
			
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getApplicationContext().startActivity(intent);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		boolean isWork = OtherUtils.checkServiceWork(0, false, "ChatActivity");
		//service未启动，退出当前聊天页面
		if (!isWork) {
			Log4jLog.e(LONG_TAG, "the servie is not running, exit ChatActivity");
			handleNotWork();
		} else if (EbCache.getInstance().getSysDataCache().getAppInfo()==null){
			Log4jLog.e(LONG_TAG, "AppAccountInfo为空, 当前状态未登录，exit ChatActivity");
			handleNotWork();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_chat);
		
		//键盘弹出/隐藏事件监听器
        mKeyboardChangeListener = new KeyboardChangeListener(this);
        mKeyboardChangeListener.setKeyBoardListener(this);
		
		AbTitleBar titleBar = this.getTitleBar();
		title = this.getIntent().getStringExtra(INTENT_TITLE);
		titleBar.setTitleText(title);
		uid = this.getIntent().getLongExtra(INTENT_UID, -1);
		if (uid > 0) {
			// 设置消息已读
			EntboostCache.markReadDynamicNewsBySender(uid);
			//EntboostCache.readMsg(uid);
		}
		
		chattype = this.getIntent().getIntExtra(INTENT_CHATTYPE, CHATTYPE_PERSON);
		if (chattype == CHATTYPE_PERSON) {
			EntboostCache.loadPersonChatMsg(uid);// 加载已有缓存会话信息
		} else {
			EntboostCache.loadGroupChatMsg(uid);
		}
		
		if (chattype == CHATTYPE_PERSON) { //单聊
			// 增加右上角会话对方的名片信息按钮
			this.getTitleBar().addRightImageButton(R.drawable.uitb_20, null, new PopMenuItem(new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					Intent intent = new Intent(ChatActivity.this, DefaultUserInfoActivity.class);
					intent.putExtra("uid", uid);
					startActivity(intent);
				}
			}));
			
			// 增加右上角转换临时讨论组按钮
			this.getTitleBar().addRightImageButton(R.drawable.ic_action_add_group, null, new PopMenuItem(new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					showDialog("提示", "是否需要转为临时讨论组", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EntboostCM.call2Group(uid, new CallUserListener() {
								@Override
								public void onFailure(int code, final String errMsg) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											removeProgressDialog();
											pageInfo.showError(errMsg);
										}
									});
								}
								
								@Override
								public void onCalling() {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											showProgressDialog("正在转为临时讨论组");
										}
									});
								}
								
								@Override
								public void onCallAlerting(Long uid, Long callid) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											removeProgressDialog();
											showToast("转为临时讨论组成功！");
										}
									});
								}

								@Override
								public void onChatEntered() {
								}
							});
						}
					});
				}

			}));
		} else { //群聊
			// 增加右上角查看群组成员列表按钮
			this.getTitleBar().addRightImageButton(R.drawable.uitb_19, null, new PopMenuItem(new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					Intent intent = new Intent(ChatActivity.this, MemberListActivity.class);
					intent.putExtra("depid", uid);
					startActivity(intent);
				}
			}));
			
			// 增加右上角查看共享文件列表按钮
			final FuncInfo funcInfo = EntboostCache.getGroupFilesFuncInfo();
			if (funcInfo!=null) {
				this.getTitleBar().addRightImageButton(R.drawable.uitb_61, null, new PopMenuItem(new PopMenuItemOnClickListener() {
					@Override
					public void onItemClick() {
						Intent intent = new Intent(ChatActivity.this, FunctionMainActivity.class);
						intent.putExtra("funcInfo", funcInfo);
						
						LinkedHashMap<String, Object> eParams = new LinkedHashMap<String, Object>();
						eParams.put("gid", uid);
						intent.putExtra("eParams", eParams);
						
						startActivityForResult(intent, 5);
					}
				}));
			}
		}
		
		// 增加右上角漫游消息的按钮
		// 1、判断是否有漫游消息的功能
		final AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if (AppAccountInfo.SAVE_CONVERSATIONS_ONLINE == appInfo.getSave_conversations() || AppAccountInfo.SAVE_CONVERSATIONS_ALL == appInfo.getSave_conversations()) {
			this.getTitleBar().addRightImageButton(R.drawable.menu_add_tempgroup, null, new PopMenuItem(new PopMenuItemOnClickListener() {
				@Override
				public void onItemClick() {
					Intent intent = new Intent(ChatActivity.this, OnlieChatMsgActivity.class);
					if (chattype == CHATTYPE_PERSON) {
						intent.putExtra("onlineChatUrl", appInfo.getPersonConversationsAllUrl(uid));
					} else {
						intent.putExtra("onlineChatUrl", appInfo.getGroupConversationsAllUrl(uid));
					}
					startActivity(intent);
				}
			}));
		}
		
		//聊天内容输入区
		mContentEdit = (EditText) findViewById(R.id.content);
		sendBtn = (Button) findViewById(R.id.sendBtn);
		voiceImg = (ImageView) this.findViewById(R.id.imageViewvoice);
		final Button voiceSendBtn = (Button) this.findViewById(R.id.voicesendBtn);
		final ImageButton voiceBtn = (ImageButton) this.findViewById(R.id.voiceBtn);
		final ImageButton keyBtn = (ImageButton) this.findViewById(R.id.keyBtn);
		
		//语音消息聊天
		voiceSendBtn.setOnTouchListener(new View.OnTouchListener() {
			private boolean noPermission = false;
			private long time = 0;
			private ExtAudioRecorder recorder;
			//录音情况回调
			private VoiceCallback callback = new VoiceCallback () {
				@Override
				public void noPermission() {
					noPermission = true;
					showToast("不能正常录音，请检查是否有开通录音权限");
				}
			};
			
			//检测录音合理性和发送录音
			public synchronized void checkAndUp(long targetTime) {
				//结束录音
				if (recorder != null)
					recorder.stopRecord();
				
				if (targetTime==0 || targetTime!=time) {
					Log4jLog.e(LONG_TAG, "checkAndUp miss, invalid duration: targetTime=" + targetTime + ", time="+time);
					return;
				}
				
				if (time==0) {
					Log4jLog.e(LONG_TAG, "checkAndUp miss, invalid time:" + time);
					return;
				}
				
				time = 0;
				
				// 松开事件发生后执行代码的区域
				voiceSendBtn.setText("按住说话");
				voiceImg.setVisibility(View.GONE);
				
				if (noPermission) {
					Log4jLog.e(LONG_TAG, "no record permission");
					return;
				}
				
				// 语音少于1秒不发
				String filePath = recorder.getFilePath();
				//Log4jLog.i(LONG_TAG, "check voice filePath:" + filePath);
				int duration = ExtAudioRecorder.getVideoPlayTime(filePath);
				Log4jLog.i(LONG_TAG, "record duration:" + duration);
				
				if (duration<1) {
					Log4jLog.e(LONG_TAG, "voice is too short");
					showToast("发送的语音时长少于1秒！");
					return;
				}
				
				if (uid < 0)
					pageInfo.showError(ChatActivity.this.getString(R.string.msg_send_uiderror));
				
				if (uid >= 0) {
					Log4jLog.i(LONG_TAG, "voice to upload");
					
					if (chattype == CHATTYPE_PERSON)
						EntboostCM.sendVoice(uid ,recorder.getFilePath());
					else
						EntboostCM.sendGroupVoice(uid, recorder.getFilePath());
				}
				
				//在主线程执行
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						refreshPage(true); //刷新界面
					}
				});
			}

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					//结束之前的录音
					if (recorder != null)
						recorder.stopRecord();
					
					if (!YINetworkUtils.isNetworkConnected(ChatActivity.this)) {
						pageInfo.showError(ChatActivity.this.getString(R.string.msg_error_localNoNetwork));
						return false;
					}
					
					final long now = System.currentTimeMillis();
					time = now;
					
					new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... arg0) {
							try {
								Thread.sleep(60 * 1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							Log4jLog.d(LONG_TAG, "onPostExecute, time="+now);
							checkAndUp(now);
						}
					}.execute();
					// 按住事件发生后执行代码的区域
					voiceImg.setVisibility(View.VISIBLE);
					
					Log4jLog.d(LONG_TAG, "going to start record, time="+System.currentTimeMillis());
					
					recorder = ExtAudioRecorder.getInstance(false, callback);
					String filePath = System.currentTimeMillis() + ".wav";
					recorder.recordChat(FileCacheUtils.getChatVoicePath(), filePath);
					
					voiceSendBtn.setText("松开发送");
				}
					break;
				case MotionEvent.ACTION_MOVE: {
					// 移动事件发生后执行代码的区域
				}
					break;
				case MotionEvent.ACTION_CANCEL: {
					Log4jLog.d(LONG_TAG, "record cancel");
					
					//结束录音
					if (recorder != null)
						recorder.stopRecord();
					
					time = 0;
					
					// 松开事件发生后执行代码的区域
					voiceSendBtn.setText("按住说话");
					voiceImg.setVisibility(View.GONE);
				}
					break;
				case MotionEvent.ACTION_UP: {
					Log4jLog.d(LONG_TAG, "MotionEvent.ACTION_UP, time="+time);
					checkAndUp(time);
				}
					break;
				default:
					break;
				}
				return false;
			}
		});
		
		//"切换语音消息"按钮
		voiceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View btnLayout = findViewById(R.id.voiceBtnLayout);
				btnLayout.setVisibility(View.VISIBLE);
				View contentLayout = findViewById(R.id.contentLayout);
				contentLayout.setVisibility(View.GONE);
				voiceBtn.setVisibility(View.GONE);
				keyBtn.setVisibility(View.VISIBLE);
				
				hidSoftInput(); //隐藏软键盘
				emotionsAppPanel.setVisibility(View.GONE); //隐藏表情输入选择视图
				morePanel.setVisibility(View.GONE); //隐藏"更多"面板
			}
		});
		
		//"切换键盘输入"按钮
		keyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View btnLayout = findViewById(R.id.voiceBtnLayout);
				btnLayout.setVisibility(View.GONE);
				View contentLayout = findViewById(R.id.contentLayout);
				contentLayout.setVisibility(View.VISIBLE);
				voiceBtn.setVisibility(View.VISIBLE);
				keyBtn.setVisibility(View.GONE);
			}
		});
		
		//文本输入框
		mContentEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					//显示"发送"按钮
					sendBtn.setVisibility(View.VISIBLE);
					moreBtn.setVisibility(View.GONE);
				} else {
					//隐藏"发送"按钮
					sendBtn.setVisibility(View.GONE);
					moreBtn.setVisibility(View.VISIBLE);
				}
			}
		});
		
		//发送按钮
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = mContentEdit.getText().toString().trim();
				if (StringUtils.isBlank(text)) {
					UIUtils.showToast(ChatActivity.this, "不能发送空消息！");
					return;
				}
				if (!YINetworkUtils.isNetworkConnected(ChatActivity.this)) {
					pageInfo.showError(ChatActivity.this.getString(R.string.msg_error_localNoNetwork));
					return;
				}
				if (uid < 0) {
					pageInfo.showError(ChatActivity.this.getString(R.string.msg_send_uiderror));
					return;
				}
				if (uid >= 0) {
					if (chattype == CHATTYPE_PERSON) {
						EntboostCM.sendText(uid, text);
					} else {
						EntboostCM.sendGroupText(uid, text);
					}
				}
				
				// 清空文本框
				mContentEdit.setText("");
				mMsgListView.setSelection(mMsgListView.getBottom());
				// 隐藏表情输入视图
				emotionsAppPanel.setVisibility(View.GONE);
				// View view = getWindow().peekDecorView();
				// if (view != null) {
				// InputMethodManager inputmanger = (InputMethodManager)
				// getSystemService(Context.INPUT_METHOD_SERVICE);
				// inputmanger.hideSoftInputFromWindow(view.getWindowToken(),
				// 0);
				// }
				
				//延时刷新聊天视图
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshPage(true);
					}
				}, 100);
			}
		});

		//聊天消息列表
		mMsgListView = (ListView) this.findViewById(R.id.mListView);
		mChatMsgViewAdapter = new ChatMsgViewAdapter(ChatActivity.this, EntboostCache.getChatMsgs(uid));
		mChatMsgViewAdapter.setChatActivity(this);
		mMsgListView.setAdapter(mChatMsgViewAdapter);
		mMsgListView.setSelection(mChatMsgViewAdapter.getCount() - 1); //滚动到最后一条记录
		
		//长按消息弹出菜单
		final PopMenu popMenu = new PopMenu(this);
		mChatMsgViewAdapter.setPopMenu(popMenu);
		
		//定义子菜单项
		List<PopMenuItem> popMenuItems = new ArrayList<PopMenuItem>();
		//"复制"菜单项
		PopMenuItem item1 = new PopMenuItem("复制文本", 0, R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				ChatRoomRichMsg msg = (ChatRoomRichMsg) popMenu.getObj();
				UIUtils.copy(msg.getTipText(), ChatActivity.this);
				popMenu.dismiss();
			}
		});
		popMenuItems.add(item1);
		
		//"转发"菜单项
		PopMenuItem item2 = new PopMenuItem("转  发", 0, R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				popMenu.dismiss();
				
				//切换至选择成员的界面
				ChatRoomRichMsg msg = (ChatRoomRichMsg) popMenu.getObj();
				Intent intent = new Intent(ChatActivity.this, MemberSelectActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("msgid", msg.getMsgid());
				
				//把当前用户在选择界面除外
				List<Long> excludeUids = new ArrayList<Long>();
				excludeUids.add(EntboostCache.getUid()); //当前登录用户编号
				excludeUids.add(ChatActivity.this.uid); //当前一对一聊天界面的对方用户编号
				intent.putExtra("excludeUids", (Serializable)excludeUids);
				
				startActivityForResult(intent, 1);
			}
		});
		popMenuItems.add(item2);
		mChatMsgViewAdapter.setPopMenuItems(popMenuItems);
		
		//"删除"菜单项
		PopMenuItem item3 = new PopMenuItem("删  除", 0, R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				popMenu.dismiss();
				
				ChatRoomRichMsg msg = (ChatRoomRichMsg) popMenu.getObj();
				EntboostCM.deleteChatMsgById(msg.getId());
				refreshPage(false);
			}
		});
		popMenuItems.add(item3);
		mChatMsgViewAdapter.setPopMenuItems(popMenuItems);		
		
		//表情输入选择区域
		expressionGriView = (GridView) this.findViewById(R.id.expressionGridView);
		emotionsImageAdapter = new EmotionsImageAdapter(this);
		expressionGriView.setAdapter(emotionsImageAdapter);
		expressionGriView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Resource emotions = (Resource) emotionsImageAdapter.getItem(position);
				UIUtils.addEmotions(ChatActivity.this.getResources(), mContentEdit, emotions);
				//emotionsAppPanel.setVisibility(View.GONE);
			}
		});
		emotionsAppPanel = (LinearLayout) this.findViewById(R.id.expressionAppPanel);
		
		//"选取待发送图片"按钮
		picBtn = (Button) this.findViewById(R.id.picBtn);
		picBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!YINetworkUtils.isNetworkConnected(ChatActivity.this)) {
					pageInfo.showError(ChatActivity.this.getString(R.string.msg_error_localNoNetwork));
					return;
				}
				getPicFromContent();
			}
		});
		
		//“切换表情输入框”按钮
		ImageButton emotionBtn = (ImageButton) this.findViewById(R.id.emotionBtn);
		emotionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (emotionsAppPanel.getVisibility() == View.GONE) {
					hidSoftInput(); //隐藏软键盘
					morePanel.setVisibility(View.GONE); //隐藏"更多"面板
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							emotionsAppPanel.setVisibility(View.VISIBLE); //显示表情输入选择视图
						}
					}, 150);
				} else {
					emotionsAppPanel.setVisibility(View.GONE);
				}
				((BaseAdapter) expressionGriView.getAdapter()).notifyDataSetChanged();
			}
		});

		//“其它输入方式”按钮
		morePanel = (LinearLayout) this.findViewById(R.id.morePanel);
		moreBtn = (ImageButton) this.findViewById(R.id.moreBtn);
		moreBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (morePanel.getVisibility() == View.GONE) {
					hidSoftInput();	//隐藏软键盘
					emotionsAppPanel.setVisibility(View.GONE); //隐藏表情输入选择视图
					morePanel.setVisibility(View.VISIBLE); //显示"更多"面板
				} else {
					morePanel.setVisibility(View.GONE);
				}
			}
		});
		
		//“选取待发送文件”按钮
		fileBtn = (Button) this.findViewById(R.id.fileBtn);
		final FuncInfo funcInfo = EntboostCache.getGroupFilesFuncInfo();
		if (chattype == CHATTYPE_PERSON || (chattype == CHATTYPE_GROUP && funcInfo!=null)) {
			fileBtn.setVisibility(View.VISIBLE);
			fileBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (!YINetworkUtils.isNetworkConnected(ChatActivity.this)) {
						pageInfo.showError(ChatActivity.this.getString(R.string.msg_error_localNoNetwork));
						return;
					}
					getFileFromContent();
				}
			});
		} else {
			fileBtn.setVisibility(View.GONE);
		}
		
		//处理默认自动发送的消息(通常用于转发消息)
		final Long msgId = getIntent().getLongExtra(INTENT_TO_MSG_ID, 0);
		if (msgId>0) {
			getIntent().removeExtra(INTENT_TO_MSG_ID);
			//新建子线程执行
			new Thread(new Runnable() {
				@Override
				public void run() {
					//调用EB API执行转发
					EntboostCM.forwardRichMsg(msgId, ChatActivity.this.uid, ChatActivity.this.chattype==CHATTYPE_GROUP?true:false);
				}
			}).start();
		}
	}

    @Override
    public void onKeyboardChange(boolean isShow, int keyboardHeight) {
        //Log4jLog.d(LONG_TAG, "onKeyboardChange() called with: " + "isShow = [" + isShow + "], keyboardHeight = [" + keyboardHeight + "]");
    	if (isShow) { //键盘弹出
    		morePanel.setVisibility(View.GONE);			//隐藏"更多"的内容面板
    		emotionsAppPanel.setVisibility(View.GONE);	//隐藏表情输入选择视图
    	} else { //键盘隐藏
    		
    	}
    }
	
    //隐藏软键盘
    private void hidSoftInput() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   
        imm.hideSoftInputFromWindow(mContentEdit.getWindowToken(),0);  
    }
    
	private void getFileFromContent() {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
		File file = new File(FileCacheUtils.getFilePath());
		
		Log4jLog.i(LONG_TAG, "filePath="+FileCacheUtils.getFilePath());
		Log4jLog.i(LONG_TAG, "Uri="+Uri.fromFile(file).toString());
		
		intent.setDataAndType(Uri.fromFile(file), "*/*");
		startActivityForResult(intent, 3);
	}

	private void getPicFromContent() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			//intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
		}
		startActivityForResult(intent, 2);
	}

//	private String getPathFromUri(Uri fileUrl) {
//		String fileName = null; 
//		
//		Uri filePathUri = fileUrl;
//		if (filePathUri != null) {
//			Log4jLog.i(LONG_TAG, "filePathUri=" + filePathUri.toString());
//			
//			if (filePathUri.getScheme().toString().compareTo("content") == 0) { // content://开头的uri
//				Log4jLog.i(LONG_TAG, "type is 'content'");
//				
//				if (Build.VERSION.SDK_INT >= 19) {
//					String wholeID = filePathUri.getLastPathSegment();
//					Log4jLog.i(LONG_TAG, "wholeID:" + wholeID);
//					
//					String id = (wholeID.split(":").length==1)?wholeID:wholeID.split(":")[1];
//					Log4jLog.i(LONG_TAG, "id:" + id);
//					
//					String[] column = {MediaStore.Images.Media.DATA};
//					
//					String sel = MediaStore.Images.Media._ID + "=?";
//					Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[] {id}, null);
//					int columnIndex = cursor.getColumnIndex(column[0]);
//					Log4jLog.i(LONG_TAG, "columnIndex:" + columnIndex);
//					
//					if (cursor.moveToFirst()) {
//						fileName = cursor.getString(columnIndex);
//						Log4jLog.i(LONG_TAG, "fileName:" + fileName);
//					}
//					 
//					cursor.close();
//				 } else {
//					String[] projection = { MediaStore.Images.Media.DATA };
//					Cursor cursor = getContentResolver().query(filePathUri, projection, null, null, null);
//					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//					cursor.moveToFirst();
//					fileName = cursor.getString(column_index);
//				 }
//				
////				Cursor cursor = null;
////				
////				if(Build.VERSION.SDK_INT >= 19){
////					 String id = fileUrl.getLastPathSegment().split(":")[1];
////			            final String[] imageColumns = {MediaStore.Images.Media.DATA };
////			            final String imageOrderBy = null;
////			            Uri tempUri = fileUrl; //getUri();
////			            cursor = getContentResolver().query(tempUri, imageColumns,
////			                    MediaStore.Images.Media._ID + "="+id, null, imageOrderBy);
////			            
////				} else {
////		            String[] projection = { MediaColumns.DATA };
////		            cursor = getContentResolver().query(fileUrl, projection, null, null, null);
////				}
////				
////				//cursor = getContentResolver().query(fileUrl, null, null, null, null);
////				if (cursor != null && cursor.moveToFirst()) {
////					int column_index = cursor
////							.getColumnIndexOrThrow(Build.VERSION.SDK_INT >= 19?MediaStore.Images.Media.DATA:MediaColumns.DATA);
////					
////					
////					fileName = cursor.getString(column_index); // 取出文件路径
////					
////					// Android 4.1 更改了SD的目录，sdcard映射到/storage/sdcard0
////					if (!fileName.startsWith("/storage")
////							&& !fileName.startsWith("/mnt")) {
////						// 检查是否有”/mnt“前缀
////						fileName = "/mnt" + fileName;
////					}
////					cursor.close();
////				}
//			} else if (filePathUri.getScheme().compareTo("file") == 0) // file:///开头的uri
//			{
//				Log4jLog.i(LONG_TAG, "type is 'file'");
//				
//				fileName = Uri.decode(filePathUri.toString()).replace("file://", "");
//				int index = fileName.indexOf("/sdcard");
//				fileName = index == -1 ? fileName : fileName.substring(index);
//				
//				// if (!fileName.startsWith("/mnt")) {
//				// // 加上"/mnt"头
//				// fileName += "/mnt";
//				// }
//			}
//		} else {
//			Log4jLog.i(LONG_TAG, "fileUrl is null");
//		}
//		
//		return fileName;
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		
		switch(requestCode) {
		case 1: //选取转发消息的目标对象，并执行转发
			Long msgId = data.getLongExtra("msgid", 0);
			Long targetUid = data.getLongExtra("target_uid", 0);
			String targetName = data.getStringExtra("target_name");
			Log4jLog.e(LONG_TAG, "转发消息，msg_id="+msgId + ", target_uid=" + targetUid + ", target_name=" + targetName);
			
			if (targetUid>0 && msgId>0) {
				//结束当前聊天窗口
				finish();
				//切换至对应的聊天界面
				Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(ChatActivity.INTENT_TITLE, targetName);
				intent.putExtra(ChatActivity.INTENT_UID, targetUid);
				intent.putExtra(ChatActivity.INTENT_TO_MSG_ID, msgId);
				
				startActivity(intent);
			}
			break;
		case 2: //获取手机中的图片
		case 3: // 获取手机中的文件
			//String filePath = getPathFromUri(data.getData());
			String filePath = OtherUtils.getFileAbsolutePath(this, data.getData());
			Log4jLog.i(LONG_TAG, "onActivityResult filePath:" + filePath);
			
			if (requestCode == 2) {// 获取手机中的图片
				if (filePath.endsWith("jpg") || filePath.endsWith("png")) {
					sendPic(filePath);
				}
			} else if (requestCode == 3) {// 获取手机中的文件
				sendFile(filePath);
			}
			break;
		case 5: //群共享文件
			long resId = data.getLongExtra("resId", 0);
			int dlType = data.getIntExtra("dlType", -1);
			
			if (dlType==1 && resId>0) {
				EntboostCM.receiveGroupFile(resId, this.uid);
			}
			
			break;
		}
	}

	/**
	 * 发送图片
	 * @param picUri 图片文件绝对路径
	 */
	public void sendPic(String picUri) {
		if (uid < 0) {
			pageInfo.showError(ChatActivity.this.getString(R.string.msg_send_uiderror));
			return;
		}
		if (uid >= 0) {
			if (chattype == CHATTYPE_PERSON) {
				EntboostCM.sendPic(uid, picUri);
			} else {
				EntboostCM.sendGroupPic(uid, picUri);
			}
		}
		
		//隐藏“更多”工具栏
		morePanel.setVisibility(View.GONE);
		//延时刷新聊天视图
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshPage(true);
			}
		}, 500);
	}
	
	/**
	 * 发送文件
	 * @param filePath 文件绝对路径
	 */
	public void sendFile(String filePath) {
		if (uid < 0) {
			pageInfo.showError(ChatActivity.this.getString(R.string.msg_send_uiderror));
			return;
		}
		if (uid >= 0) {
			if (chattype == CHATTYPE_PERSON) {
				EntboostCM.sendFile(uid, filePath);
			} else {
				//上传群共享文件
				EntboostCM.uploadGroupFile(uid, filePath, new SendFileListener() {
					@Override
					public void onOverMaxPermit() {
						//在主线程异步执行
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								showToast("文件超过最大限制");;
							}
						});
					}
					
					@Override
					public void onFailure(final int code, String errMsg) {
					}
					@Override
					public void onStart(long msg_id) {
					}
				});
			}
		}
		
		//隐藏“更多”工具栏
		morePanel.setVisibility(View.GONE);
		//延时刷新聊天视图
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshPage(true);
			}
		}, 1000);
	}
}
