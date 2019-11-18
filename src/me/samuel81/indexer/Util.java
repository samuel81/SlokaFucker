package me.samuel81.indexer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Util {

	public static String fill(char ch, int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(ch);
		}
		return sb.toString();
	}
	
	/*
	 * Move folder with pdf contents
	 */

	public static void moveFolder(File parent, File target) {
		try {
			FileUtils.copyDirectory(parent, target, null, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
