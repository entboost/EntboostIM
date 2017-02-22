package com.entboost.im.setting;

import java.util.Map;

import net.yunim.service.EntboostCache;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.entboost.Log4jLog;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.AppUtils;
import com.entboost.im.global.VersionUtils;
import com.entboost.im.push.HuaweiResolveErrorActivity;
import com.entboost.im.push.ThirdPartyPushHelper;
import com.entboost.im.ui.SegmentedRadioGroup;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class CheckVersionActivity extends EbActivity {
	
	private static String LONG_TAG = CheckVersionActivity.class.getName();
	
	@ViewInject(R.id.s2_segment_text)
	private SegmentedRadioGroup s2_segment_text;
	@ViewInject(R.id.s1_layout)
	private RelativeLayout s1_layout;
	@ViewInject(R.id.s2_layout)
	private RelativeLayout s2_layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAbContentView(R.layout.activity_check_version);
		ViewUtils.inject(this);
		
		//绑定点击事件：检查应用版本
		s1_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				String clientVer = AppUtils.getVersion(v.getContext());
				VersionUtils.checkApkVer(clientVer, v.getContext(), null);
			}
		});
		
		
		//设置是否允许安装升级华为推送服务
		Map<Long, Integer> pushSslIds = EntboostCache.getPushSslIds();
		if (pushSslIds.get(ThirdPartyPushHelper.SSLID_HUAWEI)==null) {
			s2_layout.setVisibility(View.GONE);
			return;
		}
		
		//配置文件存储实例
		final SharedPreferences preferences = getSharedPreferences("first", Context.MODE_PRIVATE);
		
		RadioButton s2_open = (RadioButton) s2_segment_text.findViewById(R.id.s2_open);
		RadioButton s2_close = (RadioButton) s2_segment_text.findViewById(R.id.s2_close);
		
		boolean s2 = preferences.getBoolean(HuaweiResolveErrorActivity.ENABLE_SHOW_HUAWEI_RESOLVE_ERROR_VIEW, true);
		if (s2)
			s2_open.setChecked(true);
		else 
			s2_close.setChecked(true);
		
		//处理控件选中变更事件
		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// 获取变更后的选中项的ID
				int radioButtonId = group.getCheckedRadioButtonId();
				// 根据ID获取RadioButton的实例
				RadioButton rb = (RadioButton)findViewById(radioButtonId);
				// 根据ID获取它对应的名称
				//String idName = getResources().getResourceEntryName(radioButtonId);
				
				boolean open = Boolean.valueOf((String)rb.getTag());
				Log4jLog.d(LONG_TAG, "isOpen:" + open);
				
				//编辑模式
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(HuaweiResolveErrorActivity.ENABLE_SHOW_HUAWEI_RESOLVE_ERROR_VIEW, open);
				editor.commit();
			}
		};
		
		s2_segment_text.setOnCheckedChangeListener(listener);
	}
	
}
