package com.example.beket.inventoryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.beket.inventoryapp.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.edit_product_name)
    EditText mNameEditText;
    @BindView(R.id.edit_product_price)
    EditText mPriceEditText;
    @BindView(R.id.edit_product_author)
    EditText mAuthorEditText;
    @BindView(R.id.edit_product_quantity)
    EditText mQuantityEditText;
    @BindView(R.id.edit_supplier_name)
    EditText mSupplierNameEditText;
    @BindView(R.id.edit_supplier_phone_number)
    EditText mSupplierPhoneNumberEditText;
    @BindView(R.id.quantity_linear_layout)
    LinearLayout quantityLinearLayout;
    @BindView(R.id.minus_button)
    Button minusButton;
    @BindView(R.id.plus_button)
    Button plusButton;
    @BindView(R.id.product_quantity_text_view)
    TextView quantityTextView;
    @BindView(R.id.order_button)
    Button orderButton;

    Uri bookUri;
    private static final int BOOK_ID = 1;
    private boolean mBookHasChanged = false;
    String productQuantity;
    String supplierPhoneNumber;
    int quantity;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        bookUri = intent.getData();

        if (bookUri == null) {
            setTitle(getString(R.string.insert_book));
            invalidateOptionsMenu();
            quantityLinearLayout.setVisibility(View.GONE);
            orderButton.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.edit_book));
            getLoaderManager().initLoader(BOOK_ID, null, this);
            mQuantityEditText.setVisibility(View.GONE);
            quantityLinearLayout.setVisibility(View.VISIBLE);
            orderButton.setVisibility(View.VISIBLE);
            mNameEditText.setOnTouchListener(mTouchListener);
            mAuthorEditText.setOnTouchListener(mTouchListener);
            mPriceEditText.setOnTouchListener(mTouchListener);
            minusButton.setOnTouchListener(mTouchListener);
            plusButton.setOnTouchListener(mTouchListener);
            mSupplierNameEditText.setOnTouchListener(mTouchListener);
            mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
        }
    }

    @OnClick(R.id.minus_button)
    public void subtractFromQuantity() {
        if (quantity > 0) {
            quantity--;
            quantityTextView.setText(String.valueOf(quantity));
        }
    }

    @OnClick(R.id.plus_button)
    public void addToQuantity() {
        if (quantity < 100) {
            quantity++;
            quantityTextView.setText(String.valueOf(quantity));
        }
    }

    @OnClick(R.id.order_button)
    public void orderBook() {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + supplierPhoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 100);
        } else {
            startActivity(phoneIntent);
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        String toastMessage;
        int bookDeleted = getContentResolver().delete(bookUri, null, null);
        if (bookDeleted == 0) toastMessage = getString(R.string.failed_delete_message);
        else toastMessage = getString(R.string.deleted_message);
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private void saveBook() {
        String toastMessage;
        String productQuantity;
        String productName = mNameEditText.getText().toString().trim();
        String productAuthor = mAuthorEditText.getText().toString().trim();
        String productPrice = mPriceEditText.getText().toString().trim();
        if (bookUri == null) productQuantity = mQuantityEditText.getText().toString().trim();
        else productQuantity = quantityTextView.getText().toString();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();

        ContentValues values = new ContentValues();

        values.put(BookEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(BookEntry.COLUMN_PRODUCT_AUTHOR, productAuthor);
        values.put(BookEntry.COLUMN_PRODUCT_PRICE, Double.parseDouble(productPrice));
        values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(productQuantity));
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);

        if (bookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) toastMessage = getString(R.string.failed_insert_message);
            else toastMessage = getString(R.string.saved_message);
        } else {
            int rowsUpdated = getContentResolver().update(bookUri, values, null, null);
            if (rowsUpdated == -1) toastMessage = getString(R.string.failed_update_message);
            else toastMessage = getString(R.string.update_message);

        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private boolean isEverythingFilled() {
        String toastMessage = "";
        String phoneNumber = mSupplierPhoneNumberEditText.getText().toString();
        if (TextUtils.isEmpty(mNameEditText.getText().toString()))
            toastMessage = getString(R.string.fill_product_name);
        else if (TextUtils.isEmpty(mAuthorEditText.getText().toString()))
            toastMessage = getString(R.string.fill_product_author);
        else if (TextUtils.isEmpty(mPriceEditText.getText().toString()))
            toastMessage = getString(R.string.fill_product_price);
        else if (bookUri == null && TextUtils.isEmpty(mQuantityEditText.getText().toString()))
            toastMessage = getString(R.string.fill_product_quantity);
        else if (TextUtils.isEmpty(mSupplierNameEditText.getText().toString()))
            toastMessage = getString(R.string.fill_supplier_name);
        else if (TextUtils.isEmpty(phoneNumber))
            toastMessage = getString(R.string.fill_supplier_phone_number);
        else if (phoneNumber.length() < 10 || phoneNumber.length() > 10) {
            Toast.makeText(this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (toastMessage.length() == 0) return true;
        else {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isEverythingFilled()) {
                    saveBook();
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_AUTHOR,
                BookEntry.COLUMN_PRODUCT_PRICE,
                BookEntry.COLUMN_PRODUCT_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(
                this,
                bookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {

            int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int productAuthorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_AUTHOR);
            int productPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String productName = cursor.getString(productNameColumnIndex);
            String productAuthor = cursor.getString(productAuthorColumnIndex);
            String productPrice = String.valueOf(cursor.getDouble(productPriceColumnIndex));
            productQuantity = String.valueOf(cursor.getInt(productQuantityColumnIndex));
            String supplierName = cursor.getString(supplierNameColumnIndex);
            supplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

            mNameEditText.setText(productName);
            mAuthorEditText.setText(productAuthor);
            mPriceEditText.setText(productPrice);
            quantityTextView.setText(productQuantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);

            quantity = Integer.parseInt(productQuantity);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
