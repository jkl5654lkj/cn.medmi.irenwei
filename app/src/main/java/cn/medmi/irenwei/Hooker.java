package cn.medmi.irenwei;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
                    dohook(classLoader);

                } catch (Exception e) {
                    Log.i("Hooker", "hook错误: ", e);
                    return;
                }
            }
        });


    }

    private void dohook(ClassLoader classLoader) {
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


        XposedHelpers.findAndHookMethod(target_className_DoHomeWorkSubjectActivity, classLoader, "setQiePingVis", boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                //XposedBridge.log(stackTrace());
                return null;
            }
        });

        XposedHelpers.findAndHookMethod(target_className_ExamRespondActivity, classLoader, "setQiePingVis", boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                //XposedBridge.log(stackTrace());
                return null;
            }
        });


        XposedHelpers.findAndHookMethod(target_className_IdentityVerifyActivity, classLoader, "setQiePingVis", boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                //XposedBridge.log(stackTrace());
                return null;
            }
        });

        XposedHelpers.findAndHookMethod("com.pmph.main.ad.AdActivity", classLoader, "onCreate", "android.os.Bundle", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "AdActivity: " + param.thisObject.getClass().getName());
                Activity activity = (Activity) param.thisObject;
                activity.finish();
            }
        });
        //我想监听布局
        XposedHelpers.findAndHookMethod("android.view.ViewGroup", classLoader, "dispatchTouchEvent", "android.view.MotionEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                View view = (View) param.thisObject;
                //检测长按手势
                MotionEvent action = (MotionEvent) param.args[0];
                //这个才是重点关注对象
                if (param.thisObject.getClass().getName().equals("com.leimingtech.exam_android.view.ExamRelativeLayout")){
                    doOnce(classLoader,param, action);
                }
            }
        });
    }
    public static List<Activity> getActivityInstances() {
        try {
            // 1. 获取 ActivityThread 实例
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);

            // 2. 获取 mActivities 字段（类型为 ArrayMap<IBinder, ActivityClientRecord>）
            Field mActivitiesField = activityThreadClass.getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            ArrayMap<IBinder, Object> mActivities = (ArrayMap<IBinder, Object>) mActivitiesField.get(activityThread);

            // 3. 遍历 mActivities 获取 Activity 实例
            List<Activity> activities = new ArrayList<>();
            for (Object record : mActivities.values()) {
                Class<?> recordClass = record.getClass();
                Field activityField = recordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(record);
                activities.add(activity);
            }
            return activities;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    HashMap<String,Integer> ids = new HashMap<>();
    boolean hasdone = false;
    @SuppressLint("WrongConstant")
    private void doOnce(ClassLoader classLoader, XC_MethodHook.MethodHookParam param, MotionEvent action) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        switch (action.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hasdone = false;
            case MotionEvent.ACTION_MOVE:
                int hisindex = action.getHistorySize() - 1;
                float historicalX;
                float historicalY;
                if (hisindex<=0){
                    return;
                }
                historicalX = action.getHistoricalX(hisindex);
                historicalY = action.getHistoricalY(hisindex);
                float x = action.getX();
                float y = action.getY();
                float vx = Math.abs(x - historicalX);
                float vy = Math.abs(y - historicalY);
                if (action.getEventTime() - action.getDownTime() > 1300) {
                    if (!(vx<2&&vy<2))return;
                    if (!hasdone) {
                        hasdone = true;
                        //反射获取当前viewgroup 的context字段
                        Field mContext = Class.forName("android.view.View").getDeclaredField("mContext");
                        mContext.setAccessible(true);
                        Context context = (Context) mContext.get(param.thisObject);
                        Toast.makeText(context, "已复制全部题目到剪贴板", Toast.LENGTH_SHORT).show();
                        Log.d("Hooker", "beforeHookedMethod: 复制全部题目");
                        Class<?> SubjectService = classLoader.loadClass("com.leimingtech.exam_android.baseadapter.SubjectService");
                        Class<?> DoHomeWorkSubjectActivity = classLoader.loadClass("com.leimingtech.exam_android.activity.DoHomeWorkSubjectActivity");
                        Field mInstance = DoHomeWorkSubjectActivity.getDeclaredField("dao");
                        Field examStudentId = DoHomeWorkSubjectActivity.getDeclaredField("examStudentId");
                        Field testId = DoHomeWorkSubjectActivity.getDeclaredField("testId");
                        examStudentId.setAccessible(true);
                        testId.setAccessible(true);
                        mInstance.setAccessible(true);
                        //这里又要反射获取aplication的实例
                        List<Activity> activityInstances = getActivityInstances();
                        AtomicReference<Activity> activity1 = new AtomicReference<>();
                        activityInstances.forEach(activity -> {
                           if (activity.getClass().getName().equals("com.leimingtech.exam_android.activity.DoHomeWorkSubjectActivity")){
                               activity1.set(activity);
                           }
                        });
                        if (activity1.get() == null) {
                            Log.d("Hooker", "doOnce: 并没有找到Activity类的实例");
                            return;
                        }
                        Activity activity = activity1.get();
                        //获取实例的dao
                        Object dao = mInstance.get(activity);
                        String examStudentID = (String) examStudentId.get(activity);
                        String testID = (String) testId.get(activity);
                        //调用dao的findall方法
                        Method method = SubjectService.getDeclaredMethod("findAll",String.class);
                        method.setAccessible(true);
                        mInstance.setAccessible(true);
                        if (dao==null){
                            Toast.makeText(context, "未获取到dao实例字段", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ids.put(testID,0);
                        ids.put(examStudentID,1);
                        Set<String> strings = ids.keySet();
                        for (String string : strings) {//string 是数据库键
                            Object invoke = method.invoke(dao, string);
                            JSONArray jsonArray = JSONArray.from(invoke);
                            if (!(jsonArray.size()>0))continue;
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                if (invoke == null){
                                    //说明当前容器已经空啦
                                    Log.d("Hooker", "doOnce: 主动调用结果: invoke:["+i+"] 没有找到");
                                }else{
                                    T t = new T((JSONObject) jsonArray.get(i));
                                    builder.append(i+". ").append(t).append("\n");
                                    Log.d("Hooker", t.toString());
                                }
                            }
                            ClipboardManager systemService = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("mmi", builder.toString());
                            systemService.setPrimaryClip(clip);
                        }
                    }
                }
        }
    }

    public static String convertDigitsToLetters(String digits) {
        StringBuilder result = new StringBuilder();
        for (char c : digits.toCharArray()) {
            // 将字符转换为数字（例如 '0' → 0）
            int num = Character.getNumericValue(c);
            // 转换为对应字母：0 → A, 1 → B, ...
            char letter = (char) ('A' + num);
            result.append(letter);
        }
        return result.toString();
    }

    public static class T implements Serializable {
        String questionModelName;
        String name;
        JSONArray choiceList;
        JSONArray questionList;


        public T(JSONObject o) {
            String questionModelName = o.getString("questionModelName");
            String name = (String) o.get("name");
            JSONArray choiceList = JSONArray.from(o.get("choiceList"));
            JSONArray questionList = JSONArray.from(o.get("questionList"));
            this.questionModelName = questionModelName;
            this.name = name;
            this.choiceList = choiceList;
            this.questionList = questionList;
        }

        String getWholeT() throws Exception {
            String question = "";
            String option = "";
            StringBuilder stringBuilder = new StringBuilder();
            switch (questionModelName){
                case "X":
                case "A1":
                case "A2"://a1a2关键数据结构一致
                    question = ThreeDESUtil.decrypt3DES(name);
                    for (int i = 0; i < choiceList.size(); i++) {
                        stringBuilder.append(convertDigitsToLetters(i + "")).append(".").append(ThreeDESUtil.decrypt3DES((String) choiceList.get(i))).append("\n");
                    }
                    option = stringBuilder.toString();
                    break;
                case "A3":
                case "A4":
                    question = ThreeDESUtil.decrypt3DES(name);//大问题+小问题
                    question = question+ThreeDESUtil.decrypt3DES(JSONObject.from(questionList.get(0)).get("name").toString());
                    JSONArray objects = ((JSONArray)((JSONObject)questionList.get(0)).get("choiceList"));
                    for (int i = 0; i < objects.size(); i++) {
                        stringBuilder.append(convertDigitsToLetters(i + "")).append(".").append(ThreeDESUtil.decrypt3DES((String) objects.get(i))).append("\n");
                    }
                    option = stringBuilder.toString();
                    break;
                case "B1":
                    //从questionList里面获取
                    question = ThreeDESUtil.decrypt3DES(JSONObject.from(questionList.get(0)).get("name").toString());
                    for (int i = 0; i < choiceList.size(); i++) {
                        stringBuilder.append(convertDigitsToLetters(i + "")).append(".").append(ThreeDESUtil.decrypt3DES((String) choiceList.get(i))).append("\n");
                    }
                    option = stringBuilder.toString();
                    break;
                default:
                    Log.d("T", "getWholeT: 还有未知题型未适配");
            }
            return question+"\n"+option+"\n";
        }

        @NonNull
        @Override
        public @NotNull String toString() {
            try {
                return getWholeT();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ThreeDESUtil {
        private static final String ENCRYPTION_MANNER = "DESede";
        private static final String key = "xUHdKxzVCbsgVIwTnc1jtpWn";


        public static String decrypt3DES(String str) throws Exception {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ENCRYPTION_MANNER);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_MANNER);
            cipher.init(2, secretKeySpec);
            return new String(cipher.doFinal(android.util.Base64.decode(str.getBytes("UTF-8"), 0)), "utf-8");
        }
    }
}