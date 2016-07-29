package com.gigaspaces.gigapro.xapapi.options;

import java.util.Random;

public class AbstractUtilities {

    public static String NiceInteger(int inputValue) {
        Integer restValue = inputValue % 1000;
        String rightTail = String.format("%03d", restValue);
        inputValue /= 1000;

        while (inputValue > 0) {
            restValue = inputValue % 1000;
            rightTail = String.format("%03d", restValue) + "'" + rightTail;
            inputValue /= 1000;
        }

        int leadingDigit = 0;
        while (rightTail.charAt(leadingDigit) == '0') leadingDigit++;
        return rightTail.substring(leadingDigit);
    }

    public static String GetRandomHexString(int length) {
        Random random = DataObjectFactory.ToolkitRandom;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int symbol = random.nextInt(16);
            if (symbol >= 10)
                symbol = 'a' + (symbol - 10);
            else
                symbol = '0' + symbol;
            stringBuilder.append((char) symbol);
        }
        return stringBuilder.toString();
    }
}
