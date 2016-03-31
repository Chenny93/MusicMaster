package com.android.musicclient.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.musicclient.R;

/**
 * 批量加载图片工具类
 */
public class ImageLoader {
	private  Context context;
	private ListView listView;
	//声明用于保存缓存图片的Map
	private  Map<String, SoftReference<Bitmap>> cache=new HashMap<String, SoftReference<Bitmap>>();
	//声明任务集合  存储图片下载任务
	private List<ImageLoaderTask> tasks=new ArrayList<ImageLoaderTask>();
	//声明工作线程  轮循任务集合
	private Thread workThread;
	private boolean isLoop=true;
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_LOAD_IMAGE_SUCCESS:
				//图片已经下载完成 
				ImageLoaderTask task=(ImageLoaderTask)msg.obj;
				//position
				int position=task.position;
				ImageView view=(ImageView)listView.findViewWithTag(position);
				//如果可以找到imageView
				if(view!=null){
					//如果bitmap不是null
					if(task.bitmap!=null){
						view.setImageBitmap(task.bitmap);
					}else{
						view.setImageResource(R.drawable.musicicon);
					}
				}
				break;
			}
		}
	};
	public static final int HANDLER_LOAD_IMAGE_SUCCESS=0;

	public ImageLoader(Context context) {}
	

	public ImageLoader(Context context, ListView listView) {
		this.context=context;
		this.listView=listView;
		//对工作线程进行初始化 并且启动
		workThread=new Thread(){
			@Override
			public void run() {
				//不断的轮循任务集合  
				while(isLoop){
					//如果集合不是空集
					if(!tasks.isEmpty()){
						ImageLoaderTask task=tasks.remove(0);
						//获取请求路径  image/xxxx.jpg
						Bitmap bitmap=loadBitmap(task.path);
						task.bitmap=bitmap;
						//给handler发送消息 
						//消息中携带ImageLoaderTask
						//把bitmap 设置到 ImageView中
						Message msg=new Message();
						msg.what=HANDLER_LOAD_IMAGE_SUCCESS;
						msg.obj=task;
						handler.sendMessage(msg);
					}else{
						//空集合的话  工作线程等待 
						try {
							synchronized (workThread) {
								workThread.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		//启动工作线程
		workThread.start();
	}
	
	/**
	 * 通过图片的path  获取图片对象
	 * @param path
	 * @return
	 */
	public   Bitmap loadBitmap(String path){
		String uri=GlobalConsts.BASEURL+path;
		try {
			HttpEntity entity=HttpUtils.send(HttpUtils.REQTYPE_GET, uri, null);
			//把entity 转成 Bitmap
			byte[] bytes=EntityUtils.toByteArray(entity);
			Bitmap bitmap=BitmapUtils.loadBitmap(bytes, 60, 60);
			//把bitmap存入内存缓存
			cache.put(path, new SoftReference<Bitmap>(bitmap));
			//把bitmap存入文件缓存
			//targetFile:  /data/data/com.tarena.musicclient/cache/images/xxx.jpg
			File targetFile=new File(context.getCacheDir(), path);
			BitmapUtils.save(bitmap, targetFile);
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public void displayImage(String albumpic, ImageView ivAlbum,int position) {
		//给imageView设置不同的tag 
		//当图片在工作线程下载完毕后，
		//handler中使用该tag值可以找到相应ImageView
		ivAlbum.setTag(position);
		//判断缓存中是否已经下载过该图片
		//如果缓存中有，则直接获取使用。
		SoftReference<Bitmap> ref=cache.get(albumpic);
		if(ref!=null && ref.get()!=null){
			Log.i("info", "当前图片是从缓存中读取的....");
			//缓存内有    直接使用
			Bitmap bitmap=ref.get();
			ivAlbum.setImageBitmap(bitmap);
			return;
		}
		//内存缓存中没有图片 去文件缓存中读取
		//filePath: /data/data/com.tarena.musicclient/image/xxx.jpg
		String filePath=new File(context.getCacheDir(),albumpic).getAbsolutePath();
		Bitmap bitmap=BitmapUtils.loadBitmp(filePath);
		if(bitmap!=null){ //文件缓存中有
			Log.i("info", "从文件缓存中读取的图片...");
			//存入内存缓存一份  供下次读取
			cache.put(albumpic, new SoftReference<Bitmap>(bitmap));
			ivAlbum.setImageBitmap(bitmap);
			return;
		}
		
		//给holder的imageView设置图片
		//图片需要从服务端下载
		//不可以再此启动工作线程
		//向任务集合中添加一个图片下载任务
		ImageLoaderTask task=new ImageLoaderTask();
		task.path=albumpic;
		task.position=position;
		//把task add 到tasks集合中
		tasks.add(task);
		synchronized (workThread) {
			//唤醒工作线程  继续轮循集合
			workThread.notify();
		}
		
	}

	/**
	 * 封装一个图片下载任务对象
	 */
	class ImageLoaderTask{
		int position; //
		String path; //图片路径
		Bitmap bitmap; //通过路径下载后的图片对象
	}

	public void stopThread() {
		isLoop=false;
		//唤醒工作线程 执行一次循环
		synchronized (workThread) {
			workThread.notify();
		}
	}
	
}

