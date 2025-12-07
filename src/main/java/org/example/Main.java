package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    private static int rows;
    private static int cols;
    private static char[][] splitterSearch;

    public static void main(String[] args) {
        BufferedReader reader;
        char splitter = '^';
        char start = 'S';
        try {
            reader = new BufferedReader(new FileReader("src/main/resources/TachyonManifolds.txt"));
            String line = reader.readLine();
            int rowCount = 0;
            int colCount = 0;

            // First pass: count rows and columns
            while (line != null) {
                rowCount++;
                colCount = line.length(); // Assuming all rows have the same length
                line = reader.readLine();
            }

            // Re-initialize BufferedReader and the splitterSearch array
            reader.close();

            reader = new BufferedReader(new FileReader("src/main/resources/TachyonManifolds.txt"));
            splitterSearch = new char[rowCount][colCount];

            int row = 0;
            // Second pass: populate the splitterSearch array
            while ((line = reader.readLine()) != null) {
                splitterSearch[row] = line.toCharArray();
                row++;
            }
            reader.close();

            // Count all accessible beam splits
            System.out.println("How many times will the beam be split?: " +
                    countBeamSplits(splitterSearch, '^', 'S'));

            // Count all accessible beam splits possibilities
            System.out.println("How many possible beam splits?: " +
                    countBeamSplitsPossibilities(splitterSearch, '^', 'S'));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long countBeamSplitsPossibilities(char[][] splitterSearch, char splitter, char start) {
        int rows = splitterSearch.length;
        int cols = splitterSearch[0].length;

        // find S
        int sRow = -1, sCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (splitterSearch[r][c] == start) {
                    sRow = r;
                    sCol = c;
                }
            }
            if (sRow != -1) break;
        }
        // Array holding number of timelines arriving at each column of current row
        long[] timelinePrev = new long[cols];
        timelinePrev[sCol] = 1;  // initial timeline at start column

        // process each row below S
        for (int r = sRow + 1; r < rows; r++) {
            long[] timelineCurr = new long[cols];

            for (int c = 0; c < cols; c++) {
                if (timelinePrev[c] == 0) {
                    continue;// no timelines from this column
                }
                if (splitterSearch[r - 1][c] == splitter) {
                    //splitter splits timeline left and right
                    if (c - 1 >= 0) {
                        timelineCurr[c - 1] += timelinePrev[c];
                    }
                    if (c + 1 < cols) {
                        timelineCurr[c + 1] += timelinePrev[c];
                    }
                } else {
                    //no splitter, timeline continues straight down
                    timelineCurr[c] += timelinePrev[c];
                }
            }
            timelinePrev = timelineCurr; //move o next row
        }
        //sum all timelines in last row
        long totalTimelines = 0;
        for(long val:timelinePrev){
            totalTimelines += val;
        }
        return totalTimelines;
    }

    public static int countBeamSplits(char[][] splitterSearch, char splitter, char start) {
        int rows = splitterSearch.length;
        int cols = splitterSearch[0].length;

        // find S
        int sRow = -1, sCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (splitterSearch[r][c] == start) {
                    sRow = r;
                    sCol = c;
                }
            }
        }

        // beams represented as boolean array of columns
        boolean[] beams = new boolean[cols];
        beams[sCol] = true;   // initial beam under S

        int splitCount = 0;

        // process each row below S
        for (int r = sRow + 1; r < rows; r++) {

            boolean[] newBeams = new boolean[cols];

            // beams simply move downward
            for (int c = 0; c < cols; c++) {
                if (beams[c]) {
                    newBeams[c] = true;
                }
            }

            boolean changed = true;

            // repeatedly process splitters on this row
            while (changed) {
                changed = false;

                for (int c = 0; c < cols; c++) {
                    if (newBeams[c] && splitterSearch[r][c] == splitter) {
                        // beam hits splitter → split
                        newBeams[c] = false;
                        splitCount++;

                        if (c - 1 >= 0 && !newBeams[c - 1]) {
                            newBeams[c - 1] = true;
                            changed = true;
                        }
                        if (c + 1 < cols && !newBeams[c + 1]) {
                            newBeams[c + 1] = true;
                            changed = true;
                        }
                    }
                }
            }
            // beams that survive continue downward next row
            for (int c = 0; c < cols; c++) {
                if (newBeams[c] && splitterSearch[r][c] != splitter) {
                    beams[c] = true;
                } else {
                    beams[c] = false;
                }
            }
            // stop early if no beams remain
            boolean any = false;
            for (boolean b : beams) {
                if (b) {
                    any = true;
                    break;
                }
            }
            if (!any) break;
        }
        return splitCount;
    }
}
