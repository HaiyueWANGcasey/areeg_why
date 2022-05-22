package com.penglab.hi5.data.dataStore;

import android.content.Context;
import android.content.SharedPreferences;

public class S2paraSetting {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Context mContext;

    public S2paraSetting(Context context){
        mContext = context;
        pref = mContext.getSharedPreferences("s2initialization",Context.MODE_PRIVATE);
    }

    public void setPara(boolean Connect_ServerMode,boolean Connect_ScopeMode, boolean Smart_ControlMode, int ParaXY, int ParaZ){
        editor = pref.edit();
        editor.putBoolean("Connect_ServerMode",Connect_ServerMode);
        editor.putBoolean("Connect_ScopeMode",Connect_ScopeMode);
        editor.putBoolean("Smart_ControlMode",Smart_ControlMode);
        editor.putInt("ParaXY",ParaXY);
        editor.putInt("ParaZ",ParaZ);
        editor.apply();

    }

    public void setTag(int img_score, int swc_score,String id ,String name,String note){
        editor = pref.edit();

        editor.putInt("image_quality_score",img_score);
        editor.putInt("swc_quality_score",swc_score);
        editor.putString("user_id",id);
        editor.putString("file_name",name);
        editor.putString("notes",note);
        editor.apply();

    }

    public boolean getConnect_ServerMode(){
        return pref.getBoolean("Connect_ServerMode",false);
    }

    public int getImage_quality_score(){
        return pref.getInt("image_quality_score",0);
    }
    public int getSwc_quality_score(){
        return pref.getInt("swc_quality_score",0);
    }
    public String getUser_id(){
        return pref.getString("user_id", String.valueOf(0));
    }
    public String getFile_name(){
        return pref.getString("file_name",String.valueOf(0));
    }
    public String getNotes(){
        return pref.getString("notes",String.valueOf(0));
    }

    public boolean getConnect_ScopeMode(){
        return pref.getBoolean("Connect_ScopeMode",false);
    }

    public boolean getSmart_ControlMode(){
        return pref.getBoolean("Smart_ControlMode",false);
    }

    public int getParaXY(){
        return pref.getInt("ParaXY",0);
    }

    public int getParaZ(){
        return pref.getInt("ParaZ",0);
    }
}
