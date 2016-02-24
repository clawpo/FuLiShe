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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.util.EMLog;

import java.util.ArrayList;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.adapter.GroupAdapter;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.bean.GroupBean;

public class GroupsActivity extends BaseActivity {
    static final int REQUEST_NEW_GROUP=0;
    static final int REQUEST_NEW_PUBLIC_GROUP=1;
    static final int REQUEST_ENTER_CHATACTIVITY=2;
	public static final String TAG = "GroupsActivity";
	private ListView groupListView;
//	protected List<EMGroup> grouplist;
	ArrayList<GroupBean> mGroupList;
	int mPosition;//正在修改的群名称在集合中的下标
	private GroupAdapter mGroupAdapter;
	private InputMethodManager inputMethodManager;
	public static GroupsActivity instance;
	private SyncListener syncListener;
	private View progressBar;
	private SwipeRefreshLayout swipeRefreshLayout;
	Handler handler = new Handler();

	DeleteGroupMemberReceiver mDeleteGroupMemberReceiver;
	GroupChangedReceiver mGroupListChangedReceiver;
	
	class SyncListener implements HXSDKHelper.HXSyncListener {
		@Override
		public void onSyncSucess(final boolean success) {
			EMLog.d(TAG, "onSyncGroupsFinish success:" + success);
			runOnUiThread(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(false);
					if (success) {
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								refresh();
								progressBar.setVisibility(View.GONE);
							}
						}, 1000);
					} else {
						if (!GroupsActivity.this.isFinishing()) {
							String s1 = getResources()
									.getString(
											R.string.Failed_to_get_group_chat_information);
							Toast.makeText(GroupsActivity.this, s1, Toast.LENGTH_LONG).show();
							progressBar.setVisibility(View.GONE);
						}
					}
				}
			});
		}
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_groups);

		instance = this;
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mGroupList = SuperWeChatApplication.getInstance().getGroupList();
		initView();
		setListener();
		refresh();
		registerDeleteGroupMemberReceiver();
		registerGroupChangedReceiver();
	}

    private void initView() {
		groupListView = (ListView) findViewById(R.id.list);
		
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
		                android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
			    MainActivity.asyncFetchGroupsFromServer();
			}
		});
		
		mGroupAdapter = new GroupAdapter(this, mGroupList);
		groupListView.setAdapter(mGroupAdapter);
		groupListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 1) {
					// 新建群聊
					startActivityForResult(new Intent(GroupsActivity.this, NewGroupActivity.class), 0);
				} else if (position == 2) {
					// 添加公开群
					startActivityForResult(new Intent(GroupsActivity.this, PublicGroupsActivity.class), 0);
				} else {
					// 进入群聊
					Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
					// it is group chat
					intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
					intent.putExtra("groupId", mGroupAdapter.getItem(position - 3).getGroupId());
					startActivityForResult(intent, 0);
				}
			}

		});
		groupListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
		
		progressBar = (View)findViewById(R.id.progress_bar);
		
		syncListener = new SyncListener();
		HXSDKHelper.getInstance().addSyncGroupListener(syncListener);

		if (!HXSDKHelper.getInstance().isGroupsSyncedWithServer()) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
		
		refresh();
		registerDeleteGroupMemberReceiver();
	}

	/**
	 * 注册DownloadContactsTask下载群成功后发送的广播
	 */
    private void registerGroupChangedReceiver() {
        mGroupListChangedReceiver=new GroupChangedReceiver();
        IntentFilter filter=new IntentFilter("update_group");
        filter.addAction("update_group_name");
        registerReceiver(mGroupListChangedReceiver,filter);
    }

	/**
	 * 注册群成员退群后的广播监听
	 */
    private void registerDeleteGroupMemberReceiver() {
        mDeleteGroupMemberReceiver=new DeleteGroupMemberReceiver();
		IntentFilter filter=new IntentFilter("delete_group");
		filter.addAction("exit_group");
		registerReceiver(mDeleteGroupMemberReceiver, filter);
    }
    
    

    private void setListener() {
        syncListener = new SyncListener();
        HXSDKHelper.getInstance().addSyncGroupListener(syncListener);
        setGroupRefreshListener();
        setGroupListViewItemClickListener();
        setGroupListViewTouchListener();
    }
	private void setGroupRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.asyncFetchGroupsFromServer();
            }
        });
    }

    private void setGroupListViewItemClickListener() {
        groupListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                if(position == 1){
                    // 新建群聊
                    startActivityForResult(new Intent(GroupsActivity.this, NewGroupActivity.class), REQUEST_NEW_GROUP);
                }else if (position == 2) {
                    // 添加公开群
                    startActivityForResult(new Intent(GroupsActivity.this,PublicGroupsActivity.class), REQUEST_NEW_PUBLIC_GROUP);
                }else{
                    mPosition=position;
                    // 进入群聊
                    Intent intent=new Intent(GroupsActivity.this,ChatActivity.class);
                    // it is group chat
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                    intent.putExtra("group", mGroupAdapter.getItem(position));
                    startActivityForResult(intent, REQUEST_ENTER_CHATACTIVITY);
                }
            }
        });
    }

    private void setGroupListViewTouchListener() {
        groupListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
    }
    /**
	 * 进入公开群聊列表
	 */
	public void onPublicGroups(View view) {
		startActivity(new Intent(this, PublicGroupsActivity.class));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(RESULT_OK!=resultCode){
		    return;
		}
		switch (requestCode) {
        case REQUEST_NEW_GROUP:
            GroupBean group=(GroupBean) data.getSerializableExtra("group");
            mGroupAdapter.addItem(group);
            break;
        }
	}

	@Override
	public void onResume() {
		super.onResume();
		mGroupList = SuperWeChatApplication.getInstance().getGroupList();
		mGroupAdapter = new GroupAdapter(this, mGroupList);
		groupListView.setAdapter(mGroupAdapter);
		mGroupAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		if (syncListener != null) {
			HXSDKHelper.getInstance().removeSyncGroupListener(syncListener);
			syncListener = null;
		}
		unregisterReceiver(mDeleteGroupMemberReceiver);
		unregisterReceiver(mGroupListChangedReceiver);
		super.onDestroy();

		instance = null;
	}
	
	public void refresh() {
		if (groupListView != null && mGroupAdapter != null) {
		    mGroupList = SuperWeChatApplication.getInstance().getGroupList();
		    mGroupAdapter = new GroupAdapter(GroupsActivity.this, mGroupList);
			groupListView.setAdapter(mGroupAdapter);
			mGroupAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}
	class DeleteGroupMemberReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GroupBean group=(GroupBean) intent.getSerializableExtra("group");
            mGroupAdapter.remove(group);
        }
	    
	}
	/**群组下载完成后，DownloadContactsTask发送的广播*/
	class GroupChangedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mGroupAdapter.getCount()==3 && intent.getAction().equals("update_group")){
                ArrayList<GroupBean> groupList=SuperWeChatApplication.getInstance().getGroupList();
                if(!mGroupList.containsAll(groupList)){
                    mGroupAdapter.AddItems(groupList);
                }
            }else if(intent.getAction().equals("update_group_name")) {
	            //获取广播中携带的新群名
                String newGroupName=intent.getStringExtra("groupName");
	            //刷新群列表显示
                GroupBean group=mGroupAdapter.getItem(mPosition);
                group.setName(newGroupName);
                mGroupAdapter.notifyDataSetChanged();
	            //修改全局的群集合
                SuperWeChatApplication.getInstance().setGroupList(mGroupAdapter.getGroups());
            }
        }
	    
	}
}
