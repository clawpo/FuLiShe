package cn.ucai.superwechat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.bean.UserBean;

/**
 * Created by clawpo on 16/2/11.
 */
public class UserDao extends SQLiteOpenHelper {

    public static final String ID="_id";
    public static final String TABLE_NAME="user";

    public UserDao(Context context) {
        super(context, "user.db", null, 1);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table if not exists "+TABLE_NAME
                +"("+ I.User.ID+" integer primary key autoincrement,"
                + I.User.USER_NAME+" varchar unique,"
                + I.User.UID+" int unique,"
                + I.User.NICK+" varchar,"
                + I.User.AVATAR+" varchar,"
                + I.User.HEADER+" varchar,"
                + I.User.LATITUDE+" double,"
                + I.User.LONGITUDE+" double,"
                + I.User.PASSWORD+" varchar,"
                + I.User.UN_READ_MSG_COUNT+" int)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    public boolean addUser(UserBean user){
        ContentValues values = new ContentValues();
        values.put(I.User.AVATAR, user.getAvatar());
        values.put(I.User.HEADER, user.getHeader());
        values.put(I.User.LATITUDE, user.getLatitude());
        values.put(I.User.LONGITUDE,user.getLongitude());
        values.put(I.User.NICK, user.getNick());
        values.put(I.User.PASSWORD, user.getPassword());
        values.put(I.User.UID,user.getId());
        values.put(I.User.UN_READ_MSG_COUNT, user.getUnreadMsgCount());
        values.put(I.User.USER_NAME , user.getUserName());
        SQLiteDatabase db = getWritableDatabase();
        long rowId  = db.insert(TABLE_NAME, null, values);
        return rowId!=0;
    }

    public UserBean findUserByUserName(String userName){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from " + TABLE_NAME+ " where "+ I.User.USER_NAME +"=?";
        Cursor c = db.rawQuery(sql, new String[]{userName});
        Log.e("main","UserDao.findUserByUserName.sql="+sql);
        if(c.moveToNext()){
            int uid = c.getInt(c.getColumnIndex(I.User.UID));
            String nick=c.getString(c.getColumnIndex(I.User.NICK));
            String avatar=c.getString(c.getColumnIndex(I.User.AVATAR));
            String header=c.getString(c.getColumnIndex(I.User.HEADER));
            double latitude=c.getDouble(c.getColumnIndex(I.User.LATITUDE));
            double longitude=c.getDouble(c.getColumnIndex(I.User.LONGITUDE));
            String password=c.getString(c.getColumnIndex(I.User.PASSWORD));
            int unreadMsgCount=c.getInt(c.getColumnIndex(I.User.UN_READ_MSG_COUNT));
            UserBean user = new UserBean(uid, "ok", userName, nick, password, avatar, latitude, longitude, unreadMsgCount);
            Log.e("main","UserDao.findUserByUserName.user="+user);
            return user;
        }
        return null;
    }

    public boolean updateUser(UserBean user){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(I.User.AVATAR, user.getAvatar());
        values.put(I.User.HEADER, user.getHeader());
        values.put(I.User.LATITUDE, user.getLatitude());
        values.put(I.User.LONGITUDE,user.getLongitude());
        values.put(I.User.NICK, user.getNick());
        values.put(I.User.PASSWORD, user.getPassword());
        values.put(I.User.UID,user.getId());
        values.put(I.User.UN_READ_MSG_COUNT, user.getUnreadMsgCount());
        values.put(I.User.USER_NAME , user.getUserName());
        int count = db.update(TABLE_NAME, values, I.User.USER_NAME+"=?", new String[]{user.getUserName()});
        return count>0;
    }
}
