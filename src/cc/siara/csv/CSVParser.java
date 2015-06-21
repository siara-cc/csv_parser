package cc.siara.csv;

public class CSVParser {

   int line_no = 1;
   int col_no = 1;
   int save_line_no;
   int save_col_no;

   private static String encodeToCSVText(String value) {
      if (value == null)
         return value;
      if (value.indexOf(',') != -1 || value.indexOf('\n') != -1
           || value.indexOf("/*") != -1) {
         if (value.indexOf('"') != -1)
            value = value.replace("\"", "\"\"");
         value = ("\"" + value + "\"");
      }
      return value;
   }

   private void increment_counters(char c) {
      if (c == '\n') {
         this.line_no++;
         this.col_no = 1;
      } else
      this.col_no++;
   }

   private void save_counters() {
      this.save_line_no = this.line_no;
      this.save_col_no = this.col_no;
   }

   private void restore_counters() {
      this.line_no = this.save_line_no;
      this.col_no = this.save_col_no;
   }

   public int skip_box_comment(String input, int i, int isize, boolean looking_for_quote, boolean to_increment_counters) {
      boolean box_started = false;
      int j;
      for (j=i; j<isize; j++) {
         char c = input.charAt(j);
         if (j < (isize-1)) {
            if (c == '/' && input.charAt(j+1) == '*') {
               box_started = true;
               j++;
               if (to_increment_counters) {
                  this.increment_counters(c);
                  this.increment_counters(c);
               }
               continue;
            }
            if (c == '*' && input.charAt(j+1) == '/') {
               box_started = false;
               j++;
               if (to_increment_counters) {
                  this.increment_counters(c);
                  this.increment_counters(c);
               }
               continue;
            }
         }
         if (!box_started) {
            if (looking_for_quote) {
               if (c != ' ' && c != '\t')
                  break;
               if (to_increment_counters)
                  this.increment_counters(c);
            } else
               break;
         }
      }
      return j;
   }

   public boolean is_eol(String input, int start, int end) {
      int len = end-start-1;
      if (len >= 0 && input.charAt(end-1) == '\n')
         return true;
      if (input.length() == (end-1))
         return true;
      return false;
   }

   public String get_token_value(String input, int start, int end) {
      String ret = "";
      boolean is_quote_enclosed = false;
      int i = start;
      boolean is_quote_end = false;
      for (; i<end; i++) {
         i = this.skip_box_comment(input, i, end, true, false);
         char c = input.charAt(i++);
         if (c == '"')
            is_quote_enclosed = true;
         else
            i = start;
         break;
      }
      for (; i<end; i++) {
         char c = input.charAt(i);
         if (is_quote_end) {
            i = this.skip_box_comment(input, i, end, true, false);
            c = input.charAt(i);
            if (c == ',' || c == '\n')
               break;
            if (c != ',' && c != ' ' && c != '\t')
               is_quote_end = false;
         }
         if (is_quote_enclosed) {
            if (c == '"') {
               if (i == end-1) break;
               char c_next = input.charAt(i+1);
               if (c_next == '"') {
                  ret += c;
                  i++;
                  continue;
               }
               is_quote_end = true;
               continue;
            }
         } else {
            i = this.skip_box_comment(input, i, end, false, false);
            c = input.charAt(i);
            if (c == ',' || c == '\n')
               break;
         }
         ret += c;
       }
       return ret;
   }

   int find_next_token_pos(String input, int isize, int prev_pos, ExceptionHandler ex) {
       int i = prev_pos;
       boolean is_quote_end = false;
       boolean is_quote_started = false;
       if (prev_pos == 0) {
          this.line_no = 1;
          this.col_no = 1;
       }
       this.save_counters();
       for (; i<isize; i++) {
          i = this.skip_box_comment(input, i, isize, true, true);
          char c = input.charAt(i++); this.increment_counters(c);
          if (c == '"')
             is_quote_started = true;
          else {
             i = prev_pos;
             this.restore_counters();
          }
          break;
       }
       for (; i<isize; i++) {
          char c = input.charAt(i);
          if (is_quote_end) {
             i = this.skip_box_comment(input, i, isize, false, true);
             c = input.charAt(i); this.increment_counters(c);
             if (c == ',' || c == '\n') break;
             if (c != ',' && c != ' ' && c != '\t') {
                ex.add_warn(6, this);
                is_quote_end = false;
             }
          }
          if (is_quote_started) {
             if (c == '"') {
                if (i == isize-1) break;
                char c_next = input.charAt(i+1);
                if (c_next == '"') {
                   i++;
                   this.increment_counters(c_next);
                   continue;
                }
                is_quote_end = true;
                continue;
             }
             this.increment_counters(c);
          } else {
             i = this.skip_box_comment(input, i, isize, false, true);
             c = input.charAt(i); this.increment_counters(c);
             if (c == ',' || c == '\n')
                break;
          }
       } 
       return i+1;
   }

}
