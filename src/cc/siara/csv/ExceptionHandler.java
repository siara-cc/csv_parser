/*
 * Copyright (C) 2015 arun@siara.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Arundale R.
 *
 */
package cc.siara.csv;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class ExceptionHandler {

    String lang = "en-US";
    public short error_code;
    public int err_line_no;
    public int err_col_no;

    public List<Short> warning_codes;
    public List<Integer> warning_line_nos;
    public List<Integer> warning_col_nos;
    public List<Short> validation_codes;
    public List<Integer> validation_line_nos;
    public List<Integer> validation_col_nos;
    Hashtable<String, String[]> msgs = new Hashtable<String, String[]>();

    public static final short E_IO = 1;
    public static final short W_CHAR_INVALID = 2;
    public static final short E_SCH_START_WITH_SPACE = 3;
    public static final short E_DUPLICATE_NODE = 4;
    public static final short E_DOWN_2_LEVELS = 5;
    public static final short E_TOO_MANY_CHARS = 6;
    public static final short E_NODE_NOT_FOUND = 7;
    public static final short E_ONLY_ONE_ROOT = 8;

    public ExceptionHandler() {
       msgs.put("en-US", new String[] {""
        , "IOException"
        , "Unexpected character"
        , "Schema definition cannot begin with a space"
        , "Duplicate node definition"
        , "Cannot go down two levels"
        , "Too many characters in a column"
        , "Node not found"
        , "There can be only one root node"
        });
       reset_exceptions();
    }

    public void reset_exceptions() {
        error_code = 0;
        err_line_no = 0;
        err_col_no = 0;
        warning_codes = new LinkedList<Short>();
        warning_line_nos = new LinkedList<Integer>();
        warning_col_nos = new LinkedList<Integer>();
        validation_codes = new LinkedList<Short>();
        validation_line_nos = new LinkedList<Integer>();
        validation_col_nos = new LinkedList<Integer>();
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

    public void set_err(short err_code, CSVParser csv_parser) {
        this.error_code = err_code;
        this.err_line_no = csv_parser.line_no;
        this.err_col_no = csv_parser.col_no;
    }

    public void add_warn(short warn_code, CSVParser csv_parser) {
        this.warning_codes.add(warn_code);
        this.warning_line_nos.add(csv_parser.line_no);
        this.warning_col_nos.add(csv_parser.col_no);
    }

    public void add_val_err(short val_code, CSVParser csv_parser) {
        this.validation_codes.add(val_code);
        this.validation_line_nos.add(csv_parser.line_no);
        this.validation_col_nos.add(csv_parser.col_no);
    }

    public String get_all_exceptions() {
       StringBuffer ex_str = new StringBuffer();
       String err_msg = this.get_error_message();
       if (err_msg != "") ex_str.append("Error:\n").append(err_msg);
       String warn_msg = this.get_warn_messages();
       if (warn_msg != "") ex_str.append("Warning(s):\n").append(warn_msg);
       String val_msg = this.get_val_messages();
       if (val_msg != "") ex_str.append("Validation Error(s):\n").append(val_msg);
       return ex_str.toString();
    }

}
