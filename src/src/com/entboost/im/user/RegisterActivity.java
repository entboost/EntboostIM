package com.entboost.im.user;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.listener.RegisterListener;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterActivity extends EbActivity {

	@ViewInject(R.id.register_username)
	private EditText userName;
	@ViewInject(R.id.register_name)
	private EditText name;
	@ViewInject(R.id.register_passwd)
	private EditText pwd;
	@ViewInject(R.id.register_confirm_passwd)
	private EditText confirmpwd;
	@ViewInject(R.id.register_ent)
	private EditText ent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_register);
		ViewUtils.inject(this);
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if (appInfo != null && appInfo.getOpen_register() == 1) {
			findViewById(R.id.register_ent_layout).setVisibility(View.GONE);
		}
	}

	@OnClick(R.id.register_register)
	public void regiter(View view) {
		final String email = userName.getText().toString();
		final String nameStr = name.getText().toString();
		final String pwdstr = pwd.getText().toString();
		final String confirmpwdstr = confirmpwd.getText().toString();
		final String entname = ent.getText().toString();
		if (!StringUtils.equals(pwdstr, confirmpwdstr)) {
			pageInfo.showError("两次输入密码不一致！");
			return;
		}
		if (StringUtils.isBlank(email)) {
			pageInfo.showError("邮箱地址不能为空！");
			return;
		}
		if (StringUtils.isBlank(nameStr)) {
			pageInfo.showError("用户名称不能为空！");
			return;
		}
		if (StringUtils.isBlank(pwdstr)) {
			pageInfo.showError("登录密码不能为空！");
			return;
		}
		showProgressDialog("正在注册中...");
		EntboostUM.emailRegister(email,nameStr, pwdstr, entname, new RegisterListener() {
			@Override
			public void onFailure(int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						pageInfo.showError(errMsg);
						removeProgressDialog();
					}
				});
			}

			@Override
			public void onRegisterSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						String temp="";
						if (EntboostCache.getAppInfo().getSend_reg_mail() == 1) {
							temp="，请到注册邮箱激活帐号！";
						}
						showDialog("提示", "注册成功"+temp, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
					}
				});
			}
		});
	}

}
