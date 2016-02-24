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

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.utils.ImageLoader;

public class GroupAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private String newGroup;
	private String addPublicGroup;
	
	Context mContext;
    /** 异步下载头像*/
    private ImageLoader mImageLoader;
    //登陆用户所属群的成员的集合
    ArrayList<GroupBean> groupList;
    
	public GroupAdapter(Context context,ArrayList<GroupBean> groupList) {
		this.inflater = LayoutInflater.from(context);
		newGroup = context.getResources().getString(R.string.The_new_group_chat);
		addPublicGroup = context.getResources().getString(R.string.add_public_group_chat);
		
		mContext = context;
		mImageLoader = ImageLoader.getInstance(mContext);
		this.groupList = groupList;
	}
	/** 
	 * 添加新建的群
	 * @param group
	 */
	public void addItem(GroupBean group){
	    if(group!=null){
	        groupList.add(group);
	        notifyDataSetChanged();
	    }
	}

    public void AddItems(ArrayList<GroupBean> groupList){
        groupList.addAll(groupList);
        notifyDataSetChanged();
    }
	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else if (position == 2) {
			return 2;
		} else {
			return 3;
		}
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		if (getItemViewType(position) == 0) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.search_bar_with_padding, null);
			}
			final EditText query = (EditText) convertView.findViewById(R.id.query);
			final ImageButton clearSearch = (ImageButton) convertView.findViewById(R.id.search_clear);
			query.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
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
				}
			});
		} else if (getItemViewType(position) == 1) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_add_group, null);
			}
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.create_group);
			((TextView) convertView.findViewById(R.id.name)).setText(newGroup);
		} else if (getItemViewType(position) == 2) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_add_group, null);
			}
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.add_public_group);
			((TextView) convertView.findViewById(R.id.name)).setText(addPublicGroup);
			((TextView) convertView.findViewById(R.id.header)).setVisibility(View.VISIBLE);

		} else {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_group, null);
			}
			GroupBean group = getItem(position);
			((TextView) convertView.findViewById(R.id.name)).setText(group.getName());
			ImageView ivGroupAvatar = (ImageView) convertView.findViewById(R.id.avatar);
			String path = I.DOWNLOAD_AVATAR_URL+group.getAvatar();
			ivGroupAvatar.setTag(path);
			Bitmap avatar = mImageLoader.displayImage(path, group.getName()+".jpg", 80, 80, new ImageLoader.OnImageLoadListener() {
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    ImageView iv = (ImageView) parent.findViewWithTag(path);
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
			    ivGroupAvatar.setImageResource(R.drawable.default_avatar);
			}else{
			    ivGroupAvatar.setImageBitmap(avatar);
			}

		}

		return convertView;
	}

	public ArrayList<GroupBean> getGroups(){
	    return groupList; 
	}
	
	
	@Override
	public int getCount() {
		return groupList.size() + 3;
	}

    @Override
    public GroupBean getItem(int position) {
        if(position>=3){
            return groupList.get(position-3);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void remove(GroupBean group) {
        boolean isRemove = groupList.remove(group);
        if(isRemove){
            notifyDataSetChanged();
        }
    }

}