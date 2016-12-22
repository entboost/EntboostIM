package com.entboost.im.chat;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.yunim.service.cache.FileCacheUtils;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.ui.base.view.popmenu.PopMenuConfig;
import com.entboost.ui.base.view.popmenu.PopMenuItem;
import com.entboost.ui.base.view.popmenu.PopMenuItemOnClickListener;

public class FileListActivity extends EbActivity {

	private ListView listView;
	private FileAdapter fileAdapter;
	private List<String> selected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_file_list);
		listView = (ListView) findViewById(R.id.filelist);
		
		fileAdapter = new FileAdapter(this);
		fileAdapter.setInput(getFileList());
		listView.setAdapter(fileAdapter);
		selected = fileAdapter.getSelected();
		
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String fileName = (String) fileAdapter.getItem(position);
				openFile(new File(fileName));
				return false;
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String fileName = (String) fileAdapter.getItem(position);
				ImageView selectImg = (ImageView) view.findViewById(R.id.user_select);
				Drawable srcImg = selectImg.getDrawable();
				if (srcImg == null) {
					selectImg.setImageResource(R.drawable.uitb_57);
					selected.add(fileName);
				} else {
					selectImg.setImageDrawable(null);
					if (selected.contains(fileName)) {
						selected.remove(fileName);
					}
				}
			}
		});
		
		initMenu();
	}

	public List<String> getFileList() {
		List<String> list = new ArrayList<String>();
		File f = new File(FileCacheUtils.getFilePath()); // 打开文件目录
		File[] files = f.listFiles(); // 获取当前目录中文件列表
		for (File file : files) {
			list.add(file.getAbsolutePath());
		}
		
		//排序
		Collections.sort(list, Collator.getInstance(java.util.Locale.CHINA));
		
		return list;
	}

	//初始化右上角按钮
	public void initMenu() {
		PopMenuConfig config = new PopMenuConfig();
		config.setBackground_resId(R.drawable.popmenu);
		config.setTextColor(Color.WHITE);
		config.setShowAsDropDownYoff(28);
		
		this.getTitleBar().addRightImageButton(R.drawable.ic_action_delete, null, new PopMenuItem(new PopMenuItemOnClickListener() {
			@Override
			public void onItemClick() {
				if (selected.size() > 0) {
					showDialog("提示", "是否删除文件！", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for (String fileName : selected) {
								deleteFile(new File(fileName));
							}
							fileAdapter.setInput(getFileList());
							fileAdapter.notifyDataSetChanged();
						}

					});
				} else {
					showToast("未选择要删除的文件！");
				}
			}
		}));
	}

	public void deleteFile(File file) {
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // 是删除的意思;
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					this.deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
				}
			}
		} else {
			showToast("文件不存在！");
		}
	}

	private void openFile(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		// 获取文件file的MIME类型
		String type = getMIMEType(file);
		// 设置intent的data和Type属性。
		intent.setDataAndType(Uri.fromFile(file), type);
		// 跳转
		try {
			startActivity(intent);
		} catch (Exception e) {
			showToast("文件无法打开！");
		}

	}

	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param file
	 */
	private String getMIMEType(File file) {
		String type = "*/*";
		String fName = file.getName();
		// 获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0) {
			return type;
		}
		/* 获取文件的后缀名 */
		String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		if (StringUtils.isBlank(end))
			return type;
		// 在MIME和文件类型的匹配表中找到对应的MIME类型。
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

	private final String[][] MIME_MapTable = {
			// {后缀名，MIME类型}
			{ ".3gp", "video/3gpp" },
			{ ".apk", "application/vnd.android.package-archive" },
			{ ".asf", "video/x-ms-asf" },
			{ ".avi", "video/x-msvideo" },
			{ ".bin", "application/octet-stream" },
			{ ".bmp", "image/bmp" },
			{ ".c", "text/plain" },
			{ ".class", "application/octet-stream" },
			{ ".conf", "text/plain" },
			{ ".cpp", "text/plain" },
			{ ".doc", "application/msword" },
			{ ".docx",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			{ ".xls", "application/vnd.ms-excel" },
			{ ".xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			{ ".exe", "application/octet-stream" },
			{ ".gif", "image/gif" },
			{ ".gtar", "application/x-gtar" },
			{ ".gz", "application/x-gzip" },
			{ ".h", "text/plain" },
			{ ".htm", "text/html" },
			{ ".html", "text/html" },
			{ ".jar", "application/java-archive" },
			{ ".java", "text/plain" },
			{ ".jpeg", "image/jpeg" },
			{ ".jpg", "image/jpeg" },
			{ ".js", "application/x-javascript" },
			{ ".log", "text/plain" },
			{ ".m3u", "audio/x-mpegurl" },
			{ ".m4a", "audio/mp4a-latm" },
			{ ".m4b", "audio/mp4a-latm" },
			{ ".m4p", "audio/mp4a-latm" },
			{ ".m4u", "video/vnd.mpegurl" },
			{ ".m4v", "video/x-m4v" },
			{ ".mov", "video/quicktime" },
			{ ".mp2", "audio/x-mpeg" },
			{ ".mp3", "audio/x-mpeg" },
			{ ".mp4", "video/mp4" },
			{ ".mpc", "application/vnd.mpohun.certificate" },
			{ ".mpe", "video/mpeg" },
			{ ".mpeg", "video/mpeg" },
			{ ".mpg", "video/mpeg" },
			{ ".mpg4", "video/mp4" },
			{ ".mpga", "audio/mpeg" },
			{ ".msg", "application/vnd.ms-outlook" },
			{ ".ogg", "audio/ogg" },
			{ ".pdf", "application/pdf" },
			{ ".png", "image/png" },
			{ ".pps", "application/vnd.ms-powerpoint" },
			{ ".ppt", "application/vnd.ms-powerpoint" },
			{ ".pptx",
					"application/vnd.openxmlformats-officedocument.presentationml.presentation" },
			{ ".prop", "text/plain" }, { ".rc", "text/plain" },
			{ ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
			{ ".sh", "text/plain" }, { ".tar", "application/x-tar" },
			{ ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
			{ ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
			{ ".wmv", "audio/x-ms-wmv" },
			{ ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
			{ ".z", "application/x-compress" },
			{ ".zip", "application/x-zip-compressed" }, { "", "*/*" } };
}
