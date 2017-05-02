package com.entboost.im.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.entboost.Log4jLog;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.UIUtils;
import com.entboost.im.ui.SegmentedRadioGroup;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class NotificationSettingActivity extends EbActivity {

	private static String LONG_TAG = NotificationSettingActivity.class.getName();

	@ViewInject(R.id.s1_segment_text)
	private SegmentedRadioGroup s1_segment_text;
	@ViewInject(R.id.s2_segment_text)
	private SegmentedRadioGroup s2_segment_text;
	@ViewInject(R.id.s3_segment_text)
	private SegmentedRadioGroup s3_segment_text;
	@ViewInject(R.id.s4_segment_text)
	private SegmentedRadioGroup s4_segment_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAbContentView(R.layout.activity_notification_setting);
		ViewUtils.inject(this);
		
		//配置文件存储实例
		final SharedPreferences preferences = getSharedPreferences("notificationSetting", Context.MODE_PRIVATE);
		
		RadioButton s1_open = (RadioButton) s1_segment_text.findViewById(R.id.s1_open);
		RadioButton s1_close = (RadioButton) s1_segment_text.findViewById(R.id.s1_close);
		
		RadioButton s2_open = (RadioButton) s2_segment_text.findViewById(R.id.s2_open);
		RadioButton s2_close = (RadioButton) s2_segment_text.findViewById(R.id.s2_close);
		
		RadioButton s3_open = (RadioButton) s3_segment_text.findViewById(R.id.s3_open);
		RadioButton s3_close = (RadioButton) s3_segment_text.findViewById(R.id.s3_close);

		RadioButton s4_open = (RadioButton) s4_segment_text.findViewById(R.id.s4_open);
		RadioButton s4_close = (RadioButton) s4_segment_text.findViewById(R.id.s4_close);
		
		//接收新消息通知
		boolean s1 = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_NEW), true);
		if (s1)
			s1_open.setChecked(true);
		else 
			s1_close.setChecked(true);

		//通知显示消息详情
		boolean s2 = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_DETAILS), true);
		if (s2)
			s2_open.setChecked(true);
		else 
			s2_close.setChecked(true);
		
		//声音
		boolean s3 = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_SOUND), true);
		if (s3)
			s3_open.setChecked(true);
		else 
			s3_close.setChecked(true);
		
		//振动
		boolean s4 = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_VIBRATE), true);
		if (s4)
			s4_open.setChecked(true);
		else 
			s4_close.setChecked(true);
		
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
				
				String groupType = (String)group.getTag();
				boolean open = Boolean.valueOf((String)rb.getTag());
				Log4jLog.d(LONG_TAG, "groupType:"+ groupType + ", isOpen:" + open);
				
				//编辑模式
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(groupType, open);
				editor.commit();
			}
		};
		
		s1_segment_text.setOnCheckedChangeListener(listener);
		s2_segment_text.setOnCheckedChangeListener(listener);
		s3_segment_text.setOnCheckedChangeListener(listener);
		s4_segment_text.setOnCheckedChangeListener(listener);
	}
	
}
