package com.example.myapplication__volume.agora;

import com.netease.nimlib.sdk.auth.LoginInfo;

public class AVConfig {

    public enum  Status{
        FREE, PEERTOPEERVOICE, PEERTOPEERVIEDO, OPENVOICECALL, OPENVIDEOCALL
    }



    public static Status status;

    private static String userid;

    public static void init(){
        status = Status.FREE;
    }

    public static void init(LoginInfo loginInfo){

        status = Status.FREE;
        userid = loginInfo.getAccount();
    }


}
