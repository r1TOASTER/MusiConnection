package com.example.musiconnection;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class Sockets extends AsyncTask<String, Void, String>
{
    private final String IP = "10.100.102.12";
    private final int PORT = 1337;

    private Socket socket;

    private OutputStream output;
    private InputStream input;

    @Override
    protected String doInBackground(String... arrMessages) {
        try {
            this.socket = new Socket(IP, PORT);

            this.output = socket.getOutputStream();
            this.input = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = arrMessages[0];
        String response;

        try {
            // Writes the message for the server
            OutputStreamWriter writer = new OutputStreamWriter(this.output);
            writer.write(Encryption.encrypt(message,3));
            writer.flush();

            // Receive back the message from the server
            ByteArrayOutputStream byteInfo = new ByteArrayOutputStream();

            try {
                int currentByte;
                while ((currentByte = this.input.read()) != -1){
                    byteInfo.write((char) currentByte);
                }
                byte[] allBytesGot = byteInfo.toByteArray();
                response = Encryption.decrypt(new String(allBytesGot, StandardCharsets.UTF_8),3);

                byteInfo.close();
            }
            catch (SocketTimeoutException e){
                byte[] allBytesGot = byteInfo.toByteArray();
                response = Encryption.decrypt(new String(allBytesGot, StandardCharsets.UTF_8), 3);

                byteInfo.close();
            }
            catch (IOException e){
                e.printStackTrace();
                response = "Failed";
            }

        } catch (IOException e) {
            e.printStackTrace();
            response = "Failed";
        }

        return response;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
