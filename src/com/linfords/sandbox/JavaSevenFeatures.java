package com.linfords.sandbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author slinford
 */
public class JavaSevenFeatures {

    public static void underscores() {
        System.out.println();
        System.out.println("Underscores in Numeric Literals");

        // hex with underscores
        final int x = 0x34_EF;
        // binary (new)
        final int y = 0b1111_1010_0000;
        // octal with underscores
        final int z = 076_54_32_10;
        final int z2 = 0_76_54_32_10;

        System.out.println("x = " + x);
        System.out.println("y = " + y);
        System.out.println("z = " + z);
        System.out.println("z2 = " + z);
    }

    public static void typeInferance() {
        System.out.println();
        System.out.println("Improved Type Infererance");

        // Right hand types no longer required
        Map<Integer, List<String>> m = new TreeMap();
        List<String> t = new ArrayList();

        t.add("This");
        t.add("is");
        m.put(10, t);

        t = new ArrayList();
        t.add("a");
        t.add("test");
        m.put(20, t);

        t = new ArrayList();
        t.add("of");
        t.add("the");
        t.add("emergency");
        m.put(30, t);

        for (Entry e : m.entrySet()) {
            System.out.println(e);
        }
    }

    public static void stringSwitching(String s) {
        switch (s) {
            case "what":
                System.out.println("Found what");
                break;
            case "who":
                System.out.println("Found who");
                break;
            case "the":
            case "a":
                System.out.println("Found article: " + s);
                break;
            default:
                System.out.println("Not found: " + s);
        }
    }

    public static void stringSwitchTest() {
        System.out.println();
        System.out.println("Switching on Strings");
        stringSwitching("Dog");
        stringSwitching("The");
        stringSwitching("what");
        stringSwitching("who");
        stringSwitching("a");
        stringSwitching("the");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        underscores();
        typeInferance();
        stringSwitchTest();
    }
}
