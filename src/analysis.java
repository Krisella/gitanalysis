import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class analysis {
	
	public static void main(String[] args) throws IOException{
		
		String directory = "C:\\Users\\Aruil\\Documents\\temp_git_repo\\jgit";
		Process proc = null;
		ProcessBuilder pb = new ProcessBuilder("git", "ls-files");
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
		
		
		

		// read any errors from the attempted command
//		System.out.println("Here is the standard error of the command (if any):\n");
//		while ((s = stdError.readLine()) != null) {
//		    System.out.println(s);
//		}
	}
}
