package com.example.musiconnection;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

// This class is responsible for creating a connection between the Client side (this) and the Server side using sockets.
public class Sockets extends AsyncTask<String, Void, String>
{
    private final String IP = "172.19.14.150";
    private final int PORT = 5556;
    private final int TIME_OUT = 10500;

    private Socket socket;

    private OutputStream output;
    private InputStream input;

    // A service for connecting a socket and sending the message encrypted to the server, returning the response decrypted, and all of that in the background 
    @Override
    protected String doInBackground(String... arrMessages) {
        try {
            this.socket = new Socket();
            SocketAddress end_point = new InetSocketAddress(IP, PORT);
            this.socket.connect(end_point, TIME_OUT);
            this.output = socket.getOutputStream();
            this.input = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", "Error while trying to access the server");
            return "Failed";
        }

        String message = arrMessages[0];
        String response;

        try {
            // Writes the message for the server
            OutputStreamWriter writer = new OutputStreamWriter(this.output);
            // encrypting the message
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
                // decrypting the message
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
                return "Failed";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Failed";
        }

        return response;
    }

    // when the connection is finished, close the socket
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
