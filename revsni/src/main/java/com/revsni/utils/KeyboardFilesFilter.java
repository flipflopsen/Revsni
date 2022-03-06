package com.revsni.utils;


import java.io.File;
import java.io.FilenameFilter;

public class KeyboardFilesFilter implements FilenameFilter {
    
    //Accept all file dirs starts with "keys_"
	@Override
    public boolean accept(File dir, String name) {
        return name.startsWith("keys_");
    }
}