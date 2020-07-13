package me.samuel81.jin.module;

import org.openqa.selenium.WebElement;

import io.appium.java_client.windows.WindowsDriver;
import me.samuel81.jin.Main;

public abstract class Checker implements Runnable {

	private Thread t;

	private int success = 0, failed = 0;

	protected WindowsDriver<WebElement> session = null;

	protected boolean upload, override;

	protected boolean useSavedPos = false;

	public Checker(boolean upload, boolean override) {
		this.session = BotIniter.getSession();
		this.upload = upload;
		this.override = override;
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();

		startCheck();
		if (!useSavedPos)
			savePos();

		long totalTime = System.currentTimeMillis() - start;

		Main.getMyFrame().doneTask(success, failed, totalTime);
	}

	public abstract void startCheck();

	public abstract void savePos();

	public abstract Pos loadPos();

	public int totalFailed() {
		return failed;
	}

	public void addFailed() {
		failed++;
	}

	public int totalSuccess() {
		return success;
	}

	public void addSuccess() {
		success++;
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			failed = 0;
			t.setPriority(10);
			t.start();
		}

	}

}