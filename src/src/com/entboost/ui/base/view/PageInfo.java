package com.entboost.ui.base.view;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.yunim.utils.YIImageLoader;

public class PageInfo extends LinearLayout {

	public int mPageInfoID = 112;
	private ImageView errorimg;
	private ImageView infoimg;
	private TextView infoText;
	
	public static final int TYPE_INFO=0;
	public static final int TYPE_ERROR=1;
	public static final int TYPE_PROGRESS=2;
	

	public PageInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPageInfo(context);
	}

	public PageInfo(Context context) {
		super(context);
		initPageInfo(context);
	}

	private void initPageInfo(Context context) {
		// 水平排列
		this.setOrientation(LinearLayout.HORIZONTAL);
		if(this.getId()<=0){
			this.setId(mPageInfoID);
		}
		this.setBackgroundColor(Color.rgb(255, 248, 215));
		this.setPadding(5, 5, 5, 5);
		this.setVisibility(View.GONE);

		errorimg = new ImageView(context);
		errorimg.setImageBitmap(YIImageLoader.getInstance().getBitmapFormSrc("image/error.png"));
		errorimg.setVisibility(View.GONE);
		this.addView(errorimg);

		infoimg = new ImageView(context);
		infoimg.setImageResource(android.R.drawable.presence_online);
		infoimg.setVisibility(View.GONE);
		this.addView(infoimg);

		infoText = new TextView(context);
		infoText.setPadding(5, 0, 0, 0);
		infoText.setTextColor(Color.rgb(136, 124, 124));
		infoText.setTextSize(12);
		this.addView(infoText);
	}

	public String getInfo() {
		return infoText.getText().toString();
	}

	public void showError(CharSequence text) {
//		if (errorimg.getVisibility() == View.VISIBLE) {
//			return;
//		}
		show(text,TYPE_ERROR);
	}

	public void showProgress(CharSequence text) {
		show(text,TYPE_PROGRESS);
	}
	
	public void show(CharSequence text,int type){
		this.setVisibility(View.VISIBLE);
		infoText.setText(text);
		errorimg.setVisibility(View.GONE);
		infoimg.setVisibility(View.GONE);
		if(type==TYPE_ERROR){
			errorimg.setVisibility(View.VISIBLE);
		}else if(type==TYPE_INFO){
			infoimg.setVisibility(View.VISIBLE);
		}
	}

	public void hide() {
		this.setVisibility(View.GONE);
		errorimg.setVisibility(View.GONE);
		infoimg.setVisibility(View.GONE);
	}

	public void showInfo(CharSequence text) {
		show(text,TYPE_INFO);
	}
	
	public void showInfo(final CharSequence text,final int time) {
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected void onPreExecute() {
				show(text,TYPE_INFO);
			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(time*1000);
				} catch (InterruptedException ex) {
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				hide();
			}
			
			
		}.execute();
	}

}
