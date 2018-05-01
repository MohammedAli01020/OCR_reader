package com.google.android.gms.samples.vision.barcodereader.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.samples.vision.barcodereader.data.ProductsContract;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsProvider;


public class ProductsIntentService extends IntentService {

    public static final String ACTION_INSERT_DUMMY_DATA = "action-insertDummyData";
    public static final String ACTION_INSERT_ITEM = "action-insertItem";
    public static final String ACTION_DELETE_ALL_DATA = "action-deleteAllData";
    public static final String ACTION_DELETE_ITEM = "action-deleteItem";
    public static final String ACTION_UPDATE_ITEM = "action-updateItem";

    public static final String ACTION_CHECK_PRODUCT_EXITS = "action-checkProductExits";
    public static final String ACTION_INCREMENT_COUNT_BY_ONE = "action-incrementCountByOne";
    public static final String ACTION_DECREMENT_COUNT_BY_ONE = "action-decrementCountByOne";


    public ProductsIntentService() {
        super("ProductsIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        String action = intent.getAction();

        assert action != null;
        switch (action) {
            case ACTION_INSERT_DUMMY_DATA: {
                insertDummyData();
                break;
            }

            case ACTION_DELETE_ALL_DATA: {
                deleteAllData();
                break;
            }

            case ACTION_DELETE_ITEM: {
                int id = intent.getIntExtra("id", 0);
                deleteItemById(id);
                break;
            }

            case ACTION_INSERT_ITEM: {
                Bundle bundle = intent.getExtras();
                insertItem(bundle);
                break;
            }

            case ACTION_UPDATE_ITEM: {
                Bundle bundle = intent.getExtras();
                updateItem(bundle);
                break;
            }

            case ACTION_INCREMENT_COUNT_BY_ONE: {
                String productBarcodeId = intent.getStringExtra("productBarcodeId");
                incrementCountByOne(productBarcodeId);
                break;
            }

            case ACTION_DECREMENT_COUNT_BY_ONE: {
                String productBarcodeId = intent.getStringExtra("productBarcodeId");
                decrementCountByOne(productBarcodeId);
                break;
            }
        }

    }

    synchronized private void incrementCountByOne(String barcodeID) {

        Cursor cursor = null;
        try {
             cursor = getContentResolver().query(ProductsProvider.Persons.CONTENT_URI,
                    null,
                    ProductsContract.PersonEntry.COLUMN_BARCODE_ID + "=?",
                    new String[] {barcodeID},
                    null);

            assert cursor != null;
            if (!cursor.moveToFirst()) return;

            String count = cursor.getString(cursor.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_COUNT));
            int newCount = Integer.parseInt(count) + 1;

            Log.d("barcode", newCount + "");

            ContentValues values = new ContentValues();
            values.put(ProductsContract.PersonEntry.COLUMN_COUNT, String.valueOf(newCount));

            getContentResolver().update(ProductsProvider.Persons.CONTENT_URI, values,
                    ProductsContract.PersonEntry.COLUMN_BARCODE_ID + "=?",
                    new String[] {String.valueOf(barcodeID)});


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    synchronized private void decrementCountByOne(String barcodeID) {

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ProductsProvider.Persons.CONTENT_URI,
                    null,
                    ProductsContract.PersonEntry.COLUMN_BARCODE_ID + "=?",
                    new String[] {barcodeID},
                    null);

            assert cursor != null;
            if (!cursor.moveToFirst()) return;

            String count = cursor.getString(cursor.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_COUNT));
            int c = Integer.parseInt(count);
            int newCount;
            if (c >= 1) {
                 newCount = c - 1;
            } else {
                Toast.makeText(getApplicationContext(), "empty !", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("barcode", newCount + "");

            ContentValues values = new ContentValues();
            values.put(ProductsContract.PersonEntry.COLUMN_COUNT, String.valueOf(newCount));

            getContentResolver().update(ProductsProvider.Persons.CONTENT_URI, values,
                    ProductsContract.PersonEntry.COLUMN_BARCODE_ID + "=?",
                    new String[] {String.valueOf(barcodeID)});


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }



    synchronized private void updateItem(Bundle bundle) {
        String name = bundle.getString("name");
        String count = bundle.getString("productCount");
        int id = bundle.getInt("id");

        ContentValues values = new ContentValues();
        values.put(ProductsContract.PersonEntry.COLUMN_NAME, name);
        values.put(ProductsContract.PersonEntry.COLUMN_COUNT, count);

        getContentResolver().update(ProductsProvider.Persons.CONTENT_URI, values,
                ProductsContract.PersonEntry._ID + "=?",
                new String[] {String.valueOf(id)});

    }

    synchronized private void insertItem(Bundle bundle) {
        String name = bundle.getString("name");
        String productCount = bundle.getString("productCount");
        Float productPrice = bundle.getFloat("productPrice");
        String productBarcodeId = bundle.getString("productBarcodeId");

        ContentValues values = new ContentValues();
        values.put(ProductsContract.PersonEntry.COLUMN_NAME, name);
        values.put(ProductsContract.PersonEntry.COLUMN_COUNT, productCount);
        values.put(ProductsContract.PersonEntry.COLUMN_PRICE, productPrice);

        values.put(ProductsContract.PersonEntry.COLUMN_BARCODE_ID, productBarcodeId);

        getContentResolver().insert(
                ProductsProvider.Persons.CONTENT_URI,
                values);
    }


    synchronized private void deleteItemById(int id) {
        getContentResolver().delete(ProductsProvider.Persons.CONTENT_URI,
                ProductsContract.PersonEntry._ID + "=?",
                new String[] {String.valueOf(id)});
    }

    synchronized private void insertDummyData() {

        try {
            ContentValues[] values = ProductsUtils.getFakeData();
            if (values != null && values.length != 0) {
                getContentResolver().bulkInsert(
                        ProductsProvider.Persons.CONTENT_URI,
                        values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    synchronized private void deleteAllData() {
        getContentResolver().delete(
                ProductsProvider.Persons.CONTENT_URI,
                null,
                null);
    }
}
