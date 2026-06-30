package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProfile = profile ?: UserProfile()
    val scrollState = rememberScrollState()
    var showResetConfirmation by remember { mutableStateOf(false) }

    // Achievements calculation
    val hasFirstStep = activeProfile.completedLessonsCount > 0
    val hasSpeakerExpert = activeProfile.sozleyisProgressCount >= 2
    val hasPersuaderExpert = activeProfile.tasirProgressCount >= 2
    val hasBusinessExpert = activeProfile.biznesProgressCount >= 2
    val hasStreakMaster = activeProfile.streak >= 3

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Profile Avatar Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeProfile.name.take(1).uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = activeProfile.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "AI Okuwçysy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Row Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatColumn(
                        value = "${activeProfile.streak} gün",
                        label = "Zynjyr",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = Color(0xFFFF9800)
                    )
                    ProfileStatColumn(
                        value = "${activeProfile.xp}",
                        label = "Jemi XP",
                        icon = Icons.Default.OfflineBolt,
                        iconColor = Color(0xFFFFC107)
                    )
                    ProfileStatColumn(
                        value = "${activeProfile.completedLessonsCount}",
                        label = "Sapaklar",
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // 2. Achievements & Medals Section
        Text(
            text = "Gazanylan üstünlikler",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AchievementBadgeRow(
                title = "Birinji Ädim",
                description = "Ilkinji sapagyňyzy üstünlikli tamamladyňyz.",
                icon = Icons.Default.MilitaryTech,
                isUnlocked = hasFirstStep,
                color = Color(0xFF4CAF50)
            )

            AchievementBadgeRow(
                title = "Çeper Diler",
                description = "Sözleýiş medeniýetinden azyndan 2 sapak geçdiňiz.",
                icon = Icons.Default.Mic,
                isUnlocked = hasSpeakerExpert,
                color = Color(0xFF4A90E2)
            )

            AchievementBadgeRow(
                title = "Master Psiholog",
                description = "Täsir ýetirmek ugrundan azyndan 2 sapak geçdiňiz.",
                icon = Icons.Default.Psychology,
                isUnlocked = hasPersuaderExpert,
                color = Color(0xFF9B59B6)
            )

            AchievementBadgeRow(
                title = "Biznes Telekeçi",
                description = "Biznes strategiýalaryndan azyndan 2 sapak geçdiňiz.",
                icon = Icons.Default.BusinessCenter,
                isUnlocked = hasBusinessExpert,
                color = Color(0xFF1ABC9C)
            )

            AchievementBadgeRow(
                title = "Kämil Orator",
                description = "Gündelik sapak yzygiderliligini 3 güne ýetirdiňiz.",
                icon = Icons.Default.EmojiEvents,
                isUnlocked = hasStreakMaster,
                color = Color(0xFFFFC107)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Danger Zone Reset Controls
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Amallar & Dolandyryş",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ähli okalan sapaklary, gazanylan XP we zynjyry doly pozmak isleseňiz, aşakdaky düwmä basyň.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!showResetConfirmation) {
                    Button(
                        onClick = { showResetConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("reset_progress_trigger")
                    ) {
                        Text(
                            text = "Maglumatlary doly arassala",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hakykatdan hem ähli maglumatlary pozmak isleýärsiňizmi? Bu amaly yza alyp bolmaz!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showResetConfirmation = false },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ýatyr")
                            }

                            Button(
                                onClick = {
                                    showResetConfirmation = false
                                    onResetProgress()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("confirm_reset_button")
                            ) {
                                Text("Hawa, Poz")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatColumn(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AchievementBadgeRow(
    title: String,
    description: String,
    icon: ImageVector,
    isUnlocked: Boolean,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Illuminated/grayed out medal badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isUnlocked) color.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isUnlocked) color else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            // Lock status icon
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Gulp",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Açyk",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
