package cn.ucai.superwechat.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileUtils {

	/**
	 * 获取sd卡的保存位置
	 * @param path:http:/10.0.2.2/images/aa.jpg
	 */
	public static String getDir(Context context,String path) {
//		File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File dir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		path=dir.getAbsolutePath()+"/"+path;
		return path;
	}
	
	/**
	 * 修改本地缓存的图片名称
	 * @param context
	 * @param oldImgName
	 * @param newImgName
	 */
	public static void renameImageFileName(Context context,String oldImgName,String newImgName){
		String dir = getDir(context, oldImgName);
		File oldFile=new File(dir);
		dir=getDir(context, newImgName);
		File newFile=new File(dir);
		oldFile.renameTo(newFile);
	}
}
