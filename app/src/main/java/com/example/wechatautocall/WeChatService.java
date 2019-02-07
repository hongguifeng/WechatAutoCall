package com.example.wechatautocall;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

import java.util.List;

public class WeChatService extends AccessibilityService {
    private static final String TAG = "WeChatService";

    private static final String HOME_LAUNCHER_ACTIVITY = "com.tencent.mm.ui.LauncherUI";
    private static final String WECHAT_CALLING_ACTIVITY = "com.tencent.mm.plugin.voip.ui.VideoActivity";
    private static final String WECHAT_LIST_ACTIVITY = "com.tencent.mm.ui.LauncherUI";

    private class ChatState{
        public static final int STATE_NONE = 0;
        public static final int STATE_PREPARE_CALL = 1;
        public static final int STATE_CALLING = 2;
        public static final int STATE_BECALLING = 3;
        public static final int STATE_SOUND_BECALLING = 4;
        public static final int STATE_TALKING = 5;
        public static final int STATE_HANGUP = 6;
        public static final int STATE_OTHER = 7;
    }

    private int mState = ChatState.STATE_NONE;

    public WeChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Shell.SU.run("input tap 1400 20");
                    Log.i(TAG, "screen on");
                }
            }
        };
        
        registerReceiver(receiver, filter);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.v(TAG, "accessibilityEvent " + accessibilityEvent.toString());

        // Not a message
//        if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
//            return;

        if (isOnLauncher(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnLauncher");
            mState = ChatState.STATE_NONE;
            return;
        }

        if(isOnLauncherAppList(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnLauncherAppList");
            mState = ChatState.STATE_OTHER;
            return;
        }

        if(isOnWechatList(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnWechatList");
            mState = ChatState.STATE_OTHER;
        }

        if(isOnWechatChat(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnWechatChat");
            if(mState == ChatState.STATE_NONE){
                Log.d(TAG, "onAccessibilityEvent: from launcher");
                CommandResult result = Shell.SU.run("input tap 1490 1900");
                CommandResult result2 = Shell.SU.run("input tap 945 1600");
                mState = ChatState.STATE_PREPARE_CALL;
            } else if((mState == ChatState.STATE_BECALLING)
                        || (mState == ChatState.STATE_CALLING)
                        || (mState == ChatState.STATE_TALKING)) {
                CommandResult result2 = Shell.SU.run("input tap 765 1995");
            }
        }

        if(isOnWechatChatOption(accessibilityEvent)){
            if(mState == ChatState.STATE_PREPARE_CALL) {
                Log.d(TAG, "onAccessibilityEvent: isOnWechatChatOption");
                CommandResult result = Shell.SU.run("input tap 750 1760");
            }
        }

        if(isOnWechatBeCalling(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnWechatBeCalling");
            CommandResult result = Shell.SU.run("input tap 1380 1800");
            mState = ChatState.STATE_TALKING;
        }

        if(isOnWechatBeSoundCalling(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnWechatBeSoundCalling");
            CommandResult result = Shell.SU.run("input tap 1380 1800");
            mState = ChatState.STATE_TALKING;
        }

        if(isOnWechatTalking(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnWechatTalking");
            mState = ChatState.STATE_TALKING;
        }

        if(isOnWechatCalling(accessibilityEvent)){
            Log.d(TAG, "onAccessibilityEvent: isOnWechatCalling");
            mState = ChatState.STATE_CALLING;
        }
    }

    private boolean isOnLauncher(AccessibilityEvent accessibilityEvent){
        if(!accessibilityEvent.getPackageName().equals("com.android.launcher4")){
            return false;
        }

        return accessibilityEvent.getText().toString().contains("主屏幕");
    }

    private boolean isOnLauncherAppList(AccessibilityEvent accessibilityEvent){
        if(!accessibilityEvent.getPackageName().equals("com.android.launcher4")){
            return false;
        }

        return accessibilityEvent.getText().toString().contains("应用");
    }

    private boolean isOnWechatList(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }

        if(accessibilityEvent.getPackageName().equals("com.tencent.mm")){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("通讯录");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("发现");

            if(nodes1.size() != 0 && nodes2.size() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnWechatChat(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }
        if(accessibilityEvent.getClassName().equals(WECHAT_LIST_ACTIVITY)){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("视频通话");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("拍摄");

            if(nodes1.size() == 0 || nodes2.size() == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnWechatTalking(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }
        if(accessibilityEvent.getClassName().equals(WECHAT_CALLING_ACTIVITY)){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("挂断");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("转换摄像头");
            List<AccessibilityNodeInfo> nodes3 =
                    eventSource.findAccessibilityNodeInfosByText("切到语音通话");

            if(nodes1.size() != 0 && nodes2.size() != 0 && nodes3.size() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnWechatCalling(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }
        if(accessibilityEvent.getClassName().equals(WECHAT_CALLING_ACTIVITY)){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("切到语音通话");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("取消");

            if(nodes1.size() != 0 && nodes2.size() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnWechatBeCalling(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }
        if(accessibilityEvent.getClassName().equals(WECHAT_CALLING_ACTIVITY)){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("挂断");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("接听");
            List<AccessibilityNodeInfo> nodes3 =
                    eventSource.findAccessibilityNodeInfosByText("切到语音接听");

            if(nodes1.size() != 0 && nodes2.size() != 0 && nodes3.size() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnWechatBeSoundCalling(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }
        if(accessibilityEvent.getClassName().equals(WECHAT_CALLING_ACTIVITY)){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("挂断");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("接听");
            List<AccessibilityNodeInfo> nodes3 =
                    eventSource.findAccessibilityNodeInfosByText("邀请你进行语音通话");

            if(nodes1.size() != 0 && nodes2.size() != 0 && nodes3.size() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnWechatChatOption(AccessibilityEvent accessibilityEvent){
        AccessibilityNodeInfo eventSource = accessibilityEvent.getSource();
        if(eventSource == null){
            return false;
        }
        if(accessibilityEvent.getPackageName().equals("com.tencent.mm")){
            List<AccessibilityNodeInfo> nodes1 =
                    eventSource.findAccessibilityNodeInfosByText("视频通话");
            List<AccessibilityNodeInfo> nodes2 =
                    eventSource.findAccessibilityNodeInfosByText("语音通话");

            if(nodes1.size() != 0 && nodes2.size() != 0) {
                return true;
            }
        }
        return false;
    }

}
