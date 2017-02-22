package com.entboost.im.contact;

import net.yunim.service.EntboostUM;
import net.yunim.service.listener.EditContactListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ContactDescriptionActivity extends EbActivity {
	@ViewInject(R.id.contactdes_description)
	private EditText contactdes_description;
	private String contact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_contact_description);
		ViewUtils.inject(this);
		contact = getIntent().getStringExtra("contact");
	}

	@OnClick(R.id.contactdes_save_btn)
	public void save(View view) {
		final String sNewDescription = contactdes_description.getText().toString();
		showToast("此功能建设中！");
//		showProgressDialog("修改备注信息");
//		EntboostUM.editContact(contact,null, null, sNewDescription,
//				new EditContactListener() {
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
//						Intent intent = new Intent();
//						intent.putExtra("description", sNewDescription);
//						setResult(RESULT_OK, intent);
//						finish();
//					}
//				});
	}

	@OnClick(R.id.contactdes_cancel_btn)
	public void cancel(View view) {
		finish();
	}

}
