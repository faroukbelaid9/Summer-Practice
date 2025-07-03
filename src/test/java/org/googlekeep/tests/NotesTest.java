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
}
