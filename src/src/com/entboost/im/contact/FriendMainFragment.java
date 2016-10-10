package com.entboost.im.contact;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.EnterpriseInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.LoadAllMemberListener;
import net.yunim.service.listener.LoadEnterpriseListener;
import net.yunim.service.listener.LoadMemberListener;
import net.yunim.utils.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.department.DepartmentListActivity;
import com.entboost.im.department.MemberInfoActivity;
import com.entboost.im.persongroup.GroupAdapter;

public class FriendMainFragment extends EbFragment {

	private ContactAdapter friendAdapter;

	private ExpandableListView listView;

	private int selectPage = 0;
	private static int SELECTPAGE_CONTACT = 0;
	private static int SELECTPAGE_GROUP = 1;
	private static int SELECTPAGE_MYDEPARTMENT = 2;
	private static int SELECTPAGE_ENT = 3;

	private View layout_contact;
	private View layout_group;
	private View layout_department;
	private View layout_ent;

	private OnChildClickListener contactListener;

	private GroupAdapter<PersonGroupInfo> groupAdapter;

	private OnChildClickListener childListener;

	private TextView text_listname;

	private GroupAdapter<DepartmentInfo> departmentAdapter;

	private OnGroupExpandListener departmentListener;

	private ExpandableListView grouplistView;

	private ExpandableListView departmentlistView;

	private ExpandableListView entlistView;

	private GroupAdapter<DepartmentInfo> entAdapter;

	private OnGroupExpandListener personGroupListener;

	private OnChildClickListener departmenChildtListener;

	private OnGroupExpandListener entListener;

	private OnChildClickListener entChildtListener;

	public void selectPageBtn(View v) {
		initPageShow();
		v.setBackgroundResource(R.drawable.bottom_line_green);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshPage();
	}

	private void initPageShow() {
		layout_contact.setBackgroundResource(0);
		layout_group.setBackgroundResource(0);
		layout_department.setBackgroundResource(0);
		layout_ent.setBackgroundResource(0);
		listView.setVisibility(View.GONE);
		grouplistView.setVisibility(View.GONE);
		departmentlistView.setVisibility(View.GONE);
	}

	@Override
	public void refreshPage() {
		super.refreshPage();
		if (selectPage == SELECTPAGE_CONTACT) {
			notifyContactChanged();
		} else if (selectPage == SELECTPAGE_GROUP) {
			notifyGroupChanged();
		} else if (selectPage == SELECTPAGE_MYDEPARTMENT) {
			notifyDepartmentChanged();
		} else if (selectPage == SELECTPAGE_ENT) {
			notifyEntChanged();
		}
	}

	public void notifyEntChanged() {
		EnterpriseInfo ent = EntboostCache.getEnterpriseInfo();
		if (ent != null) {
			text_listname.setText(ent.getEnt_name()+EntboostCache.getEnt_online_state());
		} else {
			text_listname.setText("企业架构");
		}
		entAdapter.setInput(EntboostCache.getRootDepartmentInfos());
		entAdapter.notifyDataSetChanged();
		entlistView.setVisibility(View.VISIBLE);
	}

	public void notifyGroupChanged() {
		text_listname.setText("个人群组");
		groupAdapter.setInput(EntboostCache.getPersonGroups());
		groupAdapter.notifyDataSetChanged();
		grouplistView.setVisibility(View.VISIBLE);
	}

	public void notifyDepartmentChanged() {
		text_listname.setText("我的部门");
		departmentAdapter.setInput(EntboostCache.getMyDepartments());
		departmentAdapter.notifyDataSetChanged();
		departmentlistView.setVisibility(View.VISIBLE);
	}

	public void notifyContactChanged() {
		if (text_listname != null) {
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
				text_listname.setText("我的好友");
			} else {
				text_listname.setText("通讯录");
			}
			friendAdapter.initFriendList(EntboostCache.getContactGroups(), EntboostCache.getContactInfos());
			friendAdapter.notifyDataSetChanged();
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void initEnt(final View view) {
		entlistView = (ExpandableListView) view.findViewById(R.id.entlist);
		entAdapter = new GroupAdapter<DepartmentInfo>(view.getContext(),
				entlistView);
		entListener = new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) entAdapter
						.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(),
						new LoadAllMemberListener() {

							@Override
							public void onFailure(String errMsg) {
								activity.showToast(errMsg);
							}

							@Override
							public void onLoadAllMemberSuccess() {
								entAdapter.setMembers(group.getDep_code(), true);
								notifyEntChanged();
							}
						});
			}
		};
		entChildtListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Object obj = entAdapter.getChild(groupPosition, childPosition);
				if (obj instanceof MemberInfo) {
					MemberInfo memberInfo = (MemberInfo) obj;
					if (memberInfo != null) {
						if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
							Intent intent = new Intent(view.getContext(),
									MemberInfoActivity.class);
							intent.putExtra("selfFlag", true);
							intent.putExtra("memberCode",
									memberInfo.getEmp_code());
							startActivity(intent);
						} else {
							Intent intent = new Intent(view.getContext(),
									ChatActivity.class);
							intent.putExtra(ChatActivity.INTENT_TITLE,
									memberInfo.getUsername());
							intent.putExtra(ChatActivity.INTENT_UID,
									memberInfo.getEmp_uid());
							startActivity(intent);
						}
					}
				} else if (obj instanceof DepartmentInfo) {
							DepartmentInfo departmentInfo = (DepartmentInfo) obj;
							Intent intent = new Intent(view.getContext(),
									DepartmentListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_SINGLE_TOP
									| Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("departmentInfo", departmentInfo);
							startActivity(intent);
				}
				return true;
			}
		};
		entlistView.setAdapter(entAdapter);
		entlistView.setOnGroupExpandListener(entListener);
		entlistView.setOnChildClickListener(entChildtListener);
	}

	private void initMyDepartment(final View view) {
		departmentlistView = (ExpandableListView) view
				.findViewById(R.id.departmentlist);
		departmentAdapter = new GroupAdapter<DepartmentInfo>(view.getContext(),
				departmentlistView);
		departmentListener = new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) departmentAdapter
						.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(),
						new LoadAllMemberListener() {

							@Override
							public void onFailure(String errMsg) {
								activity.showToast(errMsg);
							}

							@Override
							public void onLoadAllMemberSuccess() {
								departmentAdapter.setMembers(
										group.getDep_code(), false);
								notifyDepartmentChanged();
							}
						});
			}
		};
		departmenChildtListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Object obj = departmentAdapter.getChild(groupPosition,
						childPosition);
				MemberInfo memberInfo = (MemberInfo) obj;
				if (memberInfo != null) {
					if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
						Intent intent = new Intent(view.getContext(),
								MemberInfoActivity.class);
						intent.putExtra("selfFlag", true);
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						startActivity(intent);
					} else {
						Intent intent = new Intent(view.getContext(),
								ChatActivity.class);
						intent.putExtra(ChatActivity.INTENT_TITLE,
								memberInfo.getUsername());
						intent.putExtra(ChatActivity.INTENT_UID,
								memberInfo.getEmp_uid());
						startActivity(intent);
					}
				}
				return true;
			}
		};
		departmentlistView.setAdapter(departmentAdapter);
		departmentlistView.setOnChildClickListener(departmenChildtListener);
		departmentlistView.setOnGroupExpandListener(departmentListener);
	}

	private void initGroup(final View view) {
		grouplistView = (ExpandableListView) view.findViewById(R.id.grouplist);
		groupAdapter = new GroupAdapter<PersonGroupInfo>(view.getContext(),
				grouplistView);
		personGroupListener = new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) groupAdapter
						.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(),
						new LoadAllMemberListener() {

							@Override
							public void onFailure(String errMsg) {
								activity.showToast(errMsg);
							}

							@Override
							public void onLoadAllMemberSuccess() {
								groupAdapter.setMembers(group.getDep_code(),
										false);
								notifyGroupChanged();
							}
						});
			}

		};
		childListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Object obj = groupAdapter
						.getChild(groupPosition, childPosition);
				MemberInfo memberInfo = (MemberInfo) obj;
				if (memberInfo != null) {
					if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
						Intent intent = new Intent(view.getContext(),
								MemberInfoActivity.class);
						intent.putExtra("selfFlag", true);
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						startActivity(intent);
					} else {
						Intent intent = new Intent(view.getContext(),
								ChatActivity.class);
						intent.putExtra(ChatActivity.INTENT_TITLE,
								memberInfo.getUsername());
						intent.putExtra(ChatActivity.INTENT_UID,
								memberInfo.getEmp_uid());
						startActivity(intent);
					}
				}
				return true;
			}
		};
		grouplistView.setAdapter(groupAdapter);
		grouplistView.setOnChildClickListener(childListener);
		grouplistView.setOnGroupExpandListener(personGroupListener);
	}

	private void initContactView(View view) {
		listView = (ExpandableListView) view.findViewById(R.id.friendlist);
		friendAdapter = new ContactAdapter(view.getContext());
		contactListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Object obj = friendAdapter.getChild(groupPosition,
						childPosition);
				if (obj instanceof ContactInfo) {
					ContactInfo mi = (ContactInfo) obj;
					Intent intent = new Intent(activity, ChatActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					String name = null;
					if (StringUtils.isNotBlank(mi.getName())) {
						name = mi.getName();
					} else {
						name = mi.getContact();
					}
					intent.putExtra(ChatActivity.INTENT_TITLE, name);
					intent.putExtra(ChatActivity.INTENT_UID, mi.getCon_uid());
					startActivity(intent);
				}
				return true;
			}
		};
		listView.setAdapter(friendAdapter);
		listView.setOnChildClickListener(contactListener);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = onCreateEbView(R.layout.fragment_friend_main,
				inflater, container);
		text_listname = (TextView) view.findViewById(R.id.text_listname);
		layout_contact = view.findViewById(R.id.layout_contact1);
		layout_group = view.findViewById(R.id.layout_group1);
		layout_department = view.findViewById(R.id.layout_department);
		layout_ent = view.findViewById(R.id.layout_ent);
		TextView text_contact = (TextView) view.findViewById(R.id.text_contact);
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
			text_contact.setText("我的好友");
		}
		layout_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_CONTACT;
				selectPageBtn(v);
				refreshPage();
			}
		});
		layout_group.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_GROUP;
				selectPageBtn(v);
				refreshPage();
			}
		});
		layout_department.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_MYDEPARTMENT;
				selectPageBtn(v);
				refreshPage();
				EntboostUM.loadEnterprise(new LoadEnterpriseListener() {

					@Override
					public void onFailure(String errMsg) {
						activity.pageInfo.showError("无法加载部门信息");
					}

					@Override
					public void onLoadEntDepartmentSuccess() {
						refreshPage();
					}

				});
			}
		});
		layout_ent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_ENT;
				selectPageBtn(v);
				refreshPage();
			}
		});
		initContactView(view);
		initGroup(view);
		initMyDepartment(view);
		initEnt(view);
		selectPage = SELECTPAGE_MYDEPARTMENT;
		selectPageBtn(layout_department);
		friendAdapter.notifyDataSetChanged();
		return view;
	}
}
