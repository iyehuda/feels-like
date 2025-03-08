package com.iyehuda.feelslike.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.databinding.ItemPostBinding

class PostAdapter : ListAdapter<Post, PostAdapter.PostViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            // Use the view IDs defined in item_post.xml.
            // Adjust these if your actual IDs differ.
            binding.tvUsername.text = post.username
            binding.tvPostWeather.text = "${post.weather}, ${post.temperature}°C"
            binding.tvPostDescription.text = post.description

            // If you have an ImageView for the post image or user profile,
            // you can load them using an image loading library (Glide, Coil, etc.)
            // For example:
            // Glide.with(binding.ivUserProfile.context)
            //      .load(post.profileImageUrl)
            //      .placeholder(R.drawable.ic_profile_placeholder)
            //      .into(binding.ivUserProfile)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
        }
    }
}