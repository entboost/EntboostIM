package com.entboost.im.setting;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.entity.AccountInfo;
import net.yunim.utils.ResourceUtils;
import net.yunim.utils.UIUtils;
import net.yunim.utils.VersionUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.global.AbConstant;
import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.global.AppUtils;
import com.entboost.im.global.MyApplication;
import com.entboost.im.user.LoginActivity;
import com.entboost.im.user.UserInfoActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingFragment extends EbFragment {

	@Override
	public void onResume() {
		super.onResume();
		// 显示自己的默认名片头像
		final ImageView userHead = (ImageView) this.getActivity()
				.findViewById(R.id.user_head);
		AccountInfo user = EntboostCache.getUser();
		userHead.setFocusable(false);
		Bitmap img = ResourceUtils.getHeadBitmap(user.getHead_rid());
		if (img != null) {
			userHead.setImageBitmap(img);
		} else {
			ImageLoader.getInstance().displayImage(user.getHeadUrl(), userHead,
					MyApplication.getInstance().getImgOptions());
		}
		TextView userText = (TextView) this.getActivity().findViewById(R.id.user_name);
		userText.setText(user.getUsername());
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = onCreateEbView(R.layout.fragment_setting, inflater,
				container);
		view.findViewById(R.id.user_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(activity,
								UserInfoActivity.class);
						startActivity(intent);
					}
				});
		view.findViewById(R.id.version_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(final View v) {
						// 检查应用程序版本
						String clientVer = AppUtils.getVersion(v.getContext());
						VersionUtils.checkApkVer(clientVer, v.getContext(),
								null);

					}
				});
		view.findViewById(R.id.s_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						UIUtils.showToast(activity, "隐私与安全正在建设中...");
					}
				});
		view.findViewById(R.id.chat_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						UIUtils.showToast(activity, "聊天对话正在建设中...");
					}
				});
		view.findViewById(R.id.logout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						View view = mInflater.inflate(R.layout.dialog_exit,
								null);
						activity.showDialog(AbConstant.DIALOGBOTTOM, view);
						view.findViewById(R.id.logout_layout)
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										EntboostLC.logout();
										activity.getBottomDialog().cancel();
										activity.finish();
										Intent intent = new Intent(activity,
												LoginActivity.class);
										startActivity(intent);
									}
								});
						view.findViewById(R.id.exit_layout).setOnClickListener(
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										EntboostLC.exit();
										activity.getBottomDialog().cancel();
										activity.finish();
									}
								});
					}
				});
		return view;
	}
}
