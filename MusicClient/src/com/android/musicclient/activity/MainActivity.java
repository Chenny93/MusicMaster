package com.android.musicclient.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.android.musicclient.R;
import com.android.musicclient.adapter.MusicAdapter;
import com.android.musicclient.biz.MusicBiz;
import com.android.musicclient.entity.Music;
import com.android.musicclient.service.DownloadService;

public class MainActivity extends Activity {
	private ListView listView;
	private MusicAdapter adapter;
	private List<Music> musics;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setViews();
		//启动异步任务  发送http请求 更新listView
		MusicBiz biz=new MusicBiz(this);
		biz.execute();		
		setListener();
	}
	
	/**
	 * 添加监听
	 */
	private void setListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				//启动音乐播放界面 
				Intent intent=new Intent(MainActivity.this, PlayMusicActivity.class);
				intent.putExtra("musics", (ArrayList<Music>)musics);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});
		
/*		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				//弹窗  点击下载时执行下载业务
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("操作").setItems(new String[]{"喜欢", "下载"}, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: //喜欢
							break;
						case 1:  //下载
							//启动DownLoadService
							//并且传递过去path 执行下载 
							Intent intent=new Intent(MainActivity.this, DownloadService.class);
							Music music=musics.get(position);
							String path=music.getMusicpath();
							intent.putExtra("path", path);
							startService(intent);
							break;
						}
					}
				});
				builder.create().show();
				return false;
			}
		});*/
	}

	/**
	 * 初始化
	 */
	private void setViews() {
		listView=(ListView)findViewById(R.id.lv1);
		musics = new ArrayList<Music>();
	}

	/**
	 * //创建Adapter
		//给listView设置Adapter
	 */
	public void updateListView(List<Music> musics){
		//切记将解析的集合对象存储起来，否则不能跳转到播放界面
		this.musics = musics;
		//创建Adapter
		//给listView设置Adapter
		adapter=new MusicAdapter(this, musics, listView);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//停止Adapter中的线程
		//防止出现内存泄漏
		adapter.stopThread();
	}
}
