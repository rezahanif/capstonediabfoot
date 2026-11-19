package com.project.insole.features.notifications.data

import com.project.insole.core.database.dao.NotificationDao
import com.project.insole.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationDataSourceTest {

    private val notificationList = mutableListOf<NotificationEntity>()
    private val flow = MutableStateFlow<List<NotificationEntity>>(emptyList())

    private val fakeDao = object : NotificationDao {
        override fun getRecentNotificationsFlow(limit: Int): Flow<List<NotificationEntity>> = flow

        override suspend fun getRecentNotifications(limit: Int): List<NotificationEntity> {
            return notificationList.sortedByDescending { it.timestamp }.take(limit)
        }

        override suspend fun insertNotification(notification: NotificationEntity) {
            notificationList.removeAll { it.id == notification.id }
            notificationList.add(notification)
            flow.value = notificationList.sortedByDescending { it.timestamp }
        }

        override suspend fun markAsRead(id: String) {
            val index = notificationList.indexOfFirst { it.id == id }
            if (index != -1) {
                val old = notificationList[index]
                notificationList[index] = old.copy(read = true)
                flow.value = notificationList.sortedByDescending { it.timestamp }
            }
        }

        override suspend fun deleteOldNotifications(keepLimit: Int) {
            val sorted = notificationList.sortedByDescending { it.timestamp }
            if (sorted.size > keepLimit) {
                val toKeep = sorted.take(keepLimit).map { it.id }.toSet()
                notificationList.retainAll { it.id in toKeep }
                flow.value = notificationList.sortedByDescending { it.timestamp }
            }
        }
    }

    private lateinit var dataSource: NotificationDataSource

    @Before
    fun setUp() {
        notificationList.clear()
        flow.value = emptyList()
        dataSource = NotificationDataSource(fakeDao)
    }

    @Test
    fun `empty database returns honest empty notification list`() = runBlocking {
        val result = dataSource.getNotificationHistory()
        assertTrue(result.isSuccess)
        assertTrue("Empty DB must return empty list (Rule R7: no fake data)", result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `saveNotification inserts real guidance alert and emits via flow`() = runBlocking {
        val saveResult = dataSource.saveNotification(
            promptId = "THERMAL_LEFT",
            title = "Check your left foot",
            message = "Your left foot has been warmer than your right foot for a while.",
            severity = "ATTENTION",
            foot = "LEFT",
            thresholdVersion = "v1"
        )

        assertTrue(saveResult.isSuccess)

        val historyResult = dataSource.getNotificationHistory()
        assertTrue(historyResult.isSuccess)
        val history = historyResult.getOrNull()!!
        assertEquals(1, history.size)
        assertEquals("Check your left foot", history[0].title)
        assertEquals("ATTENTION", history[0].severity)
        assertEquals("LEFT", history[0].foot)
        assertFalse(history[0].read)

        val flowValue = dataSource.getNotificationsFlow().first()
        assertEquals(1, flowValue.size)
        assertEquals("Check your left foot", flowValue[0].title)
    }

    @Test
    fun `markAsRead marks specific notification as read`() = runBlocking {
        dataSource.saveNotification(
            promptId = "PRESSURE_RIGHT",
            title = "Your right foot is taking more pressure",
            message = "Your right foot is working harder than usual right now.",
            severity = "ATTENTION",
            foot = "RIGHT"
        )

        val id = dataSource.getNotificationHistory().getOrNull()!![0].id
        dataSource.markAsRead(id)

        val updated = dataSource.getNotificationHistory().getOrNull()!![0]
        assertTrue(updated.read)
    }

    @Test
    fun `clearOldNotifications trims beyond keepLimit`() = runBlocking {
        for (i in 1..10) {
            fakeDao.insertNotification(
                NotificationEntity(
                    id = "id-$i",
                    promptId = "PROMPT_$i",
                    title = "Title $i",
                    message = "Message $i",
                    severity = "INFO",
                    foot = null,
                    timestamp = 1000L * i
                )
            )
        }

        dataSource.clearOldNotifications(keepLimit = 5)
        val remaining = dataSource.getNotificationHistory().getOrNull()!!
        assertEquals(5, remaining.size)
        assertEquals("id-10", remaining[0].id)
    }
}
