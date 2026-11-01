package com.homequest.app.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.homequest.app.data.local.BudgetDao
import com.homequest.app.data.local.CategoryDao
import com.homequest.app.data.local.CategoryEntity
import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.Category
import com.homequest.app.util.titleCased
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val dao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val badges: BadgeTracker
) {
    val categories: Flow<List<Category>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun all(): List<Category> = dao.all().map { it.toDomain() }

    suspend fun ensureDefaults() = dao.insertAll(
        BuiltInCategories.seeds.mapIndexed { i, s -> CategoryEntity(s.id, "", s.iconKey, s.color.toArgb().toLong(), i, builtIn = true) }
    )

    suspend fun add(name: String, iconKey: String, color: Color): String {
        val id = "custom_${UUID.randomUUID()}"
        dao.upsert(CategoryEntity(id, name.titleCased(), iconKey, color.toArgb().toLong(), dao.maxSortOrder() + 1, builtIn = false))
        badges.refresh()
        return id
    }

    suspend fun update(category: Category, name: String, iconKey: String, color: Color) {
        val existing = dao.all().firstOrNull { it.id == category.id } ?: return
        dao.upsert(existing.copy(name = if (existing.builtIn) existing.name else name.titleCased(), icon = iconKey, color = color.toArgb().toLong()))
    }

    suspend fun delete(category: Category) {
        if (category.builtIn) return
        expenseDao.reassignCategory(category.id, BuiltInCategories.OTHER)
        budgetDao.delete(category.id)
        dao.deleteCustom(category.id)
    }

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        customName = name,
        nameRes = if (builtIn) BuiltInCategories.nameRes(id) else null,
        iconKey = icon,
        color = Color(color.toInt()),
        builtIn = builtIn
    )
}

fun List<Category>.byId(): Map<String, Category> = associateBy { it.id }

fun Map<String, Category>.resolve(id: String): Category =
    this[id] ?: this[BuiltInCategories.OTHER] ?: Category(BuiltInCategories.OTHER, "", BuiltInCategories.nameRes(BuiltInCategories.OTHER), "box", Color(0xFF757575), builtIn = true)
