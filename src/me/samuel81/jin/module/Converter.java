package me.samuel81.jin.module;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import me.samuel81.jin.Main;

public abstract class Converter implements Runnable {

	private Thread t;

	private List<File> files = null;

	private int success = 0, failed = 0;

	private boolean running = false;

	public Converter(List<File> files) {
		this.files = files;
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		Iterator<File> ff = files.iterator();
		while (running && (!Main.getMyFrame().isCanceled()) && ff.hasNext()) {
			try {
				convertPDF(ff.next());
			} catch (Exception e) {
				failed++;
				running = false;
			}
			Main.getMyFrame().addProggress(++success);
			if (success % 10 == 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (success % 100 == 0) {
				Main.getMyFrame().log("Converted: " + success + "\n");
			}
		}
		long totalTime = System.currentTimeMillis() - start;
		Main.getMyFrame().doneTask(files.size() - failed, failed, totalTime);
	}

	public abstract void convertPDF(File f) throws Exception;

	public int getSuccess() {
		return success;
	}

	public int getFailed() {
		return failed;
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			running = true;
			t.setPriority(10);
			t.start();
		}
	}

}
