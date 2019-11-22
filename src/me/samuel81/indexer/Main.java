package me.samuel81.indexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.swing.JFrame;

import org.yaml.snakeyaml.Yaml;

import me.samuel81.indexer.gui.MyFrame;

public class Main {

	public static Config config;

	private static Yaml yaml = new Yaml();

	private static MyFrame frame;

	public static void main(String... args) throws Exception {
		File file = new File("config.yaml");
		if (!file.exists()) {
			Util.ExportResource("/config.yaml");
		}

		try (InputStream in = Files.newInputStream(file.toPath())) {
			config = yaml.loadAs(in, Config.class);
		}

		frame = new MyFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	public static void saveYaml() {
		Yaml yaml = new Yaml();
		try {
			yaml.dump(config, new FileWriter("config.yaml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static MyFrame getMyFrame() {
		return frame;
	}

}