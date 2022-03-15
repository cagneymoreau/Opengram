package com.cagneymoreau.teletest.ui.chat.recycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.cagneymoreau.teletest.MainActivity;
import com.cagneymoreau.teletest.MessageListCallback;
import com.cagneymoreau.teletest.R;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    SortedList<TdApi.Message> messagelist;

    MainActivity mainActivity;

    MessageListCallback callback;

    public MessageListAdapter(MainActivity a, MessageListCallback callback)
    {
        buildMessageList();
        mainActivity = a;
        this.callback = callback;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.chatmessage_card, parent, false);
        viewHolder = new MessageListViewHolder(v, mainActivity, callback);

        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageListViewHolder message_viewHolder = (MessageListViewHolder) holder;

        message_viewHolder.setMessage(messagelist.get(position));


    }



    @Override
    public int getItemCount() {

      return messagelist.size();
    }


    public void addItems(ArrayList<TdApi.Message> messages)
    {
        messagelist.addAll(messages);

    }

    public void addItem(TdApi.Message messages)
    {
        messagelist.add(messages);

    }


    public  void removeItem(TdApi.Message message)
    {

        messagelist.remove(message);

    }



    private void buildMessageList()
    {
        messagelist = new SortedList<>(TdApi.Message.class, new SortedList.Callback<TdApi.Message>() {
            @Override
            public int compare(TdApi.Message o1, TdApi.Message o2) {

                if (o1.date == o2.date) return 0;

                return o1.date > o2.date ? -1 : 1;
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(TdApi.Message oldItem, TdApi.Message newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(TdApi.Message item1, TdApi.Message item2) {
                return item1.id == item2.id;
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position,count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

}
