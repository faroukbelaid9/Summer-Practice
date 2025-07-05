package org.googlekeep.components;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Component representing a single note card in Google Keep.
 */
public class NoteCardComponent extends BaseComponent {

    public NoteCardComponent(WebDriver driver, WebElement root) {
        super(driver, root);
    }

    public static By byNoteCardTitle(String titleText) {
        return By.xpath(String.format("//div[contains(@class,'IZ65Hb-n0tgWb')][.//div[@role='textbox' and contains(.,'%s')]]", titleText));
    }

    public void clickPin() {
        root.findElement(By.xpath(".//div[@role='button'][contains(@aria-label,'Pin note')]"))
                .click();
    }

    public void clickArchive() {
        root.findElement(By.xpath(".//div[@role='button'][@aria-label='Archive']"))
                .click();
    }

    public void openMoreMenu() {
        root.findElement(By.xpath(".//div[@role='button'][@aria-label='More']"))
                .click();
    }

    public void deleteFromMenu() {
        openMoreMenu();
        root.findElement(By.xpath("//div[@role='menu']//div[text()='Delete note']"))
                .click();
    }
    
    public WebElement getElement() {
        return root;
    }

    public void updateTitle(String newTitle) {
        root.findElement(By.xpath("//div[@role='textbox']")).sendKeys(newTitle);
    }

    public void open(){
        root.click();
    }

    public void changeColorTo(String colorName) {
        // Click the background options button
        WebElement colorBtn = root.findElement(By.xpath(".//div[@aria-label='Background options']"));
        colorBtn.click();

        // Select the color (using a simple color mapping)
        String colorLabel = colorName.equals("default") ? "Default color" : colorName.substring(0, 1).toUpperCase() + colorName.substring(1);
        WebElement colorOption = root.findElement(By.xpath(String.format(".//div[@aria-label='%s']", colorLabel)));
        colorOption.click();
    }
}