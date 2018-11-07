package com.example.beket.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.beket.inventoryapp.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    private BookDbHelper mDbHelper;
    private SQLiteDatabase database;
    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        if(getContext() != null) cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                assert values != null;
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {

        sanityCheck(values);

        database = mDbHelper.getWritableDatabase();
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e("this", "Failed to insert row for " + uri);
            return null;
        }
        if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        sanityCheck(values);

        if (values.size() == 0) {
            return 0;
        }
        database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void sanityCheck(ContentValues values) {

        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (productName.isEmpty()) {
                throw new IllegalArgumentException("Book requires a name.");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_PRODUCT_AUTHOR)) {
            String productName = values.getAsString(BookEntry.COLUMN_PRODUCT_AUTHOR);
            if (productName.isEmpty()) {
                throw new IllegalArgumentException("Book requires an author.");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_PRODUCT_PRICE)) {
            Double productPrice = values.getAsDouble(BookEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice == null || productPrice < 0) {
                throw new IllegalArgumentException("Book requires valid price.");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer productQuantity = values.getAsInteger(BookEntry.COLUMN_PRODUCT_QUANTITY);
            if (productQuantity == null || productQuantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity.");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName.isEmpty()) {
                throw new IllegalArgumentException("Book requires the suppliers name.");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhoneNumber = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (supplierPhoneNumber.isEmpty()) {
                throw new IllegalArgumentException("Book requires the suppliers phone number");
            }
        }
    }
}
