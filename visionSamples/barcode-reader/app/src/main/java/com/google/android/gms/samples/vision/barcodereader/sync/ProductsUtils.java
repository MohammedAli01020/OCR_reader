package com.google.android.gms.samples.vision.barcodereader.sync;

import android.content.ContentValues;

import com.google.android.gms.samples.vision.barcodereader.data.ProductsContract;


final class ProductsUtils {


    static ContentValues[] getFakeData() {

        ContentValues[] values = new ContentValues[10];

        ContentValues person1 = new ContentValues();
        person1.put(ProductsContract.PersonEntry.COLUMN_NAME, "Ahmed");
        person1.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[0] = person1;

        ContentValues person2 = new ContentValues();
        person2.put(ProductsContract.PersonEntry.COLUMN_NAME, "Ali");
        person2.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01000484313");
        values[1] = person2;

        ContentValues person3 = new ContentValues();
        person3.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person3.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[2] = person3;

        ContentValues person4 = new ContentValues();
        person4.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person4.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[3] = person4;

        ContentValues person5 = new ContentValues();
        person5.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person5.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[4] = person5;


        ContentValues person6 = new ContentValues();
        person6.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person6.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[5] = person6;

        ContentValues person7 = new ContentValues();
        person7.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person7.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[6] = person7;

        ContentValues person8 = new ContentValues();
        person8.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person8.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[7] = person8;

        ContentValues person9 = new ContentValues();
        person9.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person9.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[8] = person9;

        ContentValues person10 = new ContentValues();
        person10.put(ProductsContract.PersonEntry.COLUMN_NAME, "Mohamed");
        person10.put(ProductsContract.PersonEntry.COLUMN_COUNT, "01020718313");
        values[9] = person10;

        return values;
    }
}

