package me.samuel81.jin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.windows.WindowsDriver;

public class Uploader {

	private boolean firstTime = true;

	private WindowsDriver<WebElement> session = null;

	private Set<File> files = null;

	private int restCounter = 0;

	private int success = 0, failed = 0;

	Logger logger = Logger.getLogger("Image2PDF Batch Converter (S4-Development");
	FileHandler fh;

	private StringBuilder uploaded = new StringBuilder(), fail = new StringBuilder();

	String latestThn = "";

	public Uploader(Set<File> files) {
		this.files = files;
		try {
			File file = new File("Latest.log");
			if (!file.exists())
				file.createNewFile();
			fh = new FileHandler(file.getAbsolutePath());
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.addHandler(fh);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
	}

	public void setup() {
		try {
			DesiredCapabilities dcapabilities = new DesiredCapabilities();
			dcapabilities.setCapability("app", "Root");
			dcapabilities.setCapability("deviceName", "WindowsPC");
			session = new WindowsDriver<WebElement>(new URL("http://127.0.0.1:4723"), dcapabilities);

			WebElement applicationWindow = null;
			WebElement openWindows = session.findElementByClassName("Window");
			for (WebElement w : openWindows.findElements(By.className("Window"))) {
				// System.out.println(w.getAttribute("Name"));
				if (w.getAttribute("Name").equalsIgnoreCase("Sloka Etnik - Sistem Pengelolaan Warkah Elektronik")) {
					applicationWindow = w;
				}
			}

			String topLevelWindowHandle = applicationWindow.getAttribute("NativeWindowHandle");
			topLevelWindowHandle = Integer.toHexString(Integer.parseInt(topLevelWindowHandle));

			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability("appTopLevelWindow", topLevelWindowHandle);
			capabilities.setCapability("deviceName", "WindowsPC");
			session = new WindowsDriver<WebElement>(new URL("http://127.0.0.1:4723"), capabilities);
			session.manage().timeouts().implicitlyWait(60 * 3, TimeUnit.SECONDS);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public void startUpload() throws InterruptedException {
		for(File f:files) {
			int status = uploadSU(f);
			if(status == 0) {
				restCounter++;
				if (restCounter % 100 == 0) {
					Thread.sleep(200);
				} else if (restCounter % 10 == 0) {
					Thread.sleep(50);
				}
			} else if(status == 1) {
				//Isn't on DB
			} else if(status == 2) {
				//Error
				break;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private int uploadSU(File file) {
		String fName = file.getName();
		String[] fSplitted = fName.split("_");
		try {
			// Searching
			WebElement nomor = session.findElementByAccessibilityId("txtNomor");
			nomor.click();
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.CONTROL + "a");
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.BACK_SPACE);
			nomor.findElement(By.className("TextBox")).sendKeys(fSplitted[2].replaceFirst("^0+(?!$)", ""));

			if (!latestThn.equalsIgnoreCase(fSplitted[3])) {
				WebElement thn = session.findElementByAccessibilityId("Tahun Surat Ukur");
				thn.click();
				thn.findElement(By.className("TextBox")).sendKeys(Keys.CONTROL + "a");
				thn.findElement(By.className("TextBox")).sendKeys(Keys.BACK_SPACE);
				thn.findElement(By.className("TextBox")).sendKeys(fSplitted[3]);
				latestThn = fSplitted[3];
			}

			session.findElementByAccessibilityId("Cari").click();

			WebElement data = session.findElementByAccessibilityId("dataPresenter");

			session.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			try {
				WebElement d = data.findElement(By.name("Warkah.SuratUkurItem"));
				d.click();
			} catch (NoSuchElementException e) {
				System.out.println("SU cannot be found on DB " + fName);
				fail.append(fName + "\n");
				failed++;
				return 1;
			}
			session.manage().timeouts().implicitlyWait(60 * 3, TimeUnit.SECONDS);
			// Uploading
			session.findElementByAccessibilityId("BarSplitButtonItemLinkbOpenFromWeb").click();
			
			if (firstTime) {
				session.getKeyboard().sendKeys(Keys.F4);
				session.getKeyboard().sendKeys(Keys.CONTROL + "a" + Keys.CONTROL);
				session.getKeyboard().sendKeys(Keys.DELETE);
				session.getKeyboard().sendKeys(file.getParent());
				session.getKeyboard().sendKeys(Keys.ENTER);
				session.getKeyboard().sendKeys(Keys.LEFT_ALT + "n" + Keys.LEFT_ALT);
				firstTime = false;
			}
			WebElement input = session.findElementByAccessibilityId("1148");
			input.click();
			input.findElement(By.className("Edit")).sendKeys(fName);
			// session.getKeyboard().sendKeys(fName);
			session.getKeyboard().sendKeys(Keys.ENTER);

			session.findElementByAccessibilityId("Upload").click();
			WebElement bottom = session.findElementByClassName("SuratUkur");
			bottom.click();
			bottom.findElement(By.className("TabbedPaneItem")).findElement(By.className("ControlBoxButtonPresenter"))
					.click();
			System.out.println("Successfully uploading " + fName);
			uploaded.append(fName + "\n");
			success++;
			return 0;
		} catch (Exception e) {
			System.out.println("Failed to upload " + fName);
			fail.append(fName + "\n");
			failed++;
			return 2;
		}
	}

	public int totalSuccess() {
		return success;
	}

	public int totalFailed() {
		return failed;
	}

	public void saveLog() {
		logger.info("List of uploaded file : ");
		logger.info(uploaded.toString());
		logger.info("List of failed file : ");
		logger.info(fail.toString());
	}

	public void destroy() {
		session.close();
	}

}
