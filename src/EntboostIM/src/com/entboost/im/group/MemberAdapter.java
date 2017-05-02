package com.entboost.im.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.MemberInfo;
import net.yunim.utils.YIResourceUtils;
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
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.comparator.MemberInfoComparator;
import com.entboost.im.global.MyApplication;
import com.entboost.ui.utils.AbImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class MemberAdapter extends BaseAdapter {
	private Context mContext;
	private List<MemberInfo> memberInfos = new ArrayList<MemberInfo>();
	private boolean selecteduser;

	public Context getmContext() {
		return mContext;
	}

	public MemberAdapter(Context context) {
		mContext = context;
	}

	public void setSelecteduser(boolean selecteduser) {
		this.selecteduser = selecteduser;
	}

	public void setInput(List<MemberInfo> memberInfos) {
		this.memberInfos.clear();
		
		if (memberInfos != null) {
			Collections.sort(memberInfos, new MemberInfoComparator());
			this.memberInfos.addAll(memberInfos);
			
//			List<MemberInfo> online = new ArrayList<MemberInfo>(); //暂存在线成员
//			List<MemberInfo> offline = new ArrayList<MemberInfo>(); //暂存离线成员
//			
//			for (int i = 0; i < memberInfos.size(); i++) {
//				MemberInfo member = memberInfos.get(i);
//				if (member.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
//					offline.add(member);
//				} else {
//					online.add(member);
//				}
//			}
//			
//			//排序，待定：与MemberInfoComparator内部逻辑有冲突
//			Collections.sort(online, new MemberInfoComparator());
//			Collections.sort(offline, new MemberInfoComparator());
//			
//			this.memberInfos.addAll(online);
//			this.memberInfos.addAll(offline);
		}
	}

	private class MemberInfoViewHolder {
		ImageView userImg;
		TextView itemsText;
		TextView description;
		ImageButton user_select;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getCount() {
		return memberInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return memberInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MemberInfo memberInfo = (MemberInfo) getItem(position);
		final MemberInfoViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
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
		
		//设置头像
		Bitmap img = YIResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
		if (img != null) {
			if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
				holder1.userImg.setImageBitmap(AbImageUtil.grey(img));
			} else {
				holder1.userImg.setImageBitmap(img);
			}
		} else {
			ImageLoader.getInstance().loadImage(memberInfo.getHeadUrl(), MyApplication.getInstance().getUserImgOptions(), new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
				}

				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
						holder1.userImg.setImageBitmap(AbImageUtil
								.grey(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.default_user)));
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
		
		if (selecteduser) {
			holder1.user_select.setVisibility(View.VISIBLE);
		}
		holder1.itemsText.setText(memberInfo.getUsername());
		holder1.description.setText(memberInfo.getDescription());
		if (memberInfo.isCreator() 
				|| (memberInfo.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) {
			holder1.itemsText.setTextColor(Color.rgb(255, 0, 96));
		}else if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
			holder1.itemsText.setTextColor(Color.BLUE);
		}else{
			holder1.itemsText.setTextColor(Color.BLACK); //Color.rgb(25, 78, 98)
		}
		return convertView;
	}

}
