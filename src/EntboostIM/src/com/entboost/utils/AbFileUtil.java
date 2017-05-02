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
package com.entboost.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.entboost.Log4jLog;
import com.entboost.bitmap.AbFileCache;
import com.entboost.global.AbAppData;
import com.entboost.ui.utils.AbImageUtil;

/**
 * 描述：文件操作类.
 *
 * @author amsoft.cn
 * @date 2011-12-10
 * @version v1.0
 */
public class AbFileUtil {
	
	/** The tag. */
	private static String TAG = AbFileUtil.class.getSimpleName();
	private static String LONG_TAG = AbFileUtil.class.getName();
	
	
	/** The Constant D. */
	private static final boolean D = AbAppData.DEBUG;
	
	/** 默认下载文件地址. */
	private static  String downPathRootDir = File.separator + "download" + File.separator;
	
    /** 默认下载图片文件地址. */
	private static  String downPathImageDir = downPathRootDir + "cache_images" + File.separator;
    
    /** 默认下载文件地址. */
	private static  String downPathFileDir = downPathRootDir + "cache_files" + File.separator;
	
	/**MB  单位B*/
	private static int MB = 1024*1024;
	
	/** 设置好的图片存储目录*/
	private static String imageDownFullDir = null;
	
	/** 设置好的文件存储目录*/
	private static String fileDownFullDir = null;
	
    /**剩余空间大于200M才使用缓存*/
	private static int freeSdSpaceNeededToCache = 200*MB;
	
	static{
		initImageDownFullDir();
		initFileDownFullDir();
	}
	
	/**
	 * 下载网络文件到SD卡中.如果SD中存在同名文件将不再下载
	 * @param url 要下载文件的网络地址
	 * @return 下载好的本地文件地址
	 */
	 public static String downFileToSD(String url,String dirPath){
		 InputStream in = null;
		 FileOutputStream fileOutputStream = null;
		 HttpURLConnection connection = null;
		 String downFilePath = null;
		 File file = null;
		 try {
	    	if(!isCanUseSD()){
	    		return null;
	    	}
            //先判断SD卡中有没有这个文件，不比较后缀部分比较
            String fileNameNoMIME  = getCacheFileNameFromUrl(url);
            File parentFile = new File(imageDownFullDir);
            File[] files = parentFile.listFiles();
            for(int i = 0; i < files.length; ++i){
                 String fileName = files[i].getName();
                 String name = fileName.substring(0,fileName.lastIndexOf("."));
                 if(name.equals(fileNameNoMIME)){
                     //文件已存在
                     return files[i].getPath();
                 }
            } 
            
			URL mUrl = new URL(url);
			connection = (HttpURLConnection)mUrl.openConnection();
			connection.connect();
            //获取文件名，下载文件
            String fileName  = getCacheFileNameFromUrl(url,connection);
            
            file = new File(imageDownFullDir,fileName);
            downFilePath = file.getPath();
            if(!file.exists()){
                file.createNewFile();
            }else{
                //文件已存在
                return file.getPath();
            }
			in = connection.getInputStream();
			fileOutputStream = new FileOutputStream(file);
			byte[] b = new byte[1024];
			int temp = 0;
			while((temp=in.read(b))!=-1){
				fileOutputStream.write(b, 0, temp);
			}
		}catch(Exception e){
			e.printStackTrace();
			Log4jLog.e(LONG_TAG, "有文件下载出错了");
			//检查文件大小,如果文件为0B说明网络不好没有下载成功，要将建立的空文件删除
			file.delete();
			downFilePath = null;
		}finally{
			try {
				if(in!=null){
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if(fileOutputStream!=null){
					fileOutputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if(connection!=null){
				    connection.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//加到缓存
		AbFileCache.addFileToCache(file.getName(), file);
		return downFilePath;
	 }
	 
	 /**
	  * 描述：通过文件的网络地址从SD卡中读取图片，如果SD中没有则自动下载并保存.
	  * @param url 文件的网络地址
	  * @param type 图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
	  * 如果设置为原图，则后边参数无效，得到原图
	  * @param width 新图片的宽
	  * @param height 新图片的高
	  * @return Bitmap 新图片
	  */
	 public static Bitmap getBitmapFromSDCache(String url,int type,int width, int height){
		 Bitmap bitmap = null;
		 try {
			 if(AbStrUtil.isEmpty(url)){
		    	return null;
		     }
			 
			 //SD卡不存在 或者剩余空间不足了就不缓存到SD卡了
			 if(!isCanUseSD() || freeSdSpaceNeededToCache < freeSpaceOnSD()){
				 bitmap = getBitmapFormURL(url,type,width,height);
				 return bitmap;
		     }
			 
			 if(type != AbImageUtil.ORIGINALIMG && ( width<=0 || height<=0)){
				 throw new IllegalArgumentException("缩放和裁剪图片的宽高设置不能小于0");
			 }
			 //下载文件，如果不存在就下载，存在直接返回地址
			 String downFilePath = downFileToSD(url,imageDownFullDir);
			 if(downFilePath != null){
				 //获取图片
				 return getBitmapFromSD(new File(downFilePath),type,width,height);
			 }else{
				 return null;
			 }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
		
	 }
	 
	 
	 /**
 	 * 描述：通过文件的本地地址从SD卡读取图片.
 	 *
 	 * @param file the file
 	 * @param type 图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
 	 * 如果设置为原图，则后边参数无效，得到原图
 	 * @param newWidth 新图片的宽
 	 * @param newHeight 新图片的高
 	 * @return Bitmap 新图片
 	 */
	 public static Bitmap getBitmapFromSD(File file,int type,int newWidth, int newHeight){
		 Bitmap bit = null;
		 try {
			 //SD卡是否存在
			 if(!isCanUseSD()){
		    	return null;
		     }
			 
			 if(type != AbImageUtil.ORIGINALIMG && ( newWidth<=0 || newHeight<=0)){
				 throw new IllegalArgumentException("缩放和裁剪图片的宽高设置不能小于0");
			 }
			 
			 //文件是否存在
			 if(!file.exists()){
				 return null;
			 }
			 
			 //文件存在
			 if(type == AbImageUtil.CUTIMG){
		 		bit = AbImageUtil.cutImg(file,newWidth,newHeight);
		 	 }else if(type == AbImageUtil.SCALEIMG){
			 	bit = AbImageUtil.scaleImg(file,newWidth,newHeight);
		 	 }else{
		 		bit = AbImageUtil.originalImg(file);
		 	 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bit;
	 }
	 
	 
	 /**
	  * 描述：将图片的byte[]写入本地文件.
	  * @param imgByte 图片的byte[]形势
	  * @param fileName 文件名称，需要包含后缀，如.jpg
	  * @param type 图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
	  * @param newWidth 新图片的宽
	  * @param newHeight 新图片的高
	  * @return Bitmap 新图片
	  */
     public static Bitmap getBitmapFormByte(byte[] imgByte,String fileName,int type,int newWidth, int newHeight){
    	   FileOutputStream fos = null;
    	   DataInputStream dis = null;
    	   ByteArrayInputStream bis = null;
    	   Bitmap b = null;
    	   File file = null;
    	   try {
    		   if(imgByte!=null){
    			   
    			   file = new File(imageDownFullDir+fileName);
    			   if(!file.exists()){
    			          file.createNewFile();
    			   }
    			   fos = new FileOutputStream(file);
    			   int readLength = 0;
    			   bis = new ByteArrayInputStream(imgByte);
    			   dis = new DataInputStream(bis);
    			   byte[] buffer = new byte[1024];
    			   
    			   while ((readLength = dis.read(buffer))!=-1) {
    				   fos.write(buffer, 0, readLength);
    			       try {
    						Thread.sleep(500);
    				   } catch (Exception e) {
    				   }
    			   }
    			   fos.flush();
    			   
    			   b = getBitmapFromSD(file,type,newWidth,newHeight);
    		   }
			   
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(dis!=null){
				try {
					dis.close();
				} catch (Exception e) {
				}
			}    
			if(bis!=null){
				try {
					bis.close();
				} catch (Exception e) {
				}
			}
			if(fos!=null){
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
        return  b;
     }
	 
     /**
      * 把字节数组转换为图片对象
      * @param bytes 字节数组
      * @param options 选项
      * @return 图片对象
      */
     public static Bitmap getBitmapFromBytes(byte[] bytes, Options options) {
	     if (bytes != null) {
             if (options != null)
            	 return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
             else
            	 return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	     }
	     return null;
     }
     
	/**
	 * 描述：根据URL从互连网获取图片.
	 * @param url 要下载文件的网络地址
	 * @param type 图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
	 * @param newWidth 新图片的宽
	 * @param newHeight 新图片的高
	 * @return Bitmap 新图片
	 */
	public static Bitmap getBitmapFormURL(String url,int type,int newWidth, int newHeight){
		Bitmap bit = null;
		try {
			bit = AbImageUtil.getBitmapFormURL(url, type, newWidth, newHeight);
	    } catch (Exception e) {
	    	 if(D)Log.d(TAG, "下载图片异常："+e.getMessage());
		}
		return bit;
	}
	
	/**
	 * 描述：获取src中的图片资源.
	 *
	 * @param src 图片的src路径，如（“image/arrow.png”）
	 * @return Bitmap 图片
	 */
	public static Bitmap getBitmapFormSrc(String src){
		Bitmap bit = null;
		try {
			bit = BitmapFactory.decodeStream(AbFileUtil.class.getResourceAsStream(src));
	    } catch (Exception e) {
	    	 if(D)Log.d(TAG, "获取图片异常："+e.getMessage());
		}
		return bit;
	}
	
	/**
     * 描述：通过文件的本地地址从SD卡读取图片.
     *
     * @param file the file
     * @return Bitmap 图片
     */
     public static Bitmap getBitmapFromSD(File file){
         Bitmap bitmap = null;
         try {
             //SD卡是否存在
             if(!isCanUseSD()){
                return null;
             }
             //文件是否存在
             if(!file.exists()){
                 return null;
             }
             //文件存在
             bitmap = AbImageUtil.originalImg(file);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
     }
	
	/**
	 * 描述：获取网络文件的大小.
	 *
	 * @param Url 图片的网络路径
	 * @return int 网络文件的大小
	 */
	public static int getContentLengthFormUrl(String Url){
		int mContentLength = 0;
		try {
			 URL url = new URL(Url);
			 HttpURLConnection mHttpURLConnection = (HttpURLConnection) url.openConnection();
			 mHttpURLConnection.setConnectTimeout(5 * 1000);
			 mHttpURLConnection.setRequestMethod("GET");
			 mHttpURLConnection.setRequestProperty("Accept","image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			 mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
			 mHttpURLConnection.setRequestProperty("Referer", Url);
			 mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
			 mHttpURLConnection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			 mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			 mHttpURLConnection.connect();
			 if (mHttpURLConnection.getResponseCode() == 200){
				 // 根据响应获取文件大小
				 mContentLength = mHttpURLConnection.getContentLength();
			 }
	    } catch (Exception e) {
	    	 e.printStackTrace();
	    	 if(D)Log.d(TAG, "获取长度异常："+e.getMessage());
		}
		return mContentLength;
	}
	
	/**
     * 获取文件名，通过网络获取.
     * @param url 文件地址
     * @return 文件名
     */
    public static String getRealFileNameFromUrl(String url){
        String name = null;
        try {
            if(AbStrUtil.isEmpty(url)){
                return name;
            }
            
            URL mUrl = new URL(url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) mUrl.openConnection();
            mHttpURLConnection.setConnectTimeout(5 * 1000);
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection.setRequestProperty("Accept","image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Referer", url);
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            mHttpURLConnection.setRequestProperty("User-Agent","");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == 200){
                for (int i = 0;; i++) {
                        String mine = mHttpURLConnection.getHeaderField(i);
                        if (mine == null){
                            break;
                        }
                        if ("content-disposition".equals(mHttpURLConnection.getHeaderFieldKey(i).toLowerCase())) {
                            Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                            if (m.find())
                                return m.group(1).replace("\"", "");
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log4jLog.e(LONG_TAG, "网络上获取文件名失败");
        }
        return name;
    }
	
	 
	/**
	 * 获取真实文件名（xx.后缀），通过网络获取.
	 * @param connection 连接
	 * @return 文件名
	 */
	public static String getRealFileName(HttpURLConnection connection){
		String name = null;
		try {
			if(connection == null){
				return name;
			}
			if (connection.getResponseCode() == 200){
				for (int i = 0;; i++) {
						String mime = connection.getHeaderField(i);
						if (mime == null){
							break;
						}
						// "Content-Disposition","attachment; filename=1.txt"
						// Content-Length
						if ("content-disposition".equals(connection.getHeaderFieldKey(i).toLowerCase())) {
							Matcher m = Pattern.compile(".*filename=(.*)").matcher(mime.toLowerCase());
							if (m.find()){
								return m.group(1).replace("\"", "");
							}
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log4jLog.e(LONG_TAG, "网络上获取文件名失败");
		}
		return name;
    }
	
	/**
     * 获取真实文件名（xx.后缀），通过网络获取.
     * @param connection 连接
     * @return 文件名
     */
    public static String getRealFileName(HttpResponse response){
        String name = null;
        try {
            if(response == null){
                return name;
            }
            //获取文件名
            Header[] headers = response.getHeaders("content-disposition");
            for(int i=0;i<headers.length;i++){
                 Matcher m = Pattern.compile(".*filename=(.*)").matcher(headers[i].getValue());
                 if (m.find()){
                     name =  m.group(1).replace("\"", "");
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log4jLog.e(LONG_TAG, "网络上获取文件名失败");
        }
        return name;
    }
    
    /**
     * 获取文件名（不含后缀）
     * @param url 文件地址
     * @return 文件名
     */
    public static String getCacheFileNameFromUrl(String url){
        if(AbStrUtil.isEmpty(url)){
            return null;
        }
        String name = null;
        try {
            name = AbMd5.MD5(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
    
	
	/**
     * 获取文件名（.后缀），外链模式和通过网络获取.
     * @param url 文件地址
     * @return 文件名
     */
    public static String getCacheFileNameFromUrl(String url,HttpResponse response){
        if(AbStrUtil.isEmpty(url)){
            return null;
        }
        String name = null;
        try {
            //获取后缀
            String suffix = getMIMEFromUrl(url,response);
            if(AbStrUtil.isEmpty(suffix)){
                suffix = ".ab";
            }
            name = AbMd5.MD5(url)+suffix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
	
	
	/**
	 * 获取文件名（.后缀），外链模式和通过网络获取.
	 * @param url 文件地址
	 * @return 文件名
	 */
	public static String getCacheFileNameFromUrl(String url,HttpURLConnection connection){
		if(AbStrUtil.isEmpty(url)){
			return null;
		}
		String name = null;
		try {
			//获取后缀
			String suffix = getMIMEFromUrl(url,connection);
			if(AbStrUtil.isEmpty(suffix)){
				suffix = ".ab";
			}
			name = AbMd5.MD5(url)+suffix;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
    }
	
	
	/**
	 * 获取文件后缀，本地.
	 * @param url 文件地址
	 * @return 文件后缀
	 */
	public static String getMIMEFromUrl(String url,HttpURLConnection connection){
		
		if(AbStrUtil.isEmpty(url)){
			return null;
		}
		String suffix = null;
		try {
			//获取后缀
			if(url.lastIndexOf(".")!=-1){
				 suffix = url.substring(url.lastIndexOf("."));
				 if(suffix.indexOf("/")!=-1 || suffix.indexOf("?")!=-1 || suffix.indexOf("&")!=-1){
					 suffix = null;
				 }
			}
			if(AbStrUtil.isEmpty(suffix)){
				 //获取文件名  这个效率不高
				 String fileName = getRealFileName(connection);
				 if(fileName!=null && fileName.lastIndexOf(".")!=-1){
					 suffix = fileName.substring(fileName.lastIndexOf("."));
				 }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return suffix;
    }
	
	/**
     * 获取文件后缀，本地和网络.
     * @param url 文件地址
     * @return 文件后缀
     */
    public static String getMIMEFromUrl(String url,HttpResponse response){
        
        if(AbStrUtil.isEmpty(url)){
            return null;
        }
        String mime = null;
        try {
            //获取后缀
            if(url.lastIndexOf(".")!=-1){
                mime = url.substring(url.lastIndexOf("."));
                 if(mime.indexOf("/")!=-1 || mime.indexOf("?")!=-1 || mime.indexOf("&")!=-1){
                     mime = null;
                 }
            }
            if(AbStrUtil.isEmpty(mime)){
                 //获取文件名  这个效率不高
                 String fileName = getRealFileName(response);
                 if(fileName!=null && fileName.lastIndexOf(".")!=-1){
                     mime = fileName.substring(fileName.lastIndexOf("."));
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mime;
    }
	
	/**
	 * 描述：从sd卡中的文件读取到byte[].
	 *
	 * @param path sd卡中文件路径
	 * @return byte[]
	 */
	public static byte[] getByteArrayFromSD(String path) {  
		byte[] bytes = null; 
		ByteArrayOutputStream out = null;
	    try {
	    	File file = new File(path);  
	    	//SD卡是否存在
			if(!isCanUseSD()){
				 return null;
		    }
			//文件是否存在
			if(!file.exists()){
				 return null;
			}
	    	
	    	long fileSize = file.length();  
	    	if (fileSize > Integer.MAX_VALUE) {  
	    	      return null;  
	    	}  

			FileInputStream in = new FileInputStream(path);  
		    out = new ByteArrayOutputStream(1024);  
			byte[] buffer = new byte[1024];  
			int size=0;  
			while((size=in.read(buffer))!=-1) {  
			   out.write(buffer,0,size);  
			}  
			in.close();  
            bytes = out.toByteArray();  
   
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(out!=null){
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	    return bytes;
    }  
	
	/**
	 * 描述：将byte数组写入文件.
	 *
	 * @param path the path
	 * @param content the content
	 * @param create the create
	 */
	 public static void writeByteArrayToSD(String path, byte[] content,boolean create){  
	    
		 FileOutputStream fos = null;
		 try {
	    	File file = new File(path);  
	    	//SD卡是否存在
			if(!isCanUseSD()){
				 return;
		    }
			//文件是否存在
			if(!file.exists()){
				if(create){
					File parent = file.getParentFile();
					if(!parent.exists()){
						parent.mkdirs();
						file.createNewFile();
					}
				}else{
				    return;
				}
			}
			fos = new FileOutputStream(path);  
			fos.write(content);  
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
   }  
	 
	/**
	 * 描述：SD卡是否能用.
	 *
	 * @return true 可用,false不可用
	 */
	public static boolean isCanUseSD() { 
	    try { 
	        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	    return false; 
    } 

	/**
	 * 描述：获得当前下载的地址.
	 * @return 下载的地址（默认SD卡download）
	 */
	public static String getDownPathImageDir() {
		return downPathImageDir;
	}

	/**
	 * 描述：设置图片文件的下载保存路径（默认SD卡download/cache_images）.
	 * @param downPathImageDir 图片文件的下载保存路径
	 */
	public static void setDownPathImageDir(String downPathImageDir) {
		AbFileUtil.downPathImageDir = downPathImageDir;
		initImageDownFullDir();
	}

	
	/**
	 * Gets the down path file dir.
	 *
	 * @return the down path file dir
	 */
	public static String getDownPathFileDir() {
		return downPathFileDir;
	}

	/**
	 * 描述：设置文件的下载保存路径（默认SD卡download/cache_files）.
	 * @param downPathFileDir 文件的下载保存路径
	 */
	public static void setDownPathFileDir(String downPathFileDir) {
		AbFileUtil.downPathFileDir = downPathFileDir;
		initFileDownFullDir();
	}
	
	/**
	 * 描述：获取默认的图片保存全路径.
	 *
	 * @return 完成的存储目录
	 */
	private static void initImageDownFullDir(){
		String pathDir = null;
	    try {
			if(!isCanUseSD()){
				return;
			}
			//初始化图片保存路径
			File fileRoot = Environment.getExternalStorageDirectory();
			File dirFile = new File(fileRoot.getAbsolutePath() + downPathImageDir);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			pathDir = dirFile.getPath();
			imageDownFullDir = pathDir;
		} catch (Exception e) {
		}
	}
	
	/**
	 * 描述：获取默认的文件保存全路径.
	 *
	 * @return 完成的存储目录
	 */
	private static void initFileDownFullDir(){
		String pathDir = null;
	    try {
			if(!isCanUseSD()){
				return;
			}
			//初始化图片保存路径
			File fileRoot = Environment.getExternalStorageDirectory();
			File dirFile = new File(fileRoot.getAbsolutePath() + downPathFileDir);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			pathDir = dirFile.getPath();
			fileDownFullDir = pathDir;
		} catch (Exception e) {
		}
	}
	
	
   /**
    * 初始化缓存
    */
    public static boolean initFileCache() {
    	
       try {
    	   
    	   AbFileCache.cacheSize = 0;
    	   
		   if(!isCanUseSD()){
				return false;
		   }
		   
		   File path = Environment.getExternalStorageDirectory();
		   File fileDirectory = new File(path.getAbsolutePath() + downPathImageDir);
	       File[] files = fileDirectory.listFiles();
	       if (files == null) {
	            return true;
	       }
    	   for (int i = 0; i < files.length; i++) {
    		   AbFileCache.cacheSize += files[i].length();
    		   AbFileCache.addFileToCache(files[i].getName(), files[i]);
	       }
	   } catch (Exception e) {
		   e.printStackTrace();
		   return false;
	   }
                                                       
       return true;
    }
	
	/**
    * 释放部分文件，
    * 当文件总大小大于规定的AbFileCache.maxCacheSize或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
    * 那么删除40%最近没有被使用的文件
    */
    public static boolean freeCacheFiles() {
    	
       try {
		   if(!isCanUseSD()){
				return false;
		   }
		   
		   File path = Environment.getExternalStorageDirectory();
		   File fileDirectory = new File(path.getAbsolutePath() + downPathImageDir);
	       File[] files = fileDirectory.listFiles();
	       if (files == null) {
	            return true;
	       }
	       
           int removeFactor = (int) ((0.4 * files.length) + 1);
           Arrays.sort(files, new FileLastModifSort());
           for (int i = 0; i < removeFactor; i++) {
        	   AbFileCache.cacheSize -= files[i].length();
               files[i].delete();
               AbFileCache.removeFileFromCache(files[i].getName());
           }
	       
	   } catch (Exception e) {
		   e.printStackTrace();
		   return false;
	   }
                                                       
       return true;
    }
	
    
    /**
     * 计算sdcard上的剩余空间
     */
    public static int freeSpaceOnSD() {
       StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
       double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
       return (int) sdFreeMB;
    }
	
    /**
     * 根据文件的最后修改时间进行排序
     */
    public static class FileLastModifSort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

	/**
	 * 
	 * 描述：剩余空间大于多少B才使用缓存
	 * @return
	 * @throws 
	 */
	public static int getFreeSdSpaceNeededToCache() {
		return freeSdSpaceNeededToCache;
	}

	/**
	 * 
	 * 描述：剩余空间大于多少B才使用缓存
	 * @param freeSdSpaceNeededToCache
	 * @throws 
	 */
	public static void setFreeSdSpaceNeededToCache(int freeSdSpaceNeededToCache) {
		AbFileUtil.freeSdSpaceNeededToCache = freeSdSpaceNeededToCache;
	}
	
	/**
     * 删除所有缓存文件
    */
    public static boolean removeAllFileCache() {
    	
       try {
		   if(!isCanUseSD()){
				return false;
		   }
		   
		   File path = Environment.getExternalStorageDirectory();
		   File fileDirectory = new File(path.getAbsolutePath() + downPathImageDir);
	       File[] files = fileDirectory.listFiles();
	       if (files == null) {
	            return true;
	       }
           for (int i = 0; i < files.length; i++) {
               files[i].delete();
           }
	   } catch (Exception e) {
		   e.printStackTrace();
		   return false;
	   }
       return true;
    }
    
    
    /**
     * 
     * 描述：读取Assets目录的文件内容
     * @param context
     * @param name
     * @return
     * @throws 
     */
    public static String readAssetsByName(Context context,String name,String encoding){
    	String text = null;
    	InputStreamReader inputReader = null;
    	BufferedReader bufReader = null;
    	try {  
    		 inputReader = new InputStreamReader(context.getAssets().open(name));
    		 bufReader = new BufferedReader(inputReader);
    		 String line = null;
    		 StringBuffer buffer = new StringBuffer();
    		 while((line = bufReader.readLine()) != null){
    			 buffer.append(line);
    		 }
    		 text = new String(buffer.toString().getBytes(), encoding);
         } catch (Exception e) {  
        	 e.printStackTrace();
         } finally{
			try {
				if(bufReader!=null){
					bufReader.close();
				}
				if(inputReader!=null){
					inputReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	return text;
    }
    
    /**
     * 
     * 描述：读取Raw目录的文件内容
     * @param context
     * @param id
     * @return
     * @throws 
     */
    public static String readRawByName(Context context,int id,String encoding){
    	String text = null;
    	InputStreamReader inputReader = null;
    	BufferedReader bufReader = null;
        try {
			inputReader = new InputStreamReader(context.getResources().openRawResource(id));
			bufReader = new BufferedReader(inputReader);
			String line = null;
			StringBuffer buffer = new StringBuffer();
			while((line = bufReader.readLine()) != null){
				 buffer.append(line);
			}
            text = new String(buffer.toString().getBytes(),encoding);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(bufReader!=null){
					bufReader.close();
				}
				if(inputReader!=null){
					inputReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        return text;
    }

	public static String getImageDownFullDir() {
		return imageDownFullDir;
	}

	public static void setImageDownFullDir(String imageDownFullDir) {
		AbFileUtil.imageDownFullDir = imageDownFullDir;
	}

	public static String getFileDownFullDir() {
		return fileDownFullDir;
	}

	public static void setFileDownFullDir(String fileDownFullDir) {
		AbFileUtil.fileDownFullDir = fileDownFullDir;
	}
    
    
	/**
	 * 关闭输入流
	 * @param is
	 */
	public static void closeInputStream(InputStream is) {
		if (is!=null) {
			try {
				is.close();
			} catch (IOException e) {
				Log4jLog.e(LONG_TAG, "closeInputStream error", e);
			}
		}
	}
	
	/**
	 * 关闭输出流
	 * @param os
	 */
	public static void closeOutputStream(OutputStream os) {
		if (os!=null) {
			try {
				os.close();
			} catch (IOException e) {
				Log4jLog.e(LONG_TAG, "closeOutputStream error", e);
			}
		}
	}
	
	/**
	 * 根据Uri获取文件的绝对路径，解决Android4.4以上版本Uri转换
	 * 
	 * @param context
	 * @param fileUri
	 */
	@TargetApi(19)
	public static String getFileAbsolutePath(Context context, Uri fileUri) {
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
	
	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
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
	
	
}
