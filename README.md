# csv_parser
This is an Advanced Parser for CSV (Comma-separated-value), TSV (Tab-separated-value), TDV (Tab-delimited-value) or even files with custom delimiters such as the Pipe symbol (|).  It supports streams and pull parsing for handling huge data files.  It also supports comments and empty lines within the delimited files for annotation.

This is an advanced CSV parser that uses RFC4180 as basis. In addition, it has following advanced features:
* allows comments and empty lines within CSV.
* support stream (java.io.Reader/InputStream) for better memory management.
* allows pull parsing (returns token by token).
* Restricts token size to pre-defined level to prevent out-of-memory error (buffer overrun).

Complete documentation available at http://java-csv-parser.science

For feedback / clarification, please contact arun@siara.cc.
