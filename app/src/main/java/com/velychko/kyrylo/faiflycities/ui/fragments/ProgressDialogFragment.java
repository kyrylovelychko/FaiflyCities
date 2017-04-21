package com.velychko.kyrylo.faiflycities.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.utils.Constants;

public class ProgressDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(Constants.BUNDLE_PROGRESS_DIALOG_TITLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_progress, null);
        builder.setTitle(title)
                .setView(view);
        return builder.create();
    }

}
