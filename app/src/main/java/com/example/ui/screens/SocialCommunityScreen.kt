package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendAccount
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@Composable
fun SocialCommunityScreen(
  viewModel: FitnessViewModel
) {
  val friends by viewModel.socialFriends.collectAsState()
  val accountabilityBuddies = friends.filter { it.isAccountabilityBuddy }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Community & Accountability",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Mutual Streaks, Fist Bumps & Shared Feed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = FlameOrange.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, FlameOrange)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Whatshot, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "14D STREAK",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = FlameOrange
            )
          }
        }
      }
    }

    // Accountability Buddy Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Handshake, contentDescription = null, tint = VoltGreen, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "ACCOUNTABILITY BUDDY SYSTEM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = VoltGreen
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = VoltGreen.copy(alpha = 0.2f)
            ) {
              Text(
                text = "${accountabilityBuddies.size} Active Buddies",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = VoltGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Holding each other accountable unlocks a 3.4x higher consistency rate over 90 days. When you finish a workout or meal prep, your buddies are alerted instantly!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          accountabilityBuddies.forEach { buddy ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.background,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(VoltGreen.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(buddy.avatarInitials, fontWeight = FontWeight.Bold, color = VoltGreen)
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(buddy.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text("Shared Streak: ${buddy.currentStreakDays} Days 🔥", style = MaterialTheme.typography.labelSmall, color = FlameOrange)
                  }
                }

                Button(
                  onClick = { viewModel.sendFistBump(buddy.id, buddy.name) },
                  colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("fist_bump_buddy_${buddy.id}")
                ) {
                  Text("👊 Bump (${buddy.fistBumpsCount})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }
      }
    }

    // Weekly Community Leaderboard Header
    item {
      Text(
        text = "Weekly Consistency Leaderboard",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Leaderboard Items
    val sortedLeaderboard = friends.sortedByDescending { it.weeklyScorePoints }
    items(sortedLeaderboard.size) { index ->
      val friend = sortedLeaderboard[index]
      val medalEmoji = when (index) {
        0 -> "🥇"
        1 -> "🥈"
        2 -> "🥉"
        else -> "#${index + 1}"
      }

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
          1.dp,
          if (index == 0) VoltGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = medalEmoji,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
              modifier = Modifier.width(32.dp)
            )
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CyberTeal.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(friend.avatarInitials, fontWeight = FontWeight.Bold, color = CyberTeal)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(friend.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
              Text(friend.statusUpdate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "${friend.weeklyScorePoints} XP",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
              color = VoltGreen
            )
            IconButton(
              onClick = { viewModel.sendFistBump(friend.id, friend.name) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.SportsHandball, contentDescription = "Fist Bump", tint = FlameOrange, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  }
}
