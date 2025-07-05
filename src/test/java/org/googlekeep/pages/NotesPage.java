package org.googlekeep.pages;

import org.googlekeep.components.NewNoteComponent;
import org.googlekeep.components.NoteCardComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

/**
 * Page object representing the main Google Keep notes page.
 */
public class NotesPage extends BasePage {
    private final WebDriverWait wait;
    private final NewNoteComponent newNoteComponent;

    /**
     * Initializes the NotesPage with WebDriver instance and sets up the WebDriverWait.
     * Also initializes the NewNoteComponent for note creation operations.
     *
     * @param driver The WebDriver instance to use for this page
     */
    public NotesPage(WebDriver driver) {
        super(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.newNoteComponent = new NewNoteComponent(driver);
    }

    /**
     * Creates an empty note by clicking the new note input and immediately closing it
     * without adding any content.
     */
    public void createEmptyNote() {
        newNoteComponent.createEmptyNote();
    }

    /**
     * Creates a new note with the specified title and waits for it to appear in the notes list.
     *
     * @param title The title text to set for the new note
     */
    public void createNote(String title) {
        newNoteComponent.createNote(title);
        waitUntilNoteAppears(title);
    }

    /**
     * Pins a note identified by its title if the note exists.
     *
     * @param title The title of the note to pin
     */
    public void pinNoteByTitle(String title) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.clickPin();
        }
    }

    /**
     * Archives a note identified by its title if the note exists.
     *
     * @param title The title of the note to archive
     */
    public void archiveNoteByTitle(String title) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.clickArchive();
        }
    }

    /**
     * Deletes a note by its title with option to undo the deletion.
     *
     * @param title The title of the note to delete
     * @param undo  If true, will undo the deletion after performing it
     */
    public void deleteNoteByTitle(String title, boolean undo) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.deleteFromMenu();
            if (undo) {
                undoDeletedNote();
                waitUntilNoteAppears(title);
            }
        }
    }

    /**
     * Clicks the 'Undo' button that appears after deleting a note to restore it.
     */
    public void undoDeletedNote() {
        WebElement undoSnackbar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@role='alertdialog']//div[@role='button'][contains(.,'Undo')]")));
        undoSnackbar.click();
    }

    /**
     * Checks if a new note was saved by comparing the current note count with previous count.
     *
     * @param previousNoteCount The note count before the save operation
     * @return true if current note count is greater than previous count, false otherwise
     */
    public boolean isNoteSaved(int previousNoteCount) {
        return getAllNoteCards().size() > previousNoteCount;
    }

    /**
     * Checks if a note with the specified title is currently pinned.
     *
     * @param noteTitle The title or partial content of the note to check
     * @return true if the note exists and is pinned (aria-pressed="true"),
     *         false if note isn't pinned or can't be found
     */
    public boolean isNotePinned(String noteTitle) {
        String noteXpath = buildNoteXpath(noteTitle);
        return checkPinStatus(noteXpath);
    }

    /**
     * Verifies the pinned status of a note located by the given XPath.
     *
     * @param noteXpath The XPath to locate the note element
     * @return true if the pin button shows as pressed (pinned),
     *         false if not pinned or element not found
     * @throws TimeoutException If the note element cannot be located within timeout
     */
    private boolean checkPinStatus(String noteXpath) {
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
     *
     * @param noteTitle Title or partial content of the note to check
     * @return true if archived, false otherwise
     */
    public boolean isNoteArchived(String noteTitle) {
        String noteXpath = buildNoteXpath(noteTitle);
        boolean isNoteArchived = checkIfInMainView(noteXpath);

        if(isNoteArchived) {
            return isNoteArchived;
        }

        isNoteArchived = isInArchiveView(noteXpath);
        return isNoteArchived;
    }

    public  boolean checkIfInMainView(String noteXpath){
        try {
            // Wait briefly to see if note appears in main view
            boolean isInMainView = !driver.findElements(By.xpath(noteXpath)).isEmpty();
            if (isInMainView) {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Note check in main view failed: " + e.getMessage());
        }
        return true;
    }

    public boolean isInArchiveView(String noteXpath) {
        // Check archive with proper cleanup
        try {
            goToArchive();
            // Wait for either the note to appear or confirmation it's not there
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(driver1 -> {
                        List<WebElement> notes = driver1.findElements(By.xpath(noteXpath));
                        return !notes.isEmpty();
                    });
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

    /**
     * Constructs the XPath for locating a note by its title in the pinned section.
     *
     * @param noteTitle The title or content text to search for
     * @return Formatted XPath string to locate the note element
     */
    private String buildNoteXpath(String noteTitle) {
        return String.format(
                "//div[contains(@class,'IZ65Hb-n0tgWb')]" +
                        "[.//div[@role='textbox' and contains(.,'%s')]]",
                noteTitle);
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
        newNoteComponent.clickNewNote();

        // Включаем режим чек-листа
        WebElement checklistToggle = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath("//div[@aria-label='New list']")));
        checklistToggle.click();

        // Ввод заголовка
        //wait.until(ExpectedConditions.visibilityOf(titleField));
        //titleField.sendKeys(title);
        newNoteComponent.setTitle(title);

        // Ввод каждого элемента чек-листа
        for (String item : items) {
            WebElement inputField = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath("//div[@aria-label='List item']")));
            inputField.sendKeys(item);
            inputField.sendKeys(Keys.ENTER);
        }

        // Закрытие заметки
        //closeNoteBtn.click();
        newNoteComponent.close();
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

    public void changeNoteColor(String title, String color) {
        NoteCardComponent note = getNoteByTitle(title);
        if (note != null) {
            note.changeColorTo(color);
        }
    }
}
