package com.cagneymoreau.teletest;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.ParseException;

//This class is for entering currency amount while keeping the right format as the user enters values
public class NumberTextWatcher implements TextWatcher {
    private final DecimalFormat dfnd;
    private final EditText et;

    public NumberTextWatcher(EditText editText) {
        dfnd = new DecimalFormat("#,##0.00");
        this.et = editText;
    }

    @Override
    public void afterTextChanged(Editable s) {
        et.removeTextChangedListener(this);
        //After all the text editing, if there is a string to validate - format it
        if (s != null && !s.toString().isEmpty()) {
            try {
                //Take the input string and remove all formatting characters
                String v = s.toString().replace(String.valueOf(dfnd.getDecimalFormatSymbols().getGroupingSeparator()), "").replace("$","").replace(String.valueOf(dfnd.getDecimalFormatSymbols().getDecimalSeparator()), "");
                //Pass the altered string to a number
                Number n = dfnd.parse(v);
                //Get the decimal point correct again
                n = n.doubleValue() / 100.0;
                //Reformat the text with currency symbols, grouping places etc.
                et.setText(dfnd.format(n));
                //Add the Dollar symbol ($)
                et.setText("$".concat(et.getText().toString()));
                //Move the editing cursor back to the right place
                et.setSelection(et.getText().length());

            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
            }
        } else //if the input field is empty
        {
            et.setText("$0.00");
        }

        et.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
    }
}