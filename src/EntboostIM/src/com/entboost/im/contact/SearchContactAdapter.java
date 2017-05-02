package com.entboost.im.contact;

import java.util.ArrayList;
import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.SearchResultInfo;
import net.yunim.utils.YIResourceUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.global.MyApplication;
import com.entboost.im.group.DepartmentInfoActivity;
import com.entboost.im.group.MemberInfoActivity;
import com.entboost.im.group.MemberInfoViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SearchContactAdapter extends BaseAdapter {
	private List<SearchResultInfo> list = new ArrayList<SearchResultInfo>();
	private Context mContext;
	private LayoutInflater mInflater;

	public SearchContactAdapter(Context context, LayoutInflater mInflater, List<SearchResultInfo> list) {
		this.mContext = context;
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	public List<SearchResultInfo> getList() {
		return list;
	}

	public void addSearchResultInfo(List<SearchResultInfo> searchResultInfos) {
		list.addAll(searchResultInfos);
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return this.list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MemberInfoViewHolder holder;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = mInflater.inflate(R.layout.item_user, parent, false);
			// 减少findView的次数
			holder = new MemberInfoViewHolder();
			// 初始化布局中的元素
			holder.userImg = ((ImageView) convertView.findViewById(R.id.user_head));
			holder.itemsText = ((TextView) convertView.findViewById(R.id.user_name));
			holder.description = ((TextView) convertView.findViewById(R.id.user_description));
			convertView.setTag(holder);
		} else {
			holder = (MemberInfoViewHolder) convertView.getTag();
		}

		// 获取该行的数据
		final SearchResultInfo obj = (SearchResultInfo) getItem(position);
		if (obj != null) {
			holder.itemsText.setText(obj.getName());
			holder.description.setText(obj.getSrc());
			if (obj.getType() == SearchResultInfo.TYPE_USERCHAT) {
				MemberInfo memberInfo = (MemberInfo) obj.getObj();
				Bitmap img = YIResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
				if (img != null) {
					holder.userImg.setImageBitmap(img);
				} else {
					ImageLoader.getInstance().displayImage(memberInfo.getHeadUrl(), holder.userImg, MyApplication.getInstance().getUserImgOptions());
				}
			} else if (obj.getType() == SearchResultInfo.TYPE_CONTACTCHAT) {
				ContactInfo contactInfo = (ContactInfo) obj.getObj();
				Bitmap img = YIResourceUtils.getHeadBitmap(contactInfo.getHead_rid());
				if (img != null) {
					holder.userImg.setImageBitmap(img);
				} else {
					ImageLoader.getInstance().displayImage(contactInfo.getHeadUrl(), holder.userImg, MyApplication.getInstance().getUserImgOptions());
				}
			}
			
			holder.userImg.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (obj.getType() == SearchResultInfo.TYPE_GROUPCHAT) {
						GroupInfo departmentInfo = (GroupInfo) obj.getObj();
						Intent intent = new Intent(mContext, DepartmentInfoActivity.class);
						intent.putExtra("depid", departmentInfo.getDep_code());
						mContext.startActivity(intent);
					} else if (obj.getType() == SearchResultInfo.TYPE_CONTACTCHAT) {
						ContactInfo contactInfo = (ContactInfo) obj.getObj();
						Intent intent = new Intent(mContext, ContactInfoActivity.class);
						intent.putExtra("con_id", contactInfo.getCon_id());
						//intent.putExtra("contact", contactInfo.getContact());
						mContext.startActivity(intent);
					} else if (obj.getType() == SearchResultInfo.TYPE_USERCHAT) {
						MemberInfo memberInfo = (MemberInfo) obj.getObj();
						Intent intent = new Intent(mContext, MemberInfoActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("memberInfo", memberInfo);
						intent.putExtras(bundle);
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
							intent.putExtra("selfFlag", true);
						}
						mContext.startActivity(intent);
					}
				}
			});
		}
		
		// 图片的下载
		// mAbImageDownloader.display(holder.itemsIcon, null);

		return convertView;
	}

}
