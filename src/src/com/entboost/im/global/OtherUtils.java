package com.entboost.im.global;

import java.util.List;

import com.entboost.Log4jLog;
import com.entboost.im.chat.ChatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class OtherUtils {
	private static String LONG_TAG = OtherUtils.class.getName();
	
	/**
	 * 根据Uri获取文件的绝对路径，解决Android4.4以上版本Uri转换
	 * 
	 * @param activity
	 * @param fileUri
	 */
	@TargetApi(19)
	public static String getFileAbsolutePath(Activity context, Uri fileUri) {
		if (context == null || fileUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, fileUri)) {
			if (isExternalStorageDocument(fileUri)) {
				String docId = DocumentsContract.getDocumentId(fileUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(fileUri)) {
				String id = DocumentsContract.getDocumentId(fileUri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(fileUri)) {
				String docId = DocumentsContract.getDocumentId(fileUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} // MediaStore (and general)
		else if ("content".equalsIgnoreCase(fileUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(fileUri))
				return fileUri.getLastPathSegment();
			return getDataColumn(context, fileUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
			return fileUri.getPath();
		}
		return null;
	}	
	
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String[] projection = { MediaStore.Images.Media.DATA };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}	
	
	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}	

    /** 
     * 判断某个服务是否正在运行的方法 
     * @param mContext 
     * @param serviceName 包名+服务的类名（例如：net.yunim.service.EBClientService)
     * @param mark 标记，仅用于打印日志
     * @return true代表正在运行，false代表服务没有正在运行 
     */
    public static boolean isServiceWork(Context mContext, String serviceName, String mark) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList.size() <= 0) {  
            return false;
        }
        
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
            	Log4jLog.d(LONG_TAG, "service '" + serviceName +"' is found, mark = " + mark);
                isWork = true;
                break;
            }
        }
        
        if (!isWork)
        	Log4jLog.e(LONG_TAG, "service '" + serviceName +"' is not exist, mark = " + mark);
        return isWork;  
    }
    
	/**
	 * 检测服务是否正在运行
	 * @param times 次数
	 * @param waitting 如非正在运行，是否延时
	 * @param mark 标记
	 * @return
	 */
	public static boolean checkServiceWork(int times, boolean waitting, String mark) {
		String serviceName = "net.yunim.service.EbClientService";
		
		boolean isWork = OtherUtils.isServiceWork(MyApplication.getInstance(), serviceName, mark);
		if (!isWork && waitting) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return isWork;
	}
}
