package com.veilsync.app

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.veilsync.app.ui.theme.CalculatorTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.veilsync.app.ui.PasswordSetupScreen
import com.veilsync.app.ui.PasswordVerificationScreen
import com.veilsync.app.ui.HiddenVaultScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var securityManager: SecurityManager
    private lateinit var vaultManager: HiddenVaultManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        securityManager = SecurityManager(this)
        vaultManager = HiddenVaultManager(this)
        viewModel.initializeSharedPreferences(this)
        updateAppAlias(viewModel.currentLayout)
        
        setContent {
            CalculatorTheme {
                MainScreen(
                    viewModel = viewModel,
                    securityManager = securityManager,
                    vaultManager = vaultManager,
                    activity = this,
                    onLayoutChanged = { layout -> updateAppAlias(layout) }
                )
            }
        }
    }
    
    private fun updateAppAlias(layout: AppLayout) {
        try {
            val pm = packageManager
            when (layout) {
                AppLayout.Calculator -> {
                    pm.setComponentEnabledSetting(
                        android.content.ComponentName(this, "com.veilsync.app.CalculatorAppAlias"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        android.content.ComponentName(this, "com.veilsync.app.CalendarAppAlias"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                AppLayout.Calendar -> {
                    GenerateCalendarIconWithDate(this)
                    pm.setComponentEnabledSetting(
                        android.content.ComponentName(this, "com.veilsync.app.CalculatorAppAlias"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        android.content.ComponentName(this, "com.veilsync.app.CalendarAppAlias"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onPause() {
        super.onPause()
        viewModel.closeVault()
    }
}

@Composable
fun MainScreen(
    viewModel: CalculatorViewModel,
    securityManager: SecurityManager,
    vaultManager: HiddenVaultManager,
    activity: MainActivity,
    onLayoutChanged: (AppLayout) -> Unit = {}
) {
    var appState by remember { mutableStateOf<AppState>(if (securityManager.isPasswordSet()) AppState.CalculatorReady else AppState.SetupPassword) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.closeVault()
                appState = AppState.CalculatorReady
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel.shouldOpenVault) {
        if (viewModel.shouldOpenVault) {
            appState = AppState.VaultOpen
        }
    }

    LaunchedEffect(viewModel.currentLayout) {
        val titleText = when (viewModel.currentLayout) {
            AppLayout.Calculator -> "Calculator"
            AppLayout.Calendar -> "Calendar"
        }
        activity.title = titleText
    }

    BackHandler(enabled = appState != AppState.SetupPassword) {
        when (appState) {
            AppState.CalculatorReady -> activity.finishAffinity()
            AppState.VaultOpen -> {
                viewModel.closeVault()
                appState = AppState.CalculatorReady
            }
            else -> {}
        }
    }

    when (appState) {
        AppState.SetupPassword -> {
            PasswordSetupScreen(onPasswordSet = { password ->
                securityManager.setPassword(password)
                appState = AppState.CalculatorReady
            })
        }
        AppState.CalculatorReady -> {
            CalculatorScreen(viewModel, onLayoutChanged = onLayoutChanged)
        }
        AppState.VaultOpen -> {
            HiddenVaultScreen(
                vaultManager = vaultManager,
                onExitVault = {
                    viewModel.closeVault()
                    appState = AppState.CalculatorReady
                },
                onBackPressed = {
                    viewModel.closeVault()
                    appState = AppState.CalculatorReady
                }
            )
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel, onLayoutChanged: (AppLayout) -> Unit = {}) {
    when (viewModel.currentLayout) {
        AppLayout.Calculator -> {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (maxHeight > maxWidth) {
                    PortraitCalculator(viewModel, onLayoutChanged)
                } else {
                    LandscapeCalculator(viewModel)
                }
            }
        }
        AppLayout.Calendar -> CalendarView(viewModel, onLayoutChanged)
    }
}

@Composable
fun PortraitCalculator(viewModel: CalculatorViewModel, onLayoutChanged: (AppLayout) -> Unit = {}) {
    val density = LocalDensity.current
    val navBarHeight = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    var showLayoutSelector by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = navBarHeight)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = { showLayoutSelector = true }) {
                Canvas(modifier = Modifier.size(36.dp)) {
                    drawCircle(color = Color.Transparent, radius = 4.dp.toPx(), style = Stroke(width = 2.dp.toPx()))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (viewModel.previousDisplay.isNotEmpty()) {
            Text(text = "${viewModel.previousDisplay} ${viewModel.currentOperation} ${viewModel.secondOperand}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = Color.Gray, fontSize = 24.sp)
        }
        Text(text = viewModel.display, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = Color.White, fontSize = if (viewModel.display.length > 7) 48.sp else 64.sp, fontWeight = FontWeight.Light, maxLines = 1)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control row with History button and Backspace button
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // History button (Clock icon)
            Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFF333333), CircleShape).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { viewModel.toggleHistory() }, contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    // Draw clock circle
                    drawCircle(color = if (viewModel.isShowingHistory) Color(0xFFFF9F0A) else Color.White, radius = 8.dp.toPx(), style = Stroke(width = 1.5.dp.toPx()))
                    // Draw clock hands
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    drawLine(color = if (viewModel.isShowingHistory) Color(0xFFFF9F0A) else Color.White, start = Offset(centerX, centerY), end = Offset(centerX, centerY - 4.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                    drawLine(color = if (viewModel.isShowingHistory) Color(0xFFFF9F0A) else Color.White, start = Offset(centerX, centerY), end = Offset(centerX + 3.dp.toPx(), centerY), strokeWidth = 1.5.dp.toPx())
                }
            }
            
            // Empty spaces
            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
            
            // Backspace button
            CalculatorButton(
                text = "⌫",
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFF333333),
                contentColor = Color.White,
                onClick = { viewModel.onDelete() }
            )
        }
        
        // History card (shown instead of numberpad)
        if (viewModel.isShowingHistory) {
            Column(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "History", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.clearHistory() }, modifier = Modifier.size(24.dp)) {
                        Text(text = "Clear", color = Color(0xFFFF9F0A), fontSize = 11.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 1.dp)
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(viewModel.history) { item ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(text = item.expression, color = Color.Gray, fontSize = 12.sp)
                            Text(text = "= ${item.result}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    }
                }
            }
        } else {
            val buttons = listOf(listOf("C", "+/-", "%", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "−"), listOf("1", "2", "3", "+"), listOf("0", ".", "="))
            buttons.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { label ->
                        val isOrange = label in listOf("÷", "×", "−", "+", "=")
                        val isGray = label in listOf("C", "+/-", "%")
                        CalculatorButton(
                            text = label,
                            modifier = Modifier.weight(if (label == "0") 2f else 1f),
                            containerColor = when { isOrange -> Color(0xFFFF9F0A); isGray -> Color(0xFFA5A5A5); else -> Color(0xFF333333) },
                            contentColor = if (isGray) Color.Black else Color.White,
                            onClick = {
                                when (label) {
                                    "C" -> viewModel.onClear()
                                    "+/-" -> viewModel.onPlusMinus()
                                    "%" -> viewModel.onPercentage()
                                    "÷" -> viewModel.onOperationClick("/")
                                    "×" -> viewModel.onOperationClick("*")
                                    "−" -> viewModel.onOperationClick("-")
                                    "+" -> viewModel.onOperationClick("+")
                                    "=" -> viewModel.onEqualsClick()
                                    "." -> viewModel.onDecimalClick()
                                    else -> viewModel.onNumberClick(label)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    if (showLayoutSelector) {
        LayoutSelectorDialog(currentLayout = AppLayout.Calculator, onLayoutSelected = { layout -> viewModel.setLayout(layout); onLayoutChanged(layout); showLayoutSelector = false }, onDismiss = { showLayoutSelector = false })
    }
}

@Composable
fun LandscapeCalculator(viewModel: CalculatorViewModel) {
    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = viewModel.display, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = Color.White, fontSize = 48.sp)
        }
    }
}

@Composable
fun CalculatorButton(text: String, modifier: Modifier = Modifier, containerColor: Color, contentColor: Color = Color.White, onClick: () -> Unit) {
    Box(modifier = modifier.aspectRatio(if (text == "0") 2.2f else 1f).background(containerColor, CircleShape).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text = text, color = contentColor, fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LayoutSelectorDialog(currentLayout: AppLayout, onLayoutSelected: (AppLayout) -> Unit, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.width(320.dp).background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp)).padding(20.dp).clickable(enabled = false) {}, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Select App Mode", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LayoutCard(name = "Calculator", drawableRes = R.drawable.ic_launcher_playstore, isSelected = currentLayout == AppLayout.Calculator, onClick = { onLayoutSelected(AppLayout.Calculator) }, modifier = Modifier.weight(1f))
                LayoutCard(name = "Calendar", drawableRes = R.drawable.ic_launcher_calendar, isSelected = currentLayout == AppLayout.Calendar, onClick = { onLayoutSelected(AppLayout.Calendar) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LayoutCard(name: String, drawableRes: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(if (isSelected) Color(0xFF2C2C2E) else Color.Transparent, RoundedCornerShape(12.dp))
            .border(2.dp, if (isSelected) Color(0xFFFF9F0A) else Color(0xFF3A3A3C), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = name,
            modifier = Modifier.size(48.dp),
            colorFilter = if (isSelected) androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFFF9F0A)) else null
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
    }
}

@Composable
fun CalendarView(viewModel: CalculatorViewModel, onLayoutChanged: (AppLayout) -> Unit = {}) {
    val density = LocalDensity.current
    val navBarHeight = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    var showLayoutSelector by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var selectedDateForEvent by remember { mutableStateOf<Long?>(null) }
    
    val calendar = remember { Calendar.getInstance() }
    var displayCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var allEvents by remember { mutableStateOf(emptyList<CalendarEvent>()) }
    var selectedDayEvents by remember { mutableStateOf(emptyList<CalendarEvent>()) }
    var eventToDelete by remember { mutableStateOf<CalendarEvent?>(null) }
    
    // Initialize repository and load events
    val context = LocalContext.current
    val repository = remember { CalendarRepository(context) }
    
    LaunchedEffect(Unit) {
        repository.importDefaultEvents()
        allEvents = repository.getEvents()
    }
    
    fun updateEventsForMonth() {
        allEvents = repository.getEvents()
    }
    
    fun onDaySelected(dayNum: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, displayCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dayNum)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDateForEvent = cal.timeInMillis
        selectedDayEvents = repository.getEventsByDate(cal.timeInMillis)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = navBarHeight)) {
            // Top Action Bar
            Row(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(28.dp).alpha(0f).clickable { showLayoutSelector = true })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(28.dp).clickable { showSearchDialog = true })
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(32.dp).border(1.5.dp, Color.White, RoundedCornerShape(8.dp)).clickable { showYearPicker = true }, contentAlignment = Alignment.Center) {
                        Text(text = displayCalendar.get(Calendar.YEAR).toString().takeLast(2), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Month & Year Navigation
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = Color.White, modifier = Modifier.size(24.dp).clickable {
                    displayCalendar = Calendar.getInstance().apply {
                        time = displayCalendar.time
                        add(Calendar.MONTH, -1)
                    }
                    updateEventsForMonth()
                })
                
                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayCalendar.time).uppercase()
                Text(text = monthName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Next Month", tint = Color.White, modifier = Modifier.size(24.dp).rotate(180f).clickable {
                    displayCalendar = Calendar.getInstance().apply {
                        time = displayCalendar.time
                        add(Calendar.MONTH, 1)
                    }
                    updateEventsForMonth()
                })
            }

            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEachIndexed { index, day ->
                    Text(text = day, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (index == 6) Color(0xFFD32F2F) else Color.Gray, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                }
            }
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            val displayCal = Calendar.getInstance().apply { time = displayCalendar.time; set(Calendar.DAY_OF_MONTH, 1) }
            val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK)
            val shiftedFirstDay = (firstDayOfWeek + 5) % 7
            val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val prevMonthMaxDays = Calendar.getInstance().apply { time = displayCalendar.time; add(Calendar.MONTH, -1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today = Calendar.getInstance()

            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                repeat(6) { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        repeat(7) { day ->
                            val cellIndex = week * 7 + day
                            val (dayNum, isCurrentMonth) = when {
                                cellIndex < shiftedFirstDay -> (prevMonthMaxDays - (shiftedFirstDay - cellIndex - 1)) to false
                                cellIndex < shiftedFirstDay + daysInMonth -> (cellIndex - shiftedFirstDay + 1) to true
                                else -> (cellIndex - (shiftedFirstDay + daysInMonth) + 1) to false
                            }
                            
                            val isToday = isCurrentMonth && 
                                today.get(Calendar.YEAR) == displayCal.get(Calendar.YEAR) && 
                                today.get(Calendar.MONTH) == displayCal.get(Calendar.MONTH) && 
                                today.get(Calendar.DAY_OF_MONTH) == dayNum
                            
                            val dayDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, displayCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, displayCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, dayNum)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            
                            val dayEvents = allEvents.filter { event ->
                                val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
                                eventCal.get(Calendar.YEAR) == dayDate.get(Calendar.YEAR) &&
                                eventCal.get(Calendar.MONTH) == dayDate.get(Calendar.MONTH) &&
                                eventCal.get(Calendar.DAY_OF_MONTH) == dayDate.get(Calendar.DAY_OF_MONTH)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.8f)
                                    .padding(2.dp)
                                    .background(
                                        color = if (isCurrentMonth && selectedDateForEvent == dayDate.timeInMillis) 
                                            Color(0xFF333333) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { 
                                        if (isCurrentMonth) {
                                            onDaySelected(dayNum)
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 16.sp,
                                        color = when {
                                            isToday -> Color.White
                                            isCurrentMonth -> if (day == 6) Color(0xFFD32F2F) else Color.White
                                            else -> Color.DarkGray
                                        },
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        modifier = if (isToday) 
                                            Modifier.border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp) 
                                        else 
                                            Modifier.padding(vertical = 2.dp)
                                    )
                                    
                                    // Show event tags
                                    dayEvents.take(2).forEach { event ->
                                        EventTag(event.title, Color(android.graphics.Color.parseColor(event.color)))
                                    }
                                    if (dayEvents.size > 2) {
                                        Text(text = "+${dayEvents.size - 2}", fontSize = 7.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                }
            }
        }

        // Bottom Action Bar with selected day info and add event
        if (selectedDateForEvent != null) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBarHeight + 16.dp).padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val selectedDateFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(selectedDateForEvent!!)
                    Text(text = selectedDateFormat, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .background(Color(0xFF1C1C1E), RoundedCornerShape(28.dp))
                                .padding(horizontal = 20.dp)
                                .clickable { showAddEventDialog = true },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = "Add event", color = Color.Gray, fontSize = 16.sp)
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF333333), CircleShape)
                                .clickable { showAddEventDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    
                    // Display selected day's events
                    if (selectedDayEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Events (${selectedDayEvents.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                            items(selectedDayEvents) { event ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1C1C1E), RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                Color(android.graphics.Color.parseColor(event.color)),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(text = event.title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Delete",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { eventToDelete = event }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = navBarHeight + 16.dp).padding(horizontal = 16.dp)) {
                Text(text = "Select a day to add events", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        if (showLayoutSelector) {
            LayoutSelectorDialog(currentLayout = viewModel.currentLayout, onLayoutSelected = { layout -> viewModel.setLayout(layout); onLayoutChanged(layout); showLayoutSelector = false }, onDismiss = { showLayoutSelector = false })
        }

        if (showSearchDialog) {
            SearchEventDialog(
                repository = repository,
                viewModel = viewModel,
                context = context,
                onDismiss = { showSearchDialog = false }
            )
        }

        if (showAddEventDialog && selectedDateForEvent != null) {
            AddEventDialog(
                selectedDate = selectedDateForEvent!!,
                repository = repository,
                onDismiss = {
                    showAddEventDialog = false
                    updateEventsForMonth()
                    onDaySelected((Calendar.getInstance().apply { timeInMillis = selectedDateForEvent!! }).get(Calendar.DAY_OF_MONTH))
                }
            )
        }

        if (eventToDelete != null) {
            DeleteEventConfirmDialog(
                event = eventToDelete!!,
                onConfirm = {
                    repository.deleteEvent(eventToDelete!!.id)
                    updateEventsForMonth()
                    onDaySelected((Calendar.getInstance().apply { timeInMillis = selectedDateForEvent ?: System.currentTimeMillis() }).get(Calendar.DAY_OF_MONTH))
                    eventToDelete = null
                },
                onDismiss = { eventToDelete = null }
            )
        }

        if (showYearPicker) {
            YearPickerDialog(
                currentYear = displayCalendar.get(Calendar.YEAR),
                onYearSelected = { year ->
                    displayCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, displayCalendar.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, minOf(displayCalendar.get(Calendar.DAY_OF_MONTH), getActualMaximum(Calendar.DAY_OF_MONTH)))
                    }
                    updateEventsForMonth()
                    showYearPicker = false
                },
                onDismiss = { showYearPicker = false }
            )
        }
    }
}

@Composable
fun AddEventDialog(
    selectedDate: Long,
    repository: CalendarRepository,
    onDismiss: () -> Unit
) {
    var eventTitle by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#1976D2") }
    var selectedCategory by remember { mutableStateOf("general") }
    
    val colors = listOf(
        "#1976D2" to "Blue",
        "#2E7D32" to "Green",
        "#D32F2F" to "Red",
        "#F57C00" to "Orange",
        "#7B1FA2" to "Purple",
        "#00796B" to "Teal"
    )
    
    val categories = listOf("general", "holiday", "personal", "work", "birthday")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Event", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // Event Title Input
            OutlinedTextField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = { Text("Event Title", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Color Selection
            Text("Color:", color = Color.White, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { (colorCode, _) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(android.graphics.Color.parseColor(colorCode)),
                                shape = CircleShape
                            )
                            .border(
                                width = if (selectedColor == colorCode) 3.dp else 0.dp,
                                color = if (selectedColor == colorCode) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorCode }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Category Selection
            Text("Category:", color = Color.White, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { category ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (selectedCategory == category) Color(0xFF6200EE) else Color(0xFF333333),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(category, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text("Cancel", color = Color.White)
                }
                Button(
                    onClick = {
                        if (eventTitle.isNotBlank()) {
                            val event = CalendarEvent(
                                title = eventTitle,
                                date = selectedDate,
                                color = selectedColor,
                                category = selectedCategory
                            )
                            repository.addEvent(event)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Add", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DeleteEventConfirmDialog(
    event: CalendarEvent,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Delete Event?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            Text(event.title, fontSize = 16.sp, color = Color(android.graphics.Color.parseColor(event.color)))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text("Cancel", color = Color.White)
                }
                Button(
                    onClick = { onConfirm() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun YearPickerDialog(
    currentYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val minYear = 1900
    val maxYear = 2500
    val years = (minYear..maxYear).toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.9f)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Year", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Year grid
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items((years.size + 2) / 3) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { colIndex ->
                            val yearIndex = rowIndex * 3 + colIndex
                            if (yearIndex < years.size) {
                                val year = years[yearIndex]
                                Button(
                                    onClick = { onYearSelected(year) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (year == currentYear) Color(0xFF6200EE) else Color(0xFF333333)
                                    )
                                ) {
                                    Text(year.toString(), color = Color.White, fontSize = 12.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

@Composable
fun SearchEventDialog(
    repository: CalendarRepository,
    viewModel: CalculatorViewModel,
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(emptyList<CalendarEvent>()) }
    val securityManager = remember { SecurityManager(context) }

    fun handleDonePressed() {
        // Check if the search query matches the password
        if (securityManager.isPasswordSet() && searchQuery.isNotEmpty()) {
            if (securityManager.verifyPassword(searchQuery)) {
                viewModel.openVault()
                onDismiss()
                return
            }
        }
        // If not a password, just close the dialog
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Search Events", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    searchResults = repository.searchEvents(it)
                },
                label = { Text("Search by title or category", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleDonePressed() }),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { searchQuery = ""; searchResults = emptyList() }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Search Results
            if (searchQuery.isEmpty()) {
                Text("Enter a search term to find events", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
            } else if (searchResults.isEmpty()) {
                Text("No events found matching: \"$searchQuery\"", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
            } else {
                Text("Found ${searchResults.size} event(s)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(searchResults) { event ->
                        SearchResultItem(event)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text("Close", color = Color.White)
                }
                if (searchQuery.isNotEmpty()) {
                    Button(
                        onClick = { handleDonePressed() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(event: CalendarEvent) {
    val dateFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
    val eventDate = dateFormat.format(event.date)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF333333), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(event.color)),
                        shape = CircleShape
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(text = eventDate, color = Color.Gray, fontSize = 12.sp)
            }
        }
        
        if (event.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = event.description, color = Color.LightGray, fontSize = 11.sp)
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF6200EE), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = event.category, color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun EventTag(text: String, color: Color) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 1.dp).background(color, RoundedCornerShape(2.dp)).padding(horizontal = 2.dp, vertical = 1.dp)) {
        Text(text = text, color = Color.White, fontSize = 8.sp, maxLines = 1, lineHeight = 9.sp)
    }
}

enum class AppState { SetupPassword, CalculatorReady, VaultOpen }



