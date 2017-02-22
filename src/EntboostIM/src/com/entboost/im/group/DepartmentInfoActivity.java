package com.entboost.im.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.DelGroupListener;
import net.yunim.service.listener.LoadAllMemberListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DepartmentInfoActivity extends EbActivity {

	private DepartmentInfo departmentInfo;
	private Long depid;
	private boolean isManager = false;

	@Override
	protected void onResume() {
		super.onResume();
		// 获取群组编号
		depid = getIntent().getLongExtra("depid", -1);
		// 从缓存中，根据群组编号获取群组对象
		departmentInfo = EntboostCache.getDepartment(depid);
		init();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_department_info);
		ViewUtils.inject(this);
	}

	private void init() {
		//设置部门头像
		Context context = getApplicationContext();
		Resources resources = context.getResources();
		int indentify = resources.getIdentifier(context.getPackageName()+":drawable/"+"group_head_"+departmentInfo.getType(), null, null);
		ImageView head = (ImageView) findViewById(R.id.department_head);
		head.setImageResource(indentify);
		
		// 设置部门信息
		TextView name = (TextView) findViewById(R.id.department_username);
		TextView description = (TextView) findViewById(R.id.department_description);
		TextView depId = (TextView) findViewById(R.id.department_account);
		TextView memberNum = (TextView) findViewById(R.id.department_member);
		TextView tel = (TextView) findViewById(R.id.department_tel);
		TextView fax = (TextView) findViewById(R.id.department_fax);
		TextView email = (TextView) findViewById(R.id.department_email);
		TextView com= (TextView) findViewById(R.id.department_com);
		TextView home = (TextView) findViewById(R.id.department_home);
		TextView addr = (TextView) findViewById(R.id.department_addr);
		
		View send_btn = findViewById(R.id.department_send_btn);
		final Button department_usermanager = (Button) findViewById(R.id.department_usermanager);
		final Button department_del_btn = (Button) findViewById(R.id.department_del_btn);
		
		if (departmentInfo != null) {
			memberNum.setText(departmentInfo.getEmp_count() + "");
			depId.setText(departmentInfo.getDep_code() + "");
			tel.setText(departmentInfo.getPhone());
			fax.setText(departmentInfo.getFax());
			email.setText(departmentInfo.getEmail());
			com.setText(EntboostCache.getEnterpriseInfo().getEnt_name());
			home.setText(departmentInfo.getUrl());
			addr.setText(departmentInfo.getAddress());
			name.setText(departmentInfo.getDep_name());
			description.setText(departmentInfo.getDescription());
			
			// 当前登录用户不在该群组时，不能进行群组会话
			if (departmentInfo.getMy_emp_id() == null || departmentInfo.getMy_emp_id() <= 0) {
				send_btn.setVisibility(View.GONE);
			}
//			else {
//				if (departmentInfo.getMy_emp_id()>0) {
//					EntboostUM.loadMembers(depid, new LoadAllMemberListener() {
//						@Override
//						public void onFailure(int code, String errMsg) {
//						}
//						
//						@Override
//						public void onLoadAllMemberSuccess() {
//							HandlerToolKit.runOnMainThreadAsync(new Runnable() {
//								@Override
//								public void run() {
//									MemberInfo member = EntboostCache.getMemberByCode(departmentInfo.getMy_emp_id());
//									if (member!=null) {
//										if ((member.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) {
//											department_usermanager.setVisibility(View.VISIBLE);
//											department_del_btn.setVisibility(View.VISIBLE);
//										}
//									}
//								}
//							});
//						}
//					});
//				}
//			}
			
			// 确认部门的管理员权限
			if (departmentInfo.getCreate_uid() - EntboostCache.getUid() == 0
					|| EntboostCache.getEnterpriseInfo().getCreate_uid()
							- EntboostCache.getUid() == 0) {
				isManager = true;
			}
			//只有管理员才允许修改部门资料
			if (isManager) {
				ImageView department_name_arrow = (ImageView) findViewById(R.id.department_name_arrow);
				department_name_arrow.setVisibility(View.VISIBLE);
				
				Drawable drawable = getResources().getDrawable(R.drawable.a4040);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tel.setCompoundDrawables(null, null, drawable, null);
				fax.setCompoundDrawables(null, null, drawable, null);
				email.setCompoundDrawables(null, null, drawable, null);
				home.setCompoundDrawables(null, null, drawable, null);
				addr.setCompoundDrawables(null, null, drawable, null);
			}
		}
	}

	@OnClick(R.id.department_usermanager)
	public void usermanager(View view) {
		//加载该群组的所有成员
		EntboostUM.loadMembers(depid, new LoadAllMemberListener() {
			@Override
			public void onFailure(int code, String errMsg) {
			}
			
			@Override
			public void onLoadAllMemberSuccess() {
				//在主线程执行
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(DepartmentInfoActivity.this, MemberSelectActivity.class);
						intent.putExtra("selectType", MemberSelectActivity.SELECT_TYPE_MULTI);
						intent.putExtra("groupid", depid);
						
						List<MemberInfo> memberInfos = EntboostCache.getGroupMemberInfos(depid);
						//把当前已在群组内的用户除外
						List<Long> excludeUids = new ArrayList<Long>();
						for (MemberInfo mi : memberInfos) {
							excludeUids.add(mi.getEmp_uid());
						}
						intent.putExtra("excludeUids", (Serializable)excludeUids);
						
						startActivity(intent);
					}
				});
			}
		});
	}

	@OnClick(R.id.department_del_btn)
	public void del(View view) {
		showDialog("提示", "确定要解散部门吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("正在解散部门");
				if (departmentInfo != null) {
					EntboostUM.delDepartment(departmentInfo.getDep_code(), new DelGroupListener() {
								@Override
								public void onFailure(int code, final String errMsg) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											removeProgressDialog();
											pageInfo.showError(errMsg);
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
				} else {
					removeProgressDialog();
					showToast("解散群组失败！");
				}
			}
		});

	}

	@OnClick(R.id.department_name_layout)
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
	
	@OnClick(R.id.department_tel_layout)
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
	
	@OnClick(R.id.department_fax_layout)
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
	
	@OnClick(R.id.department_email_layout)
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
	
	@OnClick(R.id.department_home_layout)
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
	
	@OnClick(R.id.department_addr_layout)
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

	@OnClick(R.id.department_send_btn)
	public void sendMsg(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.INTENT_TITLE, departmentInfo.getDep_name());
		intent.putExtra(ChatActivity.INTENT_UID, departmentInfo.getDep_code());
		intent.putExtra(ChatActivity.INTENT_CHATTYPE,
				ChatActivity.CHATTYPE_GROUP);
		this.startActivity(intent);
	}

}
