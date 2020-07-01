package gitlet;
import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.Set;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Devun Amoranto
 */
public class Main {
    /** Variables that need serialization. */
    static final String [] VARIABLES = new String[] {
        "_activePointer", "_stagedToAdd", "_abbreviations",
        "_stagedToRemove", "_branches", "_currentBranch",
        "_allCommits"};
    /** The place where files are found. */
    private static File _repository = new File(".gitlet/");
    /** The active pointer. */
    private static Commit _activePointer;
    /** Abbreviated commit ID's. */
    private static HashMap<String, String> _abbreviations;
    /** Tracks all commits ever made. */
    private static HashSet<String> _allCommits;
    /** The current branch. */
    private static String _currentBranch;
    /** All the commit and blob objects. */
    private static File _objects = Utils.join(_repository, ".objects/");
    /** The staging area. */
    private static HashSet<String> _stagedToAdd;
    /** Files staged to be removed. */
    private static HashSet<String> _stagedToRemove;
    /** The staged merge parent. */
    private static Commit _merger;
    /** The staging area. */
    private static File _stage = Utils.join(_repository, ".stage/");
    /** All of the branch heads. */
    private static HashMap<String, Commit> _branches;
    /** Command pattern. */

    private static final Pattern COMMAND = Pattern.compile("FIXME");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            exit("Please enter a command.");
        }
        updatePersistence();
        processCommand(args);
    }

    /** Returns an array of Strings ARGS into a single line. */
    private static String convertToLine(String[] args) {
        String toReturn = "";
        int x = 0;
        while (x < args.length - 1) {
            toReturn += args[x] + " ";
            x++;
        }
        toReturn += args[x];
        return toReturn;
    }

    /** Returns the .gitlet/ folder. */
    public static File repository() {
        return _repository;
    }
    /** Updates the values of this to match persistence. */
    @SuppressWarnings("unchecked")
    private static void updatePersistence() {
        if (_repository.exists()) {
            for (String v : VARIABLES) {
                File f = Utils.join(_repository, v);
                if (v.equals("_activePointer")) {
                    _activePointer = Utils.readObject(f, Commit.class);

                } else if (v.equals("_branches")) {
                    _branches = Utils.readObject(f, HashMap.class);
                } else if (v.equals("_stagedToAdd")) {
                    _stagedToAdd = Utils.readObject(f, HashSet.class);
                } else if (v.equals("_currentBranch")) {
                    _currentBranch = Utils.readObject(f, String.class);
                } else if (v.equals("_abbreviations")) {
                    _abbreviations = Utils.readObject(f, HashMap.class);
                } else if (v.equals("_allCommits")) {
                    _allCommits = Utils.readObject(f, HashSet.class);
                } else {
                    _stagedToRemove = Utils.readObject(f, HashSet.class);
                }
            }
        }
    }

    /** Writes this state's objects to files for next time. */
    private static void exitPersistence() {
        File f = Utils.join(_repository, "_activePointer");
        File g = Utils.join(_repository, "_branches");
        File h = Utils.join(_repository, "_stagedToRemove");
        File i = Utils.join(_repository, "_stagedToAdd");
        File j = Utils.join(_repository, "_currentBranch");
        File k = Utils.join(_repository, "_abbreviations");
        File l = Utils.join(_repository, "_allCommits");
        Utils.writeObject(f, _activePointer);
        Utils.writeObject(g, _branches);
        Utils.writeObject(h, _stagedToRemove);
        Utils.writeObject(i, _stagedToAdd);
        Utils.writeObject(j, _currentBranch);
        Utils.writeObject(k, _abbreviations);
        Utils.writeObject(l, _allCommits);
    }

    /** Initializes the repository, and returns whether it already existed. */
    public static boolean setUpPersistence() {
        if (!_repository.exists()) {
            _repository.mkdir();
            _stage.mkdir();
            return false;
        }
        return true;
    }
    /** Checks lengths of ARGS. */
    private static void checkLengths(String[] args) {
        String cm = args[0];
        if (cm.equals("init") || cm.equals("log")
                || cm.equals("global-log") || cm.equals("status")) {
            checkOperandFormat(args, 0);
        } else if (cm.equals("add") || cm.equals("commit")
                || cm.equals("rm") || cm.equals("find")
                || cm.equals("branch") || cm.equals("rm-branch")
                || cm.equals("reset") || cm.equals("merge")) {
            checkOperandFormat(args, 1);
        } else if (cm.equals("checkout")) {
            return;
        } else {
            exit("No command with that name exists.");
        }
    }

    /** Makes sure .gitlet with ARGS has been
     *  initialized if it needs to be. */
    private static void checkifInitialized(String [] args) {
        String c = args[0];
        boolean dangerousArgsCaught = false;
        if (!c.equals("init")) {
            dangerousArgsCaught = true;
        }
        if (!_repository.exists() && dangerousArgsCaught) {
            exit("Not in an initialized gitlet directory.");
        }
    }

    /** Processes ARGS. */
    private static void processCommand(String[] args) {
        String command = args[0];
        checkLengths(args);
        checkifInitialized(args);
        switch (command) {
        case "init":
            initializeRepository();
            break;
        case "add":
            stageFiles(args[1]);
            break;
        case "commit":
            makeCommit(args[1]);
            break;
        case "rm":
            unstageFiles(args[1]);
            break;
        case "log":
            retrieveLog();
            break;
        case "global-log":
            retreiveGlobalLog();
            break;
        case "find":
            findCommits(args[1]);
            break;
        case "status":
            displayStatus();
            break;
        case "checkout":
            if (args.length > 4 || args.length < 2) {
                exit("Incorrect operands.");
            }
            checkoutFiles(args);
            break;
        default:
            processCommandContinued(args);
        }
    }

    /** Helps ARGS pass style check. */
    private static void processCommandContinued(String[] args) {
        switch (args[0]) {
        case "branch":
            createBranch(args[1]);
            break;
        case "rm-branch":
            removeBranch(args[1]);
            break;
        case "reset":
            reset(args[1], true);
            break;
        case "merge":
            mergeBranch(args[1]);
            break;
        default:
            exit("No command with that name exists.");
        }
    }
    /** Checks whether LENGTH of ARR is ok. */
    private static void checkOperandFormat(String[] arr, int length) {
        if (arr.length - 1 != length) {
            exit("Incorrect operands.");
        }
    }
    /** Returns the active pointer. */
    public static Commit getActivePointer() {
        return _activePointer;
    }

    /** Clears the stage. */
    private static void clearStage() {
        _stagedToAdd.clear();
        _stagedToRemove.clear();
        File[] files = _stage.listFiles();
        for (File f : files) {
            f.delete();
        }
    }

    /** Initializes the repo (init). */
    public static void initializeRepository() {
        _stagedToRemove = new HashSet<String>();
        _allCommits = new HashSet<String>();
        _stagedToAdd = new HashSet<String>();
        _branches = new HashMap<String, Commit>();
        _abbreviations = new HashMap<String, String>();
        if (setUpPersistence()) {
            exit("A Gitlet version-control"
                    + " system already exists in the current directory.");
        }
        HashSet<File> emptyList = new HashSet<File>();
        _activePointer = new Commit("initial commit",
                null, null, emptyList, emptyList, "master", true);
        _branches.put("master", _activePointer);
        _currentBranch = "master";
        _allCommits.add(_activePointer.getCommitID());
        String id = _activePointer.getCommitID();
        for (int x = 1; x <= id.length(); x++) {
            _abbreviations.put(id.substring(0, x), id);
        }
        File f = Utils.join(_repository, "_activePointer");
        File g = Utils.join(_repository, "_branches");
        File h = Utils.join(_repository, "_stagedToRemove");
        File i = Utils.join(_repository, "_stagedToAdd");
        File j = Utils.join(_repository, "_currentBranch");
        File k = Utils.join(_repository, "_abbreviations");
        File l = Utils.join(_repository, "_allCommits");
        try {
            f.createNewFile();
            g.createNewFile();
            h.createNewFile();
            i.createNewFile();
            j.createNewFile();
            k.createNewFile();
            l.createNewFile();
        } catch (IOException e) {
            exit(e.getMessage());
        }
        exitPersistence();
    }

    /** Stages FILENAME to be commited (add). */
    public static void stageFiles(String fileName) {
        File repo = new File(fileName);
        File stagedCopy = Utils.join(_stage, fileName);
        if (!new File(fileName).exists()
                && !_activePointer.getBlobs().containsKey(fileName)) {
            exit("File does not exist.");
        }
        String compare = "";
        if (_activePointer.getFile(fileName) != null) {
            compare = _activePointer.fileAsHash(fileName);
        }
        if (_stagedToRemove.contains(fileName)) {
            _stagedToRemove.remove(fileName);
            File f = new File(fileName);
            try {
                f.createNewFile();
            } catch (IOException e) {
                exit(e.getMessage());
            }
            Utils.writeContents(f, Utils.readContents(stagedCopy));
            stagedCopy.delete();
        }  else {
            String currentVersion = Utils.sha1(Utils.readContents(repo));
            if (!compare.equals("") && currentVersion.equals(compare)) {
                if (stagedCopy.exists()) {
                    stagedCopy.delete();
                    _stagedToAdd.remove(fileName);
                }
            } else {
                try {
                    if (!stagedCopy.exists()) {
                        stagedCopy.createNewFile();
                    }
                } catch (IOException e) {
                    exit(e.getMessage());
                }
                Utils.writeContents(stagedCopy,
                        Utils.readContents(new File(fileName)));
                if (!_stagedToAdd.contains(fileName)) {
                    _stagedToAdd.add(fileName);
                }
            }
        }
        exitPersistence();
    }

    /** Makes commit with MESSAGE (commit). */
    public static void makeCommit(String message) {
        if (_stagedToRemove.size() == 0 && _stagedToAdd.size() == 0) {
            exit("No changes added to the commit.");
        }
        if (message.equals("")) {
            exit("Please enter a commit message.");
        }
        HashMap<String, String> files = _activePointer.getBlobs();
        HashSet<File> newFiles = new HashSet<File>();
        HashSet<File> filesToRemove = new HashSet<File>();
        for (String key : files.keySet()) {
            File stage = Utils.join(_stage, key);
            if (_stagedToAdd.contains(key)) {
                File f = new File(key);
                newFiles.add(stage);
            } else if (_stagedToRemove.contains(key)) {
                filesToRemove.add(new File(key));
            }
        }
        for (String key : _stagedToAdd) {
            if (!newFiles.contains(key)) {
                File shorterName = new File(key);
                try {
                    shorterName.createNewFile();
                } catch (IOException e) {
                    exit(e.getMessage());
                }
                Utils.writeContents(shorterName,
                        Utils.readContentsAsString(Utils.join(_stage, key)));
                newFiles.add(shorterName);
            }
        }
        Commit c = new Commit(message, _activePointer, _merger,
                newFiles, filesToRemove, _currentBranch, true);
        String id = c.getCommitID();
        for (int x = 1; x <= id.length(); x++) {
            String toPut = id.substring(0, x);
            if (!_abbreviations.containsKey(toPut)) {
                _abbreviations.put(toPut, id);
            }
        }
        _branches.replace(_currentBranch, c);
        _activePointer = c;
        _merger = null;
        _allCommits.add(c.getCommitID());
        clearStage();
        exitPersistence();
    }

    /** Unstage file TOREMOVE that has been added (rm). */
    public static void unstageFiles(String toRemove) {
        if (!_stagedToAdd.contains(toRemove)
                && !_activePointer.getBlobs().containsKey(toRemove)) {
            exit("No reason to remove the file.");
        }
        File stagedCopy = Utils.join(_stage, toRemove);
        if (_stagedToAdd.contains(toRemove)) {
            _stagedToAdd.remove(toRemove);
            stagedCopy.delete();
        } else if (_activePointer.getBlobs().containsKey(toRemove)) {
            File f = new File(toRemove);
            _stagedToRemove.add(toRemove);
            if (f.exists()) {
                try {
                    stagedCopy.createNewFile();
                } catch (IOException e) {
                    exit(e.getMessage());
                }
                Utils.writeContents(stagedCopy, Utils.readContentsAsString(f));
                f.delete();
            }
        }
        exitPersistence();
    }

    /** Gets the commit log for one single branch (log). */
    public static void retrieveLog() {
        Commit pointer = _activePointer;
        while (pointer != null) {
            System.out.println("===");
            System.out.println("commit " + pointer.getCommitID());
            if (pointer.hasMergeParent()) {
                System.out.println(pointer.bothParents());
            }
            System.out.println("Date: " + pointer.getTimeStamp());
            System.out.println(pointer.getMessage());
            System.out.println();
            if (pointer.getParent() == null) {
                break;
            } else {
                pointer = pointer.getParent();
            }
        }
    }

    /** Gets all commits ever made with optionl QUERY (global-log & find). */
    public static void retreiveGlobalLog() {
        for (String s : _allCommits) {
            Commit pointer = Utils.readObject(
                    Utils.join(_objects, s), Commit.class);
            System.out.println("===");
            System.out.println("commit " + pointer.getCommitID());
            if (pointer.hasMergeParent()) {
                System.out.println(pointer.bothParents());
            }
            System.out.println("Date: " + pointer.getTimeStamp());
            System.out.println(pointer.getMessage());
            System.out.println();
        }
    }

    /** Retreives the commits with a given MESSAGE (find). */
    public static void findCommits(String message) {
        boolean found = false;
        for (String s : _allCommits) {
            Commit pointer = Utils.readObject(
                    Utils.join(_objects, s), Commit.class);
            if (pointer.getMessage().equals(message)) {
                found = true;
                System.out.println(pointer.getCommitID());
            }
        }
        if (!found) {
            exit("Found no commit with that message.");
        }
    }

    /** Exits program with MSG. */
    public static void exit(String msg) {
        System.out.println(msg);
        System.exit(0);
    }

    /** Returns and Sorts a hash map. */
    public static TreeMap<String, Commit> sortBranchSet() {
        TreeMap<String, Commit> sort = new TreeMap<String, Commit>();
        sort.putAll(_branches);
        return sort;
    }

    /** Returns a tree set from hashset S. */
    public static TreeSet<String> sortEntries(HashSet<String> s) {
        TreeSet<String> sort = new TreeSet<String>();
        sort.addAll(s);
        return sort;
    }
    /** Displays the status of the repository (status). */
    public static void displayStatus() {
        System.out.println("=== Branches ===");
        for (String s : sortBranchSet().keySet()) {
            if (s.equals(_currentBranch)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");

        for (String str : sortEntries(_stagedToAdd)) {
            System.out.println(str);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");

        for (String str : sortEntries(_stagedToRemove)) {
            System.out.println(str);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /** Returns S's full commit ID, or throws ERRORMESSAGE if the commit doesn't
     * exist. */
    private static String abbreviatedOrNot(String s, String errorMessage) {
        if (!_abbreviations.containsKey(s)) {
            exit(errorMessage);
        }
        return _abbreviations.get(s);
    }

    /** Checks out files of ARGS from a previous commit (checkout). */
    public static void checkoutFiles(String[] args) {
        if (args[1].equals("--")) {
            Blob f = _activePointer.getFile(args[2]);
            if (f == null) {
                exit("File "
                        + "does not exist in that commit.");
            }
            File workingVersion = new File(args[2]);
            Utils.writeContents(workingVersion, f.getString());
        } else if (args.length == 2) {
            checkoutBranch(args[1]);
        } else if (args[2].equals("--")) {
            try {
                String id = abbreviatedOrNot(
                        args[1], "No commit with that id exists.");
                if (!_allCommits.contains(id)) {
                    exit("No commit with that id exists.");
                }
                Commit c = Utils.readObject(Utils.join(
                        _objects, id), Commit.class);
                Blob f = c.getFile(args[3]);
                if (f == null) {
                    exit(
                            "File does not exist in that commit.");
                }
                File workingVersion = new File(args[3]);
                Utils.writeContents(workingVersion, f.getString());
            } catch (NullPointerException e) {
                exit("No commit with that id exists.");
            }
        } else {
            exit("Incorrect operands.");
        }
    }

    /** Checks out branch B. */
    public static void checkoutBranch(String b) {
        if (!_branches.containsKey(b)) {
            exit("No such branch exists.");
        }
        if (b.equals(_currentBranch)) {
            exit("No need to checkout the current branch.");
        }
        _currentBranch = b;
        reset(_branches.get(b).getCommitID(), false);
        exitPersistence();
    }

    /** Creates a new branch BRANCHNAME at the head (branch). */
    public static void createBranch(String branchName) {
        if (_branches.containsKey(branchName)) {
            exit("A branch with that name already exists.");
        }
        _branches.put(branchName, _activePointer);
        exitPersistence();
    }

    /** Deletes the branch BRANCHNAME (rm-branch). */
    public static void removeBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            exit("A branch with that name does not exist.");
        } else if (branchName.equals(_currentBranch)) {
            exit("Cannot remove the current branch.");
        } else {
            _branches.remove(branchName);
        }
        exitPersistence();
    }

    /** Checks out all files from a given commit with COMMITID(reset).
     *  Updates CHANGEBRANCHHEAD if told to. */
    public static void reset(String commitID, boolean changeBranchHead) {
        String id = abbreviatedOrNot(
                commitID, "No commit with that ID exists.");
        if (!_allCommits.contains(id)) {
            exit("No commit with that ID exists.");
        }
        Commit c = Utils.readObject(
                Utils.join(_objects, id), Commit.class);
        for (String s : c.getBlobs().keySet()) {
            File f = new File(s);
            if (_activePointer.getFile(s) == null && f.exists())  {
                exit("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
        for (String s : _activePointer.getBlobs().keySet()) {
            File f = new File(s);
            if (f.exists() && !c.getBlobs().keySet().contains(s)) {
                f.delete();
            }
        }
        for (String s : c.getBlobs().keySet()) {
            File old = new File(s);
            if (!old.exists()) {
                try {
                    old.createNewFile();
                } catch (IOException e) {
                    exit(e.getMessage());
                }
            }
            Utils.writeContents(old, c.getFile(s).getString());
        }
        _activePointer = c;
        if (changeBranchHead) {
            _branches.replace(_currentBranch, c);
        }
        clearStage();
        exitPersistence();
    }

    /** Merges two BRANCHNAME with the current branch together (merge). */
    public static void mergeBranch(String branchName) {
        if (!_stagedToAdd.isEmpty() || !_stagedToRemove.isEmpty()) {
            exit("You have uncommited changes.");
        }
        if (!_branches.containsKey(branchName)) {
            exit("A branch with that name does not exist.");
        }
        if (branchName.equals(_currentBranch)) {
            exit("Cannot merge a branch with itself.");
        }
        Commit mergeBranch = _branches.get(branchName);
        Commit currentBranch = _activePointer;
        Commit splitPoint = findSplitPoint(
                _branches.get(_currentBranch),
                _branches.get(branchName));

        if (splitPoint.isSameCommit(mergeBranch)) {
            exit("Given branch is an "
                    + "ancestor of the current branch.");
        }
        if (splitPoint.isSameCommit(currentBranch)) {
            checkoutBranch(branchName);
            exit("Current branch fast-forwarded.");
        }
        _merger = mergeBranch;
        Set<String> currentFiles =
                currentBranch.getBlobs().keySet();
        Set<String> mergeFiles =
                mergeBranch.getBlobs().keySet();
        Set<String> splitFiles = splitPoint.getBlobs().keySet();
        for (String s : mergeFiles) {
            File f = new File(s);
            if (f.exists() && !_activePointer.getBlobs().containsKey(s)) {
                exit("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
        boolean b = stageMerge(currentBranch, mergeBranch, splitPoint,
                currentFiles, mergeFiles, splitFiles);
        makeCommit("Merged " + branchName + " into " + _currentBranch + ".");
        if (b) {
            System.out.println("Encountered a merge conflict.");
        }
        exitPersistence();
    }

    /** Stages the correct files from CURRENT, TOMERGE, and SPLIT.
     *  Blobs represented by CURRENTFILES, MERGEFILES, and SPLITFILES.
     *  Returns true iff conflict encountered. */
    private static boolean stageMerge(
            Commit current, Commit toMerge, Commit split,
            Set<String> currentFiles, Set<String> mergeFiles,
            Set<String> splitFiles) {
        boolean encounteredConflict = false;
        Blob currentVersion, branchVersion, splitVersion;
        for (String file : splitFiles) {
            splitVersion = split.getFile(file);
            if (currentFiles.contains(file) && mergeFiles.contains(file)) {
                currentVersion = current.getFile(file);
                branchVersion = toMerge.getFile(file);
                if (currentVersion.isSameVersion(branchVersion)
                        && !currentVersion.isSameVersion(splitVersion)) {
                    checkoutFiles(new String[] {"checkout",
                            toMerge.getCommitID(), "--", file});
                    stageFiles(file);
                } else if (!currentVersion.isSameVersion(splitVersion)
                        && !branchVersion.isSameVersion(splitVersion)
                        && !branchVersion.isSameVersion(currentVersion)) {
                    processConflict(file, currentVersion, branchVersion);
                    encounteredConflict = true;
                }
            } else if (!currentFiles.contains(file)
                    && mergeFiles.contains(file)) {
                branchVersion = toMerge.getFile(file);
                if (!branchVersion.isSameVersion(splitVersion)) {
                    processConflict(file, null, branchVersion);
                    encounteredConflict = true;
                }
            } else if (!mergeFiles.contains(file)
                    && currentFiles.contains(file)) {
                currentVersion = current.getFile(file);
                if (currentVersion.isSameVersion(splitVersion)) {
                    unstageFiles(file);
                } else {
                    processConflict(file, currentVersion, null);
                    encounteredConflict = true;
                }
            }
        }
        for (String file : currentFiles) {
            currentVersion = current.getFile(file);
            if (!splitFiles.contains(file) && mergeFiles.contains(file)) {
                branchVersion = toMerge.getFile(file);
                if (!currentVersion.isSameVersion(branchVersion)) {
                    processConflict(file, currentVersion, branchVersion);
                    encounteredConflict = true;
                }
            }
        }
        for (String file : mergeFiles) {
            if (!splitFiles.contains(file) && !currentFiles.contains(file)) {
                checkoutFiles(new String[] {"checkout",
                        toMerge.getCommitID(), "--", file});
                stageFiles(file);
            }
        }
        return encounteredConflict;
    }

    /** Processes a FILENAME conflict between CURRENT and MERGE. */
    private static void processConflict(
            String fileName, Blob current, Blob merge) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                exit(e.getMessage());
            }
        }
        String toWrite = "<<<<<<< HEAD\n";
        if (current != null) {
            toWrite += current.getString();
        }
        toWrite += "=======\n";
        if (merge != null) {
            toWrite += merge.getString();
        }
        toWrite += ">>>>>>>\n";
        Utils.writeContents(f, toWrite);
        stageFiles(fileName);
    }
    /** Commits a merge. */
    private static void commitMerge() {

    }

    /** Returns the split point for CURRENT
     *  and BRANCH. */
    private static Commit findSplitPoint(
            Commit current, Commit branch) {
        HashMap<String, Object[]> commitInfo = new HashMap<String, Object[]>();
        findCurrentAncestors(current, commitInfo, 0);
        HashMap<String, Object[]> branchAncestors =
                new HashMap<String, Object[]>();
        findCurrentAncestors(branch, branchAncestors, 0);
        for (String s : commitInfo.keySet()) {
            if (branchAncestors.containsKey(s)) {
                commitInfo.get(s)[2] = "red";
            }
        }
        markParents(commitInfo);
        int minDist = Integer.MAX_VALUE;
        Commit toReturn = null;
        for (Object[] o : commitInfo.values()) {
            if (o[2].equals("red")) {
                if ((int) o[1] < minDist) {
                    minDist = (int) o[1];
                    toReturn = (Commit) o[0];
                }
            }
        }
        return toReturn;
    }

    /** Whittles down possible split points for CURRENT. */
    private static void markParents(HashMap<String, Object[]> current) {
        for (Object[] o : current.values()) {
            if (o[2].equals("red")) {
                Commit c = (Commit) o[0];
                Commit p = c.getParent();
                Commit m = c.getMergeParent();
                if (p != null) {
                    if (current.containsKey(p.getCommitID())) {
                        current.get(p.getCommitID())[2] = 1;
                    }
                }
                if (m != null) {
                    if (current.containsKey(m.getCommitID())) {
                        current.get(p.getCommitID())[2] = 1;
                    }
                }
            }
        }
    }

    /** Finds the common ancestors, updating
     *  COMMITS, MARK, and DISTANCE. */
    private static void findCurrentAncestors(
            Commit mark, HashMap<String,
            Object[]> commits, int distance) {
        if (mark != null) {
            if (!commits.containsKey(mark.getCommitID())
                    || (int) commits.get(
                            mark.getCommitID())[1] > distance) {
                commits.put(mark.getCommitID(),
                        new Object[]{mark, distance, "blue"});
                findCurrentAncestors(mark.getParent(),
                        commits, distance + 1);
                findCurrentAncestors(mark.getMergeParent(),
                        commits, distance + 1);
            }
        }
    }
}
