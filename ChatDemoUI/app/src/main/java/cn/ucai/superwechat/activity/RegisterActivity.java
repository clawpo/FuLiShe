/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.NetUtil;
import cn.ucai.superwechat.utils.Utils;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
     static final String TAG = "RegisterActivity";
	private EditText metUserName;
	private EditText metPassword;
	private EditText metConfirmPassword;
	private EditText metNick;
	
	private ImageView mivAvatar;

	RegisterActivity mContext;
	
	OnSetAvatarListener mOnSetAvatarListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mContext=this;
		initView();
		setListener();
	}


	private void setListener() {
		setRegisterClickListener();
		setLoginClickListener();
		setUserAvatarClickListener();
	}

    /**
     * 设置头像的view单击事件监听
     */
    private void setUserAvatarClickListener() {
        findViewById(R.id.layout_user_avatar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSetAvatarListener = new OnSetAvatarListener(mContext, R.id.layout_register);
            }
        });
    }

	private void setLoginClickListener() {
		findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext,LoginActivity.class));
			}
		});
	}

	private void setRegisterClickListener() {
        findViewById(R.id.btnRegister).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = metUserName.getText().toString().trim();
                final String nick = metNick.getText().toString().trim();
                final String pwd = metPassword.getText().toString().trim();
                String confirm_pwd = metConfirmPassword.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    metUserName.requestFocus();
                    metUserName.setError(mContext.getResources().getString(R.string.User_name_cannot_be_empty));
                    return;
                } 
                if(TextUtils.isEmpty(nick)){
                    metNick.requestFocus();
                    metNick.setError(mContext.getResources().getString(R.string.Nick_name_cannot_be_empty));
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    metPassword.requestFocus();
                    metPassword.setError(mContext.getResources().getString(R.string.Password_cannot_be_empty));
                    return;
                } 
                if (TextUtils.isEmpty(confirm_pwd)) {
                    metConfirmPassword.requestFocus();
                    metConfirmPassword.setError(mContext.getResources().getString(R.string.Confirm_password_cannot_be_empty));
                    return;
                } 
                if (!pwd.equals(confirm_pwd)) {
                    Toast.makeText(mContext, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
                    return;
                }
                new RegisterTask(username,nick,pwd).execute();
            }
        });
	}


	private void initView() {
        metUserName = getViewById(R.id.etUserName);
        metPassword = getViewById(R.id.etPassword);
        metConfirmPassword = getViewById(R.id.etConfirmPassword);
        metNick=getViewById(R.id.etNick);
        mivAvatar=getViewById(R.id.iv_avatar);
	}

	public void back(View view) {
		finish();
	}

	public String getUserName() throws Exception{
		String userName = metUserName.getText().toString();
		if(TextUtils.isEmpty(userName)){
			metUserName.setError(getResources().getString(R.string.User_name_cannot_be_empty));
			metUserName.requestFocus();
			throw new Exception(getResources().getString(R.string.User_name_cannot_be_empty));
		}
		return userName;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult-resultCode="+resultCode);
		if(resultCode != RESULT_OK){
			return;
		}
		try {
			String userName = getUserName();
            /*
             * 设置拍照或从相册获取图片后返回的结果
             * @param requestCode:请求码
             * @param data:返回的intent
             * @param ivAvatar：显示头像的ImageView
             * @param userName：注册窗口输入的账号
             */
			mOnSetAvatarListener.setAvatar(requestCode, data, mivAvatar, "user_avatar", userName);
		} catch (Exception e) {
			Log.i(TAG, "onActivityResult-e="+e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * 注册的线程类
	 * @author yao
	 *
	 */
	class RegisterTask extends AsyncTask<Void, Void, EaseMobException> {

		String userName,nick,password;
		ProgressDialog dialog;

		/**
		 * 向应用服务器注册，注册成功后，再向环信服务器注册
		 * @param userName
		 * @param nick
		 * @param password
		 */
		public RegisterTask(String userName,String nick,String password){
			super();
			this.userName = userName;
			this.nick = nick;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(mContext);
			dialog.setMessage(getResources().getString(R.string.Is_the_registered));
			dialog.show();
		}

		@Override
		protected EaseMobException doInBackground(Void... params) {
			EaseMobException errorEaseMobException = null;
			UserBean userBean = new UserBean(userName, nick, password);
			Log.i(TAG, "do...name="+userName+",nick="+nick+",paw="+password);
			try {
				boolean isSuccess = NetUtil.register(userBean);
				Log.i(TAG, "1-isSuccess="+isSuccess);
				if(isSuccess){
					isSuccess = NetUtil.uploadAvatar(mContext, "user_avatar", userBean.getUserName());
					Log.i(TAG, "2-isSuccess="+isSuccess);
					if(isSuccess){
						// 调用sdk注册方法
//                            EMChatManager.getInstance().createAccountOnServer(userName, password);

						try {
							// 调用sdk注册方法
							EMChatManager.getInstance().createAccountOnServer(userName, password);
						} catch (final EaseMobException e) {
							//注册失败
							int errorCode=e.getErrorCode();
							if(errorCode==EMError.NONETWORK_ERROR){
								Toast.makeText(getApplicationContext(), "网络异常，请检查网络！", Toast.LENGTH_SHORT).show();
							}else if(errorCode==EMError.USER_ALREADY_EXISTS){
								Toast.makeText(getApplicationContext(), "用户已存在！", Toast.LENGTH_SHORT).show();
							}else if(errorCode==EMError.UNAUTHORIZED){
								Toast.makeText(getApplicationContext(), "注册失败，无权限！", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getApplicationContext(), "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
						Log.i(TAG, "EMChatManager.getInstance().createAccountOnServer=？？？？？成功？？？？？");



						// 保存用户名
						SuperWeChatApplication.getInstance().setUserName(userBean.getUserName());
						SuperWeChatApplication.getInstance().setUserBean(userBean);
					}
					errorEaseMobException = new EaseMobException(getResources().getString(R.string.Registered_successfully));
					Log.i(TAG, "2-errorEaseMobException="+errorEaseMobException);
				}else{
					errorEaseMobException = new EaseMobException(getResources().getString(R.string.Registration_failed));
					Log.i(TAG, "1-errorEaseMobException="+errorEaseMobException);
				}
			} catch (final EaseMobException e) {
				errorEaseMobException = e;
				Log.i(TAG, "0-errorEaseMobException="+errorEaseMobException);
				NetUtil.unRegister(userName);
			}catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "doInBackground-e="+e.getMessage());
			}
			Log.i(TAG, "return="+errorEaseMobException);
			return errorEaseMobException;
		}

		@Override
		protected void onPostExecute(EaseMobException result) {
			if(!RegisterActivity.this.isFinishing()){
				dialog.dismiss();
			}
			Log.i(TAG, "onPostExecute-return="+result);
			if(result == null){
				Utils.showToast(mContext, getResources().getString(R.string.Registration_failed), 0);
			}else{
				Log.i(TAG, "2222222onPostExecute-return="+result.getMessage()+",???"+getResources().getString(R.string.Registered_successfully));
				if(result.getMessage().equals(getResources().getString(R.string.Registered_successfully))){
					//保存用户
					SuperWeChatApplication.getInstance().setUserName(userName);
					Utils.showToast(mContext, getResources().getString(R.string.Registered_successfully), 0);
					finish();
				}else{
					int errorCode=result.getErrorCode();
					Log.i(TAG, "onPostExecute-errorCode="+errorCode);
					if(errorCode==EMError.NONETWORK_ERROR){
						Utils.showToast(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT);
					}else if(errorCode == EMError.USER_ALREADY_EXISTS){
						Utils.showToast(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT);
					}else if(errorCode == EMError.UNAUTHORIZED){
						Utils.showToast(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT);
					}else if(errorCode == EMError.ILLEGAL_USER_NAME){
						Utils.showToast(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT);
					}else{
						Utils.showToast(getApplicationContext(), getResources().getString(R.string.Registration_failed) + result.getMessage(), Toast.LENGTH_SHORT);
					}
				}
			}
		}

	}

}
