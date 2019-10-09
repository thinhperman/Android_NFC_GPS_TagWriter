package com.learn2crack.nfc;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Listener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    CountDownTimer count;

    private Location location;
    private GoogleApiClient gac;

    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView mEtMessage;
    private TextView tvLat;
    private TextView tvLng;
    private EditText etTimer;
    public String msToWrite;
    private TextView tvRemainTime;
    private TextView tvNdef;
    private TextView tvTagSize;
    private TextView tvTimestamp;
    private TextView tvIdTag;

    private NFCWriteFragment mNfcWriteFragment;
    private NFCReadFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showToast("Hello!");

        initViews();
        initNFC();
    }

    private void initViews() {
        mEtMessage = findViewById(R.id.tv_message);
        tvLat = findViewById(R.id.tv_lat);
        tvLng = findViewById(R.id.tv_lng);
        tvNdef = findViewById(R.id.tv_ndef);
        tvIdTag = findViewById(R.id.tv_idTag);

        Button mBtWrite = findViewById(R.id.btn_write);
        Button mBtRead = findViewById(R.id.btn_read);
        Button mBtGetPos = findViewById(R.id.getPos);
        Button mBtStart = findViewById(R.id.startApp);
        Button mBtStop = findViewById(R.id.cancelApp);
        Button mBtExit = findViewById(R.id.btn_exit);

        etTimer = findViewById(R.id.etTimer);
        tvRemainTime = findViewById(R.id.tvRemainTime);
        tvTagSize = findViewById(R.id.tv_tag_size);
        tvTimestamp = findViewById(R.id.tv_timestamp);

        mBtGetPos.setOnClickListener(view -> getLocation());
        mBtWrite.setOnClickListener(view -> showWriteFragment());
        mBtRead.setOnClickListener(view -> showReadFragment());
        mBtStart.setOnClickListener(view -> setTimer());
        mBtStop.setOnClickListener(view -> canelTimer());
        mBtExit.setOnClickListener(view -> exitApp());

        setTimer();
    }

    private void exitApp() {
        count.cancel();

        showToast("Goodbye!");
        //Toast.makeText(this, "Goodbye!",Toast.LENGTH_LONG).show();

        final Timer tt = new Timer();
        tt.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();

                tt.cancel();
            }
        }, 1000);
    }

    private void canelTimer() {
        count.cancel();
    }

    private void setTimer() {
        String sTimer;
        long lTimer;

        try {
            sTimer = etTimer.getText().toString();
            lTimer = Long.parseLong(sTimer);

            count = new CountDownTimer(lTimer * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    tvRemainTime.setText("Remain: " + l / 1000 + " s");

                    getLocation();
                }

                @Override
                public void onFinish() {
                    showWriteFragment();
                }
            };
        } catch (NumberFormatException e) {
            showToast(String.valueOf(e));
        }

        count.start();
    }

    private void initNFC() {
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void getLocation() {
        DecimalFormat fotmatLat = new DecimalFormat("##.######");
        DecimalFormat fotmatLng = new DecimalFormat("###.######");

        Long lTimeStamp = System.currentTimeMillis() / 1000;
        String sTimeStamp = lTimeStamp.toString();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Kiểm tra quyền hạn
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        } else {

            location = LocationServices.FusedLocationApi.getLastLocation(gac);
            if (location != null) {
                double dLatitude = location.getLatitude();
                double dLongitude = location.getLongitude();

                String sLatitue = fotmatLat.format(dLatitude);
                String sLongitude = fotmatLng.format(dLongitude);

                // Hiển thị
                tvLat.setText(" " + sLatitue);
                tvLng.setText(" " + sLongitude);
                tvTimestamp.setText(" " + sTimeStamp);

                msToWrite = sTimeStamp + ":" + sLatitue + "-" + sLongitude + "\r\n";

                mEtMessage.setText(" " + "GPS OK");
            } else {
                showToast("Cannot display the position. Are you activate the GPS?");
                //Toast.makeText(this, "Cannot display the position. " + "Are you activate the GPS?", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000).show();
            } else {
                showToast("Device does not support!");
                //Toast.makeText(this, "Device does not support!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void showWriteFragment() {

        isWrite = true;

        mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);

        if (mNfcWriteFragment == null) {

            mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        mNfcWriteFragment.show(getFragmentManager(), NFCWriteFragment.TAG);

    }

    private void showReadFragment() {

        mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getFragmentManager(), NFCReadFragment.TAG);

    }

    @Override
    public void onDialogDisplayed() {

        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {

        isDialogDisplayed = false;
        isWrite = false;
        setTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        String[][] techList = new String[3][1];
        techList[0][0] = "android.nfc.tech.NdefFormatable";
        techList[1][0] = "android.nfc.tech.NfcA";
        techList[2][0] = "android.nfc.tech.Ndef";

        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, techList);
        }

        initNFC();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: " + intent.getAction());

        if (tag != null) {
            //Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();

            tvIdTag.setText(" " + Arrays.toString(tag.getId()));

            Ndef ndef = Ndef.get(tag);

            NdefFormatable format = NdefFormatable.get(tag);

            String sType = String.valueOf(ndef.getType());

            String[] output = sType.split("ndef.");

            int iiSize = ndef.getMaxSize();

            tvNdef.setText(" " + output[1]);
            tvTagSize.setText(" " + iiSize + " " + "bytes");

            getLocation();

            if (isDialogDisplayed) {
                if (isWrite) {
                    getLocation();
                    String messageToWrite = msToWrite;
                    mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    mNfcWriteFragment.onNfcDetected(ndef, format, messageToWrite);
                } else {
                    mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (gac == null) {
            gac = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        gac.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Error: " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    protected void onStart() {
        super.onStart();
        try {
            gac.connect();
        } catch (Exception e) {
            showToast(String.valueOf(e));
        }
    }

    protected void onStop() {
        super.onStop();
        try {
            gac.disconnect();
        } catch (Exception e) {
            showToast(String.valueOf(e));
            //Toast.makeText(this, (CharSequence) e, Toast.LENGTH_LONG).show();
        }
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG);
    }

}
