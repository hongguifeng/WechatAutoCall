 package com.example.wechatautocall;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import java.util.List;

 public class MainActivity extends AppCompatActivity
         implements AccessibilityManager.AccessibilityStateChangeListener{

     private TextView pluginStatusText;

     private AccessibilityManager accessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pluginStatusText = findViewById(R.id.layout_control_accessibility_text);

        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);

        updateServiceStatus();
    }

     @Override
     public void onAccessibilityStateChanged(boolean b) {
         updateServiceStatus();
     }

     private void updateServiceStatus() {
         if (isServiceEnabled()) {
             pluginStatusText.setText(R.string.service_off);
         } else {
             pluginStatusText.setText(R.string.service_on);
         }
     }

     private boolean isServiceEnabled() {
         List<AccessibilityServiceInfo> accessibilityServices =
                 accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
         for (AccessibilityServiceInfo info : accessibilityServices) {
             if (info.getId().equals(getPackageName() + "/.WeChatService")) {
                 return true;
             }
         }
         return false;
     }
 }
