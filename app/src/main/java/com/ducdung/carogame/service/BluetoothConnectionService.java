package com.ducdung.carogame.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.Constants;
import com.ducdung.carogame.data.model.GameData;
import com.ducdung.carogame.utility.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by framgia on 12/08/2016.
 */
public class BluetoothConnectionService {
    private static final String NAME = "connection_service";
    private static final UUID CARO_BLUETOOTH_UUID =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final int BYTE_ARRAY_SIZE = 1024;
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private final Context mContext;

    public BluetoothConnectionService(Context context, Handler handler) {
        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Constants.STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(Constants.STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device) {
        if (mState == Constants.STATE_CONNECTING && mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(Constants.STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        Message message = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_CONNECTED);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        bundle.putString(Constants.DEVICE_ADDRESS, device.getAddress());
        message.setData(bundle);
        mHandler.sendMessage(message);
        setState(Constants.STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(Constants.STATE_NONE);
    }

    public void write(GameData gameData) {
        ConnectedThread connectedThread;
        synchronized (this) {
            if (mState != Constants.STATE_CONNECTED) return;
            connectedThread = mConnectedThread;
        }
        connectedThread.write(gameData);
    }

    private void connectionError(@StringRes int stringRes) {
        setState(Constants.STATE_LISTEN);
        Message message = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, mContext.getString(stringRes));
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket serverSocket = null;
            try {
                serverSocket =
                    mAdapter.listenUsingRfcommWithServiceRecord(NAME, CARO_BLUETOOTH_UUID);
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
            mServerSocket = serverSocket;
        }

        public void run() {
            BluetoothSocket socket;
            while (mState != Constants.STATE_CONNECTED) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Looper.prepare();
                    ToastUtils.showToast(mContext, R.string.something_error);
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothConnectionService.this) {
                        switch (mState) {
                            case Constants.STATE_LISTEN:
                            case Constants.STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case Constants.STATE_NONE:
                            case Constants.STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    ToastUtils.showToast(mContext, R.string.something_error);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(CARO_BLUETOOTH_UUID);
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
            mSocket = socket;
        }

        public void run() {
            mAdapter.cancelDiscovery();
            try {
                mSocket.connect();
            } catch (IOException e) {
                connectionError(R.string.unable_to_connect_device);
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    ToastUtils.showToast(mContext, R.string.something_error);
                }
                BluetoothConnectionService.this.start();
                return;
            }
            synchronized (BluetoothConnectionService.this) {
                mConnectThread = null;
            }
            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
            mInputStream = inputStream;
            mOutputStream = outputStream;
        }

        public void run() {
            byte[] buffer = new byte[BYTE_ARRAY_SIZE];
            while (true) {
                try {
                    mHandler.obtainMessage(Constants.MESSAGE_READ, mInputStream.read(buffer), -1,
                        buffer).sendToTarget();
                } catch (IOException e) {
                    mHandler.obtainMessage(Constants.MESSAGE_DISCONNECT, -1, -1, buffer)
                        .sendToTarget();
                    connectionError(R.string.device_connection_was_lost);
                    break;
                }
            }
        }

        public void write(GameData gameData) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream =
                    new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(gameData);
                mOutputStream.write(byteArrayOutputStream.toByteArray());
                objectOutputStream.close();
                byteArrayOutputStream.close();
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, gameData).sendToTarget();
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                ToastUtils.showToast(mContext, R.string.something_error);
            }
        }
    }
}