package gitlet;
import java.io.File;
import java.io.Serializable;

/** Blob class stores data.
 * @author Devun Amoranto
 */
public class Blob implements Serializable {

    /** A new blob from file or directory F. */
    public Blob(File f) {
        _contents = Utils.readContentsAsString(f);
        _hashString = Utils.sha1(_contents);
    }

    /** Returns true iff B is the same. */
    public boolean isSameVersion(Blob b) {
        return b._hashString.equals(this._hashString);
    }

    /** Returns the string of this blob. */
    public String getString() {
        return _contents;
    }

    /** Returns the hashed contents. */
    public String getHash() {
        return _hashString;
    }

    /** Blob contents. */
    private String  _contents;

    /** The specific file and version being tracked. */
    private File _file;

    /** The SHA-1 hash of this blob. */
    private String _hashString;
}
