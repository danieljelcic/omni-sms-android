package com.example.omnisms;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class OmniSMSClient {

    private String serviceID;
    private onOmniSMSListener listener;

    private OmniSMSClient(String serviceID) {
        this.serviceID = serviceID;
    }

    public static OmniSMSClient getInstance(String serviceID) /* throws OmniSMSNotAvailable, OmniSMSServiceNotRegistered */{

        OmniSMSClient client = new OmniSMSClient(serviceID);
        client.setOnResponseListener(new onOmniSMSListener() {
            @Override
            public void onOmniSMS(byte[] data, int routeID, onOmniSMSProgressListener onProgress) {
                Log.d(TAG, "onOmniSMS: new reponse!");
            }
        });

        // set up the cursor listener to the content provider

        return client;
    }

    public void registerService (int serviceID, String authToken) /* throws OmniSMSNotAvailable */ {

    }

    public void sendFromBinary(byte[] data, int routeID, onOmniSMSProgressListener onProgress) throws InterruptedException {

            byte[] gsm7Arr = Gsm7Codec.binaryToGSM7(data);
            ArrayList<String> omniSMSSeq = getOmniSMSSeqFromGSM7Arr(gsm7Arr, routeID);

            SmsManager smsManager = SmsManager.getDefault();
            String to = "+" + this.serviceID;

            for (int i = 0; i < omniSMSSeq.size(); i++) {
                // send message

                String body = omniSMSSeq.get(i);

                TimeUnit.SECONDS.sleep(3);

                smsManager.sendTextMessage(to, null, body, null, null);

                Log.d(TAG, "sendFromBinary: sent message [" + i + "]: " + body);

                // call progress listener
            }
    }

    public void sendFromBinary(byte[] data, int routeID) throws InterruptedException {
        sendFromBinary(data, routeID, null);
    }


    public void setOnResponseListener(onOmniSMSListener listener) {
        this.listener = listener;
    }

    ArrayList<Byte[]> getUnretrievedResponses(int routeID) {
        ArrayList<Byte[]> responses = new ArrayList<Byte[]>();
        return responses;
    }

    ArrayList<Byte[]> getAllResponses(int routeID) {
        ArrayList<Byte[]> responses = new ArrayList<Byte[]>();
        return responses;
    }


    public static ArrayList<String> getOmniSMSSeqFromGSM7Arr(byte[] gsm7Arr, int routeID) {

        assert routeID < 125;

        ArrayList<String> omniSMSSeq = new ArrayList<String>();

        int seqLength = 0;
        int currOffset = 0;

        while (currOffset < gsm7Arr.length) {

            if (currOffset == 0) {

                byte[] currGsm7Arr = Gsm7Codec.getNextOfGSM7Size(gsm7Arr, currOffset, 156);
                currOffset += currGsm7Arr.length;
                String currGsm7String = "@@@";

                currGsm7String += Gsm7Codec.GSM7LENCHARS[routeID];

                for (int i = 0; i < currGsm7Arr.length; i++) {

                    currGsm7String += Gsm7Codec.GSM7CHARS[currGsm7Arr[i]];

                }

                omniSMSSeq.add(currGsm7String);

            } else {

                byte[] currGsm7Arr = Gsm7Codec.getNextOfGSM7Size(gsm7Arr, currOffset, 158);
                currOffset += currGsm7Arr.length;
                String currGsm7String = "";
                currGsm7String += Gsm7Codec.GSM7LENCHARS[(seqLength - 1) / 125];
                currGsm7String += Gsm7Codec.GSM7LENCHARS[(seqLength - 1) % 125];

                for (int i = 0; i < currGsm7Arr.length; i++) {

                    currGsm7String += Gsm7Codec.GSM7CHARS[currGsm7Arr[i]];

                }

                omniSMSSeq.add(currGsm7String);
            }

            seqLength++;
        }

        StringBuilder firstSMS = new StringBuilder(omniSMSSeq.get(0));
        firstSMS.setCharAt(1, Gsm7Codec.GSM7LENCHARS[(seqLength - 1) / 125]);
        firstSMS.setCharAt(2, Gsm7Codec.GSM7LENCHARS[(seqLength - 1) % 125]);

        omniSMSSeq.set(0, firstSMS.toString());


        Log.d(TAG, "onClick: GSM7ToOmniSMSSeq.size(): " + omniSMSSeq.size());
        for (String omniSMSString : omniSMSSeq) {
            Log.d(TAG, "onClick: omniSMSString: " + omniSMSString);
        }

        return omniSMSSeq;
    }

    public static byte[] getGSM7ArrFromOmniSMSSeq(ArrayList<String> omniSMSSeq) {

        List<Character> gsm7CharsAsList = new ArrayList<Character>();

        for (char c : Gsm7Codec.GSM7CHARS) {
            gsm7CharsAsList.add(c);
        }

//        Log.d(TAG, "getGSM7ArrFromOmniSMSSeq: gsm7CharsAsList: " + gsm7CharsAsList.toString());

        int gsm7ArrSize = 0;

        for (int i = 0; i < omniSMSSeq.size(); i++) {

            int paddingSize;
            if (i == 0) {
                paddingSize = 4;
            } else {
                paddingSize = 2;
            }

            gsm7ArrSize += omniSMSSeq.get(i).length() - paddingSize;

        }

        byte[] gsm7Arr = new byte[gsm7ArrSize];
        int gsm7ArrIterator = 0;

        for (int i = 0; i < omniSMSSeq.size(); i++) {

            String currOmniSMSString = omniSMSSeq.get(i);

            int paddingOffset;
            if (i == 0) {
                paddingOffset = 4;

                Log.d(TAG, "getGSM7ArrFromOmniSMSSeq: len septet 1: " + (byte)(gsm7CharsAsList.indexOf(currOmniSMSString.charAt(1))));
                Log.d(TAG, "getGSM7ArrFromOmniSMSSeq: len septet 2: " + (byte)(gsm7CharsAsList.indexOf(currOmniSMSString.charAt(2))));

            } else {
                paddingOffset = 2;
            }

            for (int j = paddingOffset; j < currOmniSMSString.length(); j++) {

                gsm7Arr[gsm7ArrIterator] = (byte)(gsm7CharsAsList.indexOf(currOmniSMSString.charAt(j)));
                gsm7ArrIterator++;
            }

        }

        return gsm7Arr;
    }



    interface onOmniSMSListener {
        void onOmniSMS(byte[] data, int routeID, onOmniSMSProgressListener onProgress);
    }

    interface onOmniSMSProgressListener {
        void onSessionProgress(int progress, int size);
        void onRequestMade(boolean success);
    }
}
