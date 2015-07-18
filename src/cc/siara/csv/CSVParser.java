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

import java.io.IOException;
import java.io.Reader;

/**
 * CSV Token Parser
 * 
 * @author Arundale R.
 */
public class CSVParser {

    // Parsing states
    final byte ST_NOT_STARTED = 0;
    final byte ST_DATA_STARTED_WITHOUT_QUOTE = 1;
    final byte ST_DATA_STARTED_WITH_QUOTE = 2;
    final byte ST_QUOTE_WITHIN_QUOTE = 3;
    final byte ST_DATA_ENDED_WITH_QUOTE = 4;
    final byte ST_FIELD_ENDED = 5;

    // Members and transients
    boolean isWithinComment = false;
    boolean isEOL = false;
    boolean isEOS = false;
    byte state = ST_NOT_STARTED;
    Counter counter = null;
    ExceptionHandler ex = null;
    StringBuffer data = new StringBuffer();
    StringBuffer backlog = new StringBuffer();
    String reinsertedToken = null;
    String lastToken = null;
    int reinsertedChar = -1;
    int lastChar = -1;
    int max_value_len = 65535;
    private char delimiter = ',';
    private char alt_whitespace = '\t';

    /**
     * Initializes a CSV Parser with internal counter and ExceptionHandler
     */
    public CSVParser() {
        counter = new Counter();
        ex = new ExceptionHandler(counter);
        reset();
    }

    /**
     * Initializes a CSV Parser with internal counter and ExceptionHandler
     * 
     * @param max
     *            Maximum allowable characters in a column
     */
    public CSVParser(int max) {
        this();
        max_value_len = max;
    }

    /**
     * Initializes a CSV Parser with given Counter and ExceptionHandler
     * 
     * @param c
     *            Given Counter object
     * @param e
     *            Given Exception Handler object
     * @param max
     *            Maximum allowable characters in a column
     */
    public CSVParser(Counter c, ExceptionHandler e, int max) {
        ex = e;
        counter = c;
        max_value_len = max;
        reset();
    }

    /**
     * Initializes a CSV Parser with given Counter and ExceptionHandler
     * 
     * @param c
     *            Given Counter object
     * @param e
     *            Given Exception Handler object
     */
    public CSVParser(Counter c, ExceptionHandler e) {
        ex = e;
        counter = c;
        reset();
    }

    /**
     * Sets delimiter character used for parsing
     * 
     * @param d Delimiter
     */
    public void setDelimiter(char d) {
        this.delimiter  = d;
        this.alt_whitespace = '\t';
        if (d == '\t')
            this.alt_whitespace = ' ';
    }

    /**
     * Checks whether End of Field reached based on current character
     * 
     * @param c
     *            Current character
     * @param data
     *            Current field value for removing dummy \r (Carriage Return),
     *            if any
     * @return
     */
    private boolean checkEOF(int c, StringBuffer data) {
        if (c == this.delimiter) {
            state = ST_FIELD_ENDED;
            isEOL = false;
        } else if (c == '\n') {
            state = ST_FIELD_ENDED;
            isEOL = true;
            int lastPos = data.length() - 1;
            if (lastPos > -1 && data.charAt(lastPos) == '\r')
                data.setLength(lastPos);
        } else
            return false;
        return true;
    }

    /**
     * Sets states End of Line and End of Stream to indicate end of parsing
     * 
     * @param data
     *            Return value
     * @return Last column data
     */
    private String windUp(StringBuffer data) {
        isEOS = true;
        isEOL = true;
        state = ST_NOT_STARTED;
        return data.toString();
    }

    /**
     * Resets the instance so that it can be used and re-used.
     */
    public void reset() {
        state = ST_NOT_STARTED;
        isEOS = false;
        isEOL = false;
        counter.reset_counters();
        data = new StringBuffer();
        backlog = new StringBuffer();
        reinsertedToken = null;
        lastToken = null;
        reinsertedChar = -1;
        lastChar = -1;
    }

    /**
     * Encapsulates reading a character from the stream.
     * 
     * @param r
     *            Stream being read
     * @return Character read from stream
     * @throws IOException
     */
    public int readChar(Reader r) throws IOException {
        int i;
        try {
            i = r.read();
        } catch (IOException e) {
            if (ex != null)
                ex.set_err(ExceptionHandler.E_IO);
            throw e;
        }
        lastChar = i;
        return i;
    }

    /**
     * Processes given character i according to current state. For each state,
     * the if statements decide whether to stay in the current state or jump to
     * another. Each state has its own set of decision tree.
     * 
     * The backlog variable keeps characters when a series of spaces and tabs
     * are found at the beginning of a field. If a quote is found consequently,
     * backlog is ignored, otherwise it is appended to data.
     * 
     * @param i
     */
    public void processChar(int i) {
        char c = (char) (i & 0xFFFF);
        if (isWithinComment) {
            counter.increment_counters(c);
            return;
        }
        switch (state) {
        case ST_NOT_STARTED:
            if (c == ' ' || c == alt_whitespace) {
                backlog.append(c);
            } else if (checkEOF(c, data)) {
            } else if (c == '"') {
                state = ST_DATA_STARTED_WITH_QUOTE;
                backlog.setLength(0);
            } else {
                state = ST_DATA_STARTED_WITHOUT_QUOTE;
                if (backlog.length() > 0) {
                    if (data.length() < max_value_len)
                        data.append(backlog);
                    backlog.setLength(0);
                }
                if (data.length() < max_value_len)
                    data.append(c);
            }
            break;
        case ST_DATA_STARTED_WITHOUT_QUOTE:
            if (checkEOF(c, data)) {
            } else {
                if (data.length() < max_value_len)
                    data.append(c);
            }
            break;
        case ST_DATA_STARTED_WITH_QUOTE:
            if (c == '"')
                state = ST_QUOTE_WITHIN_QUOTE;
            else {
                if (data.length() < max_value_len)
                    data.append(c);
            }
            break;
        case ST_QUOTE_WITHIN_QUOTE:
            if (c == '"') {
                state = ST_DATA_STARTED_WITH_QUOTE;
                if (data.length() < max_value_len)
                    data.append(c);
            } else if (checkEOF(c, data)) {
            } else if (c == ' ' || c == alt_whitespace || c == '\r') {
                state = ST_DATA_ENDED_WITH_QUOTE;
            } else {
                state = ST_DATA_STARTED_WITH_QUOTE;
                ex.add_warn(ExceptionHandler.W_CHAR_INVALID);
                if (data.length() < max_value_len)
                    data.append(c);
            }
            break;
        case ST_DATA_ENDED_WITH_QUOTE:
            if (c == ' ' || c == alt_whitespace) {
            } else if (checkEOF(c, data)) {
            } else
                ex.add_warn(ExceptionHandler.W_CHAR_INVALID);
            break;
        }
        counter.increment_counters(c);
    }

    /**
     * Puts back one field parsed from stream.
     */
    public void reinsertLastToken() {
        reinsertedToken = lastToken;
    }

    /**
     * Puts back one character read from stream.
     */
    public void reInsertLastChar() {
        reinsertedChar = lastChar;
        counter.decrement_counters((char) lastChar);
    }

    /**
     * Gets next token by parsing characters from the stream.
     * 
     * A comment is processed separately from the main state machine. Whenever
     * not within a comment, this method calls processChar to process the
     * character using the state machine.
     * 
     * @param r
     * @return
     * @throws IOException
     */
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
            if (c == '/' && !isWithinComment
                    && state != ST_DATA_STARTED_WITH_QUOTE) {
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
            } else if (c == '*' && isWithinComment
                    && state != ST_DATA_STARTED_WITH_QUOTE) {
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

    /**
     * Reports end of line.
     * 
     * @return true if End of line reached, false otherwise.
     */
    public boolean isEOL() {
        return isEOL;
    }

    /**
     * Reports end of stream.
     * 
     * @return true if End of stream reached, false otherwise.
     */
    public boolean isEOS() {
        return isEOS;
    }

    /**
     * Getter for the Counter object
     * 
     * @return Counter object
     */
    public Counter getCounter() {
        return counter;
    }

}
