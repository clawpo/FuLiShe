package cn.ucai.superwechat;

/**
 * 个人资料上传头像bug：服务端没有收到上传的头像。
 * ChatActivity聊天页面没有显示登陆用户的头像
 * @author chen
 *
 */
public interface I {
    //  本机服务端地址 
	public static final String SERVER_ROOT="http://10.0.2.2:8080/SuperQQ4Server/Server";
    //阿里云服务器的服务端地址
//	String SERVER_ROOT="http://139.196.185.33:8080/SuperQQ3Server/Server";
    public static final String PAGE_ID="pageId";
    public static final String PAGE_SIZE="pageSize";
    /** 上传图片的类型：user_avatar或group_icon*/
    public static final String AVATAR_TYPE="avatarType";
    
    public static class User{
        public static final String ID="id";
        public static final String UID="uid";
        public static final String USER_NAME="userName";
        
        public static final String NICK="nick";
        public static final String AVATAR="avatar";
        public static final String HEADER="header";
        public static final String PASSWORD="password";
        public static final String LATITUDE="latitude";
        public static final String LONGITUDE="longitude";
        public static final String UN_READ_MSG_COUNT="unreadMsgCount";
        public static final String GROUPS="groups";
        
    }
    
    public static class Contact extends User{
        public static final String NAME="name";
        public static final String MYUID="myuid";
        public static final String CUID="cuid";
        public static final String IS_GET_MY_LOCATION="isGetMyLocation";
        public static final String IS_SHOW_MY_LOCATION="isShowMyLocation";
    }
    
    public static class Group{
        public static final String NAME="name";//群名
        public static final String GROUP_NAME="groupName";//群名，url中使用
        public static final String GROUP_ID="groupId";//主键
        public static final String NEW_NAME="new_name";//群新名称
        public static final String AVATAR="avatar";//群图标
        public static final String INTRO="intro";//群简介
        public static final String OWNER="owner";//群主账号
        public static final String IS_PUBLIC="isPublic";//是否公开
        public static final String MODIFIED_TIME="modifiedTime";//群信息修改的时间，单位：毫秒
        public static final String MEMBERS="members";//群成员的账号
        public static final String IS_EXAME="isExame";//群成员的账号
    }
    
    /** 请求的键*/
    String KEY_REQUEST="request";
    /**
     * 客户端发送的获得服务器状态的请求
     */
    String REQUEST_SERVERSTATUS="server_status";
    /**
     * 客户端发送的注册请求
     */
    String REQUEST_REGISTER="register";
    /**
     * 发送取消注册的请求
     */
    String REQUEST_UNREGISTER="unregister";
    
    String REQUEST_UPLOAD_AVATAR="upload_avatar";

    String REQUEST_LOGIN="login";
    
    String REQUEST_DOWNLOAD_AVATAR="download_avatar";
    
    /** 下载头像的接口*/
//    String DOWNLOAD_AVATAR_URL=SERVER_ROOT+
//        "?request="+REQUEST_DOWNLOAD_AVATAR+"&avatar=";
    String DOWNLOAD_AVATAR_URL=SuperWeChatApplication.SERVER_ROOT+
            "?request="+REQUEST_DOWNLOAD_AVATAR+"&avatar=";
    
    String REQUEST_DOWNLOAD_CONTACTS="download_contacts";
    
    String REQUEST_DOWNLOAD_CONTACT_LIST="download_contact_list";
    
    String REQUEST_DELETE_CONTACT="delete_contact";
    
    String REQUEST_ADD_CONTACT="add_contact";
    
    String REQUEST_FIND_USER="find_user";
    
    String REQUEST_UPLOAD_LOCATION="upload_location";
    
    String REQUEST_DOWNLOAD_LOCATION="download_location";
    
    String REQUEST_CREATE_GROUP="create_group";

    String REQUEST_ADD_GROUP_MEMBERS="add_group_members";
    
    String REQUEST_ADD_GROUP_MEMBER="add_group_member";
    
    String REQUEST_UPDATE_GROUP_NAME="update_group_name";
    
    String REQUEST_DOWNLOAD_GROUP_MEMBERS="download_group_members";
    
    String REQUEST_DELETE_GROUP_MEMBER="delete_group_member";
    
    String REQUEST_DELETE_GROUP="delete_group";
    
    String REQUEST_DOWNLOAD_GROUPS="download_groups";
    
    String REQUEST_FIND_PUBLIC_GROUPS="download_public_groups";
    
    String REQUEST_FIND_GROUP="find_group_by_group_name";
}
