package org.doremus.marc2rdf.bnfparser;

import java.util.Scanner;

public class ControlField extends Etiq {
    
    private String data;
    
    public ControlField(String tag) {
        super(tag);
    }

    public ControlField(String tag, String data) {
        super(tag);
        this.setData(data);
    }

    public void setData(String data) {
    	Scanner scanner = new Scanner(data);
    	this.data = scanner.nextLine();
    	scanner.close();
    }

    public String getData() {
        return data;
    }

    public String toString() {
        return super.toString() + " " + getData();
    }

}