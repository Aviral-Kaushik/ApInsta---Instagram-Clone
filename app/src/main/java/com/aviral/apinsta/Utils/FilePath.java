package com.aviral.apinsta.Utils;

import android.os.Environment;

public class FilePath {

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/camera";

//    public String WHATSAPP_IMAGES = ROOT_DIR + "WhatsApp/Media/WhatsApp Images";
//    public String WHATSAPP_VIDEOS = ROOT_DIR + "WhatsApp/Media/WhatsApp Video";
//    public String WHATSAPP_STICKERS = ROOT_DIR + "WhatsApp/Media/WhatsApp Stickers";

    public String FIREBASE_IMAGE_STORAGE_LOCATION = "photos/users/";
}
