package com.entboost.im.comparator;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.entboost.Log4jLog;

/**
 * 比较器工具类
 */
public class ComparatorUtils {
	
	private static String LONG_TAG = ComparatorUtils.class.getName();	
	
	 /**
	  * 字符串在指定字符集后按字节比较
	  * 常见字符集名称：ASCII字符集、GB2312字符集、BIG5字符集、GB18030字符集、Unicode字符集等
	  * java默认Unicode
	  * 
	  * @param str1 字符串1
	  * @param str2 字符串2
	  * @param charset 字符集
	  */
	 public static int compareBytesUsingCharset(String str1, String str2, String charset) {
		 try {
			 byte[] b1 = str1.getBytes(charset);
			 byte[] b2 = str2.getBytes(charset);
			 
			 int len1 = b1.length;
			 int len2 = b2.length;
			 
			 int len = Math.min(len1, len2);
			 int k=0;
			 while(k < len){
				 byte byte1 = b1[k];
				 byte byte2 = b2[k];
				 if(byte1!=byte2)
					 return byte1-byte2;
				 k++;
			 }
			 
			 return len1 - len2;
		 } catch (UnsupportedEncodingException e) {
			 Log4jLog.e(LONG_TAG, e);
		 }
		 
		 return 0;
	 }
	 
	 /**
	  * 把字节数组转换为十六进制字符串
	  * @param bytes 字节数组
	  * @return
	  */
	 public static String toHex(byte[] bytes) {
	    String result = "";
	    for (int i = 0; i < bytes.length; i++) {
		    String hex = Integer.toHexString(bytes[i] & 0xFF);
		    if (hex.length() == 1) { //高四位设置为0
		        hex = '0' + hex;
		    }
		    if (i==0) {
		    	result = hex;
	        } else {
	            result = result +/*" "+*/ hex;
	        }
	    }
	    return result;
	 }
	 
	/**
	 * 把字符串转换为可按指定字符集比较的字符串
	 * @param str 待转换的字符串
	 * @param charset 字符集(目前仅支持GBK)
	 * @return
	 */
	 public static List<String> getCharCode(String str, String charset) {
	     List<String> list  = new ArrayList<String>();
	     
	     char[] ch = str.toCharArray();
	     for (int i = 0; i < ch.length; i++) {
	         //如果第N个是中文  
	         if ((ch[i]>= 0x4E00)&&(ch[i]<=0x9FA5)) { //0x9fbb
	             byte[] gbk = null;
	             try {
	            	 gbk = (str.substring(i, i+1)).getBytes(charset);
	             } catch (UnsupportedEncodingException e) {
	            	 Log4jLog.e(LONG_TAG, e);
	             }
	             //转换为十六进制字符串后保存
	             list.add(toHex(gbk));
	         } else {
	        	 //直接保存
	             String temp = str.substring(i, i+1);
	             list.add("   " + temp); //3个空格符
	         }
	     }
	     
	     return list;  
	 }
	 
	 /**
	  * 字符串比较(遵循GBK编码)
	  * @param str1 字符串1
	  * @param str2 字符串2
	  * @return
	  */
	 public static int strcmpUsingGBK(String str1, String str2) {
		 String charset = "GBK";
		 int nResult = 0;
	    
		 List<String> list1= getCharCode(str1, charset);
		 List<String> list2= getCharCode(str2, charset);
	    
		 //获取各串的长度，取用最短的那个
		 int len1 = list1.size();
		 int len2 = list2.size();
		 if (len1 < len2) {
			 int k = -1;
			 //逐个对比
			 for (int i = 0; i < len1; i++) {
				 nResult = (list1.get(i).toString()).compareTo(list2.get(i).toString());
				 if (nResult<0) {
					 break;
				 } else if (nResult==0) {
					 k+=1;
					 //相当于比完了，还相等，则说明长度长的大
					 if (k == len1-1) {
						 nResult=-1;
					 }
	            } else if (nResult>0) {
	            	break;
	            }
			 }
		 } else if(len1 > len2) {
			 int k = -1;
			 for (int i = 0; i < len2; i++) {
				 nResult = (list1.get(i).toString()).compareTo(list2.get(i).toString());
				 if (nResult<0) {
					 break;
				 } else if (nResult==0) {
					 //相当于比完了，还相等，则说明长度长的大  
					 k+=1;
					 if (k == len2-1) {
						 nResult=1;
					 }
	            } else if (nResult>0) {
	            	break;
	            } 
			 }
		 } else { //字符串长度相等时，此情况下，才有相等的情况出现
			 int k = -1;
			 for (int i = 0; i < len2; i++) {
				 nResult = (list1.get(i).toString()).compareTo(list2.get(i).toString());
				 if (nResult<0) {
					 break;
				 } else if (nResult==0) {
					 //相当于比完了，还相等，则说明此时一定是相等
					 k+=1;
					 if (k == len2-1) {
						 //System.out.println(""+"相等");
					 }
	        	} else if (nResult>0) {
	        		break;
	        	}
			 }
		 }
	    
		 return nResult; 
	 }
	 
}
