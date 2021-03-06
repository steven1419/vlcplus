package org.videolan.vlc.gui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

import org.videolan.vlc.R;

public class HistoryStatisticsAdapter extends RecyclerView.Adapter<HistoryStatisticsAdapter.ViewHolder> {
    public static final String TAG = "VLC/HistoryStatisticsAdapter";

    private static final int TYPE_NORMAL = 1;

    private ArrayList<String> dataList = new ArrayList<>();
    private OnItemClickListener mListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textViewItem);
        }
    }

    public void setData(ArrayList<String> dataList) {
        if (dataList != null) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    public ArrayList<String> getDataList() {
        return dataList;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_NORMAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_statistics_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String[] dataListSplit = dataList.get(position).split(",");
        if (dataListSplit.length < 2) {
            holder.textView.setText(dataList.get(position));
        } else {
            if (dataListSplit[1].length() < 10) {
                holder.textView.setText(dataListSplit[0] + "    --\"" + dataListSplit[1] + "\"");
            } else {
                holder.textView.setText(dataListSplit[0] + "    --\"" + dataListSplit[1].substring(0, 10) + "...\"");
            }
        }
        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mListener) {
        this.mListener = mListener;
    }
}
