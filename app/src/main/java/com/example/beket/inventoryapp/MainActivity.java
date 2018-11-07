package com.example.beket.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beket.inventoryapp.data.BookContract.BookEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.empty_view)
    TextView emptyView;

    BookCursorAdapter bookCursorAdapter;
    private final static int BOOK_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        bookCursorAdapter = new BookCursorAdapter(this, null);
        listView.setAdapter(bookCursorAdapter);
        listView.setEmptyView(emptyView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editActivity = new Intent(MainActivity.this, EditorActivity.class);
                editActivity.setData(ContentUris.withAppendedId(BookEntry.CONTENT_URI, id));
                startActivity(editActivity);
            }
        });
        bookCursorAdapter.setOnOrderClickListener(new OnOrderClickListener() {
            @Override
            public void onOrderClick(View v) {
                View parentRow = (View) v.getParent();
                int position = listView.getPositionForView(parentRow);
                String[] projection = {BookEntry._ID, BookEntry.COLUMN_PRODUCT_QUANTITY};
                Cursor cursor = getContentResolver().query(BookEntry.CONTENT_URI, projection, null, null, null);

                if (cursor != null && cursor.move(position + 1)) {
                    int index = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
                    int quantity = Integer.parseInt(cursor.getString(index));
                    if (quantity > 0) {
                        ContentValues values = new ContentValues();
                        quantity--;
                        values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                        Uri uriUpdate = Uri.withAppendedPath(BookEntry.CONTENT_URI, String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry._ID))));
                        String selection = BookEntry._ID + " = ?";
                        String[] selectionArgs = {String.valueOf(ContentUris.parseId(uriUpdate))};
                        getContentResolver().update(uriUpdate, values, selection, selectionArgs);
                        cursor.close();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.no_books_to_sell), Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
            case R.id.search:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllBooks() {
        getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
    }

    private void insertBook() {
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "The 100");
        values.put(BookEntry.COLUMN_PRODUCT_AUTHOR, "Kass Morgan");
        values.put(BookEntry.COLUMN_PRODUCT_PRICE, 9.99);
        values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, 15);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Kobo");
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "0753824463");

        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String productName = sharedPreferences.getString(getString(R.string.settings_search_name_key), "");
        String productAuthor = sharedPreferences.getString(getString(R.string.settings_search_author_key), "");

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_AUTHOR,
                BookEntry.COLUMN_PRODUCT_PRICE,
                BookEntry.COLUMN_PRODUCT_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        if (productName.length() != 0 || productAuthor.length() != 0) {
            String selection;
            String[] selectionArgs;
            if (productName.length() != 0 && productAuthor.length() == 0) {
                selection = BookEntry.COLUMN_PRODUCT_NAME + " =? ";
                selectionArgs = new String[]{productName};
                return new CursorLoader(
                        this,
                        BookEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);

            } else if (productName.length() == 0) {
                selection = BookEntry.COLUMN_PRODUCT_AUTHOR + " =?";
                selectionArgs = new String[]{productAuthor};

            } else {
                selection = BookEntry.COLUMN_PRODUCT_NAME + " =? AND " + BookEntry.COLUMN_PRODUCT_AUTHOR + " =?";
                selectionArgs = new String[]{productName, productAuthor};
            }
            return new CursorLoader(
                    this,
                    BookEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
        } else {
            return new CursorLoader(
                    this,
                    BookEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        bookCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookCursorAdapter.swapCursor(null);
    }


}
