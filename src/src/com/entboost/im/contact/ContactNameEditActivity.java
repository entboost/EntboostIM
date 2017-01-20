package com.entboost.im.contact;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ContactNameEditActivity extends EbActivity {
	@ViewInject(R.id.contactname_username)
	private EditText contactname_username;
	private String contact;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_contact_name_edit);
		ViewUtils.inject(this);
		contact=getIntent().getStringExtra("contact");
	}

	@OnClick(R.id.contactname_save_btn)
	public void save(View view) {
		final String sNewUserName=contactname_username.getText().toString();
		showToast("此功能建设中！");
//		showProgressDialog("修改用户名称");
//		EntboostUM.editContact(contact,null, sNewUserName, null, new  EditContactListener(){
//
//			@Override
//			public void onFailure(String errMsg) {
//				pageInfo.showError(errMsg);
//				removeProgressDialog();
//			}
//
//			@Override
//			public void onEditContactSuccess() {
//				removeProgressDialog();
//				Intent intent = new Intent();
//				intent.putExtra("username", sNewUserName);
//				setResult(RESULT_OK, intent);
//				finish();
//			}});
	}
	
	@OnClick(R.id.contactname_cancel_btn)
	public void cancel(View view){
		finish();
	}

}
