package com.penglab.hi5.core.ui.marker;

import com.penglab.hi5.basic.image.XYZ;

/**
 * Created by Jackiexing on 01/13/21
 *
 * Util Class to process coordinate conversion when communicate with Server
 */
public class CoordinateConvert {
    private final String TAG = "CoordinateConvert";

    private XYZ startLocation = new XYZ();
    private XYZ centerLocation = new XYZ();

    private final XYZ centerLocationInMaxRes = new XYZ();
    private int resIndex;
    private int imgSize=128;

    public XYZ convertGlobalToLocal(double x, double y, double z, int imageSizeX,int imageSizeY,int imageSizeZ) {
        XYZ node = convertMaxResToCurRes(x, y, z, resIndex);
        node.x -= startLocation.x + (imgSize - imageSizeX) / 2;
        node.y -= startLocation.y + (imgSize - imageSizeY) / 2;
        node.z -= startLocation.z + (imgSize - imageSizeZ) / 2;
        return node;
    }

    public XYZ convertGlobalToLocal(double x, double y, double z) {
        XYZ node = convertMaxResToCurRes(x, y, z, resIndex);
        node.x -= startLocation.x;
        node.y -= startLocation.y;
        node.z -= startLocation.z;
        return node;
    }

    public XYZ convertLocalToGlobal(double x, double y, double z) {
        x += startLocation.x;
        y += startLocation.y;
        z += startLocation.z;
        XYZ node = convertCurResToMaxRes(x, y, z, resIndex);
        return node;
    }

    public XYZ convertMaxResToCurRes(double x, double y, double z, int resIndex) {
        x /= Math.pow(2, resIndex-1);
        y /= Math.pow(2, resIndex-1);
        z /= Math.pow(2, resIndex-1);
        return new XYZ((float) x, (float) y, (float) z);
    }

    public XYZ convertCurResToMaxRes(double x, double y, double z, int resIndex) {
        x *= Math.pow(2, resIndex-1);
        y *= Math.pow(2, resIndex-1);
        z *= Math.pow(2, resIndex-1);
        return new XYZ((float) x, (float) y, (float) z);
    }

    public int getResIndex() {
        return resIndex;
    }

    public void setResIndex(int resIndex) {
        this.resIndex = resIndex;
    }

    public int getImgSize() {
        return imgSize;
    }

    public void setImgSize(int imgSize) {
        this.imgSize = imgSize;
    }

    public XYZ getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(XYZ startLocation) {
        this.startLocation = startLocation;
    }

    public XYZ getCenterLocation() {
        return centerLocation;
    }

    public void setCenterLocation(XYZ centerLocation) {
        this.centerLocation = centerLocation;
    }

    public XYZ getCenterLocationInMaxRes(){
        return centerLocationInMaxRes;
    }

    public void initLocation(XYZ centerLoc) {
        centerLocationInMaxRes.x = (int) (centerLoc.x);
        centerLocationInMaxRes.y = (int) (centerLoc.y);
        centerLocationInMaxRes.z = (int) (centerLoc.z);

        centerLocation.x = (int) (centerLoc.x / Math.pow(2, resIndex-1));
        centerLocation.y = (int) (centerLoc.y / Math.pow(2, resIndex-1));
        centerLocation.z = (int) (centerLoc.z / Math.pow(2, resIndex-1));

        startLocation.x = centerLocation.x - imgSize / 2;
        startLocation.y = centerLocation.y - imgSize / 2;
        startLocation.z = centerLocation.z - imgSize / 2;
    }
}
