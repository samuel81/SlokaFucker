package me.samuel81.jin.module;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumFluentWait;
import io.appium.java_client.windows.WindowsDriver;

public class BotIniter {

	private static WindowsDriver<WebElement> session = null;

	public static File getPos(String name) {
		try {
			File file = new File(name + ".pos");
			if (!file.exists())
				file.createNewFile();
			return file;
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Initing the bot
	 * @throws MalformedURLException
	 */
	public static void init() throws MalformedURLException {
		DesiredCapabilities dcapabilities = new DesiredCapabilities();
		dcapabilities.setCapability("app", "Root");
		dcapabilities.setCapability("deviceName", "WindowsPC");
		dcapabilities.setCapability("platformName", "Windows");
		session = new WindowsDriver<WebElement>(new URL("http://127.0.0.1:4723/"), dcapabilities);

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
		capabilities.setCapability("platformName", "Windows");
		session = new WindowsDriver<WebElement>(new URL("http://127.0.0.1:4723/"), capabilities);
	}

	public static void destroySession() {
		session.quit();
	}

	public static WindowsDriver<WebElement> getSession() {
		return session;
	}

	public static WebDriverWait getWaiter(int waitTime) {
		return new WebDriverWait(session, waitTime);
	}

	public static FluentWait<WindowsDriver<WebElement>> getFluentWaiter(int waitTime, int checkInterval) {
		return new AppiumFluentWait<>(session).withTimeout(Duration.ofSeconds(waitTime))
				.pollingEvery(Duration.ofSeconds(checkInterval)).ignoring(Exception.class)
				.ignoring(NoSuchElementException.class);
	}

}
