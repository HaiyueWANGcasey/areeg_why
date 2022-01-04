package com.penglab.hi5.core.ui.check;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.penglab.hi5.R;
import com.penglab.hi5.core.BaseActivity;
import com.penglab.hi5.core.render.view.CheckGLSurfaceView;
import com.penglab.hi5.core.ui.ResourceResult;
import com.penglab.hi5.core.ui.ViewModelFactory;
import com.penglab.hi5.data.Result;
import com.penglab.hi5.data.model.img.AnoInfo;
import com.penglab.hi5.data.model.img.ArborInfo;
import com.penglab.hi5.data.model.img.BrainInfo;
import com.penglab.hi5.data.model.img.NeuronInfo;

import java.util.List;

/**
 * Created by Jackiexing on 12/18/21
 */
public class CheckActivity extends BaseActivity {

    private static final String TAG = "CheckActivity";
    private CheckViewModel checkViewModel;

    private CheckGLSurfaceView checkGLSurfaceView;
    private ImageButton checkYesButton;
    private ImageButton checkNoButton;
    private Button checkROIButton;
    private ImageButton checkFileListButton;
    private ImageButton checkNextFileButton;
    private ImageButton checkFormerFileButton;
    private ImageButton checkZoomInButton;
    private ImageButton checkZoomOutButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_check);
        setSupportActionBar(toolbar);

        checkViewModel = new ViewModelProvider(this, new ViewModelFactory()).get(CheckViewModel.class);
        checkViewModel.getImageDataSource().getResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if (result == null){
                    return;
                }

                checkViewModel.updateImageResult(result);
            }
        });

        checkViewModel.getFileInfoState().currentOpenState.observe(this, new Observer<FileInfoState.OpenState>() {
            @Override
            public void onChanged(FileInfoState.OpenState openState) {
                switch (openState) {
                    case BRAIN_LIST:
                        showBrainListPopup();
                        break;
                    case NEURON_LIST:
                        showNeuronListPopup();
                        break;
                    case ANO_LIST:
                        showAnoListPopup();
                        break;
                    default:
                        break;
                }
            }
        });

        checkViewModel.getImageResult().observe(this, new Observer<ResourceResult>() {
            @Override
            public void onChanged(ResourceResult imageResult) {
                if (!imageResult.isSuccess()) {
                    Toast_in_Thread(imageResult.getError());
                } else {
                    checkGLSurfaceView.loadImageFile();
                    checkViewModel.downloadSWC();
                }
            }
        });

        checkViewModel.getAnnotationDataSource().getResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if (result == null) {
                    return;
                }

                checkViewModel.updateAnnotationResult(result);

            }
        });

        checkViewModel.getAnnotationResult().observe(this, new Observer<ResourceResult>() {
            @Override
            public void onChanged(ResourceResult annotationResult) {
                if (!annotationResult.isSuccess()) {
                    Toast_in_Thread(annotationResult.getError());
                } else {
                    checkGLSurfaceView.loadAnnotationFile();
                    showButtons();
                }
            }
        });

        checkViewModel.getCheckArborDataSource().getResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if (result == null) {
                    return;
                }

                checkViewModel.updateCheckArborResult(result);
            }
        });

        checkViewModel.getCheckArborInfoState().getArborOpenState().observe(this, new Observer<CheckArborInfoState.ArborOpenState>() {
            @Override
            public void onChanged(CheckArborInfoState.ArborOpenState arborOpenState) {
                if (arborOpenState == CheckArborInfoState.ArborOpenState.ARBOR_LIST) {
                    showCheckArborListPopup();
                } else if (arborOpenState == CheckArborInfoState.ArborOpenState.ARBOR_URL) {

                }
            }
        });

        initButtons();
        initCheckGLSurfaceView();
    }

    private void initButtons() {
        checkYesButton = findViewById(R.id.check_yes_button);
        checkNoButton = findViewById(R.id.check_no_button);
        checkFileListButton = findViewById(R.id.check_file_list_button);
        checkNextFileButton = findViewById(R.id.check_next_file_button);
        checkFormerFileButton = findViewById(R.id.check_former_file_button);
        checkROIButton = findViewById(R.id.check_roi_button);
        checkZoomInButton = findViewById(R.id.check_zoom_in_button);
        checkZoomOutButton = findViewById(R.id.check_zoom_out_button);

        checkYesButton.setVisibility(View.GONE);
        checkNoButton.setVisibility(View.GONE);
        checkFileListButton.setVisibility(View.GONE);
        checkNextFileButton.setVisibility(View.GONE);
        checkROIButton.setVisibility(View.GONE);
        checkZoomInButton.setVisibility(View.GONE);
        checkZoomOutButton.setVisibility(View.GONE);

        checkYesButton.setOnClickListener(new CheckButtonsClickListener());
        checkNoButton.setOnClickListener(new CheckButtonsClickListener());
        checkFileListButton.setOnClickListener(new CheckButtonsClickListener());
        checkNextFileButton.setOnClickListener(new CheckButtonsClickListener());
        checkFormerFileButton.setOnClickListener(new CheckButtonsClickListener());
        checkROIButton.setOnClickListener(new CheckButtonsClickListener());
        checkZoomInButton.setOnClickListener(new CheckButtonsClickListener());
        checkZoomOutButton.setOnClickListener(new CheckButtonsClickListener());
    }

    private void showButtons() {
        checkYesButton.setVisibility(View.VISIBLE);
        checkNoButton.setVisibility(View.VISIBLE);
        checkFileListButton.setVisibility(View.VISIBLE);
        checkNextFileButton.setVisibility(View.VISIBLE);
        checkROIButton.setVisibility(View.VISIBLE);
        checkZoomInButton.setVisibility(View.VISIBLE);
        checkZoomOutButton.setVisibility(View.VISIBLE);
    }

    private void initCheckGLSurfaceView() {
        checkGLSurfaceView = findViewById(R.id.check_gl_surface_view);

        checkGLSurfaceView.setOnDoubleClickListener(new CheckGLSurfaceView.OnDoubleClickListener() {
            @Override
            public void run(int[] center) {
                checkViewModel.getImageWithNewCenter(center);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.check_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.check_open_file_toolbar:
                checkViewModel.getCheckArborList();
                return true;

            case R.id.check_more_toolbar:
                Log.e(TAG,"more functions");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public static void start(Context context){
        Intent intent = new Intent(context, CheckActivity.class);
        context.startActivity(intent);
    }

    private void showBrainListPopup() {
        List<BrainInfo> brainInfoList = checkViewModel.getFileInfoState().getBrainList();
        String [] imageIdList = new String[brainInfoList.size()];
        for (int i = 0; i < imageIdList.length; i++) {
            imageIdList[i] = brainInfoList.get(i).getImageId();
        }

        new XPopup.Builder(this)
                .asCenterList("Brain List", imageIdList, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        checkViewModel.getNeuronListWithBrainInfo(brainInfoList.get(position));
                    }
                }).show();
    }

    private void showNeuronListPopup() {
        List<NeuronInfo> neuronInfoList = checkViewModel.getFileInfoState().getNeuronList();
        String [] neuronIdList = new String[neuronInfoList.size()];
        for (int i = 0; i < neuronIdList.length; i++) {
            neuronIdList[i] = neuronInfoList.get(i).getNeuronId();
        }

        new XPopup.Builder(this)
                .asCenterList("Neuron List", neuronIdList, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        checkViewModel.getAnoListWithNeuronInfo(neuronInfoList.get(position));
                    }
                }).show();
    }

    private void showAnoListPopup() {
        List<AnoInfo> anoInfoList = checkViewModel.getFileInfoState().getAnoList();
        String [] anoNameList = new String[anoInfoList.size()];
        for (int i = 0; i < anoNameList.length; i++) {
            anoNameList[i] = anoInfoList.get(i).getAnoName();
        }

        new XPopup.Builder(this)
                .asCenterList("Ano List", anoNameList, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        checkViewModel.getImageWithAnoInfo(anoInfoList.get(position));
                    }
                }).show();
    }

    private void showROIListPopup() {
        String [] rois = checkViewModel.getFileInfoState().getRois();
        new XPopup.Builder(this)
                .asCenterList("ROI List", rois, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        checkViewModel.getImageWithROI(text);
                    }
                }).show();
    }

    private void showCheckArborListPopup() {
        List<ArborInfo> arborInfoList = checkViewModel.getCheckArborInfoState().getArborInfoList();
        String [] arborNameList = new String[arborInfoList.size()];
        for (int i = 0; i < arborNameList.length; i++) {
            arborNameList[i] = arborInfoList.get(i).getArborName();
        }
        new XPopup.Builder(this)
                .asCenterList("Arbor List", arborNameList, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        checkViewModel.getImageWithArborInfo(arborInfoList.get(position));
                    }
                }).show();
    }

    private void checkYes() {
        checkViewModel.sendCheckYes();
    }

    private void checkNo() {
        checkViewModel.sendCheckNo();
    }

    class CheckButtonsClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check_yes_button:
                    checkYes();
                    break;
                case R.id.check_no_button:
                    checkNo();
                    break;
                case R.id.check_file_list_button:
                    showNeuronListPopup();
                    break;
                case R.id.check_roi_button:
                    showROIListPopup();
                    break;
                case R.id.check_next_file_button:

                    break;
                case R.id.check_former_file_button:

                    break;
                case R.id.check_zoom_in_button:
                    checkViewModel.getImageZoomIn();
                    break;
                case R.id.check_zoom_out_button:
                    checkViewModel.getImageZoomOut();
                    break;
                default:
                    break;
            }
        }
    }
}

