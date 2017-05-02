package com.entboost.im.function;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.FuncInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbFragment;

public class FunctionListFragment extends EbFragment {

	private ListView mAbPullListView;
	private FunctionAdapter functionAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = onCreateEbView(R.layout.fragment_function, inflater, container);
		mAbPullListView = (ListView) view.findViewById(R.id.mListView);
		
		functionAdapter = new FunctionAdapter(view.getContext(), EntboostCache.getCommonFuncInfos());
		mAbPullListView.setAdapter(functionAdapter);
		
		mAbPullListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FuncInfo funcInfo = (FuncInfo) functionAdapter.getItem(position);
				Intent intent = new Intent(FunctionListFragment.this.getActivity(), FunctionMainActivity.class);
				intent.putExtra("funcInfo", funcInfo);
				startActivity(intent);
			}
		});
		return view;
	}

	@Override
	public void refreshPage(boolean switchView, int notifyChangeWhich) {
		super.refreshPage(switchView, notifyChangeWhich);
		if(functionAdapter!=null){
			functionAdapter.setList(EntboostCache.getCommonFuncInfos());
			functionAdapter.notifyDataSetChanged();
		}
	}
}
