package com.android.musicclient.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.musicclient.R;
import com.android.musicclient.entity.Music;
import com.android.musicclient.service.DownloadService;
import com.android.musicclient.service.PlayMusicService;
import com.android.musicclient.util.BitmapUtils;
import com.android.musicclient.util.GlobalConsts;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class PlayMusicActivity extends Activity {
	
	private SeekBar seekBar;
	private TextView tvCurrent;
	private TextView tvTotal;
	private TextView tvName;
	private ImageView ivAlbum;
	private UpdateMusicReceiver receiver;
	private ImageView ivDownload;
	private ImageView ivShare;
	private int position;
	private ArrayList<Music> musics;
	private ImageView ivGood;
	private ImageView ivPlay;
	private ImageView ivPause;
	private int count = 0;
	private static int state = 0;
	private static int seek = 0;
	private IWXAPI iwxapi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_music);
		setViews();
		setListener();
		isLike();
		//不用new,直接get一个
		Intent intent = getIntent();
		musics = (ArrayList<Music>) intent.getSerializableExtra("musics");
		position = intent.getIntExtra("position", 0);
		// 立即启动PlayMusicService 播放音乐
		intent.setClass(this, PlayMusicService.class);		
		intent.putExtra("musics", musics);
		intent.putExtra("position", position);
		startService(intent);
		seek = 1;
	}
	
	/**
	 * 判断是否把歌曲标记为喜欢
	 */
	private void isLike(){
		if (state == 0) {
			ivGood.setImageResource(R.drawable.xihuan);
		} else if (state == 1) {
			ivGood.setImageResource(R.drawable.like);
		}
	}
	
	private void isPause() {
		if (seek == 0) {
			
		}
	}

	/**
	 * 添加监听
	 */
	private void setListener() {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){ //如果该操作是用户触发的
					//给Service发送广播
					Intent i=new Intent(GlobalConsts.ACTION_MUSIC_SEEKTO);
					i.putExtra("progress", progress);
					sendBroadcast(i);
				}
			}
		});
		
		//分享歌曲到微信
		ivShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//初始化一个WXMusicObject对象
				WXMusicObject music = new WXMusicObject();
				Music musicitem = musics.get(position);
				
				music.musicUrl = GlobalConsts.BASEURL+musicitem.getMusicpath();
				System.out.println("微信分享之music path"+music.musicUrl);
				WXMediaMessage msg = new WXMediaMessage();
				msg.mediaObject = music;
				msg.title = musicitem.getName();
				System.out.println("微信分享之music name"+musicitem.getName());
/*				Bitmap thumb = null;
				if (msg.title == "LOSER") {
					thumb = BitmapFactory.decodeResource(getResources(), R.drawable.bigbang);
					if (thumb == null) {
					System.out.println("thumb为空");
				}
					msg.thumbData = BitmapUtils.Bitmap2Bytes(thumb);
				}
				if (msg.title == "Lost Stars") {
					 thumb = BitmapFactory.decodeResource(getResources(), R.drawable.beginagain);
						if (thumb == null) {
						System.out.println("thumb为空");
					}
					 msg.thumbData = BitmapUtils.Bitmap2Bytes(thumb);
				}
				if (msg.title == "时光") {
					 thumb = BitmapFactory.decodeResource(getResources(), R.drawable.shiguang);
						if (thumb == null) {
						System.out.println("thumb为空");
					}
					 msg.thumbData = BitmapUtils.Bitmap2Bytes(thumb);
				}*/
//				msg.setThumbImage(BitmapUtils.loadBitmp(new File(PlayMusicActivity.this.getCacheDir(), musicitem.getAlbumpic()).getAbsolutePath()));
				
//				Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.album_default);
//				Bitmap thumb =BitmapUtils.loadBitmp(new File(PlayMusicActivity.this.getCacheDir(), musicitem.getAlbumpic()).getAbsolutePath());
//				Bitmap thumb = BitmapFactory.decodeFile(new File(PlayMusicActivity.this.getFilesDir(), musicitem.getAlbumpic()).getAbsolutePath());
//				if (thumb == null) {
//					System.out.println("thumb为空");
//				}
//				msg.thumbData = BitmapUtils.Bitmap2Bytes(thumb);
				
				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = String.valueOf(System.currentTimeMillis());
				
				req.message = msg;
				req.scene = SendMessageToWX.Req.WXSceneTimeline;
				
				iwxapi.sendReq(req);
				
			}
		});
		
		ivDownload.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(PlayMusicActivity.this, DownloadService.class);
				Music music=musics.get(position);
				String path=music.getMusicpath();
				System.out.println("下载音乐的路径path:"+path);
				intent.putExtra("path", path);
				startService(intent);				
			}
		});
		
		ivGood.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (state ==0) {
				ivGood.setImageResource(R.drawable.like);
				count++;
				state = count;
				Toast.makeText(PlayMusicActivity.this, "歌曲标记为喜欢", Toast.LENGTH_SHORT).show();
				} else if (state == 1) {
					ivGood.setImageResource(R.drawable.xihuan);
					count = 0;
					state = count;
					Toast.makeText(PlayMusicActivity.this, "取消", Toast.LENGTH_SHORT).show();
				} 
			}
		});
		
	}

	/**
	 * 控件初始化
	 */
	private void setViews() {
		seekBar=(SeekBar)findViewById(R.id.seekBar1);
		tvCurrent=(TextView)findViewById(R.id.textView3);
		tvTotal=(TextView)findViewById(R.id.textView4);
		tvName=(TextView)findViewById(R.id.tvName);
		ivShare = (ImageView) findViewById(R.id.imageView1);
		ivAlbum=(ImageView)findViewById(R.id.imageView3);
		ivDownload = (ImageView) findViewById(R.id.imageView5);
		ivGood = (ImageView) findViewById(R.id.imageView4);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPause  = (ImageView) findViewById(R.id.ivPause);
		//微信分享
        iwxapi= WXAPIFactory.createWXAPI( this,"wx6b64dee338a0412b");
        iwxapi.registerApp("wx6b64dee338a0412b");
	}

	public void doClick(View view) {
		Intent intent=new Intent();
		switch (view.getId()) {
		case R.id.ivPlay:
			ivPlay.setVisibility(View.VISIBLE);
			intent.setAction(GlobalConsts.ACTION_MUSIC_PLAY);
			ivPause.setVisibility(View.INVISIBLE);
			break;
		case R.id.ivPause:
			ivPlay.setVisibility(View.INVISIBLE);
			intent.setAction(GlobalConsts.ACTION_MUSIC_PAUSE);
			ivPause.setVisibility(View.VISIBLE);
			break;
		case R.id.ivPre:
			intent.setAction(GlobalConsts.ACTION_MUSIC_PRE);
			break;
		case R.id.ivNext:
			intent.setAction(GlobalConsts.ACTION_MUSIC_NEXT);
			break;
		}
		//发送普通广播
		sendBroadcast(intent);
	}

	/**
	 * 在onStart中注册广播接收器
	 */
	protected void onStart() {
		super.onStart();
		receiver=new UpdateMusicReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS);
		filter.addAction(GlobalConsts.ACTION_UPDATE_MUSIC_INFO);
		this.registerReceiver(receiver, filter);
	}
	
	/**
	 * 取消注册广播接收
	 */
	protected void onStop() {
		super.onStop();
		this.unregisterReceiver(receiver);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
   
	class UpdateMusicReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS)){
				int current=intent.getIntExtra("current", 0);
				int total=intent.getIntExtra("total", 0);
				//更新UI界面
				seekBar.setMax(total);
				seekBar.setProgress(current);
				//更新TextView
				SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
				tvCurrent.setText(sdf.format(new Date(current)));
				tvTotal.setText(sdf.format(new Date(total)));
			}else if(action.equals(GlobalConsts.ACTION_UPDATE_MUSIC_INFO)){
				//更新音乐的基本信息
				Music music=(Music)intent.getSerializableExtra("music");
				String name=music.getName();
				String path=music.getAlbumpic();
				String filepath=new File(PlayMusicActivity.this.getCacheDir(), path).getAbsolutePath();
				System.out.println("filepath="+filepath);
				System.out.println("music.getAlbumpic()="+music.getAlbumpic());
				//给控件进行赋值
				tvName.setText(name);
				if(name.equals("时光")) {
					ivAlbum.setImageResource(R.drawable.shiguang);
				}
				if(name.equals("LOSER")) {
					ivAlbum.setImageResource(R.drawable.bigbang);
				}
				if(name.equals("Lost Stars")) {
					ivAlbum.setImageResource(R.drawable.beginagain);
				}
				if(name.equals("我在人民广场吃炸鸡")) {
					ivAlbum.setImageResource(R.drawable.zhaji);
				}				
				if(name.equals("南山南")) {
					ivAlbum.setImageResource(R.drawable.nanshannan);
				}
//				ivAlbum.setImageBitmap(BitmapUtils.loadBitmp(filepath));
			}
		}
	}
}


