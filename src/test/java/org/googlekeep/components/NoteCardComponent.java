package org.googlekeep.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
}