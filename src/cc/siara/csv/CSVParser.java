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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class CSVParser {

   final byte ST_NOT_STARTED = 0;
   final byte ST_DATA_STARTED_WITHOUT_QUOTE = 1;
   final byte ST_DATA_STARTED_WITH_QUOTE = 2;
   final byte ST_QUOTE_WITHIN_QUOTE = 3;
   final byte ST_DATA_ENDED_WITH_QUOTE = 4;
   final byte ST_FIELD_ENDED = 5;

   int line_no = 1;
   int col_no = 1;
   boolean isWithinComment = false;
   boolean isEOL = false;
   boolean isEOS = false;
   byte state = ST_NOT_STARTED;
   ExceptionHandler ex;
   StringBuffer data = new StringBuffer();
   StringBuffer backlog = new StringBuffer();
   String reinsertedToken = null;
   String lastToken = null;
   int reinsertedChar = -1;
   int lastChar = -1;
   int max_value_len = 65535;

   public CSVParser(ExceptionHandler e) {
       ex = e;
       reset();
   }

   private void increment_counters(char c) {
      if (c == '\n') {
         this.line_no++;
         this.col_no = 1;
      } else
      this.col_no++;
   }

   private void decrement_counters(char c) {
      if (c == '\n') {
         this.line_no--;
         this.col_no = 1;
      } else
         this.col_no--;
   }

   private void reset_counters() {
      this.line_no = 1;
      this.col_no = 1;
   }

   private boolean checkEOF(int c, StringBuffer data) {
      if (c == ',') {
         state = ST_FIELD_ENDED;
         isEOL = false;
      } else
      if (c == '\n') {
         state = ST_FIELD_ENDED;
         isEOL = true;
         int lastPos = data.length()-1;
         if (lastPos > -1 && data.charAt(lastPos) == '\r')
            data.setLength(lastPos);
      } else
         return false;
      return true;
   }

   private String windUp(StringBuffer data) {
        isEOS = true;
        isEOL = true;
        state = ST_NOT_STARTED;
        return data.toString();
   }

   public static String encodeToCSVText(String value) {
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

   public void reset() {
       state = ST_NOT_STARTED;
       reset_counters();
   }

   public int readChar(Reader r) throws IOException {
       int i;
       try {
          i = r.read();
       } catch (IOException e) {
          if (ex != null)
             ex.set_err(ExceptionHandler.E_IO, this);
          throw e;
       }
       lastChar = i;
       return i;
   }

    public void processChar(int i) {
        char c = (char) (i & 0xFFFF);
        if (isWithinComment) {
           increment_counters(c);
           return;
        }
        switch (state) {
        case ST_NOT_STARTED:
            if (c == ' ' || c == '\t') {
                backlog.append(c);
            } else if (checkEOF(c, data)) {
            } else if (c == '"') {
                state = ST_DATA_STARTED_WITH_QUOTE;
                backlog.setLength(0);
            } else {
                state = ST_DATA_STARTED_WITHOUT_QUOTE;
                if (backlog.length() > 0) {
                    data.append(backlog);
                    backlog.setLength(0);
                }
                data.append(c);
            }
            break;
        case ST_DATA_STARTED_WITHOUT_QUOTE:
            if (checkEOF(c, data)) {
            } else
                data.append(c);
            break;
        case ST_DATA_STARTED_WITH_QUOTE:
            if (c == '"')
                state = ST_QUOTE_WITHIN_QUOTE;
            else
                data.append(c);
            break;
        case ST_QUOTE_WITHIN_QUOTE:
            if (c == '"') {
                state = ST_DATA_STARTED_WITH_QUOTE;
                data.append(c);
            } else if (checkEOF(c, data)) {
            } else if (c == ' ' || c == '\t' || c == '\r') {
                state = ST_DATA_ENDED_WITH_QUOTE;
            } else {
                state = ST_DATA_STARTED_WITH_QUOTE;
                ex.add_warn(ExceptionHandler.W_CHAR_INVALID, this);
                data.append(c);
            }
            break;
        case ST_DATA_ENDED_WITH_QUOTE:
            if (c == ' ' || c == '\t') {
            } else if (checkEOF(c, data)) {
            } else
                ex.add_warn(ExceptionHandler.W_CHAR_INVALID, this);
            break;
        }
        increment_counters(c);
    }

   public void reinsertLastToken() {
       reinsertedToken = lastToken;
   }

   public void reInsertLastChar() {
       reinsertedChar = lastChar;
       decrement_counters((char)lastChar);
   }

   public String parseNextToken(Reader r) throws IOException {
       if (reinsertedToken != null) {
           String ret = reinsertedToken;
           reinsertedToken = null;
           return ret;
       }
       if (state == ST_FIELD_ENDED) {
           isEOL = false;
           state = ST_NOT_STARTED;
           backlog.setLength(0);
       }
       int c = reinsertedChar;
       if (c == -1)
          c = readChar(r);
       else
          reinsertedChar = -1;
       while (state != ST_FIELD_ENDED) {
           if (c == -1)
              return windUp(data);
           if (c == '/' && !isWithinComment &&
                   state != ST_DATA_STARTED_WITH_QUOTE) {
              int c_next = readChar(r);
              if (c_next == -1) {
                 processChar(c);
                 return windUp(data);
              }
              if (c_next == '*')
                  isWithinComment = true;
              else {
                  processChar(c);
                  processChar(c_next);
              }
           } else
           if (c == '*' && isWithinComment &&
                 state != ST_DATA_STARTED_WITH_QUOTE) {
              int c_next = readChar(r);
              if (c_next == -1) {
                 processChar(c);
                 return windUp(data);
              }
              if (c_next == '/')
                  isWithinComment = false;
              else {
                  processChar(c);
                  processChar(c_next);
              }
           } else
              processChar(c);
          if (state != ST_FIELD_ENDED)
             c = readChar(r);
       }
       String ret = data.toString();
       data.setLength(0);
       lastToken = ret;
       return ret;
   }

    public boolean isEOL() {
        return isEOL;
    }
    
    public boolean isEOS() {
        return isEOS;
    }

    public int getLineNo() {
        return line_no;
    }

    public void setLineNo(int line_no) {
        this.line_no = line_no;
    }

    public int getColNo() {
        return col_no;
    }

    public void setColNo(int col_no) {
        this.col_no = col_no;
    }

}
