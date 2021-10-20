package texteditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileUtilities {
	public StringBuilder readFile(String filename)
	{
			// read the file and save the text in stringbuilder
		File file = new File(filename);
		StringBuilder fileContent = new StringBuilder();
		try
		{
			Scanner input = new Scanner(file);
			while(input.hasNextLine())
			{
				fileContent.append(input.nextLine() + "\n");
			}
			input.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
		}
		return fileContent;
	}
	
	public int countWords(StringBuilder fileContent)
	{
		return fileContent.toString().split("[\\n\\s\\,\\.\\;\\:\\(\\)]+").length;
	}
	
	public int[] searchAll(StringBuilder fileContent, String searchString)
	{
			// will return the indexes of searched strings
		int countWords = 0; // count the words before each substring
		int numSearchString = 0;
		int[] searchIndex = null;
		String s = fileContent.toString(); 
		StringBuilder indexList = new StringBuilder(); // record the index of each searched string
		while (s.contains(searchString))
		{
			numSearchString++;
			indexList.append(Integer.toString(s.indexOf(searchString)+countWords)+',');
			countWords += s.substring(0, s.indexOf(searchString)+searchString.length()).length();
			s = s.substring(s.indexOf(searchString)+searchString.length());
		}
		if (numSearchString > 0)
		{
			searchIndex = new int[numSearchString];
			for (int i=0; i<numSearchString; i++)
			{
				searchIndex[i] = Integer.valueOf(indexList.toString().split(",")[i]);
			}
		}
		return searchIndex;
	}
	
	public int replace(StringBuilder fileContent, String oldString, String newString)
	{
		return 0;
	}
}
