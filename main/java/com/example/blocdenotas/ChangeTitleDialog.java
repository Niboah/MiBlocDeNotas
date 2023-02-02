package com.example.blocdenotas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import org.jetbrains.annotations.NotNull;

public class ChangeTitleDialog extends DialogFragment {

    public interface ChangeTitleDialogListener {
        public void onDialogPositiveClick(ChangeTitleDialog dialog);
    }
    ChangeTitleDialogListener listener;

    View view;

    public String oldTitle;

    public void setOldTitle(String newTitle){
        this.oldTitle=newTitle;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ChangeTitleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("must implement NoticeDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.toolbar_edit_dialog,null);
        EditText et = view.findViewById(R.id.newTitleText);
        et.setText(oldTitle);
        et.setSelection(et.getText().length());
        builder.setMessage("New title")
                .setView(view)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ChangeTitleDialog.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view.findViewById(R.id.newTitleText).requestFocus();
    }

}
