package com.vpn.supervpnfree.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.vpn.supervpnfree.BuildConfig;
import com.vpn.supervpnfree.MainApp;
import com.vpn.supervpnfree.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginDialog extends DialogFragment {

    @BindView(R.id.host_url_ed)
    EditText hostUrlEditText;

    @BindView(R.id.carrier_id_ed)
    EditText carrierIdEditText;

    private LoginConfirmationInterface loginConfirmationInterface;

    public LoginDialog() {
    }

    public static LoginDialog newInstance() {
        LoginDialog frag = new LoginDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_login, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        hostUrlEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof LoginConfirmationInterface) {
            loginConfirmationInterface = (LoginConfirmationInterface) ctx;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginConfirmationInterface = null;
    }

    @OnClick(R.id.login_btn)
    void onLoginBtnClick(View v) {
        String hostUrl = hostUrlEditText.getText().toString();
        String carrierId = carrierIdEditText.getText().toString();
        loginConfirmationInterface.setLoginParams(hostUrl, carrierId);
        loginConfirmationInterface.loginUser();
        dismiss();
    }

    public interface LoginConfirmationInterface {
        void setLoginParams(String hostUrl, String carrierId);

        void loginUser();
    }
}
