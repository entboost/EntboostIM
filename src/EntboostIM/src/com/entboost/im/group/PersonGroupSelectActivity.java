package com.entboost.im.group;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.service.listener.AddToGroupListener;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.comparator.PersonGroupInfoComparator;
import com.entboost.ui.base.view.popmenu.PopMenuConfig;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;

public class PersonGroupSelectActivity extends EbActivity {

	private ListView mAbPullListView;
	private PersonGroupSelectAdapter personGroupSelectAdapter;
	private Long uid;
	private Set<Long> depIds = new HashSet<Long>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_person_group_select);
		mAbPullListView = (ListView) findViewById(R.id.mListView);
		personGroupSelectAdapter = new PersonGroupSelectAdapter(this);
		
		List<PersonGroupInfo> groups = EntboostCache.getPersonGroups();
		Collections.sort(groups, new PersonGroupInfoComparator()); //排序
		personGroupSelectAdapter.setInput(groups);
		
		mAbPullListView.setAdapter(personGroupSelectAdapter);
		uid = getIntent().getLongExtra("uid", -1);
		mAbPullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				PersonGroupInfo info = (PersonGroupInfo) personGroupSelectAdapter
						.getItem(position);
				ImageView selectImg = (ImageView) view
						.findViewById(R.id.user_select);
				Drawable srcImg = selectImg.getDrawable();
				if (srcImg == null) {
					selectImg.setImageResource(R.drawable.uitb_57);
					depIds.add(info.getDep_code());
				} else {
					selectImg.setImageDrawable(null);
					if (depIds.contains(info.getDep_code())) {
						depIds.remove(info.getDep_code());
					}
				}

			}
		});
		initMenu();
	}

	public void initMenu() {
		PopMenuConfig config = new PopMenuConfig();
		config.setBackground_resId(R.drawable.popmenu);
		config.setTextColor(Color.WHITE);
		config.setShowAsDropDownYoff(28);
		
		this.getTitleBar().addRightImageButton(R.drawable.ic_action_save, null, new PopMenuItem(new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				if (uid != null && uid > -1 && depIds.size() > 0) {
					showDialog("提示", "是否发出邀请！", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							showProgressDialog("正在邀请加入群组！");
							EntboostUM.addToPersonGroup(depIds, uid, new AddToGroupListener() {
								
								@Override
								public void onSuccess(Long depCode, Long uid, String account) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											showToast("邀请已经发出！");
											removeProgressDialog();
											finish();
										}
									});
								}

								@Override
								public void onFailure(int code, final String errMsg, Long depCode, Long uid, String account) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											removeProgressDialog();
											showToast(errMsg);
										}
									});	
								}

								@Override
								public void onFailure(int code, final String errMsg) {
									//do nothing
								}
							});
						}
					});
				} else {
					showToast("未选择要加入的群组！");
				}
			}

		}));
	}

}
