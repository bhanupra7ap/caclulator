package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                CalculatorScreen(viewModel)
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val isPortrait = screenHeight > screenWidth
        
        if (isPortrait) {
            PortraitCalculator(viewModel)
        } else {
            LandscapeCalculator(viewModel)
        }
    }
}

@Composable
fun PortraitCalculator(viewModel: CalculatorViewModel) {
    val density = LocalDensity.current
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    val navBarHeight = with(density) { navBarHeightPx.toDp() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = navBarHeight + 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        DisplayScreen(
            display = viewModel.display,
            previousDisplay = viewModel.previousDisplay,
            currentOperation = viewModel.currentOperation,
            secondOperand = viewModel.secondOperand,
            modifier = Modifier.weight(1f)
        )

        Column {
            IconBar(
                onDeleteClick = { viewModel.onDelete() },
                onHistoryClick = { viewModel.toggleHistory() }
            )
            Spacer(modifier = Modifier.height(28.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                if (viewModel.isShowingHistory) {
                    HistoryDisplay(
                        history = viewModel.history,
                        onBackClick = { viewModel.closeHistory() }
                    )
                } else {
                    ButtonsGrid(viewModel)
                }
            }
        }
    }
}

@Composable
fun LandscapeCalculator(viewModel: CalculatorViewModel) {
    val density = LocalDensity.current
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    val navBarHeight = with(density) { navBarHeightPx.toDp() }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = navBarHeight),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomEnd
        ) {
            DisplayScreen(
                display = viewModel.display,
                previousDisplay = viewModel.previousDisplay,
                currentOperation = viewModel.currentOperation,
                secondOperand = viewModel.secondOperand,
                isPortrait = false
            )
        }

        Box(modifier = Modifier.weight(1.2f)) {
            ButtonsGrid(viewModel, isPortrait = false)
        }
    }
}

@Composable
fun DisplayScreen(
    display: String,
    previousDisplay: String,
    currentOperation: String,
    secondOperand: String,
    modifier: Modifier = Modifier,
    isPortrait: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (isPortrait) 100.dp else 0.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            if (previousDisplay.isNotEmpty() || currentOperation.isNotEmpty() || secondOperand.isNotEmpty()) {
                Text(
                    text = "$previousDisplay $currentOperation $secondOperand",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF808080),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
            
            Text(
                text = display,
                fontSize = if (isPortrait) 60.sp else 48.sp,
                fontWeight = FontWeight.W100,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

@Composable
fun IconBar(onDeleteClick: () -> Unit, onHistoryClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // History Icon (Clock)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onHistoryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        drawCircle(color = Color.White, style = Stroke(width = 1.5.dp.toPx()))
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = center.copy(y = center.y - 7.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = center.copy(x = center.x + 5.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
            
            // Backspace icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u232B",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color(0xFF262626)
        )
    }
}

@Composable
fun HistoryDisplay(history: List<HistoryItem>, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        // Back Button Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    drawLine(
                        color = Color.White,
                        start = center.copy(x = center.x + 6.dp.toPx()),
                        end = center.copy(x = center.x - 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White,
                        start = center.copy(x = center.x + 6.dp.toPx()),
                        end = center.copy(x = center.x - 2.dp.toPx(), y = center.y - 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White,
                        start = center.copy(x = center.x + 6.dp.toPx()),
                        end = center.copy(x = center.x - 2.dp.toPx(), y = center.y + 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color(0xFF262626)
        )

        // History List
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history yet",
                    fontSize = 18.sp,
                    color = Color(0xFF808080),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(history) { item ->
                    HistoryItemRow(item)
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: HistoryItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = item.expression,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF808080),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        Text(
            text = item.result,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.End
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            thickness = 0.5.dp,
            color = Color(0xFF262626)
        )
    }
}

@Composable
fun ButtonsGrid(
    viewModel: CalculatorViewModel,
    isPortrait: Boolean = true
) {
    val buttonSpacing = 16.dp
    val buttonTextSize = if (isPortrait) 34.sp else 24.sp
    
    val colorDark = Color(0xFF171717)
    val colorLightGrey = Color(0xFF7B7B7B)
    val colorGreen = Color(0xFF27D05D)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        // Row 1
        ButtonRow(
            buttons = listOf(
                CalculatorButtonData("C", colorDark, Color.White) { viewModel.onClear() },
                CalculatorButtonData("()", colorDark, Color.White) { },
                CalculatorButtonData("%", colorDark, Color.White) { viewModel.onPercentage() },
                CalculatorButtonData("÷", colorLightGrey, Color.Black) { viewModel.onOperationClick("/") }
            ),
            spacing = buttonSpacing,
            textSize = buttonTextSize
        )

        // Row 2
        ButtonRow(
            buttons = listOf(
                CalculatorButtonData("7", colorDark, Color.White) { viewModel.onNumberClick("7") },
                CalculatorButtonData("8", colorDark, Color.White) { viewModel.onNumberClick("8") },
                CalculatorButtonData("9", colorDark, Color.White) { viewModel.onNumberClick("9") },
                CalculatorButtonData("×", colorLightGrey, Color.Black) { viewModel.onOperationClick("*") }
            ),
            spacing = buttonSpacing,
            textSize = buttonTextSize
        )

        // Row 3
        ButtonRow(
            buttons = listOf(
                CalculatorButtonData("4", colorDark, Color.White) { viewModel.onNumberClick("4") },
                CalculatorButtonData("5", colorDark, Color.White) { viewModel.onNumberClick("5") },
                CalculatorButtonData("6", colorDark, Color.White) { viewModel.onNumberClick("6") },
                CalculatorButtonData("−", colorLightGrey, Color.Black) { viewModel.onOperationClick("-") }
            ),
            spacing = buttonSpacing,
            textSize = buttonTextSize
        )

        // Row 4
        ButtonRow(
            buttons = listOf(
                CalculatorButtonData("1", colorDark, Color.White) { viewModel.onNumberClick("1") },
                CalculatorButtonData("2", colorDark, Color.White) { viewModel.onNumberClick("2") },
                CalculatorButtonData("3", colorDark, Color.White) { viewModel.onNumberClick("3") },
                CalculatorButtonData("+", colorLightGrey, Color.Black) { viewModel.onOperationClick("+") }
            ),
            spacing = buttonSpacing,
            textSize = buttonTextSize
        )

        // Row 5
        ButtonRow(
            buttons = listOf(
                CalculatorButtonData("+/−", colorDark, Color.White) { viewModel.onPlusMinus() },
                CalculatorButtonData("0", colorDark, Color.White) { viewModel.onNumberClick("0") },
                CalculatorButtonData(".", colorDark, Color.White) { viewModel.onDecimalClick() },
                CalculatorButtonData("=", colorGreen, Color.White) { viewModel.onEqualsClick() }
            ),
            spacing = buttonSpacing,
            textSize = buttonTextSize
        )
    }
}

@Composable
fun ButtonRow(
    buttons: List<CalculatorButtonData>,
    spacing: androidx.compose.ui.unit.Dp,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        buttons.forEach { buttonData ->
            CircleButton(
                label = buttonData.label,
                backgroundColor = buttonData.backgroundColor,
                textColor = buttonData.textColor,
                onClick = buttonData.onClick,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                textSize = textSize
            )
        }
    }
}

@Composable
fun CircleButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textSize: androidx.compose.ui.unit.TextUnit = 32.sp
) {
    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = textSize,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}

data class CalculatorButtonData(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color,
    val onClick: () -> Unit
)

@Suppress("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    CalculatorTheme {
        CalculatorScreen(CalculatorViewModel())
    }
}
