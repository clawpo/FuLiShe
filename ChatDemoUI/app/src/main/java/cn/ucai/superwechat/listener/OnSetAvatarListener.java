package cn.ucai.superwechat.listener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.RegisterActivity;
import cn.ucai.superwechat.utils.ImageUtils;

/**
 * 拍照、从相册选取照片、保存照片框架
 * @author chen
 *
 */
public class OnSetAvatarListener implements OnClickListener {
    /** 启动拍照Activity的请求码*/
    public static final int REQUEST_TAKE_PHOTO=1;
    /** 启动从相册选取Activity的请求码*/
    public static final int REQUEST_CHOOSE_PHOTO=2;
    /** 启动裁剪Activity的请求码*/
    public static final int REQUEST_PHOTO_CROP=3;
    private Activity mActivity;
    
    /** 所在的布局id  */
    int mActivitylayoutId;
    
    private PopupWindow mPopuWindow;
    
    /** 拍照文件保存的路径  */
    private File mAvatarFile;
    
    private View mLayout;
	/**
	 * 拍照、从相册选取照片、保存照片框架
	 * @author yao
	 */
    public OnSetAvatarListener(Activity mActivity, int layoutId){
        super();
        this.mActivity = mActivity;
        mActivitylayoutId = layoutId;
        mLayout = View.inflate(mActivity, R.layout.popu_show_avatar, null);
        mLayout.findViewById(R.id.btn_take_picture).setOnClickListener(this);
        mLayout.findViewById(R.id.btn_choose_photo).setOnClickListener(this);
        showAvatarWindow();
    }
    

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_take_picture:
            takePicture();
            break;
        case R.id.btn_choose_photo:
            choosePicture();
            break;
        }
    }

	/**
	 * 启动从相册选取照片的Activity
	 */
    private void choosePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		mActivity.startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }

	/**
	 * 启动系统拍照Activity
	 * @throws Exception 
	 */
    private void takePicture() {
        String userName = null;
        if(mActivity instanceof RegisterActivity){
            try {
                userName = ((RegisterActivity)mActivity).getUserName();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            userName = SuperWeChatApplication.getInstance().getUserName();
        }
        mAvatarFile = new File(ImageUtils.getAvatarPath(mActivity, "user_avatar"),userName);
        Uri uri = Uri.fromFile(mAvatarFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mActivity.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }
    
	/**
	 * 创建并显示选择头像的窗口
	 */
    private void showAvatarWindow(){
        mPopuWindow = new PopupWindow(mLayout, getScreenDisplay().widthPixels, (int)(90*getScreenDisplay().density));
        mPopuWindow.setOutsideTouchable(true);
        mPopuWindow.setTouchable(true);
        mPopuWindow.setFocusable(true);
		//设置popuWindow的背景,必须设置，否则PopupWindow不能隐藏
        mPopuWindow.setBackgroundDrawable(new BitmapDrawable());
		//设置从底部弹出的动画
        mPopuWindow.setAnimationStyle(R.style.styles_pop_window);
		//获取PopupWindow所在的Activity的布局
        View mLayoutParent = mActivity.findViewById(mActivitylayoutId);
		//显示PopupWindow
        mPopuWindow.showAtLocation(mLayoutParent, Gravity.BOTTOM, 0, 0);
    }
    
	/**
	 * 获取表示屏幕尺寸、密度等信息的对象
	 * @return
	 */
    private DisplayMetrics getScreenDisplay(){
		//创建用于获取屏幕尺寸、像素密度的对象
        Display defaultDisplay  = mActivity.getWindowManager().getDefaultDisplay();
		//创建用于获取屏幕尺寸、像素密度等信息的对象
        DisplayMetrics outMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(outMetrics);
        return outMetrics;
    }
    
	/**
	 * 返回拍照文件保存的位置
	 * @return
	 */
    public static File getAvatarFile(Activity activity,String avatar){
        File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file;
        try {
            file = new File(dir,avatar);
            boolean isExists = file.getParentFile().exists();
            if(!isExists){
                isExists = file.getParentFile().mkdirs();
            }
            if(isExists){
                return file;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    
	/**
	 * 启动带裁剪的Activity
	 * @param uri：保存照片的路径
	 * @param outputX：输出图片的宽度
	 * @param outputY：输出图片的宽度
	 * @param requestCode：请求码
	 * @param isCrop：是否裁剪
	 */
    private void startCropPhoto(Uri uri,int outputX,int outputY,int requestCode,boolean isCrop){
        Intent intent = null;
        if(isCrop){
            intent = new Intent("com.android.camera.action.CROP");
        }else{
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
        
        mActivity.startActivityForResult(intent, requestCode);
        
    }
    
	/**
	 * 保存裁剪图片
	 * @param ivAvatar
	 * @param data
	 * @param path
	 * @param userName
	 */
    private void saveCropPhoto(ImageView ivAvatar,Intent data,String path,String userName){
        Bundle bundle = data.getExtras();
        Bitmap photo = bundle.getParcelable("data");
        if(photo==null){
            return ;
        }
        ivAvatar.setImageBitmap(photo);
        File file=getAvatarFile(mActivity,path+"/"+userName+".jpg");
        if(!file.getParentFile().exists()){
            Toast.makeText(mActivity, "照片保存失败,保存的路径不存在", Toast.LENGTH_LONG).show();
            return ;
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            photo.compress(CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
	/**
	 * 关闭弹出的悬浮窗口
	 */
    public void closePopAvatar(){
        if(mPopuWindow!=null){
            mPopuWindow.dismiss();
        }
    }

	/**
	 * 设置拍照或从相册获取图片后返回的结果
	 * @param requestCode
	 * @param data:返回的intent
	 * @param ivAvatar：显示头像的ImageView
	 * @param avatarType:头像类型：group_icon:群logo，user_avatar:个人
	 * @param userName：注册窗口输入的账号
	 */
    public void setAvatar(int requestCode,Intent data,ImageView ivAvatar,String avatarType,String userName){
        switch (requestCode) {
        case OnSetAvatarListener.REQUEST_CHOOSE_PHOTO:
            this.startCropPhoto(data.getData(), 200, 200, 
                OnSetAvatarListener.REQUEST_PHOTO_CROP, true);
            break;
        case OnSetAvatarListener.REQUEST_TAKE_PHOTO:
            Uri uri = Uri.fromFile(getAvatarFile(mActivity,avatarType+userName));
            startCropPhoto(uri, 200, 200, OnSetAvatarListener.REQUEST_PHOTO_CROP, true);
            break;
        case OnSetAvatarListener.REQUEST_PHOTO_CROP:
            closePopAvatar();
            if(data==null){
                return ;
            }
            saveCropPhoto(ivAvatar, data,avatarType,userName);
            break;
        }
    }

}
