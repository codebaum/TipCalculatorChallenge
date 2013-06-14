package com.rosenbaum.tipcalc;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class TipCalculatorActivity extends Activity implements OnSeekBarChangeListener {

    private double mBillAmount = 0;
    private double mTaxPercentage = 0;
    private double mTipPercentage = .15;
    private double mTipAmount = 0;
    private double mTipAdjustor = 0;
    private double mPreTaxSubtotal = 0;

    private EditText mBillValueText;
    private TextView mTaxPercentageValueText;
    private TextView mPreTaxSubtotalValueText;
    private TextView mTipPercentageValueText;
    private TextView mTipValueText;

    private CheckBox mUseSubtotalCheckBox;

    private SeekBar mTaxPercentageSeekBar;
    private SeekBar mTipPercentageSeekBar;
    private SeekBar mTipAdjustorSeekBar;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        mBillValueText = (EditText) findViewById(R.id.editText1);
        mBillValueText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    if (mBillValueText.getText() == null || mBillValueText.getText().toString().equals("")) {
                        return true;
                    }

                    double billValue = Double.parseDouble(mBillValueText.getText().toString());
                    if (billValue <= 0) {
                        return true;
                    }

                    mBillAmount = billValue;
                    mPreTaxSubtotal = mBillAmount / (1 + mTaxPercentage);
                    setTipAmountWithoutAdjustments();
                    return true;
                }
                return false;
            }
        });

        mTaxPercentageValueText = (TextView) findViewById(R.id.textView3);
        mPreTaxSubtotalValueText = (TextView) findViewById(R.id.textView9);
        mTipPercentageValueText = (TextView) findViewById(R.id.textView5);
        mTipValueText = (TextView) findViewById(R.id.textView7);

        mUseSubtotalCheckBox = (CheckBox) findViewById(R.id.checkBox1);
        mUseSubtotalCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTipAmountWithoutAdjustments();
            }
        });

        mTaxPercentageSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        mTaxPercentageSeekBar.setOnSeekBarChangeListener(this);

        mTipPercentageSeekBar = (SeekBar) findViewById(R.id.seekBar2);
        mTipPercentageSeekBar.setOnSeekBarChangeListener(this);

        mTipAdjustorSeekBar = (SeekBar) findViewById(R.id.seekBar3);
        mTipAdjustorSeekBar.setOnSeekBarChangeListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.tip_calculator, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Return true to display menu
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == mTaxPercentageSeekBar.getId()) {
            progress = (int) (((double) progress) * .3);
            mTaxPercentage = ((double) progress) / 100;
            mTaxPercentageValueText.setText(String.valueOf(progress) + "%");
        } else if (seekBar.getId() == mTipPercentageSeekBar.getId()) {
            progress = (int) (((double) progress) * .15 + 15);
            mTipPercentage = ((double) progress) / 100;
            mTipPercentageValueText.setText(String.valueOf(progress) + "%");
        }

        if (mBillValueText.getText() == null || mBillValueText.getText().toString().equals("")) {
            return;
        }

        double billValue = Double.parseDouble(mBillValueText.getText().toString());
        if (billValue <= 0) {
            return;
        }

        if (seekBar.getId() == mTipAdjustorSeekBar.getId()) {
            mTipAdjustor = ((double) progress) * .1;
            NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
            mTipValueText.setText(fmt.format(mTipAmount + mTipAdjustor));

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "I just tipped" + mTipValueText.getText().toString() + " on a " + mBillValueText.getText().toString() + " bill using Brandon's Tip Calculator!");
            sendIntent.setType("text/plain");
            setShareIntent(sendIntent);

            return;
        }

        mBillAmount = billValue;

        mPreTaxSubtotal = mBillAmount / (1 + mTaxPercentage);

        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
        mPreTaxSubtotalValueText.setText(fmt.format(mPreTaxSubtotal));

        setTipAmountWithoutAdjustments();
    }

    private void setTipAmountWithoutAdjustments() {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);

        mTipAdjustor = 0;
        mTipAdjustorSeekBar.setProgress(0);

        if (mUseSubtotalCheckBox.isChecked()) {
            mTipAmount = mPreTaxSubtotal * mTipPercentage;
        } else {
            mTipAmount = mBillAmount * mTipPercentage;
        }
        mTipValueText.setText(fmt.format(mTipAmount));

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I just tipped " + mTipValueText.getText().toString() + " on a " + fmt.format(mBillAmount) + " bill using Brandon's Tip Calculator!");
        sendIntent.setType("text/plain");
        setShareIntent(sendIntent);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
