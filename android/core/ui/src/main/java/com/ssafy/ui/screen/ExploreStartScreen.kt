package com.ssafy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ssafy.ui.R
import com.ssafy.ui.component.ChallengeDialog
import com.ssafy.ui.component.CustomButtonWithImage
import com.ssafy.ui.component.ExploreDialog
import com.ssafy.ui.theme.LightBackgroundColor
import com.ssafy.ui.theme.PrimaryColor
import com.ssafy.ui.theme.SecondarySurfaceColor

//TODO 추후에 카메라 화면으로 이동하는 람다식 받아야함
@Composable
fun ExploreStartScreen(onExitExplore: () -> Unit = {}, onEnterEncyc: () -> Unit = {}) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showChallengeDialog by remember { mutableStateOf(false) }

    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(Color.Gray)
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CustomButtonWithImage(
                            text = "탐험 끝내기",
                            onClick = { showExitDialog = true },
                            buttonColor = PrimaryColor
                        )
                        CustomButtonWithImage(
                            text = "도감",
                            onClick = onEnterEncyc,
                            textColor = SecondarySurfaceColor,
                            imagePainter = R.drawable.auto_stories
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomButtonWithImage(
                        text = "도전과제", onClick = { showChallengeDialog = true },
                        textColor = SecondarySurfaceColor,
                        imagePainter = R.drawable.assignment_late
                    )
                }
                IconButton(
                    onClick = { /* TODO 카메라 AR화면으로 이동*/ },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryColor)
                        .size(52.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_center_focus_weak_24),
                        contentDescription = "Search",
                        tint = LightBackgroundColor
                    )
                }
            }
        }
    )

    if (showExitDialog) {
        Dialog(onDismissRequest = { showExitDialog = false }) {
            ExploreDialog(
                title = "탐험을 끝마칠까요?",
                onConfirm = {
                    showExitDialog = false
                    onExitExplore()
                },
                onDismiss = {
                    showExitDialog = false
                }
            )
        }
    }

    if (showChallengeDialog) {
        Dialog(onDismissRequest = { showChallengeDialog = false }) {
            ChallengeDialog(
                //TODO 추후에 카메라 화면으로 이동하는 람다식 받아서 onConfirm에 넣어주기
                onConfirm = { showChallengeDialog = false },
                onDismiss = { showChallengeDialog = false })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreStartScreenPreview() {
    ExploreStartScreen()
}