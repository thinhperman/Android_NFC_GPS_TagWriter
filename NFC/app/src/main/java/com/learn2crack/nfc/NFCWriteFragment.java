package com.learn2crack.nfc;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.Timer;
import java.util.TimerTask;

public class NFCWriteFragment extends DialogFragment {

    public static final String TAG = NFCWriteFragment.class.getSimpleName();

    public static NFCWriteFragment newInstance() {
        return new NFCWriteFragment();
    }

    private TextView mTvMessage;
    private ProgressBar mProgress;
    private Listener mListener;

    public int iiSize, iMessageSize;
    public String messageTotal, message2 = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mTvMessage = view.findViewById(R.id.tv_message);
        mProgress = view.findViewById(R.id.progress);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mListener = (MainActivity) context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onNfcDetected(Ndef ndef, NdefFormatable format, String messageToWrite) {
        mProgress.setVisibility(View.VISIBLE);
        writeToNfc(ndef, format, messageToWrite);
    }

    @SuppressLint("SetTextI18n")
    private void writeToNfc(Ndef ndef, NdefFormatable format, String message) {
        mTvMessage.setText(getString(R.string.message_write_progress));
        if (ndef != null) {

            ndef.getTag();

            try {
                ndef.connect();

                if (!ndef.isWritable()) {
                    showToast("Tag is read-only!");
                    dismiss();
                }

                NdefMessage ndefMessage = ndef.getNdefMessage();

                try {
                    message2 = new String(ndefMessage.getRecords()[0].getPayload());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                messageTotal = message2 + " " + message + " ";

                Log.d(TAG, "readFromNFC: " + messageTotal);

                iiSize = ndef.getMaxSize();
                iMessageSize = messageTotal.length();

                if (iMessageSize < iiSize) {
                    NdefRecord mimeRecord = NdefRecord.createMime("text/plain", messageTotal.getBytes(Charset.forName("US-ASCII")));

                    ndef.writeNdefMessage(new NdefMessage(mimeRecord));
                    ndef.close();
                    //Write Successful
                    mTvMessage.setText(getString(R.string.message_write_success));

                } else {
                    mTvMessage.setText("Out-of-Memory!");
                    boolean isCanMakeReadOnly = ndef.canMakeReadOnly();
                    if (isCanMakeReadOnly) {
                        ndef.makeReadOnly();
                    } else {
                        showToast("Cannot Lock This Tag!");
                    }
                }
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                mTvMessage.setText(getString(R.string.message_write_error));

            } finally {
//                mProgress.setVisibility(View.GONE);
                final Timer tt = new Timer();
                tt.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dismiss();

                        tt.cancel();
                    }
                }, 1000);
            }
        } else {
            if (format != null) {
                try {
                    format.connect();

                    format.format(new NdefMessage(NdefRecord.createApplicationRecord(message)));

                    showToast("Formated tag to NDEF!");
                } catch (FormatException | IOException e) {
                    showToast("Tag doesn't support NDEF!");
                }
            }

        }
    }

    private void showToast(String s) {
        Context context = getActivity();
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }


}
