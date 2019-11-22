package me.samuel81.indexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

public class Util {

	/**
	 * Export resource from jar file to local file
	 * 
	 * @param resourceName
	 * @throws Exception
	 */
	public static void ExportResource(String resourceName) throws Exception {
		InputStream stream = null;
		try {
			stream = Main.class.getResourceAsStream(resourceName);// note that each / is a directory down in the "jar
																	// tree" been the jar the root of the tree
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}
			Files.copy(stream, new File("config.yaml").toPath());
		} catch (Exception ex) {
			throw ex;
		} finally {
			stream.close();
		}
	}

	/**
	 * Move folder to another location NOTE: Only PDF Files that will be moved to
	 * destination
	 * 
	 * @param parent
	 * @param target
	 */
	public static void moveFolder(File parent, File target) {
		try {
			FileUtils.copyDirectory(parent, target, null, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
