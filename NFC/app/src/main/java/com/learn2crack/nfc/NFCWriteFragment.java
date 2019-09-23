package com.learn2crack.nfc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.charset.Charset;
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
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onNfcDetected(Ndef ndef, String messageToWrite) {
        mProgress.setVisibility(View.VISIBLE);
        writeToNfc(ndef, messageToWrite);
    }

    @SuppressLint("SetTextI18n")
    private void writeToNfc(Ndef ndef, String message) {
        int iiSize, iMessageSize;
        String messageTotal, message2 = "";

        mTvMessage.setText(getString(R.string.message_write_progress));

        if (ndef != null) {

            try {
                ndef.connect();

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
                    ndef.makeReadOnly();
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
        }
    }
}
