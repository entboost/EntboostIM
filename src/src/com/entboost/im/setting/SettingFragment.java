package com.entboost.im.setting;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.entity.AccountInfo;
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

import com.entboost.Log4jLog;
import com.entboost.global.AbConstant;
import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.global.AppUtils;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.UIUtils;
import com.entboost.im.global.VersionUtils;
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
		
		AccountInfo user = EntboostCache.getUser();
		// 当前用户名称
		userText.setText(user.getUsername());
		// 当前用户默认名片头像
		Bitmap img = YIResourceUtils.getHeadBitmap(user.getHead_rid());
		if (img != null) {
			userHead.setImageBitmap(img);
		} else {
			ImageLoader.getInstance().displayImage(user.getHeadUrl(), userHead, MyApplication.getInstance().getUserImgOptions());
		}
	}
	
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
		
		view.findViewById(R.id.version_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// 检查应用程序版本
				String clientVer = AppUtils.getVersion(v.getContext());
				VersionUtils.checkApkVer(clientVer, v.getContext(), null);
			}
		});
		
		view.findViewById(R.id.s_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UIUtils.showToast(activity, "隐私与安全正在建设中...");
			}
		});
		
		view.findViewById(R.id.chat_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UIUtils.showToast(activity, "聊天对话正在建设中...");
			}
		});
		
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
						activity.getBottomDialog().cancel();
						activity.finish();
						Intent intent = new Intent(activity, LoginActivity.class);
						startActivity(intent);
						
						//清除通知栏消息
						UIUtils.cancelNotificationMsg(activity);
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
						UIUtils.cancelNotificationMsg(activity);
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
