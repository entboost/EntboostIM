package com.entboost.im.persongroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;

import org.apache.log4j.lf5.util.ResourceUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.department.DepartmentInfoActivity;
import com.entboost.im.department.MemberInfoActivity;
import com.entboost.ui.utils.AbImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class GroupAdapter<T> extends BaseExpandableListAdapter {
	
	/** The tag. */
	private static String TAG = GroupAdapter.class.getSimpleName();
	private static String LONG_TAG = GroupAdapter.class.getName();
	
	private Context mContext;
	private Map<Long, Vector<Comparable>> groupMemberInfos = new HashMap<Long, Vector<Comparable>>();
	private Vector<T> groups = new Vector<T>();
	private boolean selectMember;
	private ExpandableListView listView;

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

	public void setInput(Vector<T> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
	}

	@Override
	public void notifyDataSetChanged() {
		for (Vector<Comparable> objs : groupMemberInfos.values()) {
			Collections.sort(objs);
		}
		super.notifyDataSetChanged();
	}

	public void setMembers(Long groupid, boolean hasNextGroup) {
		this.groupMemberInfos.remove(groupid);
		Vector<Comparable> temp = new Vector<Comparable>();
		if (hasNextGroup) {
			Vector<DepartmentInfo> ndis = EntboostCache.getNextDepartmentInfos(groupid);
			if (ndis != null) {
				temp.addAll(ndis);
			}
		}
		Vector<MemberInfo> gis = EntboostCache.getGroupMemberInfos(groupid);
		if (gis != null) {
			temp.addAll(gis);
		}
		// Log4jLog.e(LONG_TAG, "加载群组成员：" + groupid + "|数量：" + temp.size());
		this.groupMemberInfos.put(groupid, temp);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		List<Comparable> members = groupMemberInfos
				.get(getGroupId(groupPosition));
		if (members != null && members.size() > 0) {
			return members.get(childPosition);
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Object obj = getChild(groupPosition, childPosition);
		if (obj instanceof MemberInfo) {
			final MemberInfoViewHolder holder1;
			if (convertView == null
					|| !(convertView.getTag() instanceof MemberInfoViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.item_user1, parent, false);
				// 减少findView的次数
				holder1 = new MemberInfoViewHolder();
				// 初始化布局中的元素
				holder1.userImg = ((ImageView) convertView
						.findViewById(R.id.user_head));
				holder1.itemsText = ((TextView) convertView
						.findViewById(R.id.user_name));
				holder1.description = ((TextView) convertView
						.findViewById(R.id.user_description));
				holder1.user_select = ((ImageButton) convertView
						.findViewById(R.id.user_select));
				convertView.setTag(holder1);
			} else {
				holder1 = (MemberInfoViewHolder) convertView.getTag();
			}
			final MemberInfo memberInfo = (MemberInfo) obj;
			holder1.userImg.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							MemberInfoActivity.class);
					if (memberInfo != null) {
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
							intent.putExtra("selfFlag", true);
						}
						mContext.startActivity(intent);
					}
				}
			});
			Bitmap img = net.yunim.utils.ResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
			if (img != null) {
				if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
						.getValue()) {
					holder1.userImg.setImageBitmap(AbImageUtil.grey(img));
				} else {
					holder1.userImg.setImageBitmap(img);
				}
			} else {
				ImageLoader.getInstance().loadImage(memberInfo.getHeadUrl(),
						new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String arg0, View arg1) {
							}

							@Override
							public void onLoadingFailed(String arg0, View arg1,
									FailReason arg2) {
								if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
										.getValue()) {
									holder1.userImg.setImageBitmap(AbImageUtil
											.grey(BitmapFactory.decodeResource(
													mContext.getResources(),
													R.drawable.entboost_logo)));
								} else {
									holder1.userImg
											.setImageResource(R.drawable.entboost_logo);
								}
							}

							@Override
							public void onLoadingComplete(String arg0,
									View arg1, Bitmap arg2) {
								if (arg2 == null) {
									onLoadingFailed(arg0, arg1, null);
									return;
								}
								if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
										.getValue()) {
									holder1.userImg.setImageBitmap(AbImageUtil
											.grey(arg2));
								} else {
									holder1.userImg.setImageBitmap(arg2);
								}
							}

							@Override
							public void onLoadingCancelled(String arg0,
									View arg1) {
							}
						});
			}
			holder1.itemsText.setText(memberInfo.getUsername());
			holder1.description.setText(memberInfo.getDescription());
			if (selectMember) {
				holder1.user_select.setVisibility(View.VISIBLE);
				if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
					holder1.user_select.setVisibility(View.INVISIBLE);
				}
			}
			// 修改名称颜色
			if (memberInfo.isCreator()
					|| (memberInfo.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN
							.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN
							.getValue()) {
				holder1.itemsText.setTextColor(Color.rgb(255, 0, 96));
			}else if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
				holder1.itemsText.setTextColor(Color.BLUE);
			}else{
				holder1.itemsText.setTextColor(Color.rgb(25, 78, 98));
			}
		} else {
			GroupViewHolder holder2;
			if (convertView == null
					|| !(convertView.getTag() instanceof GroupViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.item_group1, parent, false);
				// 减少findView的次数
				holder2 = new GroupViewHolder();
				// 初始化布局中的元素
				holder2.itemsHead = ((ImageView) convertView
						.findViewById(R.id.item_group_head));
				holder2.itemsProgress = ((ProgressBar) convertView
						.findViewById(R.id.item_group_head_progress));
				holder2.itemsText = ((TextView) convertView
						.findViewById(R.id.item_group_name));
				holder2.itemsInfo = ((ImageView) convertView
						.findViewById(R.id.item_group_info));
				convertView.setTag(holder2);
			} else {
				holder2 = (GroupViewHolder) convertView.getTag();
			}
			convertView.setBackgroundResource(R.drawable.mm_listitem3);
			final DepartmentInfo group = (DepartmentInfo) obj;
			holder2.itemsHead.setVisibility(View.VISIBLE);
			holder2.itemsProgress.setVisibility(View.GONE);
			holder2.itemsText.setText(group.getDep_name()
					+ group.getEmp_online_state());
			holder2.itemsInfo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							DepartmentInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				}
			});
			if (selectMember) {
				holder2.itemsInfo.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Vector<Comparable> members = groupMemberInfos
				.get(getGroupId(groupPosition));
		if (members != null) {
			return members.size();
		}
		return 0;
	}

	private int getMemberChildrenCount(int groupPosition) {
		Vector<Comparable> members = groupMemberInfos
				.get(getGroupId(groupPosition));
		int num = 0;
		if (members != null) {
			for (Object obj : members) {
				if (obj instanceof MemberInfo) {
					++num;
				}
			}
		}
		return num;
	}

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

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded,
			View convertView, ViewGroup parent) {
		final GroupInfo group = (GroupInfo) getGroup(groupPosition);
		GroupViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_group, parent, false);
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.itemsHead = ((ImageView) convertView
					.findViewById(R.id.item_group_head));
			holder1.itemsProgress = ((ProgressBar) convertView
					.findViewById(R.id.item_group_head_progress));
			holder1.itemsText = ((TextView) convertView
					.findViewById(R.id.item_group_name));
			holder1.itemsInfo = ((ImageView) convertView
					.findViewById(R.id.item_group_info));
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
		}
		if (isExpanded
				&& group.getEmp_count() != getMemberChildrenCount(groupPosition)) {
			holder1.itemsHead.setVisibility(View.GONE);
			holder1.itemsProgress.setVisibility(View.VISIBLE);
		} else {
			holder1.itemsHead.setVisibility(View.VISIBLE);
			holder1.itemsProgress.setVisibility(View.GONE);
		}
		if (isExpanded) {
			holder1.itemsHead.setImageResource(R.drawable.ui67new);
		} else {
			holder1.itemsHead.setImageResource(R.drawable.ui66new);
		}
		holder1.itemsText.setText(group.getDep_name()
				+ group.getEmp_online_state());
		holder1.itemsInfo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (group instanceof PersonGroupInfo) {
					Intent intent = new Intent(mContext,
							PersonGroupInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				} else if (group instanceof DepartmentInfo) {
					Intent intent = new Intent(mContext,
							DepartmentInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				}
			}
		});
		if (selectMember) {
			holder1.itemsInfo.setVisibility(View.GONE);
		}
		// holder1.description.setText(mContext.getResources().getStringArray(
		// R.array.group_type)[EB_GROUP_TYPE.getIndex(group.getType())]);
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
		public ImageView itemsHead;
		public TextView itemsText;
		public ProgressBar itemsProgress;
		public ImageView itemsInfo;
	}

}
