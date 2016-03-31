package com.android.musicclient.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.musicclient.entity.Music;

public class JSONparse {

	/**
	 * 解析JSONArray  返回List<Music>
	 * @param array
	 * [{"album":"君生今世","albumpic":"images/junshengjinshi.jpg","author":"小虫",
	 * "composer":"小虫","downcount":"1896","durationtime":"4:32","favcount":"658",
	 * "id":1,"musicpath":"musics/yelaixiang.mp3","name":"夜来香","singer":"邓丽君"}]
	 * @return
	 * @throws JSONException
	 */
	public static List<Music> parseJSON(JSONArray array) throws JSONException {
		// TODO Auto-generated method stub
		List<Music> musics=new ArrayList<Music>();
		//遍历JSONArray
		for(int i=0;i<array.length();i++){
			//数组的每一个元素都是一个JSONObject
			JSONObject object=array.getJSONObject(i);
			//将JSONObject的每个子内容在new Music这个类时存储到成员变量中
			Music music=new Music(
					object.getString("album"), 
					object.getString("albumpic"), 
					object.getString("album"), 
					object.getString("composer"), 
					object.getString("downcount"), 
					object.getString("durationtime"), 
					object.getString("favcount"), 
					object.getInt("id"), 
					object.getString("musicpath"), 
					object.getString("name"), 
					object.getString("singer"));
			
			musics.add(music);
		}
		return musics;
	}
	
}
