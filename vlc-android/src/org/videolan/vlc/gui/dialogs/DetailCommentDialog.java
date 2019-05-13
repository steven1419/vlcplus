package org.videolan.vlc.gui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.videolan.vlc.R;
import org.videolan.vlc.gui.HistoryStatisticsFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;

public class DetailCommentDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    public final static String TAG = "VLC/DetailCommentDialog";

    Button mConfrimButton;
    EditText mComment;
    EditText mDate;

    private String inputRecord;
    private String[] inputSplit;
    private String comment;
    private long recordAccurateDate;
    private String recordDate;

    public DetailCommentDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AppCompatDialog dialog = new AppCompatDialog(getActivity(), getTheme());
        dialog.setTitle(R.string.comment_detail);
        return dialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_detail_comment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConfrimButton = view.findViewById(R.id.dialog_detail_confirm);
        mConfrimButton.setOnClickListener(this);

        TextInputLayout mCommentLayout = view.findViewById(R.id.dialog_detail_comment);
        mComment = mCommentLayout.getEditText();
        TextInputLayout mDateLayout = view.findViewById(R.id.dialog_detail_date);
        mDate = mDateLayout.getEditText();

        mComment.setText(comment);
        mComment.setFocusable(false);
        mComment.setFocusableInTouchMode(false);
        mDate.setText(recordDate);
        mDate.setFocusable(false);
        mDate.setFocusableInTouchMode(false);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    public void setRecord(String inputRecord, int position) {
        this.inputRecord = inputRecord;
        getOriginComment();
    }

    //bug insertion point
    //normally inputRecord will be "date,videoname,comment,timestamp",
    // but if comment contains ",", then inputSplit[3] will surely contains alphabet, then Long.valueof(inputSplit[3])
    //will throws an exception which alphabet can not transform to long value.
    private void getOriginComment() {
        inputSplit = inputRecord.split(",");
        if (inputSplit.length < 4) {
            comment = " ";
            recordDate = " ";
        } else {
            comment = inputSplit[3];
            recordAccurateDate = Long.valueOf(inputSplit[4]);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            recordDate = simpleDateFormat.format(new Date(recordAccurateDate));
        }
    }
}
