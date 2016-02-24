package cn.ucai.superwechat.task;

import java.util.ArrayList;

import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.GroupBean;
import cn.ucai.superwechat.utils.NetUtil;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DownloadContactsTask extends AsyncTask<Void, Void, Boolean> {
    
    Context context;
    String userName;
    int pageId;
    int pageSize;

    public DownloadContactsTask(Context context, String userName, int pageId,
            int pageSize) {
        super();
        this.context = context;
        this.userName = userName;
        this.pageId = pageId;
        this.pageSize = pageSize;
    }



    @Override
    protected Boolean doInBackground(Void... params) {
        Log.e("main", "!!!!!!!DownloadContactTask download contacts");
        boolean isSuccess = NetUtil.downloadContacts(SuperWeChatApplication.getInstance(), userName, pageId, pageSize);
        Log.e("main", "!!!!!!!!!DownloadContactTask download contacts:"+isSuccess);
        if(isSuccess){
            isSuccess = NetUtil.downloadContactList(userName, pageId, pageSize);
            Log.e("main","!!!!!!!Downlaod ContactList:"+isSuccess);
        }
//        else{
//            Toast.makeText(context,"服务器下载失败",Toast.LENGTH_SHORT).show();
//        }
        if(isSuccess){
            ArrayList<GroupBean> groupList = SuperWeChatApplication.getInstance().getGroupList();
            ArrayList<GroupBean>groups = NetUtil.downloadAllGroup(userName);
            if(groups!=null){
                groupList.addAll(groups);
                Intent intent = new Intent("update_group");
                context.sendBroadcast(intent);
            }
        }
        return isSuccess;
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        if(result){
			//向ContactFragment发送更新联系人的广播
            Intent intent = new Intent("update_contacts");
            context.sendBroadcast(intent);
        }
    }

}
