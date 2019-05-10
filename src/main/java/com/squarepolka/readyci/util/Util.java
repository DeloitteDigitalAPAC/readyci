package com.squarepolka.readyci.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class Util {

    public static String getMappedValueAtPath(Map map, String path) {
        String[] pathKeys = path.split("\\.");
        Object currentObject = map;
        for (String key : pathKeys) {

            // Traverse the map type objects
            if (null != currentObject && currentObject instanceof Map) {
                currentObject = ((Map) currentObject).get(key);
            } else if (null != currentObject && currentObject instanceof List) {
                List listItems = (List) currentObject;
                currentObject = parseListObject(listItems, key);
            }

        }

        // If we've landed on a String, return it
        if (null != currentObject && currentObject instanceof String) {
            return (String) currentObject;
        }

        // If all else, return an empty string
        return "";
    }



    public static Collection<File> findAllByExtension(File dir, String extension) {
        Set<File> fileTree = new HashSet<File>();
        if (dir == null || dir.listFiles() == null) {
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile() && getFileExtension(entry).equals(extension)) fileTree.add(entry);
            else fileTree.addAll(findAllByExtension(entry, extension));
        }
        return fileTree;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }


    public static Object parseListObject(List listItems, String key) {
        // Find a map in this list which contains the key we're looking for
        for (Object listObject : listItems) {
            if (listObject instanceof Map) {
                Map map = (Map) listObject;
                if (map.containsKey(key)) {
                    return map.get(key);
                }
            }
        }
        return null;
    }

    public static boolean valueExists(String string) {
        return string.length() > 0;
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader processStreamReader = new InputStreamReader(inputStream);
        BufferedReader processOutputStream = new BufferedReader(processStreamReader);
        String processOutputLine;
        while (processOutputStream.ready() && (processOutputLine = processOutputStream.readLine()) != null) {
            sb.append(processOutputLine + "\n");
        }
        return sb.toString();
    }

    /**
     * Skip half of the available data in an input stream.
     * This method is useful when a full input stream might block a process,
     * while keeping some data might be desired.
     * <p>
     * Unfortunately, an IOException is sometimes thrown when using the processInputStream.skip(long n) method.
     * We're using processInputStream.read() instead.
     * We might be suffering from this: https://bugs.java.com/view_bug.do?bug_id=6222822
     *
     * @param inputStream
     */
    public void skipHalfOfStream(InputStream inputStream) throws IOException {
        int availableBytes = inputStream.available();
        long bytesToSkip = availableBytes / 2;
        for (int i = 0; i < bytesToSkip; i++) {
            inputStream.read();
        }
    }

    /**
     * Convert a string array to a flat string.
     * @param stringArray
     * @return a flat string.
     */
    public static String arrayToString(List<String> stringArray) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = stringArray.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            stringBuilder.append(string);
            if (iterator.hasNext()) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

}
