package com.cagneymoreau.teletest;

import org.drinkless.td.libcore.telegram.TdApi;

/**
 * Allows fragment to create dialog for variety of situations and recieve complex response
 */
public interface DialogSender {

    /**
     *
     * @param obj to what object are we acting ... probably a tdapi object
     * @param pos   which item on the list
     * @param operation what type of action was the user taking
     * @param result sub options within the operations parameters
     */
    void setvalue( Object obj, String operation, int pos, int result);

}
