package cn.medmi.hookutils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import com.leimingtech.exam_android.baseadapter.SubjectService;
import static android.content.Context.WINDOW_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;

/**
 * @Author medmi
 * @Date 2025/5/20 11:25
 */
public class M {
    public interface X{
        void onApplicationAttached(ClassLoader classLoader);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static void tryCreateWindow(Activity application) {
        WindowManager wm = (WindowManager) application.getSystemService(WINDOW_SERVICE);
        TextView tv = new TextView(application);
        tv.setText("Test Window");

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION, // 常规应用窗口类型
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        // 需要声明SYSTEM_ALERT_WINDOW权限
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        // 并且需要动态请求权限
        if (!Settings.canDrawOverlays(application)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            application.startActivity(intent);
        }

        try {
            wm.addView(tv, params); // 这里会抛出异常！
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
            // 典型错误：Unable to add window -- token null is not valid
        }
    }

    public static boolean Debug = true;
    public static void onAttach(X x){

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("MMM", "afterHookedMethod: 执行");
                super.afterHookedMethod(param);
                Context mContext = (Context) param.args[0];//拿到系统传入的原始context
                ClassLoader classLoader = mContext.getClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                x.onApplicationAttached(classLoader);
            }
        });
    }

    public static String stackTrace() {
        // 获取当前调用堆栈
        Throwable stackTrace = new Throwable();
        StackTraceElement[] elements = stackTrace.getStackTrace();

        // 格式化堆栈信息
        StringBuilder sb = new StringBuilder("调用堆栈：\n");
        for (StackTraceElement element : elements) {
            sb.append("  at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(").append(element.getFileName()).append(":").append(element.getLineNumber()).append(")\n");
        }
        return sb.toString();
    }
}
