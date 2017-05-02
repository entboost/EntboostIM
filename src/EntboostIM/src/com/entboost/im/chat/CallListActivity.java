package com.entboost.im.chat;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.CardInfo;
import android.os.Bundle;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;

public class CallListActivity extends EbActivity {

	private ListView listView;
	private CallAdapter callAdapter;

	/**
	 * 呼叫超时
	 */
	@Override
	public void onCallTimeout(CardInfo arg0) {
		super.onCallTimeout(arg0);
		callAdapter.setList(EntboostCache.getAllDealCallInfos());
		callAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_call_list);
		listView = (ListView) findViewById(R.id.mListView);
		callAdapter = new CallAdapter(this, EntboostCache.getAllDealCallInfos());
		listView.setAdapter(callAdapter);
	}

}
