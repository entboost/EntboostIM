package com.entboost.im.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.yunim.service.entity.ContactGroup;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.comparator.ContactGroupComparator;

public class ContactGroupSelectAdapter extends BaseAdapter {
	private Context mContext;

	private List<ContactGroup> groups = new ArrayList<ContactGroup>();

	private long ug_id = 0l;
	private ImageButton selectImg;

	public ImageButton getSelectImg() {
		return selectImg;
	}

	public void setSelectImg(ImageButton selectImg) {
		this.selectImg = selectImg;
	}

	public long getUg_id() {
		return ug_id;
	}
	
	public void setUg_id(long ug_id) {
		this.ug_id = ug_id;
	}

	public ContactGroupSelectAdapter(Context context) {
		mContext = context;
	}

	public void setInput(List<ContactGroup> groups) {
		this.groups.clear();
		
		if (groups!=null) {
			Collections.sort(groups, new ContactGroupComparator());
			this.groups.addAll(groups);
		}
	}

	private class GroupViewHolder {
		TextView itemsText;
		ImageButton user_select;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Object getItem(int position) {
		return groups.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ContactGroup group = (ContactGroup) getItem(position);
		final GroupViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_selectcontactgroup, parent, false);
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.itemsText = ((TextView) convertView.findViewById(R.id.user_name));
			holder1.user_select = ((ImageButton) convertView.findViewById(R.id.user_select));
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
		}
		holder1.itemsText.setText(group.getGroupname());
		holder1.user_select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectImg != null) {
					selectImg.setImageDrawable(null);
				}
				selectImg = holder1.user_select;
				if (ug_id - group.getUgid() == 0) {
					ug_id = 0;
					holder1.user_select.setImageDrawable(null);
				} else {
					ug_id = group.getUgid();
					holder1.user_select.setImageResource(R.drawable.uitb_57);
				}
			}
		});
		return convertView;
	}

}
