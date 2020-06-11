package com.example.myapplication__volume;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.RemoteImg;

import java.io.InputStream;

import static com.example.ImageFile.BigFileReader.BIG_LOCAL_FILE_PATH;


//打开文件管理器读取文件
public class JumpActivity extends AppCompatActivity {

    //message 字符串用于传递文件的路径到Mainactivity中
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String Out_of_memory = "com.example.myfirstapp.MESSAGE";
    public static final String Timeout = "Timeout-MESSAGE";
    public static final String FILE_LOCAL = "com.example.myfirstapp.MESSAGE";


    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    private static Context context;

    private InputStream is;
    private int length;
    private ManageSocket manageSocket;
    private RemoteImg remoteImg;
    private BroadcastReceiver broadcastReceiver;

    private boolean select_img = false;
//    private boolean select_img = true;


    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent4 = getIntent();
        String filepath_local = intent4.getStringExtra(BIG_LOCAL_FILE_PATH);

        if (filepath_local != null){
            Log.v("JumpActivity: ", "filepath_local: " + filepath_local);
            Intent intent_file_local = new Intent(this, MainActivity.class);
            intent_file_local.putExtra(MyRenderer.LOCAL_FILE_PATH, filepath_local);
            startActivity(intent_file_local);
            return;
        }

        //接受从fileactivity传递过来的文件路径
        Intent intent1 = getIntent();
        String filepath = intent1.getStringExtra(JumpActivity.EXTRA_MESSAGE);

        if (filepath != null){
            Intent intent_file = new Intent(this, MainActivity.class);
            Log.v("JumpActivity", "filpath: " + filepath);
            intent_file.putExtra(MyRenderer.FILE_PATH, filepath);
            startActivity(intent_file);
            return;
        }

        Intent intent2 = getIntent();
        String MSG = intent2.getStringExtra(JumpActivity.Out_of_memory);

        if (MSG != null){
            Log.v("JumpActivity", MSG);
            Intent intent_outofmem = new Intent(this, MainActivity.class);
            intent_outofmem.putExtra(MyRenderer.OUTOFMEM_MESSAGE, MSG);
            startActivity(intent_outofmem);
            return;
        }

        Intent intent3 = getIntent();
        String Timeout = intent3.getStringExtra(JumpActivity.Timeout);

        if (Timeout != null){
            Log.v("JumpActivity", Timeout);
            Intent intent_timeout = new Intent(this, MainActivity.class);
            intent_timeout.putExtra(MyRenderer.Time_out, Timeout);
            startActivity(intent_timeout);
            return;
        }





    }

    //renderer 的生存周期和activity保持一致
    @Override
    protected void onPause() {
        super.onPause();
        Log.v("onPause", "---------start------------");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("onResume", "---------start------------");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

}
