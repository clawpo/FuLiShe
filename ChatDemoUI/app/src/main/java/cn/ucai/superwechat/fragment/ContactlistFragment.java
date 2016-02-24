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
package cn.ucai.superwechat.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.AddContactActivity;
import cn.ucai.superwechat.activity.ChatActivity;
import cn.ucai.superwechat.activity.GroupsActivity;
import cn.ucai.superwechat.activity.MainActivity;
import cn.ucai.superwechat.activity.NewFriendsMsgActivity;
import cn.ucai.superwechat.activity.PublicChatRoomsActivity;
import cn.ucai.superwechat.activity.RobotsActivity;
import cn.ucai.superwechat.adapter.ContactAdapter;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.applib.controller.HXSDKHelper.HXSyncListener;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.db.EMUserDao;
import cn.ucai.superwechat.db.InviteMessgeDao;
import cn.ucai.superwechat.domain.User;
import cn.ucai.superwechat.utils.UserUtils;
import cn.ucai.superwechat.widget.Sidebar;

/**
 * 联系人列表页
 * 
 */
public class ContactlistFragment extends Fragment {
	public static final String TAG = "ContactlistFragment";
	private ContactAdapter adapter;
//	private List<User> contactList;
	private ListView listView;
	private boolean hidden;
	private Sidebar sidebar;
	private InputMethodManager inputMethodManager;
	private List<String> blackList;
	ImageButton clearSearch;
	EditText query;
	HXContactSyncListener contactSyncListener;
	HXBlackListSyncListener blackListSyncListener;
	HXContactInfoSyncListener contactInfoSyncListener;
	View progressBar;
	Handler handler = new Handler();
//    private User toBeProcessUser;
    private String toBeProcessUsername;
    
    /** 被操作的当前联系人*/
    private UserBean toBeProcessUser;
    ContactChangeReceiver mContactChangeReceiver;
    /** 联系人集合*/
    ArrayList<UserBean> mContactList=new ArrayList<UserBean>();

	class HXContactSyncListener implements HXSDKHelper.HXSyncListener {
		@Override
		public void onSyncSucess(final boolean success) {
			EMLog.d(TAG, "on contact list sync success:" + success);
			ContactlistFragment.this.getActivity().runOnUiThread(new Runnable() {
				public void run() {
				    getActivity().runOnUiThread(new Runnable(){

		                @Override
		                public void run() {
		                    if(success){
		                        progressBar.setVisibility(View.GONE);
                                refresh();
		                    }else{
		                        String s1 = getResources().getString(R.string.get_failed_please_check);
		                        Toast.makeText(getActivity(), s1, Toast.LENGTH_SHORT).show();
		                        progressBar.setVisibility(View.GONE);
		                    }
		                }
		                
		            });
				}
			});
		}
	}
	
	class HXBlackListSyncListener implements HXSyncListener{

        @Override
        public void onSyncSucess(boolean success) {
            getActivity().runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    blackList = EMContactManager.getInstance().getBlackListUsernames();
                    refresh();
                }
                
            });
        }
	    
	};
	
	class HXContactInfoSyncListener implements HXSDKHelper.HXSyncListener{

		@Override
		public void onSyncSucess(final boolean success) {
			EMLog.d(TAG, "on contactinfo list sync success:" + success);
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					progressBar.setVisibility(View.GONE);
					if(success){
						refresh();
					}
				}
			});
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contact_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
		if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false)){
		    return;
		}
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		listView = (ListView) getView().findViewById(R.id.list);
		sidebar = (Sidebar) getView().findViewById(R.id.sidebar);
		sidebar.setListView(listView);
        
		//黑名单列表
		blackList = EMContactManager.getInstance().getBlackListUsernames();
//		contactList = new ArrayList<User>();
		// 获取设置contactlist
//		getContactList();
		initContactList();
		
		//搜索框
		query = (EditText) getView().findViewById(R.id.query);
		query.setHint(R.string.search);
		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				adapter.getFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);
					
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideSoftKeyboard();
			}
		});

		Log.e("main","ContactlistFragment.onActivityCreated.mContactList="+mContactList.size());
		Log.e("main","SuperWeChatApplication.getInstance().getContactList().size()="
				+SuperWeChatApplication.getInstance().getContactList().size());
		// 设置adapter
		adapter = new ContactAdapter(getActivity(), R.layout.row_contact, mContactList);
		listView.setAdapter(adapter);
        setListener();

		
		registerForContextMenu(listView);
		
		progressBar = (View) getView().findViewById(R.id.progress_bar);

		contactSyncListener = new HXContactSyncListener();
		HXSDKHelper.getInstance().addSyncContactListener(contactSyncListener);
		
		blackListSyncListener = new HXBlackListSyncListener();
		HXSDKHelper.getInstance().addSyncBlackListListener(blackListSyncListener);
		
		contactInfoSyncListener = new HXContactInfoSyncListener();
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().addSyncContactInfoListener(contactInfoSyncListener);
		
		if (!HXSDKHelper.getInstance().isContactsSyncedWithServer()) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
		
		registerContactChangeReceiver();
	}

    private void setListener() {
        setContactItemClickListener();
		setContactListTouchListener();
		setAddContactListener();
        
    }

    /** 进入添加好友页*/
    private void setAddContactListener() {
		getView().findViewById(R.id.iv_new_contact).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });
    }

    private void setContactListTouchListener() {
        listView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getActivity().getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
    }

    /**
     * 联系人列表项单击事件监听
     */
    private void setContactItemClickListener() {
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String username = adapter.getItem(position).getUserName();
				if (Constant.NEW_FRIENDS_USERNAME.equals(username)) {
					// 进入申请与通知页面
					User user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME);
					user.setUnreadMsgCount(0);
					startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
				} else if (Constant.GROUP_USERNAME.equals(username)) {
					// 进入群聊列表页面
					startActivity(new Intent(getActivity(), GroupsActivity.class));
				} else if(Constant.CHAT_ROOM.equals(username)){
					//进入聊天室列表页面
				    startActivity(new Intent(getActivity(), PublicChatRoomsActivity.class));
				}else if(Constant.CHAT_ROBOT.equals(username)){
					//进入Robot列表页面
					startActivity(new Intent(getActivity(), RobotsActivity.class));
				}else {
					// demo中直接进入聊天页面，实际一般是进入用户详情页
					startActivity(new Intent(getActivity(), ChatActivity.class)
					    .putExtra("userId", adapter.getItem(position).getUserName()));
				}
			}
		});
    }

	/**
	 * 注册联系人改变的广播接收者
	 */
    private void registerContactChangeReceiver() {
        mContactChangeReceiver=new ContactChangeReceiver();
		IntentFilter filter=new IntentFilter("update_contacts");
		getActivity().registerReceiver(mContactChangeReceiver, filter);
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (((AdapterContextMenuInfo) menuInfo).position > 2) {
		    toBeProcessUser = adapter.getItem(((AdapterContextMenuInfo) menuInfo).position);
		    toBeProcessUsername = toBeProcessUser.getUserName();
			getActivity().getMenuInflater().inflate(R.menu.context_contact_list, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			try {
                // 删除此联系人
                deleteContact(toBeProcessUser);
                // 删除相关的邀请消息
                InviteMessgeDao dao = new InviteMessgeDao(getActivity());
                dao.deleteMessage(toBeProcessUser.getUserName());
            } catch (Exception e) {
                e.printStackTrace();
            }
			return true;
		}else if(item.getItemId() == R.id.add_to_blacklist){
			moveToBlacklist(toBeProcessUsername);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}

	/**
	 * 删除联系人
	 * 
	 * @param tobeDeleteUser
	 */
	public void deleteContact(final UserBean tobeDeleteUser) {
		String st1 = getResources().getString(R.string.deleting);
		final String st2 = getResources().getString(R.string.Delete_failed);
		final ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMContactManager.getInstance().deleteContact(tobeDeleteUser.getUserName());
					// 删除db和内存中此用户的数据
					EMUserDao dao = new EMUserDao(getActivity());
					dao.deleteContact(tobeDeleteUser.getUserName());
					((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().remove(tobeDeleteUser.getUserName());
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							adapter.remove(tobeDeleteUser);
							adapter.notifyDataSetChanged();

						}
					});
				} catch (final Exception e) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2 + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});

				}

			}
		}).start();

	}

	/**
	 * 把user移入到黑名单
	 */
	private void moveToBlacklist(final String username){
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
		final String st2 = getResources().getString(R.string.Move_into_blacklist_success);
		final String st3 = getResources().getString(R.string.Move_into_blacklist_failure);
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					//加入到黑名单
					EMContactManager.getInstance().addUserToBlackList(username,false);
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2, Toast.LENGTH_SHORT).show();
							refresh();
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st3, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
		
	}
	
	// 刷新ui
	public void refresh() {
		try {
			// 可能会在子线程中调到这方法
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					//getContactList();
				    initContactList();
					adapter.notifyDataSetChanged();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		if (contactSyncListener != null) {
			HXSDKHelper.getInstance().removeSyncContactListener(contactSyncListener);
			contactSyncListener = null;
		}
		
		if(blackListSyncListener != null){
		    HXSDKHelper.getInstance().removeSyncBlackListListener(blackListSyncListener);
		}
		
		if(contactInfoSyncListener != null){
			((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().removeSyncContactInfoListener(contactInfoSyncListener);
		}
		super.onDestroy();
	}
	
	public void showProgressBar(boolean show) {
		if (progressBar != null) {
			if (show) {
				progressBar.setVisibility(View.VISIBLE);
			} else {
				progressBar.setVisibility(View.GONE);
			}
		}
	}

	
	void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null){
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    if(((MainActivity)getActivity()).isConflict){
	    	outState.putBoolean("isConflict", true);
	    }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
	    	outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
	    }
	    
	}
	/**
	 * 联系人改变的广播接收者类
	 * 1-接收来自DownloadContactTask发送的联系人下载成功的广播
	 * 
	 * @author yao
	 *
	 */
	class ContactChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<UserBean> contactList = SuperWeChatApplication.getInstance().getContactList();
			Log.e("main","ContactlistFragment.ContactChangdReceiver.contactList.size="+contactList.size()+",mContactList.size="+mContactList.size());
			Log.e("main","contactList="+contactList);
			Log.e("main","mContactList="+mContactList);
			mContactList.clear();
            mContactList.addAll(contactList);
            Log.e("main","1---mContactList.size="+mContactList.size());
            for(UserBean user: contactList){
                UserUtils.setUserHearder(user);
            }
            Collections.sort(mContactList,new Comparator<UserBean>() {

                @Override
                public int compare(UserBean user1, UserBean user2) {
                    return user1.getHeader().compareTo(user2.getHeader());
                }
            });
            refresh();
            Log.e("main","2---mContactList.size="+mContactList.size());
        }
	}
	public void initContactList(){
		Log.e("main","initContactList...1.....mContactList.size="+mContactList.size());
		ArrayList<UserBean> contactList = SuperWeChatApplication.getInstance().getContactList();
		Log.e("main","ContactlistFragment.ContactChangdReceiver.contactList.size="+contactList.size()+",mContactList.size="+mContactList.size());

		Resources res = getActivity().getResources();
	    UserBean chatRoomUser = new UserBean();
	    chatRoomUser.setUserName(Constant.CHAT_ROBOT);
	    chatRoomUser.setNick(res.getString(R.string.chat_room));
//        Log.e("main","mContactList.indexOf(chatRoomUser)="+mContactList.indexOf(chatRoomUser));
	    if(mContactList.indexOf(chatRoomUser)==-1){
	        mContactList.add(0,chatRoomUser);
	    }
        UserBean groupUser = new UserBean();
        String strGroup = getActivity().getResources().getString(
            R.string.group_chat);
        groupUser.setUserName(Constant.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
//        Log.e("main","mContactList.indexOf(groupUser)="+mContactList.indexOf(groupUser));
        if(mContactList.indexOf(groupUser)==-1){
            mContactList.add(0, groupUser);
        }
		UserBean newFriends = new UserBean();
		newFriends.setUserName(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getActivity().getResources().getString(
				R.string.Application_and_notify);
		newFriends.setNick(strChat);
//        Log.e("main","mContactList.indexOf(newFriends)="+mContactList.indexOf(newFriends));
		if(mContactList.indexOf(newFriends)==-1){
			mContactList.add(0, newFriends);
		}

        Log.e("main","initContactList...2.....mContactList.size="+mContactList.size());
    }
	
	@Override
	public void onDestroyView() {
	    super.onDestroyView();
	    if(mContactChangeReceiver!=null){
	        getActivity().unregisterReceiver(mContactChangeReceiver);
	    }
	}
}
