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

public class Counter {

    protected int line_no = 1;
    protected int col_no = 1;

    public void increment_counters(char c) {
        if (c == '\n') {
           line_no++;
           col_no = 1;
        } else
        col_no++;
     }

     public void decrement_counters(char c) {
        if (c == '\n') {
           line_no--;
           col_no = 1;
        } else
           col_no--;
     }

     public void reset_counters() {
        line_no = 1;
        col_no = 1;
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
