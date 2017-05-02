package com.entboost.im.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class SecuritySettingActivity extends EbActivity {
	
	private static String LONG_TAG = SecuritySettingActivity.class.getName();
	
	@ViewInject(R.id.s1_layout)
	private RelativeLayout s1_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAbContentView(R.layout.activity_security_setting);
		ViewUtils.inject(this);
		
		//绑定点击事件：修改密码
		s1_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Intent intent = new Intent(SecuritySettingActivity.this, ChangePWDActivity.class);
				startActivity(intent);
			}
		});
	}
	
}
