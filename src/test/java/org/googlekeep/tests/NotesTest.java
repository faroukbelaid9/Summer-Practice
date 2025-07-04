package org.googlekeep.tests;

import org.googlekeep.BaseTest;
import org.googlekeep.pages.NotesPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Test suite for Google Keep note operations.
 */
public class NotesTest extends BaseTest {
    private NotesPage notesPage;
    private final String TEST_NOTE_TITLE = "Test Note " + System.currentTimeMillis();
    private final String TEST_LABEL = "TestLabel";
    private final String[] CHECKLIST_ITEMS = {"Item 1", "Item 2", "Item 3"};

    @BeforeMethod
    public void setup() {
        notesPage = new NotesPage(driver);
    }

    @Test(priority = 1)
    public void testEmptyNote() throws InterruptedException {
        Thread.sleep(2000);
        int initialCount = notesPage.getCurrentNoteCount();
        notesPage.createEmptyNote();
        assertFalse(notesPage.isNoteSaved(initialCount), "Empty note shouldn't be saved");
    }

    @Test(priority = 2)
    public void testPinNote() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        notesPage.pinNoteByTitle(TEST_NOTE_TITLE);
        Thread.sleep(2000);
        assertTrue(notesPage.isNotePinned(TEST_NOTE_TITLE));
    }

    @Test(priority = 3)
    public void testArchiveNote() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        notesPage.archiveNoteByTitle(TEST_NOTE_TITLE);
        Thread.sleep(2000);
        assertTrue(notesPage.isNoteArchived(TEST_NOTE_TITLE));
    }

    @Test(priority = 4)
    public void testUndoDelete() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        int initialCount = notesPage.getCurrentNoteCount();
        notesPage.deleteNoteByTitle(TEST_NOTE_TITLE, true);
        Thread.sleep(2000);
        assertEquals(notesPage.getCurrentNoteCount(), initialCount);
    }

    @Test(priority = 5)
    public void testDeleteNote() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        int initialCount = notesPage.getCurrentNoteCount();
        notesPage.deleteNoteByTitle(TEST_NOTE_TITLE, false);
        Thread.sleep(2000);
        assertEquals(notesPage.getCurrentNoteCount(), initialCount - 1,
                "Note should be removed from visible notes after deletion");
    }

    @Test(priority = 6)
    public void testAddLabelToNote() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        notesPage.addLabelToNoteByTitle(TEST_NOTE_TITLE, TEST_LABEL);
        Thread.sleep(2000);
        assertTrue(notesPage.isLabelAttached(TEST_NOTE_TITLE, TEST_LABEL),
                "Label should be attached to the note");
    }

    @Test(priority = 7)
    public void testAddChecklistToNote() throws InterruptedException {
        notesPage.createChecklistNote(TEST_NOTE_TITLE, CHECKLIST_ITEMS);
        Thread.sleep(2000);
        assertTrue(notesPage.isChecklistPresent(TEST_NOTE_TITLE, CHECKLIST_ITEMS),
                "Checklist items should be present in the created note");
    }

    @Test(priority = 8)
    public void testEditNote() {
        String updatedTitle = TEST_NOTE_TITLE + " - Updated";

        // 1. Create the original note
        notesPage.createNote(TEST_NOTE_TITLE);

        // 2. Edit the note title
        notesPage.editNoteTitle(TEST_NOTE_TITLE, updatedTitle);

        // 4. Validate the updated title is present
        assertTrue(notesPage.isNotePresent(updatedTitle), "Updated note title not found");
    }

    @Test(priority = 9)
    public void testSearchNoteByTitle() throws InterruptedException {
        // Create a unique note
        String searchTitle = "Searchable Note " + System.currentTimeMillis();
        notesPage.createNote(searchTitle);

        // Perform search
        notesPage.searchNoteByTitle(searchTitle);
        Thread.sleep(2000); // Give some time for the UI to settle

        // Validate the note appears in search results
        assertTrue(notesPage.isNotePresent(searchTitle), "Searched note should be visible in search results");
    }

    @Test(priority = 10)
    public void testChangeNoteColor() {
        // Create a test note
        String colorTestNote = "Color Test " + System.currentTimeMillis();
        notesPage.createNote(colorTestNote);

        // Change to red
        notesPage.changeNoteColor(colorTestNote, "red");

        // Add a small delay to see the change (optional)
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Change to default (white)
        notesPage.changeNoteColor(colorTestNote, "default");
    }
}
