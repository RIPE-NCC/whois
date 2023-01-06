package net.ripe.db.nrtm4.util;

import java.util.ArrayList;
import java.util.List;


public class ListUtil {

    public static <T> List<List<T>> makeBatches(final List<T> inList, final int batchSize) {
        final List<List<T>> batches = new ArrayList<>();
        List<T> currentList = new ArrayList<>(batchSize);
        for (final T object : inList) {
            if (currentList.size() < batchSize) {
                currentList.add(object);
            } else {
                batches.add(currentList);
                currentList = new ArrayList<>(batchSize);
                currentList.add(object);
            }
        }
        batches.add(currentList);
        return batches;
    }

}
