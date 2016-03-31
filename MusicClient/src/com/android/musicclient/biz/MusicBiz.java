package com.android.musicclient.biz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.android.musicclient.activity.MainActivity;
import com.android.musicclient.entity.Music;
import com.android.musicclient.util.GlobalConsts;
import com.android.musicclient.util.HttpUtils;
import com.android.musicclient.util.JSONparse;

public class MusicBiz extends AsyncTask<String, String, List<Music>> {
	
	private MainActivity context;
	
	private List<Music> musics;

	public MusicBiz(MainActivity context){
		this.context=context;
		musics = new ArrayList<Music>();
	}

	/**
	 * MusicBiz.execute() 
	 * 将会在工作线程中执行
	 */
	@Override
	protected List<Music> doInBackground(String... params) {
		// TODO Auto-generated method stub
		String uri=GlobalConsts.BASEURL+"loadMusics.jsp";
		try {
			System.out.println("0");
			HttpEntity entity=HttpUtils.send(HttpUtils.REQTYPE_GET, uri, null);
			System.out.println("1");
			//把entity转成json
			String json=EntityUtils.toString(entity);
			System.out.println("2");
			//{result:ok, data:[{},{},{},{}]}
			JSONObject object=new JSONObject(json);
			System.out.println("3");
			//验证性判断
			if(object.getString("result").equals("ok")){
			JSONArray array=object.getJSONArray("data");
			System.out.println("4");
			//把JSONArray解析成集合
			musics =JSONparse.parseJSON(array);
			System.out.println("5");
			//在LogCat中测试一下
			Log.i("info","musics:"+musics);
			return musics;
			}
			else{
				Log.i("error","请求出现了异常");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (JSONException e) {
				// TODO Auto-generated catch block
			 Log.e("error", "json解析失败了， 请查看请求....");
			}
		return null;
	}
	
	
	
	/**
	 * 当执行完doInBackground方法完毕时，执行onPostExecute方法
	 * 此方法是在主线程中执行的，所以可以在这里处理更新UI的操作
	 */
	@Override
	protected void onPostExecute(List<Music> result) {
		// TODO Auto-generated method stub
//		super.onPostExecute(result);
		//当new MusicBiz这个类时，顺便将MainActivity的一个对象传递过来，保存到成员变量中
		//然后这里就可以用这个成员变量来调用MainActivity的方法了
		context.updateListView(result);
	}

}
