package com.entboost.im.base;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EbFragment extends Fragment {

	protected LayoutInflater mInflater;

	protected EbActivity activity;

	public void hide() {
		if(activity!=null){
			activity.pageInfo.hide();
		}
	}
	
	/**
	 * 刷新分页面
	 * @param switchView 是否切换页面
	 * @param notifyChangeWhich 通知哪个分页面刷新；当switchView=true无效
	 */
	public void refreshPage(boolean switchView, int notifyChangeWhich) {
		if (switchView)
			hide();
	};

	protected View onCreateEbView(int layoutId, LayoutInflater inflater,
			ViewGroup container) {
		mInflater = inflater;
		activity = (EbActivity) this.getActivity();
		return inflater.inflate(layoutId, container, false);
	}

}
