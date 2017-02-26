package com.example.android.BluetoothChat;

import android.os.Environment;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveData {
	File mPath = null;
	FileWriter writer = null;
	
	public SaveData(String fileName) {
		try {
			File root = new File(Environment.getExternalStorageDirectory(), "Notes");			
			if (!root.exists()) {
                root.mkdirs();
            }
			
//	        save every data in 'one file' that have specific file name.			
			File mPath = new File(root, fileName + ".txt");
			
//	        save data in 'each file' that have currentTimeMillis file name.
//			File mPath = new File(root, DateFormat.format("MM-dd-yyyy-h-mmssaa", System.currentTimeMillis()).toString());
            writer = new FileWriter(mPath, true);			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void write(String items) {		
		try {
			writer.write(items + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	protected void finalize() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
