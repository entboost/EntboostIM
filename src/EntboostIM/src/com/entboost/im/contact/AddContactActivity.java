package com.entboost.im.contact;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class AddContactActivity extends EbActivity {
	@ViewInject(R.id.addc_account)
	private EditText addc_account;
	@ViewInject(R.id.addc_username)
	private EditText addc_username;
	@ViewInject(R.id.addc_desc)
	private EditText addc_desc;
	@ViewInject(R.id.addc_group)
	private EditText addc_group;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_add_contact);
		ViewUtils.inject(this);
	}

	@OnClick(R.id.addc_add_btn)
	public void save(View view) {
		String addc_account_str = addc_account.getText().toString();
		String addc_username_str = addc_username.getText().toString();
		String addc_desc_str = addc_desc.getText().toString();
		String addc_group_str = addc_group.getText().toString();
		// 空值校验
		if (StringUtils.isBlank(addc_account_str)) {
			pageInfo.showError("帐号不能为空！");
			return;
		}
		showProgressDialog("正在添加新的联系人");
//		EntboostUM.addContact(addc_account_str, addc_username_str,
//				addc_desc_str, addc_group_str, new EditContactListener() {
//
//					@Override
//					public void onFailure(String errMsg) {
//						pageInfo.showError(errMsg);
//						removeProgressDialog();
//					}
//
//					@Override
//					public void onEditContactSuccess() {
//						removeProgressDialog();
//						finish();
//					}
//				});
	}

	@OnClick(R.id.addc_cancel_btn)
	public void cancel(View view) {
		this.finish();
	}

}
