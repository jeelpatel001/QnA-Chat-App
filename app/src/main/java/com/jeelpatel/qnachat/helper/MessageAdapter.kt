package com.jeelpatel.qnachat.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeelpatel.qnachat.R

class MessageAdapter(private val messageList: ArrayList<MessageModal>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class UserMessageViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val userMessage: TextView = itemView.findViewById(R.id.userMessage)
    }

    class BotMessageViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val botMessage: TextView = itemView.findViewById(R.id.botMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == 0) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.user_message, parent, false)
            UserMessageViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.bot_message, parent, false)
            BotMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sender = messageList.get(position).sender
        when(sender){
            "user" -> (holder as UserMessageViewHolder).userMessage.setText(messageList.get(position).message)
            "bot" -> (holder as BotMessageViewHolder).botMessage.setText(messageList.get(position).message)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(messageList.get(position).sender){
            "user" -> 0
            "bot" -> 1
            else ->1
        }
    }
}