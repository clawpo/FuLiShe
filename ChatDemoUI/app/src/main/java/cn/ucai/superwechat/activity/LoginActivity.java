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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.db.EMUserDao;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.domain.User;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.task.DownloadContactsTask;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.NetUtil;
import cn.ucai.superwechat.utils.Utils;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
    LoginActivity mContext;
	private static final String TAG = "LoginActivity";
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText metUserName;
	private EditText metPassword;

	private boolean progressShow;
	private boolean autoLogin = false;

	private String mUserName;
	private String mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		// 如果用户名密码都有，直接进入主页面
		if (DemoHXSDKHelper.getInstance().isLogined()) {
//			Log.e(TAG,"huanxin server checked ok,autoLogin!!!");
//			getUser();
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			return;
		}
		setContentView(R.layout.activity_login);

		metUserName = (EditText) findViewById(R.id.etUserName);
		metPassword = (EditText) findViewById(R.id.etPassword);

		setListener();


		if (SuperWeChatApplication.getInstance().getUserName() != null) {
			metUserName.setText(SuperWeChatApplication.getInstance().getUserName());
		}
	}


	private void setListener() {
        // TODO Auto-generated method stub
	    setUserNameTextChangeListener();
        setLoginClickListener();
        setRegisterClickListener();
		setServerUrlClickListener();
    }

	/**
	 * 设置服务端地址的按钮事件监听
	 */
	private void setServerUrlClickListener() {
		findViewById(R.id.btnUrl).setOnClickListener(new OnClickListener() {
			String serverUrl;
			@Override
			public void onClick(View v) {
				final SharedPreferences sp=getSharedPreferences("server_url", MODE_PRIVATE);
				serverUrl=sp.getString("url", "");
				View layout=View.inflate(LoginActivity.this, R.layout.diaolog_serverurl,null);
				final EditText etServerUrl=(EditText) layout.findViewById(R.id.etServerUrl);
				if(!serverUrl.isEmpty()){
					etServerUrl.setText(serverUrl);
				}
				AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
				builder.setTitle("设置服务端的ip地址");
				builder.setView(layout);
				builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						serverUrl=etServerUrl.getText().toString();
						if(serverUrl.isEmpty()){
							return ;
						}
						sp.edit().putString("url", serverUrl).commit();
						SuperWeChatApplication.SERVER_ROOT=serverUrl+":8080/SuperQQ3Server/Server";
						Toast.makeText(LoginActivity.this, "服务端ip地址设置完成", Toast.LENGTH_LONG).show();
					}
				});
				builder.setNegativeButton("取消", null);
				builder.create().show();
			}
		});
	}


    private void setRegisterClickListener() {
        findViewById(R.id.btnRegister).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivityForResult(new Intent(mContext,RegisterActivity.class), 0);
            }
        });
    }

    /**
	 * 登录
	 *
	 */
	private void setLoginClickListener() {
	    findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!CommonUtils.isNetWorkConnected(mContext)) {
                    Toast.makeText(mContext, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                mUserName = metUserName.getText().toString().trim();
                mPassword = metPassword.getText().toString().trim();

                if (TextUtils.isEmpty(mUserName)) {
                    Toast.makeText(mContext, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(mPassword)) {
                    Toast.makeText(mContext, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                progressShow = true;
                final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                pd.setCanceledOnTouchOutside(false);
                pd.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressShow = false;
                    }
                });
                pd.setMessage(getString(R.string.Is_landing));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.show();
                    }
                });

                final long start = System.currentTimeMillis();
                // 调用sdk登陆方法登陆聊天服务器
                EMChatManager.getInstance().login(mUserName, mPassword, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        if (!progressShow) {
                            return;
                        }
                        // 登陆成功，保存用户名密码
                        SuperWeChatApplication.getInstance().setUserName(mUserName);
                        SuperWeChatApplication.getInstance().setPassword(mPassword);
                        
                        //添加到本地的数据库中
                        boolean isSuccess = loginAppServer(mUserName, mPassword);
                        Log.i(TAG,"login...name="+mUserName+",pas="+mPassword+",isSuccess="+isSuccess);
                        if(!isSuccess){
                            runOnUiThread(new  Runnable() {
                                public void run() {
                                    Utils.showToast(mContext, "登陆失败", Toast.LENGTH_LONG);
                                }
                            });
                            return;
                        }
                        String avatar = SuperWeChatApplication.getInstance().getUserBean().getAvatar();
                        File file = OnSetAvatarListener.getAvatarFile(mContext, avatar);
                        NetUtil.downloadAvatar(file, "user_avatar", avatar);
                        //下载联系人
                        ArrayList<UserBean> contactList = SuperWeChatApplication.getInstance().getContactList();
						Log.e("main","LoginActivity.contactList.size="+contactList.size());
                        if(contactList.size()==0){
                            new DownloadContactsTask(mContext, mUserName, 0, 20).execute();
                        }

						try {
							// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
							// ** manually load all local groups and
							EMGroupManager.getInstance().loadAllGroups();
							EMChatManager.getInstance().loadAllConversations();
							// 处理好友和群组
                            initializeContacts();
						} catch (Exception e) {
							e.printStackTrace();
							// 取好友或者群聊失败，不让进入主页面
							runOnUiThread(new Runnable() {
								public void run() {
									pd.dismiss();
									DemoHXSDKHelper.getInstance().logout(true,null);
									Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_SHORT).show();
								}
							});
							return;
						}
						// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
								SuperWeChatApplication.currentUserNick.trim());
						if (!updatenick) {
							Log.e("LoginActivity", "update current user nick fail");
						}
						if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
							pd.dismiss();
						}
						// 进入主页面
						Intent intent = new Intent(LoginActivity.this,
								MainActivity.class);
						startActivity(intent);

						finish();
					}

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(final int code, final String message) {
						if (!progressShow) {
							return;
						}
						runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
			}
		});
	}

    private void setUserNameTextChangeListener() {
        // TODO Auto-generated method stub
     // 如果用户名改变，清空密码
        metUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                metPassword.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    

	private void initializeContacts() {
		Map<String, User> userlist = new HashMap<String, User>();
		// 添加user"申请与通知"
		User newFriends = new User();
		newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				R.string.Application_and_notify);
		newFriends.setNick(strChat);

		userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
		// 添加"群聊"
		User groupUser = new User();
		String strGroup = getResources().getString(R.string.group_chat);
		groupUser.setUsername(Constant.GROUP_USERNAME);
		groupUser.setNick(strGroup);
		groupUser.setHeader("");
		userlist.put(Constant.GROUP_USERNAME, groupUser);
		
		// 添加"Robot"
		User robotUser = new User();
		String strRobot = getResources().getString(R.string.robot_chat);
		robotUser.setUsername(Constant.CHAT_ROBOT);
		robotUser.setNick(strRobot);
		robotUser.setHeader("");
		userlist.put(Constant.CHAT_ROBOT, robotUser);
		
		// 存入内存
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).setContactList(userlist);
		// 存入db
		EMUserDao dao = new EMUserDao(LoginActivity.this);
		List<User> users = new ArrayList<User>(userlist.values());
		dao.saveContactList(users);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
	
	/**
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	private boolean loginAppServer(String userName, String password){
	    UserDao dao = new UserDao(mContext);
	    UserBean user = dao.findUserByUserName(userName);
		Log.e(TAG,"LoginAvtivity.loginAppServer.user="+user);
	    if(user != null){
	        if(user.getPassword().equals(MD5.getData(password))){
	            saveUser(user);
	            return true;
             }
		}else{
			try{
				user = NetUtil.login(userName, password);
                Log.e(TAG,"LoginAvtivity.loginAppServer.else.user="+user);
				if("ok".equals(user.getResult())){
					saveUser(user);
					user.setPassword(MD5.getData(password));
					dao.addUser(user);
					return true;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	    return false;
	}

    private void saveUser(UserBean user) {
        SuperWeChatApplication instanceApplication = SuperWeChatApplication.getInstance();
        instanceApplication.setUserBean(user);
        instanceApplication.setUserName(user.getUserName());
        instanceApplication.setPassword(user.getPassword());
        instanceApplication.currentUserNick = user.getNick();
    }

	private void getUser(){
		UserBean user = SuperWeChatApplication.getInstance().getUserBean();
		Log.e(TAG,"LoginActivity.getUser.user="+user);
		Log.e(TAG,"LoginActivity.getUser.username="+SuperWeChatApplication.getInstance().getUserName());
		Log.e(TAG,"LoginActivity.getUser.password="+SuperWeChatApplication.getInstance().getPassword());
//		if(user!=null){
//
//		}else{
//
//		}
	}
	
}
