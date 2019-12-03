package com.example.omnisms;

import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE;

import java.util.*;

public class Gsm7Codec {

    protected static char[] GSM7CHARS = {'@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '{', 'Å', 'å', 'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', '}', 'Æ', 'æ', 'ß', 'É', ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§', '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'};
    protected static char[] GSM7LENCHARS = {'£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', 'Å', 'å', 'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', 'Æ', 'æ', 'ß', 'É', ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§', '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'};

    public static String byteToBinaryString(byte toConvert) {
        return String.format("%8s", Integer.toBinaryString(toConvert & 0xFF)).replace(' ', '0');
    }

    public static byte[] binaryToGSM7(byte[] byteArr) {

        Log.d(TAG, "binaryToGSM7: started");

        int gsm7ArrSize = (byteArr.length / 7) * 8 + (byteArr.length % 7) + 1;
        byte[] gsm7Arr = new byte[gsm7ArrSize];

        for(int i = 0; i < byteArr.length; i++) {
            byte gsm7Byte = 0;
            byte gsm7Byte2 = 0;
            byte first = 0;
            byte second = 0;

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
                    break;
            }

            int gsm7ArrIndex = (i / 7) * 8 + (i % 7);
            gsm7Arr[gsm7ArrIndex] = gsm7Byte;
            gsm7Arr[gsm7ArrIndex + 1] = gsm7Byte2;
        }

        Log.d(TAG, "binaryToGSM7: returning");

        return gsm7Arr;
    }

    public static byte[] GSM7ToBinary(byte[] gsm7Arr) {

        Log.d(TAG, "GSM7ToBinary: starting");

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

    //    returns byte sequence starting at offset in gsm7Arr and ending at the final index
    //    that fits into GSM7 char size of gsm7Size (or less if reached end of byte array)
    public static byte[] getNextOfGSM7Size(byte[] gsm7Arr, int offset, int gsm7Size) {

        assert offset < gsm7Arr.length;

//        Log.d(TAG, "getNextOfGSM7Size: offset: " + offset);
//        Log.d(TAG, "getNextOfGSM7Size: gsm7Size: " + gsm7Size);
//        Log.d(TAG, "getNextOfGSM7Size: gsm7Arr.length: " + gsm7Arr.length);

        int currSize = 0;
        int i = 0;

        while ((currSize < gsm7Size) && ((offset + i) < gsm7Arr.length)) {

//            Log.d(TAG, "getNextOfGSM7Size: i: " + i);
//            Log.d(TAG, "getNextOfGSM7Size: currSize: " + currSize);
//            Log.d(TAG, "getNextOfGSM7Size: gsm7Arr[i]: " + gsm7Arr[i]);

            if (gsm7Arr[offset + i] == 0x1B || gsm7Arr[offset + i] == 0x0D) {
                currSize += 2;
            } else {
                currSize++;
            }

            if ((currSize < gsm7Size) && ((offset + i) < gsm7Arr.length)) {
                i++;
            }

        }

        byte[] gsm7ArrToReturn = new byte[i];

        for (int j = 0; j < i; j++) {

            assert gsm7Arr[offset + j] < 0b01111111;

            gsm7ArrToReturn[j] = gsm7Arr[offset + j];
        }

        return gsm7ArrToReturn;
    }

    public static String getGSM7String(byte[] gsm7Arr) {

        String gsm7String = "";

        int length = 0;

        for (int i = 0; i < gsm7Arr.length; i++) {

            gsm7String += GSM7CHARS[gsm7Arr[i]];

            if (gsm7Arr[i] == 0x1B || gsm7Arr[i] == 0x0D) {
                length += 2;
            } else {
                length++;
            }
        }

        return gsm7String;
    }

    public static String binaryToGSM7String(byte[] byteArr) {
        return "";
    }

}
