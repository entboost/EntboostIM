package com.entboost.im.department;

import java.util.Vector;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.utils.ResourceUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.persongroup.MemberInfoViewHolder;
import com.entboost.ui.utils.AbImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class DepAndMemberAdapter extends BaseAdapter {
	private Context mContext;
	private Vector<Object> groupMemberInfos = new Vector<Object>();
	private boolean selectMember;

	public DepAndMemberAdapter(Context context) {
		mContext = context;
	}

	public Context getmContext() {
		return mContext;
	}

	public void setSelectMember(boolean selectMember) {
		this.selectMember = selectMember;
	}

	public void setMembers(Long groupid) {
		this.groupMemberInfos.clear();
		this.groupMemberInfos.addAll(EntboostCache.getNextDepartmentInfos(groupid));
		this.groupMemberInfos.addAll(EntboostCache.getGroupMemberInfos(groupid));
	}

	@Override
	public int getCount() {
		return this.groupMemberInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		return this.groupMemberInfos.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		if (obj instanceof MemberInfo) {
			final MemberInfoViewHolder holder1;
			if (convertView == null
					|| !(convertView.getTag() instanceof MemberInfoViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.item_user, parent, false);
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
			Bitmap img = ResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
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
			// 改变名称颜色
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
			final GroupViewHolder holder2;
			if (convertView == null
					|| !(convertView.getTag() instanceof GroupViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.item_group, parent, false);
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
			final DepartmentInfo group = (DepartmentInfo) obj;
			holder2.itemsHead.setVisibility(View.VISIBLE);
			holder2.itemsProgress.setVisibility(View.GONE);
			holder2.itemsHead.setImageResource(R.drawable.ui65new);
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

	static class GroupViewHolder {
		public ImageView itemsHead;
		public TextView itemsText;
		public ProgressBar itemsProgress;
		public ImageView itemsInfo;
	}

}
