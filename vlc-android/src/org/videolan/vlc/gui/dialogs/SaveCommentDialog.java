package org.videolan.vlc.gui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.videolan.medialibrary.Tools;
import org.videolan.medialibrary.media.MediaWrapper;
import org.videolan.medialibrary.media.Playlist;
import org.videolan.vlc.R;
import org.videolan.vlc.gui.HistoryStatisticsFragment;
import org.videolan.vlc.util.WorkersKt;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;

public class SaveCommentDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    public final static String TAG = "VLC/SaveCommentDialog";

    EditText mEditText;
    Button mSaveButton;
    Button mCancelButton;

    HistoryStatisticsFragment mHistoryStatisticsFragment;

    private int position;
    private String inputRecord;
    private String[] inputSplit;
    private String comment;

    public SaveCommentDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AppCompatDialog dialog = new AppCompatDialog(getActivity(), getTheme());
        dialog.setTitle(R.string.comment_save);
        return dialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_comment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSaveButton = view.findViewById(R.id.dialog_comment_save);
        mCancelButton = view.findViewById(R.id.dialog_comment_cancel);
        TextInputLayout mLayout = view.findViewById(R.id.dialog_comment_name);
        mEditText = mLayout.getEditText();
        mSaveButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mEditText.setOnEditorActionListener(this);
        if (comment != null) {
            mEditText.setText(comment);
        } else {
            mEditText.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        saveComment();
        dismiss();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
            saveComment();
        return false;
    }

    public void setHistoryStatisticsFragment(HistoryStatisticsFragment historyStatisticsFragment) {
        mHistoryStatisticsFragment = historyStatisticsFragment;
    }

    public void setRecord(String inputRecord, int position) {
        this.inputRecord = inputRecord;
        this.position = position;
        getOriginComment();
    }

    private void saveComment() {
        String input = mEditText.getText().toString().trim();
        if (input.length() == 0) {
            setComment();
        } else {
            setComment(input);
        }
        inputRecord = inputSplit[0] + "," + inputSplit[1] + "," + inputSplit[2] + comment;
        Log.d(TAG, "saveComment: inputRecord : " + inputRecord);
        mHistoryStatisticsFragment.updateLoadRecord(inputRecord, position);
    }

    private void getOriginComment() {
        inputSplit = inputRecord.split(",");
        if (inputSplit.length < 4) {
            comment = "";
        } else {
            comment = inputSplit[3];
        }
    }

    private void setComment() {
        comment = "";
    }

    private void setComment(String input) {
        comment = "," + input;
        comment += "," + String.valueOf(System.currentTimeMillis());
    }
}
