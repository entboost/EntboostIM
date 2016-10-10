package com.entboost.ui.base.activity;

import java.util.Stack;

import android.app.Activity;

import com.entboost.Log4jLog;

public class MyActivityManager {
	
	/** The tag. */
	private static String TAG = MyActivityManager.class.getSimpleName();
	private static String LONG_TAG = MyActivityManager.class.getName();
	
	private static MyActivityManager instance = new MyActivityManager();
	private Stack<Activity> activityStack = new Stack<Activity>(); //activity栈
	
	private MyActivityManager() {
		
	}
	
	//单例模式
	public static MyActivityManager getInstance() {
	    return instance;
	}
	
	//把一个activity压入栈中
	public void pushOneActivity(Activity actvity) {
	    activityStack.add(actvity);
	    Log4jLog.d(LONG_TAG, "after push one activity, stack size = " + activityStack.size());
	}
	
//	//获取栈顶的activity，先进后出原则
//	public Activity getLastActivity() {
//	    return activityStack.lastElement();
//	}
	
	//移除一个activity
	public void popOneActivity(Activity activity) {
	    if (activityStack.size() > 0) {
	        if (activity != null) {
	            activity.finish();
	            activityStack.remove(activity);
	            Log4jLog.d(LONG_TAG, "after pop one activity, stack size = " + activityStack.size());
	        }
	    }
	}
	
    // 退出栈中所有Activity
    public void clearAllActivity() {
        while (!activityStack.isEmpty()) {
            Activity activity = activityStack.pop();  
            if (activity != null)
                activity.finish();
            Log4jLog.d(LONG_TAG, "clear one activity, stack size = " + activityStack.size());
        }
    }
}
