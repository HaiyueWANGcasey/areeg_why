package com.penglab.hi5.data;

import static com.penglab.hi5.core.ui.splash.SplashScreenViewModel.MUSIC_DOWNLOAD_FINISHED;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.netease.nim.uikit.common.util.C;
import com.penglab.hi5.basic.utils.FileHelper;
import com.penglab.hi5.core.Myapplication;
import com.penglab.hi5.core.net.HttpUtilsImage;
import com.penglab.hi5.core.net.HttpUtilsResource;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ResourceDataSource {

    private final String TAG = "ResourceDataSource";
    private final MutableLiveData<Result> result = new MutableLiveData<>();
    private String responseData;

    public LiveData<Result> getResult() {
        return result;
    }

    public void getMusicList() {
        try {
            HttpUtilsResource.getMusicListWithOkHttp(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    result.postValue(new Result.Error(new Exception("Connect Failed When Get Music List")));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        int responseCode = response.code();
                        if (responseCode == 200){
                            if (response.body() != null) {
                                responseData = response.body().string();
                                JSONArray jsonArray = new JSONArray(responseData);
                                result.postValue(new Result.Success<JSONArray>(jsonArray));
                            } else {
                                result.postValue(new Result.Error(new Exception("Response from server is null !")));
                            }
                        } else {
                            result.postValue(new Result.Error(new Exception("Fail to get music list !")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result.postValue(new Result.Error(new Exception("Fail to get music list !")));
                    }
                }
            });
        } catch (Exception exception) {
            result.postValue(new Result.Error(new IOException("Check the network please !", exception)));
        }
    }

    public void downloadMusic(String musicName) {
        try {
            HttpUtilsResource.downloadMusicWithOkHttp(musicName, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    result.postValue(new Result.Error(new Exception("Connect Failed When Download Music")));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.body() != null) {
                            byte[] fileContent = response.body().bytes();
                            if (!FileHelper.storeFile(Myapplication.getContext().getExternalFilesDir(null) + "/Resources/Music", musicName, fileContent)) {
                                Log.e(TAG, "Fail to store music");
                            }

                            result.postValue(new Result.Success<String>(MUSIC_DOWNLOAD_FINISHED));

                        } else {
                            result.postValue(new Result.Error(new Exception("Response from server is null when download music !")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.postValue(new Result.Error(new Exception("Fail to download music file !")));
                    }
                }
            });
        } catch (Exception exception) {
            result.postValue(new Result.Error(new IOException("Check the network please !", exception)));
        }
    }
}
