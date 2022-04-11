package com.penglab.hi5.core.ui.QualityInspection;


import static com.penglab.hi5.core.Myapplication.ToastEasy;
import static com.penglab.hi5.data.MarkerFactoryDataSource.NO_MORE_FILE;
import static com.penglab.hi5.data.MarkerFactoryDataSource.UPLOAD_SUCCESSFULLY;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.penglab.hi5.basic.NeuronTree;
import com.penglab.hi5.basic.image.MarkerList;
import com.penglab.hi5.basic.image.XYZ;
import com.penglab.hi5.basic.utils.FileHelper;
import com.penglab.hi5.basic.utils.FileManager;
import com.penglab.hi5.core.Myapplication;
import com.penglab.hi5.core.render.utils.AnnotationDataManager;
import com.penglab.hi5.core.ui.ResourceResult;
import com.penglab.hi5.core.ui.check.CheckArborInfoState;
import com.penglab.hi5.core.ui.marker.CoordinateConvert;
import com.penglab.hi5.data.AnnotationDataSource;
import com.penglab.hi5.data.CheckArborDataSource;
import com.penglab.hi5.data.ImageDataSource;
import com.penglab.hi5.data.ImageInfoRepository;
import com.penglab.hi5.data.MarkerFactoryDataSource;
import com.penglab.hi5.data.QualityInspectionDataSource;
import com.penglab.hi5.data.Result;
import com.penglab.hi5.data.UserInfoRepository;
import com.penglab.hi5.data.model.img.FilePath;
import com.penglab.hi5.data.model.img.FileType;
import com.penglab.hi5.data.model.img.PotentialArborMarkerInfo;
import com.penglab.hi5.data.model.img.PotentialArborMarkerInfo;
import com.penglab.hi5.data.model.img.PotentialSomaInfo;
import com.penglab.hi5.data.model.user.LoggedInUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

/**
 * Created by Jackiexing on 01/10/21
 */
public class QualityInspectionViewModel extends ViewModel {

    private final String TAG = "QualityInspectionViewModel";
    private final int DEFAULT_IMAGE_SIZE = 128;
    private final int DEFAULT_RES_INDEX = 2;


    public enum AnnotationMode{
        BIG_DATA, NONE
    }

    public enum WorkStatus{
        IMAGE_FILE_EXPIRED, START_TO_DOWNLOAD_IMAGE, START_TO_DOWNLOAD_SWC,GET_ARBOR_MARKER_LIST_SUCCESSFULLY, GET_SWC_SUCCESSFULLY,UPLOAD_MARKERS_SUCCESSFULLY, NO_MORE_FILE, DOWNLOAD_IMAGE_FINISH, NONE
    }

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final MutableLiveData<AnnotationMode> annotationMode = new MutableLiveData<>();
    private final MutableLiveData<WorkStatus> workStatus = new MutableLiveData<>();
    private final MutableLiveData<MarkerList> syncMarkerList = new MutableLiveData<>();
    private final MutableLiveData<ResourceResult> imageResult = new MutableLiveData<>();
    private final MutableLiveData<ResourceResult> uploadResult = new MutableLiveData<>();
    private MutableLiveData<ResourceResult> annotationResult = new MutableLiveData<>();
    private MutableLiveData<NeuronTree> swcResult = new MutableLiveData<>();

    private final UserInfoRepository userInfoRepository;
    private final ImageInfoRepository imageInfoRepository;
    private final ImageDataSource imageDataSource;
    private final AnnotationDataSource annotationDataSource;
    private QualityInspectionDataSource qualityInspectionDataSource;

    private final LoggedInUser loggedInUser;
    private final CoordinateConvert coordinateConvert = new CoordinateConvert();
    private final CoordinateConvert lastDownloadCoordinateConvert = new CoordinateConvert();
    private final HashMap<String, String> resMap = new HashMap<>();
    private final List<PotentialArborMarkerInfo> potentialArborMarkerInfoList = new ArrayList<>();
    private volatile PotentialArborMarkerInfo curPotentialArborMarkerInfo;
    private volatile PotentialArborMarkerInfo lastDownloadPotentialArborMarkerInfo;
    private int curIndex = -1;
    private int lastIndex = -1;
    private boolean isDownloading = false;
    private boolean noFileLeft = false;



    public QualityInspectionViewModel(UserInfoRepository userInfoRepository, ImageInfoRepository imageInfoRepository, QualityInspectionDataSource qualityInspectionDataSource,ImageDataSource imageDataSource, AnnotationDataSource annotationDataSource) {
        this.userInfoRepository = userInfoRepository;
        this.imageInfoRepository = imageInfoRepository;
        this.qualityInspectionDataSource = qualityInspectionDataSource;
        this.imageDataSource = imageDataSource;
        this.loggedInUser = userInfoRepository.getUser();
        this.annotationDataSource = annotationDataSource;
        coordinateConvert.setResIndex(DEFAULT_RES_INDEX);
        coordinateConvert.setImgSize(DEFAULT_IMAGE_SIZE);
        lastDownloadCoordinateConvert.setResIndex(DEFAULT_RES_INDEX);
        lastDownloadCoordinateConvert.setImgSize(DEFAULT_IMAGE_SIZE);

        initPreDownloadThread();
        initCheckFreshThread();
    }

    public LiveData<AnnotationMode> getAnnotationMode(){
        return annotationMode;
    }

    public LiveData<WorkStatus> getWorkStatus() {
        return workStatus;
    }

    public LiveData<MarkerList> getSyncMarkerList() {
        return syncMarkerList;
    }

    public LiveData<ResourceResult> getImageResult() {
        return imageResult;
    }

    public LiveData<NeuronTree> getSwcResult() {
        return swcResult;
    }

    public MutableLiveData<ResourceResult> getUploadResult() {
        return uploadResult;
    }

    public ImageDataSource getImageDataSource() {
        return imageDataSource;
    }

    public QualityInspectionDataSource getQualityInspectionDataSource() {
        return qualityInspectionDataSource;
    }

    public boolean isLoggedIn(){
        return userInfoRepository.isLoggedIn();
    }

    public MutableLiveData<Integer> getObservableScore() {
        return userInfoRepository.getScoreModel().getObservableScore();
    }

    public PotentialArborMarkerInfo getCurPotentialArborMarkerInfo() {
        return curPotentialArborMarkerInfo;
    }

//    public void updateImageResult(Result result) {
//        if (result instanceof Result.Success){
//            Object data = ((Result.Success<?>) result).getData();
//            if (data instanceof JSONArray){
//                // process Brain List, store res info for each brain
//                JSONArray jsonArray = (JSONArray) data;
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    try {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        String imageId = jsonObject.getString("name");
//                        String detail = jsonObject.getString("detail");
//
//                        // parse brain info
//                        detail = detail.substring(1, detail.length() - 1);
//                        String [] rois = detail.split(", ");
//                        for (int j = 0; j < rois.length; j++) {
//                            rois[j] = rois[j].substring(1, rois[j].length() - 1);
//                        }
//                        if (rois.length >= 2){
//                            resMap.put(imageId, rois[1]);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                downloadImage();
//            } else if (data instanceof String){
//                potentialSomaInfoList.add(lastDownloadPotentialSomaInfo);
//                isDownloading = false;
//                if (workStatus.getValue() == WorkStatus.START_TO_DOWNLOAD_IMAGE) {
//                    workStatus.setValue(WorkStatus.GET_SOMA_LIST_SUCCESSFULLY);
//                }
//                // process image file after download
////                String fileName = FileManager.getFileName((String) data);
////                FileType fileType = FileManager.getFileType((String) data);
////                imageInfoRepository.getBasicImage().setFileInfo(fileName, new FilePath<String >((String) data), fileType);
////                imageResult.setValue(new ResourceResult(true));
//            }
//        } else if (result instanceof Result.Error){
//            // Fail to download image
//            if (result.toString().equals("Error: " + DOWNLOAD_IMAGE_FAILED)) {
//                if (curIndex == potentialSomaInfoList.size()-1) {
//                    curIndex--;
//                }
//                potentialSomaInfoList.remove(potentialSomaInfoList.size()-1);
//            }
//            isDownloading = false;
//            ToastEasy(result.toString());
//        }
//    }

    public void handleBrainListResult(Result result) {
        if (result == null) {
            isDownloading = false;
            Log.e(TAG,"handleBrainResult_equals_to_null");
        }
        if (result instanceof Result.Success) {
            Log.e(TAG,"begin to handle brain list result");
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof JSONArray){
                // process Brain List, store res info for each brain
                JSONArray jsonArray = (JSONArray) data;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String imageId = jsonObject.getString("name");
                        String detail = jsonObject.getString("detail");

                        // parse brain info
                        detail = detail.substring(1, detail.length() - 1);
                        String [] rois = detail.split(", ");
                        for (int j = 0; j < rois.length; j++) {
                            rois[j] = rois[j].substring(1, rois[j].length() - 1);
                        }
                        if (rois.length >= 2){
                            resMap.put(imageId, rois[1]);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                downloadImage();
            } else {
//                ToastEasy("Error with brain list");
                isDownloading = false;
            }
        } else {
//            ToastEasy(result.toString());
            isDownloading = false;
        }
    }

    public void handleDownloadImageResult(Result result) {
        if (result == null) {
            isDownloading = false;
        }
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof String){
                Log.e(TAG,"Download_Image_data"+data);
                potentialArborMarkerInfoList.add(lastDownloadPotentialArborMarkerInfo);
                isDownloading = false;
                if (workStatus.getValue() == WorkStatus.START_TO_DOWNLOAD_IMAGE) {
                    workStatus.setValue(WorkStatus.DOWNLOAD_IMAGE_FINISH);
                }
            } else {
//                ToastEasy("Error when download image");
                isDownloading = false;
            }
        } else {
//            ToastEasy(result.toString());
            isDownloading = false;
        }
    }

    public void handleDownloadSwcResult(Result result){
        if(result instanceof Result.Success){
            Log.e(TAG,"begin to handle download swc result");
            Object data =( (Result.Success<?>)result).getData();
            if(data instanceof String){
                String fileName = FileManager.getFileName((String) data);
                FileType fileType =FileManager.getFileType((String) data);
                imageInfoRepository.getBasicFile().setFileInfo(fileName,new FilePath<String>((String) data),fileType);
                FilePath filePath = new FilePath(Myapplication.getContext().getExternalFilesDir(null) + "/swc" +fileName);
                NeuronTree neuronTree = NeuronTree.parse(filePath);
                NeuronTree neuronTreeCoordinateConvert = NeuronTree.convertGlobalToLocal(neuronTree, coordinateConvert);
                if (neuronTreeCoordinateConvert == null) {
                    Log.e(TAG,"neuronTreeCoordinateConvert" + neuronTreeCoordinateConvert.listNeuron);
                }
                swcResult.setValue(neuronTreeCoordinateConvert);
                if(swcResult == null){
                    Log.e(TAG,"SWC RESULT IS NULL");
                }
                annotationResult.setValue( new ResourceResult(true));
                annotationMode.setValue(AnnotationMode.BIG_DATA);
//                workStatus.setValue(WorkStatus.GET_SWC_SUCCESSFULLY);
            }
            else {
                annotationResult.setValue(new ResourceResult(false,result.toString()));
                ToastEasy("failed to get swc successfully");
            }
        }
    }


    public void updateAnnotationResult(Result result) {
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof String) {
                String fileName = FileManager.getFileName((String) data);
                FileType fileType = FileManager.getFileType((String) data);
                imageInfoRepository.getBasicFile().setFileInfo(fileName, new FilePath<String >((String) data), fileType);
                annotationResult.setValue(new ResourceResult(true));
            } else {
                annotationResult.setValue(new ResourceResult(false, result.toString()));
            }
        } else {
            annotationResult.setValue(new ResourceResult(false, result.toString()));
        }
    }


    public MutableLiveData<ResourceResult> getAnnotationResult() {
        return annotationResult;
    }

    public void updateSomaResult(Result result) {
        if (result instanceof Result.Success){
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof PotentialSomaInfo){
                lastDownloadPotentialArborMarkerInfo = (PotentialArborMarkerInfo) data;
                lastDownloadCoordinateConvert.initLocation(lastDownloadPotentialArborMarkerInfo.getLocation());

                // get res list when first download img
                if (resMap.isEmpty()) {
                    getBrainList();
                } else {
                    downloadImage();
                }
            } else if (data instanceof MarkerList) {
                // get soma list successfully
                syncMarkerList.setValue(MarkerList.covertGlobalToLocal((MarkerList) data, coordinateConvert));
                annotationMode.setValue(AnnotationMode.BIG_DATA);
                workStatus.setValue(WorkStatus.GET_ARBOR_MARKER_LIST_SUCCESSFULLY);
            } else if (data instanceof String){
                String response = (String) data;
                if (response.equals(UPLOAD_SUCCESSFULLY)){
                    workStatus.setValue(WorkStatus.UPLOAD_MARKERS_SUCCESSFULLY);
                } else if (response.equals(NO_MORE_FILE)){
                    workStatus.setValue(WorkStatus.NO_MORE_FILE);
                }
            }
        } else if (result instanceof Result.Error){
            isDownloading = false;
            ToastEasy(result.toString());
        }
    }

    public void handlePotentialLocationResult(Result result) {
        Log.e(TAG, "start to handlePotentialLocationResult");
        if (result instanceof Result.Success) {
            Log.e(TAG,"handlePotentialLocationResultSuccess");
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof PotentialArborMarkerInfo){
                lastDownloadPotentialArborMarkerInfo = (PotentialArborMarkerInfo) data;
                lastDownloadCoordinateConvert.initLocation(lastDownloadPotentialArborMarkerInfo.getLocation());
                lastDownloadPotentialArborMarkerInfo.setCreatedTime(System.currentTimeMillis());

                // get res list when first download img
                if (resMap.isEmpty()) {
                    getBrainList();
                    Log.e(TAG,"getBrainListSuccess");
                } else {
                    downloadImage();
                    Log.e(TAG,"downloadImageSuccess");
                }
            } else if (data instanceof String && ((String) data).equals(NO_MORE_FILE)) {
                workStatus.setValue(WorkStatus.NO_MORE_FILE);
                noFileLeft = true;
                isDownloading = false;
            } else {
//                ToastEasy("Error with potential location");
                isDownloading = false;
            }
        } else {
//            ToastEasy(result.toString());
            isDownloading = false;
        }
    }

    public void handleMarkerListResult(Result result) {
        if (result == null) {
            ToastEasy("Result null when get soma list");
            return;
        }
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof MarkerList) {
                Log.e(TAG,"handle marker list result successfully");
                // get soma list successfully
                syncMarkerList.setValue(MarkerList.covertGlobalToLocal((MarkerList) data, coordinateConvert));
                annotationMode.setValue(AnnotationMode.BIG_DATA);
                workStatus.setValue(WorkStatus.GET_ARBOR_MARKER_LIST_SUCCESSFULLY);
            } else {
                ToastEasy("Error get soma list");
            }
        } else {
            ToastEasy(result.toString());
        }
    }

    public void handleUpdateSomaResult(Result result) {
        if (result == null) {
            return;
        }
        if (result instanceof Result.Success) {
            Object data = ((Result.Success<?>) result).getData();
            if (data instanceof String && ((String) data).equals(UPLOAD_SUCCESSFULLY)){
                uploadResult.setValue(new ResourceResult(true));
            } else {
                uploadResult.setValue(new ResourceResult(false));
            }
        } else {
            ToastEasy(result.toString());
        }
    }

//    public void openNewFile() {
//        getPotentialLocation();
//    }

    public void getArbor(){
        qualityInspectionDataSource.getArbor();
    }

    public void openNewFile() {
//        getArbor();
        noFileLeft = false;
        while (lastIndex < potentialArborMarkerInfoList.size() - 1) {
            PotentialArborMarkerInfo arborMarkerInfo = potentialArborMarkerInfoList.get(lastIndex + 1);
            if (arborMarkerInfo.ifStillFresh()) {
                break;
            }
            lastIndex++;
        }
        if (lastIndex + 1 >= potentialArborMarkerInfoList.size()) {
            workStatus.setValue(WorkStatus.START_TO_DOWNLOAD_IMAGE);
        } else {
            lastIndex++;
            curIndex = lastIndex;
            openFileWithCurIndex();
        }
    }

    void openFileWithNoIndex(){
        String brainId = lastDownloadPotentialArborMarkerInfo.getBrianId();
        String res = resMap.get(brainId);
        if (res == null) {
            imageResult.setValue(new ResourceResult(false, "No res found"));
            return;
        }
        XYZ location = lastDownloadCoordinateConvert.getCenterLocation();
        String filePath = Myapplication.getContext().getExternalFilesDir(null) + "/Image" +
                "/" + brainId + "_" + res + "_" + (int)location.x + "_" + (int)location.y + "_" + (int)location.z + ".v3dpbd";
        String fileName = FileManager.getFileName(filePath);
        Log.e(TAG,"filePath"+filePath);
        FileType fileType = FileManager.getFileType(filePath);
        imageInfoRepository.getBasicImage().setFileInfo(fileName, new FilePath<String >(filePath), fileType);
        imageResult.setValue(new ResourceResult(true));
    }

    private void openFileWithCurIndex() {
        curPotentialArborMarkerInfo = potentialArborMarkerInfoList.get(curIndex);
        coordinateConvert.initLocation(curPotentialArborMarkerInfo.getLocation());
        String brainId = curPotentialArborMarkerInfo.getBrianId();
        String res = resMap.get(brainId);
        if (res == null) {
            imageResult.setValue(new ResourceResult(false, "No res found"));
            return;
        }
        XYZ location = coordinateConvert.getCenterLocation();
        String filePath = Myapplication.getContext().getExternalFilesDir(null) + "/Image" +
                "/" + brainId + "_" + res + "_" + (int)location.x + "_" + (int)location.y + "_" + (int)location.z + ".v3dpbd";
        String fileName = FileManager.getFileName(filePath);
        FileType fileType = FileManager.getFileType(filePath);
        imageInfoRepository.getBasicImage().setFileInfo(fileName, new FilePath<String >(filePath), fileType);
        imageResult.setValue(new ResourceResult(true));
    }

    public void removeCurFileFromList() {
        if (curIndex >= 0 && curIndex < potentialArborMarkerInfoList.size()) {
            potentialArborMarkerInfoList.get(curIndex).setBoring(true);
        } else {
            ToastEasy("Something wrong with curIndex");
        }
    }

    public void previousFile() {
        int tempCurIndex = curIndex;
        while (tempCurIndex > 0) {
            if (!potentialArborMarkerInfoList.get(tempCurIndex - 1).isBoring()
                    && (potentialArborMarkerInfoList.get(tempCurIndex - 1).ifStillFresh()
                    || (potentialArborMarkerInfoList.get(tempCurIndex - 1).isAlreadyUpload()))) {
                break;
            }
            tempCurIndex--;
        }
        if (tempCurIndex == 0) {
            ToastEasy("You have reached the earliest image !");
        } else if (tempCurIndex <= potentialArborMarkerInfoList.size() - 1 && tempCurIndex > 0) {
            curIndex = tempCurIndex-1;
            openFileWithCurIndex();
        } else {
            ToastEasy("Something wrong with curIndex");
        }
    }

    public void nextFile() {
        int tempCurIndex = curIndex;
        while (tempCurIndex < potentialArborMarkerInfoList.size() - 1) {
            if (!potentialArborMarkerInfoList.get(tempCurIndex + 1).isBoring()
                    && (potentialArborMarkerInfoList.get(tempCurIndex + 1).ifStillFresh()
                    || (potentialArborMarkerInfoList.get(tempCurIndex + 1).isAlreadyUpload()))) {
                break;
            }
            tempCurIndex++;
        }
        if (tempCurIndex == potentialArborMarkerInfoList.size()-1) {
            // open new file
            openNewFile();
        } else if (tempCurIndex < potentialArborMarkerInfoList.size()-1 && tempCurIndex >= 0) {
            curIndex = tempCurIndex+1;
            if (curIndex > lastIndex) {
                lastIndex = curIndex;
            }
            openFileWithCurIndex();
        } else {
            ToastEasy("Something wrong with curIndex");
        }
    }

    private void getBrainList() {
        imageDataSource.getBrainList();
    }


    public void downloadImage() {

        String brianId = lastDownloadPotentialArborMarkerInfo.getBrianId();
        XYZ loc = lastDownloadCoordinateConvert.getCenterLocation();
        String res = resMap.get(brianId);
        Log.e(TAG,"res"+res);
        if (res == null){
            ToastEasy("Fail to download image, something wrong with res list !");
            return;
        }
        imageDataSource.downloadImage(lastDownloadPotentialArborMarkerInfo.getBrianId(), res, (int) loc.x , (int) loc.y, (int) loc.z, DEFAULT_IMAGE_SIZE);

    }

    public void getArborMarkerList() {
        String arborName = curPotentialArborMarkerInfo.getArborName();
        qualityInspectionDataSource.getArborMarkerList(arborName);
    }

    public void updateCheckResult(MarkerList markerListToAdd, JSONArray markerListToDelete, int locationType) {
        if ((markerListToAdd == null) && (markerListToDelete == null)){
            return;
        }
        if (!curPotentialArborMarkerInfo.ifStillFresh() && !curPotentialArborMarkerInfo.isAlreadyUpload()) {
            uploadResult.setValue(new ResourceResult(false, "Expired"));
            return;
        }
        try {
            int arborId = curPotentialArborMarkerInfo.getArborId();
            String arborName = curPotentialArborMarkerInfo.getArborName();
            String username = loggedInUser.getUserId();
            qualityInspectionDataSource.UpdateCheckResult(arborId, arborName,
                    MarkerList.toJSONArrayAddType(MarkerList.covertLocalToGlobal(markerListToAdd, coordinateConvert)), markerListToDelete,username);
            if (!curPotentialArborMarkerInfo.isAlreadyUpload()) {
                curPotentialArborMarkerInfo.setAlreadyUpload(true);
                winScoreByFinishConfirmAnImage();
            }
        } catch (JSONException e) {
            ToastEasy("Fail to convert MarkerList ot JSONArray !");
            e.printStackTrace();
        }
    }

    public void getSwc(){
        Log.e(TAG,"GET SWC");
        String arborName = curPotentialArborMarkerInfo.getArborName();
        XYZ loc =curPotentialArborMarkerInfo.getLocation();
        String res = "/"+curPotentialArborMarkerInfo.getBrianId()+"/"+curPotentialArborMarkerInfo.getSomaId();
        qualityInspectionDataSource.getSwc(res,(float)loc.x,(float)loc.y,(float) loc.z,DEFAULT_IMAGE_SIZE * (int) Math.pow(2, lastDownloadCoordinateConvert.getResIndex()-1),arborName);
    }

    public void winScoreByFinishConfirmAnImage() {
        userInfoRepository.getScoreModel().finishAnImage();
    }

    public void winScoreByPinPoint() {
        userInfoRepository.getScoreModel().pinpoint();
    }

    private void initPreDownloadThread() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (lastIndex > potentialArborMarkerInfoList.size() - 7 && !isDownloading && !noFileLeft) {
                        getArbor();
                        isDownloading = true;
                    }
                }
            }
        });
    }

    private void initCheckFreshThread() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (curPotentialArborMarkerInfo != null &&
                        !curPotentialArborMarkerInfo.isAlreadyUpload() && !curPotentialArborMarkerInfo.ifStillFresh()) {
                    if (workStatus.getValue() != WorkStatus.IMAGE_FILE_EXPIRED) {
                        workStatus.postValue(WorkStatus.IMAGE_FILE_EXPIRED);
                    }
                }
            }
        }, 30,30, TimeUnit.SECONDS);
    }

    public void shutDownThreadPool() {
        executorService.shutdown();
        scheduledExecutorService.shutdown();
    }
}
