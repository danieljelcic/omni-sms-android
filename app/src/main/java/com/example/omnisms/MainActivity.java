package com.example.omnisms;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;

/*
* TO-DO
*
* make sure permission is requested when not already given
*
*
* get a byte array after file picking
* convert 7 byte groups into lists of 8 7-bit chars
* convert 7-but chars to GSM-7 chars
* add length encoding
* figure out how to send many SMSs one after the other
*
* record sessions
* resolve sessions
* display transmitted image
* */


public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "omniSMS";

    EditText messageBodyView;
    Button sendMessageBttn;
    TextView binaryFilename;
    Button sendBinaryBttn;
    Button pickBinaryBttn;
    ImageView origImage;
    ImageView decodedImage;
    Uri binaryUri = null;
    byte[] byteArr = new byte[0];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageBodyView = findViewById(R.id.messageEdit);
        sendMessageBttn = findViewById(R.id.messageButton);
        binaryFilename = findViewById(R.id.filenameText);
        sendBinaryBttn = findViewById(R.id.binarySendButton);
        pickBinaryBttn = findViewById(R.id.binaryPickBttn);
        origImage = findViewById(R.id.imageOrig);
        decodedImage = findViewById(R.id.imageDecode);

        sendMessageBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageBody = messageBodyView.getText().toString();
                sendSms(messageBody, "+17014013818");
            }
        });

        sendBinaryBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String messageBody = binaryToSms(null);
//                sendSms(messageBody, "+17014013818");
                if(byteArr.length == 0) {
//                    String binaryToGSM7 = binaryToGSM7String(byteArr);

                    byte[] tempBinary = new byte[9];
                    tempBinary[0] = (byte)0xF3;
                    tempBinary[1] = (byte)0x13;
                    tempBinary[2] = (byte)0xF4;
                    tempBinary[3] = (byte)0x0;
                    tempBinary[4] = (byte)0xC;
                    tempBinary[5] = (byte)0xD;
                    tempBinary[6] = (byte)0b11101001;
                    tempBinary[7] = (byte)0b10101010;
                    tempBinary[8] = (byte)0b00010101;
                    byte[] binaryToGSM7 = Gsm7Codec.binaryToGSM7(tempBinary);
                    byte[] backToBinary = Gsm7Codec.GSM7ToBinary(binaryToGSM7);

                    String tempBinaryString = "";
                    for(int i = 0; i < tempBinary.length; i ++) {
                        tempBinaryString += Gsm7Codec.byteToBinaryString(tempBinary[i]);
                        tempBinaryString += " | ";
                    }

                    String GSM7String = "";
                    for(int i = 0; i < binaryToGSM7.length; i ++) {
                        GSM7String += Gsm7Codec.byteToBinaryString(binaryToGSM7[i]);
                        GSM7String += " | ";
                    }

                    String backToBinaryString = "";
                    for(int i = 0; i < backToBinary.length; i ++) {
                        backToBinaryString += Gsm7Codec.byteToBinaryString(backToBinary[i]);
                        backToBinaryString += " | ";
                    }

                    Log.d(TAG, "onClick: temp binary: " + tempBinaryString);
                    Log.d(TAG, "onClick: temp binary to GSM7: " + GSM7String);
                    Log.d(TAG, "onClick: back to binary: " + backToBinaryString);


                    ArrayList<String> omniSMSSeq = OmniSMSClient.getOmniSMSSeqFromGSM7Arr(binaryToGSM7, 0);
                    Log.d(TAG, "onClick: omniSMS sequence: " + omniSMSSeq.toString());

                    byte[] backToGSM7FromOmniSMS = OmniSMSClient.getGSM7ArrFromOmniSMSSeq(omniSMSSeq);
                    String backToGSM7FromOmniSMSString = "";
                    for(int i = 0; i < binaryToGSM7.length; i ++) {
                        backToGSM7FromOmniSMSString += Gsm7Codec.byteToBinaryString(backToGSM7FromOmniSMS[i]);
                        backToGSM7FromOmniSMSString += " | ";
                    }

                    Log.d(TAG, "onClick: original temp binary to GSM7: " + GSM7String);
                    Log.d(TAG, "onClick: back to GSM7 from omniSMS: " + backToGSM7FromOmniSMSString);

                } else {

//                    byte[] binaryToGSM7 = Gsm7Codec.binaryToGSM7(byteArr);
//                    Log.d(TAG, "onClick: binaryToGSM7.length: " + binaryToGSM7.length);
//                    ArrayList<String> GSM7ToOmniSMSSeq = OmniSMSClient.getOmniSMSSeqFromGSM7Arr(binaryToGSM7, 0);
//                    byte[] backToGSM7 = OmniSMSClient.getGSM7ArrFromOmniSMSSeq(GSM7ToOmniSMSSeq);
//                    byte[] backToBinary = Gsm7Codec.GSM7ToBinary(backToGSM7);
//
//                    decodedImage.setImageBitmap(BitmapFactory.decodeByteArray(backToBinary, 0, backToBinary.length));

                    OmniSMSClient omniClient = OmniSMSClient.getInstance("17014013818");
                    try {
                        omniClient.sendFromBinary(byteArr, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    byte[] gsm7Arr = Gsm7Codec.binaryToGSM7(byteArr);
//                    ArrayList<String> omniSMSSeq = OmniSMSClient.getOmniSMSSeqFromGSM7Arr(gsm7Arr, 0);
//
////                    SmsManager smsManager = SmsManager.getDefault();
//                    String to = "+" + "17014013818";
//
//                    for (int i = 0; i < 1; i++) {
//                        // send message
//
//                        String body = omniSMSSeq.get(i);
//
////                        smsManager.sendTextMessage(to, null, "Fuck this shit", null, null);
//
////                        sendSms(body, "+17014013818");
//
//
//                        SmsManager smsManager = SmsManager.getDefault();
//                        smsManager.sendTextMessage("+17014013818", null, body, null, null);
//
//                        Log.d(TAG, "sendFromBinary: sent message [" + i + "]: " + body);
//
//                        // call progress listener
//                    }


//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage("+17014013818", null, "Message X", null, null);
//                    sendSms("Message X", "+17014013818");

//                    ArrayList<String> omniSMSSeq = OmniSMSClient.getOmniSMSSeqFromGSM7Arr(binaryToGSM7, 0);
//                    Log.d(TAG, "onClick: omniSMS sequence: " + omniSMSSeq.toString());

                }
            }
        });

        pickBinaryBttn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                binaryUri = resultData.getData();
                Log.i(TAG, "Uri: " + binaryUri.toString());

                binaryFilename.setText(getFilenameFromUri(binaryUri));

                String binary = null;
                try {
                    binary = readTextFromUri(binaryUri);
//                    Log.d(TAG, "onClick: binary stream: " + binary);

                    byteArr = getByteArrFromUri(binaryUri);

                    origImage.setImageBitmap(BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] getByteArrFromUri(Uri binaryUri) throws IOException {

        InputStream iStream = getContentResolver().openInputStream(binaryUri);
        byte[] data = getBytes(iStream, getFileSizeFromUri(binaryUri));

        return data;
    }

    private byte[] getBytes(InputStream inputStream, int bufferSize) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    public int getFileSizeFromUri(Uri uri) {
        Cursor cursor = this.getContentResolver()
                .query(uri, null, null, null, null, null);

        int size = 0;

        try {
            if (cursor != null && cursor.moveToFirst()) {

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = Integer.parseInt(cursor.getString(sizeIndex));
                } else {
                    size = 0;
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            cursor.close();
        }

        return size;
    }

    public String getFilenameFromUri(Uri uri) {
        Cursor cursor = this.getContentResolver()
                .query(uri, null, null, null, null, null);

        String displayName = "Unknown";

        try {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);
            }
        } finally {
            cursor.close();
        }

        return displayName;
    }

    private String binaryToSms(String binary) {

        if(binary == null) {
            binary = "some binary";
        }



        return binary;

    }

    public void sendSms(String body, String to) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(to, null, body, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
    }
}