package com.entboost.im.contact;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.ContactGroup;
import net.yunim.service.listener.DelGroupListener;
import net.yunim.service.listener.EditContactListener;
import net.yunim.service.listener.EditGroupListener;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.ui.base.view.popmenu.PopMenu;
import com.entboost.ui.base.view.popmenu.PopMenuConfig;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;

public class SelectContactGroupActivity extends EbActivity {
	private ListView mAbPullListView;
	private ContactGroupSelectAdapter contactGroupSelectAdapter;
	private long con_id;
	private PopMenu popMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_select_contact_group);
		
		mAbPullListView = (ListView) findViewById(R.id.mListView);
		contactGroupSelectAdapter = new ContactGroupSelectAdapter(this);
		contactGroupSelectAdapter.setInput(EntboostCache.getContactGroups());
		mAbPullListView.setAdapter(contactGroupSelectAdapter);
		con_id = getIntent().getLongExtra("con_id", -1);
		popMenu = new PopMenu(this);
		
		popMenu.addItem("编辑分组",R.layout.item_menu, new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				final ContactGroup contactGroup = (ContactGroup) popMenu
						.getObj();
				final EditText input = new EditText(
						SelectContactGroupActivity.this);
				showDialog("编辑分组", input, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = input.getText().toString();
						if (StringUtils.isBlank(value)) {
							showToast("分组名称不能为空！");
							return;
						}
						showProgressDialog("正在编辑分组！");
						EntboostUM.editContactGroup(contactGroup.getUgid(), value, new EditGroupListener() {
							@Override
							public void onFailure(int code, final String errMsg) {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										removeProgressDialog();
										showToast(errMsg);
									}
								});
							}
							@Override
							public void onEditGroupSuccess(Long dep_code) {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										removeProgressDialog();
										contactGroupSelectAdapter.setInput(EntboostCache.getContactGroups());
										contactGroupSelectAdapter.notifyDataSetChanged();
									}
								});
							}
						});
					}
				});
			}

		});
		
		popMenu.addItem("删除分组",R.layout.item_menu, new PopMenuItemOnClickListener() {

			@Override
			public void onItemClick() {
				ContactGroup contactGroup = (ContactGroup) popMenu.getObj();
				showProgressDialog("正在删除分组！");
				EntboostUM.delContactGroup(contactGroup.getUgid(), new DelGroupListener() {
							@Override
							public void onFailure(int code, final String errMsg) {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										removeProgressDialog();
										showToast(errMsg);
									}
								});
							}
							@Override
							public void onDelGroupSuccess() {
								HandlerToolKit.runOnMainThreadAsync(new Runnable() {
									@Override
									public void run() {
										removeProgressDialog();
										contactGroupSelectAdapter.setInput(EntboostCache.getContactGroups());
										contactGroupSelectAdapter.notifyDataSetChanged();
									}
								});
							}

						});
			}

		});
		
		mAbPullListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> view,
							View arg1, int arg2, long arg3) {
						ContactGroup contactGroup = (ContactGroup) contactGroupSelectAdapter
								.getItem(arg2);
						popMenu.setObj(contactGroup);
						popMenu.showCenter(view);
						return false;
					}
				});
		
		mAbPullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactGroup info = (ContactGroup) contactGroupSelectAdapter
						.getItem(position);
				long select_ugid = contactGroupSelectAdapter.getUg_id();
				ImageButton selectImg = contactGroupSelectAdapter
						.getSelectImg();
				if (selectImg != null) {
					selectImg.setImageDrawable(null);
				}
				selectImg = (ImageButton) view.findViewById(R.id.user_select);
				contactGroupSelectAdapter.setSelectImg(selectImg);
				if (select_ugid != 0 && select_ugid - info.getUgid() == 0) {
					contactGroupSelectAdapter.setUg_id(0);
					selectImg.setImageDrawable(null);
				} else {
					contactGroupSelectAdapter.setUg_id(info.getUgid());
					selectImg.setImageResource(R.drawable.uitb_57);
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
		this.getTitleBar().addRightImageButton(R.drawable.ic_action_add_group,
				null, new PopMenuItem(new PopMenuItemOnClickListener() {

					@Override
					public void onItemClick() {
						final EditText input = new EditText(
								SelectContactGroupActivity.this);
						showDialog("添加分组", input, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String value = input.getText().toString();
								if (StringUtils.isBlank(value)) {
									showToast("分组名称不能为空！");
									return;
								}
								showProgressDialog("正在添加分组！");
								EntboostUM.addContactGroup(value, new EditGroupListener() {
									@Override
									public void onFailure(int code, final String errMsg) {
										HandlerToolKit.runOnMainThreadAsync(new Runnable() {
											@Override
											public void run() {
												removeProgressDialog();
												showToast(errMsg);
											}
										});
									}
									@Override
									public void onEditGroupSuccess(Long dep_code) {
										HandlerToolKit.runOnMainThreadAsync(new Runnable() {
											@Override
											public void run() {
												removeProgressDialog();
												contactGroupSelectAdapter.setInput(EntboostCache.getContactGroups());
												contactGroupSelectAdapter.notifyDataSetChanged();
											}
										});
									}
								});
							}
						});
					}

				}));
		this.getTitleBar().addRightImageButton(R.drawable.ic_action_save, null, new PopMenuItem(new PopMenuItemOnClickListener() {

			@Override
			public void onItemClick() {
				showProgressDialog("正在移动到其它分组");
				EntboostUM.moveContact(con_id, contactGroupSelectAdapter.getUg_id(), new EditContactListener() {
					@Override
					public void onFailure(int code, final String errMsg) {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
								showToast(errMsg);
							}
						});
					}
					
					@Override
					public void onOauthForword() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
							}
						});
					}
					
					@Override
					public void onEditContactSuccess() {
						HandlerToolKit.runOnMainThreadAsync(new Runnable() {
							@Override
							public void run() {
								removeProgressDialog();
								finish();
							}
						});
					}
				});
			}
		}));
	}

}
