package com.kenung.vn.prettymusic.music_offline.folder;

/**
 * Created by sev_user on 23-Dec-16.
 */
public class FolderModel {
    private String folder_name;
    private String folder_path;
    private String art_uri;
    private int folder_size;

    public FolderModel(String folder_name, String folder_path) {
        this.folder_name = folder_name;
        this.folder_path = folder_path;
    }

    public String getArt_uri() {
        return art_uri;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public String getFolder_path() {
        return folder_path;
    }

    public int getFolder_size() {
        return folder_size;
    }

    public void setFolder_size(int folder_size) {
        this.folder_size = folder_size;
    }
}