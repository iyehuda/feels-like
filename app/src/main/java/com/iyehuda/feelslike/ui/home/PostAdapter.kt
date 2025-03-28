package com.iyehuda.feelslike.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iyehuda.feelslike.R
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
            
            // Format and set the timestamp
            binding.tvPostTimestamp.text = formatTimestamp(post.createdAt)

            if (!post.imageUrl.isNullOrEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(binding.ivPostImage.context)
                    .load(post.imageUri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val difference = currentTime - timestamp
            
            // Convert to appropriate time format
            return when {
                difference < 60 * 1000 -> "Just now"
                difference < 60 * 60 * 1000 -> "${difference / (60 * 1000)}m ago"
                difference < 24 * 60 * 60 * 1000 -> "${difference / (60 * 60 * 1000)}h ago"
                difference < 7 * 24 * 60 * 60 * 1000 -> "${difference / (24 * 60 * 60 * 1000)}d ago"
                else -> {
                    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(timestamp))
                }
            }
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