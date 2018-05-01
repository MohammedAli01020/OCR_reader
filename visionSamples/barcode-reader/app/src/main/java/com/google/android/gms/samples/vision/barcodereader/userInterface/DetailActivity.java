package com.google.android.gms.samples.vision.barcodereader.userInterface;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.R;
import com.google.android.gms.samples.vision.barcodereader.barcode.BarcodeCaptureActivity;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsContract;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsProvider;
import com.google.android.gms.samples.vision.barcodereader.sync.ProductsIntentService;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.NumberFormat;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 201;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    private EditText mNameEditText;
    private EditText mCountEditText;
    private EditText mPriceEditText;
    private Button mBarcodeReaderButton;
    private int mId;
    private Toast mToast;
    private boolean mPetHasChanged = false;
    private String mProductBarcodeId;

    private String mProductName;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mNameEditText = findViewById(R.id.et_name);
        mCountEditText = findViewById(R.id.et_count);
        mPriceEditText = findViewById(R.id.et_price);

        mBarcodeReaderButton = findViewById(R.id.bt_barcode_reader);

        mBarcodeReaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mCountEditText.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        if (intent == null) throw new NullPointerException("intent not found!");

        mId = intent.getIntExtra(Intent.EXTRA_TEXT, -1);

        if (mId == -1) {
            setTitle("Add");
            mBarcodeReaderButton.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();
        } else {
            setTitle("Edit");
            mBarcodeReaderButton.setVisibility(View.INVISIBLE);
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    mProductBarcodeId = barcode.rawValue;
                    // onPostExecute
                    new BarcodeTask().execute(mProductBarcodeId);

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

    private void incrementCountByOne(String mProductBarcodeId) {
        Intent intent = new Intent(this, ProductsIntentService.class);
        intent.setAction(ProductsIntentService.ACTION_INCREMENT_COUNT_BY_ONE);

        intent.putExtra("productBarcodeId", mProductBarcodeId);
        startService(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mId == -1) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete: {
                deleteItem();
                return true;
            }
            case R.id.action_add: {
                if (mId == -1) {
                    addItem();

                } else {
                    updateItem();
                }
                return true;
            }

            case android.R.id.home: {
                cancel();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteItem() {

        Intent intent = new Intent(this, ProductsIntentService.class);
        intent.setAction(ProductsIntentService.ACTION_DELETE_ITEM);

        intent.putExtra("id", mId);
        startService(intent);

        mNameEditText.getText().clear();
        mCountEditText.getText().clear();
        finish();
    }

    private void updateItem() {
        if (TextUtils.isEmpty(mNameEditText.getText()) ||
                TextUtils.isEmpty(mCountEditText.getText())) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, "name or phone number is empty!", Toast.LENGTH_SHORT);
            mToast.show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("name", mNameEditText.getText().toString());
        bundle.putString("productCount", mCountEditText.getText().toString());
        bundle.putInt("id", mId);

        Intent intent = new Intent(this, ProductsIntentService.class);
        intent.setAction(ProductsIntentService.ACTION_UPDATE_ITEM);

        intent.putExtras(bundle);
        startService(intent);

        mNameEditText.getText().clear();
        mCountEditText.getText().clear();

        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(this, "product updated successfully", Toast.LENGTH_SHORT);
        mToast.show();
        finish();
    }

    private void cancel() {
        if (!mPetHasChanged) {
            mNameEditText.getText().clear();
            mCountEditText.getText().clear();

            /*
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, "canceled", Toast.LENGTH_SHORT);
            mToast.show();
            */
            finish();
        } else {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            finish();
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("unsaved");
        builder.setPositiveButton("discard", discardButtonClickListener);
        builder.setNegativeButton("keep editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void addItem() {
        if (TextUtils.isEmpty(mNameEditText.getText()) ||
                TextUtils.isEmpty(mCountEditText.getText()) ||
                TextUtils.isEmpty(mPriceEditText.getText()) ||
                TextUtils.isEmpty(mProductBarcodeId)) {

            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, "name or count or productCode is empty!", Toast.LENGTH_SHORT);
            mToast.show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("name", mNameEditText.getText().toString());
        bundle.putString("productCount", mCountEditText.getText().toString());
        bundle.putFloat("productPrice", Float.parseFloat(mPriceEditText.getText().toString()));
        bundle.putString("productBarcodeId", mProductBarcodeId);

        Intent intent = new Intent(this, ProductsIntentService.class);
        intent.setAction(ProductsIntentService.ACTION_INSERT_ITEM);

        intent.putExtras(bundle);
        startService(intent);

        mNameEditText.getText().clear();
        mCountEditText.getText().clear();
        mPriceEditText.getText().clear();

        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(this, "product added successfully", Toast.LENGTH_SHORT);
        mToast.show();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ProductsProvider.Persons.CONTENT_URI,
                null,
                ProductsContract.PersonEntry._ID + "=?",
                new String[]{String.valueOf(mId)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) return;

        mNameEditText.setText(data.getString
                (data.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_NAME)));
        mCountEditText.setText(data.getString
                (data.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_COUNT)));

        mPriceEditText.setText(NumberFormat.getCurrencyInstance().format(data.getFloat
                (data.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_PRICE))));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @SuppressLint("StaticFieldLeak")
    private class BarcodeTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            if (strings.length < 1 || strings[0] == null) {
                return null;
            }

            String productBarcodeId = strings[0];

            Cursor cursor = null;
            boolean isExist = false;
            try {
                cursor = getContentResolver().query(ProductsProvider.Persons.CONTENT_URI,
                        null,
                        ProductsContract.PersonEntry.COLUMN_BARCODE_ID + "=?",
                        new String[]{productBarcodeId},
                        null);

                assert cursor != null;
                if (!cursor.moveToFirst()) return false;
                mProductName = cursor.getString(cursor.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_NAME));

                isExist = cursor.getCount() != 0;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return isExist;
        }

        @Override
        protected void onPostExecute(Boolean isExists) {
            if (isExists) {
                if (mToast != null) mToast.cancel();

                mToast = Toast.makeText(DetailActivity.this, mProductName + " exists", Toast.LENGTH_SHORT);
                mToast.show();
                incrementCountByOne(mProductBarcodeId);
            } else {
                if (mToast != null) mToast.cancel();

                mToast = Toast.makeText(DetailActivity.this, "not exists", Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }


}
