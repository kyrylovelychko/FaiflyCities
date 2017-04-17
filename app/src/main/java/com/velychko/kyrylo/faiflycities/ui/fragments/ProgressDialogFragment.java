package com.velychko.kyrylo.faiflycities.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.velychko.kyrylo.faiflycities.R;

public class ProgressDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_progress, null);
        builder.setTitle(R.string.dialog_title_loading_countries)
                .setView(view)
                .setCancelable(false);
        return builder.create();
    }

}
