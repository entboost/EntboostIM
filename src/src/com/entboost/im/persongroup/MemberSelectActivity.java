package com.entboost.im.persongroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.EnterpriseInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.AddToPersonGroupListener;
import net.yunim.service.listener.LoadAllMemberListener;
import net.yunim.service.listener.LoadEnterpriseListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.contact.ContactAdapter;
import com.entboost.im.department.DepartmentListActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.ui.base.view.hlistview.HorizontalListView;

public class MemberSelectActivity extends EbActivity {
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

	private HorizontalListView selectedListView;

	private MemberSelectedAdapter selectedAdapter;

	private Button selectedbtn;

	private long groupid;

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

	public void refreshPage() {
		List<Object> selectedMap = MyApplication.getInstance()
				.getSelectedUserList();
		selectedAdapter.setInput(selectedMap);
		selectedAdapter.notifyDataSetChanged();
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
			text_listname.setText(ent.getEnt_name());
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

	private void initSelected() {
		selectedListView = (HorizontalListView) findViewById(R.id.selectedUser);
		selectedAdapter = new MemberSelectedAdapter(this);
		selectedListView.setAdapter(selectedAdapter);
		selectedbtn = (Button) findViewById(R.id.selectedBtn);
		selectedbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showProgressDialog("邀请成员加入群组");
				Set<Long> uids = new HashSet<Long>();
				List<Object> selectedMap = MyApplication.getInstance()
						.getSelectedUserList();
				for (Object obj : selectedMap) {
					if (obj instanceof MemberInfo) {
						uids.add(((MemberInfo) obj).getEmp_uid());
					} else if (obj instanceof ContactInfo) {
						uids.add(((ContactInfo) obj).getCon_uid());
					}
				}
				if (uids.size() == 0) {
					showToast("还未选择任何成员！");
					removeProgressDialog();
					return;
				}
				EntboostUM.addToPersonGroup(groupid, uids,
						new AddToPersonGroupListener() {

							@Override
							public void onFailure(String errMsg) {
								showToast(errMsg);
								removeProgressDialog();
								MyApplication.getInstance()
										.getSelectedUserList().clear();
							}

							@Override
							public void onSuccess() {
								removeProgressDialog();
								finish();
								MyApplication.getInstance()
										.getSelectedUserList().clear();
							}

						});
			}
		});
	}

	private void initEnt() {
		entlistView = (ExpandableListView) findViewById(R.id.entlist);
		entAdapter = new GroupAdapter<DepartmentInfo>(this,entlistView);
		entAdapter.setSelectMember(true);
		entListener = new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) entAdapter
						.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(),
						new LoadAllMemberListener() {

							@Override
							public void onFailure(String errMsg) {
								showToast(errMsg);
							}

							@Override
							public void onLoadAllMemberSuccess() {
								entAdapter.setMembers(group.getDep_code(), true);
								entAdapter.notifyDataSetChanged();
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
					if (checkSelfGroup(obj)) {
						MemberInfo memberInfo = (MemberInfo) obj;
						ImageView selectImg = (ImageView) v
								.findViewById(R.id.user_select);
						if (selectImg.getVisibility() == View.GONE) {
							return true;
						}
						List<Object> selectedMap = MyApplication.getInstance()
								.getSelectedUserList();
						Drawable srcImg = selectImg.getDrawable();
						if (srcImg == null) {
							selectImg.setImageResource(R.drawable.uitb_57);
							selectedMap.add(memberInfo);
						} else {
							selectImg.setImageDrawable(null);
							selectedMap.remove(memberInfo);
						}
						selectedAdapter.setInput(selectedMap);
						selectedAdapter.notifyDataSetChanged();
					}
				} else if (obj instanceof DepartmentInfo) {
					DepartmentInfo departmentInfo = (DepartmentInfo) obj;
					Intent intent = new Intent(MemberSelectActivity.this,
							DepartmentListActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("departmentInfo", departmentInfo);
					intent.putExtra("selecteduser", true);
					startActivity(intent);
				}
				return true;
			}
		};
		entlistView.setAdapter(entAdapter);
		entlistView.setOnChildClickListener(entChildtListener);
		entlistView.setOnGroupExpandListener(entListener);
	}

	private void initMyDepartment() {
		departmentlistView = (ExpandableListView) findViewById(R.id.departmentlist);
		departmentAdapter = new GroupAdapter<DepartmentInfo>(this,entlistView);
		departmentAdapter.setSelectMember(true);
		departmentListener = new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) departmentAdapter
						.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(),
						new LoadAllMemberListener() {

							@Override
							public void onFailure(String errMsg) {
								showToast(errMsg);
							}

							@Override
							public void onLoadAllMemberSuccess() {
								departmentAdapter.setMembers(
										group.getDep_code(), false);
								departmentAdapter.notifyDataSetChanged();
							}
						});
			}
		};
		departmenChildtListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View view,
					int groupPosition, int childPosition, long id) {
				Object obj = departmentAdapter.getChild(groupPosition,
						childPosition);
				if (checkSelfGroup(obj)) {
					MemberInfo memberInfo = (MemberInfo) obj;
					ImageView selectImg = (ImageView) view
							.findViewById(R.id.user_select);
					if (selectImg.getVisibility() == View.GONE) {
						return true;
					}
					List<Object> selectedMap = MyApplication.getInstance()
							.getSelectedUserList();
					Drawable srcImg = selectImg.getDrawable();
					if (srcImg == null) {
						selectImg.setImageResource(R.drawable.uitb_57);
						selectedMap.add(memberInfo);
					} else {
						selectImg.setImageDrawable(null);
						selectedMap.remove(memberInfo);
					}
					selectedAdapter.setInput(selectedMap);
					selectedAdapter.notifyDataSetChanged();
				}
				return true;
			}
		};
		departmentlistView.setAdapter(departmentAdapter);
		departmentlistView.setOnChildClickListener(departmenChildtListener);
		departmentlistView.setOnGroupExpandListener(departmentListener);
	}

	private void initGroup() {
		grouplistView = (ExpandableListView) findViewById(R.id.grouplist);
		groupAdapter = new GroupAdapter<PersonGroupInfo>(this,entlistView);
		groupAdapter.setSelectMember(true);
		personGroupListener = new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) groupAdapter
						.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(),
						new LoadAllMemberListener() {

							@Override
							public void onFailure(String errMsg) {
								showToast(errMsg);
							}

							@Override
							public void onLoadAllMemberSuccess() {
								groupAdapter.setMembers(group.getDep_code(),
										false);
								groupAdapter.notifyDataSetChanged();
							}
						});
			}

		};
		childListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View view,
					int groupPosition, int childPosition, long id) {
				Object obj = groupAdapter
						.getChild(groupPosition, childPosition);
				if (checkSelfGroup(obj)) {
					MemberInfo memberInfo = (MemberInfo) obj;
					ImageView selectImg = (ImageView) view
							.findViewById(R.id.user_select);
					if (selectImg.getVisibility() == View.GONE) {
						return true;
					}
					Drawable srcImg = selectImg.getDrawable();
					List<Object> selectedMap = MyApplication.getInstance()
							.getSelectedUserList();
					if (srcImg == null) {
						selectImg.setImageResource(R.drawable.uitb_57);
						selectedMap.add(memberInfo);
					} else {
						selectImg.setImageDrawable(null);
						selectedMap.remove(memberInfo);
					}
					selectedAdapter.setInput(selectedMap);
					selectedAdapter.notifyDataSetChanged();
				}
				return true;
			}
		};
		grouplistView.setAdapter(groupAdapter);
		grouplistView.setOnChildClickListener(childListener);
		grouplistView.setOnGroupExpandListener(personGroupListener);
	}

	private void initContactView() {
		listView = (ExpandableListView) findViewById(R.id.friendlist);
		friendAdapter = new ContactAdapter(this);
		friendAdapter.setSelectMember(true);
		contactListener = new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Object obj = friendAdapter.getChild(groupPosition,
						childPosition);
				if (obj instanceof ContactInfo) {
					if (checkSelfGroup(obj)) {
						ImageView selectImg = (ImageView) v
								.findViewById(R.id.user_select);
						if (selectImg.getVisibility() == View.GONE) {
							return true;
						}
						Drawable srcImg = selectImg.getDrawable();
						List<Object> selectedMap = MyApplication.getInstance()
								.getSelectedUserList();
						if (srcImg == null) {
							selectImg.setImageResource(R.drawable.uitb_57);
							selectedMap.add(obj);
						} else {
							selectImg.setImageDrawable(null);
							selectedMap.remove(obj);
						}
						selectedAdapter.setInput(selectedMap);
						selectedAdapter.notifyDataSetChanged();
					}
				}
				return true;
			}
		};
		listView.setAdapter(friendAdapter);
		listView.setOnChildClickListener(contactListener);
	}

	/**
	 * 判断群组添加的新成员是否是已有成员
	 * 
	 * @return
	 */
	private boolean checkSelfGroup(Object obj) {
		if (obj instanceof ContactInfo) {
			ContactInfo contactInfo = (ContactInfo) obj;
			if (contactInfo.getCon_uid() == null) {
				showToast("当前选中联系人不是系统用户，不能添加！");
				return false;
			} else {
				if (EntboostCache.isExistMember(groupid,
						contactInfo.getCon_uid())) {
					showToast("用户已是本群成员！");
					return false;
				}
			}
		} else if (obj instanceof MemberInfo) {
			MemberInfo memberInfo = (MemberInfo) obj;
			if (EntboostCache.isExistMember(groupid, memberInfo.getEmp_uid())) {
				showToast("用户已是本群成员！");
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_member_select);
		MyApplication.getInstance().getSelectedUserList().clear();
		groupid = getIntent().getLongExtra("groupid", -1);
		text_listname = (TextView) findViewById(R.id.text_listname);
		layout_contact = findViewById(R.id.layout_contact2);
		layout_group = findViewById(R.id.layout_group2);
		layout_department = findViewById(R.id.layout_department);
		layout_ent = findViewById(R.id.layout_ent);
		TextView text_contact = (TextView) findViewById(R.id.text_contact);
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
						pageInfo.showError("无法加载部门信息");
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
		initSelected();
		initContactView();
		initGroup();
		initMyDepartment();
		initEnt();
		selectPage = SELECTPAGE_MYDEPARTMENT;
		selectPageBtn(layout_department);
		friendAdapter.notifyDataSetChanged();
	}

}
