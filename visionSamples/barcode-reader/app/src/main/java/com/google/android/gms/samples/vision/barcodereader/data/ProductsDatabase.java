package com.google.android.gms.samples.vision.barcodereader.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = ProductsDatabase.VERSION)
final class ProductsDatabase {

    static final int VERSION = 1;

    @Table(ProductsContract.PersonEntry.class)
    static final String TABLE_PERSON = ProductsContract.PersonEntry.TABLE_NAME;

}
