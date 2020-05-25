package me.samuel81.indexer;

import javax.swing.JFrame;

import me.samuel81.indexer.gui.MyFrame;

public class Main {

	private static MyFrame frame;

	public static void main(String... args) throws Exception {
		frame = new MyFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static MyFrame getMyFrame() {
		return frame;
	}

}