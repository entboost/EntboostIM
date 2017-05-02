package com.entboost.im.user;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.AccountInfo;
import net.yunim.service.listener.EditInfoListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class InfoUrlEditActivity extends EbActivity {
	@ViewInject(R.id.infoUrl)
	private EditText infoUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_info_url_edit);
		ViewUtils.inject(this);
		AccountInfo user = EntboostCache.getUser();
		infoUrl.setText(user.getUrl());
	}
	
	@OnClick(R.id.infoUrl_save_btn)
	public void save(View view) {
		String sinfoUrl=infoUrl.getText().toString();
		showProgressDialog("修改用户主页");
		EntboostUM.editUserInfo(null, null, -1, null, -1, null, -1,
				null, -1, null, null, sinfoUrl, -1, null, null, null, -1, null, null, new EditInfoListener() {
			@Override
			public void onFailure(int code, final String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						pageInfo.showError(errMsg);
						removeProgressDialog();
					}
				});
			}
			
			@Override
			public void onEditInfoSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						removeProgressDialog();
						finish();
					}
				});
			}
		});
	}
	
	@OnClick(R.id.infoUrl_cancel_btn)
	public void cancel(View view){
		finish();
	}

}
