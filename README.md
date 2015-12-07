<B> <U> MARCXML Parser </U> </B>

<B> About </B>
<p> MARCXML Parser is based on the DOREMUS model that allows a detailed description of a musical work and the events associated with it (creation, execution, etc.). It takes as input INTERMARC-XML files and generates as output RDF triples. A display in MARC format is an option of our prototype to view an INTERMARC-XML file in MARC format. </p>

<p> The MARCXML format, developed by the Library of Congress in 2001, consists to structure data to MARC in an XML environment. It was founded initially on the MARC21 format, and then it was extended by applying ISO 25577 (MarcXchange), other variants of the MARC (whose INTERMARCXML format used by BNF). </p>

<B> RDF conversion </B>
<p> The figure, below, illustrates an example an INTERMARC-XML file: <p>

 <img src="D:\1.png">
 
<p> Its display in INTERMARC format: <p>

<img src="D:\2.png">
 
<p> And its conversion in RDF triples: <p>
 
 <img src="D:\3.png">

<p> <B> How to run: </B> </p>
<ol>
<li> Load the INTERMARC-XML file in the "Data" folder and name it "XMLFile.xml". </li>
<li> Execute “RDFConversion.java” class in “main” package.</li>
<li> Display of RDF triples :</li>
<ol type=A>
<li> Go to Virtuoso (http: // localhost: 8890)</li>
<li> Click on "Conductor"</li>
<li> Login (“dba”, “dba”)</li>
<li> Click on "Linked Data"</li>
<li> Click on "Graphs"</li>
<li> Click on "DOREMUS"
------> Display of all RDF triples generated from “XMLFile.xml”</li>
</ol>
</ol>

<B> Dependencies: </B>
<ol>
<li> Jena 3.0.0 : http://apache.crihan.fr/.../binaries/apache-jena-3.0.0.zip </li>
<li> Arq 2.8.7 : http://www.java2s.com/Code/Jar/a/Downloadarq287jar.htm </li>
<li> Jena 2.6.0 : http://www.java2s.com/Code/Jar/j/Downloadjena260jar.htm </li>
<li> virt_jena2 et virtjdbc4_1 contenus dans le dossier "lib" de virtuoso </li>
<li> com.ibm.icu_3.4.4.1:http://www.java2s.com/.../Jar/c/Downloadcomibmicu3441jar.htm </li>
<li> iri-0.8 : http://www.java2s.com/Code/Jar/i/Downloadiri08jar.htm </li>
</ol>

<B> RDF Store: </B>
<p> Virtuoso 7.2.1 (http://www.openlinksw.com/) </p>
