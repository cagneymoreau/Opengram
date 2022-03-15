package com.cagneymoreau.teletest;

import org.drinkless.td.libcore.telegram.TdApi;

public interface DialogSender {

    void setvalue(int i, TdApi.Chat c, int pos);

}
