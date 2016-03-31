package com.android.musicclient.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;

import com.android.musicclient.R;
import com.android.musicclient.util.GlobalConsts;
import com.android.musicclient.util.HttpUtils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadService extends IntentService{

	private static final int NOTIFICATION_ID = 1001;

	//添加无参数构造方法
	public DownloadService() {
		super("DownloadService");
	}
	
	public DownloadService(String name) {
		super(name);
	}

	/**
	 * 在工作线程中执行
	 * 当调用startService时，IntentService
	 * 将会把onHandleIntent中的逻辑添加
	 * 消息队列中等待执行，工作线程将会
	 * 轮循该消息队列，把队列中的消息
	 * 对象一一获取，并且一一执行。
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		//执行下载    不需要启动线程
		String path=intent.getStringExtra("path");
		//path :    music/xxxx.mp3
		String httpPath=GlobalConsts.BASEURL+path;
		//发送普通的get请求
		try {
			HttpEntity entity=HttpUtils.send(HttpUtils.REQTYPE_GET, httpPath, null);
			//读取entity中的音乐数据 
			//并且边读取数据 边向SD卡中保存
			// /mnt/sdcard/Music/musics/xxx.mp3
			File targetFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), path);
			if(!targetFile.getParentFile().exists()){
				targetFile.getParentFile().mkdirs();
			}
			if(targetFile.exists()){
				Toast.makeText(getApplicationContext(), "音乐已存在", Toast.LENGTH_SHORT).show();
				return;
			}
			//发送通知提示文件开始下载
			clearNotification();
//			sendNotification("文件开始下载...", "文件下载", "文件开始下载");
			sendNotification("已加入下载队列", "音乐地带", "文件开始下载");
			InputStream is=entity.getContent();
			FileOutputStream fos = new FileOutputStream(targetFile);
			//边读  边写  边保存
			byte[] buffer = new byte[1024*100];
			int length = 0;
			long total = entity.getContentLength();
			long current = 0;
			while((length = is.read(buffer)) != -1){
				fos.write(buffer, 0, length);
				fos.flush();
				//发送通知提示文件下载进度
				current+=length;
				//求百分比
				double text = Math.floor(100.0*current / total);
//				sendNotification("音乐文件下载", "音乐下载", "下载进度:"+text+"%");
				sendNotification("音乐文件下载", "音乐地带", "下载进度:"+text+"%");
			}
			fos.close();
			//清除通知之后再次发送
			clearNotification();
			sendNotification("音乐下载完成", "音乐下载", "下载完成");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 清除通知
	 */
	public void clearNotification(){
		//1. NotificationManager
		NotificationManager manager=
		(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);		
	}
	
	/**
	 * 发送通知的方法
	 * @param ticker  滚动消息
	 * @param title   通知信息的标题
	 * @param text   通知内容
	 */
	public void sendNotification(String ticker, String title, String text){
		//1. NotificationManager
		NotificationManager manager=(NotificationManager)
				getSystemService(Context.NOTIFICATION_SERVICE);
		//2. Notification.Builder
		Notification.Builder builder=new Notification.Builder( this);
		builder.setContentTitle(title)
			.setContentText(text)
			.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.musicicon))
//			.setSmallIcon(R.drawable.musicicon)
			.setSubText("")
			.setTicker(ticker)
			.setContentInfo("");
		Notification n=builder.build();
		//3. manager.notify()
		manager.notify(NOTIFICATION_ID, n);
	}
	
}



