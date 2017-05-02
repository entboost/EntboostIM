package com.entboost.im.group;

import java.io.Serializable;
import java.util.ArrayList;
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

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.MainActivity;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.group.MemberSelectActivity;
import com.entboost.ui.base.activity.MyActivityManager;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DepartmentListActivity extends EbActivity {

	private DepartmentInfo departmentInfo;
	private ListView listView;
	private DepAndMemberAdapter adapter;
	private boolean selecteduser; //是否选择人员视图
	private boolean selectOne = false; //是否单选
	//除外的用户编号列表(不允许选中这些编号)
	private List<Long> excludeUids = new ArrayList<Long>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_department_list);
		ViewUtils.inject(this);
		
		//解析传入参数
		List<Long> eus = (List<Long>)getIntent().getSerializableExtra("excludeUids");
		if (eus!=null)
			excludeUids.addAll(eus);
		
		departmentInfo = (DepartmentInfo) getIntent().getSerializableExtra("departmentInfo");
		selecteduser = getIntent().getBooleanExtra("selecteduser", false);
		selectOne = getIntent().getBooleanExtra("selectedone", false);
		
		if (departmentInfo != null) {
			AbTitleBar titleBar = this.getTitleBar();
			titleBar.setTitleText(departmentInfo.getDep_name());
			listView = (ListView) findViewById(R.id.deplist);
			
			adapter = new DepAndMemberAdapter(this);
			adapter.setExcludeUids(excludeUids);
			adapter.setSelectMember(selecteduser);
			if (selectOne)
				adapter.setSelectOne(selectOne);
			
			showProgressDialog("正在加载下级群组和成员信息！");
			EntboostUM.loadMembers(departmentInfo.getDep_code(), new LoadAllMemberListener() {
				@Override
				public void onFailure(int code, String errMsg) {
					HandlerToolKit.runOnMainThreadAsync(new Runnable() {
						@Override
						public void run() {
							removeProgressDialog();
						}
					});
				}

				@Override
				public void onLoadAllMemberSuccess() {
					HandlerToolKit.runOnMainThreadAsync(new Runnable() {
						@Override
						public void run() {
							removeProgressDialog();
							adapter.setMembers(departmentInfo.getDep_code());
							listView.setAdapter(adapter);
						}
					});
				}
			});
			
			if (!selecteduser) { //非选择视图
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						Object obj = adapter.getItem(arg2);
						if (obj instanceof MemberInfo) {
							MemberInfo memberInfo = (MemberInfo) obj;
							if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
								Intent intent = new Intent(DepartmentListActivity.this, MemberInfoActivity.class);
								if (memberInfo != null) {
									intent.putExtra("memberCode", memberInfo.getEmp_code());
									intent.putExtra("selfFlag", true);
									startActivity(intent);
								}
							} else {
								Intent intent = new Intent(DepartmentListActivity.this, ChatActivity.class);
								intent.putExtra(ChatActivity.INTENT_TITLE, memberInfo.getUsername());
								intent.putExtra(ChatActivity.INTENT_TOID, memberInfo.getEmp_uid());
								startActivity(intent);
							}
						} else if (obj instanceof DepartmentInfo) {
							DepartmentInfo departmentInfo = (DepartmentInfo) obj;
							Intent intent = new Intent(DepartmentListActivity.this, DepartmentListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									//| Intent.FLAG_ACTIVITY_SINGLE_TOP
									//| Intent.FLAG_ACTIVITY_CLEAR_TOP
									);
							intent.putExtra("departmentInfo", departmentInfo);
							intent.putExtra("excludeUids", (Serializable)excludeUids);
							startActivity(intent);
						}
					}
				});
			} else { //选择视图
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
						Object obj = adapter.getItem(arg2);
						if (obj instanceof MemberInfo) {
							MemberInfo memberInfo = (MemberInfo) obj;
							if (!selectOne) { //多选视图
								ImageView selectImg = (ImageView) view.findViewById(R.id.user_select);
								if (selectImg.getVisibility() == View.GONE || selectImg.getVisibility() == View.INVISIBLE) {
									return;
								}
								
								Drawable srcImg = selectImg.getDrawable();
								if (srcImg == null) {
									selectImg.setImageResource(R.drawable.uitb_57);
									MemberSelectActivity.addSelectedMember(memberInfo);
								} else {
									selectImg.setImageDrawable(null);
									MemberSelectActivity.removeSelectedMember(memberInfo);
								}
							} else { //单选视图
								if (!excludeUids.contains(memberInfo.getEmp_uid())) {
									MemberSelectActivity.addSelectedMember(memberInfo);
									MemberSelectActivity activity = (MemberSelectActivity)MyActivityManager.getInstance().getActivity(MemberSelectActivity.class.getName());
									if (activity!=null) {
//										Intent intent = new Intent(view.getContext(), MemberSelectActivity.class);
//										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//										startActivity(intent);
										
										DepartmentListActivity.this.finish();
										activity.onClickOneMember();
									}
								}
							}
						} else if (obj instanceof DepartmentInfo) {
							finish();
							
							DepartmentInfo departmentInfo = (DepartmentInfo) obj;
							Intent intent = new Intent(DepartmentListActivity.this, DepartmentListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("departmentInfo", departmentInfo);
							intent.putExtra("selecteduser", true);
							if (selectOne)
								intent.putExtra("selectedone", true);
							intent.putExtra("excludeUids", (Serializable)excludeUids);
							startActivity(intent);
						}
					}
				});
			}
		}
	}

	@OnClick(R.id.back_top_btn)
	public void goTop(View view) {
		if (selecteduser) {
			//intent = new Intent(view.getContext(), MemberSelectActivity.class);
			finish();
		} else {
			Intent intent = new Intent(view.getContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	@OnClick(R.id.back_parent_btn)
	public void goParent(View view) {
		if (departmentInfo == null) {
			finish();
		} else {
			DepartmentInfo parent = EntboostCache.getDepartment(departmentInfo.getParent_code());
			if (parent == null || parent.getParent_code() == 0 || parent.getParent_code() == null) {
				finish();
			} else {
				finish();
				
				Intent intent = new Intent(view.getContext(), DepartmentListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("departmentInfo", parent);
				if (selecteduser)
					intent.putExtra("selecteduser", true);
				if (selectOne)
					intent.putExtra("selectedone", true);
				intent.putExtra("excludeUids", (Serializable)excludeUids);
				startActivity(intent);
			}
		}
	}
}
