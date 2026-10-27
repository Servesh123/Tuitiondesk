package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StudentRank
import com.example.model.TestScoreItem
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@Composable
fun TestsScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val testScores by viewModel.testScores.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    if (testScores.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(NeutralBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No test records available",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Weekly tests, scorecards, and batch leaderboards will appear once tests are recorded.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    val latestTest = testScores.last()
    var selectedTestIndex by remember { mutableStateOf(testScores.size - 1) }
    val activeTest = testScores.getOrElse(selectedTestIndex) { latestTest }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Latest Score & Rank Hero Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_test_hero_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "WEEKLY TEST REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = activeTest.date,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeTest.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Subject: ${activeTest.subject} • Class 10th CBSE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Grid (Score, Rank, Batch Avg)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Score
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Your Score",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                                )
                                Text(
                                    text = "${activeTest.studentScore}/${activeTest.maxMarks}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                )
                                Text(
                                    text = "${(activeTest.studentScore * 100) / activeTest.maxMarks}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SuccessGreenLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Rank
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Batch Rank",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                                )
                                Text(
                                    text = "#${activeTest.rank}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "of ${activeTest.totalStudents} students",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                                )
                            }
                        }

                        // Batch Average
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Batch Avg",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                                )
                                Text(
                                    text = "${activeTest.batchAverage}/${activeTest.maxMarks}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "High: ${activeTest.highestScore}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Click Share on WhatsApp
                    Button(
                        onClick = { viewModel.onShareTestReportWhatsApp(context, activeTest) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("share_whatsapp_test_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Scorecard on WhatsApp",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = com.example.util.ReportExportHelper.generateTestLeaderboardCsv(activeTest, leaderboard)
                                com.example.util.ReportExportHelper.downloadToDevice(
                                    context,
                                    "Test_${activeTest.testNumber}_Leaderboard.csv",
                                    csv,
                                    "text/csv"
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("download_test_csv_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download CSV", fontSize = 11.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                val csv = com.example.util.ReportExportHelper.generateTestLeaderboardCsv(activeTest, leaderboard)
                                val file = com.example.util.ReportExportHelper.saveContentToFile(
                                    context,
                                    "Test_${activeTest.testNumber}_Leaderboard.csv",
                                    csv
                                )
                                com.example.util.ReportExportHelper.shareFile(context, file, "text/csv", "Share Test Leaderboard CSV")
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("share_test_csv_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share File", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // BAR CHART of Last 6 Tests (Requirement)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("last_6_tests_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Performance in Last 6 Tests",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Weekly score trend and trajectory",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            color = SuccessGreenLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+28% Growth 📈",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Canvas Bar Chart
                    SixTestsBarChart(
                        tests = testScores,
                        selectedIndex = selectedTestIndex,
                        onBarClick = { index -> selectedTestIndex = index },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend & Benchmark
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NavyLight)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Student Score",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GoldSecondary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Batch Average Line",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSecondary)
                        )
                    }
                }
            }
        }

        // Batch Leaderboard Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLASS LEADERBOARD (Top Scores)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${activeTest.title}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Leaderboard Roster
        items(leaderboard, key = { it.rank }) { rankItem ->
            LeaderboardRankRow(rankItem = rankItem)
        }
    }
}

@Composable
fun SixTestsBarChart(
    tests: List<TestScoreItem>,
    selectedIndex: Int,
    onBarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val barProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "bar_anim"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 50f
            val topPadding = 40f
            val chartHeight = canvasHeight - bottomPadding - topPadding

            val count = tests.size
            if (count == 0) return@Canvas

            val slotWidth = canvasWidth / count
            val barWidth = slotWidth * 0.52f
            val maxScore = 50f

            // Draw Batch Average guideline (dashed line around 35 marks)
            val avgY = topPadding + chartHeight * (1f - (35f / maxScore))
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(0f, avgY),
                end = Offset(canvasWidth, avgY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Draw Bars
            tests.forEachIndexed { i, test ->
                val scoreFraction = (test.studentScore / maxScore).coerceIn(0f, 1f)
                val currentBarHeight = chartHeight * scoreFraction * barProgress
                val x = i * slotWidth + (slotWidth - barWidth) / 2
                val y = topPadding + chartHeight - currentBarHeight

                val isSelected = i == selectedIndex
                val barColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF3B82F6)

                // Draw Bar with rounded top
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, currentBarHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Draw Score label on top of bar
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.parseColor("#1E3A8A") else android.graphics.Color.parseColor("#475569")
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText(
                        "${test.studentScore}",
                        x + barWidth / 2,
                        y - 8f,
                        paint
                    )
                }

                // Draw X-axis label (T1, T2, etc.)
                drawContext.canvas.nativeCanvas.apply {
                    val labelPaint = android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.parseColor("#1E3A8A") else android.graphics.Color.parseColor("#94A3B8")
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isSelected
                    }
                    drawText(
                        "T${i + 1}",
                        x + barWidth / 2,
                        canvasHeight - 12f,
                        labelPaint
                    )
                }
            }
        }

        // Invisible touch overlay for clicking bars
        Row(modifier = Modifier.fillMaxSize()) {
            tests.forEachIndexed { i, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onBarClick(i) }
                )
            }
        }
    }
}

@Composable
fun LeaderboardRankRow(
    rankItem: StudentRank,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rankItem.rank <= 3) SurfaceWhite else SurfaceWhite.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rankItem.rank <= 3) 2.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_${rankItem.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank Badge / Trophy
                when (rankItem.rank) {
                    1 -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🥇", fontSize = 16.sp)
                        }
                    }
                    2 -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🥈", fontSize = 16.sp)
                        }
                    }
                    3 -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEDD5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🥉", fontSize = 16.sp)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantSlate),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${rankItem.rank}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = rankItem.studentName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Roll: ${rankItem.rollNo}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Score & Percentage
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${rankItem.score}/${rankItem.maxMarks}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "${rankItem.percentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (rankItem.percentage >= 90) SuccessGreen else GoldSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
