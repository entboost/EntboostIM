package com.entboost.im.chat;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.entboost.im.R;
import com.entboost.ui.utils.AbBitmapUtils;

public class FullImageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_image);
		String imgFilePath = this.getIntent().getStringExtra("imgFilePath");
		ImageView imgView = (ImageView) findViewById(R.id.full_img);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if (StringUtils.isNotBlank(imgFilePath)) {
			imgView.setImageBitmap(AbBitmapUtils.getBitmap(dm.widthPixels,
					dm.heightPixels, imgFilePath));
		}
		imgView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out); 
			}
		});
	}
	
	

}
