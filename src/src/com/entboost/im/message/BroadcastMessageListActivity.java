package com.entboost.im.message;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.BroadcastMessage;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;

public class BroadcastMessageListActivity extends EbActivity {

	private ListView msglistView;
	private BroadcastMessageAdapter msgAdapter;

	@Override
	public void onReceiveBroadcastMessage(BroadcastMessage message) {
		if (msgAdapter != null) {
			msgAdapter.setList(EntboostCache.getBroadcastMessages());
			msgAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_broadcast_message_list);
		msglistView = (ListView) findViewById(R.id.msglist);
		msgAdapter = new BroadcastMessageAdapter(this, EntboostCache.getBroadcastMessages());
		msglistView.setAdapter(msgAdapter);
		msglistView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 获取该行的数据
				BroadcastMessage msg = (BroadcastMessage) msgAdapter
						.getItem(position);
				Intent intent = new Intent(BroadcastMessageListActivity.this, BroadcastDetailActivity.class);
				intent.putExtra("broadcastMessage", msg);
				startActivity(intent);
			}
		});
	}

}
