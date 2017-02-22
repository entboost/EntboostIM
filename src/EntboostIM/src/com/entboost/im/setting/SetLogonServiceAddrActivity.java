package com.entboost.im.setting;

import net.yunim.service.EntboostLC;
import net.yunim.service.constants.ConfigConstants;
import net.yunim.service.listener.InitAppKeyListener;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.IMStepExecutor;
import com.entboost.im.global.MyApplication;
import com.entboost.im.push.ThirdPartyPushHelper;
import com.entboost.im.user.LoginActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class SetLogonServiceAddrActivity extends EbActivity {
	@ViewInject(R.id.setService_name)
	private EditText setService_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_set_logon_service_addr);
		ViewUtils.inject(this);
		
		EditText eText = ((EditText) findViewById(R.id.setService_name));
		if (StringUtils.isNotBlank(EntboostLC.getLogonCenterAddr()) /*&& !ConfigConstants.LOGONCENTER_ADDR.equals(EntboostLC.getLogonCenterAddr())*/) {
			eText.setText(EntboostLC.getLogonCenterAddr());
		} else {
			eText.setText(ConfigConstants.LOGONCENTER_ADDR);
		}
	}

	@OnClick(R.id.setService_save_btn)
	public void save(View view) {
		final String name = setService_name.getText().toString();
		if (!StringUtils.equals(name, EntboostLC.getLogonCenterAddr())) {
			showDialog("提示", "修改服务器地址，需要重新验证AppKey,确定需要修改吗？", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showProgressDialog("正在修改服务器地址，并重新验证AppKey!");
					
					EntboostLC.initAPPKey(name, MyApplication.appid, MyApplication.appkey, new InitAppKeyListener() {
						@Override
						public void onFailure(int code, final String errMsg) {
							HandlerToolKit.runOnMainThreadAsync(new Runnable() {
								@Override
								public void run() {
									pageInfo.showError(errMsg);
									removeProgressDialog();
									
									IMStepExecutor.getInstance().exitApplication();
								}
							});
						}

						@Override
						public void onInitAppKeySuccess() {
							HandlerToolKit.runOnMainThreadAsync(new Runnable() {
								@Override
								public void run() {
									//准备接入手机厂商推送平台
									ThirdPartyPushHelper.preparePushEnvironment(SetLogonServiceAddrActivity.this);
									
									removeProgressDialog();
									
									Intent intent = new Intent(SetLogonServiceAddrActivity.this, LoginActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									
									finish();
								}
							});
						}
					});
				}
			});
		} else {
			finish();
		}
	}

	@OnClick(R.id.setService_cancel_btn)
	public void cancel(View view) {
		finish();
	}

}
