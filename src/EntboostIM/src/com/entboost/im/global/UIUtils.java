package com.entboost.im.global;

import java.util.List;

import net.yunim.service.constants.EB_RESOURCE_TYPE;
import net.yunim.service.entity.Resource;
import net.yunim.utils.YIResourceUtils;

import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.entboost.Log4jLog;
import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.ui.utils.AbViewUtil;

public class UIUtils {
	
	private static String LONG_TAG = UIUtils.class.getName();
	
	public static int messageNotificationID = 100;
	
	/** 屏幕宽度. */
	public static int diaplayWidth = 320;
	
	/**
	 * 描述：Toast提示文本.
	 * 
	 * @param text 文本
	 */
	public static void showToast(final Context context, final String text) {
		//主线程中执行
		HandlerToolKit.runOnMainThreadAsync(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "" + text, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public static void showWindow(View parent, ListView view,
			final OnItemClickListener listener) {
		AbViewUtil.measureView(view);
		int popWidth = parent.getMeasuredWidth();
		if (view.getMeasuredWidth() > parent.getMeasuredWidth()) {
			popWidth = view.getMeasuredWidth();
		}
		int popMargin = 0;
		final PopupWindow popupWindow = new PopupWindow(
				(View) view.getParent(), popWidth, LayoutParams.WRAP_CONTENT,
				true);
		int[] location = new int[2];
		popMargin = parent.getMeasuredHeight();
		parent.getLocationInWindow(location);
		// int startX = location[0] - parent.getLeft();
		int startX = location[0];
		// if (startX + popWidth >= diaplayWidth) {
		// startX = diaplayWidth - popWidth - 2;
		// }

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new ColorDrawable(
				android.R.color.transparent));

		popupWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.LEFT,
				startX, popMargin);
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
		view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				listener.onItemClick(arg0, arg1, arg2, arg3);
				popupWindow.dismiss();
			}
		});
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public static boolean isAppOnForeground(Context applicationContext) {
		ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = applicationContext.getPackageName();
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 程序是否在运行
	 * 
	 * @return
	 */
	public static boolean isAppRunning(Context applicationContext) {
		ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = applicationContext.getPackageName();
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * 将表情资源添加到文本编辑框中
	 * 
	 * @param res 系统资源对象
	 * @param mContentEdit
	 *            文本编辑框
	 * @param emotions
	 *            表情资源对象
	 */
	public static void addEmotions(final Resources res, EditText mContentEdit, final Resource emotions) {
		String newText = emotions.getResourceStr();
		SpannableString ss = new SpannableString(newText);
		BitmapDrawable drawable = new BitmapDrawable(res, YIResourceUtils.getResourceBitmap(emotions.getRes_id(),
						EB_RESOURCE_TYPE.EB_RESOURCE_EMOTION.ordinal()));
		drawable.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
		ImageSpan dspan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
		ss.setSpan(dspan, 0, newText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mContentEdit.getText().insert(mContentEdit.getSelectionStart(), ss);
	}

	/**
	 * 展示html代码（包含图片）
	 * 
	 * @param res 系统资源对象
	 * @param html 消息内容
	 * @param smallEmotion 表情图标是否以小尺寸显示
	 * @return
	 */
	public static CharSequence getTipCharSequence(final Resources res, final String html, final boolean smallEmotion) {
		if (StringUtils.isBlank(html)) {
			return "";
		}
		
		String newHtml = html.replaceAll("\r\n", "<br/>");
		newHtml = newHtml.replaceAll("\n", "<br/>");
		newHtml = newHtml.replaceAll("\r", "<br/>");
		CharSequence charSequence = Html.fromHtml(newHtml, new ImageGetter() {
			@Override
			public Drawable getDrawable(String source) {
				BitmapDrawable drawable = new BitmapDrawable(res, YIResourceUtils.getResourceBitmap(
						Long.valueOf(source), EB_RESOURCE_TYPE.EB_RESOURCE_EMOTION.ordinal()));
				
				int right = 60, bottom = 60;
				if (smallEmotion)
					right = bottom =40;
				
				drawable.setBounds(0, 0, right, bottom);// 设置表情图片的显示大小
				return drawable;
			}
		}, null);
		return charSequence;
	}
	
//	public static CharSequence getTipCharSequence(final Resources res, final String html, final boolean smallEmotion, final int length) {
//		CharSequence charSequence = getTipCharSequence(res, html, smallEmotion);
//		if (charSequence.length() > length + 1) {
//			return charSequence.subSequence(0, length) + "...";
//		} else {
//			return charSequence;
//		}
//	}
	
	//消息通知设置
	public static int NOTIFICATION_SETTING_MESSAGE_NEW = 1;		//接收新消息通知
	public static int NOTIFICATION_SETTING_MESSAGE_DETAILS = 2;	//通知显示消息详情
	public static int NOTIFICATION_SETTING_MESSAGE_SOUND = 4;		//声音
	public static int NOTIFICATION_SETTING_MESSAGE_VIBRATE = 8;	//振动
	
	
	public static String PENDINGINTENT_TYPE_ACTIVITY 	=	"activity";
	public static String PENDINGINTENT_TYPE_SERVICE 	= 	"service";
	public static String PENDINGINTENT_TYPE_BROADCASE 	= 	"broadcase";
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	/**
	 * 发送通知栏消息
	 * @param context
	 * @param icon
	 * @param title
	 * @param content
	 * @param number
	 * @param notificationIntent
	 * @param intentType
	 */
	public static void sendNotificationMsg(Context context, int icon, CharSequence title, 
			CharSequence content, int number, Intent notificationIntent, String intentType) {
		
		//检查通知提醒的配置
		SharedPreferences preferences = context.getSharedPreferences("notificationSetting", Context.MODE_PRIVATE);
		boolean enableSound = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_SOUND), true);
		boolean enableVibrate = preferences.getBoolean(String.valueOf(UIUtils.NOTIFICATION_SETTING_MESSAGE_VIBRATE), true);
		
		//声音和震动
		int defaults = 0 | (enableSound?Notification.DEFAULT_SOUND:0) | (enableVibrate?Notification.DEFAULT_VIBRATE:0);
		
		// 定义NotificationManager
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		PendingIntent pendingIntent = null;
		if (intentType==PENDINGINTENT_TYPE_ACTIVITY)
			pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		else if (intentType==PENDINGINTENT_TYPE_SERVICE)
			pendingIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		else if (intentType==PENDINGINTENT_TYPE_BROADCASE)
			pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle(title);
		//builder.setContentInfo("");
		builder.setContentText(content);
		builder.setSmallIcon(R.drawable.ic_launcher);
		//builder.setTicker("新消息");
		//builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(pendingIntent);
		builder.setDefaults(defaults);
		
		//Notification mNotification = new Notification(icon, "[" + title + "]" + content, System.currentTimeMillis());
		//mNotification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		//mNotification.defaults = Notification.DEFAULT_SOUND;
		Notification mNotification = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mNotification = builder.build();
		} else {
			mNotification = builder.getNotification();
		}
		
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
		
		// if (mNotification.contentView != null) {
		// AppAccountInfo appInfo = EntboostCache.getAppInfo();
		// if (appInfo != null && appInfo.getEnt_logo_url() != null) {
		// Class<?> clazz;
		// try {
		// clazz = Class.forName("com.android.internal.R$id");
		// Field field = clazz.getField("icon");
		// field.setAccessible(true);
		// int id_icon = field.getInt(null);
		// mNotification.contentView.setImageViewBitmap(id_icon,
		// AbFileUtil.getBitmapFromSDCache(
		// appInfo.getEnt_logo_url(),
		// AbImageUtil.SCALEIMG, 32, 32));
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		mNotificationManager.notify(messageNotificationID, mNotification);
	}

	//删除通知栏通知
	public static void cancelNotificationMsg(Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(messageNotificationID);
	}
	
	/**
	 * 实现文本复制功能
	 * 
	 * @param content
	 */
	public static void copy(String content, Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
	}

	/**
	 * 实现粘贴功能 add by wangqianzhou
	 * 
	 * @param context
	 * @return
	 */
	public static String paste(Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		return cmb.getText().toString().trim();
	}

	//暂存自定义进度显示框
	private static Dialog customProgressDialog;
	
	/**
	 * 创建自定义的进度显示框
	 * @param context 上下文
	 * @param msg 描述内容
	 * @return
	 */
	private static Dialog createLoadingDialog(Context context, String msg) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
		
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
		ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
		TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
		
		// 加载动画
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context, R.anim.loading_animation);
		// 使用ImageView显示动画
		spaceshipImage.startAnimation(hyperspaceJumpAnimation);
		tipTextView.setText(msg);// 设置加载信息

		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

		loadingDialog.setCancelable(false);// 不可以用“返回键”取消
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
		return loadingDialog;
	}
	
	/**
	 * 显示进度框
	 * @param context 上下文
	 * @param message 描述内容
	 */
	public static void showProgressDialog(Context context, String message) {
		if (customProgressDialog == null) {
			customProgressDialog = createLoadingDialog(context, message);
			// 设置点击屏幕Dialog不消失
			customProgressDialog.setCanceledOnTouchOutside(false);
			customProgressDialog.show();
		}
	}
	
	/**
	 * 关闭进度框.
	 */
	public static void removeProgressDialog() {
		if (customProgressDialog != null) {
			try {
				customProgressDialog.dismiss();
			} catch (Exception e) {
			}
			customProgressDialog = null;
		}
	}
}
