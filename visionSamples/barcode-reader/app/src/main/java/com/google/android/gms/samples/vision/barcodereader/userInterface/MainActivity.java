package com.google.android.gms.samples.vision.barcodereader.userInterface;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.samples.vision.barcodereader.R;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsAdapter;
import com.google.android.gms.samples.vision.barcodereader.data.ProductsProvider;
import com.google.android.gms.samples.vision.barcodereader.sync.ProductsIntentService;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        ProductsAdapter.OnItemClickedListner {

    private static final int LOADER_ID = 104;
    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProductsAdapter mProductsAdapter;

    private ProgressBar mProgressBar;
    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyView = findViewById(R.id.empty_list);

        mProgressBar = findViewById(R.id.pb_indicator);

        mRecyclerView = findViewById(R.id.rv_list);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mProductsAdapter = new ProductsAdapter(this);

        mRecyclerView.setAdapter(mProductsAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();
                Intent deleteItemIntent = new Intent(getBaseContext(), ProductsIntentService.class);
                deleteItemIntent.setAction(ProductsIntentService.ACTION_DELETE_ITEM);
                deleteItemIntent.putExtra("id", id);
                startService(deleteItemIntent);

                mProductsAdapter.notifyDataSetChanged();

            }
        }).attachToRecyclerView(mRecyclerView);

        mFloatingActionButton = findViewById(R.id.fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent DetailIntent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(DetailIntent);
            }
        });

        mProgressBar.setVisibility(View.VISIBLE);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_remove_all_data: {
                deleteAllData();
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                return true;
            }

            case R.id.action_sell_product: {
                startActivity(new Intent(this, SellActivity.class));
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void deleteAllData() {
        Intent deleteAllDataIntentService =
                new Intent(this, ProductsIntentService.class);

        deleteAllDataIntentService.setAction(ProductsIntentService.ACTION_DELETE_ALL_DATA);

        startService(deleteAllDataIntentService);
    }

    private void insertDummyData() {
        Intent insertDummyDataIntentService =
                new Intent(this, ProductsIntentService.class);

        insertDummyDataIntentService.setAction(ProductsIntentService.ACTION_INSERT_DUMMY_DATA);

        startService(insertDummyDataIntentService);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ProductsProvider.Persons.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.INVISIBLE);

        if (data == null || data.getCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mProductsAdapter.setmCursor(data);

        } else {
            mEmptyView.setVisibility(View.INVISIBLE);
            mProductsAdapter.setmCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductsAdapter.setmCursor(null);
    }

    @Override
    public void onClick(int id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, id);
        startActivity(intent);
    }
}
