package com.entboost.im.message;

import net.yunim.eb.signlistener.EntboostIMListener;
import net.yunim.service.Entboost;
import net.yunim.service.EntboostCache;
import net.yunim.service.entity.DynamicNews;
import net.yunim.service.entity.FuncInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.entboost.im.MainActivity;
import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.base.EbMainActivity;
import com.entboost.im.chat.CallListActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.function.FunctionMainActivity;
import com.entboost.ui.base.view.popmenu.PopMenu;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;

public class MessageListFragment extends EbFragment {

	private ListView mAbPullListView;
	private MessageAdapter dynamicNewsAdapter;
	private EntboostIMListener listener;

	public MessageAdapter getDynamicNewsAdapter() {
		return dynamicNewsAdapter;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshPage();
	}

	/**
	 * 刷新变更
	 */
	public void refreshPage() {
		if (dynamicNewsAdapter != null) {
			dynamicNewsAdapter.setList(EntboostCache.getHistoryMsgList());
			dynamicNewsAdapter.notifyDataSetChanged();
			int noReadNums = EntboostCache.getUnreadNumDynamicNews();
			if (noReadNums > 0) {
				if (noReadNums<100)
					((EbMainActivity) activity).mBottomTabView.getItem(0).showTip(noReadNums);
				else 
					((EbMainActivity) activity).mBottomTabView.getItem(0).showTip("99+");
			} else {
				((EbMainActivity) activity).mBottomTabView.getItem(0).hideTip();
			}
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = onCreateEbView(R.layout.fragment_message_list, inflater, container);
		mAbPullListView = (ListView) view.findViewById(R.id.mListView);
		dynamicNewsAdapter = new MessageAdapter(view.getContext(), inflater, EntboostCache.getHistoryMsgList());
		mAbPullListView.setAdapter(dynamicNewsAdapter);
		mAbPullListView.setLongClickable(true);
		
		final PopMenu popMenu = new PopMenu(view.getContext());
		popMenu.addItem("删除该聊天",R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				EntboostCache.delDynamicNews((DynamicNews) popMenu.getObj());
				dynamicNewsAdapter.setList(EntboostCache.getHistoryMsgList());
				dynamicNewsAdapter.notifyDataSetChanged();
				((MainActivity) activity).mBottomTabView.getItem(0).showTip(EntboostCache.getUnreadNumDynamicNews());
			}
		});
		
		popMenu.addItem("删除所有聊天",R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				EntboostCache.clearAllMsgHistory();
				dynamicNewsAdapter.setList(EntboostCache.getHistoryMsgList());
				dynamicNewsAdapter.notifyDataSetChanged();
				((MainActivity) activity).mBottomTabView.getItem(0).showTip(EntboostCache.getUnreadNumDynamicNews());
			}
		});
		
		mAbPullListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				DynamicNews newsInfo = (DynamicNews) dynamicNewsAdapter.getItem(arg2);
				popMenu.setObj(newsInfo);
				popMenu.showCenter(view);
				return true;
			}
		});
		
		// item被点击事件
		mAbPullListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 获取该行的数据
				DynamicNews newsInfo = (DynamicNews) dynamicNewsAdapter.getItem(position);
				if (newsInfo != null) {
					//标记当前选中的会话为已读状态
					//newsInfo.readAll();
					EntboostCache.markReadDynamicNewsById(newsInfo.getId());
					
					//读取并在底部导航栏显示全部未读消息的总数量
					int noReadNums = EntboostCache.getUnreadNumDynamicNews();
					if (noReadNums > 0) {
						((EbMainActivity) activity).mBottomTabView.getItem(0).showTip(noReadNums);
					} else {
						((EbMainActivity) activity).mBottomTabView.getItem(0).hideTip();
					}
					
					if (newsInfo.getType() == DynamicNews.TYPE_GROUPCHAT || newsInfo.getType() == DynamicNews.TYPE_USERCHAT) { //聊天消息
						Intent intent = new Intent(MessageListFragment.this.getActivity(), ChatActivity.class);
						if (newsInfo.getType() == DynamicNews.TYPE_GROUPCHAT) {
							intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
						}
						intent.putExtra(ChatActivity.INTENT_TITLE, newsInfo.getTitle());
						intent.putExtra(ChatActivity.INTENT_UID, newsInfo.getSender());
						startActivity(intent);
					} else if (newsInfo.getType() == DynamicNews.TYPE_CALL) { //被邀请加入聊天的通知
						Intent intent = new Intent(MessageListFragment.this.getActivity(), CallListActivity.class);
						startActivity(intent);
					} else if (newsInfo.getType() == DynamicNews.TYPE_MYMESSAGE || newsInfo.getType() == DynamicNews.TYPE_BMESSAGE) { //我的消息(包括系统消息和广播消息)
						FuncInfo funcInfo = EntboostCache.getMessageFuncInfo();
						if (funcInfo != null) {
							Intent intent = new Intent(activity, FunctionMainActivity.class);
							intent.putExtra("funcInfo", funcInfo);
							intent.putExtra("tab_type", newsInfo.getType() == DynamicNews.TYPE_MYMESSAGE?FuncInfo.SYS_MSG:FuncInfo.BC_MSG);
							startActivity(intent);
						}
//						Intent intent = new Intent(MessageListFragment.this.getActivity(), BroadcastMessageListActivity.class);
//						startActivity(intent);
					}
				}
			}
		});
		
		listener = new EntboostIMListener() {
			@Override
			public void onReceiveDynamicNews(DynamicNews news) {
				dynamicNewsAdapter.setList(EntboostCache.getHistoryMsgList());
				dynamicNewsAdapter.notifyDataSetChanged();
			}
			@Override
			public void onDynamicNewsChanged(Long otherSideId) {
				dynamicNewsAdapter.setList(EntboostCache.getHistoryMsgList());
				dynamicNewsAdapter.notifyDataSetChanged();
			}
			
		};
		Entboost.addListener(listener);
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		Entboost.removeListener(listener);
		super.onDestroy();
	}
}
