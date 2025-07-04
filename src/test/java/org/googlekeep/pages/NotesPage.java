package org.googlekeep.pages;

import org.googlekeep.components.NoteCardComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

/**
 * Page object representing the main Google Keep notes page.
 */
public class NotesPage extends BasePage {
    private WebDriverWait wait;

    public NotesPage(WebDriver driver) {
        super(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @FindBy(className = "fmcmS-h1U9Be-LS81yb")
    WebElement newNoteInput;

    @FindBy(xpath = "//div[@role='textbox'][@aria-label='Title']")
    private WebElement titleField;

    @FindBy(xpath = "//div[@role='textbox'][@aria-label='Take a note…']")
    private WebElement bodyField;

    @FindBy(xpath = "//div[@role='button' and text()='Close']")
    private WebElement closeNoteBtn;

    public void createEmptyNote() {
        newNoteInput.click();
        closeNoteBtn.click();
    }

    public void createNote(String title) {
        newNoteInput.click();
        wait.until(ExpectedConditions.visibilityOf(titleField));
        titleField.sendKeys(title);
        closeNoteBtn.click();
        waitUntilNoteAppears(title);
    }

    public void pinNoteByTitle(String title) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.clickPin();
        }
    }

    public void archiveNoteByTitle(String title) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.clickArchive();
        }
    }

    public void deleteNoteByTitle(String title, boolean undo) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.deleteFromMenu();
            if (undo) {
                WebElement undoSnackbar = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@role='alertdialog']//div[@role='button'][contains(.,'Undo')]")));
                undoSnackbar.click();
                waitUntilNoteAppears(title);
            }
        }
    }

    public boolean isNoteSaved(int previousNoteCount) {
        return getAllNoteCards().size() > previousNoteCount;
    }

    public boolean isNotePinned(String noteTitle) {
        String noteXpath = String.format(
                "//div[contains(@class,'IZ65Hb-n0tgWb') and contains(@class,'IZ65Hb-bJ69tf')]" +
                        "[.//div[@role='textbox' and contains(.,'%s')]]",
                noteTitle);

        try {
            WebElement pinnedNote = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(noteXpath)));

            WebElement pinButton = pinnedNote.findElement(
                    By.xpath(".//div[@role='button'][contains(@aria-label,'Unpin note')]"));

            return "true".equals(pinButton.getAttribute("aria-pressed"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a note is archived by:
     * 1. Verifying it's not in main view
     * 2. Checking the archive page
     * @param noteTitle Title or partial content of the note to check
     * @return true if archived, false otherwise
     */
    public boolean isNoteArchived(String noteTitle) {
        // First check in main view with timeout
        String noteXpath = String.format(
                "//div[contains(@class,'IZ65Hb-n0tgWb')]" +
                        "[.//div[@role='textbox' and contains(.,'%s')]]",
                noteTitle);

        try {
            // Wait briefly to see if note appears in main view
            boolean isInMainView = !driver.findElements(By.xpath(noteXpath)).isEmpty();
            if (isInMainView) {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Note check in main view failed: " + e.getMessage());
        }

        // Check archive with proper cleanup
        try {
            goToArchive();

            // Wait for either the note to appear or confirmation it's not there
            boolean isArchived = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(driver -> {
                        List<WebElement> notes = driver.findElements(By.xpath(noteXpath));
                        return !notes.isEmpty();
                    });

            return isArchived;
        } catch (TimeoutException e) {
            return false; // Note not found in archive
        } finally {
            try {
                goToMainNotes();
            } catch (Exception e) {
                System.out.println("Failed to return to main notes: " + e.getMessage());
            }
        }
    }

    /**
     * Navigates to the archive page using sidebar with robust waits
     */
    public void goToArchive() {
        try {

            // More flexible archive button locator
            WebElement archiveBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class,'PvRhvb')]//*[@aria-label='Archive']")));
            archiveBtn.click();

            // Better archive page load verification
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[contains(text(),'Archived')]")),
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//div[contains(@aria-label,'Archived')]"))
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to archive: " + e.getMessage(), e);
        }
    }

    /**
     * Returns to the main notes page reliably
     */
    public void goToMainNotes() {
        try {
            // More flexible notes button locator
            WebElement notesBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class,'PvRhvb')]//*[contains(text(),'Notes') or contains(@aria-label,'Notes')]")));
            notesBtn.click();

            // Better main page load verification
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[contains(text(),'Notes')]")),
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//div[contains(@aria-label,'Notes')]"))
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to return to main notes: " + e.getMessage(), e);
        }
    }

    public int getCurrentNoteCount() {
        return getAllNoteCards().size();
    }

    private void waitUntilNoteAppears(String title) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(NoteCardComponent.byNoteCardTitle(title)));
    }

    private NoteCardComponent getNoteByTitle(String title) {
        try {
            WebElement noteEl = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(NoteCardComponent.byNoteCardTitle(title)));

            return new NoteCardComponent(driver, noteEl);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private List<WebElement> getAllNoteCards() {
        return driver.findElements(By.xpath("//div[contains(@class,'IZ65Hb-n0tgWb')]"));
    }

    public void addLabelToNoteByTitle(String title, String label) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.openMoreMenu();
    
            WebElement addLabelOption = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath("//div[@role='menuitem'][.//div[contains(text(),'Add label')]]")));
            addLabelOption.click();
    
            WebElement labelInput = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath("//input[@aria-label='Enter label name']")));
            labelInput.clear();
            labelInput.sendKeys(label);
            labelInput.sendKeys(Keys.ENTER);
    
            // Закрытие меню
            labelInput.sendKeys(Keys.ESCAPE);
        }
    }
    
    public boolean isLabelAttached(String title, String label) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            try {
                WebElement labelChip = note.getElement()
                        .findElement(By.xpath(".//div[contains(@class,'bQfzdd') and contains(text(),'" + label + "')]"));
                return labelChip.isDisplayed();
            } catch (NoSuchElementException e) {
                return false;
            }
        }
        return false;
    }

    public void createChecklistNote(String title, String[] items) {
        newNoteInput.click();
    
        // Включаем режим чек-листа
        WebElement checklistToggle = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath("//div[@aria-label='New list']")));
        checklistToggle.click();
    
        // Ввод заголовка
        wait.until(ExpectedConditions.visibilityOf(titleField));
        titleField.sendKeys(title);
    
        // Ввод каждого элемента чек-листа
        for (String item : items) {
            WebElement inputField = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath("//div[@aria-label='List item']")));
            inputField.sendKeys(item);
            inputField.sendKeys(Keys.ENTER);
        }
    
        // Закрытие заметки
        closeNoteBtn.click();
        waitUntilNoteAppears(title);
    }

    public boolean isChecklistPresent(String title, String[] items) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            try {
                for (String item : items) {
                    note.getElement().findElement(
                            By.xpath(".//div[contains(@class,'e5WBfd') and contains(text(),'" + item + "')]"));
                }
                return true;
            } catch (NoSuchElementException e) {
                return false;
            }
        }
        return false;
    }

    public void editNoteTitle(String currentTitle, String newTitle) {
        NoteCardComponent note = getNoteByTitle(currentTitle);
        if (note != null) {
            note.open();

            String xpath = String.format(
                    "//div[contains(@class,'IZ65Hb-r4nke-haAclf')]//div[@contenteditable='true' and text()='%s']",
                    currentTitle
            );
            WebElement titleInput = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));

            titleInput.clear();
            titleInput.sendKeys(newTitle);

            WebElement closeButton = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class, 'IZ65Hb-yePe5c')]//div[@role='button' and normalize-space(text())='Close']")
                    ));

            closeButton.click();
        }

/*
        // Open the note by title
        WebElement noteCard = driver.findElement(NoteCardComponent.byNoteCardTitle(currentTitle));
        noteCard.click();

        // Wait for the title field to be clickable
        WebElement titleInput = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@role='dialog']//div[contains(@class,'IZ65Hb-r4nke-haAclf')]//div[@contenteditable='true']")
                ));

        // Edit the title
        titleInput.click();
        titleInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        titleInput.sendKeys(Keys.BACK_SPACE);
        titleInput.sendKeys(newTitle);

        // Close the dialog to save
        WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@role='dialog']//div[text()='Close']")
                ));
        closeBtn.click();

        // Wait for the updated title to appear
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(
                        NoteCardComponent.byNoteCardTitle(newTitle)));
                        */
    }


    /**
     * Checks if a note with the given title is present on the page.
     *
     * @param title Title or content to match.
     * @return true if found, false otherwise.
     */
    public boolean isNotePresent(String title) {
        return !driver.findElements(NoteCardComponent.byNoteCardTitle(title)).isEmpty();
    }

    public void searchNoteByTitle(String title) {
        WebElement searchInput = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath("//input[@aria-label='Search']")));
        searchInput.click();
        searchInput.sendKeys(title);
        searchInput.sendKeys(Keys.ENTER);

        // Wait for results to load
        wait.until(ExpectedConditions.presenceOfElementLocated(NoteCardComponent.byNoteCardTitle(title)));
    }
    // Reopen note by title

    public void changeNoteColor(String title, String color) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.changeColorTo(color);
        }
    }
}
