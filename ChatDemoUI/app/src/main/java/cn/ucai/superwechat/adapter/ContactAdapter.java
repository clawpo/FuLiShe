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
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.easemob.util.EMLog;

import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.domain.User;
import cn.ucai.superwechat.utils.ImageLoader;

/**
 * 简单的好友Adapter实现
 *
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer{
    private static final String TAG = "ContactAdapter";
	List<String> list;
	List<User> userList;
	List<User> copyUserList;
	private LayoutInflater layoutInflater;
	private SparseIntArray positionOfSection;
	private SparseIntArray sectionOfPosition;
	private int res;
	private MyFilter myFilter;
    private boolean notiyfyByFilter;

    private Context mContext;
    //联系人集合
    private ArrayList<UserBean> mContactList;
    //加载头像的任务类
    private ImageLoader mImageLoader;

    public ContactAdapter(Context context,int resource,ArrayList<UserBean>contactList){
        mContext = context;
        mContactList = contactList;
        mImageLoader = ImageLoader.getInstance(context);
        this.res = resource;
        layoutInflater = LayoutInflater.from(context);
        Log.e("main","ContactAdapter----.mContactList.size="+mContactList.size());
    }
	
	private static class ViewHolder {
	    ImageView avatar;
	    TextView unreadMsgView;
	    TextView nameTextview;
	    TextView tvHeader;
    }
	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
	    ViewHolder holder;
 		if(convertView == null){
 		    holder = new ViewHolder();
			convertView = layoutInflater.inflate(res, null);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			holder.unreadMsgView = (TextView) convertView.findViewById(R.id.unread_msg_number);
			holder.nameTextview = (TextView) convertView.findViewById(R.id.name);
			holder.tvHeader = (TextView) convertView.findViewById(R.id.header);
			convertView.setTag(holder);
		}else{
		    holder = (ViewHolder) convertView.getTag();
		}
		
		UserBean user = getItem(position);
		if(user == null)
			Log.d("ContactAdapter", position + "");
		//设置nick，demo里不涉及到完整user，用username代替nick显示
		String username = user.getUserName();
		String header = user.getHeader();
		if (position == 0 || header != null && !header.equals(getItem(position - 1).getHeader())) {
			if (TextUtils.isEmpty(header)) {
			    holder.tvHeader.setVisibility(View.GONE);
			} else {
			    holder.tvHeader.setVisibility(View.VISIBLE);
			    holder.tvHeader.setText(header);
			}
		} else {
		    holder.tvHeader.setVisibility(View.GONE);
		}
		//显示申请与通知item
		if(username.equals(Constant.NEW_FRIENDS_USERNAME)){
		    holder.nameTextview.setText(user.getNick());
		    holder.avatar.setImageResource(R.drawable.new_friends_icon);
			Log.e("main","user.getUnreadMsgCount()="+user.getUnreadMsgCount());
            int c=((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME)
                    .getUnreadMsgCount();
            Log.e("main","user.c="+c);
			if(user.getUnreadMsgCount() > 0|| c >0){
			    holder.unreadMsgView.setVisibility(View.VISIBLE);
//			    holder.unreadMsgView.setText(user.getUnreadMsgCount()+"");
			}else{
			    holder.unreadMsgView.setVisibility(View.INVISIBLE);
			}
		}else if(username.equals(Constant.GROUP_USERNAME)){
			//群聊item
		    holder.nameTextview.setText(user.getNick());
		    holder.avatar.setImageResource(R.drawable.groups_icon);
		}else if(username.equals(Constant.CHAT_ROOM)){
            //群聊item
            holder.nameTextview.setText(user.getNick());
            holder.avatar.setImageResource(R.drawable.groups_icon);
		}else if(username.equals(Constant.CHAT_ROBOT)){
			//Robot item
			holder.nameTextview.setText(user.getNick());
			holder.avatar.setImageResource(R.drawable.groups_icon);
		}else{
		    holder.nameTextview.setText(user.getNick());
		    //设置用户头像
			//UserUtils.setUserAvatar(getContext(), username, holder.avatar);
			if(holder.unreadMsgView != null){
			    holder.unreadMsgView.setVisibility(View.INVISIBLE);
			}
			String path = I.DOWNLOAD_AVATAR_URL+user.getAvatar();
			holder.avatar.setTag(path);
			Bitmap avatar = mImageLoader.displayImage(path, user.getUserName()+".jpg", 80, 80, new ImageLoader.OnImageLoadListener() {
                
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    ImageView iv = (ImageView)parent.findViewWithTag(path);
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
			    holder.avatar.setImageResource(R.drawable.default_avatar);
			}else{
			    holder.avatar.setImageBitmap(avatar);
			}
		}
		
		return convertView;
	}
	
	@Override
	public UserBean getItem(int position) {
		return mContactList.get(position);
	}
	
	@Override
	public int getCount() {
	    int count = mContactList==null?0:mContactList.size();
//	    Log.e(TAG, "ContactAdapter---mContactList.size1="+count);
//        Log.e(TAG, "ContactAdapter---mContactList.size2="+ SuperWeChatApplication.getInstance().getContactList().size());
		return count;
	}

	public int getPositionForSection(int section) {
		return positionOfSection.get(section);
	}

	public int getSectionForPosition(int position) {
		return sectionOfPosition.get(position);
	}
	
	@Override
	public Object[] getSections() {
		positionOfSection = new SparseIntArray();
		sectionOfPosition = new SparseIntArray();
		int count = getCount();
		list = new ArrayList<String>();
		list.add(mContext.getString(R.string.search_header));
		positionOfSection.put(0, 0);
		sectionOfPosition.put(0, 0);
		for (int i = 1; i < count; i++) {

			String letter = getItem(i).getHeader();
			Log.d(TAG, "contactadapter getsection getHeader:" + letter + " name:" + getItem(i).getUserName());
			int section = list.size() - 1;
			if (list.get(section) != null && !list.get(section).equals(letter)) {
				list.add(letter);
				section++;
				positionOfSection.put(section, i);
			}
			sectionOfPosition.put(i, section);
		}
		return list.toArray(new String[list.size()]);
	}
	
	private class  MyFilter extends Filter{
        List<User> mOriginalList = null;
		
		public MyFilter(List<User> myList) {
			this.mOriginalList = myList;
		}

		@Override
		protected synchronized FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if(mOriginalList==null){
			    mOriginalList = new ArrayList<User>();
			}
			Log.d(TAG, "contacts original size: " + mOriginalList.size());
			Log.d(TAG, "contacts copy size: " + copyUserList.size());
			
			if(prefix==null || prefix.length()==0){
				results.values = copyUserList;
				results.count = copyUserList.size();
			}else{
				String prefixString = prefix.toString();
				final int count = mOriginalList.size();
				final ArrayList<User> newValues = new ArrayList<User>();
				for(int i=0;i<count;i++){
					final User user = mOriginalList.get(i);
					String username = user.getUsername();
					
					if(username.startsWith(prefixString)){
						newValues.add(user);
					}
					else{
						 final String[] words = username.split(" ");
	                     final int wordCount = words.length;
	
	                     // Start at index 0, in case valueText starts with space(s)
	                     for (int k = 0; k < wordCount; k++) {
	                         if (words[k].startsWith(prefixString)) {
	                             newValues.add(user);
	                             break;
	                         }
	                     }
					}
				}
				results.values=newValues;
				results.count=newValues.size();
			}
			Log.d(TAG, "contacts filter results size: " + results.count);
			return results;
		}

		@Override
		protected synchronized void publishResults(CharSequence constraint,
				FilterResults results) {
			userList.clear();
			userList.addAll((List<User>)results.values);
			Log.d(TAG, "publish contacts filter results size: " + results.count);
			if (results.count > 0) {
			    notiyfyByFilter = true;
				notifyDataSetChanged();
				notiyfyByFilter = false;
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
	
	
	@Override
	public void notifyDataSetChanged() {
	    super.notifyDataSetChanged();
	    if(!notiyfyByFilter){
	        if(copyUserList!=null){
	            copyUserList.clear();
	            copyUserList.addAll(userList);
	        }
	    }
	}
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public void remove(UserBean user){
        if(mContactList!=null&&!mContactList.isEmpty()){
            mContactList.remove(user);
            notifyDataSetChanged();
        }
    }
	

}
