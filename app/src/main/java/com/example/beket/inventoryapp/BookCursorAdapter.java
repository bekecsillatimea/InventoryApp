package com.example.beket.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.beket.inventoryapp.data.BookContract.BookEntry;


public class BookCursorAdapter extends CursorAdapter {

    private OnOrderClickListener mOnOrderClickListener;

    BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView productName = view.findViewById(R.id.product_name_text_view);
        TextView productAuthor = view.findViewById(R.id.product_author_edit_text);
        TextView productPrice = view.findViewById(R.id.product_price_text_view);
        TextView productQuantity = view.findViewById(R.id.product_quantity_text_view);
        Button soldButton = view.findViewById(R.id.sold_button);

        soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnOrderClickListener.onOrderClick(v);

//                Uri uriQuery = ContentUris.withAppendedId(BookEntry.CONTENT_URI, viewId + 1);
//                String[] projection = {BookEntry.COLUMN_PRODUCT_QUANTITY};
//                Cursor cursor = context.getContentResolver().query(uriQuery, projection, null, null, null);
//
//                assert cursor != null;
//                int quantity = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_QUANTITY)));
//                if (quantity > 0) {
//                    ContentValues values = new ContentValues();
//                    quantity--;
//                    values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, quantity);
//                    Uri uriUpdate = Uri.withAppendedPath(BookEntry.CONTENT_URI, String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry._ID))));
//                    String selection = BookEntry._ID + " = ?";
//                    String[] selectionArgs = {String.valueOf(ContentUris.parseId(uriUpdate))};
//                    context.getContentResolver().update(uriUpdate, values, selection, selectionArgs);
//                } else {
//                    Toast.makeText(context, context.getString(R.string.no_books_to_sell), Toast.LENGTH_LONG).show();
//                }
//                cursor.close();
            }
        });
        String price = String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_PRICE))) + context.getString(R.string.dollar_sign);
        String quantity = context.getString(R.string.quantity) + String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_QUANTITY)));

        productName.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_NAME)));
        productAuthor.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_AUTHOR)));
        productPrice.setText(price);
        productQuantity.setText(quantity);
    }

    void setOnOrderClickListener(OnOrderClickListener onOrderClickListener){
        mOnOrderClickListener = onOrderClickListener;
    }
}
