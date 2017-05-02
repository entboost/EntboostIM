package com.entboost.im.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.entboost.im.R;

/**
 * 禁言时间选择
 */
public class ForbidMinutesAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	/**
	 * Object[]: [0]=禁言时长(分钟)，[1]=名称或描述
	 */
	private List<Object[]> list=new ArrayList<Object[]>();
	/**
	 * 选中的禁言时长(分钟)
	 */
	private Integer selectedForbidMinutes;
	
	public void setSelectedForbidMinutes(Integer selectedForbidMinutes) {
		this.selectedForbidMinutes = selectedForbidMinutes;
	}

	public Integer getSelectedForbidMinutes() {
		return selectedForbidMinutes;
	}

	private Object[] createData(int minutes, String name) {
		Object[] objs = new Object[2];
		objs[0] = minutes;
		objs[1] = name;
		return objs;
	}
	
	public ForbidMinutesAdapter(Context context) {
		super();
		
		list.add(createData(10, "10分钟"));
		list.add(createData(30, "30分钟"));
		list.add(createData(60, "1小时"));
		list.add(createData(60*2, "2小时"));
		list.add(createData(60*12, "12小时"));
		list.add(createData(60*24, "1天"));
		list.add(createData(60*24*2, "2天"));
		list.add(createData(60*24*7, "7天"));
		list.add(createData(0, "永久"));
		//list.add(createData(-2, "自定义"));
		
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
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
			convertView = mInflater.inflate(R.layout.item_forbid_minutes, parent, false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.name = ((TextView) convertView.findViewById(R.id.name));
			holder.user_select=((ImageButton) convertView.findViewById(R.id.user_select));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Object[] objs = (Object[])getItem(position);
		holder.name.setText((String)objs[1]);
		
		//判断是否已被选中
		if (selectedForbidMinutes!=null && selectedForbidMinutes-(Integer)objs[0]==0)
			holder.user_select.setImageResource(R.drawable.uitb_57);
		else
			holder.user_select.setImageDrawable(null);

		return convertView;
	}
	
	/**
	 * View元素
	 */
	static class ViewHolder {
		TextView name;
		ImageButton user_select;
	}
}
