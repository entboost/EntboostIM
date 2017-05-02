package com.entboost.ui.base.activity;

import java.util.Iterator;
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
	
	/**
	 * 单例模式
	 */
	public static MyActivityManager getInstance() {
	    return instance;
	}
	
	/**
	 * 把一个activity压入栈中
	 * @param actvity
	 */
	public void pushOneActivity(Activity actvity) {
	    activityStack.add(actvity);
	    Log4jLog.d(LONG_TAG, "after push one activity, stack size = " + activityStack.size());
	}
	
//	//获取栈顶的activity，先进后出原则
//	public Activity getLastActivity() {
//	    return activityStack.lastElement();
//	}
	
	/**
	 * 移除一个activity
	 * @param activity
	 */
	public void popOneActivity(Activity activity) {
	    if (activityStack.size() > 0) {
	        if (activity != null) {
	            activity.finish();
	            activityStack.remove(activity);
	            Log4jLog.d(LONG_TAG, "after pop one activity, stack size = " + activityStack.size());
	        }
	    }
	}
	
	/**
	 * 弹出在某个activity之上的所有activity
	 * @param className
	 * @return
	 */
	public Activity popToActivity(String className) {
		Activity activity = null;
		Activity existActivity = getActivity(className);
		
		if (existActivity!=null) {
			do {
				activity = activityStack.peek();
				if (activity!=existActivity) {
					activityStack.pop();
					activity.finish();
					continue;
				}
				
				break;
			} while(!activityStack.empty());
		}
		
		return existActivity;
	}
	
    /**
     * 退出栈中所有Activity
     */
    public void clearAllActivity() {
        while (!activityStack.isEmpty()) {
            Activity activity = activityStack.pop();
            if (activity != null)
                activity.finish();
            Log4jLog.d(LONG_TAG, "clear one activity, stack size = " + activityStack.size());
        }
    }
    
    /**
     * 获取顶部的Activity(不影响栈中元素)
     * @return
     */
    public Activity getTopActivity() {
    	if (activityStack.size() > 0) {
    		return activityStack.get(activityStack.size()-1);
    	}
    	return null;
    }
    
    /**
     * 判断某个Activity的实例是否存在
     * @param className 类名
     * @return
     */
    public boolean isActivityExist(String className) {
    	Iterator<Activity> it = activityStack.iterator();
    	while (it.hasNext()) {
    		Activity activity = (Activity)it.next();
    		if (activity.getClass().getName().equals(className)) {
    			return true;
    		}
    	}
    	
    	Log4jLog.d(LONG_TAG, "activity instance for " + className + " is not exist");
    	return false;
    }
    
    /**
     * 获取一个指定的Activity实例
     * @param className 类全名
     * @return
     */
    public Activity getActivity(String className) {
    	Iterator<Activity> it = activityStack.iterator();
    	while (it.hasNext()) {
    		Activity activity = (Activity)it.next();
    		if (activity.getClass().getName().equals(className)) {
    			return activity;
    		}
    	}
    	return null;
    }
}
