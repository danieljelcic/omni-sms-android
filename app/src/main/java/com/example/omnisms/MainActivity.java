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
import java.util.Objects;

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
                    byte[] binaryToGSM7 = binaryToGSM7(tempBinary);
                    byte[] backToBinary = GSM7ToBinary(binaryToGSM7);

                    String tempBinaryString = "";
                    for(int i = 0; i < tempBinary.length; i ++) {
                        tempBinaryString += byteToBinaryString(tempBinary[i]);
                        tempBinaryString += " | ";
                    }

                    String GSM7String = "";
                    for(int i = 0; i < binaryToGSM7.length; i ++) {
                        GSM7String += byteToBinaryString(binaryToGSM7[i]);
                        GSM7String += " | ";
                    }

                    String backToBinaryString = "";
                    for(int i = 0; i < backToBinary.length; i ++) {
                        backToBinaryString += byteToBinaryString(backToBinary[i]);
                        backToBinaryString += " | ";
                    }

                    Log.d(TAG, "onClick: temp binary: " + tempBinaryString);
                    Log.d(TAG, "onClick: temp binary to GSM7: " + GSM7String);
                    Log.d(TAG, "onClick: back to binary: " + backToBinaryString);

                } else {

                    byte[] binaryToGSM7 = binaryToGSM7(byteArr);
                    byte[] backToBinary = GSM7ToBinary(binaryToGSM7);

                    decodedImage.setImageBitmap(BitmapFactory.decodeByteArray(backToBinary, 0, backToBinary.length));
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

    String byteToBinaryString(byte toConvert) {
        return String.format("%8s", Integer.toBinaryString(toConvert & 0xFF)).replace(' ', '0');
    }

    private String binaryToGSM7String(byte[] byteArr) {
        return "";
    }

    private byte[] binaryToGSM7(byte[] byteArr) {
        Toast.makeText(getApplicationContext(), "Converting binary to GSM7 bytes", Toast.LENGTH_LONG).show();

        Log.d(TAG, "binaryToGSM7: started");
        
        int gsm7ArrSize = (byteArr.length / 7) * 8 + (byteArr.length % 7) + 1;
        byte[] gsm7Arr = new byte[gsm7ArrSize];

        for(int i = 0; i < byteArr.length; i++) {
            byte gsm7Byte = 0;
            byte gsm7Byte2 = 0;
            byte first = 0;
            byte second = 0;

            Log.d(TAG, "binaryToGSM7: i = " + i + ", i % 7 = " + (i % 7));
            switch (i % 7) {
                case 0 :
                    first = (byte)(0);
                    second = (byte)((byteArr[i] & (byte)0xFE) >> 1);
                    second = (byte)(second & 0b01111111);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0x1));
                    gsm7Byte2 = (byte)(gsm7Byte2 << 6);
                    break;
                case 1 :
                    first = (byte)((byteArr[i - 1] & (byte)0x1) << 6);
                    second = (byte)((byteArr[i] & (byte)0xFC) >> 2);
                    second = (byte)(second & 0b00111111);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0x3));
                    gsm7Byte2 = (byte)(gsm7Byte2 << 5);
                    break;
                case 2 :
                    first = (byte)((byteArr[i - 1] & (byte)0x3) << 5);
                    second = (byte)((byteArr[i] & (byte)0xF8) >> 3);
                    second = (byte)(second & 0b00011111);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0x7));
                    gsm7Byte2 = (byte)(gsm7Byte2 << 4);
                    break;
                case 3 :
                    first = (byte)((byteArr[i - 1] & (byte)0x7) << 4);
                    second = (byte)((byteArr[i] & (byte)0xF0) >> 4);
                    second = (byte)(second & 0b00001111);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0xF));
                    gsm7Byte2 = (byte)(gsm7Byte2 << 3);
                    break;
                case 4 :
                    first = (byte)((byteArr[i - 1] & (byte)0xF) << 3);
                    second = (byte)((byteArr[i] & (byte)0xE0) >> 5);
                    second = (byte)(second & 0b00000111);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0x1F));
                    gsm7Byte2 = (byte)(gsm7Byte2 << 2);
                    break;
                case 5 :
                    first = (byte)((byteArr[i - 1] & (byte)0x1F) << 2);
                    second = (byte)((byteArr[i] & (byte)0xC0) >> 6);
                    second = (byte)(second & 0b00000011);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0x3F));
                    gsm7Byte2 = (byte)(gsm7Byte2 << 1);
                    break;
                case 6 :
                    first = (byte)((byteArr[i - 1] & (byte)0x3F) << 1);
                    second = (byte)((byteArr[i] & (byte)0x80) >> 7);
                    second = (byte)(second & 0b00000001);
                    gsm7Byte = (byte)(first | second);
                    gsm7Byte2 = (byte)((byteArr[i] & (byte)0x7F));
//                    gsm7Byte2 = (byte)(gsm7Byte2 << 0);
                    break;
                }

                int gsm7ArrIndex = (i / 7) * 8 + (i % 7);
                gsm7Arr[gsm7ArrIndex] = gsm7Byte;
                gsm7Arr[gsm7ArrIndex + 1] = gsm7Byte2;
            }

        Log.d(TAG, "binaryToGSM7: returning");
        
            return gsm7Arr;
        }

    private byte[] GSM7ToBinary(byte[] gsm7Arr) {

        Toast.makeText(getApplicationContext(), "Converting GSM7 bytes to binary", Toast.LENGTH_LONG).show();

        int byteArrSize = (gsm7Arr.length / 8) * 7 + (gsm7Arr.length % 8) - 1;
        byte[] byteArr = new byte[byteArrSize];

        for(int i = 0; i < byteArr.length; i++) {

            int gsm7ArrIndex = (i / 7) * 8 + (i % 7);
            byte firstGsm7 = (byte)(gsm7Arr[gsm7ArrIndex]);
            byte secondGsm7 = (byte)(gsm7Arr[gsm7ArrIndex + 1]);

            byte[] masks = {(byte)0b01111111, (byte)0b00111111, (byte)0b00011111, (byte)0b00001111, (byte)0b00000111, (byte)0b00000011, (byte)0b00000001};

            byte first = (byte)(firstGsm7 & masks[i % 7]);
            first = (byte)(first << ((i % 7) + 1));
            byte second = (byte)((secondGsm7 << 1) & ~(masks[i % 7]));
            second = (byte)(second >> (7 - (i % 7)));
            second = (byte)(second & masks[(7 - (i % 7)) - 1]);

            byteArr[i] = (byte)(first | second);
        }

        Log.d(TAG, "GSM7ToBinary: returning");
        
        return byteArr;
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