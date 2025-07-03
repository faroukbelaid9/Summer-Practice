package org.googlekeep.tests;

import org.googlekeep.BaseTest;
import org.googlekeep.pages.NotesPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NotesTest extends BaseTest {
    private NotesPage notesPage;
    private final String TEST_NOTE_TITLE = "Test Note " + System.currentTimeMillis();

    @BeforeMethod
    public void setup() {
        notesPage = new NotesPage(driver);
    }

    @Test(priority = 1)
    public void testEmptyNote() throws InterruptedException {
        Thread.sleep(2000);
        int initialCount = notesPage.getCurrentNoteCount();
        notesPage.createEmptyNote();
        Assert.assertFalse(notesPage.isNoteSaved(initialCount),
                "Empty note shouldn't be saved");
    }

    @Test(priority = 2)
    public void testPinNote() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        notesPage.pinNoteByTitle(TEST_NOTE_TITLE);
        Thread.sleep(2000);
        Assert.assertTrue(notesPage.isNotePinned(TEST_NOTE_TITLE));
    }

    @Test(priority = 3)
    public void testArchiveNote() throws InterruptedException {
        notesPage.createNote(TEST_NOTE_TITLE);
        notesPage.archiveNoteByTitle(TEST_NOTE_TITLE);
        Thread.sleep(2000);
        Assert.assertTrue(notesPage.isNoteArchived(TEST_NOTE_TITLE));
    }

    @Test(priority = 4)
    public void testUndoDelete() {
        notesPage.createNote(TEST_NOTE_TITLE);
        int initialCount = notesPage.getCurrentNoteCount();
        notesPage.deleteNoteByTitle(TEST_NOTE_TITLE, true);
        Assert.assertEquals(notesPage.getCurrentNoteCount(), initialCount);
    }
}
