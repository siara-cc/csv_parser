/*
 * Copyright (C) 2015 Siara Logics (cc)
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

/**
 * Maintains line number and column number during parsing
 * 
 * @author Arundale R.
 */
public class Counter {

    // members
    protected int line_no = 1;
    protected int col_no = 1;

    /**
     * Increments line number and column number based on current character.
     * 
     * @param c Current character
     */
    public void increment_counters(char c) {
        if (c == '\n') {
            line_no++;
            col_no = 1;
        } else
            col_no++;
    }

    /**
     * Decrements line number and column number based on current character.
     * 
     * @param c Current character
     */
    public void decrement_counters(char c) {
        if (c == '\n') {
            line_no--;
            col_no = 1;
        } else
            col_no--;
    }

    /**
     * Resets counters
     */
    public void reset_counters() {
        line_no = 1;
        col_no = 1;
    }

    /**
     * Getter for Line number
     * @return Current Line number
     */
    public int getLineNo() {
        return line_no;
    }

    /**
     * Setter for Current line number
     * 
     * @param line_no
     */
    public void setLineNo(int line_no) {
        this.line_no = line_no;
    }

    /**
     * Getter for current column number
     * 
     * @return Current column number
     */
    public int getColNo() {
        return col_no;
    }

    /**
     * Setter for Current column number
     * 
     * @param col_no
     */
    public void setColNo(int col_no) {
        this.col_no = col_no;
    }

}
