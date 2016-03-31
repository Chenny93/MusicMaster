package com.android.musicclient.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.widget.Toast;

import com.android.musicclient.entity.Music;
import com.android.musicclient.util.GlobalConsts;

/**
 * 音乐播放服务
 */
public class PlayMusicService extends Service{
	
	private List<Music> musics;
	private int position;
	private MediaPlayer player;
	private MusicControllReceiver receiver;
	private boolean isLoop;
	private Intent intent;
	private IntentFilter filter;
	
	/**
	 * 执行一次  创建service实例时执行
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		init();
		player.setOnPreparedListener(new OnPreparedListener() {
			//在prepare准备完成之后 执行
			@Override
			public void onPrepared(MediaPlayer mp) {
				//player.start();
			}
		});
		
		player.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				next();
			}
		});
		//启动工作线程 每隔1秒中就发一次广播
		new Thread(){
		public void run() {
		while( !isLoop ){
		try {
		        Thread.sleep(1000);
		} catch (InterruptedException e) {
		e.printStackTrace();
			}
		
		//发广播
		if(player.isPlaying()){
		int current=player.getCurrentPosition();
		int total=player.getDuration();
		intent.setAction(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS);
		intent.putExtra("current", current);
		intent.putExtra("total", total);
		sendBroadcast(intent);
			}
		              }
		         }
		}.start();
		
		//注册广播接收器

		filter.addAction(GlobalConsts.ACTION_MUSIC_NEXT);
		filter.addAction(GlobalConsts.ACTION_MUSIC_PLAY);
		filter.addAction(GlobalConsts.ACTION_MUSIC_PAUSE);
		filter.addAction(GlobalConsts.ACTION_MUSIC_PRE);
		filter.addAction(GlobalConsts.ACTION_MUSIC_SEEKTO);
		this.registerReceiver(receiver, filter);
	}
	
	private void init() {
		player=new MediaPlayer();
		musics = new ArrayList<Music>();
		 intent = new Intent();		
		receiver=new MusicControllReceiver();
		 filter=new IntentFilter();		 
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);
	}
	
	/**
	 * 当调用startService时 将会执行该方法。
	 */
	@SuppressWarnings("unchecked")
	public int onStartCommand(Intent intent, int flags, int startId) {
		//获取到List  还有 position
		 musics=(ArrayList<Music>)intent.getSerializableExtra("musics");
		position=intent.getIntExtra("position", 0);
		//执行音乐播放
		playMusic();
		return Service.START_NOT_STICKY;
	}
	
	public void playMusic(){
		Music m=musics.get(position);
		String uri=GlobalConsts.BASEURL+m.getMusicpath();
		System.out.println("playMusic"+uri);
		try {
			player.reset();
			player.setDataSource(uri);
			player.prepare();
			player.start();
			//给Activity发广播 传递当前的音乐信息
			intent.setAction(GlobalConsts.ACTION_UPDATE_MUSIC_INFO);
			//Music 类实现了序列化，不然会报错
			intent.putExtra("music", m);
			this.sendBroadcast(intent);
		} catch (IOException e) {
			e.printStackTrace();
			//音乐加载失败 自动播放下一首
			Toast.makeText( this, "加载失败 next", Toast.LENGTH_SHORT).show();
			next();
		}
	}
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	/**
	 * 上一曲
	 */
	public void pre(){
		position = position==0 ? 0 : position-1;
		//重新播放音乐
		playMusic();
	}
	
	
	 //播放
	private void play(){
		if(!player.isPlaying()){
			player.start();
			}
	}
	
	//暂停
	private void pause() {
		if (player.isPlaying()) {
		player.pause();
		}
	}
		
	
	/**
	 * 下一曲
	 */
	public void  next(){
		position = position==musics.size()-1 ? 0 : position+1;
		//重新播放音乐
		playMusic();
	}
	
	/**
	 * 定位到某一个毫秒数继续播放
	 * @param progress
	 */
	public void seekTo( int progress){
		player.seekTo(progress);
	}
	
	/**
	 * 接收控制音乐播放的广播
	 * 上一曲 、 下一曲 、 播放、暂停
	 */
	class MusicControllReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(GlobalConsts.ACTION_MUSIC_NEXT)){
				next();
			}else if(action.equals(GlobalConsts.ACTION_MUSIC_PLAY)){
				play();
			}else if(action.equals(GlobalConsts.ACTION_MUSIC_PAUSE)){
					pause();
			}else if(action.equals(GlobalConsts.ACTION_MUSIC_PRE)){
				pre();
			}else if(action.equals(GlobalConsts.ACTION_MUSIC_SEEKTO)){
				seekTo(intent.getIntExtra("progress", 0));
			}
		}
	}
	
}
