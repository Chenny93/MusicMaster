package com.android.musicclient.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.musicclient.R;
import com.android.musicclient.entity.Music;
import com.android.musicclient.util.GlobalConsts;
import com.android.musicclient.util.HttpUtils;
import com.android.musicclient.util.ImageLoader;
/**
 * 单线程轮循机制——批量下载图片模型
 */
public class MusicAdapter extends BaseAdapter {
	
	private Context context;
	private List<Music> musics = new ArrayList<Music>();
	private LayoutInflater inflater;
	//声明工作线程  轮循任务集合
	private Thread workthread;
	private ListView lv;
	private boolean isloop=true;
	//声明任务集合  存储图片下载任务
	private List<ImageLoaderTask> tasks=new ArrayList<ImageLoaderTask>();
	private ImageLoader imageLoader;
	
	private static final int HANDLER_LOAD_IMAGE_SUCCESS=0;
	
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_LOAD_IMAGE_SUCCESS:
				//图片下载成功
				ImageLoaderTask task = (ImageLoaderTask) msg.obj;
				//position
				int position=task.position;
				//设置position为Tag，通过Tag尝试找到ImageView
				ImageView imageview=(ImageView) lv.findViewWithTag(position);
				//如果可以找到ImageView控件
				if(imageview!=null){
					//如果图片对象bitmap不是null
					if(task.bitmap!=null){
						//那么这时将图片对象显示出来
						imageview.setImageBitmap(task.bitmap);
					}
					else {
						//否则，显示另一张图片
						imageview.setImageResource(R.drawable.ic_launcher);
					}
				}
				break;
			}
		};
	};
	
	
	public MusicAdapter(Context context, List<Music> musics,ListView lv) {
		super();
		this.context=context;
		this.musics = musics;
		this.lv=lv;
		this.inflater = LayoutInflater.from(context);

		/**
		 * 思考：为什么选择在构造方法中启动工作线程？
		 * ——————因为构造方法只执行一次，那么就只是启动一条工作线程，让这一条工作线程
		 * 不断地往集合添加图片对象的路径
		 */
		//对工作线程进行初始化
		workthread=new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				//不断的轮循任务集合  
				//可以用true循环，不过这里用一个boolean类型的值来代替，当之后不需要循环了，可以直接isloop=false
				while(isloop){
					//如果集合tasks不为空，那么就可以显示图片对象
					if(!tasks.isEmpty()){
						/**
						 * 思考：怎么获得图片？
						 * ——————由于tasks是一个ImageLoaderTask类型的集合，
						 * 集合中有一个方法remove，虽然是移除某一个集合元素，
						 * 但remove方法有返回值！！！并且返回的就是被移除的那个对象！！！
						 * ——————所以每次只需要将第一张图片移除，就能够返回图片了！！！
						 */
						ImageLoaderTask task=tasks.remove(0);
						//获取请求路径  image/xxxx.jpg
						//注意：此时已经保证了集合tasks中不为空的，即是说，已经从网路中下载到了图片
						//task.path的值是在主线程中的item.getAlbumpic方法中获得的
						//所以，这时候通过loadBitmap方法就能够返回图片对象
						Bitmap bitmap=loadBitmap(task.path);
						//将下载得到的图片对象进行赋值
						task.bitmap=bitmap;
						//给handler发送消息 
						//消息中携带ImageLoaderTask
						//把bitmap 显示到 ImageView中
						Message msg=new Message();
						msg.what=HANDLER_LOAD_IMAGE_SUCCESS;
						msg.obj=task;
						handler.sendMessage(msg);
					}
					else {
						//如果集合tasks为空集，则工作线程需要等待，等到有图片对象时让主线程来唤醒执行轮循操作
						try {
							//记得需要在同步代码块中执行！！！
							synchronized (workthread) {
								workthread.wait();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		//启动工作线程
		workthread.start();
	}

	/**
	 * 通过图片的path  发请求，获取（下载）图片对象
	 * @param path
	 * @return
	 */
	protected Bitmap loadBitmap(String path) {
		// TODO Auto-generated method stub
		String uri=GlobalConsts.BASEURL+path;
		try {
			//Get请求中不传参数，故集合是null
			HttpEntity entity=HttpUtils.send(HttpUtils.REQTYPE_GET, uri, null);
			//把HttpEntity转成Bitmap
		      byte[] bytes=EntityUtils.toByteArray(entity);
		      //从0到bytes.length全部解析，并返回
		     return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return musics.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return musics.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	//每显示一个Item，就执行getView方法一次
	//并且，这个方法是在主线程中执行的！！！
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		if(convertView==null){
			convertView=inflater.inflate(R.layout.item_lv_music, null);
			holder=new ViewHolder();
			//让ViewHolder的对象引用每一个控件
			holder.ivAlbum=(ImageView)convertView.findViewById(R.id.imageView);
			holder.tvName=(TextView)convertView.findViewById(R.id.tvName);
			holder.tvAuthor=(TextView)convertView.findViewById(R.id.tvAuthor);
			holder.tvSinger=(TextView)convertView.findViewById(R.id.tvSinger);
			holder.tvDuration=(TextView)convertView.findViewById(R.id.tvDuration);
			//把holder绑定在convertView中
			convertView.setTag(holder);
		}
		   
			holder=(ViewHolder)convertView.getTag();
			//找到每一个Item
			Music item=musics.get(position);
			//为每个控件赋值
			holder.tvName.setText("歌曲："+item.getName());
			holder.tvAuthor.setText("专辑："+item.getAuthor());
			holder.tvSinger.setText("歌手："+item.getSinger());
			holder.tvDuration.setText("时长："+item.getDurationtime());
			
			//给imageView设置不同的tag————由于Tag(标签)是唯一的，可以选用position来代替
			//当图片在工作线程下载完毕后，
			//handler中使用该tag值可以找到相应ImageView
			holder.ivAlbum.setTag(position);
			
			//给holder的imageView设置图片
			//图片需要从服务端下载
			//不可以在此启动工作线程
			//向任务集合中添加一个图片下载任务
			ImageLoaderTask task=new ImageLoaderTask();
			//Music类中有一个方法getAlbumpic方法，可以获得图片路径
			//通过Music对象item调用，返回的String值赋给task对象中的成员变量path
			//albumpic:  "albumpic":"images/xiaozheku.jpg"
			task.path=item.getAlbumpic();
			//图片的位置
			task.position=position;
			//把task 添加到tasks集合中
			tasks.add(task);
			synchronized (workthread) {
				//联想上面的代码，这时集合中有了元素，需要唤醒处于等待状态的工作线程来加载图片
				//继续轮循集合
				workthread.notify();
			}
			return convertView;
	}
	
	/**
	 * 停止工作线程
	 */
	public void stopThread() {
		imageLoader.stopThread();
	}	
	

}
	/**
	 * 保存相应的控件
	 */
	class ViewHolder{
		ImageView ivAlbum;
		TextView tvName;
		TextView tvAuthor;
		TextView tvSinger;
		TextView tvDuration;
	}
	
	/**
	 * 封装一个图片下载任务对象
	 */
	class ImageLoaderTask{
		int position;
		String path;         //图片路径
		Bitmap bitmap;   //通过路径下载后的图片对象
	}
