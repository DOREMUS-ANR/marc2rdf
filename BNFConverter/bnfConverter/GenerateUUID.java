package bnfConverter;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

public class GenerateUUID {

	public String get () {
		
			   /*****************************************************************/
		 UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        	   /*****************************************************************/
		 return uid.randomUUID().toString();
        	   
	}
}
