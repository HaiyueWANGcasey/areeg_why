package com.penglab.hi5.core.net;

import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.RequestBody;

public class HttpUtilsImage extends HttpUtils {

    private static final String URL_GET_IMAGE_LIST = "http://192.168.3.158:8000/ano/getimagelist";
    private static final String URL_GET_NEURON_LIST = "http://192.168.3.158:8000/ano/getneuronlist";
    private static final String URL_GET_ANO_LIST = "http://192.168.3.158:8000/ano/getanolist";
    private static final String URL_DOWNLOAD_IMAGE = "http://192.168.3.158:8000/coll/getimagebb";

    public static void getImageListWithOkHttp(String username, String password, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("name", username)
                    .put("password", password)));
            asyncRequest(URL_GET_IMAGE_LIST, body, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getNeuronListWithOkHttp(String username, String password, String brain_id, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("name", username)
                    .put("password", password)
                    .put("brain_id", brain_id)));
            asyncRequest(URL_GET_NEURON_LIST, body, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getAnoListWithOkHttp(String username, String password, String neuron_id, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("name", username)
                    .put("password", password)
                    .put("neuron_id", neuron_id)));
            asyncRequest(URL_GET_ANO_LIST, body, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadImageWithOkHttp(String username, String password, String image, int x, int y, int z, int len, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("name", username)
                    .put("password", password)
                    .put("image", image)
                    .put("x", x)
                    .put("y", y)
                    .put("z", z)
                    .put("len", len)));
            asyncRequest(URL_DOWNLOAD_IMAGE, body, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
