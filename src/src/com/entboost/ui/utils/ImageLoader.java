package com.entboost.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.entboost.Log4jLog;

public class ImageLoader {
	
	/** The tag. */
	private static String TAG = ImageLoader.class.getSimpleName();
	private static String LONG_TAG = ImageLoader.class.getName();
	
	private static ImageLoader mImageLoader;
	private LruCache<String, Bitmap> mMemoryCache;// 小图和缩略图
	private int MAXMEMONRY = (int) (Runtime.getRuntime().maxMemory() / 1024);

	private ImageLoader() {
		mMemoryCache = new LruCache<String, Bitmap>(MAXMEMONRY / 8) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// 重写此方法来衡量每张图片的大小，默认返回图片数量。
				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
			}

			@Override
			protected void entryRemoved(boolean evicted, String key,
					Bitmap oldValue, Bitmap newValue) {
				Log4jLog.i(LONG_TAG, "hard cache is full , push to soft cache");

			}
		};
	}

	public static ImageLoader getInstance() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}

	public void clearCache() {
		if (mMemoryCache != null) {
			if (mMemoryCache.size() > 0) {
				Log4jLog.i(LONG_TAG, "mMemoryCache.size() "
						+ mMemoryCache.size());
				mMemoryCache.evictAll();
				Log4jLog.i(LONG_TAG, "mMemoryCache.size()"
						+ mMemoryCache.size());
			}
			mMemoryCache = null;
		}
	}

	public Bitmap getBitmapFormSrc(Class<?> cls, String src) {
		Bitmap bit = mMemoryCache.get(src);
		if (bit == null) {
			try {
				bit = BitmapFactory.decodeStream(cls.getResourceAsStream(src));
				mMemoryCache.put(src, bit);
			} catch (Exception e) {
				// Log4jLog.d(LONG_TAG, "获取图片异常：" + e.getMessage());
			}
		}
		return bit;
	}

	public Bitmap getBitmapFormSrc(String src) {
		return getBitmapFormSrc(this.getClass(), src);
	}
	
	public Bitmap getBitmap(String key, int screenWidth, int screenHeight) {
		if (key == null) {
			return null;
		}
		Bitmap bm = mMemoryCache.get(key+"screenWH");
		if (bm != null) {
			return bm;
		} else {
			Bitmap bitmap = null;
			bitmap = AbBitmapUtils.getBitmap(screenWidth, screenHeight, key);
			if (bitmap == null) {
				return null;
			}
			mMemoryCache.put(key+"screenWH", bitmap);
			return bitmap;
		}
	}

	public Bitmap getBitmap(String key, int width) {
		if (key == null) {
			return null;
		}
		Bitmap bm = mMemoryCache.get(key+"W");
		if (bm != null) {
			return bm;
		} else {
			Bitmap bitmap = null;
			bitmap = AbBitmapUtils.getBitmap(key, width);
			if (bitmap == null) {
				return null;
			}
			mMemoryCache.put(key+"W", bitmap);
			return bitmap;
		}
	}

	/**
	 * 移除缓存
	 * 
	 * @param key
	 */
	public void removeImageCache(String key) {
		if (key != null) {
			if (mMemoryCache != null) {
				Bitmap bm = mMemoryCache.remove(key);
				if (bm != null)
					bm.recycle();
			}
		}
	}

}
