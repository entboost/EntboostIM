package com.entboost.im.contact;

import java.util.List;
import java.util.Vector;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.SearchResultInfo;
import net.yunim.service.listener.SearchMemberListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.lidroid.xutils.ViewUtils;

public class SearchContactActivity extends EbActivity {

	private ListView mListView;
	private SearchContactAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_search_contact);
		
		ViewUtils.inject(this);
		mListView = (ListView) findViewById(R.id.mListView);
		adapter = new SearchContactAdapter(this, mInflater, new Vector<SearchResultInfo>());
		mListView.setAdapter(adapter);
		
		final EditText search = (EditText) findViewById(R.id.search);
		search.setHint("搜索：用户、联系人");
		ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pageInfo.showProgress("正在搜索用户、联系人");
				adapter.getList().clear();
				adapter.getList().addAll(EntboostCache.searchContact(search.getText().toString()));
				adapter.notifyDataSetChanged();
				
				EntboostUM.searchMember(search.getText().toString(), new SearchMemberListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								pageInfo.showError(errMsg);
							}
						});
					}
					
					@Override
					public void onSearchMemberSuccess(final List<SearchResultInfo> sri) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								pageInfo.hide();
								if (sri != null) {
									Vector<SearchResultInfo> temp = new Vector<SearchResultInfo>();
									temp.addAll(sri);
									adapter.addSearchResultInfo(temp);
									adapter.notifyDataSetChanged();
								}
							}
						});
					}
				});
			}
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 获取该行的数据
				SearchResultInfo info = (SearchResultInfo) adapter
						.getItem(position);
				if (info != null && info.getUid() != null) {
					Intent intent = new Intent(SearchContactActivity.this,
							ChatActivity.class);
					intent.putExtra(ChatActivity.INTENT_TITLE, info.getName());
					intent.putExtra(ChatActivity.INTENT_TOID, info.getUid());
					startActivity(intent);
				}
			}
		});
	}
}
