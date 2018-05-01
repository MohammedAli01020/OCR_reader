package com.google.android.gms.samples.vision.barcodereader.userInterface;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.R;
import com.google.android.gms.samples.vision.barcodereader.barcode.BarcodeCaptureActivity;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsContract;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsProvider;
import com.google.android.gms.samples.vision.barcodereader.sync.ProductsIntentService;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.NumberFormat;

public class SellActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int LOADER_ID = 301;

    private static final String TAG = "BarcodeBuy";
    private FloatingActionButton mFabBuyButton;
    private TextView mSummaryTextView;
    private TextView mTotalPriceTextView;
    private Toast mToast;
    private String mProductName;
    private String mProductBarcodeId;
    private float TotalPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        mFabBuyButton = findViewById(R.id.fab_buy_product);
        mSummaryTextView = findViewById(R.id.tv_summary);
        mTotalPriceTextView = findViewById(R.id.tv_total_price);


        mFabBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("productBarcodeId", "productBarcodeId");
        getSupportLoaderManager().initLoader(LOADER_ID, bundle, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    mProductBarcodeId = barcode.rawValue;
                    // restart loader
                    Bundle bundle = new Bundle();
                    bundle.putString("productBarcodeId", mProductBarcodeId);

                    getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);

                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Log.e(TAG, String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void appendDataToTextView(Cursor data) {
        if (!data.moveToFirst()) return;

        mProductName = data.getString
                (data.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_NAME));

        float price = data.getFloat
                (data.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_PRICE));

        mSummaryTextView.append("name: " + mProductName + "\n");

        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(SellActivity.this,mProductName + " exists", Toast.LENGTH_SHORT);
        mToast.show();

        mSummaryTextView.append("price: " + NumberFormat.getCurrencyInstance().format(price) + "\n\n");

        TotalPrice += price;

        mTotalPriceTextView.setText("Total price = " + NumberFormat.getCurrencyInstance().format(TotalPrice));
    }

    private void decrementCountByOne(String mProductBarcodeId) {
        Intent intent = new Intent(this, ProductsIntentService.class);
        intent.setAction(ProductsIntentService.ACTION_DECREMENT_COUNT_BY_ONE);

        intent.putExtra("productBarcodeId", mProductBarcodeId);
        startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (args == null) return null;

        return new CursorLoader(
                this,
                ProductsProvider.Persons.CONTENT_URI,
                null,
                ProductsContract.PersonEntry.COLUMN_BARCODE_ID + "=?",
                new String[]{args.getString("productBarcodeId")},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.getCount() == 0) {
            /*
            if (mToast != null) mToast.cancel();

            mToast = Toast.makeText(SellActivity.this, "not exists", Toast.LENGTH_SHORT);
            mToast.show();
            */
            return;
        }

        //decrementCountByOne(mProductBarcodeId);
        appendDataToTextView(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSummaryTextView.setText("");
    }

}
