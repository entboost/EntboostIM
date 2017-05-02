package com.entboost.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;

import android.util.Log;

import com.entboost.Log4jLog;

/**
 * 文件工具
 * 
 * @author ye 2013-03-04
 * 
 */
public class FileUtils {
	
	/** The tag. */
	private static String TAG = FileUtils.class.getSimpleName();
	private static String LONG_TAG = FileUtils.class.getName();
	
	private static final String SIZE_B = "B";
	private static final String SIZE_KB = "KB";
	private static final String SIZE_MB = "MB";
	private static final String SIZE_GB = "GB";
	private static final DecimalFormat mDecimalFormat = new DecimalFormat(
			"0.##");

	public static File createDirectory(String path) {
		File dirFile = new File(path);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		return dirFile;
	}

	/**
	 * 获取目录下所有的文件名
	 * 
	 * @param directory
	 * @return
	 */
	public static String[] listFileNames(String directory) {
		try {
			File dirFile = new File(directory);
			if (!dirFile.exists()) {
				return null;
			}
			return dirFile.list();
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * 将inputstream保存至文件
	 * 
	 * @param inSream
	 * @param file
	 * @throws Exception
	 */
	public static void readAsFile(InputStream inSream, File file)
			throws Exception {
		FileOutputStream outStream = new FileOutputStream(file);
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inSream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inSream.close();
	}

	public static void save(byte[] bytes, String filePath, String name) {
		createDirectory(filePath);
		File file = new File(filePath + File.separator + name);
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(bytes);
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 从文件获取byte数组
	 * 
	 * @param file
	 * @return
	 */
	public static byte[] getBytesFromFile(File file) {
		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	/**
	 * 从文件url获取文件名
	 * 
	 * @param fileUrl
	 * @return 返回例如：4244446210.jpg
	 */
	public static String getFileNameFromUrl(String fileUrl) {
		// http://192.168.0.115:8080/ihuimi/gm/cr/2012/0309/4244446210.jpg
		int beginIndex = fileUrl.lastIndexOf('/');
		return fileUrl.substring(beginIndex + 1);
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static boolean copyFile(String oldPath, String newPath) {
		return copyFile(new File(oldPath), new File(newPath));
	}

	public static boolean copyFile(File oldFile, File newFile) {
		try {
			InputStream in = new FileInputStream(oldFile); // 读入原文件
			FileOutputStream fos = new FileOutputStream(newFile);
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = in.read(buffer)) != -1) {
				fos.write(buffer, 0, count);
			}
			in.close();
			return true;
		} catch (Exception e) {
			Log4jLog.e(LONG_TAG, "复制单个文件操作出错", e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			Log4jLog.e(LONG_TAG, "复制整个文件夹内容操作出错", e);
			e.printStackTrace();

		}

	}

	/**
	 * 
	 * 删除文件夹（并且删除文件夹自身）
	 * 
	 * @param folderPath
	 *            文件夹完整绝对路径
	 */

	public static boolean delFolder(String folderPath) throws Exception {
		// 删除完里面所有内容
		boolean flag = delAllFile(folderPath);
		File myFilePath = new File(folderPath);
		// 删除空文件夹
		myFilePath.delete();
		return flag;
	}

	public static boolean delFile(String pathName) {
		File file = new File(pathName);
		if (!file.exists()) {
			return false;
		}
		if (file.isFile()) {
			file.delete();
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 删除指定文件夹下所有文件（不删除文件夹自身）
	 * 
	 * @param path
	 *            文件夹完整绝对路径
	 */

	public static boolean delAllFile(String path) throws Exception {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}

		if (!file.isDirectory()) {
			return flag;
		}

		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}

			if (temp.isFile()) {
				temp.delete();
			}

			if (temp.isDirectory()) {
				// 先删除文件夹里面的文件
				delAllFile(path + "/" + tempList[i]);
				// 再删除空文件夹
				delFolder(path + "/" + tempList[i]);
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File file) throws Exception {
		if (file == null) {
			return 0l;
		}
		if (file.exists() && file.isFile()) {
			return file.length();
		}
		return 0l;
	}

	/**
	 * 获取文件夹大小（单位Byte）
	 * 
	 * @param filePath
	 *            文件夹路径
	 * @return
	 * @throws Exception
	 */
	public static long getFolderSize(File filePath) throws Exception {
		long size = 0;
		File flist[] = filePath.listFiles();
		if (flist == null) {
			throw new RuntimeException("找不到目录或该文件不是一个目录： " + filePath.getPath());
		}

		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFolderSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	/**
	 * 转换文件大小，单位(B/KB/MB/GB)
	 * 
	 * @param fileSize
	 * @return
	 */
	public static String formatFileSize(long fileSize) {

		String fileSizeString;
		if (fileSize < 1024) {
			fileSizeString = mDecimalFormat.format((double) fileSize) + SIZE_B;
		} else if (fileSize < 1048576) {
			fileSizeString = mDecimalFormat.format((double) fileSize / 1024)
					+ SIZE_KB;
		} else if (fileSize < 1073741824) {
			fileSizeString = mDecimalFormat.format((double) fileSize / 1048576)
					+ SIZE_MB;
		} else {
			fileSizeString = mDecimalFormat
					.format((double) fileSize / 1073741824) + SIZE_GB;
		}
		return fileSizeString;
	}

	/**
	 * 获取文件个数
	 * 
	 * @param file
	 * @return
	 */
	public static long getlist(File file) {
		long size = 0;
		File flist[] = file.listFiles();
		size = flist.length;
		// 递归求取目录文件个数
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getlist(flist[i]);
				size--;
			}
		}
		return size;
	}

	public static void renameFile(String oldFilePathName, String newFilePathName) {
		File oldFile = new File(oldFilePathName);
		if (oldFile.exists()) {
			oldFile.renameTo(new File(newFilePathName));
		}
	}

	public static boolean isExistFile(String directory, String fileName) {
		String[] names = listFileNames(directory);
		if (names != null) {
			for (String name : names) {
				if (StringUtils.equals(fileName, name)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String increaseExistFileName(String directory, String fileName) {
		String[] files = listFileNames(directory);
		if(files==null){
			return fileName;
		}
		int num = 0;
		String ef1 = StringUtils.substringBeforeLast(fileName, ".");
		String vf1 = StringUtils.substringAfterLast(fileName, ".");
		for (String file : files) {
			String ef = StringUtils.substringBeforeLast(file, ".");
			String vf = StringUtils.substringAfterLast(file, ".");
			if ((StringUtils.equals(ef, ef1) || ef.matches("^" + ef1
					+ "\\(\\d+\\)"))
					&& StringUtils.equals(vf, vf1)) {
				++num;
			}
		}
		if (num == 0) {
			return fileName;
		} else {
			return ef1 + "(" + num + ")." + vf1;
		}
	}

}
