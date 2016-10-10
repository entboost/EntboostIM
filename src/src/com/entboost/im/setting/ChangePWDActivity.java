package com.entboost.im.setting;

import net.yunim.service.EntboostUM;
import net.yunim.service.listener.EditInfoListener;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
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
		String changepwd_oldpasswd_str = changepwd_oldpasswd.getText()
				.toString();
		String changepwd_passwd_str = changepwd_passwd.getText().toString();
		String changepwd_confirm_passwd_str = changepwd_confirm_passwd
				.getText().toString();
		if (StringUtils.isBlank(changepwd_passwd_str)
				|| StringUtils.isBlank(changepwd_confirm_passwd_str)) {
			pageInfo.showError("密码不能为空");
			return;
		} else {
			pageInfo.hide();
		}
		if (!StringUtils.equals(changepwd_passwd_str,
				changepwd_confirm_passwd_str)) {
			pageInfo.showError("两次输入密码不一致");
			return;
		} else {
			pageInfo.hide();
		}
		showProgressDialog("修改用户密码");
		EntboostUM.changePassword(changepwd_passwd_str,
				changepwd_oldpasswd_str, new EditInfoListener() {

					@Override
					public void onFailure(String errMsg) {
						pageInfo.showError(errMsg);
						removeProgressDialog();
					}

					@Override
					public void onEditInfoSuccess() {
						pageInfo.hide();
						removeProgressDialog();
						finish();
					}
				});
	}

	@OnClick(R.id.cancel)
	public void cancel(View view) {
		finish();
	}

}
