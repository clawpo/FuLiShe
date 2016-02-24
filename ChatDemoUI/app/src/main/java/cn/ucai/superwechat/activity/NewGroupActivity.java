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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.NetUtil;
import cn.ucai.superwechat.utils.Utils;

public class NewGroupActivity extends BaseActivity {
	private EditText metGroupName;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox mchkPublic;
	private CheckBox mchkMemberInviter;
	private LinearLayout openInviteContainer;
	static final int ACTION_CREATE_GROUP = 100;
	NewGroupActivity mContext;
	OnSetAvatarListener mOnSetAvatarListener;
	ImageView mivAvatar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.activity_new_group);
		initView();
		setListener();
	}

    private void initView() {
        mivAvatar=(ImageView) findViewById(R.id.iv_avatar);
        metGroupName = (EditText) findViewById(R.id.edit_group_name);
        introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
        mchkPublic = (CheckBox) findViewById(R.id.chkPublic);
        mchkMemberInviter = (CheckBox) findViewById(R.id.chkMemberInviter);
        openInviteContainer = (LinearLayout) findViewById(R.id.ll_open_invite);
    }
	private void setListener() {
	    setOnCheckchangedListener();
	    setSaveGroupClickListener();
	    setGroupIconClickListener();
    }


    private void setGroupIconClickListener() {
        findViewById(R.id.layout_group_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSetAvatarListener=new OnSetAvatarListener(NewGroupActivity.this, R.id.layout_new_group);
            }
        });
    }

    private void setSaveGroupClickListener() {
        findViewById(R.id.btnSaveGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = getResources().getString(R.string.Group_name_cannot_be_empty);
                String groupName = metGroupName.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    metGroupName.setError(str);
                    metGroupName.requestFocus();
                    return;
                }
                startActivityForResult(new Intent(mContext,GroupPickContactsActivity.class).putExtra("groupName", groupName), ACTION_CREATE_GROUP);
            }
        });
    }

    private void setOnCheckchangedListener() {
        mchkPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    openInviteContainer.setVisibility(View.INVISIBLE);
                }else{
                    openInviteContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

	/**
	 * @param v
	 */
	public void save(View v) {
		String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
		String name = metGroupName.getText().toString();
		if (TextUtils.isEmpty(name)) {
			Intent intent = new Intent(this, AlertDialog.class);
			intent.putExtra("msg", str6);
			startActivity(intent);
		} else {
			// 进通讯录选人
			startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), 0);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(resultCode!=RESULT_OK){
	        return;
	    }
	    switch (requestCode) {
        case ACTION_CREATE_GROUP:
            new CreateGroupTask(mContext,data,requestCode).execute();
            break;

        default:
            String groupName=metGroupName.getText().toString();
            mOnSetAvatarListener.setAvatar(requestCode, data, mivAvatar, "group_icon", groupName);
            break;
        }
	}

	public void back(View view) {
		finish();
	}

	class CreateGroupTask extends AsyncTask<Void, Void, String> {
		Context context;
		ProgressDialog dialog;
		Intent intent;
		int requestCode;
		GroupBean group;

		public CreateGroupTask(Context context, Intent intent, int requestCode) {
			super();
			this.context = context;
			this.intent = intent;
			this.requestCode = requestCode;
			dialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			String str1 = getResources().getString(R.string.Is_to_create_a_group_chat);
			dialog.setMessage(str1);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}
		@Override
		protected String doInBackground(Void... params) {
			String groupName = metGroupName.getText().toString().trim();
			String desc = introductionEditText.getText().toString();
			String[] members = intent.getStringArrayExtra("newmembers");
			EMGroup emGroup=null;
			group=NetUtil.findGroupByName(groupName);
			if(group!=null){
				return getResources().getString(R.string.Group_name_existed);
			}
			try {
				if(mchkPublic.isChecked()){
					emGroup = EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
				}else{
					emGroup=EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members,mchkMemberInviter.isChecked(),200);
				}
				boolean isPublic=mchkPublic.isChecked();
				boolean isExam=!mchkMemberInviter.isChecked();
				String userName= SuperWeChatApplication.getInstance().getUserName();
				StringBuffer sbMemberBuffer=new StringBuffer();
				for(String member:members){
					sbMemberBuffer.append(member).append(",");
				}
				sbMemberBuffer.append(userName);
				String groupId=emGroup.getGroupId();
				group=new GroupBean(groupId,groupName,desc,userName,isPublic,isExam,sbMemberBuffer.toString());
				boolean isSuccess=NetUtil.createGroup(group);
				if(isSuccess){
					NetUtil.uploadAvatar(mContext, "group_icon", groupName);
					group.setAvatar("group_icon/"+groupName+".jpg");
				}
				return getResources().getString(R.string.Create_groups_Success);
			} catch (final EaseMobException e) {
				String str2=getResources().getString(R.string.Failed_to_create_groups);
				return str2+e.getLocalizedMessage();
			} catch(Exception e){
				e.printStackTrace();
				return e.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			String strExisted = getResources().getString(R.string.Group_name_existed);
			String strSuccess = getResources().getString(R.string.Create_groups_Success);
			String strFailed = getResources().getString(R.string.Create_groups_Failed);
			if(strExisted.equals(result)){
				metGroupName.setError(strExisted);
				metGroupName.requestFocus();
			}else if(strFailed.equals(result)){
				Utils.showToast(context, result, Toast.LENGTH_SHORT);
			}else if(strSuccess.equals(result)){
				Intent intent = new Intent(mContext,GroupsActivity.class);
				intent.putExtra("group", group);
				setResult(RESULT_OK,intent);
				finish();
			}
		}

	}
}
