package com.entboost.im.setting;

import net.yunim.service.EntboostLC;
import net.yunim.service.entity.ClientVer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.AppUtils;
import com.entboost.utils.AbDateUtil;
import com.lidroid.xutils.ViewUtils;

public class AppVerActivity extends EbActivity {
	
	/** The tag. */
	private static String TAG = AppVerActivity.class.getSimpleName();
	private static String LONG_TAG = AppVerActivity.class.getName();

	private String clientVer;
	private ClientVer cVer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_app_ver);
		ViewUtils.inject(this);
		clientVer = AppUtils.getVersion(AppVerActivity.this);
		cVer = (ClientVer) getIntent().getSerializableExtra("cVer");
		initView();
	}

	private void initView() {
		TextView oldVer_txt = (TextView) findViewById(R.id.oldVer_txt);
		TextView newVer_txt = (TextView) findViewById(R.id.newVer_txt);
		TextView newVerTime_txt = (TextView) findViewById(R.id.newVerTime_txt);
		TextView newVerContent_txt = (TextView) findViewById(R.id.newVerContent_txt);
		oldVer_txt.setText(clientVer);
		newVer_txt.setText(cVer.getServer_ver());
		newVerTime_txt.setText(AbDateUtil.formatDateStr2Desc(
				cVer.getUpdate_time(), AbDateUtil.dateFormatYMD));
		newVerContent_txt.setText(cVer.getDescription());
		findViewById(R.id.department_send_btn).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String url=cVer.getDownload_url();
						Log4jLog.e(LONG_TAG, url);
						Uri uri = Uri.parse(url);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
						finish();
						EntboostLC.exit();
					}
				});
	}

}
