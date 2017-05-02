package com.entboost.im.contact;

import org.apache.commons.lang3.StringUtils;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.listener.EditContactListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ContactTextEditActivity extends EbActivity {
	@ViewInject(R.id.contactedit_text)
	private EditText contactedit_text;
	@ViewInject(R.id.contactedit_hint)
	private TextView contactedit_hint;
	
	/**
	 * 被编辑字段名的extra key
	 */
	public final static String INTENT_FIELD_NAME = "fieldName";
	/**
	 * 提示的extra key
	 */
	public final static String INTENT_HINT = "hint";
	
	private String fieldName; //被编辑字段变量名
	private String fieldDesc; //被编辑字段描述
	private String hint; //提示信息
	private ContactInfo contactInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAbContentView(R.layout.activity_contact_edit);
		ViewUtils.inject(this);
		
		//提示信息
		hint = getIntent().getStringExtra(INTENT_HINT);
		if (StringUtils.isNotBlank(hint)) {
			contactedit_hint.setText(hint);
		} else {
			contactedit_hint.setText("");
		}
		
		//获取被编辑字段
		fieldName = getIntent().getStringExtra(INTENT_FIELD_NAME);
		if (fieldName.equalsIgnoreCase("name")) {
			fieldDesc = "名称";
		} else if (fieldName.equalsIgnoreCase("tel")) {
			fieldDesc = "电话";
		} else if (fieldName.equalsIgnoreCase("phone")) {
			fieldDesc = "手机";
		} else if (fieldName.equalsIgnoreCase("email")) {
			fieldDesc = "邮箱";
		} else if (fieldName.equalsIgnoreCase("job_title")) {
			fieldDesc = "职务";
		} else if (fieldName.equalsIgnoreCase("company")) {
			fieldDesc = "公司";
		}
		
		//设置界面标题
		AbTitleBar titleBar = this.getTitleBar();
		titleBar.setTitleText("编辑" + fieldDesc);
		
		//获取已有联系人资料对象
		Long con_id =getIntent().getLongExtra("con_id", 0L);
		contactInfo = EntboostCache.getContactInfoById(con_id);
		if (contactInfo!=null) {
			String value = "";
			
			if (fieldName.equalsIgnoreCase("name")) {
				value = contactInfo.getName();
			} else if (fieldName.equalsIgnoreCase("tel")) {
				value = contactInfo.getTel();
			} else if (fieldName.equalsIgnoreCase("phone")) {
				value = contactInfo.getPhone();
			} else if (fieldName.equalsIgnoreCase("email")) {
				value = contactInfo.getEmail();
			} else if (fieldName.equalsIgnoreCase("job_title")) {
				value = contactInfo.getJob_title();
			} else if (fieldName.equalsIgnoreCase("company")) {
				value = contactInfo.getCompany();
			}
			contactedit_text.setText(value);
		}
	}

	@OnClick(R.id.contactedit_save_btn)
	public void save(View view) {
		if (contactInfo==null)
			return;
		
		String value = contactedit_text.getText().toString();
		
		showProgressDialog("保存" + fieldDesc);
		
		String contact = null;
		String name = null;
		String phone = null;
		String email = null;
		String address = null;
		String description = null;
		String company = null;
		String job_title = null;
		String fax = null;
		String tel = null;
		String url = null;
		
		if (fieldName.equalsIgnoreCase("name")) {
			name = value;
		} else if (fieldName.equalsIgnoreCase("tel")) {
			tel = value;
		} else if (fieldName.equalsIgnoreCase("phone")) {
			phone = value;
		} else if (fieldName.equalsIgnoreCase("email")) {
			email = value;
		} else if (fieldName.equalsIgnoreCase("job_title")) {
			job_title = value;
		} else if (fieldName.equalsIgnoreCase("company")) {
			company = value;
		}
		
		EntboostUM.editContact(contactInfo.getCon_id(), contact, name, phone, email, address, description, 
				company, job_title, fax, tel, url, new  EditContactListener() {
			@Override
			public void onOauthForword() {
			}

			@Override
			public void onFailure(int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						showToast(errMsg);
						removeProgressDialog();
					}
				});
			}

			@Override
			public void onEditContactSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						finish();
					}
				});
			}});
	}
	
	@OnClick(R.id.contactedit_cancel_btn)
	public void cancel(View view){
		finish();
	}

}
