# MARC2RDF

### About
MARC2RDF is based on the [DOREMUS model][1] that allows a detailed description of a musical work and the events associated with it (creation, execution, etc.). It takes as input INTERMARC-XML and UNIMARC-XML files and generates as output RDF triples. A display in [MARC (MAchine Readable Cataloging)][2] format is an option of our prototype to view an INTERMARC-XML or an UNIMARC-XML file in MARC format.

The [MARCXML][3] format, developed by the Library of Congress in 2001, consists to structure data to MARC in an XML environment. It was founded initially on the [MARC21][4] format, and then it was extended by applying ISO 25577 (MarcXchange), other variants of the MARC (whose INTERMARCXML format used by BNF).

### RDF conversion
The figure, below, illustrates an example an INTERMARC-XML file:

![INTERMARC-XML](https://github.com/DOREMUS-ANR/marc2rdf/blob/master/img/1.png)
 
Its display in INTERMARC format:

![INTERMARC format](https://github.com/DOREMUS-ANR/marc2rdf/blob/master/img/2.png)
 
And its conversion in RDF triples:
 
![RDF triples](https://github.com/DOREMUS-ANR/marc2rdf/blob/master/img/3.png)

### MARC2RDF description:

This tool consists of 4 components:

1. BNFConverter: converts INTERMARC-XML records from BNF (Bibliothèque Nationale de France) in RDF triples.
2. BNFParser: a parser of INTERMARC-XML records from BNF and displays them in MARC format.
3. PPConverter: converts INTERMARC-XML and UNIMARC-XML records from Philharmonie de Paris in RDF triples.
4. PPParser:  a parser of INTERMARC-XML and UNIMARC-XML records from Philharmonie de Paris and displayes them in MARC format.

The class "Converter" is the "main" class to start converting MARCXML records in RDF triples.
The class "MainParser" is the "main" class to launch the parser and to display the MARCXML records in MARC format.


### How to run:

1. Execute “Converter.java” class in “main” package.
2. Choose the directory that contains the records to be converted.
3. Display of RDF triples :
  1. Go to Virtuoso (http: // localhost: 8890)
  2. Click on "Conductor"
  3. Login (“dba”, “dba”)
  4. Click on "Linked Data"
  5. Click on "Graphs"
  6. Click on "DOREMUS" ------> Display of all RDF triples generated from “XMLFile.xml”


### Dependencies:
1. Jena 3.0.0 : http://apache.crihan.fr/.../binaries/apache-jena-3.0.0.zip
2. Arq 2.8.7 : http://www.java2s.com/Code/Jar/a/Downloadarq287jar.htm
3. Jena 2.6.0 : http://www.java2s.com/Code/Jar/j/Downloadjena260jar.htm
4. virt_jena2 et virtjdbc4_1 contenus dans le dossier "lib" de virtuoso
5. com.ibm.icu_3.4.4.1:http://www.java2s.com/.../Jar/c/Downloadcomibmicu3441jar.htm
6. iri-0.8 : http://www.java2s.com/Code/Jar/i/Downloadiri08jar.htm

### RDF Store: 
Virtuoso 7.2.1 (http://www.openlinksw.com)

[1]: https://drive.google.com/file/d/0B_nxZpGQv9GKZmpKRGl2dmRENGc/view
[2]: https://fr.wikipedia.org/wiki/Machine-Readable_Cataloging
[3]: https://www.loc.gov/standards/marcxml
[4]: https://www.loc.gov/marc
