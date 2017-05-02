package com.entboost.im.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import net.yunim.service.listener.AddToGroupListener;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.comparator.DepartmentInfoComparator;
import com.entboost.im.comparator.PersonGroupInfoComparator;
import com.entboost.im.contact.ContactAdapter;
import com.entboost.im.contact.FriendMainFragment;
import com.entboost.ui.base.view.hlistview.HorizontalListView;

public class MemberSelectActivity extends EbActivity implements SelectedMemberListener, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4330786736506677433L;

	private static String LONG_TAG = MemberSelectActivity.class.getName();
	
	private ContactAdapter friendAdapter;

	private static int SELECTPAGE_CONTACT = FriendMainFragment.SELECTPAGE_CONTACT;
	private static int SELECTPAGE_GROUP = FriendMainFragment.SELECTPAGE_GROUP;
	private static int SELECTPAGE_MYDEPARTMENT = FriendMainFragment.SELECTPAGE_MYDEPARTMENT;
	private static int SELECTPAGE_ENT = FriendMainFragment.SELECTPAGE_ENT;
	
	private int selectPage = SELECTPAGE_MYDEPARTMENT;

	private View layout_contact;
	private View layout_group;
	private View layout_department;
	private View layout_ent;

	private LinearLayout selected_area;
	private TextView text_listname;
	
	private OnChildClickListener contactListener;
	private ExpandableListView contactlistView;

	private GroupAdapter<PersonGroupInfo> groupAdapter;
	private ExpandableListView grouplistView;
	
	private GroupAdapter<DepartmentInfo> departmentAdapter;
	private ExpandableListView departmentlistView;

	private GroupAdapter<DepartmentInfo> entAdapter;
	private ExpandableListView entlistView;
	
	private OnGroupExpandListener personGroupListener;
	private OnGroupExpandListener departmentListener;
	private OnChildClickListener childListener;
	private OnChildClickListener departmenChildtListener;

	private HorizontalListView selectedListView;

	private MemberSelectedAdapter selectedAdapter;
	private TextView selected_count;
	private Button selectedbtn;
	
	//人员选取类型
	private int selectType = SELECT_TYPE_MULTI;
	
	public final static int SELECT_TYPE_MULTI = 0; //选取多个成员
	public final static int SELECT_TYPE_SINGLE = 1; //选取一个成员
	
	//除外的用户编号列表(不允许选中这些编号)
	private List<Long> excludeUids = new ArrayList<Long>();
	
	private long groupid;

	private OnGroupExpandListener entListener;

	private OnChildClickListener entChildtListener;

	//用于临时保存已选中的成员
	private static Map<Long, List<MemberInfo>> selectedMembersMap = new HashMap<Long, List<MemberInfo>>();
	//用于临时保存已选中的联系人
	private static List<ContactInfo> selectedContactInfos = new ArrayList<ContactInfo>();
	//用于临时保存已选中的组(或部门)，仅用于标记，不作为提交保存的依据
	private static Map<Long, GroupInfo> selectedGroups = new HashMap<Long, GroupInfo>();
	
	/**
	 * 清空保存"已选中成员和联系人"的缓存
	 */
	public static void clearSelectedCache() {
		selectedGroups.clear();
		selectedMembersMap.clear();
		selectedContactInfos.clear();
	}
	
	/**
	 * 在缓存添加一个已选中组
	 * @param group
	 */
	public static void addSelectedGroup(GroupInfo group) {
		selectedGroups.put(group.getDep_code(), group);
	}
	/**
	 * 在缓存删除一个已选中组
	 * @param depCode
	 */
	public static void removeSelectedGroup(Long depCode) {
		selectedGroups.remove(depCode);
	}
	/**
	 * 在缓存判断一个组是否已被选中
	 * @param depCode
	 * @return
	 */
	public static boolean isSelectedGroup(Long depCode) {
		return selectedGroups.get(depCode)!=null?true:false;
	}
	
	/**
	 * 获取已选中成员缓存列表，如不存在则根据条件进行创建
	 * @param depCode 部门或群组编号
	 * @param create 如不存在，是否创建
	 * @return
	 */
	private static List<MemberInfo> getOrCreateSelectedMemberInfoList(Long depCode, boolean create) {
		List<MemberInfo> memberInfos = selectedMembersMap.get(depCode);
		if (memberInfos==null) {
			if (!create){
				return null;
			}
			memberInfos = new ArrayList<MemberInfo>();
			selectedMembersMap.put(depCode, memberInfos);
		}
		return memberInfos;
	}
	
	/**
	 * 在缓存添加已选中成员
	 * @param memberInfo 成员
	 */
	public static void addSelectedMember(MemberInfo memberInfo) {
		List<MemberInfo> memberInfos = getOrCreateSelectedMemberInfoList(memberInfo.getDep_code(), true);
		
		//遍历匹配之前已添加过该成员
		for (MemberInfo mi : memberInfos) {
			//判断重复记录
			if (mi.getEmp_code()-memberInfo.getEmp_code()==0) {
				Log4jLog.d(LONG_TAG, "duplicate selected member");
				return;
			}
		}
		
		memberInfos.add(memberInfo);
	}
	
	/**
	 * 在缓存添加一组已选中成员
	 * @param depCode
	 * @param newMemberInfos
	 */
	public static void addSelectedMembers(Long depCode, List<MemberInfo> newMemberInfos) {
		List<MemberInfo> memberInfos = getOrCreateSelectedMemberInfoList(depCode, true);
		
		//遍历去除重复成员
		for (MemberInfo newM : newMemberInfos) {
			for (MemberInfo oldM : memberInfos) {
				if (newM.getEmp_code()-oldM.getEmp_code()==0) {
					continue;
				}
			}
			memberInfos.add(newM);
		}
	}
	
	/**
	 * 在缓存去除整组已选中成员
	 * @param depCode
	 */
	public static void removeSelectedMembers(Long depCode) {
		selectedMembersMap.remove(depCode);
	}
	
	/**
	 * 在缓存删除已选中成员
	 * @param memberInfo
	 */
	public static void removeSelectedMember(MemberInfo memberInfo) {
		List<MemberInfo> memberInfos = getOrCreateSelectedMemberInfoList(memberInfo.getDep_code(), false);
		if (memberInfos!=null) {
			for (int i=0; i<memberInfos.size(); i++) {
				MemberInfo mi = memberInfos.get(i);
				if (mi.getEmp_code()-memberInfo.getEmp_code()==0) {
					memberInfos.remove(i);
					return;
				}
			}
		}
	}
	
	/**
	 * 获取一个部门或群组已选中成员的列表
	 * @param depCode 部门或群组编号
	 * @return
	 */
	public static List<MemberInfo> getSelectedMembers(Long depCode) {
		return getOrCreateSelectedMemberInfoList(depCode, false);
	}
	
	/**
	 * 获取全部已选中成员的列表
	 * @return
	 */
	public static List<MemberInfo> getAllSelectedMembers() {
		List<MemberInfo> results = new ArrayList<MemberInfo>();
		for (List<MemberInfo> memberInfos : selectedMembersMap.values()) {
			results.addAll(memberInfos);
		}
		return results;
	}
	
	/**
	 * 在缓存添加一个已选中的联系人
	 * @param contactInfo 联系人
	 */
	public static void addSelectedContact(ContactInfo contactInfo) {
		//遍历匹配之前已添加过该联系人
		for (ContactInfo ci : selectedContactInfos) {
			//判断重复记录
			if (ci.getCon_id()-contactInfo.getCon_id()==0) {
				Log4jLog.d(LONG_TAG, "duplicate selected contact");
				return;
			}
		}
		selectedContactInfos.add(contactInfo);
	}
	
	/**
	 * 在缓存删除已选中联系人
	 * @param memberInfo
	 */
	public static void removeSelectedContact(ContactInfo contactInfo) {
		for (int i=0; i<selectedContactInfos.size(); i++) {
			ContactInfo ci = selectedContactInfos.get(i);
			if (ci.getCon_id()-contactInfo.getCon_id()==0) {
				selectedContactInfos.remove(i);
				return;
			}
		}
	}
	
	/**
	 * 获取已选中联系人的列表
	 * @return
	 */
	public static List<ContactInfo> getSelectedContacts() {
		return selectedContactInfos;
	}
	
	/**
	 * 判断联系人是否已被选中
	 * @param contactInfo
	 * @return
	 */
	public static boolean isContactSelected(ContactInfo contactInfo) {
		for (ContactInfo ci : selectedContactInfos) {
			if (ci.getCon_id() - contactInfo.getCon_id()==0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断成员是否已被选中
	 * @param memberInfo 成员
	 * @return
	 */
	public static boolean isMemberSelected(MemberInfo memberInfo) {
		List<MemberInfo> memberInfos = getOrCreateSelectedMemberInfoList(memberInfo.getDep_code(), false);
		if (memberInfos!=null) {
			for (int i=0; i<memberInfos.size(); i++) {
				MemberInfo mi = memberInfos.get(i);
				if (mi.getEmp_code()-memberInfo.getEmp_code()==0) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获取已选中的成员和联系人的列表
	 * @return
	 */
	public static List<Object> getSelectedMembersAndContacts() {
		List<Object> selectedList = new ArrayList<Object>();
		selectedList.addAll(MemberSelectActivity.getAllSelectedMembers());
		selectedList.addAll(MemberSelectActivity.getSelectedContacts());
		return selectedList;
	}
	
	//设置"确认"按钮
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
		
		contactlistView.setVisibility(View.GONE);
		grouplistView.setVisibility(View.GONE);
		departmentlistView.setVisibility(View.GONE);
	}

	public void refreshPage() {
		this.onSelectedMembersChange();
		
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
		
		List<DepartmentInfo> rootDepInfos = EntboostCache.getRootDepartmentInfos();
		Collections.sort(rootDepInfos, new DepartmentInfoComparator()); //排序
		entAdapter.setInput(rootDepInfos);
		
		entAdapter.notifyDataSetChanged();
		entlistView.setVisibility(View.VISIBLE);
	}

	public void notifyGroupChanged() {
		text_listname.setText("个人群组");
		
		List<PersonGroupInfo> groups = EntboostCache.getPersonGroups();
		Collections.sort(groups, new PersonGroupInfoComparator()); //排序
		groupAdapter.setInput(groups);
		
		groupAdapter.notifyDataSetChanged();
		grouplistView.setVisibility(View.VISIBLE);
	}

	public void notifyDepartmentChanged() {
		text_listname.setText("我的部门");
		
		List<DepartmentInfo> deps = EntboostCache.getMyDepartments();
		Collections.sort(deps, new DepartmentInfoComparator()); //排序
		departmentAdapter.setInput(deps);
		
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
			contactlistView.setVisibility(View.VISIBLE);
		}
	}

	private void initSelected() {
		if (selectType==SELECT_TYPE_MULTI) {
			selected_area.setVisibility(View.VISIBLE);
		} else {
			selected_area.setVisibility(View.GONE);
		}
		
		selectedListView = (HorizontalListView) findViewById(R.id.selectedUser);
		selectedAdapter = new MemberSelectedAdapter(this);
		selectedListView.setAdapter(selectedAdapter);
		selectedbtn = (Button) findViewById(R.id.selectedBtn);
		
		//处理点击"确定"事件
		selectedbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Set<Long> uids = new HashSet<Long>();
				//List<Object> selectedMap = MyApplication.getInstance().getSelectedUserList();
				List<Object> selectedList = MemberSelectActivity.getSelectedMembersAndContacts();
				
				//检查选中情况
				if (selectedList.size() == 0) {
					showToast("还未选择任何成员！");
					return;
				}
				//单次最多邀请20人
				if (selectedList.size() > 30) {
					showToast("单次最多邀请30个成员");
					return;
				}
				
				showProgressDialog("邀请成员加入群组");
				
				for (Object obj : selectedList) {
					if (obj instanceof MemberInfo) {
						uids.add(((MemberInfo) obj).getEmp_uid());
					} else if (obj instanceof ContactInfo) {
						uids.add(((ContactInfo) obj).getCon_uid());
					}
				}
				
				//执行邀请成员任务
				EntboostUM.addToPersonGroup(groupid, uids, new AddToGroupListener() {
					@Override
					public void onSuccess(Long depCode, Long uid, String account) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
								finish();
								//MyApplication.getInstance().getSelectedUserList().clear();
								MemberSelectActivity.clearSelectedCache();
							}
						});
					}

					@Override
					public void onFailure(int code, final String errMsg, Long depCode, Long uid, String account) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								//showToast(errMsg);
								removeProgressDialog();
								//MyApplication.getInstance().getSelectedUserList().clear();
							}
						});
					}
					
					@Override
					public void onFailure(int code, final String errMsg) {
						//do nothing
					}
				});
			}
		});
	}

	//初始化"企业架构"
	private void initEnt() {
		entlistView = (ExpandableListView) findViewById(R.id.entlist);
		entAdapter = new GroupAdapter<DepartmentInfo>(this, entlistView);
		
		entAdapter.setExcludeUids(excludeUids);
		entAdapter.setSelectMember(true);
		if (selectType==SELECT_TYPE_SINGLE)
			entAdapter.setSelectOne(true);
		entAdapter.setSelectedMemberListener(this);
		
		//设置企业架构人数显示模式
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_DISABLE_STATSUB_GROUP_MEMBER) 
				!= AppAccountInfo.SYSTEM_SETTING_VALUE_DISABLE_STATSUB_GROUP_MEMBER)
			entAdapter.setCalculateSubDepartment(true);
		
		//展开事件
		entListener = new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) entAdapter.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(), new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								showToast(errMsg);
							}
						});
					}
					
					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								entAdapter.setMembers(group.getDep_code(), true);
								entAdapter.notifyDataSetChanged();
							}
						});
					}
				});
			}
		};
		
		//点击成员事件
		entChildtListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Object obj = entAdapter.getChild(groupPosition, childPosition);
				if (obj instanceof MemberInfo) {
					//if (checkSelfGroup(obj)) {
					MemberInfo memberInfo = (MemberInfo) obj;
					
					if (selectType==SELECT_TYPE_MULTI) {
						ImageView selectImg = (ImageView) v.findViewById(R.id.user_select);
						if (selectImg.getVisibility() == View.GONE || selectImg.getVisibility() == View.INVISIBLE) {
							return true;
						}
						
						//List<Object> selectedMap = MyApplication.getInstance().getSelectedUserList();
						Drawable srcImg = selectImg.getDrawable();
						if (srcImg == null) {
							selectImg.setImageResource(R.drawable.uitb_57);
							MemberSelectActivity.addSelectedMember(memberInfo);
							//selectedMap.add(memberInfo);
						} else {
							selectImg.setImageDrawable(null);
							MemberSelectActivity.removeSelectedMember(memberInfo);
							//selectedMap.remove(memberInfo);
						}
						
						MemberSelectActivity.this.onSelectedMembersChange();
					} else {
						if (!excludeUids.contains(memberInfo.getEmp_uid())) {
							MemberSelectActivity.addSelectedMember(memberInfo);
							MemberSelectActivity.this.onClickOneMember();
						}
					}
					//}
				} else if (obj instanceof DepartmentInfo) {
					DepartmentInfo departmentInfo = (DepartmentInfo) obj;
					Intent intent = new Intent(MemberSelectActivity.this, DepartmentListActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("departmentInfo", departmentInfo);
					intent.putExtra("selecteduser", true);
					if (selectType==SELECT_TYPE_SINGLE)
						intent.putExtra("selectedone", true);
					intent.putExtra("excludeUids", (Serializable)excludeUids);
					startActivity(intent);
				}
				return true;
			}
		};
		entlistView.setAdapter(entAdapter);
		entlistView.setOnChildClickListener(entChildtListener);
		entlistView.setOnGroupExpandListener(entListener);
	}

	//初始化"我的部门"视图
	private void initMyDepartment() {
		departmentlistView = (ExpandableListView) findViewById(R.id.departmentlist);
		departmentAdapter = new GroupAdapter<DepartmentInfo>(this, departmentlistView);
		
		departmentAdapter.setExcludeUids(excludeUids);
		departmentAdapter.setSelectMember(true);
		if (selectType==SELECT_TYPE_SINGLE)
			departmentAdapter.setSelectOne(true);
		departmentAdapter.setSelectedMemberListener(this);
		
		//展开事件
		departmentListener = new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) departmentAdapter.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(), new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								showToast(errMsg);
							}
						});
					}

					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								departmentAdapter.setMembers(group.getDep_code(), false);
								departmentAdapter.notifyDataSetChanged();
							}
						});
					}
				});
			}
		};
		
		//点击成员事件
		departmenChildtListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				Object obj = departmentAdapter.getChild(groupPosition, childPosition);
//				if (checkSelfGroup(obj)) {
				MemberInfo memberInfo = (MemberInfo) obj;
				
				if (selectType==SELECT_TYPE_MULTI) { //多选视图
					ImageView selectImg = (ImageView) view.findViewById(R.id.user_select);
					if (selectImg.getVisibility() == View.GONE || selectImg.getVisibility() == View.INVISIBLE) {
						return true;
					}
					
					Drawable srcImg = selectImg.getDrawable();
					if (srcImg == null) {
						selectImg.setImageResource(R.drawable.uitb_57);
						MemberSelectActivity.addSelectedMember(memberInfo);
					} else {
						selectImg.setImageDrawable(null);
						MemberSelectActivity.removeSelectedMember(memberInfo);
					}
					
					MemberSelectActivity.this.onSelectedMembersChange();
				} else { //单选视图
					if (!excludeUids.contains(memberInfo.getEmp_uid())) {
						MemberSelectActivity.addSelectedMember(memberInfo);
						MemberSelectActivity.this.onClickOneMember();
					}
				}
//				}
				return true;
			}
		};
		departmentlistView.setAdapter(departmentAdapter);
		departmentlistView.setOnChildClickListener(departmenChildtListener);
		departmentlistView.setOnGroupExpandListener(departmentListener);
	}

	//初始化"个人群组"视图
	private void initGroup() {
		grouplistView = (ExpandableListView) findViewById(R.id.grouplist);
		groupAdapter = new GroupAdapter<PersonGroupInfo>(this, grouplistView);
		
		groupAdapter.setExcludeUids(excludeUids);
		groupAdapter.setSelectMember(true);
		if (selectType==SELECT_TYPE_SINGLE)
			groupAdapter.setSelectOne(true);
		groupAdapter.setSelectedMemberListener(this);
		
		//展开事件
		personGroupListener = new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				final GroupInfo group = (GroupInfo) groupAdapter.getGroup(groupPosition);
				EntboostUM.loadMembers(group.getDep_code(), new LoadAllMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								showToast(errMsg);
							}
						});
					}
					
					@Override
					public void onLoadAllMemberSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								groupAdapter.setMembers(group.getDep_code(), false);
								groupAdapter.notifyDataSetChanged();
							}
						});
					}
				});
			}

		};
		
		//点击成员事件
		childListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				Object obj = groupAdapter.getChild(groupPosition, childPosition);
				//if (checkSelfGroup(obj)) {
				MemberInfo memberInfo = (MemberInfo) obj;
				
				if (selectType==SELECT_TYPE_MULTI) { //多选视图
					ImageView selectImg = (ImageView) view.findViewById(R.id.user_select);
					if (selectImg.getVisibility() == View.GONE || selectImg.getVisibility() == View.INVISIBLE) {
						return true;
					}
					
					Drawable srcImg = selectImg.getDrawable();
					if (srcImg == null) {
						selectImg.setImageResource(R.drawable.uitb_57);
						MemberSelectActivity.addSelectedMember(memberInfo);
					} else {
						selectImg.setImageDrawable(null);
						MemberSelectActivity.removeSelectedMember(memberInfo);
					}
					MemberSelectActivity.this.onSelectedMembersChange();
				} else { //单选视图
					if (!excludeUids.contains(memberInfo.getEmp_uid())) {
						MemberSelectActivity.addSelectedMember(memberInfo);
						MemberSelectActivity.this.onClickOneMember();
					}
				}
				//}
				return true;
			}
		};
		grouplistView.setAdapter(groupAdapter);
		grouplistView.setOnChildClickListener(childListener);
		grouplistView.setOnGroupExpandListener(personGroupListener);
	}

	//初始化"我的好友"视图
	private void initContactView() {
		contactlistView = (ExpandableListView) findViewById(R.id.friendlist);
		friendAdapter = new ContactAdapter(this, contactlistView);
		
		friendAdapter.setExcludeUids(excludeUids);
		friendAdapter.setSelectMember(true);
		if (selectType==SELECT_TYPE_SINGLE)
			friendAdapter.setSelectOne(true);
		
		//点击成员事件
		contactListener = new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Object obj = friendAdapter.getChild(groupPosition, childPosition);
				if (obj instanceof ContactInfo) {
					//if (checkSelfGroup(obj)) {
					ContactInfo contactInfo = (ContactInfo)obj;
					
					if (selectType==SELECT_TYPE_MULTI) {
						ImageView selectImg = (ImageView) v.findViewById(R.id.user_select);
						if (selectImg.getVisibility() == View.GONE || selectImg.getVisibility() == View.INVISIBLE) {
							return true;
						}
						Drawable srcImg = selectImg.getDrawable();
						//List<Object> selectedMap = MyApplication.getInstance().getSelectedUserList();
						if (srcImg == null) {
							selectImg.setImageResource(R.drawable.uitb_57);
							MemberSelectActivity.addSelectedContact(contactInfo);
							//selectedMap.add(obj);
						} else {
							selectImg.setImageDrawable(null);
							MemberSelectActivity.removeSelectedContact(contactInfo);
							//selectedMap.remove(obj);
						}
						
						MemberSelectActivity.this.onSelectedMembersChange();
					} else {
						if (contactInfo.getCon_uid()!=null && !excludeUids.contains(contactInfo.getCon_uid())) {
							MemberSelectActivity.addSelectedContact(contactInfo);
							MemberSelectActivity.this.onClickOneMember();
						}
					}
					//}
				}
				return true;
			}
		};
		contactlistView.setAdapter(friendAdapter);
		contactlistView.setOnChildClickListener(contactListener);
	}

	/**
	 * 判断群组添加的新成员是否是已有成员
	 * 
	 * @return
	 */
//	private boolean checkSelfGroup(Object obj) {
//		if (obj instanceof ContactInfo) {
//			ContactInfo contactInfo = (ContactInfo) obj;
//			if (contactInfo.getCon_uid() == null) {
//				showToast("当前选中联系人不是系统用户，不能添加！");
//				return false;
//			} else {
//				if (EntboostCache.isExistMember(groupid, contactInfo.getCon_uid())) {
//					showToast("用户已是本群成员！");
//					return false;
//				}
//			}
//		} else if (obj instanceof MemberInfo) {
//			MemberInfo memberInfo = (MemberInfo) obj;
//			if (EntboostCache.isExistMember(groupid, memberInfo.getEmp_uid())) {
//				showToast("用户已是本群成员！");
//				return false;
//			}
//		}
//		return true;
//	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_member_select);
		
		MemberSelectActivity.clearSelectedCache();
		Log4jLog.d(LONG_TAG, "clear selected cache");
		//MyApplication.getInstance().getSelectedUserList().clear();
		
		//解析传入参数
		List<Long> eus = (List<Long>)getIntent().getSerializableExtra("excludeUids");
		if (eus!=null)
			excludeUids.addAll(eus);
		groupid = getIntent().getLongExtra("groupid", -1);
		selectType 	= getIntent().getIntExtra("selectType", SELECT_TYPE_MULTI);
		
		selected_area = (LinearLayout) findViewById(R.id.selected_area);
		selected_count = (TextView)findViewById(R.id.selected_count);
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
					public void onFailure(int code, String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								pageInfo.showError("无法加载部门信息");
							}
						});
					}

					@Override
					public void onLoadEntDepartmentSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								refreshPage();
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

	@Override
	public void onSelectedMembersChange() {
		List<Object> list = MemberSelectActivity.getSelectedMembersAndContacts();
		selected_count.setText(""+list.size());
		selectedAdapter.setInput(list);
		selectedAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClickOneMember() {
		//获取一个已选中成员
		List<Object> list =  MemberSelectActivity.getSelectedMembersAndContacts();
		if (list!=null && list.size()>0) {
			Object obj = list.get(0);
			
			//辨识是部门(群组)成员，或是好友(联系人)
			Long targetUid = null;
			String targetName = null;
			if (obj instanceof MemberInfo) {
				MemberInfo mi = (MemberInfo)obj;
				targetUid = mi.getEmp_uid();
				targetName = mi.getUsername();
			} else if (obj instanceof ContactInfo){
				ContactInfo ci = (ContactInfo)obj;
				targetUid = ci.getCon_uid();
				targetName = ci.getName();
			}
			
			if (targetUid!=null && targetUid>0) {
				Intent intent = getIntent();
				intent.putExtra("target_uid", targetUid);
				intent.putExtra("target_name", targetName);
				setResult(RESULT_OK, intent);
			}
		}
		
		this.finish();
	}
	
}
