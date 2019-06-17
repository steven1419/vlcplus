package org.videolan.vlc.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.EntryXComparator;

import org.videolan.vlc.R;
import org.videolan.vlc.gui.browser.MediaBrowserFragment;
import org.videolan.vlc.gui.dialogs.DeleteCommentDialog;
import org.videolan.vlc.gui.dialogs.DetailCommentDialog;
import org.videolan.vlc.gui.dialogs.SaveCommentDialog;
import org.videolan.vlc.gui.helpers.HistoryOperator;
import org.videolan.vlc.gui.view.SwipeRefreshLayout;
import org.videolan.vlc.viewmodels.HistoryStatisticsModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HistoryStatisticsFragment extends MediaBrowserFragment<HistoryStatisticsModel> implements SwipeRefreshLayout.OnRefreshListener {

    public final static String TAG = "VLC/StatisticsFragment";

    private Activity mActivity;
    private LineChart chart;
    private OnChartValueSelectedListener chartListener;
    private RecyclerView mRecyclerView;
    private HistoryStatisticsAdapter mHistoryStatisticsAdapter;
    private View mEmptyView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<String> loadRecord;
    private ArrayList<Float> dateRecord;
    private HashMap<Float, Integer> videoTimeIndex;
    private ArrayList<String> videoTimeStamp;
    private ArrayList<String> videoRecord;

    private Integer currentIndex;
    private float dataIndex = -1;

    public HistoryStatisticsFragment() {
        mHistoryStatisticsAdapter = new HistoryStatisticsAdapter();
    }

    @Override
    public String getTitle() {
        return "History Statistics";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//        mode.getMenuInflater().inflate(org.videolan.vlc.R.menu.action_mode_history_statistics, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(org.videolan.vlc.R.layout.history_statistics_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity(), new HistoryStatisticsModel.Factory(requireContext())).get(HistoryStatisticsModel.class);
        mRecyclerView = view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(org.videolan.vlc.R.id.empty);
    }

    @Override
    public void onActivityCreated(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chart = mActivity.findViewById(org.videolan.vlc.R.id.chart);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.getLegend().setEnabled(false);
        initTimeData();
        chart.invalidate();

        chartListener = new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                dataIndex = h.getX();
                selectData();
//                selectData(h.getDataIndex());
//                Toast.makeText(mActivity.getApplicationContext(), "value selected :" + String.valueOf(h.getX()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {
                dataIndex = -1;
                selectData();
            }
        };

        chart.setOnChartValueSelectedListener(chartListener);

//        mHistoryStatisticsAdapter.setData(new HistoryOperator().getTestVideoList());
        loadVideoData();

        final HistoryStatisticsFragment historyStatisticsFragment = this;
        mHistoryStatisticsAdapter.setOnItemClickListener(new HistoryStatisticsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String s = (String) videoRecord.get(currentIndex + position);
                if (s.indexOf("--->") == -1) {
                    mHistoryStatisticsAdapter.getDataList().set(position, s + "---> clicked");
                } else {
                    mHistoryStatisticsAdapter.getDataList().set(position, s.substring(0, s.indexOf("--->")));
                }
                mHistoryStatisticsAdapter.notifyItemChanged(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showPopupMenu(view, position);
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mHistoryStatisticsAdapter);
        mRecyclerView.setNextFocusUpId(org.videolan.vlc.R.id.chart);
        mRecyclerView.setNextFocusDownId(android.R.id.list);
        mRecyclerView.setNextFocusLeftId(android.R.id.list);
        mRecyclerView.setNextFocusRightId(android.R.id.list);
        registerForContextMenu(mRecyclerView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void showPopupMenu(View view, final int position) {
        final HistoryStatisticsFragment historyStatisticsFragment = this;
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        popupMenu.getMenuInflater().inflate(R.menu.history_statistics_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.setComment:
                        Toast.makeText(mActivity, "set comment", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.checkDetail:
                        Toast.makeText(mActivity, "check detail", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.deleteComment:
                        Toast.makeText(mActivity, "delete comment", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    public void updateLoadRecord(String input, int position) {
        loadRecord.set(currentIndex + position, input);
        Toast.makeText(mActivity, "comment:" + loadRecord.get(currentIndex + position), Toast.LENGTH_SHORT).show();
        updateVideoRecord(position);
        selectData();
        mHistoryStatisticsAdapter.notifyItemChanged(position);
    }

    private void initTimeData() {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> timeRecord = new HistoryOperator().getTimeList();
//        ArrayList<String> timeRecord = new HistoryOperator().getTestTimeList();
        ArrayList<Float> durationRecord = new ArrayList<>();
        dateRecord = new ArrayList<>();

        for (int i = 0; i < timeRecord.size(); i++) {
            try {
                dateRecord.add(Float.valueOf(String.valueOf(simpleDateFormat.parse(timeRecord.get(i).split(",")[0]).getTime())));
//                Toast.makeText(mActivity.getApplicationContext(), "parse value :" + dateRecord.get(i), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            float xVal = dateRecord.get(i);
//            Toast.makeText(mActivity.getApplicationContext(), "float value :" + xVal, Toast.LENGTH_SHORT).show();
//            Toast.makeText(mActivity.getApplicationContext(), "float - string - long - string value :" + String.valueOf(Long.valueOf(String.valueOf(xVal))), Toast.LENGTH_SHORT).show();
            float yVal = Float.valueOf(timeRecord.get(i).split(",")[1]) / 60000;
            entries.add(new Entry(xVal, yVal));
            durationRecord.add(yVal);
        }

        Collections.sort(entries, new EntryXComparator());
        Collections.sort(durationRecord);

        XAxis x1 = chart.getXAxis();
        x1.setAvoidFirstLastClipping(true);
        x1.setAxisMinimum(entries.get(0).getX());
        x1.setAxisMaximum(entries.get(entries.size() - 1).getX());

        LineDataSet set = new LineDataSet(entries, "time Record");

        set.setLineWidth(1.5f);
        set.setCircleRadius(3f);

        LineData data = new LineData(set);

        chart.setData(data);
//
//        IAxisValueFormatter formatter = new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis)
//            {
//                int dateValue = (int)value - Integer.valueOf(dateRecord.get(0)) ;
//                if(dateValue >= 0 && dateValue < dateRecord.size())
//                {
//                    return dateRecord.get(dateValue);
//                }
//                else
//                {
//                    return "0";
//                }
//            }
//        };

//        x1.setGranularity(Integer.valueOf(dateRecord.get(0)));
//        x1.setValueFormatter(formatter);
        x1.setDrawLabels(false);
        x1.setDrawGridLines(false);
        x1.setDrawAxisLine(false);
        x1.setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGranularity(durationRecord.get(0));
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setGranularity(durationRecord.get(0));

    }

    @Override
    public void onRefresh() {
//        setTimeData();
//        chart.invalidate();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setFabPlayVisibility(boolean enable) {
        if (mFabPlay != null) mFabPlay.setVisibility(View.GONE);
    }

    private void loadVideoData() {
        if (loadRecord == null) {
            loadRecord = new HistoryOperator().getVideoList();
//            loadRecord = new HistoryOperator().getTestVideoList();
        }
        String lastInsertTime = "";

        if (videoTimeIndex != null) {
            videoTimeIndex = null;
        }
        if (videoTimeStamp != null) {
            videoTimeStamp = null;
        }
        if (videoRecord != null) {
            videoRecord = null;
        }

        videoTimeIndex = new HashMap<>();
        videoTimeStamp = new ArrayList<>();
        videoRecord = new ArrayList<>();

        String[] stringSplit;
        for (int i = 0; i < loadRecord.size(); i++) {
            stringSplit = loadRecord.get(i).split(",");
            videoTimeStamp.add(stringSplit[0]);
            videoRecord.add(stringSplit[1]);
            if (!lastInsertTime.equals(stringSplit[0])) {
                try {
                    videoTimeIndex.put(Float.valueOf(String.valueOf(simpleDateFormat.parse(stringSplit[0]).getTime())), i);
//                    Toast.makeText(mActivity.getApplicationContext(), "hashmap value :" + Float.valueOf(String.valueOf(simpleDateFormat.parse(stringSplit[0]).getTime())), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                videoTimeIndex.put(stringSplit[0], i);
                lastInsertTime = stringSplit[0];
            }
        }
    }

    private void updateVideoRecord(int position) {
//        Toast.makeText(mActivity.getApplicationContext(), "loadRecord:" + loadRecord.get(currentIndex + position), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "updateVideoRecord: loadRecord: " + loadRecord.get(currentIndex + position));
        String[] stringSplit = loadRecord.get(currentIndex + position).split(",");
        videoRecord.set(currentIndex + position, loadRecord.get(currentIndex + position).substring(stringSplit[0].length() + stringSplit[1].length() + 2));
    }

    private void selectData() {
        ArrayList<String> updatedData = new ArrayList<>();
        currentIndex = videoTimeIndex.get(dataIndex);
        if (currentIndex != null) {
            int i = currentIndex;
            while (i < videoTimeStamp.size() && videoTimeStamp.get(i).equals(videoTimeStamp.get(currentIndex))) {
                updatedData.add(videoRecord.get(i));
                i++;
            }
        }
        updateAdapterData(updatedData);
    }

    private void updateAdapterData(ArrayList<String> updatedData) {
        mHistoryStatisticsAdapter.setData(updatedData);
    }
}
