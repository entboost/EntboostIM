package com.entboost.im.setting;

import net.yunim.service.EntboostLC;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_STATE_CODE;
import net.yunim.service.listener.EditInfoListener;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.im.user.LoginActivity;
import com.entboost.ui.base.activity.MyActivityManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ChangePWDActivity extends EbActivity {
	@ViewInject(R.id.changepwd_oldpasswd)
	private EditText changepwd_oldpasswd;
	@ViewInject(R.id.changepwd_passwd)
	private EditText changepwd_passwd;
	@ViewInject(R.id.changepwd_confirm_passwd)
	private EditText changepwd_confirm_passwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_change_pwd);
		ViewUtils.inject(this);
	}

	@OnClick(R.id.changepwd_btn)
	public void save(View view) {
		String changepwd_oldpasswd_str = changepwd_oldpasswd.getText().toString().trim();
		String changepwd_passwd_str = changepwd_passwd.getText().toString().trim();
		String changepwd_confirm_passwd_str = changepwd_confirm_passwd.getText().toString().trim();
		
		if (StringUtils.isBlank(changepwd_oldpasswd_str)) {
			pageInfo.showError("当前密码不能为空");
			return;
		}
		
		if (StringUtils.isBlank(changepwd_passwd_str) || StringUtils.isBlank(changepwd_confirm_passwd_str)) {
			pageInfo.showError("新密码不能为空");
			return;
		} else {
			pageInfo.hide();
		}
		
		if (!StringUtils.equals(changepwd_passwd_str, changepwd_confirm_passwd_str)) {
			pageInfo.showError("两次输入的新密码不一致");
			return;
		} else {
			pageInfo.hide();
		}
		
		if (StringUtils.equals(changepwd_oldpasswd_str, changepwd_passwd_str)) {
			pageInfo.showError("新密码和当前密码不能相同");
			return;
		}
		
		showProgressDialog("修改登录密码");
		EntboostUM.changePassword(changepwd_passwd_str, changepwd_oldpasswd_str, new EditInfoListener() {
			@Override
			public void onFailure(final int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						
						if (code==EB_STATE_CODE.EB_STATE_ACC_PWD_ERROR.getValue())
							pageInfo.showError("当前密码错误");
						else
							pageInfo.showError(errMsg);
					}
				});
			}
			
			@Override
			public void onEditInfoSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						pageInfo.hide();
						
						AlertDialog dialog = showDialogOnlyConfirm("提示", "修改密码成功，请重新登录",  new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								ChangePWDActivity.this.finish();
								
								//执行用户登出
								EntboostLC.logout();
								MyApplication.getInstance().setLogin(false);
								
								MyActivityManager.getInstance().clearAllActivity();
								//跳转到登录界面
								Intent intent = new Intent(ChangePWDActivity.this, LoginActivity.class);
								startActivity(intent);
							}
						});
						
						dialog.setCancelable(false);
					}
				});
			}
		});
	}

	@OnClick(R.id.cancel)
	public void cancel(View view) {
		finish();
	}

}
