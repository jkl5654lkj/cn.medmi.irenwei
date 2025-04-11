package cn.medmi.irenwei;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Author medmi
 * @Date 2025/4/10 13:13
 */
public class Hooker implements IXposedHookLoadPackage {

    private static final String TAG = "Hooker";
    final private String target_className_fragment = "com.ebooks.reader.ui.fragment.ReaderFragment";
    final private String target_className_DoHomeWorkSubjectActivity = "com.leimingtech.exam_android.activity.DoHomeWorkSubjectActivity";
    final private String target_className_ExamRespondActivity = "com.leimingtech.exam_android.activity.ExamRespondActivity";
    final private String target_className_IdentityVerifyActivity = "com.leimingtech.exam_android.activity.IdentityVerifyActivity";

    public String stackTrace() {
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

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {



        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context mContext = (Context) param.args[0];//拿到系统传入的原始context
                        ClassLoader classLoader = mContext.getClassLoader();
                        try {
                            XposedHelpers.findAndHookMethod(target_className_fragment, classLoader, "copyContent", new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    Class<?> Class_ReaderFragment = methodHookParam.thisObject.getClass();
                                    try {
                                        Method method = Class_ReaderFragment.getMethod("getActivity");
                                        method.setAccessible(true);
                                        Context fragmentAvtivitycontext = (Context) method.invoke(methodHookParam.thisObject);
                                        ClipboardManager clipboardManager = (ClipboardManager) fragmentAvtivitycontext.getSystemService(Context.CLIPBOARD_SERVICE);
                                        Field selectionText = Class_ReaderFragment.getDeclaredField("selectionText");
                                        selectionText.setAccessible(true);
                                        clipboardManager.setText(selectionText.get(methodHookParam.thisObject).toString());
                                        Toast.makeText(fragmentAvtivitycontext, "复制成功！", Toast.LENGTH_SHORT).show();
                                    } catch (IllegalAccessException e) {
                                        Log.e(TAG, "replaceHookedMethod: " + e.getMessage());
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "replaceHookedMethod: " + e.getMessage());
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            Log.i("Hooker", "没有找到类：com.ebooks.reader.ui.fragment.ReaderFragment", e);
                            return;
                        }
                        try {
                            XposedHelpers.findAndHookMethod(target_className_DoHomeWorkSubjectActivity, classLoader, "setQiePingVis", boolean.class, new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    //XposedBridge.log(stackTrace());
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            Log.i("Hooker", "没有找到类：com.ebooks.reader.ui.fragment.ReaderFragment", e);
                            return;
                        }
                        try {
                            XposedHelpers.findAndHookMethod(target_className_ExamRespondActivity, classLoader, "setQiePingVis", boolean.class, new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    //XposedBridge.log(stackTrace());
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            Log.i("Hooker", "没有找到类：com.ebooks.reader.ui.fragment.ReaderFragment", e);
                            return;
                        }
                        try {
                            XposedHelpers.findAndHookMethod(target_className_IdentityVerifyActivity, classLoader, "setQiePingVis", boolean.class, new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    //XposedBridge.log(stackTrace());
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            Log.i("Hooker", "没有找到类：com.ebooks.reader.ui.fragment.ReaderFragment", e);
                            return;
                        }

                    }
        });


    }
}