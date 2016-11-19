package com.entboost.im.chat;

import java.io.File;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.entboost.im.R;

public class FileAdapter extends BaseAdapter{

	private Vector<String> list=new Vector<String>();
	private LayoutInflater mInflater;
	
	public void setInput(Vector<String> list){
		this.list.clear();
		this.list.addAll(list);
	}
	
	public FileAdapter(Context context) {
		super();
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
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
			convertView = mInflater.inflate(R.layout.item_file, parent, false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.fileName = ((TextView) convertView
					.findViewById(R.id.fileName));
			holder.user_select=((ImageButton) convertView
					.findViewById(R.id.user_select));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String fileName=String.valueOf(getItem(position));
		holder.fileName.setText(StringUtils.substringAfterLast(fileName, File.separator));
		return convertView;
	}

	/**
	 * View元素
	 */
	static class ViewHolder {
		TextView fileName;
		ImageButton user_select;
	}

}
