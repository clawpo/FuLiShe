package cn.ucai.superwechat.utils;

import android.content.Context;
import android.widget.Toast;

import com.easemob.chat.EMMessage.ChatType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.UserBean;

public class Utils {
    
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }
    
    public static void showToast(Context context,String text,int time){
        Toast.makeText(context,text,time).show();
    }
    
    public static void showToast(Context context,int  strId,int time){
        Toast.makeText(context, strId, time).show();
    }

    /**
     * 将数组转换为ArrayList集合
     * @param ary
     * @return
     */
    public static <T> ArrayList<T> array2List(T[] ary){
        List<T> list = Arrays.asList(ary);
        ArrayList<T> arrayList=new ArrayList<T>(list);
        return arrayList;
    }
    
    /**
     * 添加新的数组元素：数组扩容
     * @param array：数组
     * @param t：添加的数组元素
     * @return：返回添加后的数组
     */
    public static <T> T[] add(T[] array,T t){
        array=Arrays.copyOf(array, array.length+1);
        array[array.length-1]=t;
        return array;
    }
    
    /**
     * 返回发送消息者，发送消息者可能是群聊中成员或单聊中的好友
     * @param chatType：群聊/单聊
     * @param groupId：群聊的groupId或单聊中的登陆者userName
     * @param userName：发送消息者的userName
     * @return
     */
    public static UserBean getMessageFromUser(ChatType chatType,String groupId,String userName){
        ArrayList<UserBean> userList = null;
        switch (chatType) {
        case GroupChat://群聊
            HashMap<String,ArrayList<UserBean>> groupMembers = SuperWeChatApplication.getInstance().getGroupMembers();
            //获取指定groupId的群聊成员集合
            userList = groupMembers.get(groupId);
            break;
        case ChatRoom:
            break;
        default://单聊
            userList = SuperWeChatApplication.getInstance().getContactList();
            break;
        }
        //获取发送消息者
        UserBean user = new UserBean();
        user.setUserName(userName);
        if(userList==null ||userList.isEmpty()){
            return null;
        }
        int id = userList.indexOf(user);
        if(id>=0){
            return userList.get(id);
        }
        return null;
    }
}
