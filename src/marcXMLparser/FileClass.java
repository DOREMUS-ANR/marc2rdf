package marcXMLparser;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class FileClass {

	public static void main(String[] args) throws IOException {
		    final Collection<File> all = new ArrayList<File>();
		    findFilesRecursively(new File("C:\\Users\\Manel\\Music\\files\\"), all, ".xml");
		    for (File file : all) {
		    	System.out.println(file.getName());
		    }
	}
	/*************************************************************************************************/
	private static void findFilesRecursively(File file, Collection<File> all, final String extension) {
	    final File[] children = file.listFiles(new FileFilter() {
	    	public boolean accept(File f) {
	                return f.getName().endsWith(extension) ;
	            }}
	    );
	    if (children != null) {
	        for (File child : children) {
	            all.add(child);
	            findFilesRecursively(child, all, extension);
	        }
	    }
	}
	/*************************************************************************************************/
}
