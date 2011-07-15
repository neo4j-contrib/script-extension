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
