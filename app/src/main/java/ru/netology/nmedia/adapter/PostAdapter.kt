package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatNumberShort
import ru.netology.nmedia.view.loadCircleCrop
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.view.load


interface OnInteractorListener {
    fun onLike(post: Post)
    //fun onDisLike(post: Post)
    fun onShare(post: Post)
    fun onView(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)   // Для создания нового поста
    fun onUpdate(post: Post) // Для обновления Поста
    fun onContentClicked(post: Post)
}

class PostAdapter(
    private val onInteractorListener: OnInteractorListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallBack) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractorListener)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
         when (val item = getItem(position)) {
             is Post -> (holder as? PostViewHolder)?.bind(item)
             is Ad -> (holder as? AdViewHolder)?.bind(item)
             null -> error("unknown item type")
         }
    }
}

// Для Рекламы
class AdViewHolder(
    private val binding: CardAdBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        // Загружаем Рекламу
        binding.image.load("${BuildConfig.BASE_URL}/media/${ad.image}")
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
        //textVideo.text = post.video

        // Загружаем аватарки
        iconNetology.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")

        // Обновление UI на основе текущего состояния
        imageHeart.apply {
            isChecked = post.likedByMe

        }

        imageHeart.text = formatNumberShort(post.likesCount)
        imageShare.text = formatNumberShort(post.sharesCount)
        imageLook.text = formatNumberShort(post.looksCount)

        iconMenu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

        // Обработчики кликов
        // ==================
        imageHeart.setOnClickListener {
            onInteractorListener.onLike(post)  // Вызываем CallBack для лайка
        }

        imageShare.setOnClickListener {
            onInteractorListener.onShare(post)
        }

        imageLook.setOnClickListener {
            onInteractorListener.onView(post) // Вызываем колбэк просмотра поста

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

//        // Обработка нажатия на ссылку textVideo
//        textVideo.setOnClickListener {
//            if (post.video.isNotEmpty()) {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
//                it.context.startActivity(intent)
//            }
//        }

        // Обработка нажатия на поле textContent
        textContent.setOnClickListener {
            onInteractorListener.onContentClicked(post)
        }

    }
}

object PostDiffCallBack: DiffUtil.ItemCallback<FeedItem>(){
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}