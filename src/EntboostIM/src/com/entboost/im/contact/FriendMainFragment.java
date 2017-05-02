package com.entboost.im.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.comparator.DepartmentInfoComparator;
import com.entboost.im.comparator.PersonGroupInfoComparator;
import com.entboost.im.group.DepartmentListActivity;
import com.entboost.im.group.GroupAdapter;
import com.entboost.im.group.MemberInfoActivity;

public class FriendMainFragment extends EbFragment {
	
	private static String LONG_TAG = FriendMainFragment.class.getName();

	private ContactAdapter friendAdapter;

	private ExpandableListView contactListView;
	
	//定义值-通知刷新指定分页面
	public final static int NotifyChangeNone = 0x0;
	public final static int NotifyChangeContact =	0x1;
	public final static int NotifyChangeGroup =	0x2;
	public final static int NotifyChangeMyDepartment = 0x4;
	public final static int NotifyChangeEnt= 0x8;
	public final static int NotifyChangeAll = NotifyChangeContact|NotifyChangeGroup|NotifyChangeMyDepartment|NotifyChangeEnt;	
	
	//定义值-切换分页面
	public final static int SELECTPAGE_CONTACT = NotifyChangeContact;
	public final static int SELECTPAGE_GROUP = NotifyChangeGroup;
	public final static int SELECTPAGE_MYDEPARTMENT = NotifyChangeMyDepartment;
	public final static int SELECTPAGE_ENT = NotifyChangeEnt;
	
	private int selectPage = SELECTPAGE_MYDEPARTMENT;
	
	private View layout_contact;
	private View layout_group;
	private View layout_department;
	private View layout_ent;

	private TextView text_listname;

	private GroupAdapter<PersonGroupInfo> groupAdapter;
	private GroupAdapter<DepartmentInfo> departmentAdapter;
	private GroupAdapter<DepartmentInfo> entAdapter;

	private ExpandableListView grouplistView;
	private ExpandableListView departmentlistView;
	private ExpandableListView entlistView;
	
	/**
	 * 暂存"个人群组"已展开的父节点
	 */
	private List<Long> groupExpandedList;
	/**
	 * 暂存"我的部门"已展开的父节点
	 */
	private List<Long> departmentExpandedList;	
	/**
	 * 暂存"企业部门"已展开的父节点
	 */
	private List<Long> entExpandedList;		
	
	
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
		refreshPage(false, selectPage);
	}

	private void initPageShow() {
		layout_contact.setBackgroundResource(0);
		layout_group.setBackgroundResource(0);
		layout_department.setBackgroundResource(0);
		layout_ent.setBackgroundResource(0);
		
		contactListView.setVisibility(View.GONE);
		grouplistView.setVisibility(View.GONE);
		departmentlistView.setVisibility(View.GONE);
	}
	
	@Override
	public void refreshPage(boolean switchView, int notifyChangeWhich) {
		super.refreshPage(switchView, notifyChangeWhich);
		
		if (switchView) {
			if (selectPage == SELECTPAGE_CONTACT) {
				notifyContactChanged(switchView);
			} else if (selectPage == SELECTPAGE_GROUP) {
				notifyGroupChanged(switchView, null);
			} else if (selectPage == SELECTPAGE_MYDEPARTMENT) {
				notifyDepartmentChanged(switchView, null);
			} else if (selectPage == SELECTPAGE_ENT) {
				notifyEntChanged(switchView, null, false, false, false);
			}
		} else {
			if ((notifyChangeWhich&NotifyChangeContact) == NotifyChangeContact)
				notifyContactChanged(switchView);
			if ((notifyChangeWhich&NotifyChangeGroup) == NotifyChangeGroup)
				notifyGroupChanged(switchView, null);
			if ((notifyChangeWhich&NotifyChangeMyDepartment) == NotifyChangeMyDepartment)
				notifyDepartmentChanged(switchView, null);
			if ((notifyChangeWhich&NotifyChangeEnt) == NotifyChangeEnt)
				notifyEntChanged(switchView, null, false, false, false);
		}
	}

	/**
	 * 响应更新"企业架构"视图
	 * @param switchView 是否把该视图切换至前台
	 * @param groupid 群组编号
	 * @param removeGroup 是否删除部门
	 * @param updateGroup 是否新增、更新部门
	 * @param modifyMember 是否成员变更
	 */
	public void notifyEntChanged(boolean switchView, Long groupid, boolean removeGroup, boolean updateGroup, boolean modifyMember) {
		if (entAdapter!=null) {
			List<DepartmentInfo> rootDepInfos = EntboostCache.getRootDepartmentInfos();
			Collections.sort(rootDepInfos, new DepartmentInfoComparator()); //排序
			entAdapter.setInput(rootDepInfos);
			
			if (groupid!=null) {
				if (removeGroup) { //解散部门
					entAdapter.removeDepartmentInfoLeafNode(groupid);
				} else if (updateGroup) { //新增或更新部门
					GroupInfo group = EntboostCache.getGroup(groupid);
					//遍历根部门列表，以寻找对应的上级部门
					if (group!=null && group instanceof DepartmentInfo && group.getParent_code()!=null && group.getParent_code()>0) {
						for (DepartmentInfo depInfo : rootDepInfos) {
							if (depInfo.getDep_code()-group.getParent_code()==0) {
								int groupPosition = entAdapter.getGroupPosition(depInfo.getDep_code());
								if (groupPosition>-1 && entlistView.isGroupExpanded(groupPosition))
									entAdapter.setMembers(depInfo.getDep_code(), true);
								break;
							}
						}
					}
				} else if (modifyMember) { //新增、更新、删除成员
					int groupPosition = entAdapter.getGroupPosition(groupid);
					if (groupPosition>-1 && entlistView.isGroupExpanded(groupPosition))
						entAdapter.setMembers(groupid, true);
				} else { //其它情况
					int groupPosition = entAdapter.getGroupPosition(groupid);
					if (groupPosition>-1 && entlistView.isGroupExpanded(groupPosition))
						entAdapter.setMembers(groupid, false);
				}
			} else {
				//刷新已展开父节点的成员列表
				for (Long depCode : entExpandedList) {
					entAdapter.setMembers(depCode, true);
				}
			}
			
			entAdapter.notifyDataSetChanged();
		}
		
		if (switchView)
			entlistView.setVisibility(View.VISIBLE);
		
		EnterpriseInfo ent = EntboostCache.getEnterpriseInfo();
		if (ent != null) {
			int[] counts = EntboostCache.getEnt_online_state();
			String memberCountStr = " [" + counts[1] + "/" + counts[0] + "]";
			text_listname.setText(ent.getEnt_name()+memberCountStr);
		} else {
			text_listname.setText("企业架构");
		}
	}

	/**
	 * 响应更新"个人群组"视图
	 * @param switchView 是否把该视图切换至前台
	 * @param groupid 群组编号，用于判断是否展开该群组
	 */
	public void notifyGroupChanged(boolean switchView, Long groupid) {
		if (groupAdapter!=null) {
			List<PersonGroupInfo> groups = EntboostCache.getPersonGroups();
			Collections.sort(groups, new PersonGroupInfoComparator()); //排序
			groupAdapter.setInput(groups);
			
			if (groupid!=null) {
				int groupPosition = groupAdapter.getGroupPosition(groupid);
				if (groupPosition>-1 && grouplistView.isGroupExpanded(groupPosition))
					groupAdapter.setMembers(groupid, false);
			} else {
				//刷新已展开父节点的成员列表
				for (Long depCode : groupExpandedList) {
					groupAdapter.setMembers(depCode, false);
				}
			}
			
			groupAdapter.notifyDataSetChanged();
		}
		
		if (switchView) {
			text_listname.setText("个人群组");
			grouplistView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 响应更新"我的部门"视图
	 * @param switchView 是否把该视图切换至前台
	 * @param groupid 部门编号，用于判断是否展开该部门
	 */
	public void notifyDepartmentChanged(boolean switchView, Long groupid) {
		if (departmentAdapter!=null) {
			List<DepartmentInfo> deps = EntboostCache.getMyDepartments();
			Collections.sort(deps, new DepartmentInfoComparator()); //排序
			departmentAdapter.setInput(deps);
			
			if (groupid!=null) {
				int groupPosition = departmentAdapter.getGroupPosition(groupid);
				if (groupPosition>-1 && departmentlistView.isGroupExpanded(groupPosition))
					departmentAdapter.setMembers(groupid, false);
			} else {
				//刷新已展开父节点的成员列表
				for (Long depCode : departmentExpandedList) {
					departmentAdapter.setMembers(depCode, false);
				}
			}
			
			departmentAdapter.notifyDataSetChanged();
		}
		
		if (switchView) {
			text_listname.setText("我的部门");
			departmentlistView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 响应更新"联系人"视图
	 * @param switchView 是否把该视图切换至前台
	 */
	public void notifyContactChanged(boolean switchView) {
		if (friendAdapter!=null) {
			friendAdapter.initFriendList(EntboostCache.getContactGroups(), EntboostCache.getContactInfos());
			friendAdapter.notifyDataSetChanged();
		}
		
		if (switchView && text_listname != null) {
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
				text_listname.setText("我的好友");
			} else {
				text_listname.setText("通讯录");
			}
			
			contactListView.setVisibility(View.VISIBLE);
		}
		
	}

	//初始化“企业架构”
	private void initEnt(final View view) {
		entlistView = (ExpandableListView) view.findViewById(R.id.entlist);
		entAdapter = new GroupAdapter<DepartmentInfo>(view.getContext(), entlistView);
		entExpandedList = new ArrayList<Long>();
		
		//设置企业架构人数显示模式
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_DISABLE_STATSUB_GROUP_MEMBER) 
				!= AppAccountInfo.SYSTEM_SETTING_VALUE_DISABLE_STATSUB_GROUP_MEMBER)
			entAdapter.setCalculateSubDepartment(true);
		
		//展开父节点事件
		OnGroupExpandListener groupExpandListener = new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) entAdapter.getGroup(groupPosition);
				final Long depCode = group.getDep_code();
				entExpandedList.add(depCode);
				entAdapter.setLoading(depCode, true);
				
				//加载成员列表
				EntboostUM.loadMembers(depCode, new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								entAdapter.setLoading(depCode, false);
								activity.showToast(errMsg);							
							}
						});
					}

					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								entAdapter.setLoading(depCode, false);
								entAdapter.setMembers(group.getDep_code(), true);
								notifyEntChanged(true, null, false, false, false);
							}
						});
					}
				});
			}
		};
		
		//折叠父节点事件
		OnGroupCollapseListener groupCollapseListener = new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				final GroupInfo group = (GroupInfo) entAdapter.getGroup(groupPosition);
				entExpandedList.remove(group.getDep_code());
			}
		};
		
		//点击子节点事件
		OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Object obj = entAdapter.getChild(groupPosition, childPosition);
				if (obj instanceof MemberInfo) {
					MemberInfo memberInfo = (MemberInfo) obj;
					if (memberInfo != null) {
						if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
							Intent intent = new Intent(view.getContext(), MemberInfoActivity.class);
							intent.putExtra("selfFlag", true);
							intent.putExtra("memberCode", memberInfo.getEmp_code());
							startActivity(intent);
						} else {
							Intent intent = new Intent(view.getContext(), ChatActivity.class);
							intent.putExtra(ChatActivity.INTENT_TITLE, memberInfo.getUsername());
							intent.putExtra(ChatActivity.INTENT_TOID, memberInfo.getEmp_uid());
							startActivity(intent);
						}
					}
				} else if (obj instanceof DepartmentInfo) {
					DepartmentInfo departmentInfo = (DepartmentInfo) obj;
					Intent intent = new Intent(view.getContext(), DepartmentListActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("departmentInfo", departmentInfo);
					
					startActivity(intent);
				}
				return true;
			}
		};
		entlistView.setAdapter(entAdapter);
		entlistView.setOnGroupExpandListener(groupExpandListener);
		entlistView.setOnChildClickListener(childClickListener);
		entlistView.setOnGroupCollapseListener(groupCollapseListener);
	}

	//初始化“我的部门”
	private void initMyDepartment(final View view) {
		departmentlistView = (ExpandableListView) view.findViewById(R.id.departmentlist);
		departmentAdapter = new GroupAdapter<DepartmentInfo>(view.getContext(), departmentlistView);
		departmentExpandedList = new ArrayList<Long>();
		
		//展开父节点事件
		OnGroupExpandListener groupExpandListener = new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) departmentAdapter.getGroup(groupPosition);
				final Long depCode = group.getDep_code();
				departmentExpandedList.add(depCode);
				departmentAdapter.setLoading(depCode, true);
				
				//加载成员列表
				EntboostUM.loadMembers(depCode, new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								departmentAdapter.setLoading(depCode, false);
								activity.showToast(errMsg);
							}
						});
					}
					
					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								departmentAdapter.setLoading(depCode, false);
								departmentAdapter.setMembers(group.getDep_code(), false);
								notifyDepartmentChanged(true, null);
							}
						});
						
					}
				});
			}
		};
		
		//折叠父节点事件
		OnGroupCollapseListener groupCollapseListener = new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				final GroupInfo group = (GroupInfo) departmentAdapter.getGroup(groupPosition);
				departmentExpandedList.remove(group.getDep_code());
			}
		};
		
		//点击子节点事件
		OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Object obj = departmentAdapter.getChild(groupPosition, childPosition);
				MemberInfo memberInfo = (MemberInfo) obj;
				if (memberInfo != null) {
					if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
						Intent intent = new Intent(view.getContext(), MemberInfoActivity.class);
						intent.putExtra("selfFlag", true);
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						startActivity(intent);
					} else {
						Intent intent = new Intent(view.getContext(), ChatActivity.class);
						intent.putExtra(ChatActivity.INTENT_TITLE, memberInfo.getUsername());
						intent.putExtra(ChatActivity.INTENT_TOID, memberInfo.getEmp_uid());
						startActivity(intent);
					}
				}
				return true;
			}
		};
		departmentlistView.setAdapter(departmentAdapter);
		departmentlistView.setOnChildClickListener(childClickListener);
		departmentlistView.setOnGroupExpandListener(groupExpandListener);
		departmentlistView.setOnGroupCollapseListener(groupCollapseListener);
	}

	//初始化“个人群组”
	private void initGroup(final View view) {
		grouplistView = (ExpandableListView) view.findViewById(R.id.grouplist);
		groupAdapter = new GroupAdapter<PersonGroupInfo>(view.getContext(), grouplistView);
		groupExpandedList = new ArrayList<Long>();
		
		//展开父节点事件
		OnGroupExpandListener groupExpandListener = new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) groupAdapter.getGroup(groupPosition);
				final Long depCode = group.getDep_code();
				groupExpandedList.add(depCode);
				groupAdapter.setLoading(depCode, true);
				
				//加载成员列表
				EntboostUM.loadMembers(depCode, new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								groupAdapter.setLoading(depCode, false);
								activity.showToast(errMsg);
							}
						});
					}
					
					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								groupAdapter.setLoading(depCode, false);
								groupAdapter.setMembers(group.getDep_code(), false);
								notifyGroupChanged(true, null);
							}
						});
					}
				});
			}
		};
		
		//折叠父节点事件
		OnGroupCollapseListener groupCollapseListener = new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				final GroupInfo group = (GroupInfo) groupAdapter.getGroup(groupPosition);
				groupExpandedList.remove(group.getDep_code());
			}
		};
		
		//点击子节点事件
		OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Object obj = groupAdapter.getChild(groupPosition, childPosition);
				MemberInfo memberInfo = (MemberInfo) obj;
				if (memberInfo != null) {
					if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
						Intent intent = new Intent(view.getContext(), MemberInfoActivity.class);
						intent.putExtra("selfFlag", true);
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						startActivity(intent);
					} else {
						Intent intent = new Intent(view.getContext(), ChatActivity.class);
						intent.putExtra(ChatActivity.INTENT_TITLE, memberInfo.getUsername());
						intent.putExtra(ChatActivity.INTENT_TOID, memberInfo.getEmp_uid());
						startActivity(intent);
					}
				}
				return true;
			}
		};
		
		grouplistView.setAdapter(groupAdapter);
		grouplistView.setOnChildClickListener(childClickListener);
		grouplistView.setOnGroupExpandListener(groupExpandListener);
		grouplistView.setOnGroupCollapseListener(groupCollapseListener);
	}
	
	//初始化“我的好友”
	private void initContactView(View view) {
		contactListView = (ExpandableListView) view.findViewById(R.id.friendlist);
		friendAdapter = new ContactAdapter(view.getContext(), contactListView);
		
		//点击子节点事件
		OnChildClickListener contactListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Object obj = friendAdapter.getChild(groupPosition, childPosition);
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
					intent.putExtra(ChatActivity.INTENT_TOID, mi.getCon_uid());
					startActivity(intent);
				}
				return true;
			}
		};
		contactListView.setAdapter(friendAdapter);
		contactListView.setOnChildClickListener(contactListener);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = onCreateEbView(R.layout.fragment_friend_main, inflater, container);
		
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
				refreshPage(true, NotifyChangeNone);
			}
		});
		
		layout_group.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_GROUP;
				selectPageBtn(v);
				refreshPage(true, NotifyChangeNone);
			}
		});
		
		layout_department.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_MYDEPARTMENT;
				selectPageBtn(v);
				refreshPage(true, NotifyChangeNone);
				
				EntboostUM.loadEnterprise(new LoadEnterpriseListener() {
					@Override
					public void onFailure(int code, String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								activity.pageInfo.showError("无法加载部门信息");
							}
						});
					}
					@Override
					public void onLoadEntDepartmentSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								refreshPage(true, NotifyChangeNone);
							}
						});
					}
				});
			}
		});
		
		layout_ent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectPage = SELECTPAGE_ENT;
				selectPageBtn(v);
				refreshPage(true, NotifyChangeNone);
			}
		});
		
		initContactView(view);
		initGroup(view);
		initMyDepartment(view);
		initEnt(view);
		
		//默认切换至"我的部门"页面
		selectPage = SELECTPAGE_MYDEPARTMENT;
		selectPageBtn(layout_department);
		//refreshPage(false, NotifyChangeContact);
		//friendAdapter.notifyDataSetChanged();
		return view;
	}
}
