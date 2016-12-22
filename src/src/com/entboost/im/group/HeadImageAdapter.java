package com.entboost.im.group;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.Resource;
import net.yunim.utils.YIResourceUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.entboost.im.R;

public class HeadImageAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	public HeadImageAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return EntboostCache.getHeadsSize();
	}

	@Override
	public Object getItem(int position) {
		return EntboostCache.getHeadByPosition(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final HeadViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gridview_head_item, null);
			viewHolder = new HeadViewHolder();
			viewHolder.img = (ImageView) convertView.findViewById(R.id.headImg);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (HeadViewHolder) convertView.getTag();
		}
		viewHolder.img.setImageBitmap(YIResourceUtils.getHeadBitmap(((Resource) getItem(position)).getRes_id()));
		return convertView;
	}

	static class HeadViewHolder {
		public ImageView img;
	}
}
