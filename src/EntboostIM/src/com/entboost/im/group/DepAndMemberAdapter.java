package com.entboost.im.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.constants.EB_MANAGER_LEVEL;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.LoadAllMemberListener;
import net.yunim.utils.YIResourceUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.comparator.DepartmentInfoComparator;
import com.entboost.im.comparator.MemberInfoComparator;
import com.entboost.im.global.MyApplication;
import com.entboost.im.global.UIUtils;
import com.entboost.im.group.MemberInfoViewHolder;
import com.entboost.im.group.MemberSelectActivity;
import com.entboost.ui.utils.AbImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class DepAndMemberAdapter extends BaseAdapter {
	private Context mContext;
	private List<Object> groupMemberInfos = new ArrayList<Object>();
	private boolean selectMember; //是否选择人员视图
	private boolean selectOne = false; //是否单选
	//除外的用户编号列表(不允许选中这些编号)
	private List<Long> excludeUids = new ArrayList<Long>();
	
	public DepAndMemberAdapter(Context context) {
		mContext = context;
	}

	public Context getmContext() {
		return mContext;
	}

	public void setSelectMember(boolean selectMember) {
		this.selectMember = selectMember;
	}

	public void setSelectOne(boolean selectOne) {
		this.selectOne = selectOne;
	}
	
	public void setExcludeUids(List<Long> excludeUids) {
		this.excludeUids = excludeUids;
	}

	public void setMembers(Long groupid) {
		this.groupMemberInfos.clear();
		
		List<DepartmentInfo> depInfos = EntboostCache.getNextDepartmentInfos(groupid);
		if (depInfos!=null) {
			Collections.sort(depInfos, new DepartmentInfoComparator()); //排序
			this.groupMemberInfos.addAll(depInfos);
		}
		
		List<MemberInfo> memberInfos = EntboostCache.getGroupMemberInfos(groupid);
		if (memberInfos!=null) {
			Collections.sort(memberInfos, new MemberInfoComparator()); //排序
			this.groupMemberInfos.addAll(memberInfos);
		}
	}

	@Override
	public int getCount() {
		return this.groupMemberInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		return this.groupMemberInfos.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		if (obj instanceof MemberInfo) { //成员
			final MemberInfoViewHolder holder1;
			if (convertView == null || !(convertView.getTag() instanceof MemberInfoViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
				// 减少findView的次数
				holder1 = new MemberInfoViewHolder();
				// 初始化布局中的元素
				holder1.userImg = ((ImageView) convertView.findViewById(R.id.user_head));
				holder1.itemsText = ((TextView) convertView.findViewById(R.id.user_name));
				holder1.description = ((TextView) convertView.findViewById(R.id.user_description));
				holder1.user_select = ((ImageButton) convertView.findViewById(R.id.user_select));
				convertView.setTag(holder1);
			} else {
				holder1 = (MemberInfoViewHolder) convertView.getTag();
				holder1.user_select.setImageResource(0);
			}
			
			final MemberInfo memberInfo = (MemberInfo) obj;
			//处理点击头像事件；选择视图不允许点击
			if (!selectMember) {
				holder1.userImg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, MemberInfoActivity.class);
						if (memberInfo != null) {
							intent.putExtra("memberCode", memberInfo.getEmp_code());
							if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
								intent.putExtra("selfFlag", true);
							}
							mContext.startActivity(intent);
						}
					}
				});
			}
			
			//设置头像
			Bitmap img = YIResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
			if (img != null) {
				if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
						.getValue()) {
					holder1.userImg.setImageBitmap(AbImageUtil.grey(img));
				} else {
					holder1.userImg.setImageBitmap(img);
				}
			} else {
				ImageLoader.getInstance().loadImage(memberInfo.getHeadUrl(), MyApplication.getInstance().getUserImgOptions(), new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String arg0, View arg1) {
					}

					@Override
					public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
						if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
							holder1.userImg.setImageBitmap(AbImageUtil.grey(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.default_user)));
						} else {
							holder1.userImg.setImageResource(R.drawable.default_user);
						}
					}
					
					@Override
					public void onLoadingComplete(String arg0,
							View arg1, Bitmap arg2) {
						if (arg2 == null) {
							onLoadingFailed(arg0, arg1, null);
							return;
						}
						if (memberInfo.getState() <= EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE.getValue()) {
							holder1.userImg.setImageBitmap(AbImageUtil.grey(arg2));
						} else {
							holder1.userImg.setImageBitmap(arg2);
						}
					}

					@Override
					public void onLoadingCancelled(String arg0, View arg1) {
					}
				});
			}
			
			holder1.itemsText.setText(memberInfo.getUsername());
			holder1.description.setText(memberInfo.getDescription());
			
			//在选择视图
			if (selectMember) {
				holder1.user_select.setVisibility(View.VISIBLE);
				
				if (selectOne || excludeUids.contains(memberInfo.getEmp_uid())/*|| memberInfo.getEmp_uid() - EntboostCache.getUid() == 0*/) {
					holder1.user_select.setVisibility(View.INVISIBLE);
				}
				
				if (MemberSelectActivity.isMemberSelected(memberInfo))
					holder1.user_select.setImageResource(R.drawable.uitb_57);
			}
			
			// 改变名称颜色
			if (memberInfo.isCreator() || (memberInfo.getManager_level() & EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) == EB_MANAGER_LEVEL.EB_LEVEL_DEP_ADMIN.getValue()) {
				holder1.itemsText.setTextColor(Color.rgb(255, 0, 96));
			}else if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
				holder1.itemsText.setTextColor(Color.BLUE);
			}else{
				holder1.itemsText.setTextColor(Color.BLACK); //Color.rgb(25, 78, 98)
			}
		} else { //深层部门
			final GroupViewHolder holder2;
			if (convertView == null || !(convertView.getTag() instanceof GroupViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false);
				// 减少findView的次数
				holder2 = new GroupViewHolder();
				// 初始化布局中的元素
				holder2.itemsHead = ((ImageView) convertView.findViewById(R.id.item_group_head));
				holder2.itemsProgress = ((ProgressBar) convertView.findViewById(R.id.item_group_head_progress));
				holder2.itemsText = ((TextView) convertView.findViewById(R.id.item_group_name));
				holder2.itemsInfo = ((ImageView) convertView.findViewById(R.id.item_group_info));
				holder2.itemsTalk = ((ImageView) convertView.findViewById(R.id.item_call_talk));
				holder2.itemsSelectAll = ((ImageView) convertView.findViewById(R.id.select_all));
				
				convertView.setTag(holder2);
			} else {
				holder2 = (GroupViewHolder) convertView.getTag();
			}
			
			final DepartmentInfo group = (DepartmentInfo) obj;
			holder2.itemsHead.setVisibility(View.VISIBLE);
			holder2.itemsProgress.setVisibility(View.GONE);
			holder2.itemsHead.setImageResource(R.drawable.ui65new);
			
			//名称+成员在线人数和成员人数
			AppAccountInfo appInfo = EntboostCache.getAppInfo();
			String formatedCountStr = " ";
			if ((appInfo.getSystem_setting() & AppAccountInfo.SYSTEM_SETTING_VALUE_DISABLE_STATSUB_GROUP_MEMBER) 
					!= AppAccountInfo.SYSTEM_SETTING_VALUE_DISABLE_STATSUB_GROUP_MEMBER) {
				int count = EntboostCache.getDepartmentMemberCount(group.getDep_code(), true);
				formatedCountStr = formatedCountStr + (count>0?("[" + EntboostCache.getDepartmentMemberOnlineCount(group.getDep_code(), true) + "/" + count + "]"):"");
			} else {
				formatedCountStr = formatedCountStr + (group.getEmp_count()>0?("[" + EntboostCache.getGroupOnlineCount(group.getDep_code()) + "/" + group.getEmp_count() + "]"):"");
			}
			holder2.itemsText.setText(group.getDep_name() + formatedCountStr);
			
			//最右边的按钮布局
			RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.rightMargin = 5;
			
			//选择视图
			if (selectMember) {
				//选择框靠最右边
				holder2.itemsSelectAll.setLayoutParams(layoutParams);
				
				holder2.itemsInfo.setVisibility(View.GONE);
				holder2.itemsTalk.setVisibility(View.GONE);
				holder2.itemsSelectAll.setVisibility(View.VISIBLE);
				
				//设置全选按钮显示状态
				if (MemberSelectActivity.isSelectedGroup(group.getDep_code())) {
					holder2.itemsSelectAll.setImageResource(R.drawable.uitb_57);
				} else {
					holder2.itemsSelectAll.setImageResource(0);
				}
				
				if (selectOne)
					holder2.itemsSelectAll.setVisibility(View.GONE);
			} else { //普通视图
				//查看属性按钮靠最右边
				holder2.itemsInfo.setLayoutParams(layoutParams);
				
				holder2.itemsInfo.setVisibility(View.VISIBLE);
				holder2.itemsTalk.setVisibility(View.VISIBLE);
				holder2.itemsSelectAll.setVisibility(View.GONE);
			}
			
			//查看属性按钮
			holder2.itemsInfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, DepartmentInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				}
			});
			
			//点击进入聊天按钮
			if (!selectMember && group.getMy_emp_id()!=null && group.getMy_emp_id()>0) {
				holder2.itemsTalk.setVisibility(View.VISIBLE);
				holder2.itemsTalk.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 打开聊天会话界面
						Intent intent = new Intent(DepAndMemberAdapter.this.mContext, ChatActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(ChatActivity.INTENT_TITLE, group.getDep_name());
						intent.putExtra(ChatActivity.INTENT_TOID, group.getDep_code());
						intent.putExtra(ChatActivity.INTENT_CHATTYPE, ChatActivity.CHATTYPE_GROUP);
						DepAndMemberAdapter.this.mContext.startActivity(intent);
					}
				});
			} else {
				holder2.itemsTalk.setVisibility(View.GONE);
			}
			
			//全选子项按钮
			if (holder2.itemsSelectAll.getVisibility() == View.VISIBLE) {
				holder2.itemsSelectAll.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final Long depCode = group.getDep_code();
						Drawable srcImg = holder2.itemsSelectAll.getDrawable();
						
						//选中
						if (srcImg == null) {
							holder2.itemsSelectAll.setImageResource(R.drawable.uitb_57);
							//全选组内全部成员
							UIUtils.showProgressDialog(mContext, "请稍后...");
							EntboostUM.loadMembers(depCode, new LoadAllMemberListener() {
								@Override
								public void onFailure(int code, final String errMsg) {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											UIUtils.removeProgressDialog();
											//showToast(errMsg);
										}
									});
								}
								
								@Override
								public void onLoadAllMemberSuccess() {
									HandlerToolKit.runOnMainThreadAsync(new Runnable() {
										@Override
										public void run() {
											List<MemberInfo> memberInfos = EntboostCache.getGroupMemberInfos(depCode);
											if (memberInfos != null) {
												MemberSelectActivity.addSelectedGroup(group);
												//MemberSelectActivity.addSelectedMembers(depCode, memberInfos);
												
												//去除被过滤的用户
												for (MemberInfo mi : memberInfos) {
													if (!excludeUids.contains(mi.getEmp_uid()))
														MemberSelectActivity.addSelectedMember(mi);
												}
												
//												//触发变更回调事件
//												if (selectedMemberListener!=null)
//													selectedMemberListener.onSelectedMemberChange();
											}
											
											UIUtils.removeProgressDialog();
										}
									});
								}
							});
						} else { //反选
							holder2.itemsSelectAll.setImageResource(0);
							//整组删除已选中成员
							MemberSelectActivity.removeSelectedMembers(depCode);
							MemberSelectActivity.removeSelectedGroup(depCode);
							
//							//触发变更回调事件
//							if (selectedMemberListener!=null)
//								selectedMemberListener.onSelectedMemberChange();
						}
					}
				});
			}
		}
		return convertView;
	}

	static class GroupViewHolder {
		public ImageView itemsHead; //头像控件
		public TextView itemsText;	//名称控件
		public ProgressBar itemsProgress; //加载进度控件
		public ImageView itemsInfo; //查看属性按钮
		public ImageView itemsTalk; //进入聊天会话按钮
		public ImageView itemsSelectAll; //全选组内成员按钮
	}

}
