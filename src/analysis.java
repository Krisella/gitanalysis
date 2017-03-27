import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

public class analysis {
	
	public static void main(String[] args) throws IOException{
		
		HashMap<String,Integer> commiters = new HashMap<String,Integer>(); 
		String directory = "C:\\Users\\Aruil\\Documents\\temp_git_repo\\jgit";
		Process proc = null;
		ProcessBuilder pb;
		pb = new ProcessBuilder("git", "ls-files");
		pb.directory(new File(directory));
		proc = pb.start();
		

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

//		BufferedReader stdError = new BufferedReader(new 
//		     InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		System.out.println("Here is the standard output of the command:\n");
		String s = null;
		int num_of_files = 0;
		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    num_of_files++;
		}
		System.out.println("Number of files in repository: " + num_of_files);
		stdInput.close();
		
		//2nd
		
		pb = new ProcessBuilder("git", "diff","--stat","4b825dc642cb6eb9a060e54bf8d69288fbee4904");
		pb.directory(new File(directory));
		proc = pb.start();
		
		//TODO to confirm
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String last_line = null;
		while ((s = stdInput.readLine()) != null) {
		   // System.out.println(s);
		    last_line = s;
		} 
		String[] parts = last_line.split(" ");
		System.out.println("Total number of lines " + parts[4]);
		stdInput.close();
		
		//3rd
		
		pb = new ProcessBuilder("git", "branch");
		pb.directory(new File(directory));
		proc = pb.start();
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		int num_of_branches = 0;
		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    num_of_branches++;
		}
		System.out.println("Number of branches in repository: " + num_of_branches);
		stdInput.close();
		
		
		pb = new ProcessBuilder("git", "tag");
		pb.directory(new File(directory));
		proc = pb.start();
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		int num_of_tags = 0;
		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    num_of_tags++;
		}
		System.out.println("Number of tags in repository: " + num_of_tags);
		stdInput.close();
		
		
		pb = new ProcessBuilder("git","--no-pager","log","--pretty=tformat:%aN","--all");
//		pb = new ProcessBuilder("git","--no-pager","shortlog","-s","-n");

		pb.directory(new File(directory));

		proc = pb.start();
//	    try {
//			proc.waitFor();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));		

		while ((s = stdInput.readLine()) != null) {
		 //   System.out.println(s);
		    		    
		    Integer i = commiters.get(s);
		    if (i == null)
		    	i=1;
		    else
		    	i++;
		    commiters.put(s, i);	    	
		}
		
		System.out.println("Number of commiters in repository: " + commiters.size());
		
//		Set<String> keys = commiters.keySet();
//		for(String str: keys)
//		{
//		    System.out.println(str + ", " + commiters.get(str));
//		}
//		
		stdInput.close();
		
		
		// read any errors from the attempted command
//		System.out.println("Here is the standard error of the command (if any):\n");
//		while ((s = stdError.readLine()) != null) {
//		    System.out.println(s);
//		}
	}
}
