import java.util.ArrayList;
import java.util.HashMap;

public class BranchInfo {
	String branchName;
	String creationDate;
	String lastModified;
	Double commitPercentage;
	ArrayList<CommitInfo> commitList;
	HashMap<String,Double> percentPerCommiter;
	
	public BranchInfo(){
		branchName = null;
		creationDate = null;
		lastModified = null;
		commitList = new ArrayList<CommitInfo>();
		percentPerCommiter = new HashMap<String,Double>();
	}
}

class CommitInfo {
	String id;
	String message;
	String date;
	String author;
	String tag;
	
	public CommitInfo(){
		id = null;
		message = null;
		date = null;
		author = null;
		tag = "";
	}
}

class Committer{
	Integer numOfCommits;
	Double commitPercentage;
	Double commitsPerDay;
	Double commitsPerMonth;
	Double commitsPerYear;
	
	public Committer(){
		numOfCommits = 0;
	}
}
