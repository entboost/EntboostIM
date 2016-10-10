package com.entboost.im.department;

import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.LoadAllMemberListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DepartmentListActivity extends EbActivity {

	private DepartmentInfo departmentInfo;
	private ListView listView;
	private DepAndMemberAdapter adapter;
	private boolean selecteduser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_department_list);
		ViewUtils.inject(this);
		departmentInfo = (DepartmentInfo) getIntent().getSerializableExtra(
				"departmentInfo");
		selecteduser = getIntent().getBooleanExtra("selecteduser", false);
		if (departmentInfo != null) {
			AbTitleBar titleBar = this.getTitleBar();
			titleBar.setTitleText(departmentInfo.getDep_name());
			listView = (ListView) findViewById(R.id.deplist);
			adapter = new DepAndMemberAdapter(this);
			adapter.setSelectMember(selecteduser);
			showProgressDialog("正在加载下级群组和成员信息！");
			EntboostUM.loadMembers(departmentInfo.getDep_code(),
					new LoadAllMemberListener() {

						@Override
						public void onFailure(String errMsg) {
							removeProgressDialog();
						}

						@Override
						public void onLoadAllMemberSuccess() {
							removeProgressDialog();
							adapter.setMembers(departmentInfo.getDep_code());
							listView.setAdapter(adapter);
						}
					});
			if (!selecteduser) {
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Object obj = adapter.getItem(arg2);
						if (obj instanceof MemberInfo) {
							MemberInfo memberInfo = (MemberInfo) obj;
							if (memberInfo.getEmp_uid()
									- EntboostCache.getUid() == 0) {
								Intent intent = new Intent(
										DepartmentListActivity.this,
										MemberInfoActivity.class);
								if (memberInfo != null) {
									intent.putExtra("memberCode",
											memberInfo.getEmp_code());
									intent.putExtra("selfFlag", true);
									startActivity(intent);
								}
							} else {
								Intent intent = new Intent(DepartmentListActivity.this,
										ChatActivity.class);
								intent.putExtra(ChatActivity.INTENT_TITLE,
										memberInfo.getUsername());
								intent.putExtra(ChatActivity.INTENT_UID,
										memberInfo.getEmp_uid());
								startActivity(intent);
							}
						} else if (obj instanceof DepartmentInfo) {
							DepartmentInfo group = (DepartmentInfo) obj;
							if (group.getMy_emp_id() == null || group.getMy_emp_id() <= 0) {
								showToast("您不是该部门成员，不允许发起会话！");
							}else{
								Intent intent = new Intent(DepartmentListActivity.this, ChatActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_SINGLE_TOP
										| Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.putExtra(ChatActivity.INTENT_TITLE,
										group.getDep_name());
								intent.putExtra(ChatActivity.INTENT_UID,
										group.getDep_code());
								intent.putExtra(ChatActivity.INTENT_CHATTYPE,
										ChatActivity.CHATTYPE_GROUP);
								startActivity(intent);
							}
						}
					}
				});
			} else {
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int arg2, long arg3) {
						Object obj = adapter.getItem(arg2);
						if (obj instanceof MemberInfo) {
							MemberInfo memberInfo = (MemberInfo) obj;
							ImageView selectImg = (ImageView) view
									.findViewById(R.id.user_select);
							if (selectImg.getVisibility() == View.GONE) {
								return;
							}
							List<Object> selectedMap = MyApplication
									.getInstance().getSelectedUserList();
							Drawable srcImg = selectImg.getDrawable();
							if (srcImg == null) {
								selectImg.setImageResource(R.drawable.uitb_57);
								selectedMap.add(memberInfo);
							} else {
								selectImg.setImageDrawable(null);
								selectedMap.remove(memberInfo);
							}
						} else if (obj instanceof DepartmentInfo) {
							finish();
							DepartmentInfo departmentInfo = (DepartmentInfo) obj;
							Intent intent = new Intent(
									DepartmentListActivity.this,
									DepartmentListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_SINGLE_TOP
									| Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("departmentInfo", departmentInfo);
							intent.putExtra("selecteduser", true);
							startActivity(intent);
						}
					}
				});
			}
		}
	}

	@OnClick(R.id.back_top_btn)
	public void goTop(View view) {
		finish();
	}

	@OnClick(R.id.back_parent_btn)
	public void goParent(View view) {
		if (departmentInfo == null) {
			finish();
		} else {
			DepartmentInfo parent = EntboostCache.getDepartment(departmentInfo
					.getParent_code());
			if (parent == null || parent.getParent_code() == 0
					|| parent.getParent_code() == null) {
				finish();
			} else {
				finish();
				Intent intent = new Intent(view.getContext(),
						DepartmentListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("departmentInfo", parent);
				startActivity(intent);
			}
		}
	}
}
