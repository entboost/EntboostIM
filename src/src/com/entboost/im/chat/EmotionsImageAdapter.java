package com.entboost.im.chat;

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

public class EmotionsImageAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	public EmotionsImageAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return EntboostCache.getEmotionsSize();
	}

	@Override
	public Object getItem(int position) {
		return EntboostCache.getEmotionByPosition(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final EmotionsViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gridview_expression_item, null);
			viewHolder = new EmotionsViewHolder();
			viewHolder.img = (ImageView) convertView.findViewById(R.id.sendmsg_emotion);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (EmotionsViewHolder) convertView.getTag();
		}
		viewHolder.img.setImageBitmap(YIResourceUtils.getEmotionBitmap(((Resource) getItem(position)).getRes_id()));
		return convertView;
	}

	static class EmotionsViewHolder {
		public ImageView img;
	}
}
