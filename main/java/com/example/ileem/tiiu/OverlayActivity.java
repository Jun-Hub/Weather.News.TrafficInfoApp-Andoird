
package com.example.ileem.tiiu;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by ileem on 2016-11-17.
 */
public class OverlayActivity extends AppCompatActivity {

    private static final String TAG = OverlayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        // 상태바 숨기기
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//        // Remember that you should never show the action bar if the
//        // status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // fragment表示
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentType.OVERLAY.getTag());
        if (fragment == null) {
            fragment = Activity1_1.newInstance();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment, FragmentType.OVERLAY.getTag());
        ft.commit();
    }

    private enum FragmentType {
        OVERLAY("overlay");
        private String tag;

        private FragmentType(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
            finish();
    }

}
