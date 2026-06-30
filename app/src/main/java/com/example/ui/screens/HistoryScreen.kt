package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompletedLesson
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    completedLessons: List<CompletedLesson>,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedLessonForDetail by remember { mutableStateOf<CompletedLesson?>(null) }

    val filteredLessons = remember(completedLessons, selectedCategory) {
        if (selectedCategory == null) {
            completedLessons
        } else {
            completedLessons.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("Hemmesi") },
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = selectedCategory == "Sözleýiş Medeniýeti",
                onClick = { selectedCategory = "Sözleýiş Medeniýeti" },
                label = { Text("Sözleýiş") },
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = selectedCategory == "Adamlara Täsir Ýetirmek",
                onClick = { selectedCategory = "Adamlara Täsir Ýetirmek" },
                label = { Text("Täsir") },
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = selectedCategory == "Biznes Strategiýalary",
                onClick = { selectedCategory = "Biznes Strategiýalary" },
                label = { Text("Biznes") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 2. History List
        if (filteredLessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedCategory == null) "Geçilen sapak ýok" else "Bu kategoriýada okalan sapak ýok",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Birinji sapagyňyzy tamamlap, bu ýerde okalan maglumatlaryňyzy gür gözden geçiriň.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLessons) { lesson ->
                    HistoryItemCard(
                        lesson = lesson,
                        onClick = { selectedLessonForDetail = lesson }
                    )
                }
            }
        }
    }

    // 3. Lesson Detail Sheet (Dialog)
    selectedLessonForDetail?.let { lesson ->
        LessonDetailDialog(
            lesson = lesson,
            onDismiss = { selectedLessonForDetail = null }
        )
    }
}

@Composable
fun HistoryItemCard(
    lesson: CompletedLesson,
    onClick: () -> Unit
) {
    val dateString = remember(lesson.timestamp) {
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(lesson.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        when (lesson.category) {
                            "Sözleýiş Medeniýeti" -> Color(0xFF4A90E2).copy(alpha = 0.12f)
                            "Adamlara Täsir Ýetirmek" -> Color(0xFF9B59B6).copy(alpha = 0.12f)
                            else -> Color(0xFF1ABC9C).copy(alpha = 0.12f)
                        },
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (lesson.category) {
                        "Sözleýiş Medeniýeti" -> Icons.Default.Mic
                        "Adamlara Täsir Ýetirmek" -> Icons.Default.Psychology
                        else -> Icons.Default.BusinessCenter
                    },
                    contentDescription = lesson.category,
                    tint = when (lesson.category) {
                        "Sözleýiş Medeniýeti" -> Color(0xFF4A90E2)
                        "Adamlara Täsir Ýetirmek" -> Color(0xFF9B59B6)
                        else -> Color(0xFF1ABC9C)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Score tag
            Box(
                modifier = Modifier
                    .background(
                        if (lesson.quizScore == lesson.quizTotal) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${lesson.quizScore}/${lesson.quizTotal}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (lesson.quizScore == lesson.quizTotal) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailDialog(
    lesson: CompletedLesson,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_close_button")) {
                Text("Ýap")
            }
        },
        title = {
            Column {
                Text(
                    text = lesson.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (lesson.category) {
                        "Sözleýiş Medeniýeti" -> Color(0xFF4A90E2)
                        "Adamlara Täsir Ýetirmek" -> Color(0xFF9B59B6)
                        else -> Color(0xFF1ABC9C)
                    }
                )
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Intro text
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = lesson.introduction,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lesson main text
                Text(
                    text = "Sapak Maglumaty",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lesson.lessonText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Practical assignment review
                Text(
                    text = "Amaly Ýumuş (Maşk)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = lesson.practicalAssignment,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score performance review
                Text(
                    text = "Synag Netijesi",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Finished",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dogry jogaplar: ${lesson.quizScore} sany (jemi ${lesson.quizTotal} soragdan)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
