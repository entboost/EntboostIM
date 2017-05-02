package com.entboost.im.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.DelGroupListener;
import net.yunim.service.listener.EditGroupListener;
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
import com.entboost.im.comparator.MemberInfoComparator;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DepartmentInfoActivity extends EbActivity {

	private DepartmentInfo departmentInfo;
	private Long depid;
	private boolean isManager = false;
	
	@ViewInject(R.id.department_head)
	private ImageView head;
	@ViewInject(R.id.department_tel)
	private TextView tel;
	@ViewInject(R.id.department_fax)
	private TextView fax;
	@ViewInject(R.id.department_email)
	private TextView email;
	@ViewInject(R.id.department_com)
	private TextView com;
	@ViewInject(R.id.department_home)
	private TextView home;
	@ViewInject(R.id.department_addr)
	private TextView addr;
	@ViewInject(R.id.department_name_arrow)
	private ImageView department_name_arrow;
	@ViewInject(R.id.department_username)
	private TextView name;
	@ViewInject(R.id.department_description)
	private TextView description;
	@ViewInject(R.id.department_account)
	private TextView depId;
	@ViewInject(R.id.department_member)
	private TextView memberNum;
	@ViewInject(R.id.department_send_btn)
	private Button send_btn;
	@ViewInject(R.id.department_usermanager)
	private Button department_usermanager;
	@ViewInject(R.id.department_del_btn)
	private Button department_del_btn;
	@ViewInject(R.id.department_forbid_btn)
	private Button department_forbid_btn;
	
	
	@Override
	protected void onResume() {
		super.onResume();
		renderView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_department_info);
		ViewUtils.inject(this);
	}

	private void renderView() {
		// 获取群组编号
		depid = getIntent().getLongExtra("depid", -1);
		// 从缓存中，根据群组编号获取群组对象
		departmentInfo = EntboostCache.getDepartment(depid);
		
		//设置部门头像
		Context context = getApplicationContext();
		Resources resources = context.getResources();
		int indentify = resources.getIdentifier(context.getPackageName()+":drawable/"+"group_head_"+departmentInfo.getType(), null, null);
		head.setImageResource(indentify);
		
		//禁言状态
		department_forbid_btn.setTag((departmentInfo.getExt_data()&0x1) == 0x1);
		if ((Boolean)department_forbid_btn.getTag()==true) {
			department_forbid_btn.setText("解除禁言");
		} else {
			department_forbid_btn.setText("群禁言");
		}
		
		// 设置部门信息
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
			if (departmentInfo.getMy_emp_id() > 0) {
				EntboostUM.loadMembers(departmentInfo.getDep_code(), new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, String errMsg) {
					}
					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								MemberInfo myMember = EntboostCache.getMemberByCode(departmentInfo.getMy_emp_id());
								if(myMember==null){
									showToast("加载成员权限信息失败！");
									return;
								}
								
								if ((myMember.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) {
									isManager = true;
									managerConfig();
								}
							}
						});
					}
				});
			} else {
				send_btn.setVisibility(View.GONE);
			}
			
			// 确认部门的管理员权限
			if (departmentInfo.getCreate_uid() - EntboostCache.getUid() == 0
					|| EntboostCache.getEnterpriseInfo().getCreate_uid() - EntboostCache.getUid() == 0) {
				isManager = true;
			}
			
			managerConfig();
		}
	}
	
	//管理功能配置
	private void managerConfig() {
		if (isManager) {
			department_forbid_btn.setVisibility(View.VISIBLE);
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
						Collections.sort(memberInfos, new MemberInfoComparator()); //排序
						
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

	private String progressMsg = null;
	
	@OnClick(R.id.department_forbid_btn)
	public void forbidGroup(final View view) {
		if (departmentInfo != null) {
			int forbid_minutes = ((Boolean)department_forbid_btn.getTag()==true)?-1:0;
			progressMsg = forbid_minutes==-1?"正在执行解除禁言":"正在执行禁言群组";
			showProgressDialog(progressMsg);
			
			EntboostUM.forbidGroup(departmentInfo.getDep_code(), forbid_minutes, new EditGroupListener () {
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
							renderView();
						}
					});
				}
			});
		}
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
		intent.putExtra(ChatActivity.INTENT_TOID, departmentInfo.getDep_code());
		intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
		this.startActivity(intent);
	}

}
