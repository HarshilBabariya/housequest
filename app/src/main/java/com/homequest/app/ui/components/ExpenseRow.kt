package com.homequest.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.repository.isCredit
import com.homequest.app.domain.Category
import com.homequest.app.util.Dates
import com.homequest.app.util.money

val CreditGreen = Color(0xFF16A34A)

@Composable
fun ExpenseRow(item: ExpenseWithPayer, category: Category, symbol: String, onClick: () -> Unit, showDate: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(category)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(item.expense.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                buildString {
                    append(category.label())
                    append(" · ")
                    append(item.payer.name)
                    append(" · ")
                    append(stringResource(item.expense.paymentMode.labelRes))
                    if (showDate) { append(" · "); append(Dates.shortLabel(item.expense.epochDay)) }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val credit = item.expense.isCredit
        Text(
            item.expense.amount.money(symbol),
            style = MaterialTheme.typography.titleMedium,
            color = if (credit) CreditGreen else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
