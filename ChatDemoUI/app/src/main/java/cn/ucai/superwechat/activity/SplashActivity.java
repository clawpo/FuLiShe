package cn.ucai.superwechat.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

import java.util.ArrayList;

import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.MessageBean;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.task.DownloadContactsTask;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.NetUtil;

/**
 * 开屏页
 *
 */
public class SplashActivity extends BaseActivity {
	private RelativeLayout rootLayout;
	private TextView versionText;
	
	private static final int sleepTime = 2000;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.activity_splash);
		super.onCreate(arg0);

		rootLayout = (RelativeLayout) findViewById(R.id.splash_root);
		versionText = (TextView) findViewById(R.id.tv_version);

		versionText.setText(getVersion());
		AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
		animation.setDuration(1500);
		rootLayout.startAnimation(animation);
	}

	@Override
	protected void onStart() {
		super.onStart();

		new Thread(new Runnable() {
			public void run() {
				if (DemoHXSDKHelper.getInstance().isLogined()) {
					// ** 免登陆情况 加载所有本地群和会话
					//不是必须的，不加sdk也会自动异步去加载(不会重复加载)；
					//加上的话保证进了主页面会话和群组都已经load完毕
					long start = System.currentTimeMillis();
					EMGroupManager.getInstance().loadAllGroups();
					EMChatManager.getInstance().loadAllConversations();
					long costTime = System.currentTimeMillis() - start;
					//等待sleeptime时长
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//将数据库中的当前登录用户保存在内存中
					String userName = SuperWeChatApplication.getInstance().getUserName();
					UserDao dao = new UserDao(SplashActivity.this);
					UserBean user = dao.findUserByUserName(userName);
					SuperWeChatApplication.getInstance().setUserBean(user);
                    //添加连接服务器状态的判断
                    MessageBean msg = NetUtil.getServerStatus();
                    Log.e("SplashAvtivity","！！！！！！SplashAvtivity.NetUtil.login.user="+msg);
                    if(msg.isSuccess()){
                        Log.e("SplashAvtivity","！！！！！！SplashAvtivity.NetUtil.login.useif*****r="+msg.isSuccess());
                        //下载联系人数据
                        ArrayList<UserBean> contactList  = SuperWeChatApplication.getInstance().getContactList();
                        if(contactList.size() == 0){
                            Log.i("main","SplashActivity.download contactList");
                            new DownloadContactsTask(SplashActivity.this, userName, 0, 20).execute();
                        }
                        //进入主页面
                        startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra("server_status",msg.isSuccess()));
                        finish();
                    }else{
                        Log.e("SplashAvtivity","！！！！！！SplashAvtivity.NetUtil.login.userelse****="+msg.isSuccess());
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
//                        NetUtil.isServerConnectioned();
                        finish();
                    }
				}else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					finish();
				}
			}
		}).start();

	}
	
	/**
	 * 获取当前应用程序的版本号
	 */
	private String getVersion() {
		String st = getResources().getString(R.string.Version_number_is_wrong);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return st;
		}
	}
}
