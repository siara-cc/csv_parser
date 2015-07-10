# csv_parser
This is an advanced CSV parser that uses RFC4180 as basis.

In addition, it has following advanced features:
* allows comments and empty lines within CSV
* support stream (java.io.Reader/InputStream) for better memory management
* allows pull parsing (returns token by token)
* Restricts token size to pre-defined level to prevent out-of-memory error (buffer overrun)
