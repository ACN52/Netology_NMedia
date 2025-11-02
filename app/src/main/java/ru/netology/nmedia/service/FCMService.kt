package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random

// Не забываем Сервис регистрировать в Manifest`е   !!!

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    // Регистрируем канал Сервиса
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Получаем токен устройства
    override fun onNewToken(token: String) {
        println("Наш токен= $token")
        //appAuth.sendPushToken(token)
    }

    // Если в приложение придёт уведомление, у которого поле action не соответствует ни
    // одному значению из enum-класса Action, то
    // В строке Action.valueOf(it) будет выброшено исключение IllegalArgumentException,
    // потому что метод valueOf() пытается найти значение enum с точным совпадением имени.
    // Так как исключение не обрабатывается, приложение может упасть
    // Поэтому добавлю конструкцию "try -> catch"

    override fun onMessageReceived(message: RemoteMessage) {
        message.data[action]?.let { actionValue ->
            try {
                when (Action.valueOf(actionValue)) {
                    Action.LIKE -> handleLike(gson.fromJson(message.data[content], ActionLike::class.java))
                    Action.BIG_POST -> handleBigPost(gson.fromJson(message.data[content], ActionLike::class.java))
                }
            } catch (e: IllegalArgumentException) {
                println("Неизвестный action: $actionValue")
            } catch (e: JsonSyntaxException) {
                println("Ошибка парсинга JSON: ${e.message}")
            }
        }
    }

    // Отправляем Уведомление о том, что пользователь Лайкнул Пост

    private fun handleLike(content: ActionLike) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_24)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    //  Реализуем метод handleBigPost для отображения уведомления

    private fun handleBigPost(content: ActionLike) {
        // Форматируем текст поста (если нужно обрезать или добавить переносы)
        val postText = if (content.postText.length > 100) {
            content.postText.take(100) + "..."  // Обрезаем длинный текст
        } else {
            content.postText
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_24)
            .setContentTitle("${content.userName} опубликовал новый пост")
            .setContentText(postText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(postText))  // Разворачиваемый текст
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }
}

enum class Action {
    LIKE,
    BIG_POST  // Для больших Уведомлений
}

data class ActionLike(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
    val postText: String  // // Для больших Уведомлений
)