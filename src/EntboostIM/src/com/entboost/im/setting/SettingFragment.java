package com.entboost.im.setting;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.entity.AccountInfo;
import net.yunim.service.entity.FuncInfo;
import net.yunim.utils.YIResourceUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.global.AbConstant;
import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.function.FunctionMainActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.im.user.LoginActivity;
import com.entboost.im.user.UserInfoActivity;
import com.entboost.ui.base.activity.MyActivityManager;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingFragment extends EbFragment {

	private TextView userText;
	private ImageView userHead;
	
	@Override
	public void onResume() {
		super.onResume();
		
		refreshUserHead();
	}
	
	/**
	 * 刷新用户头像
	 */
	public void refreshUserHead() {
		AccountInfo user = EntboostCache.getUser();
		
		// 当前用户名称
		if (userText!=null)
			userText.setText(user.getUsername());
		
		// 当前用户默认名片头像
		if (userHead!=null) {
			Bitmap img = YIResourceUtils.getHeadBitmap(user.getHead_rid());
			if (img != null) {
				userHead.setImageBitmap(img);
			} else {
				ImageLoader.getInstance().displayImage(user.getHeadUrl(), userHead, MyApplication.getInstance().getUserImgOptions());
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = onCreateEbView(R.layout.fragment_setting, inflater, container);
		
		userText = (TextView) view.findViewById(R.id.user_name);
		userHead = (ImageView) view.findViewById(R.id.user_head);
		userHead.setFocusable(false);
		
		view.findViewById(R.id.user_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, UserInfoActivity.class);
				startActivity(intent);
			}
		});
		
		//检查版本
		view.findViewById(R.id.version_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Intent intent = new Intent(activity, CheckVersionActivity.class);
				startActivity(intent);
			}
		});
		
		//通知提醒
		view.findViewById(R.id.notify_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, NotificationSettingActivity.class);
				startActivity(intent);
			}
		});
		
		//个人收藏
		if (EntboostCache.isSupportMyCollectionFuncInfo()) {
			View collectLayoutView = view.findViewById(R.id.collect_layout);
			collectLayoutView.setVisibility(View.VISIBLE);
			collectLayoutView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FuncInfo funcInfo = (FuncInfo) EntboostCache.getMyCollectionFuncInfo();
					if (funcInfo!=null) {
						Intent intent = new Intent(SettingFragment.this.getActivity(), FunctionMainActivity.class);
						intent.putExtra("funcInfo", funcInfo);
						startActivity(intent);
					}
				}
			});
		}
		
		//私隐与安全
		view.findViewById(R.id.s_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, SecuritySettingActivity.class);
				startActivity(intent);
			}
		});
		/*
		//聊天对话设置
		view.findViewById(R.id.chat_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UIUtils.showToast(activity, "聊天对话正在建设中...");
			}
		});*/
		
		//退出登录
		view.findViewById(R.id.logout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view = mInflater.inflate(R.layout.dialog_exit, null);
				activity.showDialog(AbConstant.DIALOGBOTTOM, view);
				
				//登出当前用户
				view.findViewById(R.id.logout_layout).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EntboostLC.logout();
						MyApplication.getInstance().setLogin(false);
						
						activity.getBottomDialog().cancel();
						activity.finish();
						
						//退出所有Activity
						MyActivityManager.getInstance().clearAllActivity();
						Intent intent = new Intent(activity, LoginActivity.class);
						startActivity(intent);
						
						//清除通知栏消息
						//UIUtils.cancelNotificationMsg(activity);
					}
				});
				
				//退出应用程序
				view.findViewById(R.id.exit_layout).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EntboostLC.exit();
						
						activity.getBottomDialog().cancel();
						activity.finish();
						
						//清除通知栏消息
						//UIUtils.cancelNotificationMsg(activity);
						//退出所有Activity
						MyActivityManager.getInstance().clearAllActivity();
						//完全退出进程
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								System.exit(0);
							}
						}, 1000);
					}
				});
			}
		});
		
		return view;
	}
}
