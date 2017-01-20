package com.entboost.ui.base.view.popmenu;

import android.view.View;

public class PopMenuItem {
	public String text;
	public int img_resId;
	public int endImg_resId;
	public int layout_resId;
	public PopMenuItemOnClickListener listener;
	public View.OnClickListener endListener;

	public PopMenuItem(String text,int img_resId,int layout_resId,
			PopMenuItemOnClickListener listener) {
		super();
		this.text = text;
		this.img_resId = img_resId;
		this.listener = listener;
		this.layout_resId=layout_resId;
	}
	
	public PopMenuItem(String text,PopMenuItemOnClickListener listener) {
		super();
		this.text = text;
		this.listener = listener;
	}
	
	public PopMenuItem(String text,int endImg_resId,int layout_resId,
			PopMenuItemOnClickListener listener,
			View.OnClickListener endListener) {
		super();
		this.text = text;
		this.endImg_resId = endImg_resId;
		this.listener = listener;
		this.endListener=endListener;
		this.layout_resId=layout_resId;
	}
	
	public PopMenuItem(String text,int endImg_resId,
			PopMenuItemOnClickListener listener) {
		super();
		this.text = text;
		this.endImg_resId = endImg_resId;
		this.listener = listener;
	}
	
	
	public PopMenuItem(PopMenuItemOnClickListener listener) {
		super();
		this.listener = listener;
	}
}
