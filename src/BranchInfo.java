import java.util.ArrayList;

public class BranchInfo {
	String BranchName;
	String CreationDate;
	String LastModified;
	ArrayList<CommitInfo> CommitList;
	
	public BranchInfo(){
		BranchName = null;
		CreationDate = null;
		LastModified = null;
		CommitList = new ArrayList<CommitInfo>();
	}
}

class CommitInfo {
	String id;
	String message;
	String date;
	String author;
	
	public CommitInfo(){
		id = null;
		message = null;
		date = null;
		author = null;
	}
}
