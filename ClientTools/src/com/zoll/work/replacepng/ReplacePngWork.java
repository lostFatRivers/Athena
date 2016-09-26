package com.zoll.work.replacepng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoll.work.IToolsWork;


public class ReplacePngWork implements IToolsWork {
	public static final String SUFFIX1 = ".png";
	public static final String SUFFIX2 = ".jpg";
	
	private String pngFileName;
	private String jpgFileName;
	
	private byte[] pngByte = new byte[1024 * 5];
	private byte[] jpgByte = new byte[1024 * 5];
	
	private List<String> filesPath;
	private List<String> expectFileName;
	private String backupPath;
	private boolean isBackup = false;
	
	private static Logger logger = LoggerFactory.getLogger("FILELOG");
	
	@Override
	public void recover(Map<String, String> map) {
		File pngFile = new File(changeFileSeparator(map.get("pngFile")));
		File jpgFile = new File(changeFileSeparator(map.get("jpgFile")));
		cacheFileByte(pngFile, jpgFile);
		filesPath = parseParamToList(changeFileSeparator(map.get("basePath")));
		expectFileName = parseParamToList(changeFileSeparator(map.get("exceptFileName")));
		backupPath = changeFileSeparator(map.get("backupPath"));
		isBackup = "true".equals(map.get("isBackup"));
	}
	
	private void cacheFileByte(File pngFile, File jpgFile) {
		try {
			pngFileName = pngFile.getName();
			jpgFileName = jpgFile.getName();
			pngByte = readFileByte(pngFile, pngByte);
			jpgByte = readFileByte(jpgFile, jpgByte);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private byte[] readFileByte(File file, byte[] fileByte) throws Exception {
		FileInputStream inputStream1 = new FileInputStream(file);
		int len = inputStream1.read(fileByte);
		inputStream1.close();
		return Arrays.copyOfRange(fileByte, 0, len);
	}

	private List<String> parseParamToList(String string) {
		String[] split = string.split(",");
		return Arrays.asList(split);
	}
	
	private String changeFileSeparator(String str) {
		return str.replace("\\", File.separator);
	}
	
	@Override
	public boolean doTask() {
		try {
			for (String eachPath : filesPath) {
				scanPath(eachPath);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	private void scanPath(String path) throws Exception {
		File file = new File(path);
		if (checkSkipFile(file)) {
			logger.info("file not exist or in expect file list, name: {}", file.getName());
			return;
		}
		if (file.isDirectory()) {
			String[] list = file.list();
			logger.info("scan deeper: {}", path);
			for (String eachFile : list) {
				scanPath(path + File.separator + eachFile);
			}
		}
		logger.info("file path: {}", path);
		if (file.getName().endsWith(SUFFIX1)) {
			backupAndReplace(file, pngByte);
		}
		if (file.getName().endsWith(SUFFIX2)) {
			backupAndReplace(file, jpgByte);
		}
	}
	
	private boolean checkSkipFile(File file) {
		return !file.exists() || expectFileName.contains(file.getName()) || jpgFileName.equals(file.getName()) || pngFileName.equals(file.getName());
	}

	private void backupAndReplace(File oldfile, byte[] fileByte) throws Exception {
		if (isBackup) {
			backup(oldfile);
		}
		replace(oldfile, fileByte);
	}

	private void replace(File oldfile, byte[] fileByte) throws Exception {
		FileOutputStream output = new FileOutputStream(oldfile);
        output.write(fileByte, 0, fileByte.length); 
        output.flush();
        output.close();
	}

	private void backup(File oldfile) throws Exception {
		InputStream input = new FileInputStream(oldfile);
		FileOutputStream output = new FileOutputStream(new File(backupPath + File.separator + oldfile.getName()));
		byte[] b = new byte[1024 * 5]; 
        int len; 
        while ( (len = input.read(b)) != -1) { 
            output.write(b, 0, len); 
        } 
        output.flush();
        input.close();
        output.close();
	}

}
