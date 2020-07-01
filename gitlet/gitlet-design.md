# Gitlet Design Document

**Name**: Devun Amoranto

# Classes and Data Structures
## Main
This class runs the main gitlet program. 
### Fields
- File REPOSITORY: The directory ./gitlet, where all operations will take place.
- Pattern COMMAND: This pattern is a utility to recognize input commands by the user.

## Commit
This class contains pointers to blobs and other information to make it easily identifiable.
### Fields 
- String _name: The name of the commit.
- String TIMESTAMP: The calendar representation of the time of commit.
- ArrayList<Blob> _blobs: All of the blobs that this commit is pointing to.
- Commit _parent: The preceding (parent) Commit of this one.
- String _commitID: The SHA-1 value of this commit, for use in persistence.

## Blob
This class contains pointers to specific versions of specific files at specific points in time.
### Fields
- String _hashString: The address of this blob.
- File _file: The (serialized) snapshot of the file being pointed to.

## CommitTree
This class is a utility class for searching for specific commits.
### Fields
- Commit _activePointer: The ACTIVE commit pointer of the tree (Note: There can only be one head at a time!).
- HashMap<String, Commit> _branchNames: The collection of all the SHA-1 pointer values of the branches in the tree. The main branch should be "master."

# Algorithms
## Adding commits to be tracked and creating commits
- Track the specified directory and all subdirectories
- If any tracked files have been changed, create a new blob for them. If not, use the already-instantiated blob.
- If any files have been added (or removed), create new blobs for them (or remove pointers to blobs in the case of removal).
## Making the commits that have been marked for tracking
- Update the active pointer, creating a new commit (with the updated blobs from "gitlet.Main add") with
  the given name and timestamp
## Removing (rm)
- Unstage the given file or directory, removing all NEW blobs that have been added.
## Find
- For every commit with a given name, return that commit in a list.
## Log and global log
- For log, return a list of all ancestors in the current branch.
- For global log, return all commits.
## Status check
- For a list of branches, return CommitTree._branchNames.
## Branch implementation
- When creating a new branch, create a COPY of a specific commit (preserving pointers), with a new pointer TOWARDS it marking the ID of its branch.
- When checking out to a specific branch, find the commit name in CommitTree._branchNames, and make that the active pointer.  
- When calling rm-branch, remove the pointer from the list of branches in CommitTree._branchNames, but do not change the commits.
## Merging branches
- Merge the most recent common ancestors (if the two branches specified have pointers at the same commit, DO NOTHING).
- Read spec for Gitlet, as there are many implementations that are much clearer on the webpage.
- It is like a selective checkout
## Checking out a previous commit
- Recall a previous commit and overwrite the given files (or add/delete) where necessary.
- Create a new branch if ANY edits are made. 

## Persistence
All commits and blobs will be serialized to save space.
### Possible problems
- Serialization can copy pointers, and this may turn into a problem 
  when dealing with branch manipulation
- Be VERY aware of space and time constraints, only adding blobs when necessary and implementing constant search time whenever possible.


