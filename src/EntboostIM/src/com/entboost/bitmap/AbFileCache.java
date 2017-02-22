/*
 * Copyright (C) 2015 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.entboost.bitmap;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.entboost.global.AbAppData;
import com.entboost.utils.AbFileUtil;
import com.entboost.utils.AbStrUtil;

// TODO: Auto-generated Javadoc
/**
 * 描述：文件SD卡缓存管理.
 *
 * @author amsoft.cn
 * @date：2013-5-23 上午10:10:53
 * @version v1.0
 */

public class AbFileCache {
	
	/** The tag. */
	private static String TAG = "AbFileCache";
	
	/** The Constant D. */
	private static final boolean D = AbAppData.DEBUG;
	
	/** 10MB. */
	public static int maxCacheSize = 10 * 1024 * 1024; 
	
	/** 当前缓存大小. */
	public static int cacheSize = 0; 
	
	/** 文件缓存(文件名，文件) */
	private static final HashMap<String, File> fileCache = new HashMap<String, File>();     
	
    /**锁对象*/
    public static final ReentrantLock lock = new ReentrantLock();
	
    static {
    	//初始化缓存
    	AbFileUtil.initFileCache();
    }
    
	/**
	 * 描述：从缓存中获取这个File.
	 *
	 * @param name 文件名
	 * @return the file from mem cache
	 */
	public  static File getFileFromCache(String name) {  
		  return fileCache.get(name);  
	} 
	
	/**
	 * 描述：增加一个File到缓存.
	 *
	 * @param name   文件名
	 * @param bitmap the bitmap
	 */
	public  static void addFileToCache(String name,File file){
		try {
			lock.lock();
			if(AbStrUtil.isEmpty(name)){
				return;
			}
			
			if (getFileFromCache(name) == null && file!=null) {
				fileCache.put(name, file);
			}
			
			//当前大小大于预定缓存空间
	        if (cacheSize > maxCacheSize) {
	        	//释放部分文件
	        	AbFileUtil.freeCacheFiles();
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}  finally{
			lock.unlock();
		}
	}
	
	/**
	 * 描述：从缓存删除.
	 *
	 * @param name   文件名
	 */
	public  static void removeFileFromCache(String name){
		try {
			lock.lock();
			if (getFileFromCache(name) != null) {  
				fileCache.remove(name);
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}  finally{
			lock.unlock();
		}
	}
	
	/**
	 * 描述：清空缓存的Bitmap.
	 */
	public  static void removeAllFileFromCache() { 
		    AbFileUtil.removeAllFileCache();
		    fileCache.clear();
	}


	/**
	 * 设置缓存空间大小 
	 * @param cacheSize
	 */
	public static void setMaxCacheSize(int cacheSize) {
		AbFileCache.maxCacheSize = cacheSize;
	}
	
	/**
     * 
     * 描述：缓存文件夹的大小
     * @return
     * @throws 
     */
	public static int getCacheSize() {
		return cacheSize;
	}
	
    
		
}
