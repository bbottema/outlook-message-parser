[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE) [![Latest Release](https://img.shields.io/maven-central/v/com.auxilii.msgparser/msgparser.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.auxilii.msgparser%22%20AND%20a%3A%22msgparser%22) [![Javadocs](http://www.javadoc.io/badge/com.auxilii.msgparser/msgparser.svg)](http://www.javadoc.io/doc/com.auxilii.msgparser/msgparser) [![Build Status](https://img.shields.io/travis/bbottema/msgparser.svg?style=flat)](https://travis-ci.org/bbottema/msgparser) [![Codacy](https://img.shields.io/codacy/f06332da7f0d4e70a4e53ca6d1df0cc5.svg?style=flat)](https://www.codacy.com/app/b-bottema/msgparser)

# msgparser
*msgparser* is a small open source Java library that parses Outlook .msg files and provides their content using Java objects.

```
<dependency>
  <groupId>com.auxilii.msgparser</groupId>
  <artifactId>msgparser</artifactId>
  <version>1.1.15</version>
</dependency>
```

msgparser uses the [Apache POI - POIFS](http://poi.apache.org/poifs/) library to parse the message files which use the OLE 2 Compound Document format. Thus, it is merely a convenience library that covers the details of the .msg file. The implementation is based on the information provided at [fileformat.info](http://www.fileformat.info/format/outlookmsg/). 

**Help wanted**: To support a wide range of .msg files (e.g., in Chinese or Japanese), we need some example files. Please send us your .msg files that could not be parsed with this library.

 * [Documentation](https://github.com/bbottema/msgparser/wiki)
 * [Project's original Home](http://auxilii.com/msgparser/)
 * Forked code from [SourceForge](https://sourceforge.net/projects/msgparser), so that msgparser can be deployed to Maven Central
