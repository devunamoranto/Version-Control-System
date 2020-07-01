package gitlet;
import org.junit.Test;
import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import static org.junit.Assert.*;
/** Collection of tests for the Commit class.
 * @author Devun Amoranto*/
public class CommitTest {

    @Test
    public void initializeTimeAndDate() {
        SimpleDateFormat s = new SimpleDateFormat(
                "EEE MMM d HH:m:ss YYYY Z");
        Commit c = new Commit("testerCommit", null, null,
                new HashSet<File>(), new HashSet<File>(), null, false);
        Date d = c.getDateInstance();
        String representation = c.toString();
        assertEquals("testerCommit", c.getMessage());
        assertEquals(s.format(d), c.getTimeStamp());
    }

    @Test
    public void serializationTestSimple() {
        HashSet<File> emptyList = new HashSet<>();
        Commit c0 = new Commit("thisIsATest", null, null,
                emptyList, emptyList, "branch0", false);
        Commit c1 = new Commit("thisIsATest", null, null,
                emptyList, emptyList, "branch0", false);
        Commit differentBranch = new Commit("thisIsATest",
                null, null, emptyList, emptyList, "branch1", false);
        Commit differentName = new Commit("thisIsNotATest",
                null, null, emptyList, emptyList, "branch0", false);
        Commit differentParent = new Commit("thisIsATest",
                c0, null, emptyList, emptyList,  "branch0", false);
        Commit differentParentButSameBranch = new Commit(
                "thisIsATest", c0, null, emptyList, emptyList,
                "branch0", false);
        assertEquals(c0.getCommitID(), c1.getCommitID());
        assertEquals(differentParent.getCommitID(),
                differentParentButSameBranch.getCommitID());
        assert (!c0.getCommitID().equals(differentBranch.getCommitID()));
        assert (!c0.getCommitID().equals(differentName.getCommitID()));
        assert (!c0.getCommitID().equals(differentParent.getCommitID()));
    }

    @Test
    public void saveCommitObject() {
        Commit c = new Commit("tester", null, null,
                new HashSet<File>(), new HashSet<File>(), "branch0", true);
        String id = c.getCommitID();
        File f = Utils.join(c.getWorkingDirectory(), id);
        Commit retrieved = Utils.readObject(f, Commit.class);
        assertEquals(retrieved.getCommitID(), id);
        assertEquals(retrieved.getMessage(), c.getMessage());
    }

}
