package com.yang.jigsaw.bean;

import android.util.Log;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2016/2/5.
 */
public class ImagesPathsBean implements Serializable{
    private Set<String> selectedImagesPaths = new HashSet<>();
    public ImagesPathsBean(Set<String> paths){
        selectedImagesPaths = paths;
    }
    public Set<String> getPaths(){
        return  selectedImagesPaths;
    }
}
