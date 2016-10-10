package com.entboost.im.contact;

import java.util.Vector;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.ContactGroup;
import net.yunim.service.entity.ContactInfo;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.chat.ChatActivity;
import com.entboost.ui.utils.AbImageUtil;

public class ContactAdapter extends BaseExpandableListAdapter {
	private Map<Long, Vector<ContactInfo>> mList = new HashMap<Long, Vector<ContactInfo>>();
	private Vector<ContactGroup> grouplist = new Vector<ContactGroup>();
	private Context mContext;
	private boolean selectMember;

	public Context getmContext() {
		return mContext;
	}

	public void setSelectMember(boolean selectMember) {
		this.selectMember = selectMember;
	}

	public void initFriendList(Vector<ContactGroup> grouplist,
			Vector<ContactInfo> contactInfos) {
		this.grouplist.clear();
		mList.clear();
		Collections.sort(contactInfos);
		Collections.sort(grouplist);
		ContactGroup nogroup = new ContactGroup();
		nogroup.setGroupname("未分组");
		this.grouplist.add(nogroup);
		this.grouplist.addAll(grouplist);
		mList.put(0l, new Vector<ContactInfo>());
		for (ContactGroup cg : grouplist) {
			mList.put(cg.getUgid(), new Vector<ContactInfo>());
		}
		for (ContactInfo gi : contactInfos) {
			Vector<ContactInfo> clist = mList.get(gi.getUgid());
			if (clist != null) {
				clist.add(gi);
			}
		}
	}

	public ContactAdapter(Context context) {
		mContext = context;
	}

	/**
	 * View元素
	 */
	private class ItemViewHolder {
		ImageView userImg;
		TextView userName;
		TextView description;
		ImageButton user_select;
	}

	private class GroupViewHolder {
		ImageView itemsHead;
		TextView itemsText;
		ImageView itemsInfo;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Vector<ContactInfo> childs = mList.get(getGroupId(groupPosition));
		if (childs == null) {
			return null;
		}
		return childs.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ItemViewHolder holder2 = null;
		if (convertView == null
				|| convertView.getTag() instanceof GroupViewHolder) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_user, parent, false);
			holder2 = new ItemViewHolder();
			// 初始化布局中的元素
			holder2.userImg = ((ImageView) convertView
					.findViewById(R.id.user_head));
			holder2.userName = ((TextView) convertView
					.findViewById(R.id.user_name));
			holder2.description = ((TextView) convertView
					.findViewById(R.id.user_description));
			holder2.user_select = ((ImageButton) convertView
					.findViewById(R.id.user_select));
			convertView.setTag(holder2);
		} else {
			holder2 = (ItemViewHolder) convertView.getTag();
		}
		if (selectMember) {
			holder2.user_select.setVisibility(View.VISIBLE);
		}
		final ContactInfo mi = (ContactInfo) getChild(groupPosition,
				childPosition);
		String name = null;
		if (StringUtils.isNotBlank(mi.getName())) {
			name = mi.getName();
		} else {
			name = mi.getContact();
		}
		holder2.userName.setText(name);
		String type = "";
		if (mi.getCon_uid() == null) {
			type = "[非系统用户]";
			if (selectMember) {
				holder2.user_select.setVisibility(View.INVISIBLE);
			}
			holder2.userImg.setImageBitmap(AbImageUtil.grey(BitmapFactory
					.decodeResource(mContext.getResources(),
							R.drawable.entboost_logo)));
		} else {
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			if (mi.getType() == 0
					&& (appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
				type = "[系统用户-未验证]";
			} else {
				type = "[系统用户]";
			}
			if (mi.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
					.getValue()) {
				holder2.userImg.setImageBitmap(AbImageUtil.grey(BitmapFactory
						.decodeResource(mContext.getResources(),
								R.drawable.entboost_logo)));
			} else {
				holder2.userImg.setImageResource(R.drawable.entboost_logo);
			}
			holder2.userImg.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							ContactInfoActivity.class);
					if (mi != null) {
						intent.putExtra("con_id", mi.getCon_id());
						mContext.startActivity(intent);
					}
				}
			});
		}
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) != AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
			holder2.userImg.setImageResource(R.drawable.entboost_logo);
		}
		holder2.description.setText(type + mi.getDescription());
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition >= mList.size()) {
			return 0;
		}
		Vector<ContactInfo> childrens = mList.get(getGroupId(groupPosition));
		if (childrens == null) {
			return 0;
		}
		return childrens.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return grouplist.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return grouplist.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return grouplist.get(groupPosition).getUgid();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		final ContactGroup group = grouplist.get(groupPosition);
		GroupViewHolder holder1;
		if (convertView == null
				|| convertView.getTag() instanceof ItemViewHolder) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_contact_group, parent, false);
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.itemsHead = ((ImageView) convertView
					.findViewById(R.id.item_contact_group_head));
			holder1.itemsText = ((TextView) convertView
					.findViewById(R.id.item_contact_group_name));
			holder1.itemsInfo = ((ImageView) convertView
					.findViewById(R.id.item_group_info));
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
		}
		if (isExpanded) {
			holder1.itemsHead.setImageResource(R.drawable.ui67new);
		} else {
			holder1.itemsHead.setImageResource(R.drawable.ui66new);
		}
		if (group != null) {
			int size = 0;
			int onlineSize = 0;
			if (group.getUgid() != null && mList.get(group.getUgid()) != null) {
				size = mList.get(group.getUgid()).size();
				for (ContactInfo ci : mList.get(group.getUgid())) {
					if (ci.getState() > EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
							.getValue()) {
						++onlineSize;
					}
				}
			}
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			if (size != 0) {
				if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) != AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
					holder1.itemsText.setText(group.getGroupname() + " ["
							+ size + "]");
				} else {
					holder1.itemsText.setText(group.getGroupname() + " ["
							+ onlineSize + "/" + size + "]");
				}
			} else {
				holder1.itemsText.setText(group.getGroupname());
			}
		}
		if (group.getUgid() == 0) {
			holder1.itemsInfo.setVisibility(View.GONE);
		} else {
			holder1.itemsInfo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							ContactGroupActivity.class);
					intent.putExtra("contactgroup", group);
					mContext.startActivity(intent);
				}
			});
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
