package com.entboost.im.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.ContactGroup;
import net.yunim.service.entity.ContactInfo;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.comparator.ContactGroupComparator;
import com.entboost.im.comparator.ContactInfoComparator;
import com.entboost.im.global.MyApplication;
import com.entboost.im.group.MemberSelectActivity;
import com.entboost.ui.utils.AbImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ContactAdapter extends BaseExpandableListAdapter {
	private Map<Long, List<ContactInfo>> mList = new HashMap<Long, List<ContactInfo>>();
	private List<ContactGroup> grouplist = new ArrayList<ContactGroup>();
	private Context mContext;
	private boolean selectMember; //是否选择人员视图
	private boolean selectOne = false; //是否单选
	//除外的用户编号列表(不允许选中这些编号)
	private List<Long> excludeUids = new ArrayList<Long>();
	
	private ExpandableListView listView;

	public Context getmContext() {
		return mContext;
	}

	public void setSelectMember(boolean selectMember) {
		this.selectMember = selectMember;
	}
	
	public void setSelectOne(boolean selectOne) {
		this.selectOne = selectOne;
	}

	public void setExcludeUids(List<Long> excludeUids) {
		this.excludeUids = excludeUids;
	}

	public void initFriendList(List<ContactGroup> grouplist, List<ContactInfo> contactInfos) {
		this.grouplist.clear();
		mList.clear();
		
		//排序
		Collections.sort(contactInfos, new ContactInfoComparator());
		Collections.sort(grouplist, new ContactGroupComparator());
		
		//未分组
		ContactGroup nogroup = new ContactGroup();
		nogroup.setGroupname("未分组");
		this.grouplist.add(nogroup);
		//其它分组
		this.grouplist.addAll(grouplist);
		
		//初始化各分组列表
		mList.put(0L, new ArrayList<ContactInfo>());
		for (ContactGroup cg : grouplist) {
			mList.put(cg.getUgid(), new ArrayList<ContactInfo>());
		}
		
		//联系人依次填入各分组
		for (ContactInfo gi : contactInfos) {
			List<ContactInfo> clist = mList.get(gi.getUgid());
			if (clist != null)
				clist.add(gi);
		}
	}

	public ContactAdapter(Context context, ExpandableListView listView) {
		mContext = context;
		this.listView = listView;
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
		List<ContactInfo> childs = mList.get(getGroupId(groupPosition));
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
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final ItemViewHolder holder2;
		if (convertView == null || convertView.getTag() instanceof GroupViewHolder) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
			holder2 = new ItemViewHolder();
			// 初始化布局中的元素
			holder2.userImg = ((ImageView) convertView.findViewById(R.id.user_head));
			holder2.userName = ((TextView) convertView.findViewById(R.id.user_name));
			holder2.description = ((TextView) convertView.findViewById(R.id.user_description));
			holder2.user_select = ((ImageButton) convertView.findViewById(R.id.user_select));
			convertView.setTag(holder2);
		} else {
			holder2 = (ItemViewHolder) convertView.getTag();
			holder2.user_select.setImageResource(0);
		}
		
		final ContactInfo ci = (ContactInfo) getChild(groupPosition, childPosition);
		
		if (selectMember) {
			holder2.user_select.setVisibility(View.VISIBLE);
			
			if (selectOne || ci.getCon_uid()==null || excludeUids.contains(ci.getCon_uid())) {
				holder2.user_select.setVisibility(View.INVISIBLE);
			}
			
			if (MemberSelectActivity.isContactSelected(ci))
				holder2.user_select.setImageResource(R.drawable.uitb_57);
		}
		
		String name = null;
		if (StringUtils.isNotBlank(ci.getName())) {
			name = ci.getName();
		} else {
			name = ci.getContact();
		}
		
		holder2.userName.setText(name);
		String type = "";
		if (ci.getCon_uid()==null || ci.getCon_uid()==0) { //非系统用户
			type = "[非系统用户]";
			if (selectMember)
				holder2.user_select.setVisibility(View.INVISIBLE);
			
			holder2.userImg.setImageBitmap(AbImageUtil.grey(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_user)));
		} else { //系统用户
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			if (ci.getType() == 0 && (appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) == AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
				type = "[系统用户-未验证]";
			} else {
				type = "";//"[系统用户]";
			}
			
			ImageLoader.getInstance().loadImage(ci.getHeadUrl(), MyApplication.getInstance().getUserImgOptions(), new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
				}

				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					if (ci.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) { //离线
						holder2.userImg.setImageBitmap(AbImageUtil
								.grey(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.default_user)));
					} else { //在线
						holder2.userImg.setImageResource(R.drawable.default_user);
					}
				}

				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
					if (arg2 == null) {
						onLoadingFailed(arg0, arg1, null);
						return;
					}
					if (ci.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) { //离线
						holder2.userImg.setImageBitmap(AbImageUtil.grey(arg2));
					} else { //在线
						holder2.userImg.setImageBitmap(arg2);
					}
				}

				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
				}
			});
		}
		
		//点击头像事件；选择视图不允许点击
		if (!selectMember) {
			holder2.userImg.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, ContactInfoActivity.class);
					if (ci != null) {
						intent.putExtra("con_id", ci.getCon_id());
						mContext.startActivity(intent);
					}
				}
			});
		}
		
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) != AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
			holder2.userImg.setImageResource(R.drawable.default_user);
		}
		holder2.description.setText(type + ci.getDescription());
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition >= mList.size()) {
			return 0;
		}
		List<ContactInfo> childrens = mList.get(getGroupId(groupPosition));
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
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final ContactGroup group = grouplist.get(groupPosition);
		GroupViewHolder holder1;
		if (convertView == null || convertView.getTag() instanceof ItemViewHolder) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_contact_group, parent, false);
			
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.itemsHead = ((ImageView) convertView.findViewById(R.id.item_contact_group_head));
			holder1.itemsText = ((TextView) convertView.findViewById(R.id.item_contact_group_name));
			holder1.itemsInfo = ((ImageView) convertView.findViewById(R.id.item_group_info));
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
		}
		
		if (isExpanded) {
			holder1.itemsHead.setImageResource(R.drawable.tree_opened);
		} else {
			holder1.itemsHead.setImageResource(R.drawable.tree_closed);
		}
		
		if (group != null) {
			int size = 0;
			int onlineSize = 0;
			if (group.getUgid() != null && mList.get(group.getUgid()) != null) {
				size = mList.get(group.getUgid()).size();
				for (ContactInfo ci : mList.get(group.getUgid())) {
					if (ci.getState() > EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
						++onlineSize;
					}
				}
			}
			
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			if (size != 0) {
				if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) 
						!= AppAccountInfo.SYSTEM_SETTING_VALUE_AUTH_CONTACT) {
					holder1.itemsText.setText(group.getGroupname() + " [" + size + "]");
				} else {
					holder1.itemsText.setText(group.getGroupname() + " [" + onlineSize + "/" + size + "]");
				}
			} else {
				holder1.itemsText.setText(group.getGroupname());
			}
		}
		
		if (group.getUgid() == 0 || selectMember) {
			holder1.itemsInfo.setVisibility(View.GONE);
		} else {
			holder1.itemsInfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, ContactGroupActivity.class);
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
