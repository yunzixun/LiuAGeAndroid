package tv.baokan.liuageandroid.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.ShareSDK;
import okhttp3.OkHttpClient;
import tv.baokan.liuageandroid.model.UserBean;

public class App extends LitePalApplication {

    private static final String TAG = "App";

    public static App app;

    // 用于存放所有启动的Activity的集合
    private List<Activity> mActivityList;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        // 存放所有activity的集合
        mActivityList = new ArrayList<>();

        // 初始化app异常处理器 - 打包的时候开启
//        CrashHandler handler = CrashHandler.getInstance();
//        handler.init(getApplicationContext());

        // 初始化OkHttpUtils
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);

        // 初始化Fresco
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .build();
        Fresco.initialize(this, config);

        // 初始化ShareSDK
        ShareSDK.initSDK(this);

        // 初始化JPush
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

        // 更新用户登录状态
        UserBean.updateUserInfoFromNetwork(new UserBean.OnUpdatedUserInfoListener());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ShareSDK.stopSDK(this);
    }

    /**
     * 获取当前应用的版本号
     *
     * @return 版本号
     */
    public String getVersionName() {
        PackageManager pm = getPackageManager();
        // 第一个参数：应用程序的包名
        // 第二个参数：指定信息的标签，0表示获取基础信息，比如包名、版本号。要想获取权限等信息必须要通过标签指定。
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 添加Activity
     */
    public void addActivity(Activity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity);
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity(Activity activity) {
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeAllActivity() {
        for (Activity activity : mActivityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 获取当前应用的版本号
     *
     * @return 版本号
     */
//    private String getVersionName() {
//        PackageManager pm = getPackageManager();
//        // 第一个参数：应用程序的包名
//        // 第二个参数：指定信息的标签，0表示获取基础信息，比如包名、版本号。要想获取权限等信息必须要通过标签指定。
//        try {
//            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
//            return info.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    /**
     *判断当前应用程序处于前台还是后台
     */
    public boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
