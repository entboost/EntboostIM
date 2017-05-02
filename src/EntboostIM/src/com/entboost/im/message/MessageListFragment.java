package com.entboost.im.message;

import net.yunim.eb.signlistener.EntboostIMListener;
import net.yunim.service.Entboost;
import net.yunim.service.EntboostCache;
import net.yunim.service.entity.BroadcastMessage;
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

import com.entboost.Log4jLog;
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
	
	private static String LONG_TAG = MessageListFragment.class.getName();

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
			dynamicNewsAdapter.setList(EntboostCache.getDynamicNewsList());
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
		dynamicNewsAdapter = new MessageAdapter(view.getContext(), inflater, EntboostCache.getDynamicNewsList());
		mAbPullListView.setAdapter(dynamicNewsAdapter);
		mAbPullListView.setLongClickable(true);
		
		final PopMenu popMenu = new PopMenu(view.getContext());
		popMenu.addItem("删除该聊天",R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				EntboostCache.deleteDynamicNews((DynamicNews) popMenu.getObj(), true);
				dynamicNewsAdapter.setList(EntboostCache.getDynamicNewsList());
				dynamicNewsAdapter.notifyDataSetChanged();
				((MainActivity) activity).mBottomTabView.getItem(0).showTip(EntboostCache.getUnreadNumDynamicNews());
			}
		});
		
		popMenu.addItem("删除所有聊天",R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				EntboostCache.clearAllDynamicNews(true);
				dynamicNewsAdapter.setList(EntboostCache.getDynamicNewsList());
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
						intent.putExtra(ChatActivity.INTENT_TOID, newsInfo.getSender());
						startActivity(intent);
					} else if (newsInfo.getType() == DynamicNews.TYPE_CALL) { //被邀请加入聊天的通知
						Intent intent = new Intent(MessageListFragment.this.getActivity(), CallListActivity.class);
						startActivity(intent);
					} else if (newsInfo.getType() == DynamicNews.TYPE_MYMESSAGE || newsInfo.getType() == DynamicNews.TYPE_BMESSAGE) { //我的消息(包括系统消息和广播消息)
						//自定义广播消息
						//subType=[0-99]保留给系统使用，其它由第三方自行定制
						if (newsInfo.getType() == DynamicNews.TYPE_BMESSAGE && newsInfo.getSubType()>=100) {
							switch(newsInfo.getSubType()) {
							case 100:
								Log4jLog.i(LONG_TAG, newsInfo.getTitle()); //标题
								Log4jLog.i(LONG_TAG, newsInfo.getContent()); //自定义内容1
								Log4jLog.i(LONG_TAG, newsInfo.getContentText()); //自定义内容2
								if (newsInfo.getAssociate_id()!=null) {
									//获取关联的广播消息记录
									BroadcastMessage brmsg = EntboostCache.getBroadcastMessageById(newsInfo.getAssociate_id().intValue());
									if (brmsg!=null) {
										Log4jLog.i(LONG_TAG, brmsg.getMsg_name());
										Log4jLog.i(LONG_TAG, brmsg.getMsg_content());
									}
								}
								//自定义实现，例如：打开webview访问某个URL地址
								break;
							case 101:
								//自定义实现，参考上述
								break;
							default:
								//其它...
								break;
							}
						} else { //系统广播消息或系统消息
							//跳转到浏览系统消息和广播消息的页面
							FuncInfo funcInfo = EntboostCache.getMessageFuncInfo();
							if (funcInfo != null) {
								Intent intent = new Intent(activity, FunctionMainActivity.class);
								intent.putExtra("funcInfo", funcInfo);
								intent.putExtra("tab_type", newsInfo.getType() == DynamicNews.TYPE_MYMESSAGE?FuncInfo.SYS_MSG:FuncInfo.BC_MSG);
								startActivity(intent);
							}
//							Intent intent = new Intent(MessageListFragment.this.getActivity(), BroadcastMessageListActivity.class);
//							startActivity(intent);
						}
					}
				}
			}
		});
		
		listener = new EntboostIMListener() {
			@Override
			public void onReceiveDynamicNews(DynamicNews news) {
				dynamicNewsAdapter.setList(EntboostCache.getDynamicNewsList());
				dynamicNewsAdapter.notifyDataSetChanged();
			}
			@Override
			public void onDynamicNewsChanged(Long otherSideId) {
				dynamicNewsAdapter.setList(EntboostCache.getDynamicNewsList());
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
