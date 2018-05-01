package com.google.android.gms.samples.vision.barcodereader.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.samples.vision.barcodereader.R;

import java.text.NumberFormat;


public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    public void setmCursor(Cursor mCursor) {
        this.mCursor = mCursor;
        notifyDataSetChanged();
    }

    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_product, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ProductsAdapter.ViewHolder holder, int position) {

        if (!mCursor.moveToPosition(position)) return;

        holder.name.setText(mCursor.getString
                (mCursor.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_NAME)));

        // count
        String productCount = mContext.getString(R.string.product_count,
                mCursor.getString(mCursor.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_COUNT)));
        holder.count.setText(productCount);

        // price
        float price = mCursor.getFloat(mCursor.getColumnIndexOrThrow(ProductsContract.PersonEntry.COLUMN_PRICE));
        String priceFormatted = formatPrice(price);
        String productPrice = mContext.getString(R.string.product_price, priceFormatted);
        holder.price.setText(productPrice);

        int id = mCursor.getInt((mCursor.getColumnIndexOrThrow(ProductsContract.PersonEntry._ID)));

        holder.itemView.setTag(id);
    }

    private String formatPrice(float price) {
        return NumberFormat.getCurrencyInstance().format(price);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView count;
        TextView price;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            count = itemView.findViewById(R.id.tv_count);
            price = itemView.findViewById(R.id.tv_price);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int id = mCursor.getInt((mCursor.getColumnIndexOrThrow(ProductsContract.PersonEntry._ID)));

            mOnItemClickedListner.onClick(id);
        }
    }

    private OnItemClickedListner mOnItemClickedListner;

    public interface OnItemClickedListner {
        void onClick(int id);
    }

    public ProductsAdapter(OnItemClickedListner mOnItemClickedListner) {
        this.mOnItemClickedListner = mOnItemClickedListner;
    }
}
