package cc.siara.csv;

import java.util.*;

public class ExceptionHandler {

    String lang = "en-US";
    int error_code = 0;
    int err_line_no = 0;
    int err_col_no = 0;

    List<Integer> warning_codes = new LinkedList<Integer>();
    List<Integer> warning_line_nos = new LinkedList<Integer>();
    List<Integer> warning_col_nos = new LinkedList<Integer>();
    List<Integer> validation_codes = new LinkedList<Integer>();
    List<Integer> validation_line_nos = new LinkedList<Integer>();
    List<Integer> validation_col_nos = new LinkedList<Integer>();
    Hashtable<String, String[]> msgs = new Hashtable<String, String[]>();

    ExceptionHandler() {
       msgs.put("en-US", new String[] {""
        , "Schema definition cannot begin with a space"
        , "Duplicate node definition"
        , "Cannot go down two levels"
        , "Too many characters in a column"
        , "Node not found"
        , "Improperly closed quote"
        , "There can be only one root node"
        });
    }

    public String get_error_message() {
        if (this.error_code == 0)
            return "";
        return "Line:" + this.err_line_no + ", Col:" + this.err_col_no + ": "
                + msgs.get(lang)[error_code];
    }

	public String get_warn_messages() {
		if (warning_codes.size() == 0)
			return "";
		StringBuffer warn_msgs = new StringBuffer();
		for (int i = 0; i < this.warning_codes.size(); i++) {
			if (i > 0)
				warn_msgs.append("\r\n");
			warn_msgs.append("Line:").append(this.warning_line_nos.get(i))
					.append(", Col:").append(this.warning_col_nos.get(i))
					.append(": ")
					.append(msgs.get(lang)[this.warning_codes.get(i)]);
		}
		return warn_msgs.toString();
	}

	public String get_val_messages() {
		if (this.validation_codes.size() == 0)
			return "";
		StringBuffer val_msgs = new StringBuffer();
		for (int i = 0; i < this.validation_codes.size(); i++) {
			if (i > 0)
				val_msgs.append("\r\n");
			val_msgs.append("Line:").append(this.validation_line_nos.get(i))
					.append(", Col:").append(this.validation_col_nos.get(i))
					.append(": ")
					.append(msgs.get(lang)[this.validation_codes.get(i)]);
		}
		return val_msgs.toString();
	}

	public void add_err(int err_code, CSVParser csv_parser) {
		this.error_code = err_code;
		this.err_line_no = csv_parser.line_no;
		this.err_col_no = csv_parser.col_no;
	}

	public void add_warn(int warn_code, CSVParser csv_parser) {
		this.warning_codes.add(warn_code);
		this.warning_line_nos.add(csv_parser.line_no);
		this.warning_col_nos.add(csv_parser.col_no);
	}

	public void add_val_err(int val_code, CSVParser csv_parser) {
		this.validation_codes.add(val_code);
		this.validation_line_nos.add(csv_parser.line_no);
		this.validation_col_nos.add(csv_parser.col_no);
	}
	/*    public String display_exceptions = function() {
   var err_msg = this.get_error_message();
   if (err_msg != "") alert("Error:\n"+err_msg);
   var warn_msg = this.get_warn_messages();
   if (warn_msg != "") alert("Warning(s):\n"+warn_msg);
   var val_msg = this.get_val_messages();
   if (val_msg != "") alert("Validation Error(s):\n"+val_msg);
   if (err_msg != "") return true;
   return false;
}*/

}
