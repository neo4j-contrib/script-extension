/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.extension.script;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author tbaum
 * @since 15.07.11
 */
public class LogBufferOutputStream extends OutputStream {

    private static final int MAX_LINE_LENGTH = 500;
    private static final int MAX_LINES = 10000;
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS ";

    private final StringBuilder currentLine = new StringBuilder();
    private final LinkedList<LogLine> logLines = new LinkedList<LogLine>();
    private Date currentDate;
    private final String marker;

    public LogBufferOutputStream(String marker) {
        this.marker = marker;
    }

    public SortedSet<LogLine> getLogSince(final Date start) {
        final SortedSet<LogLine> result = new TreeSet<LogLine>();
        for (LogLine logLine : logLines) {
            if (start != null && logLine.date.before(start)) continue;
            result.add(logLine);
        }

        return result;
    }

    @Override public synchronized void write(int i) throws IOException {
        if (currentLine.length() == 0) {
            currentDate = new Date();
        }

        final char c = (char) (i & 0xff);

        currentLine.append(c);

        if (c == '\n' || currentLine.length() > MAX_LINE_LENGTH) {
            addLine(currentDate, currentLine.toString());
            currentLine.setLength(0);
        }
    }

    private void addLine(Date currentDate, String message) {
        final LogLine logLine = new LogLine(marker, currentDate, message);
        System.err.println(logLine.toString());
        logLines.addLast(logLine);
        while (logLines.size() > MAX_LINES) {
            logLines.removeFirst();
        }
    }

    public class LogLine implements Comparable<LogLine> {
        private final String marker;
        private final Date date;
        private final String message;

        private LogLine(String marker, Date date, String message) {
            this.marker = marker;
            this.date = date;
            this.message = message.replaceAll("\n", "");
        }

        public String toString() {
            return new SimpleDateFormat(DATE_FORMAT_PATTERN).format(date) + marker + message;
        }

        @Override public int compareTo(LogLine other) {
            return date.compareTo(other.date);
        }
    }
}
