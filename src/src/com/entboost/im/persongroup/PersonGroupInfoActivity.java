package com.entboost.im.persongroup;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.DelGroupListener;
import net.yunim.service.listener.DelMemberListener;
import net.yunim.service.listener.LoadMemberListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.department.EditGroupAddrActivity;
import com.entboost.im.department.EditGroupEmailActivity;
import com.entboost.im.department.EditGroupFaxActivity;
import com.entboost.im.department.EditGroupHomeActivity;
import com.entboost.im.department.EditGroupNameActivity;
import com.entboost.im.department.EditGroupTelActivity;
import com.entboost.im.department.MemberListActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonGroupInfoActivity extends EbActivity {

	private PersonGroupInfo groupInfo;
	private Long depid;
	private boolean isManager;

	@Override
	protected void onResume() {
		super.onResume();
		// 获取群组编号
		depid = getIntent().getLongExtra("depid", -1);
		// 从缓存中，根据群组编号获取群组对象
		groupInfo = (PersonGroupInfo) EntboostCache.getGroup(depid);
		init();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_persongroup_info);
		ViewUtils.inject(this);
	}

	@OnClick(R.id.persongroup_name_layout)
	public void editGroupName(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupNameActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.persongroup_tel_layout)
	public void editGroupTel(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupTelActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.persongroup_fax_layout)
	public void editGroupFax(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupFaxActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.persongroup_email_layout)
	public void editGroupEmail(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupEmailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.persongroup_home_layout)
	public void editGroupHome(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupHomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.persongroup_addr_layout)
	public void editGroupAddr(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupAddrActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	private void init() {
		// 设置群组信息
		TextView name = (TextView) findViewById(R.id.persongroup_username);
		TextView description = (TextView) findViewById(R.id.persongroup_description);
		TextView depId = (TextView) findViewById(R.id.persongroup_account);
		TextView memberNum = (TextView) findViewById(R.id.persongroup_member);
		final TextView tel = (TextView) findViewById(R.id.persongroup_tel);
		final TextView fax = (TextView) findViewById(R.id.persongroup_fax);
		final TextView email = (TextView) findViewById(R.id.persongroup_email);
		final TextView home = (TextView) findViewById(R.id.persongroup_home);
		final TextView addr = (TextView) findViewById(R.id.persongroup_addr);
		final Button umbtn = (Button) findViewById(R.id.persongroup_usermanager);
		final Button persongroup_del_btn = (Button) findViewById(R.id.persongroup_del_btn);
		final Button persongroup_out_btn = (Button) findViewById(R.id.persongroup_out_btn);
		if (groupInfo != null) {
			memberNum.setText(groupInfo.getEmp_count() + "");
			depId.setText(groupInfo.getDep_code() + "");
			name.setText(groupInfo.getDep_name());
			tel.setText(groupInfo.getPhone());
			fax.setText(groupInfo.getFax());
			email.setText(groupInfo.getEmail());
			home.setText(groupInfo.getUrl());
			addr.setText(groupInfo.getAddress());
			description.setText(groupInfo.getDescription());
			// 拥有群组管理权限
			if (groupInfo.getMy_emp_id() != null
					|| groupInfo.getMy_emp_id() > 0) {
				// 首先需要加载登录用户在当前群组的成员信息，获取在群组的权限
				EntboostUM.loadMemberByCode(groupInfo.getMy_emp_id(),
						new LoadMemberListener() {

							@Override
							public void onFailure(String errMsg) {
							}

							@Override
							public void onLoadMemberSuccess(MemberInfo member) {
								if(member==null){
									showToast("加载成员权限信息失败！");
									return;
								}
								if ((member.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN
										.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN
										.getValue()) {
									umbtn.setVisibility(View.VISIBLE);
									persongroup_del_btn
											.setVisibility(View.VISIBLE);
									// 只有管理员才允许修改部门资料
									isManager = true;
									ImageView persongroup_name_arrow = (ImageView) findViewById(R.id.persongroup_name_arrow);
									persongroup_name_arrow
											.setVisibility(View.VISIBLE);
									Drawable drawable = getResources()
											.getDrawable(R.drawable.a4040);
									drawable.setBounds(0, 0,
											drawable.getMinimumWidth(),
											drawable.getMinimumHeight());
									tel.setCompoundDrawables(null, null,
											drawable, null);
									fax.setCompoundDrawables(null, null,
											drawable, null);
									email.setCompoundDrawables(null, null,
											drawable, null);
									home.setCompoundDrawables(null, null,
											drawable, null);
									addr.setCompoundDrawables(null, null,
											drawable, null);
								} else {
									persongroup_out_btn
											.setVisibility(View.VISIBLE);
								}
							}
						});
			}
		}
	}

	@OnClick(R.id.persongroup_usermanager)
	public void usermanager(View view) {
		Intent intent = new Intent(this, MemberSelectActivity.class);
		intent.putExtra("groupid", depid);
		startActivity(intent);
	}

	@OnClick(R.id.persongroup_send_btn)
	public void sendMsg(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.INTENT_TITLE, groupInfo.getDep_name());
		intent.putExtra(ChatActivity.INTENT_UID, groupInfo.getDep_code());
		intent.putExtra(ChatActivity.INTENT_CHATTYPE,
				ChatActivity.CHATTYPE_GROUP);
		this.startActivity(intent);
	}

	@OnClick(R.id.persongroup_del_btn)
	public void del(View view) {
		showDialog("提示", "确定要解散群组吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("正在解散群组");
				if (groupInfo != null) {
					EntboostUM.delPersonGroup(groupInfo.getDep_code(),
							new DelGroupListener() {

								@Override
								public void onFailure(String errMsg) {
									removeProgressDialog();
									pageInfo.showError(errMsg);
								}

								@Override
								public void onDelGroupSuccess() {
									removeProgressDialog();
									finish();
								}
							});
				} else {
					removeProgressDialog();
					showToast("解散群组失败！");
				}
			}
		});

	}

	@OnClick(R.id.persongroup_out_btn)
	public void out(View view) {
		showDialog("提示", "确定要退出群组吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("正在退出群组");
				if (groupInfo != null) {
					EntboostUM.delGroupMember(groupInfo.getMy_emp_id(),
							new DelMemberListener() {

								@Override
								public void onFailure(String errMsg) {
									pageInfo.showError(errMsg);
									removeProgressDialog();
								}

								@Override
								public void onDelMemberSuccess() {
									removeProgressDialog();
									finish();
								}

							});
				} else {
					removeProgressDialog();
					showToast("退出群组失败！");
				}
			}
		});

	}

}
