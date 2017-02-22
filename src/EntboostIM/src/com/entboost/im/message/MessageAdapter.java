package com.entboost.im.message;

import java.util.ArrayList;
import java.util.List;

import net.yunim.service.cache.EbCache;
import net.yunim.service.entity.DynamicNews;
import net.yunim.service.entity.GroupInfo;
import net.yunim.utils.YIResourceUtils;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.UIUtils;
import com.entboost.utils.AbDateUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MessageAdapter extends BaseAdapter {

	private Context mContext;
	// xml转View对象
	private LayoutInflater mInflater;
	private List<DynamicNews> list = new ArrayList<DynamicNews>();

	public MessageAdapter(Context context, LayoutInflater mInflater, List<DynamicNews> list) {
		this.mContext = context;
		// 用于将xml转为View
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setList(list);
	}

	public void setList(List<DynamicNews> list) {
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
			convertView = mInflater.inflate(R.layout.item_msg_history, parent, false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.itemsCount = ((TextView) convertView.findViewById(R.id.unread_msg_num));
			holder.itemsIcon = ((ImageView) convertView.findViewById(R.id.msg_head));
			holder.itemsTitle = ((TextView) convertView.findViewById(R.id.msg_name));
			holder.itemsText = ((TextView) convertView.findViewById(R.id.msg_message));
			holder.itemsTime = ((TextView) convertView.findViewById(R.id.msg_time));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 获取该行的数据
		DynamicNews dyn = (DynamicNews) getItem(position);
		if (dyn.getNoReadNum() == 0) {
			holder.itemsCount.setVisibility(View.GONE);
		} else {
			holder.itemsCount.setVisibility(View.VISIBLE);
			if (dyn.getNoReadNum()<100)
				holder.itemsCount.setText(String.valueOf(dyn.getNoReadNum()));
			else 
				holder.itemsCount.setText("99+");
		}
		
		holder.itemsTitle.setText(dyn.getTitle());
		if (dyn.getLastRetractMsgId()!=null && dyn.getLastRetractMsgId()>0 && dyn.getMsg_id()!=null && dyn.getLastRetractMsgId()-dyn.getMsg_id()==0)
			holder.itemsText.setText("[撤回一条消息]");
		else 
			holder.itemsText.setText(UIUtils.getTipCharSequence(mContext.getResources(), dyn.getContent(), true));
		holder.itemsTime.setText(AbDateUtil.formatDateStr2Desc(AbDateUtil
				.getStringByFormat(dyn.getTime(), AbDateUtil.dateFormatYMDHMS), AbDateUtil.dateFormatYMDHMS));
		if (dyn.getType() == DynamicNews.TYPE_GROUPCHAT) {
			//设置部门/群组的头像
			GroupInfo groupInfo = EbCache.getInstance().getSysDataCache().getDepartmentInfo(dyn.getSender());
			if (groupInfo==null)
				groupInfo = EbCache.getInstance().getSysDataCache().getPersonGroupInfo(dyn.getSender());
			
			if (groupInfo!=null) {
				Context context = MyApplication.getInstance().getApplicationContext();
				Resources resources = context.getResources();
				int indentify = resources.getIdentifier(context.getPackageName()+":drawable/"+"group_head_"+groupInfo.getType(), null, null);
				holder.itemsIcon.setImageResource(indentify);
			} else 
				holder.itemsIcon.setImageResource(R.drawable.group_head_0);
		} else if (dyn.getType() == DynamicNews.TYPE_MYMESSAGE || dyn.getType() == DynamicNews.TYPE_BMESSAGE
				 || dyn.getType() == DynamicNews.TYPE_MYSYSTEMMESSAGE) {
			holder.itemsIcon.setImageResource(R.drawable.message_head);
		} else {
			Bitmap img = YIResourceUtils.getHeadBitmap(dyn.getHid());
			if (img != null) {
				holder.itemsIcon.setImageBitmap(img);
			} else {
				ImageLoader.getInstance().displayImage(dyn.getHeadUrl(), holder.itemsIcon, MyApplication.getInstance().getUserImgOptions());
			}
		}
		return convertView;
	}

	/**
	 * View元素
	 */
	private class ViewHolder {
		TextView itemsCount;
		ImageView itemsIcon;
		TextView itemsTitle;
		TextView itemsText;
		TextView itemsTime;
	}
}
