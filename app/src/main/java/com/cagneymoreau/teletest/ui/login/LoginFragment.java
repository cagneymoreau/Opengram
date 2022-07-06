package com.cagneymoreau.teletest.ui.login;


import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.R;
import com.cagneymoreau.teletest.data.TelegramController;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This should only be seen on apps first launch to allow acess
 *
 * 1) (for now) you should already have a telegram account to use this app along with a next button
 * 2) a phone number entry point
 * 3) an access code entry point
 * // TODO: 12/22/2021 what if request access code doesnt work
 *
 */

public class LoginFragment extends Fragment {


    private final static String TAG = "login_fragment";

    Client telegramClient;
    TelegramController telegramController;

    View fragment;
    TextView titleTv, descTv;
    EditText inputEditText;
    Button confirmBtton, extraButton;

    PhoneNumberFormattingTextWatcher pwatch;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment = inflater.inflate(R.layout.login_fragment, container, false);
        telegramController = TelegramController.getInstance(((MainActivity) getActivity()));

        telegramClient = telegramController.getTelegramClient();

        telegramController.setLoginFragment(this);

        buildFrag();

        return fragment;
    }


    private void buildFrag() {
        titleTv = fragment.findViewById(R.id.login_title_textview);
        descTv = fragment.findViewById(R.id.login_description_textview);
        inputEditText = fragment.findViewById(R.id.login_editText);
        confirmBtton = fragment.findViewById(R.id.login_button);
        extraButton = fragment.findViewById(R.id.login_extra_button);

        titleTv.setText(R.string.app_intro);
        descTv.setText("");
        inputEditText.setVisibility(View.INVISIBLE);
        confirmBtton.setText(R.string.confirm);
        confirmBtton.setOnClickListener(view -> {
            changeState(telegramController.getAuthorizationState());
        });
        extraButton.setVisibility(View.INVISIBLE);
    }

    public void changeState(TdApi.AuthorizationState auth) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                switch (auth.getConstructor()) {

                    case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                        //input phone number
                        titleTv.setText(R.string.app_intro);
                        descTv.setText(R.string.number_request);
                        inputEditText.setVisibility(View.VISIBLE);
                        pwatch = new PhoneNumberFormattingTextWatcher();
                        inputEditText.addTextChangedListener(pwatch);
                        confirmBtton.setText("Submit phone number");
                        confirmBtton.setOnClickListener(view -> {

                            String phoneNumber = inputEditText.getText().toString();
                            //1 (949) 922-7955
                            phoneNumber = phoneNumber.replace(" ", "");
                            phoneNumber = phoneNumber.replace("(", "");
                            phoneNumber = phoneNumber.replace(")", "");
                            phoneNumber= phoneNumber.replace("-", "");
                            phoneNumber = "+" + phoneNumber;
                            if (phoneNumber.length() != 12 || !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){

                                Toast.makeText(getContext(), "Please enter 1 digit country code, 3 digit area code and 7 digit phone number", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            telegramClient.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), telegramController);
                            confirmBtton.setText("processing");
                        });
                        extraButton.setVisibility(View.INVISIBLE);

                        break;

                    case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                        //input access code
                        titleTv.setText(R.string.app_intro);
                        descTv.setText(R.string.access_code_request);
                        inputEditText.setVisibility(View.VISIBLE);
                        inputEditText.removeTextChangedListener(pwatch);
                        inputEditText.setText("");
                        confirmBtton.setText("Submit Access Code");
                        confirmBtton.setOnClickListener(view -> {

                            String accesscode = inputEditText.getText().toString();

                            telegramController.submitLoginCode(accesscode);


                        });

                        extraButton.setVisibility(View.VISIBLE);
                        extraButton.setText("Re-Enter Phone Number");
                        extraButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                changeState(new TdApi.AuthorizationStateWaitPhoneNumber());
                            }
                        });

                        break;

                    default:
                        //oh shit
                        Log.e(TAG, "changeState: ", null );

                        break;


                }



            }
        });


    }





}
