package com.penglab.hi5.core.ui.home.screens;

import static com.penglab.hi5.core.MainActivity.ifGuestLogin;
import static com.penglab.hi5.core.Myapplication.ToastEasy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.google.android.material.navigation.NavigationView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.penglab.hi5.R;
import com.penglab.hi5.basic.utils.CrashHandler;
import com.penglab.hi5.basic.utils.CrashReports;
import com.penglab.hi5.core.BaseActivity;
import com.penglab.hi5.core.ui.login.LoginActivity;
import com.penglab.hi5.core.ui.home.adapters.MainPagerAdapter;
import com.penglab.hi5.dataStore.PreferenceLogin;

import java.io.File;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";
    private static final String USERNAME = "USERNAME";

    public static String username = null;
    private long exitTime = 0;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private Context homeContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeContext = this;
        username = getIntent().getStringExtra(USERNAME);

        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        setNavDrawer();

        // set up for cards view
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_main);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        // set up for the tab
        final NavigationTabStrip navigationTabStrip = (NavigationTabStrip) findViewById(R.id.nts);
//        navigationTabStrip.setTitles("HOW WE WORK", "WE WORK WITH");
        navigationTabStrip.setTitles("HOW WE WORK");
        navigationTabStrip.setViewPager(viewPager);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNavDrawer(){
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open_content_description,
                R.string.drawer_closed_content_description) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        navigationView = findViewById(R.id.nav_view);
        if (ifGuestLogin) {
            MenuItem accountItem = navigationView.getMenu().findItem(R.id.nav_account);
            accountItem.setTitle("Login");
        }
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_account:
                if (ifGuestLogin) {
                    login();
                } else
                    logout();
                break;
            case R.id.nav_settings:
                settings();
                break;
            case R.id.nav_about:
                about();
                break;
        }
        drawerLayout.closeDrawer(navigationView);
        return false;
    }

    /*
    Start this activity
     */
    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String username) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(USERNAME, username);
        context.startActivity(intent);
    }

    /*
    Press twice to exit app
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
                return false;
            } else {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    ToastEasy("Press again to exit the program");
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void logout() {
        new AlertDialog.Builder(homeContext)
                .setTitle("Log out")
                .setMessage("Are you sure to Log out?")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 清理缓存&注销监听&清除状态
                        NimUIKit.logout();
                        NIMClient.getService(AuthService.class).logout();

//                        AgoraMsgManager.getInstance().getRtmClient().logout(null);

                        PreferenceLogin preferenceLogin = PreferenceLogin.getInstance();
                        preferenceLogin.setPref(preferenceLogin.getUsername(), preferenceLogin.getPassword(), false, true);
                        // DemoCache.clear();

                        LoginActivity.start(HomeActivity.this);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    private void login() {
        new AlertDialog.Builder(homeContext)
                .setTitle("Log in")
                .setMessage("Are you sure to Log in?")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceLogin preferenceLogin = PreferenceLogin.getInstance();
                        preferenceLogin.setPref(preferenceLogin.getUsername(), preferenceLogin.getPassword(), false, true);
                        // DemoCache.clear();

                        LoginActivity.start(HomeActivity.this);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    private void settings(){
        new XPopup.Builder(this)
                .asCenterList("Settings", new String[]{"Crash Report", "Clean Img Cache"},
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                switch (text){
                                    case "Crash Report":
                                        shareCrashReports();
                                        break;
                                    case "Clean Img Cache":
                                        cleanImgCache();
                                        break;
                                    default:
                                        Log.e(TAG,"Something wrong in settings !");
                                }
                            }
                        })
                .show();
    }

    private void about() {
        new XPopup.Builder(this)
                .asConfirm("Hi5: VizAnalyze Big 3D Images", "By Peng lab @ BrainTell. \n\n" +

                                "Version: 20210803a 11:39 UTC+8 build",

                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                            }
                        })
                .setCancelText("Cancel")
                .setConfirmText("Confirm")
                .show();
    }

    /* share crash report */
    private void shareCrashReports(){
        CrashReports crashReports = CrashHandler.getCrashReportFiles(homeContext);
        new XPopup.Builder(this)
                .maxHeight(1350)
                .asCenterList("Select a Crash Report", crashReports.reportNames,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                if (!crashReports.isEmpty){
                                    String filePath = CrashHandler.getCrashFilePath(getApplicationContext()) + "/" + text + ".txt";
                                    File file = new File(filePath);
                                    if (file.exists()){
                                        Intent intent = new Intent();
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(homeContext, "com.penglab.hi5.provider", new File(filePath)));  //传输图片或者文件 采用流的方式
                                        intent.setType("*/*");   //分享文件
                                        startActivity(Intent.createChooser(intent, "Share From Hi5"));
                                    }else {
                                        ToastEasy("File does not exist");
                                    }
                                }
                            }
                        })
                .show();
    }

    /* clean img cache of Big Data */
    public void cleanImgCache(){
        new XPopup.Builder(this)
                .asConfirm("Clean the img cache", "Are you sure to CLEAN ALL IMG CACHE OF BIG DATA MODULE?",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                deleteImg();
                            }
                        })
                .setCancelText("Cancel")
                .setConfirmText("Confirm")
                .show();
    }

    private void deleteImg(){
        String imgPath = getExternalFilesDir(null).toString() + "/Img";
        File file = new File(imgPath);
        recursionDeleteFile(file);
    }
}


