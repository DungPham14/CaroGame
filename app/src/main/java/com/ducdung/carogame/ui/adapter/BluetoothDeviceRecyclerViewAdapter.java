package com.ducdung.carogame.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ducdung.carogame.R;
import com.ducdung.carogame.ui.listener.OnClickItemListener;

import java.util.List;

/**
 * Created by framgia on 11/08/2016.
 */
public class BluetoothDeviceRecyclerViewAdapter
    extends RecyclerView.Adapter<BluetoothDeviceRecyclerViewAdapter.BluetoothDeviceViewHolder> {
    private LayoutInflater mLayoutInflater;
    private List<BluetoothDevice> mBluetoothDevices;
    private boolean mIsPairedDevice;
    private OnClickItemListener mOnClickItemListener;

    public BluetoothDeviceRecyclerViewAdapter(Context context,
                                              List<BluetoothDevice> bluetoothDevices,
                                              boolean isPairedDevice,
                                              OnClickItemListener onClickItemListener) {
        mLayoutInflater = LayoutInflater.from(context);
        mBluetoothDevices = bluetoothDevices;
        mIsPairedDevice = isPairedDevice;
        mOnClickItemListener = onClickItemListener;
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BluetoothDeviceViewHolder(mLayoutInflater.inflate(R.layout
            .item_bluetooth_device, parent, false));
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, final int position) {
        BluetoothDevice bluetoothDevice = mBluetoothDevices.get(position);
        holder.mTextViewDeviceName.setText(bluetoothDevice.getName());
        holder.mTextViewDeviceAddress.setText(bluetoothDevice.getAddress());
        holder.mLinearLayoutDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnClickItemListener.onClickItem(view, position, mIsPairedDevice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBluetoothDevices == null ? 0 : mBluetoothDevices.size();
    }

    public static class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewDeviceName;
        private TextView mTextViewDeviceAddress;
        private LinearLayout mLinearLayoutDevice;

        public BluetoothDeviceViewHolder(View itemView) {
            super(itemView);
            mTextViewDeviceName = (TextView) itemView.findViewById(R.id.text_view_device_name);
            mTextViewDeviceAddress = (TextView) itemView.findViewById(R.id
                .text_view_device_address);
            mLinearLayoutDevice = (LinearLayout) itemView.findViewById(R.id
                .linear_layout_item_bluetooth_device);
        }
    }
}
