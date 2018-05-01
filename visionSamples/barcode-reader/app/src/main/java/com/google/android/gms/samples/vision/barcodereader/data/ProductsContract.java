package com.google.android.gms.samples.vision.barcodereader.data;

import android.provider.BaseColumns;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.REAL;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

public final class ProductsContract {

    public static final class PersonEntry implements BaseColumns {
        static final String TABLE_NAME = "product";

        @DataType(INTEGER)
        @PrimaryKey
        @AutoIncrement
        public static final String _ID = BaseColumns._ID;
        @DataType(TEXT)
        @NotNull
        public static final String COLUMN_NAME = "name";
        @DataType(TEXT)
        @NotNull
        public static final String COLUMN_COUNT = "count";
        @DataType(REAL)
        @NotNull
        public static final String COLUMN_PRICE = "price";
        @DataType(TEXT)
        @NotNull
        @Unique
        public static final String COLUMN_BARCODE_ID = "barcodeId";

    }
}
