package com.entboost.im.setting;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.constants.ConfigConstants;
import net.yunim.service.listener.InitAppKeyListener;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.MyApplication;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class SetLogonServiceAddrActivity extends EbActivity {
	@ViewInject(R.id.setService_name)
	private EditText setService_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_set_logon_service_addr);
		ViewUtils.inject(this);
		if (!ConfigConstants.LOGONCENTER_ADDR.equals(EntboostCache
				.getSharedLogonCenterAddr())) {
			((EditText) findViewById(R.id.setService_name))
					.setText(EntboostCache.getSharedLogonCenterAddr());
		}
	}

	@OnClick(R.id.setService_save_btn)
	public void save(View view) {
		final String name = setService_name.getText().toString();
		if (!StringUtils.equals(name, EntboostCache.getSharedLogonCenterAddr())) {
			showDialog("提示", "修改服务器地址，需要重新验证AppKey,确定需要修改吗？",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							showProgressDialog("正在修改服务器地址，并重新验证AppKey!");
							EntboostLC.initAPPKey(name, MyApplication.appid,
									MyApplication.appkey,
									new InitAppKeyListener() {

										@Override
										public void onFailure(String errMsg) {
											pageInfo.showError(errMsg);
											removeProgressDialog();
										}

										@Override
										public void onInitAppKeySuccess() {
											removeProgressDialog();
											finish();
										}
									});

						}

					});
		} else {
			finish();
		}
	}

	@OnClick(R.id.setService_cancel_btn)
	public void cancel(View view) {
		finish();
	}

}
