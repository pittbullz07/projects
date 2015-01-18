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
public final class ArrayUtils {

    private ArrayUtils() {

    }

    public static <T> int indexOf(T[] array, T toFind) {
        int index = -1;
        if (array != null) {
            for (int i = 0; i < array.length; ++i) {
                if (array[i].equals(toFind)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static <T> boolean arrayContains(T[] array, T toFind) {
        return indexOf(array, toFind) != -1;
    }
}
