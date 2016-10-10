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


import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entboost.Log4jLog;
import com.entboost.ui.base.view.adapter.AbFragmentPagerAdapter;

// TODO: Auto-generated Javadoc
/**
 * 名称：AbSlidingTabView
 * 描述：滑动的tab.
 * @author amsoft.cn
 * @date 2011-11-28
 * @version
 */
public class AbBottomTabView extends LinearLayout {
	
	/** The tag. */
	private static String TAG = AbBottomTabView.class.getSimpleName();
	private static String LONG_TAG = AbBottomTabView.class.getName();
	
	/** The context. */
	private Context context;
	
	/** tab的线性布局. */
	private LinearLayout mTabLayout = null;
	
	/** The m view pager. */
	private ViewPager mViewPager;
	
	private ViewPager.OnPageChangeListener mListener;
	
	/**tab的列表*/
	private Vector<TextView> tabItemList = null;
	
	/**内容的View*/
	private Vector<Fragment> pagerItemList = null;
	
	/**tab的文字*/
	private Vector<String> tabItemTextList = null;
	
	/**tab的图标*/
	private Vector<Drawable> tabItemDrawableList = null;
	
	
	/** The layout params ff. */
	public LinearLayout.LayoutParams layoutParamsFF = null;
	
	/** The layout params fw. */
	public LinearLayout.LayoutParams layoutParamsFW = null;
	
	/** The layout params ww. */
	public LinearLayout.LayoutParams layoutParamsWW = null;
	
	/**当前选中编号*/
	private int mSelectedTabIndex = 0;
	
	/**内容区域的适配器*/
	private AbFragmentPagerAdapter mFragmentPagerAdapter = null;

	/**tab的背景*/
    private int tabBackgroundResource = -1;
    
	/**tab的文字大小*/
	private int tabTextSize = 16;
	
	/**tab的文字颜色*/
	private int tabTextColor = Color.BLACK;
	
	/**tab的选中文字颜色*/
	private int tabSelectColor = Color.WHITE;
	
	private OnClickListener mTabClickListener = new OnClickListener() {
        public void onClick(View view) {
        	AbTabItemView tabView = (AbTabItemView)view;
            setCurrentItem(tabView.getIndex());
        }
    };
	
	
	public AbBottomTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		
		layoutParamsFW = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParamsFF = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		layoutParamsWW = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		this.setOrientation(LinearLayout.VERTICAL);
		this.setBackgroundColor(Color.rgb(255, 255, 255));
		
		mTabLayout = new LinearLayout(context);
		mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
		mTabLayout.setGravity(Gravity.CENTER);
		
		//内容的View的适配
		mViewPager = new ViewPager(context);
		//手动创建的ViewPager,必须调用setId()方法设置一个id
		mViewPager.setId(1985);
		pagerItemList = new Vector<Fragment>();
		this.addView(mViewPager,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,0,1));
		addView(mTabLayout, layoutParamsFW);
		
		//定义Tab栏
  		tabItemList = new Vector<TextView>();
  		tabItemTextList = new Vector<String>();
  		tabItemDrawableList = new Vector<Drawable>();
		//要求必须是FragmentActivity的实例
		if(!(this.context instanceof FragmentActivity)){
			Log4jLog.e(LONG_TAG, "构造AbSlidingTabView的参数context,必须是FragmentActivity的实例。");
		}
		
		FragmentManager mFragmentManager = ((FragmentActivity)this.context).getSupportFragmentManager();
		mFragmentPagerAdapter = new AbFragmentPagerAdapter(
				mFragmentManager, pagerItemList);
		mViewPager.setAdapter(mFragmentPagerAdapter);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPager.setOffscreenPageLimit(3);
		
	}

	
	
	public class MyOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		@Override
		public void onPageSelected(int arg0) {
			setCurrentItem(arg0);
		}
		
	}
	
	public AbTabItemView getItem(int item) {
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		final int tabCount = mTabLayout.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			if (i == item) {
				return (AbTabItemView) mTabLayout.getChildAt(i);
			}
		}
		return null;
	}
	
	public void initItemsTip(int resid){
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		final int tabCount = mTabLayout.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			 AbTabItemView child = (AbTabItemView) mTabLayout
					.getChildAt(i);
			 child.initTip(resid);
		}
	}
	
	/**
     * 
     * 描述：设置显示哪一个
     * @param item
     * @throws 
     */
    public void setCurrentItem(int index) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mSelectedTabIndex = index;
        final int tabCount = mTabLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final AbTabItemView child = (AbTabItemView)mTabLayout.getChildAt(i);
            final boolean isSelected = (i == index);
            child.setSelected(isSelected);
            if (isSelected) {
            	child.setTabTextColor(tabSelectColor);
            	if(tabBackgroundResource!=-1){
            		 child.setTabBackgroundResource(tabBackgroundResource);
                }
            	if(tabItemDrawableList.size() >= tabCount*2){
             		 child.setTabCompoundDrawables(null, tabItemDrawableList.get(index*2+1), null, null);
             	}else if(tabItemDrawableList.size() >= tabCount){
    			     child.setTabCompoundDrawables(null, tabItemDrawableList.get(index), null, null);
    		    }
            	mViewPager.setCurrentItem(index);
            }else{
            	if(tabBackgroundResource!=-1){
           		   child.setBackgroundDrawable(null);
                }
            	if(tabItemDrawableList.size() >= tabCount*2){
            		child.setTabCompoundDrawables(null, tabItemDrawableList.get(i*2), null, null);
            	}
            	child.setTabTextColor(tabTextColor);
            }
        }
    }
    
    /**
     * 
     * 描述：设置一个外部的监听器
     * @param listener
     * @throws 
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
        mViewPager.setOnPageChangeListener(listener);
    }
    
	@Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
	/**
     * 描述：设置单个tab的背景选择器
     * @param resid
     * @throws 
     */
	public void setTabBackgroundResource(int resid) {
    	tabBackgroundResource = resid;
    }
	
	/**
	 * 
	 * 描述：设置Tab的背景
	 * @param res
	 * @throws 
	 */
	public void setTabLayoutBackgroundResource(int resid) {
		this.mTabLayout.setBackgroundResource(resid);
	}

	public int getTabTextSize() {
		return tabTextSize;
	}

	public void setTabTextSize(int tabTextSize) {
		this.tabTextSize = tabTextSize;
	}
	
	/**
	 * 
	 * 描述：设置tab文字的颜色
	 * @param tabColor
	 * @throws 
	 */
	public void setTabTextColor(int tabColor) {
		this.tabTextColor = tabColor;
	}

	/**
	 * 
	 * 描述：设置选中的颜色
	 * @param tabColor
	 * @throws 
	 */
	public void setTabSelectColor(int tabColor) {
		this.tabSelectColor = tabColor;
	}
    
    /**
     * 
     * 描述：创造一个Tab
     * @param text
     * @param index
     * @throws 
     */
    private void addTab(String text, int index) {
    	addTab(text,index,null);
    }
    
    /**
     * 
     * 描述：创造一个Tab
     * @param text
     * @param index
     * @throws 
     */
    private void addTab(String text, int index,Drawable top) {
   	
    	AbTabItemView tabView = new AbTabItemView(this.context);
       
        if(top!=null){
        	tabView.setTabCompoundDrawables(null, top, null, null);
        }
    	tabView.setTabTextColor(tabTextColor);
    	tabView.setTabTextSize(tabTextSize);
        
        tabView.init(index,text);
        tabItemList.add(tabView.getTextView());
        tabView.setOnClickListener(mTabClickListener);
        mTabLayout.addView(tabView, new LayoutParams(0,LayoutParams.WRAP_CONTENT,1));
    }
    
    /**
     * 
     * 描述：tab有变化刷新
     * @throws 
     */
    public void notifyTabDataSetChanged() {
        mTabLayout.removeAllViews();
        tabItemList.clear();
        final int count = mFragmentPagerAdapter.getCount();
        for (int i = 0; i < count; i++) {
        	if(tabItemDrawableList.size()>=count*2){
        		addTab(tabItemTextList.get(i), i,tabItemDrawableList.get(i*2));
        	}else if(tabItemDrawableList.size()>=count){
        		addTab(tabItemTextList.get(i), i,tabItemDrawableList.get(i));
        	}else{
        		addTab(tabItemTextList.get(i), i);
        	}
        }
        if (mSelectedTabIndex > count) {
            mSelectedTabIndex = count - 1;
        }
        setCurrentItem(mSelectedTabIndex);
        requestLayout();
    }
	
	
    /**
	 * 
	 * 描述：增加一组内容与tab
	 * @throws 
	 */
	public void addItemViews(Vector<String> tabTexts,Vector<Fragment> fragments){
		
		tabItemTextList.addAll(tabTexts);
		pagerItemList.addAll(fragments);
		
		mFragmentPagerAdapter.notifyDataSetChanged();
		notifyTabDataSetChanged();
	}
	
	/**
	 * 
	 * 描述：增加一组内容与tab附带顶部图片
	 * @throws 
	 */
	public void addItemViews(Vector<String> tabTexts,Vector<Fragment> fragments,Vector<Drawable> drawables){
		
		tabItemTextList.addAll(tabTexts);
		pagerItemList.addAll(fragments);
		tabItemDrawableList.addAll(drawables);
		mFragmentPagerAdapter.notifyDataSetChanged();
		notifyTabDataSetChanged();
	}
	
	/**
	 * 
	 * 描述：增加一个内容与tab
	 * @throws 
	 */
	public void addItemView(String tabText,Fragment fragment){
		tabItemTextList.add(tabText);
		pagerItemList.add(fragment);
		mFragmentPagerAdapter.notifyDataSetChanged();
		notifyTabDataSetChanged();
	}
	
	/**
	 * 
	 * 描述：增加一个内容与tab
	 * @throws 
	 */
	public void addItemView(String tabText,Fragment fragment,Drawable drawableNormal,Drawable drawablePressed){
		tabItemTextList.add(tabText);
		pagerItemList.add(fragment);
		tabItemDrawableList.add(drawableNormal);
		tabItemDrawableList.add(drawablePressed);
		mFragmentPagerAdapter.notifyDataSetChanged();
		notifyTabDataSetChanged();
	}
	
	public Fragment getItemView(int index){
		return pagerItemList.get(index);
	}
	
	
	/**
	 * 
	 * 描述：删除某一个
	 * @param index
	 * @throws 
	 */
	public void removeItemView(int index){
		
		mTabLayout.removeViewAt(index);
		pagerItemList.remove(index);
		tabItemList.remove(index);
		tabItemDrawableList.remove(index);
		mFragmentPagerAdapter.notifyDataSetChanged();
		notifyTabDataSetChanged();
	}
	
	/**
	 * 
	 * 描述：删除所有
	 * @throws 
	 */
	public void removeAllItemViews(){
		mTabLayout.removeAllViews();
		pagerItemList.clear();
		tabItemList.clear();
		tabItemDrawableList.clear();
		mFragmentPagerAdapter.notifyDataSetChanged();
		notifyTabDataSetChanged();
	}
	
	/**
	 * 
	 * 描述：获取这个View的ViewPager
	 * @return
	 * @throws 
	 */
	public ViewPager getViewPager() {
		return mViewPager;
	}
	
	/**
	 * 
	 * 描述：设置每个tab的边距
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @throws 
	 */
	public void setTabPadding(int left, int top, int right, int bottom) {
		for(int i = 0;i<tabItemList.size();i++){
			TextView tv = tabItemList.get(i);
			tv.setPadding(left, top, right, bottom);
		}
	}
	
}
