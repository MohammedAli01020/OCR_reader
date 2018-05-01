package com.google.android.gms.samples.vision.barcodereader.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = ProductsProvider.AUTHORITY, database = ProductsDatabase.class)
public final class ProductsProvider {
    static final String AUTHORITY = "com.google.android.gms.samples.vision.barcodereader";

    @TableEndpoint(table = ProductsDatabase.TABLE_PERSON)
    public static class Persons {

        @ContentUri(
                path = ProductsContract.PersonEntry.TABLE_NAME,
                type = "vnd.android.cursor.dir/" + ProductsContract.PersonEntry.TABLE_NAME,
                defaultSort = ProductsContract.PersonEntry.COLUMN_NAME + " ASC")
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/" + ProductsContract.PersonEntry.TABLE_NAME);

    }
}
