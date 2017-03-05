[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE) [![Latest Release](https://img.shields.io/maven-central/v/com.auxilii.msgparser/msgparser.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.auxilii.msgparser%22%20AND%20a%3A%22msgparser%22) [![Javadocs](http://www.javadoc.io/badge/com.auxilii.msgparser/msgparser.svg)](http://www.javadoc.io/doc/com.auxilii.msgparser/msgparser) [![Build Status](https://img.shields.io/travis/bbottema/msgparser.svg?style=flat)](https://travis-ci.org/bbottema/msgparser) [![Codacy](https://img.shields.io/codacy/f06332da7f0d4e70a4e53ca6d1df0cc5.svg?style=flat)](https://www.codacy.com/app/b-bottema/msgparser)

# Outlook Message Parser
*Outlook Message Parser* is a small open source Java library that parses Outlook .msg files.

```
<dependency>
  <groupId>org.simplejavamail</groupId>
  <artifactId>outlook-message-parser</artifactId>
  <version>1.1.16</version>
</dependency>
```

Outlook Message Parser is a continuation (or fork if that project independently continues) of [msgparser](https://github.com/bbottema/msgparser). 

Under the hood it uses the [Apache POI - POIFS](http://poi.apache.org/poifs/) library to parse the message files which use the OLE 2 Compound Document format. Thus, it is merely a convenience library that covers the details of the .msg file. The implementation is based on the information provided at [fileformat.info](http://www.fileformat.info/format/outlookmsg/). 

 * [Documentation](https://github.com/bbottema/outlook-message-parser/wiki)
 * Forked code from [bbottema/msgparser](https://github.com/bbottema/msgparser)
