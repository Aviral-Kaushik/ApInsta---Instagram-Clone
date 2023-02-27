package com.aviral.apinsta.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FileSearch {

    public static ArrayList<String> getDirectoryPath(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        assert listfiles != null;
        for (File listFile : listfiles) {
            if (listFile.isDirectory()) {
                pathArray.add(listFile.getAbsolutePath());
            }
        }
        return pathArray;
    }

    public static ArrayList<String> getFilePath(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (File listFile : Objects.requireNonNull(listFiles)) {
            if (listFile.isFile()) {
                pathArray.add(listFile.getAbsolutePath());
            }
        }
        return pathArray;
    }

}
