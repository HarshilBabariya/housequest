package com.homequest.app.data.repository

import com.homequest.app.data.local.ReminderDao
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler
) {
    val reminders: Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun byId(id: Long): ReminderEntity? = dao.byId(id)

    suspend fun save(reminder: ReminderEntity) {
        val saved = if (reminder.id == 0L) reminder.copy(id = dao.insert(reminder)) else { dao.update(reminder); reminder }
        scheduler.schedule(saved)
    }

    suspend fun setEnabled(reminder: ReminderEntity, enabled: Boolean) = save(reminder.copy(enabled = enabled))

    suspend fun delete(reminder: ReminderEntity) {
        dao.delete(reminder)
        scheduler.cancel(reminder.id)
    }
}
