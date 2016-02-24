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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.adapter.ContactAdapter;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.widget.Sidebar;

public class PickContactNoCheckboxActivity extends BaseActivity {

	private ListView listView;
	private Sidebar sidebar;
	protected ContactAdapter contactAdapter;
	private ArrayList<UserBean> contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_contact_no_checkbox);
		listView = (ListView) findViewById(R.id.list);
		sidebar = (Sidebar) findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		contactList = new ArrayList<UserBean>();
		// 获取设置contactlist
		getContactList();

		Log.e("main","PickContactNoCheckboxActivity.onCreated.contactList="+contactList.size());
		Log.e("main","SuperWeChatApplication.getInstance().getContactList().size()="
				+SuperWeChatApplication.getInstance().getContactList().size());
		// 设置adapter
		contactAdapter = new ContactAdapter(this, R.layout.row_contact, contactList);
		listView.setAdapter(contactAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}
		});

	}

	protected void onListItemClick(int position) {
//		if (position != 0) {
			setResult(RESULT_OK, new Intent().putExtra("username", contactAdapter.getItem(position)
					.getUserName()));
			finish();
//		}
	}

	public void back(View view) {
		finish();
	}

	private void getContactList() {
		contactList.clear();
		ArrayList<UserBean> userList = SuperWeChatApplication.getInstance().getContactList();
		contactList.addAll(userList);
		
		// 排序
		Collections.sort(contactList, new Comparator<UserBean>() {

			@Override
			public int compare(UserBean lhs, UserBean rhs) {
				return lhs.getUserName().compareTo(rhs.getUserName());
			}
		});
	}

}
