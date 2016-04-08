package ppMarcXML;

public class Subfield  {

    private char code;
    private String data;
    

    public Subfield(char code) {
    	this.code = code;
    }

    public Subfield(char code, String data) {
    	this.code = code;
    	this.data = data;
    }

    public char getCode() {
        return code;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public String toString() {
    	if (getCode()==' '){return "";}
    	else return "$" + getCode() + getData();
    }
    
}