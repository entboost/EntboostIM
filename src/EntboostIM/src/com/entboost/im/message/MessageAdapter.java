package com.entboost.im.message;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.cache.EbCache;
import net.yunim.service.entity.ChatRoomRichMsg;
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

import com.entboost.Log4jLog;
import com.entboost.im.R;
import com.entboost.im.comparator.DynamicNewsCompare;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.UIUtils;
import com.entboost.utils.AbDateUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MessageAdapter extends BaseAdapter {
	
	private static String LONG_TAG = MessageAdapter.class.getName();
	
	//用户自定义消息类型，具体值要求"大于等于"ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_USER_DATA的值
	/**
	 * 用户自定义消息类型0，用于发送文本；第三方可根据自己需求进行含义变更
	 */
	public final static int UserDataType_0_Text = ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_USER_DATA + 0;
	/**
	 * 用户自定义消息类型1，用于发送字节数组；第三方可根据自己需求进行含义变更
	 */
	public final static int UserDataType_1_Bytes = ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_USER_DATA + 1;
	//===更多请自行定义...
	
	
	private Context mContext;
	// xml转View对象
	private LayoutInflater mInflater;
	private List<DynamicNews> list = new ArrayList<DynamicNews>();

	public MessageAdapter(Context context, LayoutInflater mInflater, List<DynamicNews> list) {
		this.mContext = context;
		// 用于将xml转为View
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (list!=null) {
			Collections.sort(list, new DynamicNewsCompare()); //排序
		}
		setList(list);
	}

	public void setList(List<DynamicNews> list) {
		this.list.clear();
		if (list!=null) {
			Collections.sort(list, new DynamicNewsCompare()); //排序
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

		// 设置未读数量角标
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
		
		//是否自定义广播消息；subType=[0-99]系统保留，其它由第三方自行定制
		boolean isCustomBMMessage = (dyn.getType() == DynamicNews.TYPE_BMESSAGE && dyn.getSubType()>=100);
		//是否用户自定义聊天消息
		boolean isUserCustomData = ((dyn.getType()==DynamicNews.TYPE_USERCHAT || dyn.getType()==DynamicNews.TYPE_GROUPCHAT) && dyn.getSubType()>=ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_USER_DATA);
		
		//显示消息标题
		if (isCustomBMMessage) { //自定义广播消息
			//holder.itemsTitle.setText("第三方可自定义标题[subType=" + dyn.getSubType() + "]");
			holder.itemsTitle.setText(dyn.getContent() /*+ "[subType=" + dyn.getSubType() + "]"*/);
		} else {
			holder.itemsTitle.setText(dyn.getTitle());
		}
		
		//显示消息预览
		if (dyn.getLastRetractMsgId()!=null && dyn.getLastRetractMsgId()>0 && dyn.getMsg_id()!=null && dyn.getLastRetractMsgId()-dyn.getMsg_id()==0)
			holder.itemsText.setText("[撤回一条消息]");
		else {
			//自定义广播消息
			if (isCustomBMMessage) {
				//holder.itemsText.setText("第三方可自定义内容");
				//holder.itemsText.setText(dyn.getContent());
				holder.itemsText.setText(dyn.getContentText());
			} else if (isUserCustomData) { //用户自定义聊天消息
				//用户自定义文本消息的例子
				if (dyn.getSubType() == MessageAdapter.UserDataType_0_Text) {
					//通过msgid获取聊天记录
					if (dyn.getMsg_id()!=null) {
						ChatRoomRichMsg msg = EntboostCache.getChatMsgByMsgId(dyn.getMsg_id());
						if (msg!=null && msg.getBinData()!=null) {
							String text = "";
							try {
								text =new String(msg.getBinData(), "utf-8");
							} catch (UnsupportedEncodingException e) {
								Log4jLog.e(LONG_TAG, e);
							}
							
							holder.itemsText.setText(text);
						}
					}
				} else if (dyn.getSubType() == MessageAdapter.UserDataType_1_Bytes) {
					//第三方自行实现
					holder.itemsText.setText("[图片]");
				}
				//其它自定义消息子类型...
			} else {
				holder.itemsText.setText(UIUtils.getTipCharSequence(mContext.getResources(), dyn.getContent(), true));
			}
		}
		
		//显示格式时间
		holder.itemsTime.setText(AbDateUtil.formatDateStr2Desc(AbDateUtil
				.getStringByFormat(dyn.getTime(), AbDateUtil.dateFormatYMDHMS), AbDateUtil.dateFormatYMDHMS));
		
		//显示头像
		if (dyn.getType() == DynamicNews.TYPE_GROUPCHAT) { //群组聊天
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
				 || dyn.getType() == DynamicNews.TYPE_MYSYSTEMMESSAGE) { //广播消息和系统消息
			//自定义广播消息
			if (isCustomBMMessage) {
				//根据业务[subType]自行使用定制头像
				holder.itemsIcon.setImageResource(R.drawable.message_head);
			} else {
				holder.itemsIcon.setImageResource(R.drawable.message_head);
			}
		} else { //一对一聊天
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
