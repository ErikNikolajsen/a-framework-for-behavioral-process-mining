package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
	
	private String fileName;
	private FileWriter myWriter;

	public Log(String name) {
		fileName = name;
	}
	
	public void createFile() {
	}
	
	public void writeToFile(String output) {
	    try {
	      myWriter.write(output + "\n");
	    } catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	}
	
	public void openFileWriter() {

		try {

			   File myfile = new File(fileName+".csv");

			   myWriter = new FileWriter(myfile,true);

			  

		} catch (IOException e) {

			   // TODO Auto-generated catch block

			   e.printStackTrace();

		}

 }
	
	public void closeFileWriter() {
		try {
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
