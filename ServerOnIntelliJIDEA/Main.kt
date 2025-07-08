package ru.netology.pusher

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

// Данные берем из Firebase.
// Файл "fcm.json" генерируем: Project Settings -> Service accounts
// Просто переименовали его в "fcm.json"

// Серверная часть Кастомизированная
fun main() {
    // Инициализация Firebase
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream("fcm.json")))
        .build()
    FirebaseApp.initializeApp(options)

    // Форматтер для даты: DD.MM.YYYY H:M:S
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy   HH:mm:ss")

    // Бесконечный цикл с интервалом в 10 сек
    while (true) {
        try {
            sendFcmMessage()
            val currentTime = LocalDateTime.now().format(formatter)
            println("Сообщение отправлено в $currentTime")
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
        }

        // Пауза между отправками (10 секунд)
        Thread.sleep(10.seconds.inWholeMilliseconds)
    }
}

fun sendFcmMessage() {
    val token = "cAYq2Tv1R0qmQmzAcoWupS:APA91bE1LB8EzNyEkywd4mShMNNllBf1U2BnvPm_7sRxvlfB1N096FnTFPbZF5jcV_akSYUztrXU8a1MOJRh4fptbAtrWsJ24ThetDZumExVBudmopaKFAM"

    // Выбираем тип уведомления рандомно
    val isNewPostNotification = Random.nextBoolean()

    val message = if (isNewPostNotification) {
        // Пример данных в формате JSON для BIG_POST
        Message.builder()
            .putData("action", "BIG_POST")
            .putData("content", """{
                "userId": 3,
                "userName": "Андрей",
                "postId": 4,
                "postText": "Привет, это новая Нетология! Когда-то Нетология начиналась
                 с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, 
                 разработке, аналитике и управлению."                
            }""".trimIndent())
            .setToken(token)
            .build()
    } else {
        // Пример данных в формате JSON для LIKE
        Message.builder()
            .putData("action", "LIKE")
            .putData("content", """{
                "userId": 1,
                "userName": "Andrey",
                "postId": 2,
                "postAuthor": "Netology"
            }""".trimIndent())
            .setToken(token)
            .build()
    }

    FirebaseMessaging.getInstance().send(message)
    println("Отправлено уведомление типа: ${if (isNewPostNotification) "BIG_POST" else "LIKE"}")
}


// Серверная часть, как в Лекции

//import com.google.auth.oauth2.GoogleCredentials
//import com.google.firebase.FirebaseApp
//import com.google.firebase.FirebaseOptions
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.Message
//import java.io.FileInputStream
//
//// Данные берем из Firebase.
//// Файл "fcm.json" генерируем: Project Settings -> Service accounts
//// Просто переименовали его в "fcm.json"
//
//fun main() {
//    val options = FirebaseOptions.builder()
//        .setCredentials(GoogleCredentials.fromStream(FileInputStream("fcm.json")))
//        .build()
//
//    FirebaseApp.initializeApp(options)
//
//    val token = "dSiEtxZEQGic19zYwe6eia:APA91bG9vETMfrbB_JlmmIaaXqUs5GIDvvjSQ95e9FTV6n6n9GBVHa6xHOp4H7qiG8xECRrxdY39w6zFywsb9SZFRha_y11cWdIKAo4c3Xp5PsoE17CNecw"
//    val message = Message.builder()
//        .putData("action", "LIKE")
//        .putData("content", """{
//          "userId": 1,
//          "userName": "Andrey",
//          "postId": 2,
//          "postAuthor": "Netology"
//        }""".trimIndent())
//        .setToken(token)
//        .build()
//
//    FirebaseMessaging.getInstance().send(message)
//}

