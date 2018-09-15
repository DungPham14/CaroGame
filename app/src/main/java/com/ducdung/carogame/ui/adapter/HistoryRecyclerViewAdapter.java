package com.ducdung.carogame.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.model.History;
import com.ducdung.carogame.ui.listener.OnClickItemHistoryListener;

import java.util.List;

/**
 * Created by framgia on 31/08/2016.
 */
public class HistoryRecyclerViewAdapter
    extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {
    private List<History> mHistoryList;
    private OnClickItemHistoryListener mOnClickItemHistoryListener;

    public HistoryRecyclerViewAdapter(List<History> historyList,
                                      OnClickItemHistoryListener onClickItemHistoryListener) {
        mHistoryList = historyList;
        mOnClickItemHistoryListener = onClickItemHistoryListener;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(
            R.layout.item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        final History history = mHistoryList.get(position);
        holder.mTextViewId.setText("" + history.getId());
        holder.mTextViewHumanStatus.setText(history.getHumanStatus());
        holder.mTextViewComputerStatus.setText(history.getComputerStatus());
        holder.mTextViewLevel.setText(history.getLevel());
        holder.mTextViewTimeGame.setText(history.getTimeGame());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickItemHistoryListener != null) {
                    mOnClickItemHistoryListener.onClickItem(holder, history.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mHistoryList == null ? 0 : mHistoryList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewId;
        private TextView mTextViewHumanStatus;
        private TextView mTextViewComputerStatus;
        private TextView mTextViewLevel;
        private TextView mTextViewTimeGame;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            mTextViewId = (TextView) itemView.findViewById(R.id.text_history_index);
            mTextViewHumanStatus = (TextView) itemView.findViewById(R.id.text_history_human_status);
            mTextViewComputerStatus = (TextView) itemView.findViewById(
                R.id.text_history_computer_status);
            mTextViewLevel = (TextView) itemView.findViewById(R.id.text_history_level);
            mTextViewTimeGame = (TextView) itemView.findViewById(R.id.text_history_time_game);
        }
    }
}
