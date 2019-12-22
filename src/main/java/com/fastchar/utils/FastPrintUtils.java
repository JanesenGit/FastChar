package com.fastchar.utils;

public class FastPrintUtils {

    public synchronized static void printProgress(int curr, int total, int length) {
        printProgress('[', ']', '*', ' ',
                curr, total, length);
    }

    public synchronized static void printProgress(char startBound, char endBound,
                                     char progress, char secondProgress,
                                     int curr, int total, int length) {
        System.out.print('\r');
        double perValue = FastNumberUtils.formatToDouble(curr) / FastNumberUtils.formatToDouble(total);
        int perString = FastNumberUtils.formatToInt(perValue * 100);
        String preTip = perString + "%";
        int currProgressLength = (int) (perValue * length);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(startBound);
        for (int i = 0; i < length; i++) {
            if (i <= currProgressLength) {
                stringBuilder.append(progress);
            } else {
                stringBuilder.append(secondProgress);
            }
        }
        stringBuilder.append(preTip);
        stringBuilder.append(endBound);
        System.out.print(stringBuilder);
    }
}
