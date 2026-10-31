package com.homequest.app.data.repository

import com.homequest.app.data.local.ReminderDao
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.notifications.ReminderScheduler
import com.homequest.app.util.titleCased
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler,
    private val badges: BadgeTracker
) {
    val reminders: Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun byId(id: Long): ReminderEntity? = dao.byId(id)

    suspend fun save(reminder: ReminderEntity) {
        val titled = reminder.copy(title = reminder.title.titleCased())
        val saved = if (titled.id == 0L) titled.copy(id = dao.insert(titled)) else { dao.update(titled); titled }
        scheduler.schedule(saved)
        badges.refresh()
    }

    suspend fun rescheduleAll() = dao.allEnabled().forEach { scheduler.schedule(it) }

    suspend fun setEnabled(reminder: ReminderEntity, enabled: Boolean) = save(reminder.copy(enabled = enabled))

    suspend fun delete(reminder: ReminderEntity) {
        dao.delete(reminder)
        scheduler.cancel(reminder.id)
    }
}
