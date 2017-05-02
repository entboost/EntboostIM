package com.entboost.im.group;

import net.yunim.service.EntboostUM;
import net.yunim.service.entity.Resource;
import net.yunim.service.listener.SetHeadListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.UIUtils;

public class SelectHeadImgActivity extends EbActivity {

	private static String LONG_TAG = SelectHeadImgActivity.class.getName();
	
	private GridView headGriView;
	private HeadImageAdapter headImageAdapter;
	private long memberCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_select_head_img);
		
		memberCode = getIntent().getLongExtra("memberCode", -1);
		headGriView = (GridView) this.findViewById(R.id.headGridView);
		headImageAdapter = new HeadImageAdapter(this);
		headGriView.setAdapter(headImageAdapter);
		
		headGriView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Resource head = (Resource) headImageAdapter.getItem(position);
				showProgressDialog("正在设置头像");
				EntboostUM.setUserHead(head.getRes_id(), memberCode, new SetHeadListener() {
					@Override
					public void onFailure(int code, String errMsg) {
						Log4jLog.e(LONG_TAG, errMsg + "(" + code + ")");
						
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
								UIUtils.showToast(SelectHeadImgActivity.this, "设置头像失败！");
							}
						});
					}

					@Override
					public void onSetHeadSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
								SelectHeadImgActivity.this.finish();
							}
						});
					}
				});
			}
		});
	}

}
