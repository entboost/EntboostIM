package com.entboost.im.group;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.LoadAllMemberListener;
import android.content.Intent;
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
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.group.MemberSelectActivity;
import com.entboost.ui.base.view.popmenu.PopMenuConfig;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;
import com.lidroid.xutils.ViewUtils;

public class MemberListActivity extends EbActivity {

	private ListView memberlistView;
	private MemberAdapter memberAdapter;
	private OnItemClickListener memberListener;
	private long depid;
	private boolean selecteduser;

	@Override
	protected void onResume() {
		super.onResume();
		memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
		memberAdapter.notifyDataSetChanged();
	}

	@Override
	public void onUserStateChange(Long uid) {
		memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
		memberAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_member_list);
		ViewUtils.inject(this);
		
		depid = getIntent().getLongExtra("depid", -1);
		selecteduser = getIntent().getBooleanExtra("selecteduser", false);
		
		memberlistView = (ListView) findViewById(R.id.memberlist);
		memberAdapter = new MemberAdapter(this);
		memberAdapter.setSelecteduser(selecteduser);
		if (!selecteduser) {
			memberListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Object obj = memberAdapter.getItem(position);
					MemberInfo memberInfo = (MemberInfo) obj;
					if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
						Intent intent = new Intent(view.getContext(),
								MemberInfoActivity.class);
						if (memberInfo != null) {
							intent.putExtra("memberCode",
									memberInfo.getEmp_code());
							intent.putExtra("selfFlag", true);
							startActivity(intent);
						}
					} else {
						Intent intent = new Intent(view.getContext(), ChatActivity.class);
						intent.putExtra(ChatActivity.INTENT_TITLE,
								memberInfo.getUsername());
						intent.putExtra(ChatActivity.INTENT_TOID,
								memberInfo.getEmp_uid());
						startActivity(intent);
					}
				}
			};
		} else {
			memberListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Object obj = memberAdapter.getItem(position);
					MemberInfo memberInfo = (MemberInfo) obj;
					ImageView selectImg = (ImageView) view.findViewById(R.id.user_select);
					if (selectImg.getVisibility() == View.GONE) {
						return;
					}
					
					//List<Object> selectedMap = MyApplication.getInstance().getSelectedUserList();
					Drawable srcImg = selectImg.getDrawable();
					if (srcImg == null) {
						selectImg.setImageResource(R.drawable.uitb_57);
						MemberSelectActivity.addSelectedMember(memberInfo);
						//selectedMap.add(memberInfo);
					} else {
						selectImg.setImageDrawable(null);
						MemberSelectActivity.removeSelectedMember(memberInfo);
						//selectedMap.remove(memberInfo);
					}
				}
			};
		}
		
		memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
		memberlistView.setAdapter(memberAdapter);
		memberlistView.setOnItemClickListener(memberListener);
		pageInfo.showProgress("正在加载群组成员信息");
		
		EntboostUM.loadMembers(depid, new LoadAllMemberListener() {
			@Override
			public void onFailure(int code, String errMsg) {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						pageInfo.showError("无法加载群组成员信息");
					}
				});
			}
			
			@Override
			public void onLoadAllMemberSuccess() {
				HandlerToolKit.runOnMainThreadAsync(new Runnable() {
					@Override
					public void run() {
						pageInfo.hide();
						memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
						memberAdapter.notifyDataSetChanged();
					}
				});
			}
		});
		initMenu();
	}

	public void initMenu() {
		PopMenuConfig config = new PopMenuConfig();
		config.setBackground_resId(R.drawable.popmenu);
		config.setTextColor(Color.WHITE);
		this.getTitleBar().addRightImageButton(R.drawable.ic_action_refresh,
				config, new PopMenuItem(new PopMenuItemOnClickListener() {

					@Override
					public void onItemClick() {
						pageInfo.showProgress("正在加载群组成员信息");
						EntboostUM.loadMembers(depid, new LoadAllMemberListener() {
							@Override
							public void onFailure(int code, String errMsg) {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										pageInfo.showError("无法加载群组成员信息");
									}
								});
							}

							@Override
							public void onLoadAllMemberSuccess() {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										pageInfo.hide();
										memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
										memberAdapter.notifyDataSetChanged();
									}
								});
							}
						});
					}

				}));
	}

}
