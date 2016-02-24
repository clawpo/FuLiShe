package cn.ucai.superwechat.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.ContactBean;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.bean.MessageBean;
import cn.ucai.superwechat.bean.UserBean;


public final class NetUtil {
    
    public static final String TAG="NetUtil";

	private String Server_root = "";

	/**
	 * 向app服务器注册
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
    public static boolean register(UserBean user) throws Exception {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_REGISTER));
		params.add(new BasicNameValuePair(I.User.USER_NAME, user.getUserName()));
		params.add(new BasicNameValuePair(I.User.NICK, user.getNick()));
		params.add(new BasicNameValuePair(I.User.PASSWORD, user.getPassword()));

		InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT,
				params, HttpUtils.METHOD_GET);
		ObjectMapper om = new ObjectMapper();
		MessageBean msg;
		try {
		    Log.i(TAG, "in="+in);
	        msg=om.readValue(in, MessageBean.class);
            Log.i(TAG, "msg="+msg.toString());
	        return msg.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return false;
	}

	/**
	 * 上传头像
	 * 
	 * @param activity
	 *            ：当前Activity
	 * @param userName
	 *            ：用户账号
	 * @param avatarType
	 *            :上传图片类型(user_avatar或group_icon)，也是图片保存在sd卡的最后 一个文件夹。
	 * @return
	 * @throws IOException
	 */
	public static boolean uploadAvatar(Activity activity, String avatarType,
			String userName) throws Exception {
	    Log.i(TAG, "NetUtil.uploadAvatar");
		HttpClient client = new DefaultHttpClient();
		String url=SuperWeChatApplication.SERVER_ROOT+"?"+I.KEY_REQUEST+"="+I.REQUEST_UPLOAD_AVATAR
				+"&"+I.User.USER_NAME+"="+userName
				+"&"+I.AVATAR_TYPE+"="+avatarType;
        Log.i(TAG, "NetUtil.uploadAvatar.url="+url);
		HttpPost post = new HttpPost(url);
		try {
	        Log.i(TAG, "NetUtil.uploadAvatar.userName="+userName);
    		File file = new File(ImageUtils.getAvatarPath(activity, avatarType),
    				userName + ".jpg");
            Log.i(TAG, "NetUtil.uploadAvatar.file="+file);
    		HttpEntity entity = HttpUtils.createInputStreamEntity(file);
            Log.i(TAG, "NetUtil.uploadAvatar.entity="+entity);
    		post.setEntity(entity);
    		HttpResponse response = client.execute(post);
            Log.i(TAG, "NetUtil.uploadAvatar.response="+response.getStatusLine().getStatusCode());
    		if (response.getStatusLine().getStatusCode() == 200) {
    			InputStream in = response.getEntity().getContent();
    			ObjectMapper om = new ObjectMapper();
    			MessageBean msg=om.readValue(in, MessageBean.class);
                Log.i(TAG, "NetUtil.uploadAvatar.msg="+msg.isSuccess());
    			return msg.isSuccess();
    		}
        }catch(FileNotFoundException e){
            Log.i(TAG, "NetUtil.uploadAvatar.FileNotFoundException="+e.getMessage().toString());
            e.printStackTrace();
	    }catch (Exception e) {
            Log.i(TAG, "NetUtil.uploadAvatar.Exception="+e.getMessage().toString());
            e.printStackTrace();
        }
        Log.i(TAG, "NetUtil.uploadAvatar.return false=");
		return false;
	}

	/**
	 * 从应用服务器下载头像
	 * @param file:头像保存的sd卡路径
	 * @param requestType：头像类型：user_avatar：个人，group_icon：群组头像
	 * @param avatar:服务端头像保存的文件名
	 */
	public static void downloadAvatar(File file, String requestType,String avatar) {
	    if(file==null){
	        return;
	    }
		if (!file.exists()) {
			try {
				ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_AVATAR));
				params.add(new BasicNameValuePair(I.User.AVATAR, avatar));
				InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT,params,HttpUtils.METHOD_GET);
				Bitmap bmpAvatar = BitmapFactory.decodeStream(in);
				OutputStream out = new FileOutputStream(file);
				if(null!=bmpAvatar){
				    bmpAvatar.compress(CompressFormat.JPEG, 100, out);
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				HttpUtils.closeClient();
			}
		}
	}

	/**
	 * 登陆应用服务器
	 * 
	 * @param userName
	 *            ：账号
	 * @param password
	 *            ：密码
	 * @return true:登陆成功
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 */
	public static UserBean login(String userName, String password)
			throws IllegalStateException, ClientProtocolException, IOException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_LOGIN));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.User.PASSWORD, password));
		InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
				HttpUtils.METHOD_GET);
		Log.e("main","server_root="+SuperWeChatApplication.SERVER_ROOT);
		ObjectMapper om = new ObjectMapper();
		UserBean user = om.readValue(in, UserBean.class);
		return user;
	}


	/**
	 * 向服务器添加联系人，并返回联系人完整信息->ContactBean类型
	 * 
	 * @param userName
	 *            ：当前用户账号
	 * @param name
	 *            ：联系人账号
	 * @return ContactBean
	 */
	public static ContactBean addContact(String userName, String name) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_ADD_CONTACT));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.Contact.NAME, name));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			ContactBean contact = om.readValue(in, ContactBean.class);
			Log.e("main","NetUtil.addContact.contact="+contact);
			return contact;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return null;
	}

	/**
	 * 删除联系人
	 * 
	 * @param myuid
	 *            :当前用户的id
	 * @param cuid
	 *            ：联系人的id
	 */
	public static boolean deleteContact(int myuid, int cuid) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,
				I.REQUEST_DELETE_CONTACT));
		params.add(new BasicNameValuePair(I.Contact.MYUID, myuid + ""));
		params.add(new BasicNameValuePair(I.Contact.CUID, cuid + ""));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			Boolean isSuccess = om.readValue(in, Boolean.class);
			Log.i("main", "删除联系人成功:" + isSuccess);
			return isSuccess;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

	public static UserBean findUserByUserName(String userName) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_USER));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			UserBean user = om.readValue(in, UserBean.class);
			return user;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return null;
	}

	/**
	 * 上传当前用户的当前位置信息
	 * 
	 * @return
	 */
	public static boolean uploadLocation(UserBean user) {
		MessageBean msg = new MessageBean(false, "上传位置失败");

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_UPLOAD_LOCATION));
		params.add(new BasicNameValuePair(I.User.USER_NAME, user.getUserName()));
		params.add(new BasicNameValuePair(I.User.LATITUDE, user.getLatitude()+ ""));
		params.add(new BasicNameValuePair(I.User.LONGITUDE, user.getLongitude()+ ""));
		params.add(new BasicNameValuePair(I.User.ID, user.getId() + ""));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			msg = om.readValue(in, MessageBean.class);
            Log.e(TAG,"uploadLocation.msg="+msg);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return msg.isSuccess();
	}

	/**
	 * 下载指定范围的用户数据，目的：获取用户的地理位置信息
	 * 
	 * @param pageId
	 * @param pageSize
	 * @return
	 */
	public static ArrayList<UserBean> downloadLocation(String userName,int pageId, int pageSize) {
		ArrayList<UserBean> users = null;

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_LOCATION));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
            if(in!=null){
                Log.e(TAG,"NetUtils.downloadLocation.in="+in);
                UserBean[] userArray = om.readValue(in, UserBean[].class);
                Log.e(TAG,"NetUtils.downloadLocation.userArray="+userArray.length);
                List<UserBean> list = Arrays.asList(userArray);
                users = new ArrayList<UserBean>(list);
            }
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return users;
	}

	/**
	 * 向应用服务器上传当前群新增的所有成员的账号
	 * 
	 * @param groupName:群名
	 * @param membersName：新增成员的账号数组
	 */
	public static boolean addGroupMembers(String groupName, String[] membersName) {
		MessageBean msg=new MessageBean(false, "新增组成员失败");
		StringBuilder newGroupMembers = new StringBuilder();
		for (String member : membersName) {
			newGroupMembers.append(member).append(",");
		}
		newGroupMembers.deleteCharAt(newGroupMembers.length() - 1);

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_ADD_GROUP_MEMBERS));
		params.add(new BasicNameValuePair(I.Group.GROUP_NAME, groupName));
		params.add(new BasicNameValuePair(I.Group.MEMBERS, newGroupMembers.toString()));
		try {
			InputStream in=HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT,params,HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			msg = om.readValue(in, MessageBean.class);
			return msg.isSuccess();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

    /**
     * 创建群,将群信息上传应用服务器
     * 
     * @param group：群名
     */
    public static boolean createGroup(GroupBean group) {
        MessageBean msg=new MessageBean(false, SuperWeChatApplication.getInstance().getResources().getString(R.string.Create_groups_Failed));
        String url=SuperWeChatApplication.SERVER_ROOT+"?"+I.KEY_REQUEST+"="+I.REQUEST_CREATE_GROUP;
        HttpClient client=new DefaultHttpClient();
        try {
            HttpPost post=new HttpPost(url);
            ObjectMapper om = new ObjectMapper();
            String strGroup = om.writeValueAsString(group);
            StringEntity entity=new StringEntity(strGroup,"utf-8");
            entity.setContentType("application/json;charset=utf-8");
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            if(response.getStatusLine().getStatusCode()==200){
                msg = om.readValue(response.getEntity().getContent(), 
                    MessageBean.class);
                return msg.isSuccess();
            }
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            HttpUtils.closeClient();
        }
        return false;
    }	
	
	/**
	 * 将当前用户的账号添加到应用服务器的当前群,申请加群
	 * 
	 * @param groupName：群名
	 * @param userName：用户名
	 * @return true:添加成功
	 */
	public static GroupBean addGroupMember(String groupName, String userName) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_ADD_GROUP_MEMBER));
		params.add(new BasicNameValuePair(I.Group.GROUP_NAME, groupName));
		params.add(new BasicNameValuePair(I.Group.MEMBERS, userName));
		try {
			InputStream in=HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			GroupBean group=om.readValue(in, GroupBean.class);
			return group;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return null;
	}

	/**
	 * 修改群昵称
	 * 
	 * @param groupName:群名
	 * @param groupNick：群昵称
	 */
	public static boolean updateGroupName(String groupName, String groupNewName) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_UPDATE_GROUP_NAME));
		params.add(new BasicNameValuePair(I.Group.GROUP_NAME, groupName));
		params.add(new BasicNameValuePair(I.Group.NEW_NAME, groupNewName));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			MessageBean msg = om.readValue(in, MessageBean.class);
			return msg.isSuccess();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

	/**
	 * 下载所有群成员
	 * 
	 * @param noContactGroupMember
	 * @return
	 */
	public static ArrayList<UserBean> downloadGroupMembers(String groupId) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_GROUP_MEMBERS));
		params.add(new BasicNameValuePair(I.Group.GROUP_ID, groupId));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			UserBean[] userArray = om.readValue(in, UserBean[].class);
			Log.e(TAG,"NetUtil.downloadGroupMembers.userArray="+userArray.length);
			List<UserBean> list = Arrays.asList(userArray);
			Log.e(TAG,"NetUtil.downloadGroupMembers.list="+list.size());
			ArrayList<UserBean> users = new ArrayList<UserBean>(list);
			Log.e(TAG,"NetUtil.downloadGroupMembers.userArray="+users.size());
			return users;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}

		return null;
	}

	/**
	 * 删除群成员,T群
	 * 
	 * @param groupName
	 * @param userName
	 */
	public static boolean deleteGroupMember(String groupName, String userName) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DELETE_GROUP_MEMBER));
		params.add(new BasicNameValuePair(I.Group.GROUP_NAME, groupName));
		params.add(new BasicNameValuePair(I.Group.MEMBERS, userName));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			Boolean isSuccess = om.readValue(in, Boolean.class);
			return isSuccess;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

	/**
	 * 向服务端发送解散指定群的请求
	 * 
	 * @param groupName
	 */
	public static boolean deleteGroup(String groupName) {
		MessageBean msg=new MessageBean(false, "解散群失败");
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_DELETE_GROUP));
		params.add(new BasicNameValuePair(I.Group.GROUP_NAME, groupName));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,
				HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			msg = om.readValue(in, MessageBean.class);
			return msg.isSuccess();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

	/**
	 * 从应用服务器下载userName的所有群组
	 * 
	 * @return
	 */
	public static ArrayList<GroupBean> downloadAllGroup(String userName) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_GROUPS));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		try {
			InputStream in=HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT,params,HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			GroupBean[] groupArray = om.readValue(in, GroupBean[].class);
			if(groupArray==null){
				return null;
			}
			String json = om.writeValueAsString(groupArray);
			List<GroupBean> list = Arrays.asList(groupArray);
			ArrayList<GroupBean> groups = new ArrayList<GroupBean>(list);
			return groups;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return null;
	}

	/**
	 * 下载所有公开群
	 */
	public static ArrayList<GroupBean> findPublicGroup(String userName,int pageId,int pageSize) {
	    ArrayList<GroupBean> publicGroupList=null;
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_PUBLIC_GROUPS));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, ""+pageId));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, ""+pageSize));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			GroupBean[] groups = om.readValue(in, GroupBean[].class);
			if(groups!=null){
			    publicGroupList=Utils.array2List(groups);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return publicGroupList;
	}
	
	/**
	 * 下载联系人->HashMap<Integer,ContactBean>
	 * @param url
	 */
	public static boolean downloadContacts(SuperWeChatApplication instance,String userName,int pageId,int pageSize){
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_CONTACTS));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
		try {
			InputStream in=HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
//			Log.e("main","in="+in.toString());
			ContactBean[] contacts = om.readValue(in, ContactBean[].class);
			HashMap<Integer, ContactBean> map = new HashMap<Integer, ContactBean>();
			for (ContactBean contact : contacts) {
				map.put(contact.getCuid(), contact);
			}
			HashMap<Integer,ContactBean> contactMap=instance.getContacts();
			contactMap.putAll(map);
			return true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}
	
	/**
	 * 下载联系人集合：ArrayList<UserBean>
	 * @param userName
     * @[param pageId
     * @param pageSize
	 */
	public static boolean downloadContactList(String userName,int pageId,int pageSize) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_CONTACT_LIST));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params,HttpUtils.METHOD_GET);
//			if(in!=null){
//				byte b[] = new byte[1000];
//				int c = in.read(b);
//				String ss = new String(b,0,c);
//				Log.e("main","ss="+ss);
//
//			}
			ObjectMapper om = new ObjectMapper();
			UserBean[] userArray = om.readValue(in, UserBean[].class);
			if(userArray==null){
			    Log.i("main","download contact list false");
				return false;
			}
			//将数组转换为集合
			ArrayList<UserBean> userList=Utils.array2List(userArray);
			//获取已添加的所有联系人的集合
			ArrayList<UserBean> contactList = SuperWeChatApplication.getInstance().getContactList();
			//将新下载的数据添加到原联系人集合中
			contactList.addAll(userList);
			return true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}
	/**
	 * 获得服务器状态的请求
	 */
	public static MessageBean getServerStatus() {
		MessageBean msg = new MessageBean(false, "连接失败");
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_SERVERSTATUS));
		try {
			InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			msg = om.readValue(in, MessageBean.class);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return msg;
	}
	/**
	 * 将注册账号为userName的用户从应用服务器删除,同时将上传的头像从服务器删除
	 * @param userName
	 */
    public static MessageBean unRegister(String userName) {
//		if(isServerConnectioned()){
//			return null;
//		}
        MessageBean msg = new MessageBean(false, "取消注册失败");
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_UNREGISTER));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        try {
            InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            msg = om.readValue(in, MessageBean.class);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            HttpUtils.closeClient();
        }
        return msg;
    }
	
    /**
     *查询指定群名称的群
     * @param groupName
     * @return 
     */
    public static GroupBean findGroupByName(String groupName){
//		if(isServerConnectioned()){
//			return null;
//		}
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_GROUP));
        params.add(new BasicNameValuePair(I.Group.NAME, groupName));
        try {
            InputStream in = HttpUtils.getInputStream(SuperWeChatApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            GroupBean group = om.readValue(in, GroupBean.class);
            return group;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            HttpUtils.closeClient();
        }
        return null;
    }
//
//	public static boolean isServerConnectioned(){
//		final String st2 = SuperWeChatApplication.applicationContext.getResources().getString(R.string.the_current_network);
//		//添加本地服务器连接监听
//		boolean localConnectioned = NetUtil.getServerStatus().isSuccess();
//		Log.e("main", "MainActivity.MyConnectionListener.localConnectioned=" + localConnectioned);
//		if (!localConnectioned) {
//			Log.e("main", "MainActivity.MyConnectionListener.localConnectioned is false,show the popu");
//			Toast.makeText(SuperWeChatApplication.applicationContext,st2,Toast.LENGTH_LONG).show();
//		}
//		return localConnectioned;
//	}
}
