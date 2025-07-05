package org.googlekeep.components;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewNoteComponent {
    private final WebDriver driver;

    @FindBy(className = "fmcmS-h1U9Be-LS81yb")
    private WebElement newNoteInput;

    @FindBy(xpath = "//div[@role='textbox'][@aria-label='Title']")
    private WebElement titleField;

    @FindBy(xpath = "//div[@role='textbox'][@aria-label='Take a note…']")
    private WebElement bodyField;

    @FindBy(xpath = "//div[@role='button' and text()='Close']")
    private WebElement closeNoteBtn;

    public NewNoteComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void clickNewNote() {
        newNoteInput.click();
    }

    public void setTitle(String title) {
        titleField.sendKeys(title);
    }

    public void setBody(String body) {
        bodyField.sendKeys(body);
    }

    public void close() {
        closeNoteBtn.click();
    }

    public void createEmptyNote() {
        clickNewNote();
        close();
    }

    public void createNote(String title) {
        clickNewNote();
        setTitle(title);
        close();
    }
}