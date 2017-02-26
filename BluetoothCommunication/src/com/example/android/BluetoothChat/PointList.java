package com.example.android.BluetoothChat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pusik on 2016-07-05.
 */
public class PointList {
    public List<Integer> lists = null;
    public int maxItem = 16;

    public PointList() {
        lists = new ArrayList<Integer>();
    }

    private void removeItem() {
        if(lists.isEmpty() == false)
            lists.remove(0);
    }
    public void addItem(int item) {
        if(lists.size() >= maxItem - 1)
            removeItem();
        lists.add(Integer.valueOf(item));
    }
}