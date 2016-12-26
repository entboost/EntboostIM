package com.entboost.ui.base.view.popmenu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class PopMenu {

	private List<PopMenuItem> itemList = new ArrayList<PopMenuItem>();
	private PopupWindow popupWindow;
	private ListView listView;
	private Context context;
	private Object obj;
	private PopMenuConfig config;
	private OnDismissListener dismissListener;
	private PopAdapter adapter;

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public PopMenu(Context context) {
		this.context = context;
	}

	public PopMenu(Context context, PopMenuConfig config) {
		this.context = context;
		this.config = config;
	}

	private void init() {
		LinearLayout view = new LinearLayout(context);
		view.setOrientation(LinearLayout.VERTICAL);
		view.setGravity(Gravity.CENTER_HORIZONTAL);
		view.setBackgroundColor(Color.rgb(224, 224, 224));
		view.setPadding(1, 1, 1, 1);
		listView = new ListView(context);
		adapter = new PopAdapter();
		listView.setAdapter(adapter);
		listView.setCacheColorHint(0x00000000);
		listView.setDivider(new ColorDrawable(0xe0e0e0));
		listView.setDividerHeight(1);
		if (this.config != null && this.config.getBackground_resId() != 0) {
			listView.setBackgroundResource(this.config.getBackground_resId());
		} else {
			listView.setBackgroundColor(Color.rgb(224, 224, 224));
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		view.addView(listView, layoutParams);
		if (this.config != null && this.config.getWidth() > 0) {
			popupWindow = new PopupWindow(view, this.config.getWidth(),
					LayoutParams.WRAP_CONTENT);
		} else {
			// 为了自适应长度添加
			TextView text = new TextView(context);
			text.setHeight(0);
			text.setText("图片的空" + getMostItemText());
			text.setTextSize(16);
			text.setPadding(5, 5, 5, 5);
			view.addView(text, layoutParams1);
			popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}
		if (dismissListener != null) {
			popupWindow.setOnDismissListener(dismissListener);
		}
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		// 刷新状态
		popupWindow.update();
		popupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					popupWindow.dismiss();
					return true;
				}
				return false;
			}

		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 获取该行的数据
				PopMenuItem popMenuItem = (PopMenuItem) listView.getAdapter()
						.getItem(arg2);
				if (popMenuItem.listener != null) {
					popMenuItem.listener.onItemClick();
					dismiss();
				}
			}

		});
	}

	/**
	 * 删除单个菜单项
	 * @param text
	 */
	public void removeItem(String text) {
		for (PopMenuItem item : itemList) {
			if (StringUtils.equals(item.text, text)) {
				itemList.remove(item);
				break;
			}
		}
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 删除全部菜单项
	 */
	public void removeAllItem() {
		itemList.clear();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	// 单个添加菜单项
	public void addItem(String text, int layout_resId,
			PopMenuItemOnClickListener listener) {
		itemList.add(new PopMenuItem(text, 0, layout_resId, listener));
	}

	public void addItem(String text, int endImg_resId, int layout_resId,
			PopMenuItemOnClickListener listener,
			View.OnClickListener endListener) {
		itemList.add(new PopMenuItem(text, endImg_resId, layout_resId,
				listener, endListener));
	}

	public void addItem(PopMenuItem item) {
		itemList.add(item);
	}
	
	public String getMostItemText() {
		String text = "";
		for (PopMenuItem item : itemList) {
			if (item.text != null && item.text.length() > text.length()) {
				text = item.text;
			}
		}
		return text;
	}

	public void addItems(List<PopMenuItem> items) {
		itemList.addAll(items);
	}

	public void addItems(PopMenuItem[] items) {
		for (PopMenuItem item : items) {
			itemList.add(item);
		}
	}

	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent) {
		if (listView == null) {
			init();
		}
		popupWindow.showAsDropDown(parent, 0, 0);
	}

	public void showAsDropDown(View parent, int yoff) {
		if (listView == null) {
			init();
		}
		popupWindow.showAsDropDown(parent, 0, yoff);
	}

	public void showCenter(View parent) {
		if (listView == null) {
			init();
		}
		popupWindow.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0,
				0);
	}

	// 隐藏菜单
	public void dismiss() {
		popupWindow.dismiss();
	}

	public void setOnDismissListener(OnDismissListener listener) {
		dismissListener = listener;
	}

	LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);

	// 适配器
	private final class PopAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			PopMenuItem popMenuItem = (PopMenuItem) listView.getAdapter()
					.getItem(position);
			if (convertView == null) {
				holder = new ViewHolder();
				if (popMenuItem.layout_resId != 0) {
					convertView = LayoutInflater.from(parent.getContext())
							.inflate(popMenuItem.layout_resId, parent, false);
					int size = ((ViewGroup) convertView).getChildCount();
					if (size == 1) {
						holder.text = (TextView) ((ViewGroup) convertView)
								.getChildAt(0);
					}
					if (size == 2) {
						if (((ViewGroup) convertView).getChildAt(0) instanceof TextView) {
							holder.text = (TextView) ((ViewGroup) convertView)
									.getChildAt(0);
						} else if (((ViewGroup) convertView).getChildAt(0) instanceof ImageView) {
							holder.img = (ImageView) ((ViewGroup) convertView)
									.getChildAt(0);
						}
						if (((ViewGroup) convertView).getChildAt(1) instanceof TextView) {
							holder.text = (TextView) ((ViewGroup) convertView)
									.getChildAt(1);
						} else if (((ViewGroup) convertView).getChildAt(1) instanceof ImageView) {
							holder.endimg = (ImageView) ((ViewGroup) convertView)
									.getChildAt(1);
						}
					}
					if (size == 3) {
						holder.img = (ImageView) ((ViewGroup) convertView)
								.getChildAt(0);
						holder.text = (TextView) ((ViewGroup) convertView)
								.getChildAt(1);
						holder.endimg = (ImageView) ((ViewGroup) convertView)
								.getChildAt(2);
					}
				} else {
					param.gravity = Gravity.CENTER_VERTICAL;
					convertView = new LinearLayout(parent.getContext());
					convertView.setPadding(15, 15, 15, 15);
					((LinearLayout) convertView)
							.setOrientation(LinearLayout.HORIZONTAL);
					((LinearLayout) convertView).setGravity(Gravity.CENTER);
					if (popMenuItem.img_resId != 0) {
						holder.img = new ImageView(parent.getContext());
						((LinearLayout) convertView)
								.setGravity(Gravity.CENTER_VERTICAL
										| Gravity.LEFT);
						((LinearLayout) convertView).addView(holder.img, param);
					}
					holder.text = new TextView(parent.getContext());
					holder.text.setPadding(20, 10, 20, 10);
					((LinearLayout) convertView).addView(holder.text, param);
					if (PopMenu.this.config != null
							&& PopMenu.this.config.getTextColor() != 0) {
						holder.text.setTextColor(PopMenu.this.config
								.getTextColor());
					} else {
						holder.text.setTextColor(Color.BLACK);
					}
					if (PopMenu.this.config != null
							&& PopMenu.this.config.getTextSize() != 0) {
						holder.text.setTextSize(PopMenu.this.config
								.getTextSize());
					} else {
						holder.text.setTextSize(16);
					}
					if (popMenuItem.endImg_resId != 0) {
						holder.endimg = new ImageView(parent.getContext());
						((LinearLayout) convertView)
								.setGravity(Gravity.CENTER_VERTICAL
										| Gravity.RIGHT);
						((LinearLayout) convertView).addView(holder.endimg,
								param);
					}
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (holder.text != null) {
				holder.text.setText(popMenuItem.text);
			}
			if (popMenuItem.endImg_resId != 0 && holder.endimg != null) {
				holder.endimg.setBackgroundResource(popMenuItem.endImg_resId);
			}
			if (popMenuItem.img_resId != 0 && holder.img != null) {
				holder.img.setBackgroundResource(popMenuItem.img_resId);
			}
			if (popMenuItem.endListener != null && holder.endimg != null) {
				holder.endimg.setOnClickListener(popMenuItem.endListener);
			}
			return convertView;
		}

		private final class ViewHolder {
			TextView text;
			ImageView img;
			ImageView endimg;
		}
	}

}
