package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatNumberShort


typealias OnLikeListener = (post: Post) -> Unit   // Добавляем CallBack
typealias OnShareListener = (post: Post) -> Unit
typealias OnViewListener = (post: Post) -> Unit
typealias OnRemoveListener = (post: Post) -> Unit

interface OnInteractorListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onView(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
}

class PostAdapter(
    private val onInteractorListener: OnInteractorListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractorListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractorListener: OnInteractorListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) = with(binding) {
        textAuthor.text = post.author
        textContent.text = post.content
        textPublished.text = post.published

        // Обновление UI на основе текущего состояния
        imageHeart.setImageResource(
            if (!post.likeByMe) R.drawable.baseline_favorite_24
            else R.drawable.baseline_thumb_up_24
        )
        textCountLikes.text = formatNumberShort(post.likesCount)
        textCountShare.text = formatNumberShort(post.sharesCount)
        textCountLook.text = formatNumberShort(post.looksCount)

        // Обработчики кликов
        // ==================
        imageHeart.setOnClickListener {
            onInteractorListener.onLike(post)  // Вызываем CallBack для лайка
        }

        imageShare.setOnClickListener {
            onInteractorListener.onShare(post)
        }

        imageLook.setOnClickListener {
            onInteractorListener.onView(post)  // Вызываем колбэк просмотра поста
        }

        iconMenu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_options)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractorListener.onRemove(post)
                            true
                        }

                        R.id.edit -> {
                            onInteractorListener.onEdit(post)
                            true
                        }
                        else -> false
                    }

                }

            }.show()
        }
        // ==================

    }
}

object PostDiffCallBack: DiffUtil.ItemCallback<Post>(){
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}