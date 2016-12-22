package com.entboost.im.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.yunim.service.entity.ContactInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.utils.YIResourceUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.global.MyApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MemberSelectedAdapter extends BaseAdapter{
	private Context mContext;

	private List<Object> selected = new ArrayList<Object>();

	public MemberSelectedAdapter(Context context) {
		mContext = context;
	}
	
	public void setInput(Collection<Object> selected) {
		this.selected.clear();
		this.selected.addAll(selected);
	}
	
	private class GroupViewHolder {
		ImageView userImg;
		TextView  userName;
	}
	
	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getCount() {
		return selected.size();
	}

	@Override
	public Object getItem(int position) {
		return selected.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		final GroupViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_selecteduser, parent, false);
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.userImg = ((ImageView) convertView.findViewById(R.id.user_head));
			holder1.userName= ((TextView) convertView.findViewById(R.id.user_name));
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
		}
		if(obj instanceof MemberInfo){
			MemberInfo memberInfo = (MemberInfo)obj;
			holder1.userName.setText(memberInfo.getUsername());
			Bitmap img = YIResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
			if (img != null) {
				holder1.userImg.setImageBitmap(img);
			} else {
				ImageLoader.getInstance().displayImage(memberInfo.getHeadUrl(), holder1.userImg,
						MyApplication.getInstance().getUserImgOptions());
			}
		}else if(obj instanceof ContactInfo){
			ContactInfo contactInfo = (ContactInfo)obj;
			holder1.userImg.setImageResource(R.drawable.default_user);
			holder1.userName.setText(contactInfo.getName());
		}
		return convertView;
	}
}
