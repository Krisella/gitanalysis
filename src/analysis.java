import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class analysis {
	
	static HashMap<String,Committer> committers;
	static ArrayList<String> branches;
	static Integer CommitCount;
	static ArrayList<BranchInfo> branchInfoArray;
	static Integer numOfInsertions;
	static Integer numOfDeletions;
	static Integer num_of_files;
	static Integer num_of_lines;
	static Integer num_of_branches;
	static Integer num_of_tags;
	
	public static void main(String[] args) throws IOException, URISyntaxException{
		
		String repoPath = args[0];
		String outputPath = args[1];
		
		numOfInsertions = 0;
		numOfDeletions = 0;
		committers = new HashMap<String,Committer>(); 
		branches = new ArrayList<String>();
		
		String directory = repoPath;
		Process proc = null;
		ProcessBuilder pb;
		pb = new ProcessBuilder("git", "ls-files");
		pb.directory(new File(directory));
		proc = pb.start();
		

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));


		// read the output from the command
//		System.out.println("Here is the standard output of the command:\n");
		String s = null;
		num_of_files = 0;
		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    num_of_files++;
		}
	//	System.out.println("Number of files in repository: " + num_of_files);
		stdInput.close();
		
		//2nd
		
		pb = new ProcessBuilder("git", "diff","--stat","4b825dc642cb6eb9a060e54bf8d69288fbee4904");
		pb.directory(new File(directory));
		proc = pb.start();
		
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String last_line = null;
		while ((s = stdInput.readLine()) != null) {
		   // System.out.println(s);
		    last_line = s;
		} 
		String[] parts = last_line.split(" ");
//		System.out.println("Total number of lines " + parts[4]);
		num_of_lines = Integer.valueOf(parts[4]);
		stdInput.close();
		
		//3rd
		
		pb = new ProcessBuilder("git", "branch");
		pb.directory(new File(directory));
		proc = pb.start();
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		num_of_branches = 0;
		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    num_of_branches++;
		    String[] cleanstr = s.split(" ");
		    if(cleanstr.length > 2)
		    	branches.add(cleanstr[2]);
		    else
			    branches.add(cleanstr[1]);
		}
	//	System.out.println("Number of branches in repository: " + num_of_branches);
		stdInput.close();
		
		
		pb = new ProcessBuilder("git", "tag");
		pb.directory(new File(directory));
		proc = pb.start();
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		num_of_tags = 0;
		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    num_of_tags++;
		}
//		System.out.println("Number of tags in repository: " + num_of_tags);
		stdInput.close();
		
		
		pb = new ProcessBuilder("git","--no-pager","log","--pretty=tformat:%aN","--all");
		pb.directory(new File(directory));
		proc = pb.start();
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));		

		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    		    
		    Committer c = committers.get(s);
		    if (c == null){
		    	c = new Committer();
		    	c.numOfCommits = 1;
		    }
		    else
		    	c.numOfCommits = c.numOfCommits + 1;
		    
		    committers.put(s, c);	    	
		}
		
//		System.out.println("Number of commiters in repository: " + committers.size());
		
		Set<String> keys = committers.keySet();
		int i=0;
		for(String str: keys)
		{
			i+=committers.get(str).numOfCommits;
		    
		}
		CommitCount = i;
//		System.out.println("Commit count: " + i);
		
		stdInput.close();
		
		//4th
		
		HashMap<String,String> tag_relations = new HashMap<String,String>();
		ArrayList<String> tag_list = new ArrayList<String>();
		
		//get tag list for each commit
		Process get_tags=null;
		ProcessBuilder get_tags_builder = new ProcessBuilder("git","tag");
		get_tags_builder.directory(new File(directory));
		get_tags = get_tags_builder.start();
		BufferedReader tag_input =  new BufferedReader(new InputStreamReader(get_tags.getInputStream()));
		String temp;
		while ((temp = tag_input.readLine()) != null) {
			tag_list.add(temp);
		}
		tag_input.close();
		for(String str: tag_list){
			get_tags_builder = new ProcessBuilder("git","rev-list","-n","1",str);
			get_tags_builder.directory(new File(directory));
			get_tags = get_tags_builder.start();
			tag_input =  new BufferedReader(new InputStreamReader(get_tags.getInputStream()));
			while ((temp = tag_input.readLine()) != null) {
				tag_relations.put(temp,str);
			}
			tag_input.close();
		}
		
		branchInfoArray = new ArrayList<BranchInfo>();
		int k=0;
		for (String str: branches){
			
			BranchInfo branchinfo = new BranchInfo();
			branchinfo.branchName = str;
			CommitInfo commitinfo;
			pb = new ProcessBuilder("git","--no-pager","log","--pretty=format:%H%n%aN%n%ad%n%s%n%n%b%nendofbody","--date-order",str,"--reverse");
			pb.directory(new File(directory));
			proc = pb.start();
			stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));	
			
			while ((s = stdInput.readLine()) != null) {
				
				k++;
				commitinfo = new CommitInfo();
				commitinfo.id = s;
				commitinfo.tag = tag_relations.get(s);
				s = stdInput.readLine();
				commitinfo.author = s;
				s = stdInput.readLine();
				commitinfo.date = s;
				
				StringBuilder strbuilder = new StringBuilder("");
				while(!((s=stdInput.readLine()).equals("endofbody"))){
					strbuilder.append(s);
					strbuilder.append("\n");
				}
				commitinfo.message = strbuilder.toString();
				
				branchinfo.commitList.add(commitinfo);
			}
			branchinfo.creationDate = branchinfo.commitList.get(0).date;
			branchinfo.lastModified = branchinfo.commitList.get(branchinfo.commitList.size()-1).date;
	//		System.out.println(branchinfo.branchName + " Creation Date: " + branchinfo.creationDate + " \nLast Modified: " + branchinfo.lastModified + "\n");
			branchInfoArray.add(branchinfo);
			stdInput.close();
		}
		
//		System.out.println("Number of commits: " + k);
		
		BranchStatistics();
		calculateActiveTime(repoPath);
		calculateNumOfChanges(repoPath);
		InjectCode l = new InjectCode();

		
		if(outputPath.charAt(outputPath.length()-1) != '/' && outputPath.charAt(outputPath.length()-1)!='\\'){
			outputPath = outputPath + "/";
		}
		l.inject(outputPath);
		File dir = new File(outputPath + "css");
		dir.mkdir();
		dir = new File(outputPath + "js");
		dir.mkdir();
		
		copyResourceFile("bootstrap.css", outputPath + "/css/");
		copyResourceFile("bootstrap.css.map", outputPath + "/css/");
		copyResourceFile("bootstrap.min.css", outputPath + "/css/");
		copyResourceFile("bootstrap-table.css", outputPath + "/css/");
		copyResourceFile("bootstrap-theme.css", outputPath + "/css/");
		copyResourceFile("bootstrap-theme.css.map", outputPath + "/css/");
		copyResourceFile("bootstrap-theme.min.css", outputPath + "/css/");
		copyResourceFile("styles.css", outputPath + "/css/");
		copyResourceFile("bootstrap.min.js", outputPath + "/js/");
		copyResourceFile("chart.min.js", outputPath + "/js/");
		copyResourceFile("chart-data.js", outputPath + "/js/");
		copyResourceFile("easypiechart.js", outputPath + "/js/");
		copyResourceFile("easypiechart-data.js", outputPath + "/js/");
		copyResourceFile("jquery-1.11.1.min.js", outputPath + "/js/");
		copyResourceFile("lumino.glyphs.js", outputPath + "/js/");
		
	}
	
	public static File copyResourceFile(String p, String path) throws IOException{
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(p);
		
		File f = new File(path + p);
		
		FileOutputStream out = new FileOutputStream(f);
		byte[] buffer = new byte[1024];
		int count = 0;
		while((count = in.read(buffer)) != -1){
			out.write(buffer, 0, count);
		}
		out.close();
		
		return f;
	}
	
	public static void BranchStatistics(){
		
		Set<String> keys = committers.keySet();
		for(String str: keys){
			Committer c = committers.get(str);
			c.commitPercentage = (((double) c.numOfCommits / CommitCount ) * 100);
			committers.put(str, c);
		}
		
		for(BranchInfo b: branchInfoArray){
			b.commitPercentage =  (((double) b.commitList.size() / CommitCount ) * 100);
			
			HashMap<String, Integer> commitsToBranch = new HashMap<String, Integer>();
			for(CommitInfo c: b.commitList){
				
				Integer i = commitsToBranch.get(c.author);
				if(i == null)
					i = 1;
				else
					i++;
				commitsToBranch.put(c.author, i);
			}
			
			keys = commitsToBranch.keySet();
			for(String str: keys){
				Double percentage = (double) (((double) commitsToBranch.get(str) / b.commitList.size() ) * 100);
				b.percentPerCommiter.put(str, percentage);
			}
 		}
				
	}
	
	public static void calculateActiveTime(String repo) throws IOException{
		String directory = repo;

		Process proc = null;
		ProcessBuilder pb;
		pb = new ProcessBuilder("git", "--no-pager","log","--pretty=format:%ct","--date-order","--reverse");
		pb.directory(new File(directory));
		proc = pb.start();
		
		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));
		String s = null;
		String temp = null;
		String firstDate = null;
		String lastDate;
		boolean flag = false;
		while ((s = stdInput.readLine()) != null) {
			if(flag == false){
				flag = true;
				firstDate = s;
			}
			temp = s;
		}
		lastDate = temp;
		
		Double days, months, years;
		Integer diff = Integer.valueOf(lastDate) - Integer.valueOf(firstDate);
		days =  ((double)diff / (3600*24));
		months = days / 30 ;
		years = months / 12;
		
		Set<String> keys = committers.keySet();
		for(String str: keys){
			Committer c = committers.get(str);
			c.commitsPerDay = c.numOfCommits / days;
			c.commitsPerMonth = c.numOfCommits / months;
			c.commitsPerYear = c.numOfCommits / years;
			
			committers.put(str, c);
		}
	}
	
	public static void calculateNumOfChanges(String repo) throws IOException{
		String directory = repo;

		Process proc = null;
		ProcessBuilder pb;
		pb = new ProcessBuilder("git", "--no-pager","log","--pretty=short","--numstat","--all");
		pb.directory(new File(directory));
		proc = pb.start();
		
		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));
		
		String s=null;
		while ((s = stdInput.readLine()) != null) {
			
			String[] parts = s.split(" ");
			if( parts[0].equals("commit")){
				
				for(int i=0; i<4; i++)
					s=stdInput.readLine();
				
				while ((s = stdInput.readLine()) != null && !s.equals("")) {
					
					parts = s.split("\t");
					if(!parts[0].equals("-")){
						numOfInsertions += Integer.valueOf(parts[0]);
						numOfDeletions += Integer.valueOf(parts[1]);
					}
				}
				if(s==null)
					break;
			}
		}
	}
}
