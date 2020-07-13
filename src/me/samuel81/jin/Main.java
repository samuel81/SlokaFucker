package me.samuel81.jin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import me.samuel81.jin.gui.MyFrame;
import me.samuel81.jin.module.bt.BTFakeMaker;

public class Main {

	private static MyFrame frame;
	private static List<File> fileRusak = new ArrayList<>();

	private static Random rand = new Random();

	private static File folderRusak;

	/**
	 * Main things to do before we start the tuyul
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		if (args.length > 0) {

			File txtFile = new File(args[0]);
			if (!txtFile.exists())
				return;

			// Adding list of file rusak template
			File folder = new File("Random File Rusak Folder");
			if (!folder.exists())
				folder.mkdirs();
			for (File file : folder.listFiles()) {
				fileRusak.add(file);
			}

			folderRusak = new File("Folder Rusak");
			if (!folder.exists())
				folder.mkdirs();
			
			BTFakeMaker maker = new BTFakeMaker(txtFile);
			maker.startMaking();

		} else {
			// Showing up the gui
			frame = new MyFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
	}

	public static MyFrame getMyFrame() {
		return frame;
	}

	public static List<File> getFileRusak() {
		return fileRusak;
	}

	public static File getRandomFile() {
		return fileRusak.get(rand.nextInt(fileRusak.size()));
	}

	public static File getFolderRusak() {
		return folderRusak;
	}

}