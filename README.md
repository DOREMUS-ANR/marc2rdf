<B> <U> MARC2RDF </U> </B>

<B> About </B>
<p> MARC2RDF is based on the DOREMUS model <sup>[1]</sup> that allows a detailed description of a musical work and the events associated with it (creation, execution, etc.). It takes as input INTERMARC-XML and UNIMARC-XML files and generates as output RDF triples. A display in MARC (MAchine Readable Cataloging) <sup>[2]</sup> format is an option of our prototype to view an INTERMARC-XML or an UNIMARC-XML file in MARC format. </p>

<p> The MARCXML format <sup>[3]</sup>, developed by the Library of Congress in 2001, consists to structure data to MARC in an XML environment. It was founded initially on the MARC21 <sup>[4]</sup> format, and then it was extended by applying ISO 25577 (MarcXchange), other variants of the MARC (whose INTERMARCXML format used by BNF). </p>

<B> RDF conversion </B>
<p> The figure, below, illustrates an example an INTERMARC-XML file: <p>

 <img src="https://github.com/DOREMUS-ANR/marc2rdf/blob/master/img/1.png">
 
<p> Its display in INTERMARC format: <p>

<img src="https://github.com/DOREMUS-ANR/marc2rdf/blob/master/img/2.png">
 
<p> And its conversion in RDF triples: <p>
 
 <img src="https://github.com/DOREMUS-ANR/marc2rdf/blob/master/img/3.png">

<p> <B> MARC2RDF description: </B> </p>

<p> This tool consists of 4 components:
<ol>
<li> BNFConverter: converts INTERMARC-XML records from BNF (Bibliothèque Nationale de France) in RDF triples. </li>
<li> BNFParser: a parser of INTERMARC-XML records from BNF and displays them in MARC format. </li>
<li> PPConverter: converts INTERMARC-XML and UNIMARC-XML records from Philharmonie de Paris in RDF triples. </li>
<li> PPParser:  a parser of INTERMARC-XML and UNIMARC-XML records from Philharmonie de Paris and displayes them in MARC format. </li>
</ol>
The class "Converter" is the "main" class to start converting MARCXML records in RDF triples.
The class "MainParser" is the "main" class to launch the parser and to display the MARCXML records in MARC format.
</p>

<p> <B> How to run: </B> </p>
<ol>
<li> Edit the path about records to be converted in “Converter.java” class in “main” package. </li>
<li> Execute “Converter.java” class in “main” package.</li>
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
<br>
<br>
<p> <a name="">[1]</a>: https://drive.google.com/file/d/0B_nxZpGQv9GKZmpKRGl2dmRENGc/view </p>
<p> <a name="">[2]</a>: https://fr.wikipedia.org/wiki/Machine-Readable_Cataloging / </p>
<p> <a name="">[3]</a>: https://www.loc.gov/standards/marcxml/ </p>
<p> <a name="">[4]</a>:https://www.loc.gov/marc/ </p>
