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

public class InfoMobileEditActivity extends EbActivity {
	@ViewInject(R.id.infoMobile)
	private EditText infoMobile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_info_mobile_edit);
		ViewUtils.inject(this);
		AccountInfo user = EntboostCache.getUser();
		infoMobile.setText(user.getMobile());
	}
	
	@OnClick(R.id.infoMobile_save_btn)
	public void save(View view) {
		String sinfoMobile=infoMobile.getText().toString();
		showProgressDialog("修改电话号码");
		EntboostUM.editUserInfo(null, null, -1, null, -1, null, -1,
				null, -1, null, null, null, -1, null, sinfoMobile, null, -1, null, null, new EditInfoListener() {
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
	
	@OnClick(R.id.infoMobile_cancel_btn)
	public void cancel(View view){
		finish();
	}

}
