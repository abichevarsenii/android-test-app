package com.example.database

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.database.databinding.MyItemBinding

class UserAdapter(
    private val list: List<MyItem>,
    private val onClick: (MyItem) -> Unit,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    class UserViewHolder(var binding: MyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: MyItem) {
            binding.name.text = user.name
            binding.description.text = user.description.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val holder = UserViewHolder(
            MyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        holder.binding.deleteButton.setOnClickListener {
            onClick(list[holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount(): Int {
        return list.size
    }
}