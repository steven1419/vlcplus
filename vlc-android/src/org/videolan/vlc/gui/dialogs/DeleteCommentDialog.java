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
import org.w3c.dom.Text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;

public class DeleteCommentDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    public final static String TAG = "VLC/DeleteCommentDialog";

    Button mConfrimButton;
    Button mCancelButton;

    HistoryStatisticsFragment mHistoryStatisticsFragment;

    private int position;
    private String inputRecord;
    private String[] inputSplit;
    private String comment;

    public DeleteCommentDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AppCompatDialog dialog = new AppCompatDialog(getActivity(), getTheme());
        dialog.setTitle(R.string.comment_delete);
        return dialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_delete_comment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConfrimButton = view.findViewById(R.id.dialog_delete_ok);
        mCancelButton = view.findViewById(R.id.dialog_delete_cancel);
        mConfrimButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        deleteComment();
        dismiss();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
            deleteComment();
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

    public String getRecord() {
        return inputRecord;
    }

    private void deleteComment() {
        setComment();
        inputRecord = inputSplit[0] + "," + inputSplit[1] + "," + inputSplit[2] + comment;
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
}
