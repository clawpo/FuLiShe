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
package cn.ucai.superwechat.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.db.InviteMessgeDao;
import cn.ucai.superwechat.domain.InviteMessage;
import cn.ucai.superwechat.utils.ImageLoader;
import cn.ucai.superwechat.utils.NetUtil;

public class NewFriendsMsgAdapter extends ArrayAdapter<InviteMessage> {

	private InviteMessgeDao messgeDao;
	private Context context;
	ImageLoader mImageLoader;
	public NewFriendsMsgAdapter(Context context, int textViewResourceId, List<InviteMessage> objects) {
		super(context, textViewResourceId, objects);
		Log.e("main","NewFriendsMsgAdapter.list.size="+objects.size());
		this.context = context;
		messgeDao = new InviteMessgeDao(context);
		mImageLoader= ImageLoader.getInstance(context);
		this.context=context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.row_invite_msg, null);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			holder.reason = (TextView) convertView.findViewById(R.id.message);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.status = (Button) convertView.findViewById(R.id.user_state);
			holder.groupContainer = (LinearLayout) convertView.findViewById(R.id.ll_group);
			holder.groupname = (TextView) convertView.findViewById(R.id.tv_groupName);
			// holder.time = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String str1 = context.getResources().getString(R.string.Has_agreed_to_your_friend_request);
		String str2 = context.getResources().getString(R.string.agree);
		
		String str3 = context.getResources().getString(R.string.Request_to_add_you_as_a_friend);
		String str4 = context.getResources().getString(R.string.Apply_to_the_group_of);
		String str5 = context.getResources().getString(R.string.Has_agreed_to);
		String str6 = context.getResources().getString(R.string.Has_refused_to);
		final InviteMessage msg = getItem(position);
		if (msg != null) {
			if(msg.getGroupId() != null){ // 显示群聊提示
				holder.groupContainer.setVisibility(View.VISIBLE);
				holder.groupname.setText(msg.getGroupName());
			} else{
				holder.groupContainer.setVisibility(View.GONE);
			}
			
			holder.reason.setText(msg.getReason());
			holder.name.setText(msg.getFrom());
			// holder.time.setText(DateUtils.getTimestampString(new
			// Date(msg.getTime())));
			if (msg.getStatus() == InviteMessage.InviteMesageStatus.BEAGREED) {
				holder.status.setVisibility(View.INVISIBLE);
				holder.reason.setText(str1);
			} else if (msg.getStatus() == InviteMessage.InviteMesageStatus.BEINVITEED || msg.getStatus() == InviteMessage.InviteMesageStatus.BEAPPLYED) {
				holder.status.setVisibility(View.VISIBLE);
				holder.status.setEnabled(true);
				holder.status.setBackgroundResource(android.R.drawable.btn_default);
				holder.status.setText(str2);
				if(msg.getStatus() == InviteMessage.InviteMesageStatus.BEINVITEED){
					if (msg.getReason() == null) {
						// 如果没写理由
						holder.reason.setText(str3);
					}
				}else{ //入群申请
					if (TextUtils.isEmpty(msg.getReason())) {
						holder.reason.setText(str4 + msg.getGroupName());
					}
				}
				// 设置点击事件
				holder.status.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 同意别人发的好友请求
						acceptInvitation(holder.status, msg);
					}
				});
			} else if (msg.getStatus() == InviteMessage.InviteMesageStatus.AGREED) {
				holder.status.setText(str5);
				holder.status.setBackgroundDrawable(null);
				holder.status.setEnabled(false);
			} else if(msg.getStatus() == InviteMessage.InviteMesageStatus.REFUSED){
				holder.status.setText(str6);
				holder.status.setBackgroundDrawable(null);
				holder.status.setEnabled(false);
			}

			// 设置用户头像
			String userName = msg.getFrom();
			UserBean user=getUserFromUserName(userName,holder.avatar);
			if(user!=null){
    			showAvatar(holder.avatar, user);
			}
		}

		return convertView;
	}

	/**
	 * 下载显示头像
	 * @param ivAvatar
	 * @param user
	 */
    private void showAvatar(final ImageView ivAvatar, UserBean user) {
        String path= I.DOWNLOAD_AVATAR_URL+user.getAvatar();
        Bitmap avatar = mImageLoader.displayImage(path, user.getUserName()+".jpg", 80, 80, new ImageLoader.OnImageLoadListener() {
            @Override
            public void onSuccess(String path, Bitmap bitmap) {
                ivAvatar.setImageBitmap(bitmap);
            }
            
            @Override
            public void error(String errorMsg) {
                // TODO Auto-generated method stub
                
            }
        });
        if(avatar==null){
            ivAvatar.setImageResource(R.drawable.group_icon);
        }else{
            ivAvatar.setImageBitmap(avatar);
        }
    }

    /**
     * 从联系人集合中获取userName用户,若没有该用户，则从应用服务器下载该用户
     * @param userName
     * @param ivAvatar
     * @return
     */
	private UserBean getUserFromUserName(String userName,ImageView ivAvatar) {
        ArrayList<UserBean> contactList = SuperWeChatApplication.getInstance().getContactList();
        UserBean user=new UserBean(userName);
        int id = contactList.indexOf(user);
        if(id>=0){
            user=contactList.get(id);
        }else{//若没有该用户，则从应用服务器下载该用户
            new DownloadUserTask(context, user.getUserName(), ivAvatar).execute();
        }
        return user;
    }

    /**
	 * 同意好友请求或者群申请
	 * 
	 * @param button
	 * @param msg
	 */
	private void acceptInvitation(final Button button, final InviteMessage msg) {
		final ProgressDialog pd = new ProgressDialog(context);
		String str1 = context.getResources().getString(R.string.Are_agree_with);
		final String str2 = context.getResources().getString(R.string.Has_agreed_to);
		final String str3 = context.getResources().getString(R.string.Agree_with_failure);
		pd.setMessage(str1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();

		new Thread(new Runnable() {
			public void run() {
				// 调用sdk的同意方法
				try {
					if(msg.getGroupId() == null) //同意好友请求
						EMChatManager.getInstance().acceptInvitation(msg.getFrom());
					else //同意加群申请
					    EMGroupManager.getInstance().acceptApplication(msg.getFrom(), msg.getGroupId());
					((Activity) context).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							pd.dismiss();
							button.setText(str2);
							msg.setStatus(InviteMessage.InviteMesageStatus.AGREED);
							// 更新db
							ContentValues values = new ContentValues();
							values.put(InviteMessgeDao.COLUMN_NAME_STATUS, msg.getStatus().ordinal());
							messgeDao.updateMessage(msg.getId(), values);
							button.setBackgroundDrawable(null);
							button.setEnabled(false);

						}
					});
				} catch (final Exception e) {
					((Activity) context).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							pd.dismiss();
							Toast.makeText(context, str3 + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});

				}
			}
		}).start();
	}

	private static class ViewHolder {
		ImageView avatar;
		TextView name;
		TextView reason;
		Button status;
		LinearLayout groupContainer;
		TextView groupname;
		// TextView time;
	}

	/**下载申请用户,然后显示该用户头像*/
	class DownloadUserTask extends AsyncTask<Void, Void, UserBean> {
	    Context context;
	    String userName;
	    ImageView ivAvatar;
	    
        public DownloadUserTask(Context context, String userName,
                ImageView ivAvatar) {
            this.context = context;
            this.userName = userName;
            this.ivAvatar = ivAvatar;
        }

        @Override
	    protected UserBean doInBackground(Void... params) {
	        UserBean user = NetUtil.findUserByUserName(userName);
	        return user;
	    }
	    
        @Override
        protected void onPostExecute(UserBean user) {
            if(user!=null){
                showAvatar(ivAvatar, user);
            }
        }
	}
}
