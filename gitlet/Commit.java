package gitlet;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.HashMap;
import java.io.File;
import java.util.Date;

/** A commit.
 * @author Devun Amoranto
 */
public class Commit implements Serializable {

    /** This commit with MESSAGE and PARENT and MERGEPARENT,
     *  and BRANCH and FILESTOCOMMIT and FILESTOREMOVE.
     SAVEFILE determines if we are serializing this stuff.. */
    public Commit(String message, Commit parent, Commit mergeParent,
                  HashSet<File> filesToCommit,
                  HashSet<File> filesToRemove,
                  String branch, boolean saveFile) {
        if (filesToCommit == null) {
            exit("No files have been staged.");
        }
        _parent = parent;
        _branchHead = branch;
        _mergedParent = mergeParent;
        if (parent == null) {
            _blobs = new HashMap<String, String>();
        } else {
            HashMap<String, String> parentBlobs = _parent.getBlobs();
            _blobs = new HashMap<String, String>();
            for (String key : parentBlobs.keySet()) {
                _blobs.put(key, parentBlobs.get(key));
            }
            for (File f : filesToRemove) {
                _blobs.remove(f.getName());
            }

        }
        for (File f : filesToCommit) {
            Blob b = new Blob(f);
            String hashed = Utils.sha1(Utils.serialize(b));
            if (!_blobs.containsKey(f.getName())) {
                _blobs.put(f.getName(), hashed);
            } else {
                _blobs.replace(f.getName(), hashed);
            }
            if (saveFile) {
                File toSave = new File(hashed);
                writeFile(hashed, b);
            }
        }
        _message = message;
        _commitID = Utils.sha1(Utils.serialize(this));
        setTime();
        if (saveFile) {
            writeContents();
        }
    }
    /** Sets the timestamp. */
    private void setTime() {
        Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        SimpleDateFormat s = new SimpleDateFormat("EEE MMM d HH:mm:ss YYYY Z");
        Date d = new Date();
        if (_message.equals("initial commit")) {
            d.setTime(0);
        }
        _date = d;
        _timestamp = s.format(d);
    }

    /** Returns this commit's parent. */
    public Commit getParent() {
        return _parent;
    }

    /** Returns true if this has a merge parent. */
    public boolean hasMergeParent() {
        return _mergedParent != null;
    }

    /** Returns the merge parent. */
    public Commit getMergeParent() {
        return _mergedParent;
    }
    /** Returns the branch head. */
    public String getBranch() {
        return _branchHead;
    }

    /** Returns the first 7 digits of both parents. */
    public String bothParents() {
        return "Merge: " + _parent.getCommitID().substring(0, 7)
                + " " + _mergedParent.getCommitID().substring(0, 7);
    }

    /** Returns deserialized file BLOB with NAME in this commit. */
    public Blob getFile(String name) {
        if (!_blobs.keySet().contains(name)) {
            return null;
        }
        String serialized = _blobs.get(name);
        return Utils.readObject(Utils.join(_workingDirectory,
                serialized), Blob.class);
    }

    /** Returns F as a string. */
    public String fileAsString(File f) {
        return Utils.readContentsAsString(f);
    }

    /** Returns the hashcode of NAME. */
    public String fileAsHash(String name) {
        File f = Utils.join(_workingDirectory, _blobs.get(name));
        Blob b = Utils.readObject(f, Blob.class);
        return b.getHash();
    }

    /** Returns the blobs. */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }


    /** Writes contents (of B with SHA-1 READ) values to .objects/NAME. */
    private void writeFile(String name, Blob b) {
        checkPersistence();
        File result = Utils.join(_workingDirectory, name);
        try {
            result.createNewFile();
        } catch (IOException e) {
            exit(e.getMessage());
        }
        Utils.writeObject(result, b);
    }

    /** Exits with MSG. */
    public void exit(String msg) {
        System.out.println(msg);
        System.exit(0);
    }

    /** Returns whether OTHER is the same instance. */
    public boolean isSameCommit(Commit other) {
        return this.getCommitID().equals(other.getCommitID());
    }


    /** Writes this to .objects/ folder. */
    private void writeContents() {
        checkPersistence();
        File f = Utils.join(_workingDirectory, _commitID);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            exit(e.getMessage());
        }
        Utils.writeObject(f, this);
    }

    /** Makes sure that .objects/ has been initialized. */
    private void checkPersistence() {
        if (!_workingDirectory.exists()) {
            _workingDirectory.mkdir();
        }
    }

    /** Returns a String representation of this commit. */
    public String toString() {
        return "commit " + _commitID + "\r\n" + "Date: "
                + _timestamp + "\r\n" + _message + "\r\n";
    }

    /** Returns this commit's message. */
    public String getMessage() {
        return _message;
    }

    /** Returns the working directory. */
    public File getWorkingDirectory() {
        return _workingDirectory;
    }

    /** Returns this commit's timestamp. */
    public String getTimeStamp() {
        return _timestamp;
    }

    /** Returns the Date instance of this commit. */
    public Date getDateInstance() {
        return _date;
    }

    /** Returns the Commit's ID. */
    public String getCommitID() {
        return _commitID;
    }

    /** The message of this commit. */
    private String _message;
    /** The working directory. */
    private File _workingDirectory = Utils.join(Main.repository(), ".objects/");
    /** This commit's hash value. */
    private String _commitID;
    /** The staging area. */
    private File _stage = Utils.join(Main.repository(), ".stage/");
    /** The files brought in from the staging area to be commited. */
    private HashMap<String, String> _blobs;
    /** The optional second parent for a merged commit. */
    private Commit _mergedParent;
    /** Timestamp. */
    private String _timestamp;
    /** Parent commit. */
    private Commit _parent;
    /** The name of this commit's branch. */
    private String _branchHead;
    /** For testing purposes, returns this commit's Date instance. */
    private Date _date;
}
