package com.iyehuda.feelslike.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.databinding.ItemPostBinding
import com.iyehuda.feelslike.ui.utils.ImageUtil

class PostAdapter(
    private val resolveLocation: (Double, Double) -> String,
    private val loadUserProfilePicture: ((String, (String?) -> Unit) -> Unit)? = null,
    private val isProfileView: Boolean = false,
    private val onEditClick: ((Post) -> Unit)? = null
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(resolveLocation, loadUserProfilePicture, binding, isProfileView, onEditClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    class PostViewHolder(
        private val resolveLocation: (Double, Double) -> String,
        private val loadUserProfilePicture: ((String, (String?) -> Unit) -> Unit)?,
        private val binding: ItemPostBinding,
        private val isProfileView: Boolean,
        private val onEditClick: ((Post) -> Unit)? = null
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(post: Post) {
            with(binding) {
                tvUsername.text = post.username
                tvUserLocation.text = resolveLocation(post.latitude, post.longitude)
                tvPostWeather.text = "${post.temperature.toInt()}°C, ${post.weather}"
                tvPostDescription.text = post.description
                tvPostTimestamp.text = formatTimestamp(post.createdAt)

                // Load profile image using userId if available
                if (post.userId.isNotEmpty() && loadUserProfilePicture != null) {
                    loadUserProfilePicture.invoke(post.userId) { profileImageUrl ->
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(ivUserProfile.context)
                                .load(profileImageUrl.toUri())
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(ivUserProfile)
                        } else {
                            ivUserProfile.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    }
                } else {
                    ivUserProfile.setImageResource(R.drawable.ic_profile_placeholder)
                }

                if (!post.imageUrl.isNullOrEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(ivPostImage.context).load(post.imageUrl.toUri())
                        .placeholder(R.drawable.ic_image_placeholder).into(ivPostImage)
                } else {
                    ivPostImage.visibility = View.GONE
                }

                // Show edit icon only in profile view and set click listener
                ivEditPost.visibility = if (isProfileView) View.VISIBLE else View.GONE
                ivEditPost.setOnClickListener {
                    onEditClick?.invoke(post)
                }
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