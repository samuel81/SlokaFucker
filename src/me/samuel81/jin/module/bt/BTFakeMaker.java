package me.samuel81.jin.module.bt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import me.samuel81.jin.Main;
import me.samuel81.jin.Util;

public class BTFakeMaker {

	private File textFile;

	public BTFakeMaker(File text) {
		this.textFile = text;
	}

	public void startMaking() {
		List<String> lists = Util.readFile(textFile.getAbsolutePath());
		for (String str : lists) {
			if (!str.contains("_"))
				continue;
			String[] s = str.split("_");
			if (s.length < 3)
				continue;
			String kel = s[0];
			String jenis = s[1];
			String nomor = String.format("%05d", Integer.parseInt(s[2]));
			File randomFile = Main.getRandomFile();

			String fName = jenis + "_" + nomor + ".pdf";

			try {
				FileUtils.copyFile(randomFile, new File(Main.getFolderRusak() + "/" + kel + "/" + jenis, fName));
				System.out.println("Bip bip... making fake file: " + fName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
