package com.learn2crack.nfc;

import android.app.DialogFragment;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

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
        View view = inflater.inflate(R.layout.fragment_write,container,false);
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
        mListener = (MainActivity)context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onNfcDetected(Ndef ndef, String messageToWrite){
        mProgress.setVisibility(View.VISIBLE);
        writeToNfc(ndef,messageToWrite);
    }

    private void writeToNfc(Ndef ndef, String message){

        mTvMessage.setText(getString(R.string.message_write_progress));
        if (ndef != null) {

            try {
                //Connect to tag
                ndef.connect();

//                int iSize = ndef.getMaxSize();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                String message2 = new String(ndefMessage.getRecords()[0].getPayload());
                String messageTotal;

//                if(iSize) {
                    if (message2 != null) {
                        messageTotal = message + " " + message2 + " ";
                    } else {
                        messageTotal = message + " ";
                    }
//                }

                Log.d(TAG, "readFromNFC: " + messageTotal);

                NdefRecord mimeRecord = NdefRecord.createMime("text/plain", messageTotal.getBytes(Charset.forName("US-ASCII")));

                ndef.writeNdefMessage(new NdefMessage(mimeRecord));
                ndef.close();
                //Write Successful
                mTvMessage.setText(getString(R.string.message_write_success));

            } catch (IOException | FormatException e) {
                e.printStackTrace();
                mTvMessage.setText(getString(R.string.message_write_error));

            } finally {
                mProgress.setVisibility(View.GONE);
            }

        }
    }

    private NdefRecord createRecord(String text, String message) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] messageBytes = message.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        int messageLength = message.length();
        byte[] payload    = new byte[1 + langLength + textLength + messageLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
        System.arraycopy(messageBytes,0,payload,1+langLength,messageLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }
}
