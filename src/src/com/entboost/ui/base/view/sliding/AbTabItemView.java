/*
 * Copyright (C) 2015 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.entboost.ui.base.view.sliding;

import java.security.SecureRandom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * Copyright (c) 2012 All rights reserved 名称：TabView.java 描述：表示一个TAB
 * 
 * @author amsoft.cn
 * @date：2013-11-25 下午6:02:43
 * @version v1.0
 */
public class AbTabItemView extends RelativeLayout {

	private Context mContext;
	// 当前的索引
	private int mIndex;
	// 包含的TextView
	private TextView mTextView;
	// 未读信息的TextView
	private TextView unReadTextView;

	public AbTabItemView(Context context) {
		this(context, null);
	}

	public AbTabItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.setGravity(Gravity.CENTER);
		this.setPadding(12, 15, 12, 10);
		mTextView = new TextView(context);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setPadding(5, 5, 5, 5);
		mTextView.setId(new SecureRandom().nextInt(100));
		mTextView.setFocusable(true);
		mTextView.setSingleLine();
		this.addView(mTextView, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		unReadTextView = new TextView(context);
		unReadTextView.setTextColor(Color.WHITE);
		unReadTextView.setTextSize(8);
		unReadTextView.setGravity(Gravity.CENTER);
		unReadTextView.setVisibility(View.INVISIBLE);
		RelativeLayout.LayoutParams unReadTextViewLayout = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		unReadTextViewLayout.addRule(RelativeLayout.ALIGN_RIGHT,
				mTextView.getId());
		this.addView(unReadTextView, unReadTextViewLayout);
	}

	public void initTip(int resid) {
		unReadTextView.setBackgroundResource(resid);
	}

	public void showTip(int num) {
		if (num == 0) {
			unReadTextView.setVisibility(View.INVISIBLE);
		} else {
			unReadTextView.setVisibility(View.VISIBLE);
			unReadTextView.setText(String.valueOf(num));
		}
	}
	
	public void showTip(String formatedNum) {
		if (formatedNum!=null && formatedNum.length()>0) {
			unReadTextView.setVisibility(View.VISIBLE);
			unReadTextView.setText(formatedNum);
		} else {
			unReadTextView.setVisibility(View.INVISIBLE);
		}
	}

	public void hideTip() {
		unReadTextView.setVisibility(View.INVISIBLE);
	}

	public void init(int index, String text) {
		mIndex = index;
		mTextView.setText(text);
	}

	public int getIndex() {
		return mIndex;
	}

	public TextView getTextView() {
		return mTextView;
	}

	/**
	 * 
	 * 描述：设置文字大小
	 * 
	 * @param tabTextSize
	 * @throws
	 */
	public void setTabTextSize(int tabTextSize) {
		mTextView.setTextSize(tabTextSize);
	}

	/**
	 * 
	 * 描述：设置文字颜色
	 * 
	 * @param tabTextSize
	 * @throws
	 */
	public void setTabTextColor(int tabColor) {
		mTextView.setTextColor(tabColor);
	}

	/**
	 * 
	 * 描述：设置文字图片
	 * 
	 * @throws
	 */
	public void setTabCompoundDrawables(Drawable left, Drawable top,
			Drawable right, Drawable bottom) {
		if (left != null) {
			left.setBounds(0, 0, left.getIntrinsicWidth(),
					left.getIntrinsicHeight());
		}
		if (top != null) {
			top.setBounds(0, 0, top.getIntrinsicWidth(),
					top.getIntrinsicHeight());
		}
		if (right != null) {
			right.setBounds(0, 0, right.getIntrinsicWidth(),
					right.getIntrinsicHeight());
		}
		if (bottom != null) {
			bottom.setBounds(0, 0, bottom.getIntrinsicWidth(),
					bottom.getIntrinsicHeight());
		}
		mTextView.setCompoundDrawables(left, top, right, bottom);
	}

	/**
	 * 
	 * 描述：设置tab的背景选择
	 * 
	 * @param resid
	 * @throws
	 */
	public void setTabBackgroundResource(int resid) {
		this.setBackgroundResource(resid);
	}

	/**
	 * 
	 * 描述：设置tab的背景选择
	 * 
	 * @param resid
	 * @throws
	 */
	public void setTabBackgroundDrawable(Drawable d) {
		this.setBackgroundDrawable(d);
	}

}