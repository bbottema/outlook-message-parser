[![APACHE v2 License](https://img.shields.io/badge/license-apachev2-blue.svg?style=flat)](LICENSE-2.0.txt) 
[![Latest Release](https://img.shields.io/maven-central/v/org.simplejavamail/outlook-message-parser.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.simplejavamail%22%20AND%20a%3A%22outlook-message-parser%22) 
[![Javadocs](http://www.javadoc.io/badge/org.simplejavamail/outlook-message-parser.svg)](http://www.javadoc.io/doc/org.simplejavamail/outlook-message-parser) 
[![Codacy](https://img.shields.io/codacy/grade/db23d489d8374704a7a7e145f2dc6129?style=flat)](https://www.codacy.com/app/b-bottema/outlook-message-parser)

# Outlook Message Parser
*Outlook Message Parser* is a small open source Java library that parses Outlook .msg files.

```
<dependency>
  <groupId>org.simplejavamail</groupId>
  <artifactId>outlook-message-parser</artifactId>
  <version>1.4.1</version>
</dependency>
```

Outlook Message Parser is a continuation (or fork if that project independently continues) of [msgparser](https://github.com/bbottema/msgparser). 

Under the hood it uses the [Apache POI - POIFS](http://poi.apache.org/poifs/) library to parse the message files which use the OLE 2 Compound Document format. Thus, it is merely a convenience library that covers the details of the .msg file. The implementation is based on the information provided at [fileformat.info](http://www.fileformat.info/format/outlookmsg/).


v1.4.1 (22-October-2019)

- [#17](https://github.com/bbottema/outlook-message-parser/issues/17) Fixed encoding error for UTF-8's Windows legacy name (cp)65001


v1.4.0 (13-October-2019)

- [#9](https://github.com/bbottema/outlook-message-parser/issues/9) Replaced the RFC to HTML converter with a brand new RFC-compliant convert! (thanks to @fadeyev!)


v1.3.0 (4-October-2019)

- [#14](https://github.com/bbottema/outlook-message-parser/issues/14) Dependency problem with Java9+, missing Jakarta Activation Framework
- [#13](https://github.com/bbottema/outlook-message-parser/issues/13) HTML start tags with extra space not handled correctly
- [#11](https://github.com/bbottema/outlook-message-parser/issues/11) SimpleRTF2HTMLConverter inserts too many <br/> tags
- [#10](https://github.com/bbottema/outlook-message-parser/issues/10) Embedded images with DOS-like names are classified as attachments
- [#9](https://github.com/bbottema/outlook-message-parser/issues/9) SimpleRTF2HTMLConverter removes some valid tags during conversion


v1.2.1 (12-May-2019)

- Ignore non S/MIME related content types when extracting S/MIME metadata
- Added toString and equals methods to the S/MIME data classes


v1.1.21 (4-May-2019)

- Upgraded mediatype recognition based on file extension for incomplete attachments
- Added / improved support for public S/MIME meta data 


v1.1.20 (14-April-2019)

- [#7](https://github.com/bbottema/outlook-message-parser/issues/7) Fix missing S/MIME header details that are needed to determine the type of S/MIME application


v1.1.19 (10-April-2019)

- Log rtf compression error, but otherwise ignore it and keep going and extract what we can.


v1.1.18 (5-April-2019)

- [#6](https://github.com/bbottema/outlook-message-parser/issues/6) Missing mimeTag for attachments should be guessed based on file extension


v1.1.17 (19-August-2018)

- [#3](https://github.com/bbottema/simple-java-mail/issues/3) implemented robust support for character sets / code pages in RTF to HTML 
conversion (fixes chinese support #3)
- fixed bug where too much text was cleaned up as part of superfluous RTF cleanup step when converting to HTML
- Performance boost in the RTF -> HTML converter


v1.1.16 (~28-Februari-2017)

- First Maven deployment, continuing version number from 1.1.15 of msgparser (https://github.com/bbottema/msgparser)


v1.16
 - Added support for replyTo name and address
 - cleaned up code (1st wave)