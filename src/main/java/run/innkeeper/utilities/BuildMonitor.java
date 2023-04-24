package run.innkeeper.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildMonitor {

    public static class BuildLogParts {
        public String[] lines;
        public String hash;
        public String branch;
        public String author;
        public String date;
        public BuildLogParts(String[] lines, String hash, String branch, String author, String date) {
            this.lines = lines;
            this.hash = hash;
            this.branch = branch;
            this.author = author;
            this.date = date;
        }
        public String[] getLines() {
            return lines;
        }
        public String getHash() {
            return hash;
        }
        public String getBranch() {
            return branch;
        }
        public String getAuthor() {
            return author;
        }
        public String getDate() {
            return date;
        }
    }

    public static String grabPart(String log, String key){
        final String regex = key+":([a-zA-Z0-9\\/ .\\-]+)";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(log);

        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    public static BuildLogParts get(String log){
        return new BuildLogParts(
            log.split("\n"),
            grabPart(log, "hash"),
            grabPart(log, "branch"),
            grabPart(log, "author"),
            grabPart(log, "date")
        );
    }
}
