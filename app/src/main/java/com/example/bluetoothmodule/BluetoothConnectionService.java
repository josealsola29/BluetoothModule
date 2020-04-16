package com.example.bluetoothmodule;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothService";
    private static final String appName = "MYAPP";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    Context context;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;
    ProgressDialog progressDialog;
    private ConnectedThread connectedThread;

    public BluetoothConnectionService(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up Server using" + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: Error constructor");
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            bluetoothServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket bluetoothSocket = null;
            try {
                // Keep listening until exception occurs or a socket is returned.
                Log.d(TAG, "run: RFCOM server socket start . . .");
                bluetoothSocket = bluetoothServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG, "run: RFCOM server socket error. " + e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if (bluetoothSocket != null) {
                connected(bluetoothSocket, bluetoothDevice);//todo revisar
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Cancelling AcceptThread.");
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close os AcceptThread fail." + e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started");
            bluetoothDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN ConnectThread");
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID" +
                        MY_UUID_INSECURE);
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d(TAG, "ConnectThread: Could not create InsecureRfcommSocket." + e.getMessage());
            }
            bluetoothSocket = tmp;
            //Discovery mode is very memory intensive
            //Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            //Make a connection to the BluetoothSocket

            try {
                //This is a blocking call and only return on a successful connection or an exception
                bluetoothSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                //Close the socket
                try {
                    bluetoothSocket.close();
                    Log.d(TAG, "run: Closed Socket");
                } catch (IOException ex) {
                    Log.e(TAG, "ConnectThread: run: Unable to close connection in socket" + ex.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }
            //will talk about this in the 3rd
            connected(bluetoothSocket, bluetoothDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing client Socket.");
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: close() of bluetoothSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        //Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection
     * Then ConnectThread starts and attempt  to make a connection with the other devices AcceptThread
     */
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");
        progressDialog = ProgressDialog.show(context, "Connecting Bluetooth",
                "Please wait....", true);

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    /**
     * Finally the ConnnectedThread wich is responsible for maintaining the BTConnection, sending
     * the data, and receiving incoming data through input/output streams respectively
     */
    private class ConnectedThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");
            Toast.makeText(context, TAG + " ConnectedThread: Starting.", Toast.LENGTH_SHORT).show();

            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressDialog when connection is established
            try {
                progressDialog.dismiss();

            } catch (Exception e) {
                Log.e(TAG, "Error Progress Dialog: " + e.getMessage());
            }

            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Error " + e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes;
            //Keep listening to the InputStream until an exception occurs
            while (true) {
                //read from the inputstream
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading inputStream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the MainActivity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write, Writting to outputStream: " + text);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputStream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection*/
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    private void connected(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "connected: Starting");
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    private void write(byte[] out) {
        //Create temporary object
        ConnectedThread r;
        //Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write called");
        //Perform the write
        connectedThread.write(out);
    }
}
