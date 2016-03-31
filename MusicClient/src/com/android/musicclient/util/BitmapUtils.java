package com.android.musicclient.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/**
 * 图片相关操作的工具类
 */
public class BitmapUtils {
	
	/**
	 * 通过path路径  返回响应的Bitmap对象
	 * @param path
	 * @return
	 */
	public static Bitmap loadBitmp(String path){
		File file=new File(path);
		//如果路径不存在 则直接返回null
		if(!file.exists()){
			return null;
		}
		//如果路径存在 则解析出Bitmap return
		return BitmapFactory.decodeFile(path);
	}
	
	
	/**
	 * 保存一张图片 
	 * @param bitmap 待保存的图片
	 * @param targetFile  目标文件
	 * @throws FileNotFoundException 
	 */
	public static void save(Bitmap bitmap, File targetFile) throws FileNotFoundException{
		//判断父目录是否存在
		if(!targetFile.getParentFile().exists()){
			//创建父目录
			targetFile.getParentFile().mkdirs();
		}
		//把bitmap输出到targetFile中
		//format  图片压缩格式
		//quality   压缩比率
		//stream  输出流
		FileOutputStream fos=
				new FileOutputStream(targetFile);
		bitmap.compress(CompressFormat.JPEG, 
				100, fos);
	}
	
	/**
	 * 从字节数组中按照用户的参数要求
	 * 压缩图片。
	 * @param bytes   源字节数组
	 * @param width   压缩后的图片宽度
	 * @param height  压缩后的图片高度
	 * @return
	 */
	public static Bitmap loadBitmap(byte[] bytes, int width, int height){
		Options opts=new Options();
		//仅仅加载图片的边界属性
		opts.inJustDecodeBounds=true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		//获取原始的宽度与高度
		int w=opts.outWidth/width;
		int h=opts.outHeight/height;
		int scale=w>h ? w : h;
		opts.inSampleSize=scale;
		//不仅仅加载边界属性
		opts.inJustDecodeBounds=false;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
	}
	
	/**
	 * Bitmap ——> byte[]
	 * @param bitmap
	 * @return
	 */
	public static byte[] Bitmap2Bytes(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
}
