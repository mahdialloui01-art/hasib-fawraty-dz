package dz.hasibfawraty.app

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

private val Ink = Color(0xFF101A25)
private val Panel = Color(0xFF192736)
private val Green = Color(0xFF20C997)
private val Gold = Color(0xFFFFC857)
private val Muted = Color(0xFF9EAFBD)

internal fun calculateConsumptionRight(electricKwh: Double): Double = when {
    electricKwh > 390.0 -> 100.0
    electricKwh > 190.0 -> 50.0
    electricKwh > 70.0 -> 25.0
    else -> 0.0
}

internal fun calculateHousingTax(months: Int, professional: Boolean, highRateArea: Boolean): Double {
    // CREG: annual housing tax is 300/1200 DA in ordinary municipalities
    // and 600/2400 DA in district capitals and the four specified wilayas.
    val annual = when {
        professional && highRateArea -> 2400.0
        professional -> 1200.0
        highRateArea -> 600.0
        else -> 300.0
    }
    return annual * months.coerceIn(1, 12) / 12.0
}

internal fun calculateCashStamp(amountTtc: Double): Double {
    if (amountTtc <= 300.0) return 0.0
    val ratePerHundred = when {
        amountTtc <= 30_000.0 -> 1.0
        amountTtc <= 100_000.0 -> 1.5
        else -> 2.0
    }
    val hundredDinarTranches = kotlin.math.ceil(amountTtc / 100.0)
    return maxOf(5.0, hundredDinarTranches * ratePerHundred)
}

class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private var updateDownloaded by mutableStateOf(false)

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            // The user may postpone or cancel a flexible update; the app remains usable.
        }
    }

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            updateDownloaded = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateListener)
        checkForPlayUpdate()
        setContent {
            HasibApp(
                updateDownloaded = updateDownloaded,
                onCompleteUpdate = { appUpdateManager.completeUpdate() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    updateDownloaded = true
                }
            }
        }
    }

    override fun onDestroy() {
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(installStateListener)
        }
        super.onDestroy()
    }

    private fun checkForPlayUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build())
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }
}

data class BillCategory(val title: String, val subtitle: String, val icon: ImageVector, val color: Color)
data class BillHistoryEntry(val id:String,val category:String,val amount:Double,val details:String,val createdAt:Long)
private object BillHistoryStore {
 private const val PREF="bill_history"
 fun load(c:Context):List<BillHistoryEntry> = try {
  val a=JSONArray(c.getSharedPreferences(PREF,0).getString("items","[]") ?: "[]")
  (0 until a.length()).map { i -> a.getJSONObject(i).let { o -> BillHistoryEntry(o.getString("id"),o.getString("category"),o.getDouble("amount"),o.optString("details"),o.getLong("date")) } }.sortedByDescending { it.createdAt }
 } catch (_:Exception) { emptyList() }
 fun save(c:Context,items:List<BillHistoryEntry>) {
  val a=JSONArray()
  items.forEach { e -> a.put(JSONObject().put("id",e.id).put("category",e.category).put("amount",e.amount).put("details",e.details).put("date",e.createdAt)) }
  c.getSharedPreferences(PREF,0).edit().putString("items",a.toString()).apply()
 }
}

private fun waterTariffZone(wilaya: String): String = when (wilaya) {
    "الشلف", "عين الدفلى", "غليزان", "تيارت", "تيسمسيلت", "الجلفة" -> "CHLEF"
    "ورقلة", "الوادي", "إليزي", "الأغواط", "غرداية", "بشار", "تندوف", "أدرار", "تمنراست" -> "OUARGLA"
    "وهران", "عين تموشنت", "تلمسان", "مستغانم", "معسكر", "سيدي بلعباس", "سعيدة", "النعامة", "البيض" -> "ORAN"
    "قسنطينة", "جيجل", "ميلة", "باتنة", "خنشلة", "بسكرة", "عنابة", "الطارف", "سكيكدة", "سوق أهراس", "قالمة", "تبسة", "أم البواقي" -> "CONSTANTINE"
    else -> "ALGER"
}

private fun waterZoneLabel(zone: String): String = when (zone) {
    "CHLEF" -> "منطقة الشلف"
    "OUARGLA" -> "منطقة ورقلة"
    "ORAN" -> "منطقة وهران"
    "CONSTANTINE" -> "منطقة قسنطينة"
    else -> "منطقة الجزائر"
}

@Composable
fun HasibApp(updateDownloaded: Boolean = false, onCompleteUpdate: () -> Unit = {}) {
    val categories = listOf(
        BillCategory("سونلغاز", "فاتورة موحدة • الكهرباء والغاز", Icons.Default.Bolt, Color(0xFFFFC857)),
        BillCategory("الماء", "ADE أو شركة المياه المحلية", Icons.Default.WaterDrop, Color(0xFF54B9F2)),
    )
    val context = androidx.compose.ui.platform.LocalContext.current
    val history = remember { mutableStateListOf<BillHistoryEntry>().apply { addAll(BillHistoryStore.load(context)) } }
    var selected by remember { mutableStateOf<BillCategory?>(null) }
    var search by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    val visible = categories.filter { it.title.contains(search, true) || it.subtitle.contains(search, true) }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Green, secondary = Gold,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Green) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0; selected = null }, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("الرئيسية") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1; selected = null }, icon = { Icon(Icons.Default.ReceiptLong, null) }, label = { Text("فواتيري") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2; selected = null }, icon = { Icon(Icons.Default.Analytics, null) }, label = { Text("الإحصائيات") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3; selected = null }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("الإعدادات") })
                }
            }
        ) { padding ->
            if (tab == 3 && selected == null) {
                SettingsScreen(contentPadding = padding)
            } else if (tab == 1 && selected == null) {
                BillHistoryScreen(padding, history, { e -> history.removeAll { it.id == e.id }; BillHistoryStore.save(context,history) }, { history.clear(); BillHistoryStore.save(context,history) })
            } else if (tab == 2 && selected == null) {
                StatisticsScreen(padding, history)
            } else if (selected != null) {
                CategoryDetail(selected!!, contentPadding = padding, onBack = { selected = null }, onSave = { name, amount, details ->
                    history.add(0, BillHistoryEntry(UUID.randomUUID().toString(),name,amount,details,System.currentTimeMillis()))
                    BillHistoryStore.save(context,history)
                })
            } else {
                Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("حاسب فاتورتي", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("DZ  •  مصاريفك تحت السيطرة", color = Muted, fontSize = 12.sp)
                        }
                        Box(Modifier.size(46.dp).background(Brush.linearGradient(listOf(Green, Color(0xFF087F68))), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(25.dp))
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF164C48), Color(0xFF1B3445))), RoundedCornerShape(24.dp)).padding(18.dp)) {
                        Column {
                            Text("ملخص مصاريفك", color = Color(0xFFC5E8DF), fontSize = 13.sp)
                            Spacer(Modifier.height(7.dp))
                            Text("تابع فواتيرك في مكان واحد", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(13.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = Gold, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("ابدأ بإضافة أول فاتورة لهذا الشهر", color = Color.White.copy(alpha = .82f), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(19.dp))
                    Text("خدمات وفواتير المواطن", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("اختر الشركة أو نوع الفاتورة", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = search, onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ابحث عن شركة أو فاتورة...", color = Muted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                        singleLine = true, shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green, unfocusedBorderColor = Color(0xFF344657),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = Green
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        items(visible) { item ->
                            CategoryCard(item) { selected = item }
                        }
                    }
                }
            }
        }
        if (updateDownloaded) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("التحديث جاهز") },
                text = { Text("تم تنزيل تحديث التطبيق من Google Play. أعد تشغيل التطبيق لتثبيت النسخة الجديدة.") },
                confirmButton = {
                    TextButton(onClick = onCompleteUpdate) { Text("تثبيت وإعادة التشغيل") }
                }
            )
        }
    }
}

@Composable
private fun CategoryCard(item: BillCategory, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(139.dp).background(Panel, RoundedCornerShape(19.dp))
            .clickable(onClick = onClick).padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(Modifier.size(42.dp).background(item.color.copy(alpha = .15f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(23.dp))
        }
        Column {
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
            Text(item.subtitle, color = Muted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun CategoryDetail(item: BillCategory, contentPadding: PaddingValues, onBack: () -> Unit, onSave: (String,Double,String) -> Unit) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("") }
    var gasFirst by remember { mutableStateOf("") }
    var gasSecond by remember { mutableStateOf("") }
    var gasPcs by remember { mutableStateOf("") }
    var kw by remember { mutableStateOf("6") }
    var months by remember { mutableStateOf("3") }
    var telecomPrice by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("0") }
    var otherBillLines by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var premisesType by remember { mutableStateOf("HOME") }
    var housingArea by remember { mutableStateOf("NORMAL") }
    var waterWilaya by remember { mutableStateOf("اختر الولاية") }
    var waterMenuExpanded by remember { mutableStateOf(false) }
    var waterCategory by remember { mutableStateOf("HOME") }
    val isSonelgaz = item.title == "سونلغاز"
    val isElectric = false
    val isGas = false
    val isWater = item.title == "الماء"
    val isTelecom = item.title == "اتصالات الجزائر"
    val count = months.toIntOrNull()?.coerceIn(1, 12) ?: 3
    val a = first.replace(',', '.').toDoubleOrNull()
    val b = second.replace(',', '.').toDoubleOrNull()
    val gasA = gasFirst.replace(',', '.').toDoubleOrNull()
    val gasB = gasSecond.replace(',', '.').toDoubleOrNull()
    val pcsValue = gasPcs.replace(',', '.').toDoubleOrNull()
    val invoiceExtras = otherBillLines.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val electricEstimate = if (isSonelgaz && a != null && b != null && b >= a)
        AlgerianTariffs.electricity(a, b, kw.toDoubleOrNull() ?: 6.0, count, 0.0, premisesType == "HOME") else null
    val gasEstimate = if (isSonelgaz && gasA != null && gasB != null && gasB >= gasA && (pcsValue ?: 0.0) > 0.0)
        AlgerianTariffs.gas(gasA, gasB, pcsValue!!, count, 0.0, premisesType == "HOME") else null
    val electricKwh = if (a != null && b != null && b >= a) b - a else 0.0
    val consumptionRight = if (isSonelgaz && premisesType == "HOME" && electricEstimate != null) calculateConsumptionRight(electricKwh) else 0.0
    val housingTax = if (isSonelgaz) calculateHousingTax(count, premisesType == "PRO", housingArea == "HIGH") else 0.0
    val sonelgazReady = isSonelgaz && electricEstimate != null && gasEstimate != null
    val estimate = when {
        isWater && waterWilaya != "اختر الولاية" && a != null && b != null && b >= a -> AlgerianTariffs.water(b - a, count, waterTariffZone(waterWilaya), waterCategory, waterWilaya in listOf("الأغواط", "غرداية", "الوادي", "تندوف", "بشار", "إليزي", "تمنراست", "أدرار", "بسكرة", "ورقلة"), invoiceExtras)
        else -> null
    }
    val sonelgazTotal = (electricEstimate?.let { it.subtotalHt + it.vat } ?: 0.0) +
        (gasEstimate?.let { it.subtotalHt + it.vat } ?: 0.0) + consumptionRight + housingTax + invoiceExtras
    val cashStamp = if (paymentMethod == "CASH" && sonelgazReady) calculateCashStamp(sonelgazTotal) else 0.0
    val sonelgazPayable = sonelgazTotal + cashStamp
    val waterStamp = if (isWater && estimate != null && paymentMethod == "CASH") calculateCashStamp(estimate.total) else 0.0
    val waterPayable = (estimate?.total ?: 0.0) + waterStamp
    val telecomTotal = ((telecomPrice.replace(',', '.').toDoubleOrNull() ?: 0.0) * count) +
            (extra.replace(',', '.').toDoubleOrNull() ?: 0.0)

    Column(Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 18.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White) }
            Column {
                Text(item.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("حاسبة تقديرية بالتعريفة الجزائرية", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            Text(
                when {
                    isSonelgaz -> "فاتورة سونلغاز الموحدة: الكهرباء + الغاز"
                    isWater -> "أدخل مؤشري العداد القديم والجديد (m³)"
                    else -> "أدخل سعر اشتراكك حسب العرض الحالي"
                },
                color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            if (isSonelgaz) {
                Text("أولاً: الكهرباء", color = Gold, fontWeight = FontWeight.Bold)
                CalcField("مؤشر الكهرباء القديم (kWh)", first) { first = it }
                Spacer(Modifier.height(9.dp))
                CalcField("مؤشر الكهرباء الجديد (kWh)", second) { second = it }
                Spacer(Modifier.height(12.dp))
                Text("ثانياً: الغاز", color = Gold, fontWeight = FontWeight.Bold)
                CalcField("مؤشر الغاز القديم", gasFirst) { gasFirst = it }
                Spacer(Modifier.height(9.dp))
                CalcField("مؤشر الغاز الجديد", gasSecond) { gasSecond = it }
                Spacer(Modifier.height(9.dp))
                CalcField("معامل PCS من الفاتورة", gasPcs) { gasPcs = it }
                Text("أدخل معامل PCS كما هو مكتوب في فاتورتك.", color = Gold, fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
                Text("القدرة المتعاقد عليها للكهرباء (PMD)", color = Muted, fontSize = 12.sp)
                Text("أحادي الطور 220V", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("4", "6", "12").forEach { v ->
                        FilterChip(selected = kw == v, onClick = { kw = v }, label = { Text("$v kW") })
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("ثلاثي الطور 380V", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("20", "40", "60", "80").forEach { v ->
                        FilterChip(selected = kw == v, onClick = { kw = v }, label = { Text("$v kW") })
                    }
                }
                Text("اختر PMD المطابقة لخانة القدرة في الفاتورة.", color = Gold, fontSize = 10.sp)
            } else if (isElectric || isGas) {
                CalcField("المؤشر القديم", first) { first = it }
                Spacer(Modifier.height(9.dp))
                CalcField("المؤشر الجديد", second) { second = it }
                if (isGas) {
                    Spacer(Modifier.height(9.dp))
                    CalcField("معامل PCS (من فاتورتك)", factor) { factor = it }
                    Text("أدخل قيمة PCS الظاهرة في فاتورتك حتى لا يكون الحساب تخمينياً.", color = Gold, fontSize = 11.sp)
                } else {
                    Spacer(Modifier.height(9.dp))
                    Text("القدرة المتعاقد عليها (PMD)", color = Muted, fontSize = 12.sp)
                    Text("أحادي الطور 220V", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("4", "6", "12").forEach { v ->
                            FilterChip(selected = kw == v, onClick = { kw = v }, label = { Text("$v kW") })
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("ثلاثي الطور 380V", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("20", "40", "60", "80").forEach { v ->
                            FilterChip(selected = kw == v, onClick = { kw = v }, label = { Text("$v kW") })
                        }
                    }
                    Text("اختر القيمة المطابقة لخانة PMD في فاتورتك.", color = Gold, fontSize = 10.sp)
                }
            } else if (isWater) {
                CalcField("المؤشر القديم لعداد الماء", first) { first = it }
                Spacer(Modifier.height(9.dp))
                CalcField("المؤشر الجديد لعداد الماء", second) { second = it }
                Spacer(Modifier.height(10.dp))
                Text("اختر الولاية لتحديد منطقة التسعير تلقائياً", color = Muted, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { waterMenuExpanded = true }) {
                        Text(waterWilaya, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Green)
                    }
                    DropdownMenu(expanded = waterMenuExpanded, onDismissRequest = { waterMenuExpanded = false }) {
                        listOf("أدرار", "عين الدفلى", "عنابة", "باتنة", "بشار", "بجاية", "سيدي بلعباس", "بسكرة", "البليدة", "برج بوعريريج", "البويرة", "بومرداس", "الشلف", "قسنطينة", "الجلفة", "البيض", "الوادي", "الطارف", "غرداية", "قالمة", "إليزي", "جيجل", "خنشلة", "الأغواط", "معسكر", "المدية", "ميلة", "مستغانم", "المسيلة", "النعامة", "وهران", "ورقلة", "أم البواقي", "غليزان", "سعيدة", "سطيف", "سكيكدة", "سوق أهراس", "تمنراست", "عين تموشنت", "تيارت", "تندوف", "تيبازة", "تيسمسيلت", "تيزي وزو", "تلمسان").forEach { wilaya ->
                            DropdownMenuItem(text = { Text(wilaya) }, onClick = { waterWilaya = wilaya; waterMenuExpanded = false })
                        }
                    }
                }
                Text(waterZoneLabel(waterTariffZone(waterWilaya)), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("نوع الاشتراك", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FilterChip(selected = waterCategory == "HOME", onClick = { waterCategory = "HOME" }, label = { Text("منزلي") })
                    FilterChip(selected = waterCategory == "ADMIN", onClick = { waterCategory = "ADMIN" }, label = { Text("إداري/تجاري") })
                    FilterChip(selected = waterCategory == "INDUSTRY", onClick = { waterCategory = "INDUSTRY" }, label = { Text("صناعي/سياحي") })
                }
                Text("الحساب يعتمد شرائح الاستهلاك الفصلية ويشمل الماء والتطهير والاشتراك الثابت. اختر نفس نوع الاشتراك الموجود في فاتورتك.", color = Muted, fontSize = 11.sp)
            } else {
                CalcField("سعر الاشتراك للفترة (دج)", telecomPrice) { telecomPrice = it }
                CalcField("إضافات أو مستحقات أخرى (دج)", extra) { extra = it }
                Text("اكتب سعر عرض Idoom الذي تشترك فيه كما يظهر في فضاء الزبون أو وصل الدفع؛ الأسعار تتغير حسب العرض.", color = Muted, fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            if (isSonelgaz || isWater) {
                Text("مدة الفوترة بالأشهر", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1", "2", "3", "6", "12").forEach { v ->
                        FilterChip(selected = months == v, onClick = { months = v }, label = { Text(v) })
                    }
                }
            }
            if (isSonelgaz || isWater) {
                CalcField("الديون السابقة (دج)", otherBillLines) { otherBillLines = it }
                Text("أدخل الديون السابقة الظاهرة في الفاتورة فقط. لا تدخل الضرائب أو الإتاوات أو الاشتراك أو الطابع لأنها محسوبة آلياً.", color = Gold, fontSize = 10.sp)
            }
            if (isWater) {
                Spacer(Modifier.height(10.dp))
                Text("طريقة الدفع", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = paymentMethod == "CASH", onClick = { paymentMethod = "CASH" }, label = { Text("نقداً") })
                    FilterChip(selected = paymentMethod == "ELECTRONIC", onClick = { paymentMethod = "ELECTRONIC" }, label = { Text("إلكتروني") })
                }
                Text(if (paymentMethod == "CASH") "يُضاف طابع المخالصة نقداً حسب المبلغ؛ الدفع الإلكتروني معفى." else "الدفع الإلكتروني معفى من طابع المخالصات.", color = Gold, fontSize = 10.sp)
            }
            if (isSonelgaz) {
                Spacer(Modifier.height(10.dp))
                Text("نوع المحل", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = premisesType == "HOME", onClick = { premisesType = "HOME" }, label = { Text("سكني") })
                    FilterChip(selected = premisesType == "PRO", onClick = { premisesType = "PRO" }, label = { Text("مهني") })
                }
                Text("منطقة رسم السكن", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = housingArea == "NORMAL", onClick = { housingArea = "NORMAL" }, label = { Text("بلدية عادية") })
                    FilterChip(selected = housingArea == "HIGH", onClick = { housingArea = "HIGH" }, label = { Text("مقر دائرة / الجزائر / عنابة / قسنطينة / وهران") })
                }
                Text("يُحسب رسم السكن السنوي بالتناسب مع مدة الفوترة. اختر المنطقة حسب بلديتك.", color = Gold, fontSize = 10.sp)
                Spacer(Modifier.height(10.dp))
                Text("طريقة الدفع", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = paymentMethod == "CASH", onClick = { paymentMethod = "CASH" }, label = { Text("نقداً") })
                    FilterChip(selected = paymentMethod == "ELECTRONIC", onClick = { paymentMethod = "ELECTRONIC" }, label = { Text("إلكتروني") })
                }
                Text(if (paymentMethod == "CASH") "سيُضاف الطابع آلياً حسب شرائح المادة 100 من قانون الطابع." else "الدفع الإلكتروني معفى من طابع المخالصات.", color = Gold, fontSize = 10.sp)
            }
            Spacer(Modifier.height(18.dp))
            if (isSonelgaz && sonelgazReady) {
                Text("تفصيل فاتورة سونلغاز الموحدة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("الكهرباء: HT %.2f دج • TVA %.2f دج".format(electricEstimate!!.subtotalHt, electricEstimate.vat), color = Muted, fontSize = 12.sp)
                Text("الغاز: HT %.2f دج • TVA %.2f دج".format(gasEstimate!!.subtotalHt, gasEstimate.vat), color = Muted, fontSize = 12.sp)
                Text("حقوق ثابتة على الاستهلاك: %.2f دج".format(consumptionRight), color = Muted, fontSize = 12.sp)
                Text("رسم السكن للفترة: %.2f دج".format(housingTax), color = Muted, fontSize = 12.sp)
                Text("الديون السابقة: %.2f دج".format(invoiceExtras), color = Muted, fontSize = 12.sp)
                Text("الصافي TTC قبل الطابع: %.2f دج".format(sonelgazTotal), color = Muted, fontSize = 12.sp)
                Text("الطابع النقدي: %.2f دج".format(cashStamp), color = Muted, fontSize = 12.sp)
            }
            if (estimate != null) {
                Spacer(Modifier.height(8.dp))
                Text("تفصيل الحساب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("الاستهلاك: %.2f".format(estimate.consumption), color = Muted, fontSize = 12.sp)
                Text("المبلغ HT: %.2f دج".format(estimate.subtotalHt), color = Muted, fontSize = 12.sp)
                Text("TVA: %.2f دج".format(estimate.vat), color = Muted, fontSize = 12.sp)
                if (estimate.extra > 0) Text(if (isWater) "إتاوات الماء (التسيير وحماية النوعية واقتصاد الماء): %.2f دج".format(estimate.extra) else "الرسم الثابت على الاستهلاك: %.2f دج".format(estimate.extra), color = Muted, fontSize = 12.sp)
                if (estimate.otherBillLines > 0) Text("الديون السابقة: %.2f دج".format(estimate.otherBillLines), color = Muted, fontSize = 12.sp)
                if (isWater) {
                    Text("المبلغ قبل الطابع: %.2f دج".format(estimate.total), color = Muted, fontSize = 12.sp)
                    Text("الطابع النقدي: %.2f دج".format(waterStamp), color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        when {
            isSonelgaz && sonelgazReady -> {
                EstimatePanel(sonelgazPayable, "المبلغ النهائي للدفع (الكهرباء + الغاز)", "الصافي TTC: %.2f دج • الطابع: %.2f دج • %s".format(sonelgazTotal, cashStamp, if (paymentMethod == "CASH") "نقداً" else "إلكترونياً"))
            }
            isSonelgaz -> {
                ResultHint("فاتورة سونلغاز الموحدة", "أدخل مؤشري الكهرباء ومؤشري الغاز ومعامل PCS والقدرة PMD لإظهار المجموع.")
            }
            isTelecom && telecomPrice.replace(',', '.').toDoubleOrNull() != null -> {
                EstimatePanel(telecomTotal, "المبلغ المتوقع لاشتراكك", "السعر المدخل × $count أشهر + الإضافات. راجع عرضك قبل الدفع.")
            }
            isWater && estimate != null -> {
                EstimatePanel(waterPayable, "المبلغ المتوقع للفاتورة", "قبل الطابع: %.2f دج • الطابع: %.2f دج • %s".format(estimate.total, waterStamp, if (paymentMethod == "CASH") "نقداً" else "إلكترونياً"))
            }
            estimate != null -> {
                EstimatePanel(estimate.total, "المبلغ المتوقع للفاتورة", estimate.note)
            }
            isTelecom -> {
                ResultHint("النتيجة تظهر هنا", "أدخل سعر اشتراك Idoom لتظهر قيمة المبلغ المتوقع مباشرة.")
            }
            isWater && waterWilaya == "اختر الولاية" -> {
                ResultHint("اختر الولاية أولاً", "اختر ولاية مكان العداد حتى نحدد منطقة التسعير الصحيحة قبل حساب الفاتورة.")
            }
            isWater && (a == null || b == null) -> {
                ResultHint("النتيجة تظهر هنا", "أدخل مؤشري العداد القديم والجديد، واختر الولاية ونوع الاشتراك لحساب الاستهلاك تلقائياً.")
            }
            else -> {
                ResultHint("تحقق من البيانات", "تأكد أن المؤشر الجديد أكبر من أو يساوي المؤشر القديم وأن القيم صحيحة.")
            }
        }
        val saveAmount = when {
            isSonelgaz && sonelgazReady -> sonelgazPayable
            isWater && estimate != null -> waterPayable
            isTelecom && telecomPrice.replace(',', '.').toDoubleOrNull() != null -> telecomTotal
            else -> null
        }
        if (saveAmount != null) {
            Button(onClick = {
                val details = when {
                    isSonelgaz -> "الكهرباء " + first + " → " + second + " kWh؛ الغاز " + gasFirst + " → " + gasSecond + "؛ PCS " + gasPcs + "؛ PMD " + kw + "؛ " + count + " أشهر"
                    isWater -> "العداد " + first + " → " + second + " m³؛ الولاية " + waterWilaya + "؛ " + waterCategory + "؛ " + count + " أشهر"
                    else -> "اشتراك " + telecomPrice + " دج؛ " + count + " أشهر؛ إضافات " + extra + " دج"
                }
                onSave(item.title,saveAmount,details)
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor=Green,contentColor=Ink)) {
                Icon(Icons.Default.Save,null); Spacer(Modifier.width(8.dp)); Text("حفظ الحساب في فواتيري")
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(if (isSonelgaz) "تنبيه: هذه حاسبة تقديرية وليست فاتورة رسمية. الرسوم الأخرى يجب إدخالها كما تظهر في فاتورتك؛ الطابع النقدي يُحسب آلياً وفق قانون الطابع." else "تنبيه: هذه حاسبة تقديرية وليست فاتورة رسمية. قد لا تشمل الديون السابقة أو الرسوم الخاصة.", color = Gold, fontSize = 10.sp)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CalcField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValue, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green, unfocusedBorderColor = Color(0xFF344657),
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedLabelColor = Green, unfocusedLabelColor = Muted, cursorColor = Green
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun ResultHint(heading: String, note: String) {
    Column(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(heading, color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(note, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun EstimatePanel(total: Double, heading: String, note: String) {
    Column(
        Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(Color(0xFF164C48), Color(0xFF1B3445))),
            RoundedCornerShape(18.dp)
        ).padding(16.dp)
    ) {
        Text(heading, color = Color(0xFFC5E8DF), fontSize = 12.sp)
        Text("%.2f دج".format(total), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text(note, color = Color.White.copy(alpha = .75f), fontSize = 10.sp)
    }
}

@Composable
private fun SettingsScreen(contentPadding: PaddingValues) {
    var showPrivacy by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 18.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("الإعدادات", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("الخصوصية والدعم ومعلومات التطبيق", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        SettingsRow(Icons.Default.Policy, "سياسة الخصوصية", "اقرأها هنا دون فتح أي موقع خارجي") { showPrivacy = true }
        SettingsRow(Icons.Default.SupportAgent, "الدعم والإبلاغ عن مشكلة", "إرشادات ونسخ وصف المشكلة") { showSupport = true }
        SettingsRow(Icons.Default.Email, "تواصل مع المطوّر", "راسلنا عبر البريد الإلكتروني") {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:mahdialloui01@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "دعم تطبيق حاسب فاتورتي DZ")
            }
            try {
                context.startActivity(emailIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "لا يوجد تطبيق بريد إلكتروني مثبت على الجهاز.", Toast.LENGTH_LONG).show()
            }
        }
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(16.dp)) {
            Text("حول التطبيق", color = Green, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text("حاسب فاتورتي DZ", color = Color.White, fontWeight = FontWeight.Bold)
            Text("الإصدار ${BuildConfig.VERSION_NAME}", color = Muted, fontSize = 12.sp)
            Text("المطوّر: حاسب فاتورتي DZ", color = Color.White, fontSize = 12.sp)
            Text("البريد: mahdialloui01@gmail.com", color = Muted, fontSize = 12.sp)
            Text("حاسبة تقديرية لفواتير الكهرباء والغاز والماء. النتائج ليست فاتورة رسمية.", color = Muted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("لا يرسل التطبيق بيانات الفواتير تلقائياً. عند اختيار التواصل، يفتح تطبيق البريد لإرسال رسالة بموافقتك.", color = Green, fontSize = 11.sp)
    }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("إغلاق") } },
            title = { Text("سياسة الخصوصية") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text("آخر تحديث: 8 نوفمبر 2026\n\n1. بيانات الفواتير: قد تدخل قراءات العدادات والاستهلاك والولاية ومعامل PCS والديون لغرض الحساب فقط. تتم معالجة هذه البيانات محلياً ولا يرسلها التطبيق إلى خوادم المطوّر.\n\n2. التحديثات: يستخدم التطبيق مكتبة Google Play In-App Updates للتحقق من توفر تحديث عبر Google Play. تعالج خدمات Google Play بيانات تقنية مثل إصدار التطبيق وبيانات الجهاز اللازمة لتحديد توفر التحديث وحجمه، وفق شروط وسياسة Google Play. لا تتضمن هذه العملية إرسال قراءات العدادات أو سجل الفواتير إلى المطوّر.\n\n3. التخزين والحذف: يحفظ قسم «فواتيري» الحسابات التي تختار حفظها محلياً على جهازك. يمكنك حذف الفواتير من داخل التطبيق. لا توجد مزامنة سحابية. مسح بيانات التطبيق أو إزالته يحذف البيانات المحلية وفق سلوك Android.\n\n4. الإعلانات والتحليلات: لا يتضمن الإصدار الحالي إعلانات أو أدوات تحليل مدمجة. لا نبيع بيانات الفواتير ولا نشاركها.\n\n5. التواصل: لا يرسل التطبيق رسالة أو بيانات تلقائياً. إذا اخترت «تواصل مع المطوّر»، يفتح تطبيق البريد برسالة جديدة إلى عنوان الدعم ويمكنك مراجعتها وإرسالها بنفسك.\n\n6. الأذونات والأطفال: لا يطلب التطبيق أذونات حساسة مثل الموقع أو جهات الاتصال أو الرسائل. وهو أداة حساب عامة وليس موجهاً خصيصاً للأطفال.", color = Color.White)
                }
            }
        )
    }
    if (showSupport) {
        AlertDialog(
            onDismissRequest = { showSupport = false },
            confirmButton = {
                TextButton(onClick = { showSupport = false }) { Text("إغلاق") }
            },
            dismissButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString("مشكلة في تطبيق حاسب فاتورتي DZ\nالإصدار: ${BuildConfig.VERSION_NAME}\nوصف المشكلة: "))
                }) { Text("نسخ وصف المشكلة") }
            },
            title = { Text("الدعم والمساعدة") },
            text = {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    Text("هذا القسم يعرض إرشادات ولا يرسل معلوماتك تلقائياً.", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("قبل الإبلاغ عن مشكلة:", color = Green, fontWeight = FontWeight.Bold)
                    Text("• تأكد من إدخال المؤشرات كما تظهر في الفاتورة.\n• في سونلغاز، أدخل معامل PCS والقدرة PMD الصحيحين.\n• في الماء، اختر الولاية ونوع الاشتراك الصحيحين.\n• قارن النتيجة مع فاتورة نفس الفترة.", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("يمكنك نسخ قالب المشكلة ثم إرساله بنفسك عبر «تواصل مع المطوّر».", color = Muted, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).background(Panel, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Green, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
        Icon(Icons.Default.ChevronLeft, null, tint = Muted)
    }
}

@Composable
private fun BillHistoryScreen(p:PaddingValues, entries:List<BillHistoryEntry>, onDelete:(BillHistoryEntry)->Unit, onDeleteAll:()->Unit) {
 var ask by remember { mutableStateOf(false) }
 Column(Modifier.fillMaxSize().padding(p).padding(16.dp)) {
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
   Column { Text("فواتيري السابقة",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold); Text(entries.size.toString()+" حساب محفوظ على الهاتف",color=Muted,fontSize=12.sp) }
   if(entries.isNotEmpty()) TextButton(onClick={ask=true}) { Text("حذف الكل",color=Color(0xFFFF8A80)) }
  }
  Spacer(Modifier.height(12.dp))
  if(entries.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
   Column(horizontalAlignment=Alignment.CenterHorizontally) {
    Icon(Icons.Default.ReceiptLong,null,tint=Muted,modifier=Modifier.size(48.dp))
    Text("لا توجد حسابات محفوظة",color=Color.White,fontWeight=FontWeight.Bold)
    Text("بعد الحساب اضغط حفظ الحساب في فواتيري.",color=Muted,fontSize=12.sp)
   }
  } else androidx.compose.foundation.lazy.LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)) {
   items(entries,key={it.id}) { e ->
    Column(Modifier.fillMaxWidth().background(Panel,RoundedCornerShape(16.dp)).padding(14.dp)) {
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
      Text(e.category,color=Green,fontWeight=FontWeight.Bold)
      Text(SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.getDefault()).format(Date(e.createdAt)),color=Muted,fontSize=10.sp)
     }
     Text(String.format(Locale.getDefault(),"%.2f دج",e.amount),color=Color.White,fontSize=22.sp,fontWeight=FontWeight.ExtraBold)
     Text(e.details,color=Muted,fontSize=11.sp)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
      TextButton(onClick={onDelete(e)}) { Icon(Icons.Default.Delete,null,tint=Color(0xFFFF8A80)); Text("حذف",color=Color(0xFFFF8A80)) }
     }
    }
   }
  }
 }
 if(ask) AlertDialog(onDismissRequest={ask=false},title={Text("حذف كل الفواتير؟")},text={Text("لن تتمكن من استرجاع الحسابات بعد حذفها.")},
  confirmButton={TextButton(onClick={onDeleteAll();ask=false}){Text("حذف الكل",color=Color(0xFFFF8A80))}},
  dismissButton={TextButton(onClick={ask=false}){Text("إلغاء")}})
}
@Composable
private fun StatisticsScreen(p:PaddingValues,entries:List<BillHistoryEntry>) {
 val total=entries.sumOf{it.amount}
 Column(Modifier.fillMaxSize().padding(p).padding(18.dp).verticalScroll(rememberScrollState())) {
  Text("إحصائيات فواتيرك",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold)
  Text("مبنية على الحسابات المحفوظة",color=Muted,fontSize=12.sp)
  Spacer(Modifier.height(16.dp))
  Column(Modifier.fillMaxWidth().background(Panel,RoundedCornerShape(18.dp)).padding(16.dp)) {
   Text("مجموع الحسابات",color=Muted,fontSize=12.sp)
   Text(String.format(Locale.getDefault(),"%.2f دج",total),color=Green,fontSize=27.sp,fontWeight=FontWeight.ExtraBold)
   Text(entries.size.toString()+" حساب",color=Muted,fontSize=11.sp)
  }
  entries.groupBy{it.category}.forEach { (name,list) ->
   Spacer(Modifier.height(8.dp))
   Row(Modifier.fillMaxWidth().background(Panel,RoundedCornerShape(14.dp)).padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween) {
    Text(name,color=Color.White,fontWeight=FontWeight.Bold)
    Text(String.format(Locale.getDefault(),"%.2f دج",list.sumOf{it.amount}),color=Gold,fontWeight=FontWeight.Bold)
   }
  }
 }
}
