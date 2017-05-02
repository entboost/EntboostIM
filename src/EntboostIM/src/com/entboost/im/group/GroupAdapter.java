package com.entboost.im.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.LoadAllMemberListener;
import net.yunim.utils.YIResourceUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.comparator.DepartmentInfoComparator;
import com.entboost.im.comparator.MemberInfoComparator;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.UIUtils;
import com.entboost.ui.utils.AbImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class GroupAdapter<T> extends BaseExpandableListAdapter {
	
	/** The tag. */
	private static String TAG = GroupAdapter.class.getSimpleName();
	private static String LONG_TAG = GroupAdapter.class.getName();
	
	private Context mContext;
	/**
	 * 以部门(群组)编号为key，缓存"部门和成员"列表
	 */
	private Map<Long, List<Object>> localGroupMemberInfos = new HashMap<Long, List<Object>>();
	/**
	 * 部门(群组)列表
	 */
	private List<T> groups = new ArrayList<T>();
	
	private boolean selectMember; //是否选择人员视图
	private boolean selectOne = false; //是否单选
	//除外的用户编号列表(不允许选中这些编号)
	private List<Long> excludeUids = new ArrayList<Long>();
	
	private ExpandableListView listView;
	//选中成员事件监听器
	private SelectedMemberListener selectedMemberListener;
	
	//暂记录正在加载信息的群组
	private Map<Long, Boolean> groupLoadings = new ConcurrentHashMap<Long, Boolean>();
	
	//是否统计子部门成员的人数
	private boolean calculateSubDepartment;
	
	public void setCalculateSubDepartment(boolean calculateSubDepartment) {
		this.calculateSubDepartment = calculateSubDepartment;
	}

	public void setSelectedMemberListener(SelectedMemberListener selectedMemberListener) {
		this.selectedMemberListener = selectedMemberListener;
	}
	
	public void setSelectOne(boolean selectOne) {
		this.selectOne = selectOne;
	}
	
	public void setExcludeUids(List<Long> excludeUids) {
		this.excludeUids = excludeUids;
	}

	/**
	 * 设置某群组"是否正在加载"
	 * @param depCode 部门或群组编号
	 * @param isLoading 是否正在加载
	 */
	public synchronized void setLoading(long depCode, boolean isLoading) {
		//Log4jLog.d(LONG_TAG, "group "+ depCode+", setLoading "+ isLoading);
		Long key = Long.valueOf(depCode);
		if (isLoading) {
			groupLoadings.put(key, isLoading);
		} else {
			groupLoadings.remove(key);
		}
	}
	
	/**
	 * 判断某群组"是否正在加载"
	 * @param depCode 部门或群组编号
	 * @return 是否正在加载
	 */
	public synchronized boolean isLoading(long depCode) {
		Boolean isLoading = groupLoadings.get(Long.valueOf(depCode));
		if (isLoading!=null && isLoading.booleanValue())
			return true;
		return false;
	}

	public Context getmContext() {
		return mContext;
	}

	public void setSelectMember(boolean selectMember) {
		this.selectMember = selectMember;
	}

	public GroupAdapter(Context context, ExpandableListView listView) {
		mContext = context;
		this.listView = listView;
	}

	public void setInput(List<T> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
	}

//	@Override
//	public void notifyDataSetChanged() {
////		for (List<Object> objs : localGroupMemberInfos.values()) {
////			Collections.sort(objs);
////		}
//		super.notifyDataSetChanged();
//	}

	//删除作为叶子的部门节点
	public void removeDepartmentInfoLeafNode(Long groupid) {
		for (List<Object> list: localGroupMemberInfos.values()) {
			Iterator<Object> it = list.iterator();
			while(it.hasNext()) {
				Object obj = it.next();
				if (obj instanceof DepartmentInfo) {
					DepartmentInfo depInfo = (DepartmentInfo)obj;
					if (depInfo.getDep_code()-groupid ==0) {
						it.remove();
					}
				}
			}
		}
	}
	
	public void setMembers(Long groupid, boolean hasNextGroup) {
		this.localGroupMemberInfos.remove(groupid);
		List<Object> temp = new ArrayList<Object>();
		
		//获取下级部门
		if (hasNextGroup) {
			List<DepartmentInfo> ndis = EntboostCache.getNextDepartmentInfos(groupid);
			if (ndis != null) {
				Collections.sort(ndis, new DepartmentInfoComparator());
				temp.addAll(ndis);
			}
		}
		
		//获取该部门下成员
		List<MemberInfo> gis = EntboostCache.getGroupMemberInfos(groupid);
		if (gis != null) {
			Collections.sort(gis, new MemberInfoComparator());
			temp.addAll(gis);
		}
		
		// Log4jLog.e(LONG_TAG, "加载群组成员：" + groupid + "|数量：" + temp.size());
		this.localGroupMemberInfos.put(groupid, temp);
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		List<Object> objs = localGroupMemberInfos.get(getGroupId(groupPosition));
		if (objs != null && objs.size() > 0) {
			return objs.get(childPosition);
		}
		return null;
	}
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Object obj = getChild(groupPosition, childPosition);
		if (obj instanceof MemberInfo) { //成员
			final MemberInfoViewHolder holder1;
			if (convertView == null || !(convertView.getTag() instanceof MemberInfoViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user1, parent, false);
				// 减少findView的次数
				holder1 = new MemberInfoViewHolder();
				// 初始化布局中的元素
				holder1.userImg = ((ImageView) convertView.findViewById(R.id.user_head));
				holder1.itemsText = ((TextView) convertView.findViewById(R.id.user_name));
				holder1.description = ((TextView) convertView.findViewById(R.id.user_description));
				holder1.user_select = ((ImageButton) convertView.findViewById(R.id.user_select));
				convertView.setTag(holder1);
			} else {
				holder1 = (MemberInfoViewHolder) convertView.getTag();
				holder1.user_select.setImageResource(0);
			}
			
			final MemberInfo memberInfo = (MemberInfo) obj;
			//处理点击头像事件；选择视图不允许点击
			if (!selectMember) {
				holder1.userImg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, MemberInfoActivity.class);
						if (memberInfo != null) {
							intent.putExtra("memberCode", memberInfo.getEmp_code());
							if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
								intent.putExtra("selfFlag", true);
							}
							mContext.startActivity(intent);
						}
					}
				});
			}
			
			//设置头像
			Bitmap img = YIResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
			if (img != null) {
				if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
					holder1.userImg.setImageBitmap(AbImageUtil.grey(img));
				} else {
					holder1.userImg.setImageBitmap(img);
				}
			} else {
				String t = memberInfo.getHeadUrl();
				Log4jLog.d(LONG_TAG, t);
				ImageLoader.getInstance().loadImage(t, MyApplication.getInstance().getUserImgOptions(), new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String arg0, View arg1) {
					}
					
					@Override
					public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
						if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
							holder1.userImg.setImageBitmap(AbImageUtil.grey(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_user)));
						} else {
							holder1.userImg.setImageResource(R.drawable.default_user);
						}
					}

					@Override
					public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
						if (arg2 == null) {
							onLoadingFailed(arg0, arg1, null);
							return;
						}
						if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
							holder1.userImg.setImageBitmap(AbImageUtil.grey(arg2));
						} else {
							holder1.userImg.setImageBitmap(arg2);
						}
					}

					@Override
					public void onLoadingCancelled(String arg0, View arg1) {
					}
				});
			}
			
			holder1.itemsText.setText(memberInfo.getUsername());
			holder1.description.setText(memberInfo.getDescription());
			
			//在选择视图
			if (selectMember) {
				holder1.user_select.setVisibility(View.VISIBLE);
				
				if (selectOne || excludeUids.contains(memberInfo.getEmp_uid())/*|| memberInfo.getEmp_uid() - EntboostCache.getUid() == 0*/) {
					holder1.user_select.setVisibility(View.INVISIBLE);
				}
				
				if (MemberSelectActivity.isMemberSelected(memberInfo))
					holder1.user_select.setImageResource(R.drawable.uitb_57);
			}
			
			// 修改名称颜色
			if (memberInfo.isCreator()
					|| (memberInfo.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN
							.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) {
				holder1.itemsText.setTextColor(Color.rgb(255, 0, 96));
			}else if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
				holder1.itemsText.setTextColor(Color.BLUE);
			}else{
				holder1.itemsText.setTextColor(Color.BLACK); //Color.rgb(25, 78, 98)
			}
		} else { //深层部门
			final GroupViewHolder holder2;
			if (convertView == null || !(convertView.getTag() instanceof GroupViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_group1, parent, false);
				// 减少findView的次数
				holder2 = new GroupViewHolder();
				// 初始化布局中的元素
				holder2.itemsHead = ((ImageView) convertView.findViewById(R.id.item_group_head));
				holder2.itemsProgress = ((ProgressBar) convertView.findViewById(R.id.item_group_head_progress));
				holder2.itemsText = ((TextView) convertView.findViewById(R.id.item_group_name));
				holder2.itemsInfo = ((ImageView) convertView.findViewById(R.id.item_group_info));
				holder2.itemsTalk = ((ImageView) convertView.findViewById(R.id.item_call_talk));
				holder2.itemsSelectAll = ((ImageView) convertView.findViewById(R.id.select_all));
				
				convertView.setTag(holder2);
			} else {
				holder2 = (GroupViewHolder) convertView.getTag();
			}
			convertView.setBackgroundResource(R.drawable.mm_listitem3);
			
			final DepartmentInfo group = (DepartmentInfo) obj;
			holder2.itemsHead.setVisibility(View.VISIBLE);
			holder2.itemsProgress.setVisibility(View.GONE);
			//名称+人数
			holder2.itemsText.setText(group.getDep_name() + createFormatedStrOfGroupCount(group));
			
			//最右边的按钮布局
			RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.rightMargin = 5;
			
			//选择视图
			if (selectMember) {
				//选择框靠最右边
				holder2.itemsSelectAll.setLayoutParams(layoutParams);
				
				holder2.itemsInfo.setVisibility(View.GONE);
				holder2.itemsTalk.setVisibility(View.GONE);
				holder2.itemsSelectAll.setVisibility(View.VISIBLE);
				
				//设置全选按钮显示状态
				if (MemberSelectActivity.isSelectedGroup(group.getDep_code())) {
					holder2.itemsSelectAll.setImageResource(R.drawable.uitb_57);
				} else {
					holder2.itemsSelectAll.setImageResource(0);
				}
				
				if (selectOne)
					holder2.itemsSelectAll.setVisibility(View.GONE);
			} else { //普通视图
				//查看属性按钮靠最右边
				holder2.itemsInfo.setLayoutParams(layoutParams);
				
				holder2.itemsInfo.setVisibility(View.VISIBLE);
				holder2.itemsTalk.setVisibility(View.VISIBLE);
				holder2.itemsSelectAll.setVisibility(View.GONE);
			}
			
			//查看属性按钮
			holder2.itemsInfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, DepartmentInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				}
			});
			
			//点击进入聊天按钮
			if (!selectMember && group.getMy_emp_id()!=null && group.getMy_emp_id()>0) {
				holder2.itemsTalk.setVisibility(View.VISIBLE);
				holder2.itemsTalk.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 打开聊天会话界面
						Intent intent = new Intent(GroupAdapter.this.mContext, ChatActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(ChatActivity.INTENT_TITLE, group.getDep_name());
						intent.putExtra(ChatActivity.INTENT_TOID, group.getDep_code());
						intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
						GroupAdapter.this.mContext.startActivity(intent);
					}
				});
			} else {
				holder2.itemsTalk.setVisibility(View.GONE);
			}
			
			//全选子项按钮
			if (holder2.itemsSelectAll.getVisibility() == View.VISIBLE) {
				holder2.itemsSelectAll.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final Long depCode = group.getDep_code();
						//final int groupPosition = getGroupPosition(depCode);
						Drawable srcImg = holder2.itemsSelectAll.getDrawable();
						
						//选中
						if (srcImg == null) {
							holder2.itemsSelectAll.setImageResource(R.drawable.uitb_57);
							//全选组内全部成员
							UIUtils.showProgressDialog(mContext, "请稍后...");
							EntboostUM.loadMembers(depCode, new LoadAllMemberListener() {
								@Override
								public void onFailure(int code, final String errMsg) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											UIUtils.removeProgressDialog();
											//showToast(errMsg);
										}
									});
								}
								
								@Override
								public void onLoadAllMemberSuccess() {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											List<MemberInfo> memberInfos = EntboostCache.getGroupMemberInfos(depCode);
											if (memberInfos != null) {
												MemberSelectActivity.addSelectedGroup(group);
												//MemberSelectActivity.addSelectedMembers(depCode, memberInfos);
												
												//去除被过滤的用户
												for (MemberInfo mi : memberInfos) {
													if (!excludeUids.contains(mi.getEmp_uid()))
														MemberSelectActivity.addSelectedMember(mi);
												}
												
												//触发变更回调事件
												if (selectedMemberListener!=null)
													selectedMemberListener.onSelectedMembersChange();
											}
											
											UIUtils.removeProgressDialog();
										}
									});
								}
							});
						} else { //反选
							holder2.itemsSelectAll.setImageResource(0);
							//整组删除已选中成员
							MemberSelectActivity.removeSelectedMembers(depCode);
							MemberSelectActivity.removeSelectedGroup(depCode);
							
							//触发变更回调事件
							if (selectedMemberListener!=null)
								selectedMemberListener.onSelectedMembersChange();
						}
					}
				});
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<Object> objs = localGroupMemberInfos.get(getGroupId(groupPosition));
		if (objs != null) {
			return objs.size();
		}
		return 0;
	}
	
//	private List<MemberInfo> getChildren(int groupPosition) {
//		List<MemberInfo> results = new ArrayList<MemberInfo>();
//		List<Object> objs = localGroupMemberInfos.get(getGroupId(groupPosition));
//		if (objs!=null) {
//			for (Object obj : objs) {
//				if (obj instanceof MemberInfo) {
//					results.add((MemberInfo)obj);
//				}
//			}
//		}
//		
//		return results;
//	}
	
//	private int getMemberChildrenCount(int groupPosition) {
//		List<Comparable> members = localGroupMemberInfos.get(getGroupId(groupPosition));
//		int num = 0;
//		if (members != null) {
//			for (Object obj : members) {
//				if (obj instanceof MemberInfo) {
//					++num;
//				}
//			}
//		}
//		return num;
//	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		GroupInfo group = (GroupInfo) groups.get(groupPosition);
		if (group == null) {
			return -1;
		} else {
			return group.getDep_code();
		}
	}
	
	/**
	 * 通过id获取位置
	 * @param groupId
	 * @return
	 */
	public int getGroupPosition(long groupId) {
		if (groups!=null) {
			for (int i=0; i<groups.size(); i++) {
				GroupInfo group = (GroupInfo)groups.get(i);
				if (group.getDep_code()-groupId==0) {
					return i;
				}
			}
		}
		
		return -1;
	}

	//名称+成员在线人数和成员人数
	private String createFormatedStrOfGroupCount(GroupInfo group) {
		//名称+成员在线人数和成员人数
		String formatedCountStr = " ";
		if (calculateSubDepartment) {
			int count = EntboostCache.getDepartmentMemberCount(group.getDep_code(), true);
			formatedCountStr = formatedCountStr + (count>0?("[" + EntboostCache.getDepartmentMemberOnlineCount(group.getDep_code(), true) + "/" + count + "]"):"");
		} else {
			formatedCountStr = formatedCountStr + (group.getEmp_count()>0?("[" + EntboostCache.getGroupOnlineCount(group.getDep_code()) + "/" + group.getEmp_count() + "]"):"");
		}
		
		return formatedCountStr;
	}
	
	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
		final GroupInfo group = (GroupInfo) getGroup(groupPosition);
		final GroupViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false);
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.itemsHead = ((ImageView) convertView.findViewById(R.id.item_group_head));
			holder1.itemsProgress = ((ProgressBar) convertView.findViewById(R.id.item_group_head_progress));
			holder1.itemsText = ((TextView) convertView.findViewById(R.id.item_group_name));
			holder1.itemsInfo = ((ImageView) convertView.findViewById(R.id.item_group_info));
			holder1.itemsTalk = ((ImageView) convertView.findViewById(R.id.item_call_talk));
			holder1.itemsSelectAll = ((ImageView) convertView.findViewById(R.id.select_all));
			
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
			holder1.itemsSelectAll.setImageResource(0);
		}
		
		//if (isExpanded && group.getEmp_count() != getMemberChildrenCount(groupPosition)) {
		if (isExpanded && GroupAdapter.this.isLoading(group.getDep_code())) {
			//Log4jLog.d(LONG_TAG, "group is loading");
			holder1.itemsHead.setVisibility(View.GONE);
			holder1.itemsProgress.setVisibility(View.VISIBLE);
		} else {
			//Log4jLog.d(LONG_TAG, "group ok");
			holder1.itemsHead.setVisibility(View.VISIBLE);
			holder1.itemsProgress.setVisibility(View.GONE);
		}
		
		
		//折叠/展开标记
		Context context = MyApplication.getInstance().getApplicationContext();
		int indentify = context.getResources().getIdentifier(
				context.getPackageName()+":drawable/"+"tree_type"+group.getType()+"_"+(isExpanded?"opened":"closed"), null, null);
		holder1.itemsHead.setImageResource(indentify);
		//名称+人数
		holder1.itemsText.setText(group.getDep_name() + createFormatedStrOfGroupCount(group));
		
		//最右边的按钮布局
		RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.rightMargin = 5;
		
		//在选择视图
		if (selectMember) {
			//选择框靠最右边
			holder1.itemsSelectAll.setLayoutParams(layoutParams);
			
			//设置控件可见性
			holder1.itemsInfo.setVisibility(View.GONE);
			holder1.itemsTalk.setVisibility(View.GONE);
			holder1.itemsSelectAll.setVisibility(View.VISIBLE);
			
			//设置全选按钮显示状态
			if (MemberSelectActivity.isSelectedGroup(group.getDep_code())) {
				holder1.itemsSelectAll.setImageResource(R.drawable.uitb_57);
			} else {
				holder1.itemsSelectAll.setImageResource(0);
			}
			
			if (selectOne)
				holder1.itemsSelectAll.setVisibility(View.GONE);
		} else {
			//查看属性按钮靠最右边
			holder1.itemsInfo.setLayoutParams(layoutParams);
			
			//设置控件可见性
			holder1.itemsInfo.setVisibility(View.VISIBLE);
			holder1.itemsTalk.setVisibility(View.VISIBLE);
			holder1.itemsSelectAll.setVisibility(View.GONE);
		}
		
		//点击属性按钮
		if (holder1.itemsInfo.getVisibility() == View.VISIBLE) {
			holder1.itemsInfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (group instanceof PersonGroupInfo) {
						Intent intent = new Intent(mContext,PersonGroupInfoActivity.class);
						intent.putExtra("depid", group.getDep_code());
						mContext.startActivity(intent);
					} else if (group instanceof DepartmentInfo) {
						Intent intent = new Intent(mContext,DepartmentInfoActivity.class);
						intent.putExtra("depid", group.getDep_code());
						mContext.startActivity(intent);
					}
				}
			});
		}
		
		//点击进入聊天按钮
		if (!selectMember && group.getMy_emp_id()!=null && group.getMy_emp_id()>0) {
			holder1.itemsTalk.setVisibility(View.VISIBLE);
			holder1.itemsTalk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 打开聊天会话界面
					Intent intent = new Intent(GroupAdapter.this.mContext, ChatActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(ChatActivity.INTENT_TITLE, group.getDep_name());
					intent.putExtra(ChatActivity.INTENT_TOID, group.getDep_code());
					intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
					GroupAdapter.this.mContext.startActivity(intent);
				}
			});
		} else {
			holder1.itemsTalk.setVisibility(View.GONE);
		}
		
		//全选子项按钮
		if (holder1.itemsSelectAll.getVisibility() == View.VISIBLE) {
			holder1.itemsSelectAll.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log4jLog.d(LONG_TAG, "select all items");
					final Long depCode = group.getDep_code();
					final int groupPosition = getGroupPosition(depCode);
					Drawable srcImg = holder1.itemsSelectAll.getDrawable();
					
					//选中
					if (srcImg == null) {
						holder1.itemsSelectAll.setImageResource(R.drawable.uitb_57);
						//全选组内全部成员
						if (groupPosition>=0) {
							UIUtils.showProgressDialog(mContext, "请稍后...");
							
							EntboostUM.loadMembers(depCode, new LoadAllMemberListener() {
								@Override
								public void onFailure(int code, final String errMsg) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											UIUtils.removeProgressDialog();
											//showToast(errMsg);
										}
									});
								}
								
								@Override
								public void onLoadAllMemberSuccess() {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											List<MemberInfo> memberInfos = EntboostCache.getGroupMemberInfos(depCode);
											if (memberInfos != null) {
												MemberSelectActivity.addSelectedGroup(group);
												//MemberSelectActivity.addSelectedMembers(depCode, memberInfos);
												
												//去除被过滤的用户
												for (MemberInfo mi : memberInfos) {
													if (!excludeUids.contains(mi.getEmp_uid()))
														MemberSelectActivity.addSelectedMember(mi);
												}
												
												//自动展开子项
												listView.expandGroup(groupPosition);
												//触发变更回调事件
												if (selectedMemberListener!=null)
													selectedMemberListener.onSelectedMembersChange();
											}
											
											UIUtils.removeProgressDialog();
										}
									});
								}
							});
						}
					} else { //反选
						holder1.itemsSelectAll.setImageResource(0);
						//整组删除已选中成员
						MemberSelectActivity.removeSelectedMembers(depCode);
						MemberSelectActivity.removeSelectedGroup(depCode);
						
						//借助伸缩功能刷新子项目
						if (listView.isGroupExpanded(groupPosition))
							listView.expandGroup(groupPosition);
						else 
							listView.collapseGroup(groupPosition);
						
						//触发变更回调事件
						if (selectedMemberListener!=null)
							selectedMemberListener.onSelectedMembersChange();
					}
				}
			});
		}
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	static class GroupViewHolder {
		public ImageView itemsHead; //头像控件
		public TextView itemsText;	//名称控件
		public ProgressBar itemsProgress; //加载进度控件
		public ImageView itemsInfo; //查看属性按钮
		public ImageView itemsTalk; //进入聊天会话按钮
		public ImageView itemsSelectAll; //全选组内成员按钮
	}

}
