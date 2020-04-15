package com.example.myapplication__volume;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.basic.Image4DSimple;
import com.example.basic.ImageMarker;
import com.example.basic.LocationSimple;
import com.example.basic.NeuronTree;
import com.feature_calc_func.MorphologyCalculate;
import com.tracingfunc.app2.ParaAPP2;
import com.tracingfunc.app2.V3dNeuronAPP2Tracing;
import com.tracingfunc.gd.CurveTracePara;
import com.tracingfunc.gd.V3dNeuronGDTracing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import cn.carbs.android.library.MDDialog;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


public class MainActivity extends AppCompatActivity {
    private MyGLSurfaceView myGLSurfaceView;
    private MyRenderer myrenderer;
    private static final String DEBUG_TAG = "Gestures";
    private static Context context;
    private long length;
    private InputStream is;
    private String filepath = "";
    private boolean ifPainting = false;
    private boolean ifPoint = false;
    private boolean ifImport = false;
    private boolean ifAnalyze = false;
    private boolean ifGDTracing = false;
    private boolean ifDeletingMarker = false;
    private boolean ifAnimation = false;
    private Button buttonAnimation;

    private int eswc_length;
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static int REQUEST_PERMISSION_CODE = 1;

    //    private int Paintmode = 0;
    private ArrayList<Float> lineDrawed = new ArrayList<Float>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //接受从fileactivity传递过来的文件路径
        Intent intent = getIntent();
        filepath = intent.getStringExtra(FileActivity.EXTRA_MESSAGE);

        myrenderer = new MyRenderer();
        myrenderer.SetPath(filepath);

//        Uri uri = Uri.parse((String) filepath);
//
//        try {
//            ParcelFileDescriptor parcelFileDescriptor =
//                    getContentResolver().openFileDescriptor(uri, "r");
//
//            is = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
//
//            length = (int)parcelFileDescriptor.getStatSize();
//            myrenderer.setInputStream(is);
//            myrenderer.setLength(length);
//
//        }catch (Exception e){
//            Log.v("MainActivity","Some problems in the MainActivity");
//        }


        Log.v("filepath-mainactivity", filepath);

        myGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(myGLSurfaceView);

        LinearLayout ll = new LinearLayout(this);
        HorizontalScrollView hs = new HorizontalScrollView(this);

        this.addContentView(hs, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        hs.addView(ll);

        final Button button_1 = new Button(this);
        button_1.setText("draw curve");
        ll.addView(button_1);

        final Button button_2 = new Button(this);
        button_2.setText("pinpoint");
        ll.addView(button_2);

        final Button button_3 = new Button(this);
        button_3.setText("load");
        ll.addView(button_3);

        final Button button_4 = new Button(this);
        button_4.setText("Analyze");
        ll.addView(button_4);

        final Button button_5 = new Button(this);
        button_5.setText("GD_Tracing");
        ll.addView(button_5);

        final Button buttonDeleteMarker = new Button(this);
        buttonDeleteMarker.setText("delete marker");
        ll.addView(buttonDeleteMarker);

        buttonAnimation = new Button(this);
        buttonAnimation.setText("Animation");
        ll.addView(buttonAnimation);


        button_1.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                ifPainting = !ifPainting;
                ifPoint = false;
                ifDeletingMarker = false;
                if(ifPainting) {
                    button_1.setTextColor(Color.RED);
                    button_2.setTextColor(Color.BLACK);
                    buttonDeleteMarker.setTextColor(Color.BLACK);
                }
                else {
                    button_1.setTextColor(Color.BLACK);
                }
            }
        });

        button_2.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                ifPoint = !ifPoint;
                ifPainting = false;
                ifDeletingMarker = false;
                if(ifPoint) {
                    button_2.setTextColor(Color.RED);
                    button_1.setTextColor(Color.BLACK);
                    buttonDeleteMarker.setTextColor(Color.BLACK);
                }
                else {
                    button_2.setTextColor(Color.BLACK);
                }
            }
        });

        button_3.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!ifImport) {

                    ifImport = !ifImport;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");    //设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        button_4.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");    //设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                ifAnalyze = true;
            }
        });

        button_5.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v("Mainactivity", "GD-Tracing start~");
                    Toast.makeText(v.getContext(), "GD-Tracing start~", Toast.LENGTH_SHORT).show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            /**
                             * 延时执行的代码
                             */
                            try {
                                GDTraing();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    },1000); // 延时1秒
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonDeleteMarker.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                ifDeletingMarker = !ifDeletingMarker;
                ifPainting = false;
                ifPoint = false;
                if (ifDeletingMarker){
                    button_1.setTextColor(Color.BLACK);
                    button_2.setTextColor(Color.BLACK);
                    buttonDeleteMarker.setTextColor(Color.RED);
                }else{
                    buttonDeleteMarker.setTextColor(Color.BLACK);
                }
            }
        });

        buttonAnimation.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                ifAnimation = !ifAnimation;
                ifPainting = false;
                ifPoint = false;
                if (ifAnimation){
                    button_1.setTextColor(Color.BLACK);
                    button_2.setTextColor(Color.BLACK);
                    buttonDeleteMarker.setTextColor(Color.BLACK);
                    buttonAnimation.setTextColor(Color.RED);
                    myGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                    myrenderer.setAnimation();
                }else{
                    buttonAnimation.setTextColor(Color.BLACK);
                    myrenderer.setAnimation();
                    myGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                }
            }
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        context = getApplicationContext();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.v("MainActivity", "read the eswc now~~~~~~~~~~~~~~~~");

        if (resultCode == RESULT_OK) {
            data.getDataString();
            Uri uri = data.getData();
//            Uri uri_old = data.getData();

            String filePath = uri.toString();
//            String filePath= getpath(this, uri);

            String filePath_getPath = uri.getPath();

//            String filePath = uri.getPath().substring(14);
//            String filePath = Uri2PathUtil.getRealPathFromUri(getApplicationContext(), uri);
//            String filePath = FilePath.substring(14);
//            String filePath = "/storage/emulated/0/Download/image.v3draw";


            Log.v("MainActivity", filePath);
            Log.v("uri.getPath()", filePath_getPath);
            Log.v("Uri_Scheme:", uri.getScheme());

//            Log.v("Uri_Scheme:", DocumentsContract.getDocumentId(uri));

//            Uri uri_old = data.getData();

//            Toast.makeText(this, "Open" + filePath + "--successfully", Toast.LENGTH_SHORT).show();

            try {

                Log.v("MainActivity", "123456");
//                Log.v("MainActivity",String.valueOf(fileSize));

//                Uri uri_1 = Uri.parse((String) filePath);
//                Log.v("Uri_1: ", uri_1.toString());


                //用于测试能不能修改uri
//                String uri_string = uri_old.toString();
//                int index = uri_string.lastIndexOf("/");
//
//                String filePath = uri_string.substring(0, index+1) + "1pic1.v3draw.apo";
//
//                Uri uri = Uri.parse((String) filePath);


                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(uri, "r");
                is = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
                int length = (int) parcelFileDescriptor.getStatSize();

                Log.v("Legth: ", Integer.toString(length));

//                is = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
//
//                eswc_length = (int)parcelFileDescriptor.getStatSize();
//
//                Log.v("Legth: ", Integer.toString(eswc_length));
//
//                ArrayList<ArrayList<Float>> eswc = new ArrayList<ArrayList<Float>>();
//                EswcReader eswcReader = new EswcReader();
//
//                eswc = eswcReader.read(eswc_length, is);
//
//                myrenderer.importEswc(eswc);

                if (ifImport) {

                    String filetype = filePath.substring(filePath.lastIndexOf(".")).toUpperCase();
                    Log.v("load file", filetype);

                    switch (filetype) {
                        case ".APO":
                            Log.v("Mainctivity", uri.toString());
                            ArrayList<ArrayList<Float>> apo = new ArrayList<ArrayList<Float>>();
                            ApoReader apoReader = new ApoReader();
                            apo = apoReader.read(uri);
                            myrenderer.importApo(apo);
                            break;
                        case ".SWC":
                            ArrayList<ArrayList<Float>> swc = new ArrayList<ArrayList<Float>>();
                            SwcReader swcReader = new SwcReader();
                            swc = swcReader.read(uri);
                            myrenderer.importSwc(swc);
                            break;
                        case ".ANO":
                            ArrayList<ArrayList<Float>> ano_swc = new ArrayList<ArrayList<Float>>();
                            ArrayList<ArrayList<Float>> ano_apo = new ArrayList<ArrayList<Float>>();
                            AnoReader anoReader = new AnoReader();
                            SwcReader swcReader_1 = new SwcReader();
                            ApoReader apoReader_1 = new ApoReader();
                            anoReader.read(uri);

//                        Uri swc_uri = anoReader.getSwc_result();
//                        Uri apo_uri = anoReader.getApo_result();

                            String swc_path = anoReader.getSwc_Path();
                            String apo_path = anoReader.getApo_Path();

//                        String swc_path = getPath(this, swc_uri);
//                        String apo_path = getPath(this, apo_uri);


////                        requestPermissions(PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
//                        grantUriPermission(getPackageName(), swc_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        ParcelFileDescriptor parcelFileDescriptor_swc =
//                                getContentResolver().openFileDescriptor(swc_uri, "r");
//                        InputStream is_swc = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor_swc);
//                        int length_swc = (int)parcelFileDescriptor_swc.getStatSize();
//
//                        Log.v("Length of swc: ", Integer.toString(length_swc));
//
//
////                        requestPermissions(PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
//                        grantUriPermission(getPackageName(), apo_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        ParcelFileDescriptor parcelFileDescriptor_apo =
//                                getContentResolver().openFileDescriptor(apo_uri, "r");
//                        InputStream is_apo = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor_apo);
//                        int length_apo = (int)parcelFileDescriptor_apo.getStatSize();
//
//                        Log.v("Length of apo: ", Integer.toString(length_apo));


//                        ano_swc = swcReader_1.read(length_swc, is_swc);
//                        ano_apo = apoReader_1.read(length_apo, is_apo);

//                        ano_swc = swcReader_1.read(swc_uri);
//                        ano_apo = apoReader_1.read(apo_uri);

                            ano_swc = swcReader_1.read(swc_path);
                            ano_apo = apoReader_1.read(apo_path);
                            myrenderer.importSwc(ano_swc);
                            myrenderer.importApo(ano_apo);
                            break;
                        case ".ESWC":
                            ArrayList<ArrayList<Float>> eswc = new ArrayList<ArrayList<Float>>();
                            EswcReader eswcReader = new EswcReader();

                            eswc = eswcReader.read(length, is);
                            myrenderer.importEswc(eswc);
                            break;

                        default:
                            Toast.makeText(this, "do not support this file", Toast.LENGTH_SHORT).show();

                    }

                }


                if (ifAnalyze) {
                    MorphologyCalculate morphologyCalculate = new MorphologyCalculate();
                    double[] features = morphologyCalculate.Calculate(uri);
                    if (features != null) displayResult(features);
                }

//
//                ArrayList<ArrayList<Float>> swc = new ArrayList<ArrayList<Float>>();
//                SwcReader swcReader = new SwcReader();
//
//                swc = swcReader.read(uri);
////                apo = apoReader.read(filePath);
//
//                myrenderer.importSwc(swc);

//                ArrayList<ArrayList<Float>> swc = new ArrayList<ArrayList<Float>>();
//                ArrayList<ArrayList<Float>> apo = new ArrayList<ArrayList<Float>>();
//                AnoReader anoReader = new AnoReader();
//
//                anoReader.read(uri);
//
//                swc = anoReader.getSwc_result();
//                apo = anoReader.getApo_result();
////                apo = apoReader.read(filePath);
//
//                myrenderer.importSwc(swc);
//                myrenderer.importApo(apo);


//                File f = new File(filePath);
//                FileInputStream fid = new FileInputStream(f);

//                fid.write(message.getBytes());
//                long fileSize = f.length();
            } catch (Exception e) {
                Toast.makeText(this, " dddddd  ", Toast.LENGTH_SHORT).show();
                Log.v("MainActivity", "111222");
                Log.v("Exception", e.toString());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void GDTraing() throws Exception {
        Image4DSimple img = myrenderer.getImg();
        if (!img.valid()) {
            Log.v("GDTracing", "Please load img first!");
        }
        ArrayList<ImageMarker> markers = myrenderer.getMarkerList();
        if (markers.size() <= 1) {
            Log.v("GDTracing", "Please get two marker at least!");
        }
        LocationSimple p0 = new LocationSimple(markers.get(0).x, markers.get(0).y, markers.get(0).z);
        Vector<LocationSimple> pp = new Vector<LocationSimple>();
        for (int i = 1; i < markers.size(); i++) {
            LocationSimple p = new LocationSimple(markers.get(i).x, markers.get(i).y, markers.get(i).z);
            pp.add(p);
        }

        NeuronTree outswc = new NeuronTree();
        int[] sz = new int[]{(int) img.getSz0(), (int) img.getSz1(), (int) img.getSz2(), (int) img.getSz3()};
        CurveTracePara curveTracePara = new CurveTracePara();


        try {
            outswc = V3dNeuronGDTracing.v3dneuron_GD_tracing(img.getDataCZYX(), sz, p0, pp, curveTracePara, 1.0);
//            ParaAPP2 p = new ParaAPP2();
//            p.p4dImage = img;
//            p.xc0 = p.yc0 = p.zc0 =0;
//            p.xc1 =(int) p.p4dImage.getSz0()-1;
//            p.yc1 =(int) p.p4dImage.getSz1()-1;
//            p.zc1 =(int) p.p4dImage.getSz2()-1;
//            p.landmarks = new LocationSimple[markers.size()];
//            for(int i=0;i<markers.size();i++){
//                p.landmarks[i] = new LocationSimple(markers.get(i).z, markers.get(i).y, markers.get(i).x);
//            }
//            System.out.println("---------------start---------------------");
//            p.outswc_file = "/storage/emulated/0/Download/app2.swc";
//            V3dNeuronAPP2Tracing.proc_app2(p);
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Looper.loop();
        }
//        String filePath = "/storage/emulated/0/Download/3.swc";
//        outswc.writeSWC_file(filePath);
//
//
        ArrayList<ArrayList<Float>> swc = new ArrayList<ArrayList<Float>>();
        for (int i = 0; i < outswc.listNeuron.size(); i++) {
            ArrayList<Float> s = new ArrayList<Float>();
            s.add((float) outswc.listNeuron.get(i).n);
            s.add((float) outswc.listNeuron.get(i).type);
            s.add((float) outswc.listNeuron.get(i).x);
            s.add((float) outswc.listNeuron.get(i).y);
            s.add((float) outswc.listNeuron.get(i).z);
            s.add((float) outswc.listNeuron.get(i).radius);
            s.add((float) outswc.listNeuron.get(i).parent);
            swc.add(s);
        }
        Log.v("MainActivity--GD", Integer.toString(swc.size()));

        Looper.prepare();
        Toast.makeText(this, "GD-Tracing finish, size of result swc: " + Integer.toString(swc.size()), Toast.LENGTH_SHORT).show();
        myrenderer.importSwc(swc);
        myGLSurfaceView.requestRender();
        Looper.loop();

    }

    /**
     * display the result of morphology calculate
     *
     * @param result the features of result
     */

    @SuppressLint("DefaultLocale")
    private void displayResult(final double[] result) {


        final String[] title = {
                "number of nodes",
                "soma surface",
                "number of stems",
                "number of bifurcations",
                "number of branches",
                "number of tips",
                "overall width",
                "overall height",
                "overall depth",
                "average diameter",
                "total length",
                "total surface",
                "total volume",
                "max euclidean distance",
                "max path distance",
                "max branch order",
                "average contraction",
                "average fragmentation",
                "average parent-daughter ratio",
                "average bifurcation angle local",
                "average bifurcation angle remote",
                "Hausdorff dimension"
        };

        final int[] id_title = new int[]{R.id.title0, R.id.title1, R.id.title2, R.id.title3, R.id.title4,
                R.id.title5, R.id.title6, R.id.title7, R.id.title8, R.id.title9,
                R.id.title10, R.id.title11, R.id.title12, R.id.title13, R.id.title14,
                R.id.title15, R.id.title16, R.id.title17, R.id.title18, R.id.title19,
                R.id.title20, R.id.title21};

        final int[] id_content = new int[]{R.id.content0, R.id.content1, R.id.content2, R.id.content3, R.id.content4,
                R.id.content5, R.id.content6, R.id.content7, R.id.content8, R.id.content9,
                R.id.content10, R.id.content11, R.id.content12, R.id.content13, R.id.content14,
                R.id.content15, R.id.content16, R.id.content17, R.id.content18, R.id.content19,
                R.id.content20, R.id.content21};


//        new XPopup.Builder(this)
////                .maxWidth(960)
//                .maxHeight(1350)
//                .asBottomList("Global features of the neuron", result_display,
//                        new OnSelectListener() {
//                            @Override
//                            public void onSelect(int position, String text) {
////                                toast("click " + text);
//                            }
//                        })
//                .show();


        MDDialog mdDialog = new MDDialog.Builder(this)
                .setContentView(R.layout.analysis_result)
                .setContentViewOperator(new MDDialog.ContentViewOperator() {
                    @Override
                    public void operate(View contentView) {//这里的contentView就是上面代码中传入的自定义的View或者layout资源inflate出来的view

                        String result_str;
                        int num = 2;
                        for (int i = 0; i < 22; i++) {
                            TextView tx = contentView.findViewById(id_title[i]);
                            tx.setText(title[i]);

                            TextView ct = contentView.findViewById(id_content[i]);
                            if (title[i].substring(0, 6).equals("number") || title[i].substring(0, 6).equals("max br")) {
                                result_str = ": " + String.format("%d", (int) result[i]);
                            } else {
                                num = Value_Display_Length(result[i]);
                                result_str = ": " + String.format("%." + String.format("%d", num) + "f", (float) result[i]);
                            }
                            ct.setText(result_str);

                        }
                    }
                })
                .setTitle("Global features of the neuron")
                .create();

        mdDialog.show();
        mdDialog.getWindow().setLayout(1000, 1500);

    }

    /**
     * format output of morphology feature's value
     *
     * @param value feature's value
     * @return Number of digits
     */
    private int Value_Display_Length(double value) {
        String s_value = (value + "").split("\\.")[0];
        int len = s_value.length();
        if (len >= 8) {
            return 0;
        } else if ((8 - len) > 4) {
            return 4;
        } else {
            return 8 - len;
        }
    }

    /**
     * add space at the end of string
     *
     * @param str   original string
     * @param count num of space
     * @return result string
     */
    private String AddSpace(String str, int count) {
        String result = str;
        for (int i = 0; i < count; i++) {
            result = result + " ";
        }
        return result;
    }


    private String getPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getpath(Context context, Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        String path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    String path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    String path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }


    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static Context getContext() {
        return context;
    }


    //renderer 的生存周期和activity保持一致
    @Override
    protected void onPause() {
        super.onPause();
        myGLSurfaceView.onPause();
        Log.v("onPause", "start-----");

    }

    @Override
    protected void onResume() {
        super.onResume();
        myGLSurfaceView.onResume();
        Log.v("Path", filepath);
        Log.v("onResume", "start-----");

//        myrenderer = new MyRenderer();

//        Uri uri = Uri.parse((String) filepath);
//
//        try {
//            ParcelFileDescriptor parcelFileDescriptor =
//                    getContentResolver().openFileDescriptor(uri, "r");
//
//            is = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
//
//            length = (int)parcelFileDescriptor.getStatSize();
//            myrenderer.setInputStream(is);
//            myrenderer.setLength(length);
//
//        }catch (Exception e){
//            Log.v("MainActivity","Some problems in the MainActivity");
//        }
//
//        Log.v("Load data successfully!", "here we are");
    }


    //opengl中的显示区域
    class MyGLSurfaceView extends GLSurfaceView {
        private float X, Y;
        private double dis_start;
        private boolean isZooming;


        public MyGLSurfaceView(Context context) {
            super(context);

            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ConfigurationInfo info = am.getDeviceConfigurationInfo();
            String v = info.getGlEsVersion(); //判断是否为3.0 ，一般4.4就开始支持3.0版本了。

            Log.v("MainActivity", "GLES-version: " + v);

            //设置一下opengl版本；
            setEGLContextClientVersion(3);

//            myrenderer.setLineDrawed(lineDrawed);

            setRenderer(myrenderer);


            //调用 onPause 的时候保存EGLContext
            setPreserveEGLContextOnPause(true);

            //当发生交互时重新执行渲染， 需要配合requestRender();
//            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        }

        //触摸屏幕的事件
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouchEvent(MotionEvent motionEvent) {

            //ACTION_DOWN不return true，就无触发后面的各个事件
            if (motionEvent != null) {
                final float normalizedX = toOpenGLCoord(this, motionEvent.getX(), true);
                final float normalizedY = toOpenGLCoord(this, motionEvent.getY(), false);
//
//                final float normalizedX =motionEvent.getX();
//                final float normalizedY =motionEvent.getY();

                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        X = normalizedX;
                        Y = normalizedY;
                        if (ifPainting) {
                            lineDrawed.add(X);
                            lineDrawed.add(Y);
                            lineDrawed.add(-1.0f);
                            myrenderer.setIfPainting(true);
                            requestRender();
                            Log.v("actionPointerDown", "Paintinggggggggggg");
                        }
                        if (ifPoint) {
                            Log.v("actionPointerDown", "Pointinggggggggggg");
                            myrenderer.setMarkerDrawed(X, Y);
                            Log.v("actionPointerDown", "(" + X + "," + Y + ")");
                            requestRender();

                        }
                        if (ifDeletingMarker){
                            Log.v("actionPointerDown", "DeletingMarker");
                            myrenderer.deleteMarkerDrawed(X, Y);
                            requestRender();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        isZooming = true;
                        float x1 = toOpenGLCoord(this, motionEvent.getX(1), true);
                        float y1 = toOpenGLCoord(this, motionEvent.getY(1), false);

//                        float x1=motionEvent.getX(1);
//                        float y1=motionEvent.getY(1);
                        dis_start = computeDis(normalizedX, x1, normalizedY, y1);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!ifPainting) {
                            if (isZooming) {
                                float x2 = toOpenGLCoord(this, motionEvent.getX(1), true);
                                float y2 = toOpenGLCoord(this, motionEvent.getY(1), false);

//                            float x2=motionEvent.getX(1);
//                            float y2=motionEvent.getY(1);
                                double dis = computeDis(normalizedX, x2, normalizedY, y2);
                                double scale = dis / dis_start;
                                myrenderer.zoom((float) scale);

                                //配合GLSurfaceView.RENDERMODE_WHEN_DIRTY使用
                                requestRender();
                                dis_start = dis;
                            } else {
                                myrenderer.rotate(normalizedX - X, normalizedY - Y, (float) (computeDis(normalizedX, X, normalizedY, Y)));

                                //配合GLSurfaceView.RENDERMODE_WHEN_DIRTY使用
                                requestRender();
                                X = normalizedX;
                                Y = normalizedY;
                            }
                        } else {
                            lineDrawed.add(normalizedX);
                            lineDrawed.add(normalizedY);
                            lineDrawed.add(-1.0f);

                            myrenderer.setLineDrawed(lineDrawed);
                            requestRender();

                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        isZooming = false;
                        X = normalizedX;
                        Y = normalizedY;
//                        if (ifPainting){
//                            lineDrawed.clear();
//                            myrenderer.setLineDrawed(lineDrawed);
//                            requestRender();
//
//                            myrenderer.setIfPainting(false);
//                            requestRender();
//
//                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isZooming = false;
                        if (ifPainting) {
                            myrenderer.setIfPainting(false);
                            myrenderer.addLineDrawed(lineDrawed);
                            lineDrawed.clear();
                            myrenderer.setLineDrawed(lineDrawed);

                            requestRender();
//                            requestRender();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
            return false;
        }


        //坐标系变换
        private float toOpenGLCoord(View view, float value, boolean isWidth) {
            if (isWidth) {
                return (value / (float) view.getWidth()) * 2 - 1;
            } else {
                return -((value / (float) view.getHeight()) * 2 - 1);
            }
        }


        //距离计算
        private double computeDis(float x1, float x2, float y1, float y2) {
            return sqrt(pow((x2 - x1), 2) + pow((y2 - y1), 2));
        }
    }

}
