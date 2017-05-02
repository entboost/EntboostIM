package com.entboost.im.contact;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_ACCOUNT_TYPE;
import net.yunim.service.constants.EB_CONTACT_TYPE;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.CardInfo;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.listener.EditContactListener;
import net.yunim.service.listener.QueryUserListener;
import net.yunim.utils.YIResourceUtils;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.ui.base.activity.MyActivityManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DefaultUserInfoActivity extends EbActivity {
	private static String LONG_TAG = DefaultUserInfoActivity.class.getName();
	
	@ViewInject(R.id.user_head)
	private ImageView userHead;
	@ViewInject(R.id.na)
	private TextView na;
	@ViewInject(R.id.account)
	private TextView account;
	@ViewInject(R.id.uid)
	private TextView uid;
	@ViewInject(R.id.ph)
	private TextView ph;
	@ViewInject(R.id.tel)
	private TextView tel;
	@ViewInject(R.id.em)
	private TextView em;
	@ViewInject(R.id.ti)
	private TextView ti;
	@ViewInject(R.id.de)
	private TextView de;
	@ViewInject(R.id.en)
	private TextView en;
	@ViewInject(R.id.ad)
	private TextView ad;
	
	@ViewInject(R.id.btnLayout)
	private LinearLayout btnLayout;
	@ViewInject(R.id.send_btn)
	private Button sendBtn;
	@ViewInject(R.id.add_contact_btn)
	private Button addContactBtn;
	
	//好友验证模式(添加好友是否需要验证)
	private boolean contactAuthMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_default_user_info);
		//getTitleBar().setTitleText("xxx");
		
		ViewUtils.inject(this);
		renderView(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		renderView(intent);
	}

	//渲染界面
	private void renderView(Intent intent) {
		btnLayout.setVisibility(View.GONE);
		
		//确定好友验证模式
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
			contactAuthMode = true;
		}
		
		//加载电子名片
		final Long uid = intent.getLongExtra("uid", -1);
		if (uid > 0) {
			EntboostUM.loadDefaultCardInfo(uid + "", new QueryUserListener() {
				@Override
				public void onFailure(int code, String errMsg) {
					Log4jLog.e(LONG_TAG, errMsg + "(" + code + ")");
				}
				
				@Override
				public void onQueryUserSuccess(final CardInfo cardInfo, String fInfo) {
					fillData(cardInfo);
				}
			});
		}
	}
	
	//填充数据
	private void fillData(final CardInfo card) {
		if (card != null) {
			//在主线程执行
			HandlerToolKit.runOnMainThreadAsync(new Runnable() {
				@Override
				public void run() {
//					if(StringUtils.isNotBlank(card.getEc())) {
//						Long mid=Long.valueOf(card.getEc());
						
//						if (StringUtils.isNotBlank(card.getHid()) && !card.getHid().equals("0")) {
//							Bitmap img = YIResourceUtils.getHeadBitmap(Long.valueOf(card.getHid()));
//							if (img != null) {
//								userHead.setImageBitmap(img);
//							} else {
//								ImageLoader.getInstance().displayImage(card.getHeadUrl(), userHead, MyApplication.getInstance().getUserImgOptions());
//							}
//						}
						
//						MemberInfo member = EntboostCache.getMemberByCode(mid);
//						if(member!=null){
//							Bitmap img = YIResourceUtils.getHeadBitmap(member.getH_r_id());
//							if (img != null) {
//								userHead.setImageBitmap(img);
//							} else {
//								ImageLoader.getInstance().displayImage(member.getHeadUrl(), userHead,
//										MyApplication.getInstance().getUserImgOptions());
//							}
//						}
//					}
					
					//头像图片
					if (StringUtils.isNotBlank(card.getHid()) && !card.getHid().equals("0")) {
						Bitmap img = YIResourceUtils.getHeadBitmap(Long.valueOf(card.getHid()));
						if (img != null) {
							userHead.setImageBitmap(img);
						} else {
							ImageLoader.getInstance().displayImage(card.getHeadUrl(), userHead, MyApplication.getInstance().getUserImgOptions());
						}
					}
					
					//其它信息
					Long lUid = card.getUid();
					if (lUid!=null)
						uid.setText(String.valueOf(lUid));	
					
					if (card.getAccount()!=null)
						account.setText(card.getAccount());
					
					na.setText(StringUtils.isNotBlank(card.getAcn())?card.getAcn():card.getNa());
					ph.setText(card.getPh());
					tel.setText(card.getTel());
					em.setText(card.getEm());
					ti.setText(card.getTi());
					de.setText(card.getDe());
					en.setText(card.getEn());
					ad.setText(card.getAd());
					
					//工具按钮
					if (lUid!=null && lUid>0) {
						boolean showBtnLayout = false;
						sendBtn.setVisibility(View.GONE);
						addContactBtn.setVisibility(View.GONE);
						ContactInfo contactInfo = EntboostCache.getContactInfo(lUid);
						
						//相同企业成员 或 相同群组成员 或 好友 才允许发送聊天消息
						if (lUid-EntboostCache.getUid()!=0 && (card.getT()==EB_ACCOUNT_TYPE.EB_ACCOUNT_TYPE_IN_ENT.ordinal()
								|| (contactInfo!=null && contactInfo.getType()==EB_CONTACT_TYPE.EB_CONTACT_TYPE_AUTH.getValue()))) {
							sendBtn.setVisibility(View.VISIBLE);
							showBtnLayout = true;
						}
						
						if (lUid-EntboostCache.getUid()!=0 && (contactInfo==null || (contactAuthMode && contactInfo.getType()!=EB_CONTACT_TYPE.EB_CONTACT_TYPE_AUTH.getValue()))) {
							addContactBtn.setVisibility(View.VISIBLE);
							showBtnLayout = true;
						}
						
						if (showBtnLayout)
							btnLayout.setVisibility(View.VISIBLE);
					}
				}
			});
		}
	}
	
	//邀请好友(联系人)
	private void addContact(String description) {
		showProgressDialog("正在加为好友！");
		Long lUid = null;
		String str = uid.getText().toString();
		if (StringUtils.isNotBlank(str)) {
			lUid = Long.valueOf(str);
		}
		
		EntboostUM.addContact(null, lUid, account.getText().toString(), na.getText().toString(), description, null, new EditContactListener() {
			@Override
			public void onFailure(int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						showToast(errMsg);
						removeProgressDialog();
					}
				});
			}
			
			@Override
			public void onOauthForword() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						showToast("加为好友的邀请已经发出，请等待对方验证！");
						removeProgressDialog();
					}
				});
			}			
			
			@Override
			public void onEditContactSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						finish();
					}
				});
			}
		});
	}
	
	@OnClick(R.id.add_contact_btn)
	public void addContact(View view) {
		if (contactAuthMode) {
			final EditText input = new EditText(this);
			showDialog("邀请好友的留言", input, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String value = input.getText().toString();
					addContact(value);
				}
			});
		} else {
			addContact("");
		}
	}
	
	@OnClick(R.id.send_btn)
	public void sendMsg(View view) {
		//寻找已打开的聊天会话界面，并关闭它和在它之上的Activity
		Activity activity = MyActivityManager.getInstance().popToActivity(ChatActivity.class.getName());
		if (activity!=null)
			MyActivityManager.getInstance().popOneActivity(activity);
		
		
		// 打开聊天会话界面
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.INTENT_TITLE, na.getText());
		intent.putExtra(ChatActivity.INTENT_TOID, Long.valueOf(uid.getText().toString()));
		this.startActivity(intent);
	}
}
