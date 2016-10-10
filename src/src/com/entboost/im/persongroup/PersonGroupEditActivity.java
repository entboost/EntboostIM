package com.entboost.im.persongroup;

import net.yunim.service.EntboostUM;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.EditGroupListener;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonGroupEditActivity extends EbActivity {
	@ViewInject(R.id.pgroup_name)
	private TextView pgroup_name;
	@ViewInject(R.id.pgroup_desc)
	private TextView pgroup_desc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_person_group_edit);
		ViewUtils.inject(this);
	}
	
	@OnClick(R.id.pgroup_add_btn)
	public void save(View view) {
		String pgroup_name_str = pgroup_name.getText().toString();
		String pgroup_desc_str = pgroup_desc.getText().toString();
		// 空值校验
		if (StringUtils.isBlank(pgroup_name_str)) {
			pageInfo.showError("群组名称不能为空！");
			return;
		}
		showProgressDialog("正在添加新的个人群组");
		PersonGroupInfo group =new PersonGroupInfo();
		group.setDep_name(pgroup_name_str);
		group.setDescription(pgroup_desc_str);
		EntboostUM.addPersonGroup(group, new EditGroupListener() {
			
			@Override
			public void onFailure(String errMsg) {
				pageInfo.showError(errMsg);
				removeProgressDialog();
			}
			
			@Override
			public void onEditGroupSuccess(Long dep_code) {
				removeProgressDialog();
				finish();
			}
		});
	}

	@OnClick(R.id.pgroup_cancel_btn)
	public void cancel(View view) {
		this.finish();
	}


}
