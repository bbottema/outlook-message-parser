[![APACHE v2 License](https://img.shields.io/badge/license-apachev2-blue.svg?style=flat)](LICENSE-2.0.txt) 
[![Latest Release](https://img.shields.io/maven-central/v/org.simplejavamail/outlook-message-parser.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.simplejavamail%22%20AND%20a%3A%22outlook-message-parser%22) 
[![Javadocs](http://www.javadoc.io/badge/org.simplejavamail/outlook-message-parser.svg)](http://www.javadoc.io/doc/org.simplejavamail/outlook-message-parser) 
[![Codacy](https://img.shields.io/codacy/grade/db23d489d8374704a7a7e145f2dc6129?style=flat)](https://www.codacy.com/app/b-bottema/outlook-message-parser)

# Outlook Message Parser
*Outlook Message Parser* is a small open source Java library that parses Outlook .msg files.

```xml
<dependency>
  <groupId>org.simplejavamail</groupId>
  <artifactId>outlook-message-parser</artifactId>
  <version>1.11.1</version>
</dependency>
```

Outlook Message Parser is a continuation (or fork if that project independently continues) of [msgparser](https://github.com/bbottema/msgparser). 

Under the hood it uses the [Apache POI - POIFS](http://poi.apache.org/poifs/) library to parse the message files which use the OLE 2 Compound Document format. Thus, it is merely a convenience library that covers the details of the .msg file. The implementation is based on the information provided at [fileformat.info](http://www.fileformat.info/format/outlookmsg/).

v1.11.0 - [v1.11.1](https://search.maven.org/#artifactdetails%7Corg.simplejavamail%7Coutlook-message-parser%7C1.11.1%7Cjar)

- 1.11.1 (08-December-2023): [#69](https://github.com/bbottema/outlook-message-parser/pull/69): Enhancement: instead of ignoring them completely, only ignore for embedded images
- 1.11.0 (08-December-2023): [#69](https://github.com/bbottema/outlook-message-parser/pull/69): Enhancement: ignore attachment with missing content


v1.10.0 - [v1.10.2](https://search.maven.org/#artifactdetails%7Corg.simplejavamail%7Coutlook-message-parser%7C1.10.2%7Cjar)

- 1.10.2 (03-December-2023): [#68](https://github.com/bbottema/outlook-message-parser/pull/68) Improved heuristics for X500 Names
- 1.10.1 (24-October-2023): [#67](https://github.com/bbottema/outlook-message-parser/pull/67) Fixed "possibility to parse X500 Names"
- 1.10.0 (24-October-2023): [#67](https://github.com/bbottema/outlook-message-parser/pull/67) Adding possibility to parse X500 Names (dont' use this version)


v1.9.0 - [v1.9.6](https://search.maven.org/#artifactdetails%7Corg.simplejavamail%7Coutlook-message-parser%7C1.9.6%7Cjar) 
    
- v1.9.6 (18-July-2022): [#57](https://github.com/bbottema/outlook-message-parser/pull/57) Same, but now with Collection values to support duplicate headers
- v1.9.5 (18-July-2022): [#57](https://github.com/bbottema/outlook-message-parser/pull/57) Headers should be more accessible, rather than just a big string of text
- v1.9.x - a bunch of dependency fixes and tries apparently, my release train was not so smooth here, sorry
- v1.9.0 (13-May-2021): [#55](https://github.com/bbottema/outlook-message-parser/pull/55) CVE issue: Update Apache POI and POI Scratchpad


v1.8.0 - [v1.8.1](https://search.maven.org/#artifactdetails%7Corg.simplejavamail%7Coutlook-message-parser%7C1.8.1%7Cjar)

- v1.8.1 (31-January-2022): [#41](https://github.com/bbottema/outlook-message-parser/pull/41) OutlookMessage.getPropertyValue() should be public
- v1.8.0 (31-January-2022): [#52](https://github.com/bbottema/outlook-message-parser/pull/52) Adjust dependencies and make Java 9+ friendly
- v1.8.0 (31-January-2022): [#45](https://github.com/bbottema/outlook-message-parser/pull/45) Bump commons-io from 2.6 to 2.7


v1.7.10 - v1.7.13 (17-November-2021)

- [#49](https://github.com/bbottema/outlook-message-parser/issues/49) bugfix solved by improved charset handling
- [#46](https://github.com/bbottema/outlook-message-parser/issues/46) bugfix Rare NPE case of producing empty nested outlook attachment when there should be no attachments
- [#43](https://github.com/bbottema/outlook-message-parser/issues/43) bugfix bugfix getFromEmailFromHeaders cannot handle "quoted-name-with@at-sign"
- some minor code improvements


v1.7.9 (10-October-2020)

- [#28](https://github.com/bbottema/outlook-message-parser/issues/28) / [#36](https://github.com/bbottema/outlook-message-parser/issues/36) bugfix NumberFormatException on parsing .msg files


v1.7.8 (4-August-2020)

- [#35](https://github.com/bbottema/outlook-message-parser/issues/35) Clarify permission to publish project using Apache v2 license


v1.7.0 - v1.7.7 (9-January-2020 - 17-July-2020)
 
- v1.7.7 - [#34](https://github.com/bbottema/outlook-message-parser/issues/34) Wrong encoding for bodyHTML
- v1.7.5 - [#31](https://github.com/bbottema/outlook-message-parser/issues/31) Bugfix for attachments with special characters in the name
- v1.7.4 - [#27](https://github.com/bbottema/outlook-message-parser/issues/27) Same as 1.7.3, but now also for chinese senders
- v1.7.3 - [#27](https://github.com/bbottema/outlook-message-parser/issues/27) When from name/address are not available (unsent emails), these fields are filled with binary garbage
- v1.7.2 - [#26](https://github.com/bbottema/outlook-message-parser/issues/26) To email address is not handled properly when name is omitted
- v1.7.1 - [#25](https://github.com/bbottema/outlook-message-parser/issues/25) NPE on ClientSubmitTime when original message has not been sent yet
- v1.7.1 - [#23](https://github.com/bbottema/outlook-message-parser/issues/23) Bug: __nameid_ directory should not be parsed (and causing invalid HTML body)
- v1.7.0 - [#18](https://github.com/bbottema/outlook-message-parser/issues/18) Upgrade Apache POI 3.9 -> 4.x

Note: Apache POI requires minimum Java 8


v1.6.0 (8-January-2020)

- [#21](https://github.com/bbottema/outlook-message-parser/issues/21) Multiple TO recipients are not handles properly


v1.5.0 (18-December-2019)

- [#20](https://github.com/bbottema/outlook-message-parser/issues/20) CC and BCC recipients are not parsed properly
- [#19](https://github.com/bbottema/outlook-message-parser/issues/19) Use real Outlook ContentId Attribute to resolve CID Attachments


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