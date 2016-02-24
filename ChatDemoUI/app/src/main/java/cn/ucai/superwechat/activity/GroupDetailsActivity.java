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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.NetUtils;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.utils.ImageLoader;
import cn.ucai.superwechat.utils.NetUtil;
import cn.ucai.superwechat.utils.Utils;
import cn.ucai.superwechat.widget.ExpandGridView;

public class GroupDetailsActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "GroupDetailsActivity";
	private static final int REQUEST_CODE_ADD_USER = 0;
	private static final int REQUEST_CODE_EXIT = 1;
	private static final int REQUEST_CODE_DELETE_GROUP = 2;
	private static final int REQUEST_CODE_CLEAR_ALL_HISTORY = 3;
	private static final int REQUEST_CODE_ADD_TO_BALCKLIST = 4;
	private static final int REQUEST_CODE_EDIT_GROUPNAME = 5;

	String longClickUsername = null;

	private ExpandGridView mgvGroupMembers;
	private String groupId;
	private ProgressBar loadingPB;
	private Button exitBtn;
	private Button deleteBtn;
	private GroupBean mGroup;
	private EMGroup group;
	private GroupMembersAdapter mAdapter;
	private int referenceWidth;
	private int referenceHeight;
	private ProgressDialog progressDialog;

	private RelativeLayout rl_switch_block_groupmsg;
	/**
	 * 屏蔽群消息imageView
	 */
	private ImageView iv_switch_block_groupmsg;
	/**
	 * 关闭屏蔽群消息imageview
	 */
	private ImageView iv_switch_unblock_groupmsg;

	public static GroupDetailsActivity instance;
	
	String st = "";
	// 清空所有聊天记录
	private RelativeLayout clearAllHistory;
	private RelativeLayout blacklistLayout;
	private RelativeLayout changeGroupNameLayout;
    private RelativeLayout idLayout;
    private TextView idText;
    
    ArrayList<UserBean> mGroupMembers;
    GroupDetailsActivity mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mContext=this;
	    // 获取传过来的groupid
        groupId = getIntent().getStringExtra("groupId");
        group = EMGroupManager.getInstance().getGroup(groupId);
        mGroupMembers = (ArrayList<UserBean>) getIntent().getSerializableExtra("groupMembers");

		Log.e(TAG,"groupId="+groupId);
		Log.e(TAG,"group="+group);
		Log.e(TAG,"mGroupMembers="+mGroupMembers);

        // we are not supposed to show the group if we don't find the group
        if(group == null){
            finish();
            return;
        }
        
		setContentView(R.layout.activity_group_details);
		instance = this;
		st = getResources().getString(R.string.people);
		clearAllHistory = (RelativeLayout) findViewById(R.id.clear_all_history);
		mgvGroupMembers = (ExpandGridView) findViewById(R.id.gridview);
		loadingPB = (ProgressBar) findViewById(R.id.progressBar);
		exitBtn = (Button) findViewById(R.id.btn_exit_grp);
		deleteBtn = (Button) findViewById(R.id.btn_exitdel_grp);
		blacklistLayout = (RelativeLayout) findViewById(R.id.rl_blacklist);
		changeGroupNameLayout = (RelativeLayout) findViewById(R.id.rl_change_group_name);
		idLayout = (RelativeLayout) findViewById(R.id.rl_group_id);
		idLayout.setVisibility(View.VISIBLE);
		idText = (TextView) findViewById(R.id.tv_group_id_value);
		
		rl_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.rl_switch_block_groupmsg);

		iv_switch_block_groupmsg = (ImageView) findViewById(R.id.iv_switch_block_groupmsg);
		iv_switch_unblock_groupmsg = (ImageView) findViewById(R.id.iv_switch_unblock_groupmsg);

		rl_switch_block_groupmsg.setOnClickListener(this);

		Drawable referenceDrawable = getResources().getDrawable(R.drawable.smiley_add_btn);
		referenceWidth = referenceDrawable.getIntrinsicWidth();
		referenceHeight = referenceDrawable.getIntrinsicHeight();


		idText.setText(groupId);
		if (group.getOwner() == null || "".equals(group.getOwner())
				|| !group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
			exitBtn.setVisibility(View.GONE);
			deleteBtn.setVisibility(View.GONE);
			blacklistLayout.setVisibility(View.GONE);
			changeGroupNameLayout.setVisibility(View.GONE);
		}
		// 如果自己是群主，显示解散按钮
		if (EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())) {
			exitBtn.setVisibility(View.GONE);
			deleteBtn.setVisibility(View.VISIBLE);
		}
		
		((TextView) findViewById(R.id.group_name)).setText(group.getGroupName() + "(" + group.getAffiliationsCount() + st);
		
		initGroupMembers();
		setListener();
		


	}

	private void initGroupMembers() {
        mGroup=(GroupBean) getIntent().getSerializableExtra("group");
        //获取登陆用户所属的群
        HashMap<String, ArrayList<UserBean>> groupMemberMap=SuperWeChatApplication.getInstance().getGroupMembers();
        mGroupMembers=groupMemberMap.get(groupId);
        if(mGroupMembers!=null){
            initAdapter();
        }else{
            new DownloadGroupTask(instance, groupId).execute();
        }
    }

    /**
     * 设置监听器
     */
    private void setListener() {
        setGridViewTouchListener();
        setClearAllHistoryListener();
        setBlackClickListener();
        setGroupNameChangeListener();
        setExitGroupClickListener();
        setDeleteGroupClickListener();
    }

    /**解散群*/
    private void setDeleteGroupClickListener() {
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mContext,ExitGroupDialog.class)
                .putExtra("deleteToast", getString(R.string.dissolution_group_hint)),
                REQUEST_CODE_DELETE_GROUP);
            }
        });
    }

    /**退群按钮单击事件监听*/
    private void setExitGroupClickListener() {
        exitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(instance,ExitGroupDialog.class),
                        REQUEST_CODE_EXIT);
            }
        });
    }

    /** 清除所有聊天记录*/
    private void setClearAllHistoryListener() {
        clearAllHistory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String st9 = getResources().getString(R.string.sure_to_empty_this);
                Intent intent = new Intent(instance,AlertDialog.class);
                intent.putExtra("cancel", true);
                intent.putExtra("titleIsCancel", true);
                intent.putExtra("msg", st9);
                startActivityForResult(intent, REQUEST_CODE_CLEAR_ALL_HISTORY);
            }
        });
    }

    /** 屏蔽群消息*/
    private void setBlackClickListener() {
        blacklistLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(instance,GroupBlacklistActivity.class)
                .putExtra("groupId", groupId));
            }
        });
    }

    /**设置修改群名*/
    private void setGroupNameChangeListener() {
        changeGroupNameLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(instance,EditActivity.class)
                .putExtra("data", group.getName()), REQUEST_CODE_EDIT_GROUPNAME);
            }
        });
    }

    /** 设置OnTouchListener*/
    private void setGridViewTouchListener() {
        mgvGroupMembers.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(mAdapter.isInDeleteMode){
                        mAdapter.isInDeleteMode = false;
                        mAdapter.notifyDataSetChanged();
                        return true;
                    }
                    break;
                }
                return false;
            }
        });
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String st1 = getResources().getString(R.string.being_added);
		String st2 = getResources().getString(R.string.is_quit_the_group_chat);
		String st3 = getResources().getString(R.string.chatting_is_dissolution);
		String st4 = getResources().getString(R.string.are_empty_group_of_news);
		String st5 = getResources().getString(R.string.is_modify_the_group_name);
		final String st6 = getResources().getString(R.string.Modify_the_group_name_successful);
		final String st7 = getResources().getString(R.string.change_the_group_name_failed_please);
		String st8 = getResources().getString(R.string.Are_moving_to_blacklist);
		final String st9 = getResources().getString(R.string.failed_to_move_into);
		
		final String stsuccess = getResources().getString(R.string.Move_into_blacklist_success);
		if (resultCode == RESULT_OK) {
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(GroupDetailsActivity.this);
				progressDialog.setMessage(st1);
				progressDialog.setCanceledOnTouchOutside(false);
			}
			switch (requestCode) {
			case REQUEST_CODE_ADD_USER:// 添加群成员
				final String[] newmembers = data.getStringArrayExtra("newmembers");
				new AddGroupMembersTask(mContext, newmembers).execute();
				break;
			case REQUEST_CODE_EXIT: // 退出群
			    String userName=SuperWeChatApplication.getInstance().getUserName();
			    if(!mGroup.getOwner().equals(userName)){
			        new ExitGroupTask(mContext, mGroup, userName).execute();
			    }
				break;
			case REQUEST_CODE_DELETE_GROUP: // 解散群
				progressDialog.setMessage(st3);
				progressDialog.show();
				new DeleteGroupTask(mContext, mGroup).execute();
				break;
			case REQUEST_CODE_CLEAR_ALL_HISTORY:
				// 清空此群聊的聊天记录
				progressDialog.setMessage(st4);
				progressDialog.show();
				clearGroupHistory();
				break;

			case REQUEST_CODE_EDIT_GROUPNAME: //修改群名称
				final String newGroupName = data.getStringExtra("groupName");
				if(!TextUtils.isEmpty(newGroupName)){
				    new UpdateGroupNameTask(mContext, mGroup, newGroupName, 
				            group.getAffiliationsCount()).execute();
				}
				break;
			case REQUEST_CODE_ADD_TO_BALCKLIST:
				progressDialog.setMessage(st8);
				progressDialog.show();
				new Thread(new Runnable() {
					public void run() {
						try {
						    EMGroupManager.getInstance().blockUser(groupId, longClickUsername);
							runOnUiThread(new Runnable() {
								public void run() {
								    mAdapter.notifyDataSetChanged();
									progressDialog.dismiss();
									Toast.makeText(getApplicationContext(), stsuccess, Toast.LENGTH_SHORT).show();
								}
							});
						} catch (EaseMobException e) {
							runOnUiThread(new Runnable() {
								public void run() {
									progressDialog.dismiss();
									Toast.makeText(getApplicationContext(), st9, Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				}).start();

				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 点击退出群组按钮
	 * 
	 * @param view
	 */
	public void exitGroup(View view) {
		startActivityForResult(new Intent(this, ExitGroupDialog.class), REQUEST_CODE_EXIT);

	}

	/**
	 * 点击解散群组按钮
	 * 
	 * @param view
	 */
	public void exitDeleteGroup(View view) {
		startActivityForResult(new Intent(this, ExitGroupDialog.class).putExtra("deleteToast", getString(R.string.dissolution_group_hint)),
				REQUEST_CODE_DELETE_GROUP);

	}

	/**
	 * 清空群聊天记录
	 */
	public void clearGroupHistory() {

		EMChatManager.getInstance().clearConversation(group.getGroupId());
		progressDialog.dismiss();
		// adapter.refresh(EMChatManager.getInstance().getConversation(toChatUsername));

	}

	/**
	 * 退出群组
	 * 
	 * @param
	 */
	private void exitGrop() {
		String st1 = getResources().getString(R.string.Exit_the_group_chat_failure);
		new Thread(new Runnable() {
			public void run() {
				try {
				    EMGroupManager.getInstance().exitFromGroup(groupId);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
							if(ChatActivity.activityInstance != null)
							    ChatActivity.activityInstance.finish();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Exit_the_group_chat_failure) + " " + e.getMessage(), 1).show();
						}
					});
				}
			}
		}).start();
	}

	/**
	 * 解散群组
	 * 
	 * @param
	 */
	private void deleteGrop() {
		final String st5 = getResources().getString(R.string.Dissolve_group_chat_tofail);
		new Thread(new Runnable() {
			public void run() {
				try {
				    EMGroupManager.getInstance().exitAndDeleteGroup(groupId);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
							if(ChatActivity.activityInstance != null)
							    ChatActivity.activityInstance.finish();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(getApplicationContext(), st5 + e.getMessage(), 1).show();
						}
					});
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		String st6 = getResources().getString(R.string.Is_unblock);
		final String st7 = getResources().getString(R.string.remove_group_of);
		switch (v.getId()) {
		case R.id.rl_switch_block_groupmsg: // 屏蔽群组
			if (iv_switch_block_groupmsg.getVisibility() == View.VISIBLE) {
				EMLog.d(TAG, "change to unblock group msg");
				if (progressDialog == null) {
	                progressDialog = new ProgressDialog(GroupDetailsActivity.this);
	                progressDialog.setCanceledOnTouchOutside(false);
	            }
				progressDialog.setMessage(st6);
				progressDialog.show();
				new Thread(new Runnable() {
                    public void run() {
                        try {
                            EMGroupManager.getInstance().unblockGroupMessage(groupId);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
                                    iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
                                    progressDialog.dismiss();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), st7, Toast.LENGTH_SHORT).show();
                                }
                            });
                            
                        }
                    }
                }).start();
				
			} else {
				String st8 = getResources().getString(R.string.group_is_blocked);
				final String st9 = getResources().getString(R.string.group_of_shielding);
				EMLog.d(TAG, "change to block group msg");
				if (progressDialog == null) {
                    progressDialog = new ProgressDialog(GroupDetailsActivity.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                }
				progressDialog.setMessage(st8);
				progressDialog.show();
				new Thread(new Runnable() {
                    public void run() {
                        try {
                            EMGroupManager.getInstance().blockGroupMessage(groupId);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
                                    iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
                                    progressDialog.dismiss();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), st9, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        
                    }
                }).start();
			}
			break;

		case R.id.clear_all_history: // 清空聊天记录
			String st9 = getResources().getString(R.string.sure_to_empty_this);
			Intent intent = new Intent(GroupDetailsActivity.this, AlertDialog.class);
			intent.putExtra("cancel", true);
			intent.putExtra("titleIsCancel", true);
			intent.putExtra("msg", st9);
			startActivityForResult(intent, REQUEST_CODE_CLEAR_ALL_HISTORY);
			break;

		case R.id.rl_blacklist: // 黑名单列表
			startActivity(new Intent(GroupDetailsActivity.this, GroupBlacklistActivity.class).putExtra("groupId", groupId));
			break;

		case R.id.rl_change_group_name:
			startActivityForResult(new Intent(this, EditActivity.class).putExtra("data", group.getGroupName()), REQUEST_CODE_EDIT_GROUPNAME);
			break;

		default:
			break;
		}

	}

	/**
	 * 群组成员gridadapter
	 * 
	 * @author admin_new
	 * 
	 */
	private class GroupMembersAdapter extends BaseAdapter {

		Context context;
		private ArrayList<UserBean> groupMembers;
		public boolean isInDeleteMode;
		ImageLoader imageLoader;


		public GroupMembersAdapter(Context context,
                ArrayList<UserBean> groupMembers) {
            super();
            this.context = context;
            this.groupMembers = groupMembers;
            isInDeleteMode = false;
            imageLoader = imageLoader.getInstance(context);
        }

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
		    ViewHolder holder = null;
			if (convertView == null) {
			    holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.grid, null);
				holder.ivAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
				holder.textView = (TextView) convertView.findViewById(R.id.tv_name);
				holder.badgeDeleteView = (ImageView) convertView.findViewById(R.id.badge_delete);
				convertView.setTag(holder);
			}else{
			    holder = (ViewHolder) convertView.getTag();
			}
			final LinearLayout button = (LinearLayout) convertView.findViewById(R.id.button_avatar);
			String currentUserName=SuperWeChatApplication.getInstance().getUserName();
			// 最后一个item，减人按钮
			if (position == getCount() - 1) {
			    holder.textView.setText("");
				// 设置成删除按钮
			    holder.ivAvatar.setImageResource(R.drawable.smiley_minus_btn);
//				button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.smiley_minus_btn, 0, 0);
				// 如果不是创建者或者没有相应权限，不提供加减人按钮
				if (!group.getOwner().equals(currentUserName)) {
					// if current user is not group admin, hide add/remove btn
					convertView.setVisibility(View.INVISIBLE);
				} else { // 显示删除按钮
					if (isInDeleteMode) {
						// 正处于删除模式下，隐藏删除按钮
						convertView.setVisibility(View.INVISIBLE);
					} else {
						// 正常模式
						convertView.setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
					}
					final String st10 = getResources().getString(R.string.The_delete_button_is_clicked);
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							EMLog.d(TAG, st10);
							isInDeleteMode = true;
							notifyDataSetChanged();
						}
					});
				}
			} else if (position == getCount() - 2) { // 添加群组成员按钮
			    holder.textView.setText("");
			    holder.ivAvatar.setImageResource(R.drawable.smiley_add_btn);
//				button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.smiley_add_btn, 0, 0);
				// 如果不是创建者或者没有相应权限
				if (!group.isAllowInvites() && !group.getOwner().equals(currentUserName)) {
					// if current user is not group admin, hide add/remove btn
					convertView.setVisibility(View.INVISIBLE);
				} else {
					// 正处于删除模式下,隐藏添加按钮
					if (isInDeleteMode) {
						convertView.setVisibility(View.INVISIBLE);
					} else {
						convertView.setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
					}
					final String st11 = getResources().getString(R.string.Add_a_button_was_clicked);
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							EMLog.d(TAG, st11);
							// 进入选人页面
							startActivityForResult(
									(new Intent(GroupDetailsActivity.this, 
									GroupPickContactsActivity.class)
									.putExtra("groupId", groupId)),REQUEST_CODE_ADD_USER);
						}
					});
				}
			} else { // 普通item，显示群组成员
				final UserBean user = getItem(position);
				convertView.setVisibility(View.VISIBLE);
				button.setVisibility(View.VISIBLE);
				holder.textView.setText(user.getUserName());
				//获取群成员
				//设置该成员头像下载地址
				String path= I.DOWNLOAD_AVATAR_URL+user.getAvatar();
				holder.ivAvatar.setTag(path);
				//下载头像
	            Bitmap avatar = imageLoader.displayImage(path, user.getUserName()+".jpg", 80, 80, new ImageLoader.OnImageLoadListener() {
	                @Override
	                public void onSuccess(String path, Bitmap bitmap) {
	                    ImageView iv=(ImageView) parent.findViewWithTag(path);
	                    if(iv!=null){
	                        iv.setImageBitmap(bitmap);
	                    }
	                }
	                @Override
	                public void error(String errorMsg) {
	                    // TODO Auto-generated method stub
	                    
	                }
	            });
	            if(avatar==null){
	                holder.ivAvatar.setImageResource(R.drawable.default_avatar);
	            }else{
	                holder.ivAvatar.setImageBitmap(avatar);
	            }
				
				if (isInDeleteMode) {
					// 如果是删除模式下，显示减人图标
					convertView.findViewById(R.id.badge_delete).setVisibility(View.VISIBLE);
				} else {
					convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
				}
				final String st12 = getResources().getString(R.string.not_delete_myself);
				final String st15 = getResources().getString(R.string.confirm_the_members);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isInDeleteMode) {
							// 如果是删除自己，return
							if (EMChatManager.getInstance().getCurrentUser().equals(user.getUserName())) {
								startActivity(new Intent(GroupDetailsActivity.this, AlertDialog.class).putExtra("msg", st12));
								return;
							}
							if (!NetUtils.hasNetwork(getApplicationContext())) {
								Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
								return;
							}
							EMLog.d("group", "remove user from group:" + user.getUserName());
							new DeleteGroupMemberTask(mContext, GroupMembersAdapter.this,
					            mGroup.getName(), user).execute();
//							deleteMembersFromGroup(userName);
						} else {
							// 正常情况下点击user，可以进入用户详情或者聊天页面等等
							// startActivity(new
							// Intent(GroupDetailsActivity.this,
							// ChatActivity.class).putExtra("userId",
							// user.getUsername()));

						}
					}
				});

				button.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
					    if(EMChatManager.getInstance().getCurrentUser().equals(user.getUserName()))
					        return true;
						if (group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
							Intent intent = new Intent(GroupDetailsActivity.this, AlertDialog.class);
							intent.putExtra("msg", st15);
							intent.putExtra("cancel", true);
							startActivityForResult(intent, REQUEST_CODE_ADD_TO_BALCKLIST);
							longClickUsername = user.getUserName();
						}
						return false;
					}
				});
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return groupMembers.size() + 2;
		}

        @Override
        public UserBean getItem(int position) {
            return groupMembers.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        /**
         * 将删除的群成员从内存中删除
         * @param user
         */
        public void remove(UserBean user) {
            groupMembers.remove(user);
            notifyDataSetChanged();
        }
	}

	protected void updateGroup() {
		new Thread(new Runnable() {
			public void run() {
				try {
					final EMGroup returnGroup = EMGroupManager.getInstance().getGroupFromServer(groupId);
					// 更新本地数据
					EMGroupManager.getInstance().createOrUpdateLocalGroup(returnGroup);

					runOnUiThread(new Runnable() {
						public void run() {
							((TextView) findViewById(R.id.group_name)).setText(group.getGroupName() + "(" + group.getAffiliationsCount()
									+ ")");
							loadingPB.setVisibility(View.INVISIBLE);
                            mAdapter.notifyDataSetChanged();
							if (EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())) {
								// 显示解散按钮
								exitBtn.setVisibility(View.GONE);
								deleteBtn.setVisibility(View.VISIBLE);
							} else {
								// 显示退出按钮
								exitBtn.setVisibility(View.VISIBLE);
								deleteBtn.setVisibility(View.GONE);
							}

							// update block
							EMLog.d(TAG, "group msg is blocked:" + group.getMsgBlocked());
							if (group.isMsgBlocked()) {
								iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
								iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
							} else {
								iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
								iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
							}
						}
					});

				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							loadingPB.setVisibility(View.INVISIBLE);
						}
					});
				}
			}
		}).start();
	}

	public void back(View view) {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}
	
	private static class ViewHolder{
	    ImageView ivAvatar;;
	    TextView textView;
	    ImageView badgeDeleteView;
	}
	
	class DownloadGroupTask extends AsyncTask<Void, Void, Boolean>{
	    Context context;
	    String groupId;
	    
        public DownloadGroupTask(Context context, String groupId) {
            super();
            this.context = context;
            this.groupId = groupId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<UserBean> groupMembers=NetUtil.downloadGroupMembers(groupId);
			if(mGroupMembers==null){
				initGroupMembers();
			}
            if(mGroupMembers!=null && groupMembers!=null){
                mGroupMembers.addAll(groupMembers);
                return true;
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                initAdapter();
            }
        }
	}

    private void initAdapter() {
        mAdapter=new GroupMembersAdapter(instance, mGroupMembers);
	    mgvGroupMembers.setAdapter(mAdapter);
	    updateGroup();
    }
    
    class AddGroupMembersTask extends AsyncTask<Void, Void, Boolean>{
        Context context;
        String[] newMembers;
        ProgressDialog dialog;
        String resultMsg;
        
        public AddGroupMembersTask(Context context, String[] newMembers) {
            super();
            this.context = context;
            this.newMembers = newMembers;
			 Log.e(TAG,"GroupDetailsActivity.addGroupMembersTask.newMembers="+newMembers.toString());
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
			dialog.setMessage(getResources().getString(R.string.being_added));
			dialog.setCanceledOnTouchOutside(false);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<UserBean> contactList = SuperWeChatApplication.getInstance().getContactList();
			Log.e(TAG,"GroupDetailsActivity.addGroupMembersTask.mGroupMembers="+mGroupMembers);
			Log.e(TAG,"GroupDetailsActivity.addGroupMembersTask.contactList="+contactList);
            for(int i=0;i<newMembers.length;i++){
                UserBean user = new UserBean(newMembers[i]);
                int id=contactList.indexOf(user);
                if(id>=0){
                    mGroupMembers.add(user);
                }
            }
			Log.e(TAG,"GroupDetailsActivity.addGroupMembersTask.mGroupMembers="+mGroupMembers);
            boolean isSuccess = false;
            try {
				Log.e(TAG,"GroupDetailsActivity.addGroupMembersTask.groupId="+groupId+",group="+group+",mGroup="+mGroup+",newMembers="+newMembers);
                if(EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())){
                    EMGroupManager.getInstance().addUsersToGroup(groupId, newMembers);
                    isSuccess=NetUtil.addGroupMembers(mGroup.getName(), newMembers);
                }else{
                    EMGroupManager.getInstance().inviteUser(groupId, newMembers, null);
                }
                resultMsg=group.getName()+"("+group.getAffiliationsCount()+st;
            } catch (Exception e) {
                resultMsg=getResources().getString(R.string.Modify_the_group_name_successful)+e.getMessage();
            }
			Log.e(TAG,"GroupDetailsActivity.addGroupMembersTask.isSuccess="+isSuccess+",resultMsg="+resultMsg);
            return isSuccess;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if(result){
                mAdapter.notifyDataSetChanged();
                ((TextView)findViewById(R.id.group_name)).setText(resultMsg);
            }else{
                Utils.showToast(context, resultMsg, Toast.LENGTH_LONG);
            }
        }
    }
    
    class ExitGroupTask extends AsyncTask<Void, Void, Boolean>{
        Context context;
        GroupBean group;
        String userName;
        ProgressDialog dialog;
        String resultMsg;
        
        public ExitGroupTask(Context context, GroupBean group, String userName) {
            super();
            this.context = context;
            this.group = group;
            this.userName = userName;
        }
        
        @Override
        protected void onPreExecute() {
            dialog=new ProgressDialog(context);
            dialog.setMessage(getResources().getString(R.string.is_quit_the_group_chat));
            dialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                EMGroupManager.getInstance().exitFromGroup(group.getGroupId());
                boolean exitGroup=NetUtil.deleteGroupMember(group.getName(), userName);
                return exitGroup;
            } catch (Exception e) {
                resultMsg=getResources().getString(R.string.Exit_the_group_chat_failure)+" " +e.getMessage();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if(result){
                setResult(RESULT_OK);
                Intent intent=new Intent("exit_group");
                intent.putExtra("group", group);
                sendBroadcast(intent);
                finish();
                if(ChatActivity.activityInstance!=null){
                    ChatActivity.activityInstance.finish();
                }
            }else{
                Utils.showToast(mContext, resultMsg, Toast.LENGTH_LONG);
            }
        }
        
    }
    class DeleteGroupMemberTask extends AsyncTask<Void, Void, Boolean>{
        Context context;
	    GroupMembersAdapter adapter;
	    String groupName;//群名
	    UserBean user;//被删除的群成员
	    ProgressDialog dialog;
	    String resultMsg;
	    
        
        
        public DeleteGroupMemberTask(Context context,
                GroupMembersAdapter adapter, String groupName, UserBean user) {
            super();
            this.context = context;
            this.adapter = adapter;
            this.groupName = groupName;
            this.user = user;
        }
        @Override
        protected void onPreExecute() {
            final String hint=context.getResources().getString(R.string.Are_removed);
            dialog=new ProgressDialog(mContext);
            dialog.setMessage(hint);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                EMGroupManager.getInstance().removeUserFromGroup(groupId, user.getUserName());
                boolean exitGroup=NetUtil.deleteGroupMember(mGroup.getName(), user.getUserName());
                adapter.isInDeleteMode = false;
                resultMsg=group.getName() + "("+ group.getAffiliationsCount() + st;
                return exitGroup;
            } catch (final Exception e) {
                resultMsg = getResources().getString(R.string.Delete_failed);
            }
	        return false;
	    }
	    
	    @Override
	    protected void onPostExecute(Boolean result) {
	        dialog.dismiss();
	        Toast.makeText(getApplicationContext(), resultMsg, 1).show();
	        if(result){
	            mAdapter.remove(user);
	        }
	    }
	}
	
	/**解散群*/
	class DeleteGroupTask extends AsyncTask<Void, Void, Boolean>{
	    
	    Context context;
	    GroupBean group;
	    ProgressDialog dialog;
	    String resultMsg;
	    
	    public DeleteGroupTask(Context context, GroupBean group) {
            super();
            this.context = context;
            this.group = group;
        }

        @Override
	    protected void onPreExecute() {
	        String hint = getResources().getString(R.string.chatting_is_dissolution);
            dialog = new ProgressDialog(context);
            dialog.setMessage(hint);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
	    }
	    
	    @Override
	    protected Boolean doInBackground(Void... params) {
	        try {
                EMGroupManager.getInstance().exitAndDeleteGroup(group.getGroupId());
                boolean isDelete = NetUtil.deleteGroup(group.getName());
                return isDelete;
            } catch (final Exception e) {
                resultMsg= getResources().getString(
                    R.string.is_modify_the_group_name) + e.getMessage();
            }
	        return false;
	    }
	    
	    @Override
	    protected void onPostExecute(Boolean result) {
	        dialog.dismiss();
	        if(result){
	            //向GroupsActivity发送解散群的广播
                Intent intent=new Intent("delete_group");
                intent.putExtra("group", group);
                sendBroadcast(intent);
	            setResult(RESULT_OK);
                finish();
                if(ChatActivity.activityInstance != null)
                    ChatActivity.activityInstance.finish();
	        }else{
	            Utils.showToast(getApplicationContext(), resultMsg, 1);
	        }
	    }
	}

	/** 修改群名称*/
	class UpdateGroupNameTask extends AsyncTask<Void, Void, Boolean>{
	    
	    GroupDetailsActivity context;
	    GroupBean group;
	    String newGroupName;
	    /**受影响的数量*/
	    int affiliationsCount;
	    ProgressDialog dialog;
        String resultMsg;
        
        public UpdateGroupNameTask(GroupDetailsActivity context, GroupBean group,
                String newGroupName,int affiliationsCount) {
            super();
            this.context = context;
            this.group = group;
            this.newGroupName = newGroupName;
            this.affiliationsCount=affiliationsCount;
        }

        @Override
	    protected void onPreExecute() {
            String hint = context.getResources().getString(R.string.is_modify_the_group_name);
            dialog = new ProgressDialog(context);
            dialog.setMessage(hint);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) {
	        try {
                EMGroupManager.getInstance().changeGroupName(group.getGroupId(), newGroupName);
                boolean isUpdate = NetUtil.updateGroupName(group.getName(), newGroupName);
                if(isUpdate){
                    resultMsg = context.getResources().getString(R.string.Modify_the_group_name_successful);
                    //向GroupsActivity发送群名修改的广播
                    sendBroadcast(new Intent("update_group_name")
                        .putExtra("groupName", newGroupName));
                }else{
                    resultMsg="修改群名失败";
                }
                return isUpdate;
            } catch (EaseMobException e) {
                e.printStackTrace();
                resultMsg = getResources().getString(R.string.change_the_group_name_failed_please);
            }
	        
	        return false;
	    }
	    
	    @Override
	    protected void onPostExecute(Boolean result) {
	        dialog.dismiss();
	        Utils.showToast(context, resultMsg, 1);
	        if(result){
	            ((TextView) context.findViewById(R.id.group_name))
	                .setText(newGroupName + "(" + affiliationsCount
	                + getResources().getString(R.string.people));
	        }
	    }
	}
}
