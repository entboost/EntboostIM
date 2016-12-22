package com.entboost.im.contact;

import org.apache.commons.lang3.StringUtils;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.ContactGroup;
import net.yunim.service.listener.DelGroupListener;
import net.yunim.service.listener.EditGroupListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ContactGroupActivity extends EbActivity {
	@ViewInject(R.id.contactgroup_group)
	private EditText contactgroup_group;
	private ContactGroup group;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_contact_group);
		ViewUtils.inject(this);
		group = (ContactGroup) getIntent().getSerializableExtra("contactgroup");
		if (group != null) {
			contactgroup_group.setText(group.getGroupname());
		}
	}

	@OnClick(R.id.contactgroup_save_btn)
	public void save(View view) {
		final String contactgroup_group_str = contactgroup_group.getText()
				.toString();
		if(StringUtils.isBlank(contactgroup_group_str)){
			showToast("分组名称不能为空！");
			return;
		}
		showProgressDialog("修改联系人分组");
		EntboostUM.editContactGroup(group.getUgid(), contactgroup_group_str, new EditGroupListener() {
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
			public void onEditGroupSuccess(Long dep_code) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						finish();
					}
				});
			}
		});
	}
	
	@OnClick(R.id.contactgroup_del_btn)
	public void del(View view) {
		showProgressDialog("正在删除分组！");
		EntboostUM.delContactGroup(group.getUgid(), new DelGroupListener() {
			@Override
			public void onFailure(int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						showToast(errMsg);
					}
				});
			}

			@Override
			public void onDelGroupSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						finish();
					}
				});
			}
		});
	}

	@OnClick(R.id.contactgroup_cancel_btn)
	public void cancel(View view) {
		finish();
	}

}
