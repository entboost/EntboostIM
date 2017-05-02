package com.entboost.im.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.DelGroupListener;
import net.yunim.service.listener.DelMemberListener;
import net.yunim.service.listener.EditGroupListener;
import net.yunim.service.listener.LoadAllMemberListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.comparator.MemberInfoComparator;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonGroupInfoActivity extends EbActivity {
	
	private static String LONG_TAG = PersonGroupInfoActivity.class.getName();

	private PersonGroupInfo groupInfo;
	private Long depid;
	private boolean isManager;
	
	@ViewInject(R.id.persongroup_head)
	private ImageView head;
	@ViewInject(R.id.persongroup_forbid_btn)
	private Button persongroup_forbid_btn;
	@ViewInject(R.id.persongroup_username)
	private TextView name;
	@ViewInject(R.id.persongroup_description)
	private TextView description;
	@ViewInject(R.id.persongroup_account)
	private TextView depId;	
	@ViewInject(R.id.persongroup_member)
	private TextView memberNum;	
	@ViewInject(R.id.persongroup_tel)
	private TextView tel;	
	@ViewInject(R.id.persongroup_fax)
	private TextView fax;	
	@ViewInject(R.id.persongroup_email)
	private TextView email;
	@ViewInject(R.id.persongroup_home)
	private TextView home;
	@ViewInject(R.id.persongroup_addr)
	private TextView addr;
	@ViewInject(R.id.persongroup_usermanager)
	private TextView umbtn;
	@ViewInject(R.id.persongroup_send_btn)
	private Button persongroup_send_btn;
	@ViewInject(R.id.persongroup_del_btn)
	private Button persongroup_del_btn;
	@ViewInject(R.id.persongroup_out_btn)
	private Button persongroup_out_btn;
	
	@Override
	protected void onResume() {
		super.onResume();
		renderView();
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
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("depid", depid);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.persongroup_tel_layout)
	public void editGroupTel(View view) {
		if (isManager) {
			Intent intent = new Intent(this, EditGroupTelActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

	private void renderView() {
		// 获取群组编号
		depid = getIntent().getLongExtra("depid", -1);
		// 从缓存中，根据群组编号获取群组对象
		groupInfo = (PersonGroupInfo) EntboostCache.getGroup(depid);
		
		//设置部门头像
		Resources resources = getResources();
		int indentify = resources.getIdentifier(getPackageName()+":drawable/"+"group_head_"+groupInfo.getType(), null, null);
		head.setImageResource(indentify);
		
		// 设置群组信息
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
			
			//禁言状态
			persongroup_forbid_btn.setTag((groupInfo.getExt_data()&0x1) == 0x1);
			if ((Boolean)persongroup_forbid_btn.getTag()==true) {
				persongroup_forbid_btn.setText("解除禁言");
			} else {
				persongroup_forbid_btn.setText("群禁言");
			}
			
			//判断群组管理权限
			if (groupInfo.getMy_emp_id() > 0) {
				//加载登录用户在当前群组的成员信息，以获取在群组的权限
				EntboostUM.loadMembers(groupInfo.getDep_code(), new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, String errMsg) {
						Log4jLog.e(LONG_TAG, errMsg + "(" + code +")");
					}
					
					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								MemberInfo myMember = EntboostCache.getMemberByCode(groupInfo.getMy_emp_id());
								
								if(myMember==null){
									showToast("加载成员权限信息失败！");
									return;
								}
								
								//拥有管理权限
								if ((myMember.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) {
									isManager = true;
									managerConfig();
								}
							}
						});
					}
				});
			} else {
				//不在群组内，不允许发起聊天
				persongroup_send_btn.setVisibility(View.GONE);
			}
			
			if (groupInfo.getCreate_uid() - EntboostCache.getUid()==0) {
				isManager = true;
			} else {
				persongroup_out_btn.setVisibility(View.VISIBLE);
			}
			
			managerConfig();
		}
	}
	
	//管理功能配置
	private void managerConfig() {
		if (isManager) {
			umbtn.setVisibility(View.VISIBLE);
			persongroup_del_btn.setVisibility(View.VISIBLE);
			persongroup_forbid_btn.setVisibility(View.VISIBLE);
			
			ImageView persongroup_name_arrow = (ImageView) findViewById(R.id.persongroup_name_arrow);
			persongroup_name_arrow.setVisibility(View.VISIBLE);
			Drawable drawable = getResources().getDrawable(R.drawable.a4040);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			
			tel.setCompoundDrawables(null, null, drawable, null);
			fax.setCompoundDrawables(null, null, drawable, null);
			email.setCompoundDrawables(null, null, drawable, null);
			home.setCompoundDrawables(null, null, drawable, null);
			addr.setCompoundDrawables(null, null, drawable, null);
		}
	}

	@OnClick(R.id.persongroup_usermanager)
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
						Intent intent = new Intent(PersonGroupInfoActivity.this, MemberSelectActivity.class);
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

	@OnClick(R.id.persongroup_send_btn)
	public void sendMsg(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.INTENT_TITLE, groupInfo.getDep_name());
		intent.putExtra(ChatActivity.INTENT_TOID, groupInfo.getDep_code());
		intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
		this.startActivity(intent);
	}

	private String progressMsg = null;
	
	@OnClick(R.id.persongroup_forbid_btn)
	public void forbidGroup(final View view) {
		if (groupInfo != null) {
			int forbid_minutes = ((Boolean)persongroup_forbid_btn.getTag()==true)?-1:0;
			progressMsg = forbid_minutes==-1?"正在执行解除禁言":"正在执行禁言群组";
			showProgressDialog(progressMsg);
			
			EntboostUM.forbidGroup(groupInfo.getDep_code(), forbid_minutes, new EditGroupListener () {
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
	
	@OnClick(R.id.persongroup_del_btn)
	public void del(View view) {
		showDialog("提示", "确定要解散群组吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("正在解散群组");
				if (groupInfo != null) {
					EntboostUM.delPersonGroup(groupInfo.getDep_code(), new DelGroupListener() {
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

	@OnClick(R.id.persongroup_out_btn)
	public void out(View view) {
		showDialog("提示", "确定要退出群组吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("正在退出群组");
				if (groupInfo != null) {
					EntboostUM.delGroupMember(groupInfo.getMy_emp_id(), new DelMemberListener() {
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
						public void onDelMemberSuccess() {
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
					showToast("退出群组失败！");
				}
			}
		});

	}

}
