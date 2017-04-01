import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class InjectCode extends analysis{
	
	public void inject(String s) throws IOException{
		
		String panelBody = "<small>$authorPercent</small> " +
           " <div class='progress'> " +
                "<div class='progress-bar $barstyle ' role='progressbar' aria-valuenow='$number' aria-valuemin='0' aria-valuemax='100' style='width: $percentage'>"+
                    "<span class='sr-only'>$percentage</span>"+
                "</div>"+
            "</div>";
		
		String panelBodyTemplate = " <div class='col-xs-12 col-sm-3' style='float:left; width:25%;'>" +
						" <button type='button' class='btn btn-info' data-toggle='collapse' data-target='#$demo'>$BranchNameReport</button>" +
						" <div id='$demo' class='collapse'>"
						+ "<div class='panel panel-default'>" +
						"<div class='panel-heading'>" +
						"<h4>$BranchNameReport</h4></div>" +
						"<div class='panel-body'>" +
						"$panelbody</div></div></div></div>";
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("template.html");
		byte[] buffer = new byte[1024];
		int count = 0;
		File tfile = new File("template.html");
		FileOutputStream outf = new FileOutputStream(tfile);
		while((count = in.read(buffer)) != -1){
			outf.write(buffer, 0, count);
		}
		outf.close();
		in = ClassLoader.getSystemClassLoader().getResourceAsStream("commit_template.html");
		File ctfile = new File("commit_template.html");
		outf = new FileOutputStream(ctfile);
		count = 0;
		while((count = in.read(buffer)) != -1){
			outf.write(buffer, 0, count);
		}
		outf.close();
		
		String htmlString = new String(Files.readAllBytes(Paths.get("template.html")));
		
		String commitTemplate = new String(Files.readAllBytes(Paths.get("commit_template.html")));
		
		htmlString = htmlString.replace("$numOfFiles", num_of_files.toString());
		htmlString = htmlString.replace("$numOfLines", num_of_lines.toString());
		htmlString = htmlString.replace("$numOfBranches", num_of_branches.toString());
		htmlString = htmlString.replace("$tags", num_of_tags.toString());
		htmlString = htmlString.replace("$numOfCommitters", Integer.toString(committers.size()));
		htmlString = htmlString.replace("$numOfCommits", Integer.toString(CommitCount));
		
		DecimalFormat df = new DecimalFormat("#.##");
		StringBuilder table = new StringBuilder("");

		StringBuilder finalBody = new StringBuilder("");
		StringBuilder doughnut2 = new StringBuilder("");

		String[] barColors = {"progress-bar-success", "progress-bar-info", "progrss-bar-warning", "progress-bar-danger"};
		int colorCounter = 0, i=0, columnCount=0, branchCount = 0;
		for(BranchInfo b: branchInfoArray){
			table.append("<tr><td> <a href='" + b.branchName + ".html'>" + b.branchName + "</a></td>" +
						"<td>" + b.creationDate + "/td" +
						"<td>" + b.lastModified + "</td></tr>");
			
			PrintWriter branchOut = new PrintWriter(s + b.branchName + ".html");
			StringBuilder commits = new StringBuilder("");
			for(CommitInfo c: b.commitList){
				commits.append("<tr><td><p><b>CommitId:</b> " + c.id + "</p><br>" +
						"<p><b>Author:</b> " + c.author + " <b>Date:</b> " + c.date + "</p><br>" +
						"<p>" + "<pre>" + c.message + "</pre></p><br>");
				if(c.tag!=null)
					commits.append("<p><b>Tag:</b> " + c.tag + "</p>");
				commits.append("</td></tr>");
			}
			String out = commitTemplate.replace("$branchName", b.branchName);
			out = out.replace("$tableCommits", commits);
			branchOut.print(out);
			branchOut.close();
			
			if(i!=0)
				doughnut2.append(",");
			doughnut2.append("{label: \"" + b.branchName + "\", value: " + df.format(b.commitPercentage) + "}");
			i++;
			
			StringBuilder buildBody = new StringBuilder("");
			Set<String> keys = b.percentPerCommiter.keySet();
			for(String author: keys){
				
				String panelBodyFilled = panelBody;
				panelBodyFilled = panelBodyFilled.replace("$authorPercent", author + " - " + df.format(b.percentPerCommiter.get(author)) + "%");
				panelBodyFilled = panelBodyFilled.replace("$barstyle", barColors[colorCounter]);
				panelBodyFilled = panelBodyFilled.replace("$number",df.format(b.percentPerCommiter.get(author)));
	//			System.out.println(b.percentPerCommiter.get(author));
				panelBodyFilled = panelBodyFilled.replace("$percentage", df.format(b.percentPerCommiter.get(author)) + "%");
				
				buildBody.append(panelBodyFilled);
				
				if(colorCounter == 3)
					colorCounter = 0;
				else
					colorCounter++;
			}
			String singlePanel = panelBodyTemplate;
			singlePanel = singlePanel.replace("$demo", "branch" + branchCount);
			singlePanel = singlePanel.replace("$BranchNameReport", b.branchName);
			singlePanel = singlePanel.replace("$panelbody", buildBody);
			if(columnCount == 0)
				finalBody.append("<div class='row'>");
				
			finalBody.append(singlePanel);	
			if(columnCount == 3){
				finalBody.append("</div>");
				columnCount = 0;
			}else
				columnCount++;
	
			branchCount++;
		}
		
		htmlString = htmlString.replace("$secondDonut", doughnut2);
		htmlString = htmlString.replace("$branchPanel", finalBody);
		htmlString = htmlString.replace("$tableDates", table);	
		htmlString = htmlString.replace("$insertions", numOfInsertions.toString());
		htmlString = htmlString.replace("$deletes", numOfDeletions.toString());
		
		Set<String> keys = committers.keySet();
		StringBuilder doughnut1 = new StringBuilder("");
		StringBuilder committerTable = new StringBuilder("");
		i=0;
		for(String authorName: keys){
			if(i!=0){
				doughnut1.append(",");
			}
			Committer committer = committers.get(authorName);
			doughnut1.append("{label: \"" + authorName + "\", value: " + df.format(committer.commitPercentage) + "}");
			committerTable.append("<tr><td>" + authorName + "</td>"
					+ "<td>" + df.format(committer.commitsPerDay) + "</td>" +
					"<td>" + df.format(committer.commitsPerMonth) + "</td>" +
					"<td>" + df.format(committer.commitsPerYear) + "</td></tr>" );

			
			i++;
		}
		htmlString = htmlString.replace("$firstDonut", doughnut1);
		htmlString = htmlString.replace("$committerDates", committerTable);
		
		PrintWriter out = new PrintWriter(s + "index.html");
		out.print(htmlString);
		out.close();
		
		Files.delete(tfile.toPath());
		Files.delete(ctfile.toPath());
	}
}
