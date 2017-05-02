package com.entboost.im.message;

import java.util.ArrayList;
import java.util.List;

import net.yunim.service.entity.BroadcastMessage;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.utils.AbDateUtil;

public class BroadcastMessageAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<BroadcastMessage> list = new ArrayList<BroadcastMessage>();

	public BroadcastMessageAdapter(Context context, List<BroadcastMessage> list) {
		// 用于将xml转为View
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setList(list);
	}

	public void setList(List<BroadcastMessage> list) {
		this.list.clear();
		this.list.addAll(list);
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = mInflater.inflate(R.layout.item_broadcast, parent,
					false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.itemsTitle = ((TextView) convertView
					.findViewById(R.id.msg_name));
			holder.itemsTime = ((TextView) convertView
					.findViewById(R.id.msg_time));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 获取该行的数据
		BroadcastMessage obj = (BroadcastMessage) getItem(position);
		holder.itemsTitle.setText(obj.getMsg_name());
		holder.itemsTime.setText(AbDateUtil.formatDateStr2Desc(AbDateUtil
				.getStringByFormat(obj.getSendTime(),
						AbDateUtil.dateFormatYMDHMS),
				AbDateUtil.dateFormatYMDHMS));
		return convertView;
	}

	/**
	 * View元素
	 */
	static class ViewHolder {
		TextView itemsTitle;
		TextView itemsTime;
	}
}
