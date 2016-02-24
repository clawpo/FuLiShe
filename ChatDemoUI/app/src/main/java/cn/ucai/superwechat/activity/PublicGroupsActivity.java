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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.EMGroupInfo;

import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.utils.ImageLoader;
import cn.ucai.superwechat.utils.NetUtil;

public class PublicGroupsActivity extends BaseActivity {
    Context mContext;
    public static final int PAGE_SIZE = 20;
    public int mPageId = 0;
    
	private ProgressBar pb;
	private ListView mlvPublicGroup;
	private GroupsAdapter mAdapter;
	
	private List<EMGroupInfo> groupsList;
	private boolean isLoading;
	private boolean isFirstLoading = true;
	private boolean hasMoreData = true;
	private String cursor;
	private final int pagesize = 20;
    private LinearLayout footLoadingLayout;
    private ProgressBar footLoadingPB;
    private TextView footLoadingText;
    private Button searchBtn;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_public_groups);
		initView();
		
        //获取及显示数据
//        loadAndShowData();
        mContext=this;
        ArrayList<GroupBean> publicGroupList=SuperWeChatApplication.getInstance().getPublicGroupList();
        if(publicGroupList.size()==0){
            new DownloadPublicGroupTask(this, 0, PAGE_SIZE).execute();
        }else{
            mAdapter=new GroupsAdapter(this, publicGroupList);
            mlvPublicGroup.setAdapter(mAdapter);
        }
        
        setListener();
	}

    private void initView() {
        pb = (ProgressBar) findViewById(R.id.progressBar);
        mlvPublicGroup = (ListView) findViewById(R.id.list);
        groupsList = new ArrayList<EMGroupInfo>();
        searchBtn = (Button) findViewById(R.id.btn_search);
        
        View footView = getLayoutInflater().inflate(R.layout.listview_footer_view, null);
        footLoadingLayout = (LinearLayout) footView.findViewById(R.id.loading_layout);
        footLoadingPB = (ProgressBar)footView.findViewById(R.id.loading_bar);
        footLoadingText = (TextView) footView.findViewById(R.id.loading_text);
        mlvPublicGroup.addFooterView(footView, null, false);
        footLoadingLayout.setVisibility(View.GONE);
    }
	/**
     * 设置事件监听
     */
	private void setListener() {
        setListViewItemClickListener();
        setListViewScrollListener();
        setSearchClickListener();
    }

    /**
     * 搜索按钮单击事件监听
     */
    private void setSearchClickListener() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext,PublicGroupsSeachActivity.class));
            }
        });
    }

    /**
     * 设置下拉刷新事件监听
     */
    private void setListViewScrollListener() {
        mlvPublicGroup.setOnScrollListener(new OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
                    if(mlvPublicGroup.getCount() != 0){
                        int lasPos = view.getLastVisiblePosition();
                        if(hasMoreData && !isLoading && lasPos == mlvPublicGroup.getCount()-1){
//                            loadAndShowData();
                            mPageId++;
                            new DownloadPublicGroupTask(PublicGroupsActivity.this, mPageId, PAGE_SIZE).execute();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void setListViewItemClickListener() {
      //设置item点击事件
        mlvPublicGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupBean group = mAdapter.getItem(position);
                startActivity(new Intent(PublicGroupsActivity.this,
                        GroupSimpleDetailActivity.class).
                        putExtra("group", group));
            }
        });
    }


	
//	private void loadAndShowData(){
//	    new Thread(new Runnable() {
//
//            public void run() {
//                try {
//                    isLoading = true;
//                    final EMCursorResult<EMGroupInfo> result = EMGroupManager.getInstance().getPublicGroupsFromServer(pagesize, cursor);
//                    //获取group list
//                    final List<EMGroupInfo> returnGroups = result.getData();
//                    runOnUiThread(new Runnable() {
//
//                        public void run() {
//                            searchBtn.setVisibility(View.VISIBLE);
//                            groupsList.addAll(returnGroups);
//                            if(returnGroups.size() != 0){
//                                //获取cursor
//                                cursor = result.getCursor();
//                                if(returnGroups.size() == pagesize)
//                                    footLoadingLayout.setVisibility(View.VISIBLE);
//                            }
//                            if(isFirstLoading){
//                                pb.setVisibility(View.INVISIBLE);
//                                isFirstLoading = false;
//                                //设置adapter
//                                adapter = new GroupsAdapter(PublicGroupsActivity.this, 1, groupsList);
//                                listView.setAdapter(adapter);
//                            }else{
//                                if(returnGroups.size() < pagesize){
//                                    hasMoreData = false;
//                                    footLoadingLayout.setVisibility(View.VISIBLE);
//                                    footLoadingPB.setVisibility(View.GONE);
//                                    footLoadingText.setText("No more data");
//                                }
//                                adapter.notifyDataSetChanged();
//                            }
//                            isLoading = false;
//                        }
//                    });
//                } catch (EaseMobException e) {
//                    e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            isLoading = false;
//                            pb.setVisibility(View.INVISIBLE);
//                            footLoadingLayout.setVisibility(View.GONE);
//                            Toast.makeText(PublicGroupsActivity.this, "加载数据失败，请检查网络或稍后重试", 0).show();
//                        }
//                    });
//                }
//            }
//        }).start();
//	}
	/**
	 * adapter
	 *
	 */
	private class GroupsAdapter extends BaseAdapter {
	    ArrayList<GroupBean> groupList;
		private LayoutInflater inflater;
		ImageLoader imageLoader;

		public GroupsAdapter(Context context,ArrayList<GroupBean> groupList) {
			this.inflater = LayoutInflater.from(context);
			this.groupList = groupList;
			imageLoader = ImageLoader.getInstance(context);
		}

		@Override
		public View getView(int position,  View convertView, final ViewGroup parent) {
		    ViewHolder holder=null;
			if (convertView == null) {
			    holder=new ViewHolder();
				convertView = inflater.inflate(R.layout.row_group, null);
				holder.tvGroupName=(TextView) convertView.findViewById(R.id.name);
				holder.ivGroupLogo=(ImageView) convertView.findViewById(R.id.avatar);
				convertView.setTag(holder);
			}else{
			    holder=(ViewHolder) convertView.getTag();
			}
			GroupBean group =getItem(position);
			holder.tvGroupName.setText(group.getName());
			String path = I.DOWNLOAD_AVATAR_URL+group.getAvatar();
			Bitmap avatar = imageLoader.displayImage(path, group.getName()+".jpg", 80, 80, new ImageLoader.OnImageLoadListener() {
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    ImageView iv = (ImageView) parent.findViewWithTag(path);
                    if(iv!=null){
                        iv.setImageBitmap(bitmap);
                    }
                }
                @Override
                public void error(String errorMsg) {
                }
            });
			if(avatar==null){
			    holder.ivGroupLogo.setImageResource(R.drawable.group_icon);
			}else{
			    holder.ivGroupLogo.setImageBitmap(avatar);
			}

			return convertView;
		}

		class ViewHolder{
		    TextView tvGroupName;
		    ImageView ivGroupLogo;
		}
		
        @Override
        public int getCount() {
            return groupList.size();
        }

        @Override
        public GroupBean getItem(int position) {
            return groupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }
	}
    /**
     * 下载公开群
     * @author yao
     *
     */
    class DownloadPublicGroupTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        ArrayList<GroupBean> groupList;
        int pageId;
        int pageSize;

        public DownloadPublicGroupTask(Context context, int pageId, int pageSize) {
            super();
            this.context = context;
            this.pageId = pageId;
            this.pageSize = pageSize;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String userName = SuperWeChatApplication.getInstance().getUserName();
            groupList= NetUtil.findPublicGroup(userName, pageId, pageSize);
            return groupList!=null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                mAdapter= new GroupsAdapter(context, groupList);
                mlvPublicGroup.setAdapter(mAdapter);
            }
        }

    }
	
	public void back(View view){
		finish();
	}
}
