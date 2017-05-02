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
package com.entboost.ui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.entboost.global.AbAppData;
import com.entboost.model.AbDisplayMetrics;
import com.entboost.utils.AbAppUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class AbViewUtil.
 */
public class AbViewUtil {

	/**
	 * 描述：重置AbsListView的高度. item 的最外层布局要用
	 * RelativeLayout,如果计算的不准，就为RelativeLayout指定一个高度
	 * 
	 * @param absListView
	 *            the abs list view
	 * @param lineNumber
	 *            每行几个 ListView一行一个item
	 * @param verticalSpace
	 *            the vertical space
	 */
	public static void setAbsListViewHeight(AbsListView absListView,
			int lineNumber, int verticalSpace) {

		int totalHeight = getAbsListViewHeight(absListView, lineNumber,
				verticalSpace);
		ViewGroup.LayoutParams params = absListView.getLayoutParams();
		params.height = totalHeight;
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		absListView.setLayoutParams(params);
	}

	/**
	 * 描述：获取AbsListView的高度.
	 * 
	 * @param absListView
	 *            the abs list view
	 * @param lineNumber
	 *            每行几个 ListView一行一个item
	 * @param verticalSpace
	 *            the vertical space
	 */
	public static int getAbsListViewHeight(AbsListView absListView,
			int lineNumber, int verticalSpace) {
		int totalHeight = 0;
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		absListView.measure(w, h);
		ListAdapter mListAdapter = absListView.getAdapter();
		if (mListAdapter == null) {
			return totalHeight;
		}

		int count = mListAdapter.getCount();
		if (absListView instanceof ListView) {
			for (int i = 0; i < count; i++) {
				View listItem = mListAdapter.getView(i, null, absListView);
				listItem.measure(w, h);
				totalHeight += listItem.getMeasuredHeight();
			}
			if (count == 0) {
				totalHeight = verticalSpace;
			} else {
				totalHeight = totalHeight
						+ (((ListView) absListView).getDividerHeight() * (count - 1));
			}

		} else if (absListView instanceof GridView) {
			int remain = count % lineNumber;
			if (remain > 0) {
				remain = 1;
			}
			if (mListAdapter.getCount() == 0) {
				totalHeight = verticalSpace;
			} else {
				View listItem = mListAdapter.getView(0, null, absListView);
				listItem.measure(w, h);
				int line = count / lineNumber + remain;
				totalHeight = line * listItem.getMeasuredHeight() + (line - 1)
						* verticalSpace;
			}

		}
		return totalHeight;

	}

	/**
	 * 测量这个view，最后通过getMeasuredWidth()获取宽度和高度.
	 * 
	 * @param v
	 *            要测量的view
	 * @return 测量过的view
	 */
	public static void measureView(View v) {
		ViewGroup.LayoutParams p = v.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		v.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * 
	 * 描述：dip转换为px
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 * @throws
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 
	 * 描述：px转换为dip
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 * @throws
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 描述：根据屏幕大小缩放.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 * @param size
	 * @return
	 */
	public static int resize(Context context, float pxValue) {
		AbDisplayMetrics mDisplayMetrics = AbAppUtil.getDisplayMetrics(context);
		return resize(mDisplayMetrics.displayWidth,
				mDisplayMetrics.displayHeight, pxValue);
	}

	/**
	 * 描述：根据屏幕大小缩放.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 * @param size
	 * @return
	 */
	public static int resize(int displayWidth, int displayHeight, float pxValue) {
		float scale = 1;
		try {
			float scaleWidth = (float) displayWidth / AbAppData.uiWidth;
			float scaleHeight = (float) displayHeight / AbAppData.uiHeight;
			scale = Math.min(scaleWidth, scaleHeight);
		} catch (Exception e) {
		}
		return Math.round(pxValue * scale);
	}

	/**
	 * 适配大小
	 */
	public static float getDipSize(Context context, float pxSize) {
		Resources mResources = context.getResources();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxSize,
				mResources.getDisplayMetrics());
	}

	/**
	 * 适配大小
	 */
	public static float getDipSize(float pxSize) {
		Resources mResources = Resources.getSystem();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxSize,
				mResources.getDisplayMetrics());
	}

	public static void setPadding(Context context, View view, int left,
			int top, int right, int bottom) {
		int paramLeft = resize(context, left);
		int paramTop = resize(context, top);
		int paramRight = resize(context, right);
		int paramBottom = resize(context, bottom);
		view.setPadding(paramLeft, paramTop, paramRight, paramBottom);
	}

	public static void setMargin(Context context, View view, int left, int top,
			int right, int bottom) {
		int paramLeft = resize(context, left);
		int paramTop = resize(context, top);
		int paramRight = resize(context, right);
		int paramBottom = resize(context, bottom);
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view
				.getLayoutParams();
		if (left != Integer.MAX_VALUE && left != Integer.MIN_VALUE) {
			params.leftMargin = paramLeft;
		}
		if (right != Integer.MAX_VALUE && left != Integer.MIN_VALUE) {
			params.rightMargin = paramRight;
		}
		if (top != Integer.MAX_VALUE && left != Integer.MIN_VALUE) {
			params.topMargin = paramTop;
		}
		if (bottom != Integer.MAX_VALUE && left != Integer.MIN_VALUE) {
			params.bottomMargin = paramBottom;
		}
		view.setLayoutParams(params);
	}

}
