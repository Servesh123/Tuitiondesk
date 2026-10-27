package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.TuitionCentre
import com.example.ui.theme.*
import com.example.viewmodel.TuitionTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionTopBar(
    currentTab: TuitionTab,
    centreInfo: TuitionCentre = TuitionCentre(),
    onPitchGuideClick: () -> Unit = {},
    onExportShareClick: () -> Unit = {},
    onWhatsAppApiClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // TuitionDesk Official App Logo
                Image(
                    painter = painterResource(id = R.drawable.tuition_desk_icon_1792669245331),
                    contentDescription = "TuitionDesk Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .testTag("app_logo_image")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TuitionDesk",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = GoldContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = centreInfo.name.ifBlank { "Vidya Tutorials & Coaching Classes" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            // WhatsApp Cloud API quick action button
            FilledTonalButton(
                onClick = onWhatsAppApiClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = WhatsAppLight,
                    contentColor = WhatsAppDark
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("whatsapp_api_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "WhatsApp Cloud API",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WA API",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Clean overflow menu for remaining tools to prevent horizontal clipping
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("top_bar_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = NavyPrimary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export & Share Reports") },
                        onClick = {
                            showMenu = false
                            onExportShareClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Director Pitch Guide") },
                        onClick = {
                            showMenu = false
                            onPitchGuideClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldSecondary)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Institute Profile & UPI") },
                        onClick = {
                            showMenu = false
                            onProfileClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceWhite
        )
    )
}

@Composable
fun TuitionBottomNavigation(
    currentTab: TuitionTab,
    onTabSelected: (TuitionTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            TuitionTab.DASHBOARD to (Icons.Default.Dashboard to Icons.Outlined.Dashboard),
            TuitionTab.STUDENTS to (Icons.Default.Groups to Icons.Outlined.Groups),
            TuitionTab.ATTENDANCE to (Icons.Default.FactCheck to Icons.Outlined.FactCheck),
            TuitionTab.FEES to (Icons.Default.AccountBalanceWallet to Icons.Outlined.AccountBalanceWallet),
            TuitionTab.SETTINGS to (Icons.Default.Settings to Icons.Outlined.Settings)
        )

        items.forEach { (tab, icons) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyPrimary,
                    selectedTextColor = NavyPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}


@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    )
                }
            }
        }
    }
}
