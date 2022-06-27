package com.penglab.hi5.core.ui.collaboration;

import static com.penglab.hi5.core.Myapplication.ToastEasy;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.huawei.hms.support.api.PendingResultImpl;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.penglab.hi5.basic.image.XYZ;
import com.penglab.hi5.core.ui.QualityInspection.QualityInspectionViewModel;
import com.penglab.hi5.core.ui.ResourceResult;
import com.penglab.hi5.core.ui.marker.CoordinateConvert;
import com.penglab.hi5.data.CollorationDataSource;
import com.penglab.hi5.data.ImageDataSource;
import com.penglab.hi5.data.ImageInfoRepository;
import com.penglab.hi5.data.QualityInspectionDataSource;
import com.penglab.hi5.data.Result;
import com.penglab.hi5.data.UserInfoRepository;
import com.penglab.hi5.data.model.img.CollaborateNeuronInfo;
import com.penglab.hi5.data.model.user.LoggedInUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CollaborationViewModel extends ViewModel {
    private final String TAG = "CollaborationViewModel";

    private MutableLiveData<Integer> portNum = new MutableLiveData<>();
    private MutableLiveData<Integer> ano = new MutableLiveData<>();


    private Context mainContext;


    private final int DEFAULT_IMAGE_SIZE = 128;
    private final int DEFAULT_RES_INDEX = 2;
    private boolean isDownloading = false;
    private boolean noFileLeft = false;

    private final HashMap<String, String> resMap = new HashMap<>();
    private volatile CollaborateNeuronInfo potentialDownloadNeuronInfo = new CollaborateNeuronInfo();
    private final CoordinateConvert coordinateConvert = new CoordinateConvert();
    private final CoordinateConvert downloadCoordinateConvert = new CoordinateConvert();



    public CollaborationViewModel(UserInfoRepository userInfoRepository, ImageInfoRepository imageInfoRepository, ImageDataSource imageDataSource, CollorationDataSource collorationDataSource) {
        this.userInfoRepository = userInfoRepository;
        this.imageInfoRepository = imageInfoRepository;
        this.imageDataSource = imageDataSource;
        this.collorationDataSource = collorationDataSource;
    }

    public enum AnnotationMode{
        BIG_DATA, NONE
    }

    private final UserInfoRepository userInfoRepository;
    private final ImageInfoRepository imageInfoRepository;
    private final ImageDataSource imageDataSource;
    private final CollorationDataSource collorationDataSource;

    private final MutableLiveData<CollaborationViewModel.AnnotationMode> annotationMode = new MutableLiveData<>();
    public ImageDataSource getImageDataSource() {
        return imageDataSource;
    }
    public boolean isLoggedIn(){
        return userInfoRepository.isLoggedIn();
    }


    public CollorationDataSource getCollorationDataSource() {
        return collorationDataSource;
    }


    public void handleBrainNumber(String brainNumber) {
        Log.e(TAG,"handleBrainNumber"+brainNumber);
        potentialDownloadNeuronInfo.setBrainNumber(brainNumber);
        getNeuronList(brainNumber);
    }

    public LiveData<CollaborationViewModel.AnnotationMode> getAnnotationMode() {
        return annotationMode;
    }


    public void handleNeuronListResult(Result result){
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof CollaborateNeuronInfo) {
                potentialDownloadNeuronInfo = (CollaborateNeuronInfo) data;

                downloadCoordinateConvert.initLocation(potentialDownloadNeuronInfo.getLocation());
                if (resMap.isEmpty()) {
                    getBrainList();
                } else {
                    downloadImage();
                }
            }
        } else {
            ToastEasy(result.toString());
        }

    }

    public void handleAnoResult(Result result){
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof JSONObject) {
                JSONObject neuronListResult = (JSONObject) data;
                try {



                } catch (Exception e) {
                    ToastEasy("Fail to parse jsonArray when get user performance !");
                }
            }
        } else {
            ToastEasy(result.toString());
        }


    }

    public void handleLoadAnoResult (Result result){
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof JSONObject) {
                JSONObject neuronListResult = (JSONObject) data;
                try {
                    portNum.postValue(neuronListResult.getInt("port"));
                    ano.postValue(neuronListResult.getInt("ano"));

                } catch (Exception e) {
                    ToastEasy("Fail to parse jsonArray when get user performance !");
                }
            }
        } else {
            ToastEasy(result.toString());
        }


    }

    public void getImageList(){
        collorationDataSource.getImageList();
    }

    public void getNeuronList(String brainNumber) {
        collorationDataSource.getNeuron(brainNumber);
    }

    private void getBrainList() {
        imageDataSource.getBrainList();
    }

    public void downloadImage() {
        String brainId = potentialDownloadNeuronInfo.getBrainName();
        XYZ loc = downloadCoordinateConvert.getCenterLocation();
        String res = resMap.get(brainId);
        if (res == null){
            ToastEasy("Fail to download image, something wrong with res list !");
            isDownloading = false;
            return;
        }
        imageDataSource.downloadImage(potentialDownloadNeuronInfo.getBrainName(), res, (int) loc.x , (int) loc.y, (int) loc.z, DEFAULT_IMAGE_SIZE);
    }














}
