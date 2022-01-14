package com.penglab.hi5.core.net;

import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.RequestBody;

public class HttpUtilsImage extends HttpUtils {
    private static final String URL_GET_BRAIN_LIST = SERVER_IP + "/dynamic/image/getimagelist";
    private static final String URL_GET_NEURON_LIST = SERVER_IP + "/dynamic/ano/getneuronlist";
    private static final String URL_GET_ANO_LIST = SERVER_IP + "/dynamic/ano/getanolist";
    private static final String URL_DOWNLOAD_IMAGE = SERVER_IP + "/dynamic/image/cropimage";
    private static final String URL_GET_BBSWC = SERVER_IP + "/dynamic/coll/getswcbb";

    public static void getBrainListWithOkHttp(JSONObject userInfo, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("user", userInfo)));
            asyncRequest(URL_GET_BRAIN_LIST, body, callback);
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

    /**
     * download image block
     * @param userInfo username & password
     * @param brainId such as 18454
     * @param res such as RES(26298x35000x11041)"
     * @param loc offset of axis x, y, z
     * @param len size of image block
     * @param callback the callback func
     */
    public static void downloadImageWithOkHttp(JSONObject userInfo, String brainId, String res, JSONObject loc, int len, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("user", userInfo)
                    .put("image", brainId)
                    .put("res", res)
                    .put("loc", loc)
                    .put("len", len)));
            asyncRequest(URL_DOWNLOAD_IMAGE, body, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * download image block
     * @param username username
     * @param password password
     * @param swc such as 18454/18454_01130/18454_01130_DAH_YLL_SYY_stamp_2021_12_29_17_45/18454_01130_DAH_YLL_SYY_stamp_2021_12_29_17_45.ano.eswc
     * @param res such as RES(26298x35000x11041)"
     * @param x offset of axis x
     * @param y offset of axis y
     * @param z offset of axis z
     * @param len size of image block
     * @param callback the callback func
     */
    public static void getBBSwcWithOkHttp(String username, String password, String swc, int res, int x, int y, int z, int len, Callback callback) {
        try {
            RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject()
                    .put("name", username)
                    .put("password", password)
                    .put("swc", swc)
                    .put("x", x)
                    .put("y", y)
                    .put("z", z)
                    .put("len", len)
                    .put("res", res)));
            asyncRequest(URL_GET_BBSWC, body, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
