package com.entboost.im.function;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import net.yunim.service.entity.FuncInfo;
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
import com.entboost.im.comparator.FuncInfoComparator;
import com.entboost.im.global.MyApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FunctionAdapter extends BaseAdapter {
	private Context mContext;
	// xml转View对象
	private Vector<FuncInfo> list = new Vector<FuncInfo>();

	public FunctionAdapter(Context context, List<FuncInfo> list) {
		this.mContext = context;
		
		if (list!=null) {
			Collections.sort(list, new FuncInfoComparator()); //排序
			this.list.addAll(list);
		}
	}

	public void setList(List<FuncInfo> list) {
		this.list.clear();
		
		if (list!=null) {
			Collections.sort(list, new FuncInfoComparator()); //排序
			this.list.addAll(list);
		}
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
		ViewHolder holder;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_funcinfo, parent, false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.itemsIcon = ((ImageView) convertView.findViewById(R.id.msg_head));
			holder.itemsTitle = ((TextView) convertView.findViewById(R.id.msg_name));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 获取该行的数据
		FuncInfo funcInfo = (FuncInfo) getItem(position);
		holder.itemsTitle.setText(funcInfo.getFunc_name());
		if (funcInfo.getIcon_res_id()>0) { //StringUtils.isNotBlank(funcInfo.getIcon_res_id())
			Bitmap img = YIResourceUtils.getHeadBitmap(Long.valueOf(funcInfo.getIcon_res_id()));
			if (img != null) {
				holder.itemsIcon.setImageBitmap(img);
			} else {
				//设置几个内置应用的图标
				if (funcInfo.getSub_id()==1002300102) {
					holder.itemsIcon.setImageResource(R.drawable.subid_1002300102);
				} else if (funcInfo.getSub_id()==1002300103) {
					holder.itemsIcon.setImageResource(R.drawable.message_head);
				} else if (funcInfo.getSub_id()==1002300104) {
					holder.itemsIcon.setImageResource(R.drawable.subid_1002300104);
				} else 
					ImageLoader.getInstance().displayImage(funcInfo.getIconUrl(), holder.itemsIcon, MyApplication.getInstance().getFuncInfoImgOptions());
			}
		}
		return convertView;
	}

	/**
	 * View元素
	 */
	private class ViewHolder {
		ImageView itemsIcon;
		TextView itemsTitle;
	}
}
