/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.utils;

/**
 *
 * @author Florin Matei
 */
public final class PropertyUtils {

    private PropertyUtils() {
    }

    public static int getIntegerFromString(String value, int defaultValue) {
        int returnValue = defaultValue;
        try {
            returnValue = Integer.valueOf(value);
        }
        catch (Exception nfe) {
        }
        return returnValue;
    }

    public static boolean getBooleanFromString(String value, boolean defaultValue) {
        boolean returnValue = defaultValue;
        try {
            returnValue = Boolean.valueOf(value);
        }
        catch (Exception nfe) {
        }
        return returnValue;
    }
}
