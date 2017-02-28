# msgparser
*msgparser* is a small open source Java library that parses Outlook .msg files and provides their content using Java objects.

msgparser uses the [Apache POI - POIFS](http://poi.apache.org/poifs/) library to parse the message files which use the OLE 2 Compound Document format. Thus, it is merely a convenience library that covers the details of the .msg file. The implementation is based on the information provided at [fileformat.info](http://www.fileformat.info/format/outlookmsg/). 

**Help wanted**: To support a wide range of .msg files (e.g., in Chinese or Japanese), we need some example files. Please send us your .msg files that could not be parsed with this library.
