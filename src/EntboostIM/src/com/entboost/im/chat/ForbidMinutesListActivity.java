package com.entboost.im.chat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;

public class ForbidMinutesListActivity extends EbActivity {
	
	/**
	 * 已选中禁言时长(输出参数)
	 */
	public final static String INTENT_SELECTED_FORBID_MINUTES_OUTPUT = "forbid_minutes_selected_input";
	/**
	 * 已选中禁言时长(输入参数)
	 */
	public final static String INTENT_SELECTED_FORBID_MINUTES_INPUT = "forbid_minutes_selected_input";
	
	private ListView listView;
	private ForbidMinutesAdapter forbidMinutesAdapter;
	private Integer selected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_forbid_minutes_list);
		listView = (ListView) findViewById(R.id.forbid_minutes_list);
		
		forbidMinutesAdapter = new ForbidMinutesAdapter(this);
		listView.setAdapter(forbidMinutesAdapter);
		selected = getIntent().getIntExtra(INTENT_SELECTED_FORBID_MINUTES_INPUT, -3);
		forbidMinutesAdapter.setSelectedForbidMinutes(selected);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object[] objs = (Object[]) forbidMinutesAdapter.getItem(position);
				
				ImageView selectImg = (ImageView) view.findViewById(R.id.user_select);
				Drawable srcImg = selectImg.getDrawable();
				if (srcImg == null) {
					selectImg.setImageResource(R.drawable.uitb_57);
					selected = (Integer)objs[0];
				} else {
					selectImg.setImageDrawable(null);
					selected = null;
				}
				
				Intent intent = getIntent();
				if (selected!=null)
					intent.putExtra(INTENT_SELECTED_FORBID_MINUTES_OUTPUT, selected);
				setResult(RESULT_OK, intent);
				ForbidMinutesListActivity.this.finish();
			}
		});
	}
	
	
}
