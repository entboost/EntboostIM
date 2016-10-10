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

	public void refreshPage() {
		hide();
	};

	protected View onCreateEbView(int layoutId, LayoutInflater inflater,
			ViewGroup container) {
		mInflater = inflater;
		activity = (EbActivity) this.getActivity();
		return inflater.inflate(layoutId, container, false);
	}

}
