package com.entboost.im.department;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.DelMemberListener;
import net.yunim.service.listener.EditContactListener;
import net.yunim.service.listener.EditInfoListener;
import net.yunim.utils.ResourceUtils;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class MemberInfoActivity extends EbActivity {

	private MemberInfo memberInfo;
	@ViewInject(R.id.member_username)
	private TextView member_username;
	@ViewInject(R.id.member_account)
	private TextView member_account;
	@ViewInject(R.id.member_id)
	private TextView member_id;
	@ViewInject(R.id.member_head)
	private ImageView member_head;
	@ViewInject(R.id.member_phone)
	private TextView member_phone;
	@ViewInject(R.id.member_tel)
	private TextView member_tel;
	@ViewInject(R.id.member_email)
	private TextView member_email;
	@ViewInject(R.id.member_dep)
	private TextView member_dep;
	@ViewInject(R.id.member_dep_lab)
	private TextView member_dep_lab;
	@ViewInject(R.id.member_job)
	private TextView member_job;
	@ViewInject(R.id.member_job_layout)
	private RelativeLayout member_job_layout;
	@ViewInject(R.id.member_send_btn)
	private Button member_send_btn;
	@ViewInject(R.id.member_set_default)
	private Button member_set_default;
	@ViewInject(R.id.member_del)
	private Button member_del;
	@ViewInject(R.id.member_add_friend)
	private Button member_add_friend;
	@ViewInject(R.id.member_add_contact)
	private Button member_add_contact;
	private long memberCode;

	private boolean selfFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_member_info);
		ViewUtils.inject(this);
		// 获取成员编号
		memberCode = getIntent().getLongExtra("memberCode", -1);
		// 获取群组成员是否为当前登录用户
		selfFlag = getIntent().getBooleanExtra("selfFlag", false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 1、首先根据传入的成员编号获取群组成员对象
		memberInfo = EntboostCache.getMemberByCode(memberCode);
		// 2、如果根据传入的成员编号获取群组成员对象为空，那么直接从传入的数据中获取群组成员
		if (memberInfo == null) {
			memberInfo = (MemberInfo) getIntent().getSerializableExtra(
					"memberInfo");
		}
		if (memberInfo != null) {
			// 3、设置成员信息
			member_account.setText(memberInfo.getEmp_account());
			member_username.setText(memberInfo.getUsername());
			member_id.setText(memberInfo.getEmp_uid() + "");
			member_phone.setText(memberInfo.getWork_phone());
			member_tel.setText(memberInfo.getCell_phone());
			member_email.setText(memberInfo.getEmail());
			GroupInfo group = EntboostCache.getGroup(memberInfo.getDep_code());
			if (group != null) {
				member_dep.setText(group.getDep_name());
			}
			member_job.setText(memberInfo.getJob_title());
			// 3-1、设置成员头像，如果没有则设置为默认头像
			Bitmap img = ResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
			if (img != null) {
				member_head.setImageBitmap(img);
			} else {
				member_head.setImageResource(R.drawable.entboost_logo);
			}
			// 3-2、判断成员所在的群组是个人群组或部门，区别展示不同的标题，职位是否显示等
			AbTitleBar titleBar = this.getTitleBar();
			if (group instanceof PersonGroupInfo) {
				titleBar.setTitleText("群组成员信息");
				member_job_layout.setVisibility(View.GONE);
				member_dep_lab.setText("群组");
			} else if (group instanceof DepartmentInfo) {
				titleBar.setTitleText("部门成员信息");
				member_dep_lab.setText("部门");
				member_job_layout.setVisibility(View.VISIBLE);
			}
			if (selfFlag) {
				// 3-3、如果成员是当前登录用户，隐藏发送消息、添加好友的按钮，显示设置默认名片的按钮，显示不同的退出按钮文字
				member_set_default.setVisibility(View.VISIBLE);
				member_del.setVisibility(View.VISIBLE);
				if (group instanceof PersonGroupInfo) {
					member_del.setText("退出群组");
				} else if (group instanceof DepartmentInfo) {
					member_del.setText("退出部门");
				}
			} else {
				// 3-4、如果成员不是当前登录用户，显示发送消息、添加好友的按钮，隐藏设置默认名片的按钮，显示不同的退出按钮文字
				member_send_btn.setVisibility(View.VISIBLE);
				member_del.setVisibility(View.VISIBLE);
				member_del.setText("移除成员");
				AppAccountInfo appInfo = EntboostCache.getAppInfo();
				if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
					member_add_friend.setVisibility(View.VISIBLE);
				} else {
					member_add_contact.setVisibility(View.VISIBLE);
				}
				// 3-5、如果成员已经是好友，则隐藏添加好友的按钮
				if (EntboostUM.isContactMember(memberInfo)) {
					member_add_contact.setVisibility(View.GONE);
					member_add_friend.setVisibility(View.GONE);
				}
			}
		}
	}

	@OnClick(R.id.member_head)
	public void openInfoHeader(View view) {
		// 如果成员是当前登录用户，则可以修改头像，打开设置头像的界面
		if (selfFlag) {
			Intent intent = new Intent(this, SelectHeadImgActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("memberCode", memberCode);
			this.startActivity(intent);
		}
	}

	@OnClick(R.id.member_add_friend)
	public void addFriend(View view) {
		if (memberInfo != null) {
			showProgressDialog("正在加为好友！");
			EntboostUM.addContact(memberInfo, memberInfo.getDescription(),
					new EditContactListener() {

						@Override
						public void onFailure(String errMsg) {
							showToast(errMsg);
							removeProgressDialog();
						}

						@Override
						public void onEditContactSuccess() {
							removeProgressDialog();
							finish();
						}
					});
		}
	}

	@OnClick(R.id.member_add_contact)
	public void addContact(View view) {
		if (memberInfo != null) {
			final EditText input = new EditText(this);
			showDialog("邀请好友", input, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String value = input.getText().toString();
					showProgressDialog("正在加为好友！");
					EntboostUM.addContact(memberInfo, value,
							new EditContactListener() {

								@Override
								public void onFailure(String errMsg) {
									showToast(errMsg);
									removeProgressDialog();
								}

								@Override
								public void onEditContactSuccess() {
									removeProgressDialog();
									finish();
								}
							});
				}
			});
		}
	}

	private String progressMsg = null;

	@OnClick(R.id.member_del)
	public void delMember(View view) {
		if (memberInfo != null) {
			// 根据所在群组的情况来设置不同的提示信息
			String msg = null;
			GroupInfo group = EntboostCache.getGroup(memberInfo.getDep_code());
			if (selfFlag) {
				if (group instanceof PersonGroupInfo) {
					msg = "是否退出群组";
					progressMsg = "正在退出群组";
				} else if (group instanceof DepartmentInfo) {
					msg = "是否退出部门";
					progressMsg = "正在退出部门";
				}
			} else {
				msg = "是否移除成员" + memberInfo.getEmp_account();
				progressMsg = "正在移除成员";
			}
			showDialog("提示", msg, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					showProgressDialog(progressMsg);
					EntboostUM.delGroupMember(memberInfo.getEmp_code(),
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
				}
			});

		}
	}

	@OnClick(R.id.member_send_btn)
	public void sendMsg(View view) {
		// 打开与成员的会话界面
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.INTENT_TITLE, memberInfo.getUsername());
		intent.putExtra(ChatActivity.INTENT_UID, memberInfo.getEmp_uid());
		this.startActivity(intent);
	}

	@OnClick(R.id.member_set_default)
	public void sendDefault(View view) {
		showProgressDialog("设置默认名片");
		EntboostUM.setMyDefaultMemberCode(memberCode, new EditInfoListener() {

			@Override
			public void onFailure(String errMsg) {
				pageInfo.showError(errMsg);
				removeProgressDialog();
			}

			@Override
			public void onEditInfoSuccess() {
				removeProgressDialog();
				finish();
			}

		});
	}

}
