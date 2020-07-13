package me.samuel81.jin.module;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;

import io.appium.java_client.windows.WindowsDriver;
import me.samuel81.jin.Main;

public abstract class Uploader implements Runnable {

	private Thread t;

	private List<File> files = null;

	private boolean running = false;

	private int success = 0, failed = 0;

	protected WindowsDriver<WebElement> session = null;

	protected Wait<WindowsDriver<WebElement>> waiter = null;

	private boolean onPause = false;
	private long pauseCounter = 0;

	private boolean tabOpened = false;

	protected boolean override = false;

	public Uploader(List<File> files, boolean override) {
		this.override = override;
		this.files = files;
		this.session = BotIniter.getSession();
		this.waiter = BotIniter.getFluentWaiter(60 * 3, 1);
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		if (checkSloka()) {
			Iterator<File> ff = files.iterator();
			int i = 1;
			while (running && (!Main.getMyFrame().isCanceled()) && ff.hasNext()) {
				if (!onPause) {
					int status = upload(ff.next());
					if (status == 0) {
						// Success
					} else if (status == 1) {
						// Isn't on DB
					} else if (status == 2) {
						// running = false;
						// break;
						try {
							// Wait until 3 minutes to find if there's any error window appear
							WebElement element = waiter.until(new Function<WindowsDriver<WebElement>, WebElement>() {
								@Override
								public WebElement apply(WindowsDriver<WebElement> t) {
									return session.findElementByAccessibilityId("PART_OkButton");
								}
							});
							element.sendKeys(Keys.chord(Keys.ENTER));
							if (tabOpened) {
								closeTab();
								tabOpened = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							Main.getMyFrame().log("Uploading error but there's no error in it");
							running = false;
							break;
						}

					}
					Main.getMyFrame().addProggress(i++);
					if (i % 50 == 0) {
						onPause = true;
						pauseCounter = System.currentTimeMillis();
					}
				} else {
					long now = System.currentTimeMillis();
					if (TimeUnit.MILLISECONDS.toMinutes(now - pauseCounter) >= 1) {
						onPause = false;
					}
				}
			}
		}
		long totalTime = System.currentTimeMillis() - start;
		Main.getMyFrame().doneTask(success, failed, totalTime);
	}

	public abstract void closeTab();

	public abstract int upload(File file);

	public abstract boolean checkSloka();

	public void toggleTabOpen() {
		tabOpened = !tabOpened;
	}

	public boolean isTabOpened() {
		return tabOpened;
	}

	public int totalSuccess() {
		return success;
	}

	public void addSuccess() {
		success++;
	}

	public int totalFailed() {
		return failed;
	}

	public void addFailed() {
		failed++;
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			running = true;
			t.setPriority(Thread.MAX_PRIORITY);
			t.start();
		}
	}

}
