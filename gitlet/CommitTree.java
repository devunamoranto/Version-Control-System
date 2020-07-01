package gitlet;
import java.util.HashMap;

/** Provides a way of finding specific commits, keeping track of branches.
 * @author Devun Amoranto
 * */
public class CommitTree {

    /** The COMMIT at the current active head of this tree. */
    private Commit _activePointer;

    /** The name of this branch (The main branch is "master").
     *  (String, Commit) -->  */
    private HashMap<String, Commit> _branchNames;
}
