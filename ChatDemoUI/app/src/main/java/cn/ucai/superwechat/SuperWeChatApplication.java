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
package cn.ucai.superwechat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.easemob.EMCallBack;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.bean.ContactBean;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.bean.UserBean;

public class SuperWeChatApplication extends Application {

	//    public static String SERVER_ROOT="http://10.0.2.2:8080/SuperQQ3Server/Server";
	public static String SERVER_ROOT="http://139.196.185.33:8080/SuperQQ3Server/Server";

	public static boolean isLocalhost = false;

	public static Context applicationContext;
	private static SuperWeChatApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	
	/**
	 * 当前用户nickname,为了苹果推送不是userid而是昵称
	 */
	public static String currentUserNick = "";
	public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

	@Override
	public void onCreate() {
		super.onCreate();
        applicationContext = this;
        instance = this;

        /**
         * this function will initialize the HuanXin SDK
         * 
         * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
         * 
         * 环信初始化SDK帮助函数
         * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
         * 
         * for example:
         * 例子：
         * 
         * public class DemoHXSDKHelper extends HXSDKHelper
         * 
         * HXHelper = new DemoHXSDKHelper();
         * if(HXHelper.onInit(context)){
         *     // do HuanXin related work
         * }
         */

		if(isLocalhost){
			initServerUrl();
		}else{
			SERVER_ROOT = "http://10.0.2.2:8080/SuperQQ4Server/Server";
		}
		Log.e("main","***************************Superwechat.serverurl="+SERVER_ROOT);
		hxSDKHelper.onInit(applicationContext);
	}


	public void initServerUrl() {
		final SharedPreferences sp=getSharedPreferences("server_url", MODE_PRIVATE);
		String serverUrl=sp.getString("url", "");
		if(serverUrl==null || serverUrl.isEmpty()){
			SERVER_ROOT="http://139.196.185.33:8080/SuperQQ3Server/Server";
		}else {
			SuperWeChatApplication.SERVER_ROOT=serverUrl+":8080/SuperQQ3Server/Server";
		}
	}

	public static SuperWeChatApplication getInstance() {
		return instance;
	}
 

	/**
	 * 获取当前登陆用户名
	 *
	 * @return
	 */
	public String getUserName() {
	    return hxSDKHelper.getHXId();
	}

	/**
	 * 获取密码
	 *
	 * @return
	 */
	public String getPassword() {
		return hxSDKHelper.getPassword();
	}

	/**
	 * 设置用户名
	 *
	 * @param username
	 */
	public void setUserName(String username) {
	    hxSDKHelper.setHXId(username);
	}

	/**
	 * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
	 * 内部的自动登录需要的密码，已经加密存储了
	 *
	 * @param pwd
	 */
	public void setPassword(String pwd) {
	    hxSDKHelper.setPassword(pwd);
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout(final boolean isGCM,final EMCallBack emCallBack) {
		// 先调用sdk logout，在清理app中自己的数据
	    hxSDKHelper.logout(isGCM,emCallBack);
	}
	
	
	UserBean userBean;

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

	public void logout(final EMCallBack emCallBack) {
	    hxSDKHelper.logout(true, emCallBack);
	}
	
    private ArrayList<UserBean> contactList = new ArrayList<UserBean>();

    public ArrayList<UserBean> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<UserBean> contactList) {
        this.contactList = contactList;
    }
    
    private HashMap<Integer, ContactBean> contacts = new HashMap<Integer, ContactBean>();

    public HashMap<Integer, ContactBean> getContacts() {
        return contacts;
    }

    public void setContacts(HashMap<Integer, ContactBean> contacts) {
        this.contacts = contacts;
    }
    
    private ArrayList<GroupBean> mGroupList = new ArrayList<GroupBean>();

    public ArrayList<GroupBean> getGroupList() {
        return mGroupList;
    }

    public void setGroupList(ArrayList<GroupBean> mGroupList) {
        this.mGroupList = mGroupList;
    }
    
    private ArrayList<GroupBean> mPublicGroupList = new ArrayList<GroupBean>();

    public ArrayList<GroupBean> getPublicGroupList() {
        return mPublicGroupList;
    }

    public void setPublicGroupList(ArrayList<GroupBean> mPublicGroupList) {
        this.mPublicGroupList = mPublicGroupList;
    }
    
    /**
     * 缓存指定群成员的集合
     */
    private HashMap<String, ArrayList<UserBean>> mGroupMembers = new HashMap<String, ArrayList<UserBean>>();

    public HashMap<String, ArrayList<UserBean>> getGroupMembers() {
        return mGroupMembers;
    }

    public void setGroupMembers(HashMap<String, ArrayList<UserBean>> mGroupMembers) {
        this.mGroupMembers = mGroupMembers;
    }
    
    
    
}
