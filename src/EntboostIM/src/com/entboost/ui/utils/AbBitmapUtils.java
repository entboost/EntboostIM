package com.entboost.ui.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.entboost.Log4jLog;
import com.entboost.utils.AbFileUtil;
import com.entboost.utils.AbStrUtil;

public class AbBitmapUtils {
	
	private static String LONG_TAG = AbBitmapUtils.class.getName();
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 3;
	public static final int BOTTOM = 4;
	public static final int IMAGE_MAX_HEIGHT = 800;
	public static final int IMAGE_MAX_WIDTH = 480;

	/** */
	/**
	 * 图片去色,返回灰度图片
	 * 
	 * @param bmpOriginal
	 *            传入的图片
	 * @return 去色后的图片
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/** */
	/**
	 * 去色同时加圆角
	 * 
	 * @param bmpOriginal
	 *            原图
	 * @param pixels
	 *            圆角弧度
	 * @return 修改后的图片
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal, int pixels) {
		return toRoundCorner(toGrayscale(bmpOriginal), pixels);
	}

	/** */
	/**
	 * 把图片变成圆角
	 * 
	 * @param bitmap
	 *            需要修改的图片
	 * @param pixels
	 *            圆角的弧度
	 * @return 圆角图片
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/** */
	/**
	 * 使圆角功能支持BitampDrawable
	 * 
	 * @param bitmapDrawable
	 * @param pixels
	 * @return
	 */
	public static BitmapDrawable toRoundCorner(BitmapDrawable bitmapDrawable,
			int pixels) {
		Bitmap bitmap = bitmapDrawable.getBitmap();
		bitmapDrawable = new BitmapDrawable(toRoundCorner(bitmap, pixels));
		return bitmapDrawable;
	}

	/**
	 * 读取路径中的图片，然后将其转化为缩放后的bitmap
	 * 
	 * @param path
	 */
	public static void saveBefore(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回bm为空
		options.inJustDecodeBounds = false;
		// 计算缩放比
		int be = (int) (options.outHeight / (float) 200);
		if (be <= 0)
			be = 1;
		options.inSampleSize = 2; // 图片长宽各缩小二分之一
		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
		bitmap = BitmapFactory.decodeFile(path, options);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		System.out.println(w + " " + h);
		// savePNG_After(bitmap,path);
		saveJPGE_After(bitmap, path);
	}

	/**
	 * 保存图片为PNG
	 * 
	 * @param bitmap
	 * @param name
	 */
	public static void savePNG_After(Bitmap bitmap, String name) {
		File file = new File(name);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			AbFileUtil.closeOutputStream(out);
		}
	}

	/**
	 * 保存图片为JPEG
	 * 
	 * @param bitmap
	 * @param path
	 */
	public static void saveJPGE_After(Bitmap bitmap, String path) {
		File file = new File(path);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			AbFileUtil.closeOutputStream(out);
		}
	}

	/**
	 * 水印
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createBitmapForWatermark(Bitmap src, Bitmap watermark) {
		if (src == null) {
			return null;
		}
		int w = src.getWidth();
		int h = src.getHeight();
		int ww = watermark.getWidth();
		int wh = watermark.getHeight();
		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);
		// draw src into
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
		// draw watermark into
		cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newb;
	}

	/**
	 * 图片合成
	 * 
	 * @return
	 */
	public static Bitmap potoMix(int direction, Bitmap... bitmaps) {
		if (bitmaps.length <= 0) {
			return null;
		}
		if (bitmaps.length == 1) {
			return bitmaps[0];
		}
		Bitmap newBitmap = bitmaps[0];
		// newBitmap = createBitmapForFotoMix(bitmaps[0],bitmaps[1],direction);
		for (int i = 1; i < bitmaps.length; i++) {
			newBitmap = createBitmapForFotoMix(newBitmap, bitmaps[i], direction);
		}
		return newBitmap;
	}

	private static Bitmap createBitmapForFotoMix(Bitmap first, Bitmap second,
			int direction) {
		if (first == null) {
			return null;
		}
		if (second == null) {
			return first;
		}
		int fw = first.getWidth();
		int fh = first.getHeight();
		int sw = second.getWidth();
		int sh = second.getHeight();
		Bitmap newBitmap = null;
		if (direction == LEFT) {
			newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, sw, 0, null);
			canvas.drawBitmap(second, 0, 0, null);
		} else if (direction == RIGHT) {
			newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, 0, 0, null);
			canvas.drawBitmap(second, fw, 0, null);
		} else if (direction == TOP) {
			newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, 0, sh, null);
			canvas.drawBitmap(second, 0, 0, null);
		} else if (direction == BOTTOM) {
			newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, 0, 0, null);
			canvas.drawBitmap(second, 0, fh, null);
		}
		return newBitmap;
	}

	/**
	 * 将Bitmap转换成指定大小
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap createBitmapBySize(Bitmap bitmap, int width, int height) {
		return Bitmap.createScaledBitmap(bitmap, width, height, true);
	}

	/**
	 * Drawable 转 Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmapByBD(Drawable drawable) {
		BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		return bitmapDrawable.getBitmap();
	}

	/**
	 * Bitmap 转 Drawable
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Drawable bitmapToDrawableByBD(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}

	/**
	 * byte[] 转 bitmap
	 * 
	 * @param b
	 * @return
	 */
	public static Bitmap bytesToBimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * 根据图片字节数组，对图片可能进行二次采样，不致于加载过大图片出现内存溢出
	 * 
	 * @param bytes
	 * @return
	 */
	public static Bitmap getBitmapByBytes(byte[] bytes) {

		// 对于图片的二次采样,主要得到图片的宽与高
		int width = 0;
		int height = 0;
		int sampleSize = 1; // 默认缩放为1
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 仅仅解码边缘区域
		// 如果指定了inJustDecodeBounds，decodeByteArray将返回为空
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		// 得到宽与高
		height = options.outHeight;
		width = options.outWidth;

		// 图片实际的宽与高，根据默认最大大小值，得到图片实际的缩放比例
		while ((height / sampleSize > IMAGE_MAX_HEIGHT)
				|| (width / sampleSize > IMAGE_MAX_WIDTH)) {
			sampleSize *= 2;
		}

		// 不再只加载图片实际边缘
		options.inJustDecodeBounds = false;
		// 并且制定缩放比例
		options.inSampleSize = sampleSize;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	}
	
	/**
	 * 根据图片字节数组，对图片可能进行二次采样，不致于加载过大图片出现内存溢出
	 * @param bytes 字节数组
	 * @param maxWidth 最大宽度
	 * @param maxHeight 最大高度
	 * @return
	 */
	public static Bitmap getBitmapByBytes(byte[] bytes, int maxWidth, int maxHeight) {
		// 对于图片的二次采样,主要得到图片的宽与高
		int width = 0;
		int height = 0;
		int sampleSize = 1; // 默认缩放为1
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 仅仅解码边缘区域
		// 如果指定了inJustDecodeBounds，decodeByteArray将返回为空
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		// 得到宽与高
		height = options.outHeight;
		width = options.outWidth;

		// 图片实际的宽与高，根据默认最大大小值，得到图片实际的缩放比例
		while ((height / sampleSize > maxHeight)
				|| (width / sampleSize > maxWidth)) {
			sampleSize *= 2;
		}

		// 不再只加载图片实际边缘
		options.inJustDecodeBounds = false;
		// 并且制定缩放比例
		options.inSampleSize = sampleSize;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	}

	/**
	 * bitmap 转 byte[]
	 * 
	 * @param bm
	 * @return
	 */
	public static byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 将图片按照某个角度进行旋转
	 * @param bitmap
	 * @param degree
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
		if (bitmap == null)
			return null;
		
		Bitmap returnBm = null;
		
		Matrix mtx = new Matrix();
		mtx.postRotate(degree);
		
		try {
			returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);
		} catch (OutOfMemoryError e) {
			Log4jLog.e(LONG_TAG, e);
	    }
		
	    return returnBm;
	}

	/**
	 * 读取图片的旋转角度
	 * @param filePath
	 * @return
	 */
	public static int getBitmapDegree(String filePath) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(filePath);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			Log4jLog.e(LONG_TAG, e);
		}
		return degree;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	
	/**
	 * 获取指定路径的图片对象
	 * @param filePath 文件路径
	 * @return 图片对象
	 */
	public static Bitmap getBitmap(String filePath) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(filePath);
			return BitmapFactory.decodeFile(filePath);
		} catch (FileNotFoundException e) {
			Log4jLog.e(LONG_TAG, e);
		}catch (OutOfMemoryError e) {
			Log4jLog.e(LONG_TAG, e);
		} finally {
			AbFileUtil.closeInputStream(is);
		}
		return null;
	}
	
	/**
	 * 获取指定路径的等比例定宽的压缩图片
	 * @param filePath 文件路径
	 * @param width 固定宽度
	 * @return 图片对象
	 */
	public static Bitmap getBitmap(String filePath, int width) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		if (options.outWidth == 0 || options.outHeight == 0) {
			return null;
		}
		
		if(width<options.outWidth) {
			int w = options.outWidth;
			int height = options.outHeight * width / options.outWidth;
			options.outWidth = width;
			options.outHeight = height;
			int be = 1;// be=1表示不缩放
			be = (int) (w / options.outWidth);
			if (be <= 0) {
				be = 1;
			}
			options.inSampleSize = be;// 设置缩放比例
		}
		
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		
		FileInputStream is = null;
		try {
			is = new FileInputStream(filePath);
			return BitmapFactory.decodeFileDescriptor(is.getFD(), null, options);
		} catch (FileNotFoundException e) {
			Log4jLog.e(LONG_TAG, e);
		} catch (IOException e) {
			Log4jLog.e(LONG_TAG, e);
		} catch (OutOfMemoryError e) {
			Log4jLog.e(LONG_TAG, e);
		} finally {
			AbFileUtil.closeInputStream(is);
		}
		return null;
	}

	/**
	 * 获取指定路径的等比例自适应长宽的压缩图片
	 * @param width 长度
	 * @param height 宽度
	 * @param filePath 文件路径
	 * @return 图片对象
	 */
	public static Bitmap getBitmap(int width, int height, String filePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// 获取到这个图片的原始宽度和高度
		int picWidth = options.outWidth;
		int picHeight = options.outHeight;

		// isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
		options.inSampleSize = 1;
		// 根据屏的大小和图片大小计算出缩放比例
		if (picWidth > picHeight) {
			if (picWidth > width)
				options.inSampleSize = picWidth / width;
		} else {
			if (picHeight > height)
				options.inSampleSize = picHeight / height;
		}
		// 这次再真正地生成一个有像素的，经过缩放了的bitmap
		options.inJustDecodeBounds = false;
		
		FileInputStream is = null;
		try {
			is = new FileInputStream(filePath);
			return BitmapFactory.decodeFileDescriptor(is.getFD(), null, options);
		} catch (FileNotFoundException e) {
			Log4jLog.e(LONG_TAG, e);
		} catch (IOException e) {
			Log4jLog.e(LONG_TAG, e);
		} catch (OutOfMemoryError e) {
			Log4jLog.e(LONG_TAG, e);
		} finally {
			AbFileUtil.closeInputStream(is);
		}
		return null;
	}

	/**
	 * 压缩图片
	 * @param image
	 * @return
	 */
	public static byte[] compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 200) { // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
			
			//防止压缩率低于正常值
			if (options<=0) {
				options+=10;
				break;
			}
		}
		return baos.toByteArray();
	}
	
	/**
	 * 保存图片至系统相册(如果图片非正常角度，将自动旋转为正常角度)
	 * @param context 上下文
	 * @param filePath 图片文件路径
	 * @param maxWidth 图片最大宽度
	 * @return
	 */
	public static Uri insertImage(Context context, String filePath, Integer maxWidth) {
		Bitmap bitmap = AbBitmapUtils.getBitmap(filePath, maxWidth);
		
		if (bitmap!=null) {
			int degree = AbBitmapUtils.getBitmapDegree(filePath);
			//旋转图片
			if (degree>0) {
				Log4jLog.d(LONG_TAG, "photo degree:" + degree);
				Bitmap newBitmap = AbBitmapUtils.rotateBitmap(bitmap, degree);
				
				if(!bitmap.isRecycled() && newBitmap!=bitmap) {
					bitmap.recycle(); //回收图片所占的内存
					System.gc(); //提醒系统及时回收
				}
				
				bitmap = newBitmap;
				AbBitmapUtils.saveJPGE_After(bitmap, filePath);
			}
			
			//保存至相册
			String title = AbStrUtil.getFileNameWithoutSuffix(filePath);
			long size = new File(filePath).length();
			Uri imgUri = AbBitmapUtils.insertImage(context, bitmap, size, title, null, title);
			
			if(!bitmap.isRecycled()) {
				bitmap.recycle(); //回收图片所占的内存
				System.gc(); //提醒系统及时回收
			}
			
			return imgUri;
		}
		
		return null;
	}
	
	/**
	 * 往系统相册插入图片
	 * @param context 上下文
	 * @param source 图片对象
	 * @param size 图片大小(字节数)
	 * @param title 标题
	 * @param description 描述
	 * @param displayName 名称
	 * @return 指向新图片对象的Uri
	 */
	public static Uri insertImage(Context context, Bitmap source, long size, String title, String description, String displayName) {
		ContentResolver cr = context.getContentResolver();
		
		long time = System.currentTimeMillis();
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, title);
		values.put(MediaStore.Images.Media.DESCRIPTION, description);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.Images.Media.SIZE, size);
		values.put(MediaStore.Images.Media.DATE_ADDED, time/1000); //单位秒
		values.put(MediaStore.Images.Media.DATE_MODIFIED, time/1000); //单位秒
		values.put(MediaStore.Images.Media.DATE_TAKEN, time); //单位毫秒
		
		values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
//		values.put(MediaStore.Images.Media.LATITUDE,36);
//		values.put(MediaStore.Images.Media.LONGITUDE, 120);
//		values.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "6666");
		
		Uri uri = null;
		try {
			uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			
			if (uri!=null) {
				OutputStream imageOut = cr.openOutputStream(uri);
				try {
					source.compress(Bitmap.CompressFormat.JPEG, 100, imageOut);
				} finally {
					imageOut.close(); 
				}
				
				long id = ContentUris.parseId(uri);
				
				// Wait until MINI_KIND thumbnail is generated.
				Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);  
				// This is for backward compatibility.
				storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
				
				if (!miniThumb.isRecycled()) {
					miniThumb.recycle();
	            	System.gc();
	            }
				
                //对某些不更新相册的应用程序强制刷新
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri2 = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+"/image.jpg"));//固定写法
                intent.setData(uri2);
                context.sendBroadcast(intent);
			}
		} catch (Exception e) {
			Log4jLog.e(LONG_TAG, "Failed to insert image", e);
			if (uri != null) {
				cr.delete(uri, null, null);
				uri = null;
			}
		}
		
		return uri;
	}
	
    private static final void storeThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {
        Matrix matrix = new Matrix();
        
        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();
        matrix.setScale(scaleX, scaleY);  
        
        Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        
        //基本属性
        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND, kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());
        
        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);
        OutputStream thumbOut = null;
        try {
            thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            
            if (!thumb.isRecycled()) {
            	thumb.recycle();
            	System.gc();
            }
            //return thumb;
        } catch (FileNotFoundException e) {
        	Log4jLog.e(LONG_TAG, e);
            //return null;
        } catch (IOException e) {
        	Log4jLog.e(LONG_TAG, e);
            //return null;
        } finally {
        	AbFileUtil.closeOutputStream(thumbOut);
        }
    }
}
