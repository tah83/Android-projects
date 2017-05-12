package com.tah.tcp_blinky;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    ToggleButton tgButton;
    private String ip;
    private int port;

    private Socket socket;
    private boolean connection = false;

    private PrintWriter pwOut;
    private BufferedReader brIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tgButton = (ToggleButton) findViewById(R.id.toggleButton);
        tgButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //handle to text input fields
                EditText mEditTextServerIP = (EditText) findViewById(R.id.editTextIP);
                EditText mEditTextServerPort = (EditText) findViewById(R.id.editTextPort);


                if (isChecked) {
                    String serverIP = mEditTextServerIP.getText().toString();
                    String serverPort = mEditTextServerPort.getText().toString();

                    if(!serverIP.isEmpty() && !serverPort.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Connecting to " + serverIP + " on port " + serverPort, Toast.LENGTH_LONG).show();
                        ip = serverIP;
                        port = Integer.parseInt(serverPort);

                        if(socket == null)//start thread and open socket
                            new Thread(new ClientThread()).start();
                       /* if(connection == true) { // wont be in-sync with the socket thread
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                        }
                        else if (connection == false) {
                            Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
                            tgButton.setChecked(false);
                        }*/
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Enter IP and Port", Toast.LENGTH_LONG).show();
                        tgButton.setChecked(false);
                    }
                } else {
                    if(socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        socket = null;
                        pwOut.close();
                        pwOut = null;
                        connection = false;
                    }
                    Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void onClickOn(View v){
        if(checkConnection())
            pwOut.println("1");

    }
    //
    public void onClickOff(View v){
        if(checkConnection())
            pwOut.println("0");

    }

    private boolean checkConnection(){
        boolean result = false;
        //no socket
        if(socket == null)
            Toast.makeText(getApplicationContext(), "Connect to Server", Toast.LENGTH_LONG).show();
        else{
            //socket exits, but is it connected to anything
            if (socket.isConnected())
                result = true;
            else
                Toast.makeText(getApplicationContext(), "Not Conencted", Toast.LENGTH_LONG).show();
        }
        return result;
    }


    class ClientThread implements Runnable {
        private String mServerMsg;

        @Override
        public void run() {

            connection = false;
            final EditText status = (EditText) findViewById(R.id.textView);

            try{
                //send ip as a string. is ip a correct ip format
                socket = new Socket(ip, port);
                connection = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                    }
                });

                try {
                    //setup print writer, auto flush
                    pwOut = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    //setup input reader
                    brIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    //while socket is conencted
                    // read in messages from server, update status display
                    while(connection){
                        mServerMsg = brIn.readLine();

                        if(mServerMsg != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    status.setText(mServerMsg);
                                }
                            });

                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            //hack for now
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
                    tgButton.setChecked(false);
                }
            });
        }

    }
}
