package com.entboost.im.global;

import java.util.List;

import net.yunim.eb.signlistener.EntboostIMListener;
import net.yunim.service.Entboost;
import net.yunim.service.entity.DynamicNews;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;

import com.entboost.Log4jLog;
import com.entboost.im.MainActivity;
import com.entboost.im.R;
import com.entboost.im.exception.CrashHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MyApplication extends Application {
	
	private static String LONG_TAG = MyApplication.class.getName();
	
	//应用对象
	private static MyApplication myInstance;
	//欢迎页面
//	private WelcomeActivity welcomeActivity;

	public static long appid = 278573612908l;

	public static String appkey = "ec1b9c69094db40d9ada80d657e08cc6";
	
	//IM事件监听类
	private EntboostIMListener imListener;
	//是否已登录
	private boolean isLogin = false;
	//是否界面模式
	private boolean inInterface = false;
	//是否显示通知栏消息
	private boolean showNotificationMsg = true;
	
	//默认提报加载选项
	private DisplayImageOptions defaultImgOptions;
	//用户头像图标加载选项
	private DisplayImageOptions userImgOptions;	
	//内置应用图标加载选项
	private DisplayImageOptions funcInfoImgOptions;

	public DisplayImageOptions getDefaultImgOptions() {
		return defaultImgOptions;
	}

	public DisplayImageOptions getUserImgOptions() {
		return userImgOptions;
	}

	public DisplayImageOptions getFuncInfoImgOptions() {
		return funcInfoImgOptions;
	}

	public boolean isShowNotificationMsg() {
		return showNotificationMsg;
	}

	public void setShowNotificationMsg(boolean showNotificationMsg) {
		this.showNotificationMsg = showNotificationMsg;
	}

	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	
	public boolean isInInterface() {
		return inInterface;
	}

	public void setInInterface(boolean inInterface) {
		this.inInterface = inInterface;
	}

	@Override
	public void onTerminate() {
//		welcomeActivity = null;
		if (imListener!=null) {
			Entboost.removeListener(imListener);
			imListener = null;
		}
		myInstance = null;
		
		super.onTerminate();
		
//		ActivityManager am = (ActivityManager) this.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//		am.killBackgroundProcesses(getPackageName());
//		System.exit(0);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//仅在主进程执行初始化，否则忽略
		if (!checkMainProcess()) {
			Log4jLog.d(LONG_TAG, "leave application create");
			return;
		}
		
		myInstance = this;
		initEbConfig("MyApplication");
		
		//创建IM事件监听实例
		imListener = new EntboostIMListener() {
			@Override
			public void onReceiveDynamicNews(DynamicNews news) {
				//非界面模式才需要发送通知栏消息
				if (!isInInterface()) {
					MainActivity.handleReceiveDynamicNews(MyApplication.this, news);
				}
			}
		};
		Entboost.addListener(imListener);
		
		Log4jLog.i(LONG_TAG, "My Application Created");
	}

	public void initEbConfig(String type) {
		Log4jLog.i(LONG_TAG, "initEbConfig start, type=" + type);
		
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		
		Log4jLog.i(LONG_TAG, "initEbConfig 1");
		Entboost.setServiceListener(crashHandler);
		Log4jLog.i(LONG_TAG, "initEbConfig 2");
		Entboost.init(getApplicationContext());
		Log4jLog.i(LONG_TAG, "initEbConfig 3");
		Entboost.showSotpLog(false);
		Log4jLog.i(LONG_TAG, "initEbConfig 4");
		
		Builder imgConfig = new ImageLoaderConfiguration.Builder(this);
		imgConfig.threadPriority(Thread.NORM_PRIORITY - 2);// 设置线程的优先级
		imgConfig.diskCacheFileNameGenerator(new Md5FileNameGenerator());// 设置缓存文件的名字
		imgConfig.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		imgConfig.denyCacheImageMultipleSizesInMemory();// 当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
		imgConfig.tasksProcessingOrder(QueueProcessingType.LIFO);// 设置图片下载和显示的工作队列排序
		ImageLoader.getInstance().init(imgConfig.build());
		
		defaultImgOptions = createImgOptions(R.drawable.entboost_logo, R.drawable.entboost_logo, R.drawable.entboost_logo);
		userImgOptions = createImgOptions(0, R.drawable.default_user, R.drawable.default_user);
		funcInfoImgOptions = createImgOptions(0, R.drawable.default_app, R.drawable.default_app);
	}
	
	//检查当前进程是否主进程
    private boolean checkMainProcess() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        Log4jLog.d(LONG_TAG, "mainProcessName:" + mainProcessName + ", myPid:" + myPid);
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
	
	//创建ImageLoader加载选项
	private DisplayImageOptions createImgOptions(int loadingResid, int emptyResid, int failResid) {
		DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
		builder.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565);
		if (loadingResid!=0)
			builder.showImageOnLoading(loadingResid);
		if (failResid!=0)
			builder.showImageOnFail(failResid);
		if (emptyResid!=0)
			builder.showImageForEmptyUri(emptyResid);
		
		return builder.build();
	}

	public static MyApplication getInstance() {
		return myInstance;
	}
}
