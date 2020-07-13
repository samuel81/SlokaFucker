package me.samuel81.jin.module.su;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;

import io.appium.java_client.windows.WindowsDriver;
import me.samuel81.jin.Main;
import me.samuel81.jin.module.BotIniter;
import me.samuel81.jin.module.Checker;
import me.samuel81.jin.module.Pos;

public class SUChecker extends Checker {

	private Wait<WindowsDriver<WebElement>> waiter = null;

	public SUChecker(List<File> ff, boolean upload, boolean override) {
		super(upload, override);
		waiter = BotIniter.getFluentWaiter(60 * 3, 10);
	}

	@Override
	public void startCheck() {
		try {

			// Searching for the object in DB
			session.findElementByAccessibilityId("Cari").click();

			try {

				// Waiting for the object that appear in the box to show up, if its not showing
				// up until 3 minutes, its not registered on database
				WebElement total = waiter.until(new Function<WindowsDriver<WebElement>, WebElement>() {

					@Override
					public WebElement apply(WindowsDriver<WebElement> t) {
						WebElement presenter = session.findElementByAccessibilityId("LayoutFooter");
						return presenter.findElement(By.className("TextBlock"));
					}
				});
				System.out.println(total.getText());
			} catch (Exception e) {
				e.printStackTrace();
				Main.getMyFrame().log("There's no BT in DB \n");
				addFailed();
				return;
			} finally {

			}
			return;
		} catch (Exception e) {
			Main.getMyFrame().log("Failed to check \n");
			addFailed();
			return;
		}
	}

	@Override
	public void savePos() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Pos loadPos() {
		// TODO Auto-generated method stub
		return null;
	}

}
