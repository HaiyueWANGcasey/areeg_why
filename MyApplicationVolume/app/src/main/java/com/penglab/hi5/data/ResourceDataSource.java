package com.penglab.hi5.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.penglab.hi5.basic.utils.FileHelper;
import com.penglab.hi5.core.Myapplication;
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
                        if (response.body() != null) {
                            JSONArray jsonArray = new JSONArray(response.body().string());
                            result.setValue(new Result.Success<JSONArray>(jsonArray));
                        } else {
                            result.setValue(new Result.Error(new Exception("Response from server is null !")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result.setValue(new Result.Error(new Exception("Fail to get music list !")));
                    }
                }
            });
        } catch (Exception exception) {
            result.postValue(new Result.Error(new IOException("Check the network please !", exception)));
        }
    }

    public void downloadMusic(String name, String url, int index, int sum) {
        try {
            HttpUtilsResource.downloadMusicWithOkHttp(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    result.postValue(new Result.Error(new Exception("Connect Failed When Download Music")));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.body() != null) {
                            byte[] fileContent = response.body().bytes();
                            FileHelper.storeFile(Myapplication.getContext().getExternalFilesDir(null) + "/Resources/Music", name, fileContent);
                            if (index == sum){
                                result.setValue(new Result.Success<String>("download "));
                            }
                        } else {
                            result.setValue(new Result.Error(new Exception("Response from server is null when download music !")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.setValue(new Result.Error(new Exception("Fail to download music file !")));
                    }
                }
            });
        } catch (Exception exception) {
            result.postValue(new Result.Error(new IOException("Check the network please !", exception)));
        }
    }
}
