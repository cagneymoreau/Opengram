package com.cagneymoreau.teletest.ui.newgroup

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.cagneymoreau.teletest.MainActivity
import com.cagneymoreau.teletest.R
import com.cagneymoreau.teletest.data.Controller
import com.cagneymoreau.teletest.data.TelegramController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

 class Visibility: Fragment() {

    lateinit var fragment: View
    lateinit var controller: Controller
    lateinit var telegramController: TelegramController

    lateinit var checkBox: CheckBox
    lateinit var editText: EditText
    lateinit var textView: TextView
    lateinit var fab: FloatingActionButton


    var channId: Long = 0
    var public = false
    var linkfound = false
    var query: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragment = inflater.inflate(R.layout.visibility_layout, container, false)

        controller = Controller.controller;
        telegramController = TelegramController.getInstance((MainActivity()))

        checkBox = fragment.findViewById(R.id.visibility_publicprivate_checkbox)
        editText = fragment.findViewById(R.id.visibility_link_editext)
        textView = fragment.findViewById(R.id.visibility_linkstatus_textview)
        fab = fragment.findViewById(R.id.visibility_fab)

        retrieveChannelInfo()
        buildUI()

        return fragment
    }

    private fun retrieveChannelInfo()
    {
        channId = controller.channIDHolder
    }


    private fun buildUI()
    {
        checkBox.setOnCheckedChangeListener { compoundButton, b ->

            public = b

            if (b){
                editText.visibility = View.VISIBLE
            }else{
                editText.visibility = View.INVISIBLE
            }

        }


        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length < 5) {
                    textView.text = "Must be 5 characters"
                } else {
                    textView.text = "Checking"
                    checkLinkAgainstServer(charSequence.toString())
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        /** All the nav work here */
        fab.setOnClickListener {

            //private channel
            if (!public){

                //navigate to
                val b = Bundle()
                val id: Long = channId
                b.putLong("chatId", id)
                findNavController(fragment).navigate(R.id.action_global_simpleChat, b)

            }



            if (public && linkfound) {

                //make public then nav to
                val  h = Client.ResultHandler {

                    val b = Bundle()
                    val id: Long = channId
                    b.putLong("chatId", id)
                    findNavController(fragment).navigate(R.id.action_global_simpleChat, b)

                }

                telegramController.makeMyChannelPublic(channId, query, h)




            } else {
                //public without link found
                Toast.makeText(context, "check error", Toast.LENGTH_SHORT).show()
            }

        }


    }


    private fun checkLinkAgainstServer(link: String)
    {
        query = link

        val h = Client.ResultHandler {

            when (it.constructor){

                TdApi.CheckChatUsernameResultOk.CONSTRUCTOR -> {

                    linkfound = true;
                    textView.text = "ok!"

                }
                else -> {

                    linkfound = false
                    textView.text = it.constructor.toString()
                }

            }


        }

        telegramController.checkIfPublicLinkAvailable(channId, link, h)


    }



}