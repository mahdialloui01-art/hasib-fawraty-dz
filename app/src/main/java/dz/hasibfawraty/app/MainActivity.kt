package dz.hasibfawraty.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF101A25)
private val Panel = Color(0xFF192736)
private val Green = Color(0xFF20C997)
private val Gold = Color(0xFFFFC857)
private val Muted = Color(0xFF9EAFBD)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HasibApp() }
    }
}

data class BillCategory(val title: String, val subtitle: String, val icon: ImageVector, val color: Color)

@Composable
fun HasibApp() {
    val categories = listOf(
        BillCategory("الكهرباء", "سونلغاز • استهلاك الكهرباء", Icons.Default.Bolt, Color(0xFFFFC857)),
        BillCategory("الغاز", "سونلغاز • استهلاك الغاز", Icons.Default.LocalFireDepartment, Color(0xFFFF8E72)),
        BillCategory("الماء", "ADE أو شركة المياه المحلية", Icons.Default.WaterDrop, Color(0xFF54B9F2)),
        BillCategory("اتصالات الجزائر", "Idoom • الإنترنت والهاتف الثابت", Icons.Default.Wifi, Color(0xFF9B8CFF))
    )
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
            if (selected != null) {
                CategoryDetail(selected!!, onBack = { selected = null })
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
private fun CategoryDetail(item: BillCategory, onBack: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("") }
    var kw by remember { mutableStateOf("6") }
    var months by remember { mutableStateOf("3") }
    var telecomPrice by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("0") }
    val isElectric = item.title == "الكهرباء"
    val isGas = item.title == "الغاز"
    val isWater = item.title == "الماء"
    val isTelecom = item.title == "اتصالات الجزائر"
    val count = months.toIntOrNull()?.coerceIn(1, 12) ?: 3
    val a = first.replace(',', '.').toDoubleOrNull()
    val b = second.replace(',', '.').toDoubleOrNull()
    val estimate = when {
        isElectric && a != null && b != null && b >= a -> AlgerianTariffs.electricity(a, b, kw.toDoubleOrNull() ?: 6.0, count)
        isGas && a != null && b != null && b >= a && (factor.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0 -> AlgerianTariffs.gas(a, b, factor.replace(',', '.').toDoubleOrNull() ?: 0.0, count)
        isWater && a != null && a >= 0 -> AlgerianTariffs.water(a, count)
        else -> null
    }
    val telecomTotal = ((telecomPrice.replace(',', '.').toDoubleOrNull() ?: 0.0) * count) +
            (extra.replace(',', '.').toDoubleOrNull() ?: 0.0)

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White) }
            Column {
                Text(item.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("حاسبة تقديرية بالتعريفة الجزائرية", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).fillMaxWidth()) {
            Text(
                when {
                    isElectric -> "أدخل مؤشري العداد القديم والجديد (kWh)"
                    isGas -> "أدخل مؤشري عداد الغاز ومعامل PCS من الفاتورة"
                    isWater -> "أدخل حجم الاستهلاك بالمتر المكعب (m³)"
                    else -> "أدخل سعر اشتراكك حسب العرض الحالي"
                },
                color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            if (isElectric || isGas) {
                CalcField("المؤشر القديم", first) { first = it }
                Spacer(Modifier.height(9.dp))
                CalcField("المؤشر الجديد", second) { second = it }
                if (isGas) {
                    Spacer(Modifier.height(9.dp))
                    CalcField("معامل PCS (من فاتورتك)", factor) { factor = it }
                    Text("أدخل قيمة PCS الظاهرة في فاتورتك حتى لا يكون الحساب تخمينياً.", color = Gold, fontSize = 11.sp)
                } else {
                    Spacer(Modifier.height(9.dp))
                    Text("القدرة المتعاقد عليها (kW)", color = Muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("4", "6", "12", "20", "40").forEach { v ->
                            FilterChip(selected = kw == v, onClick = { kw = v }, label = { Text(v) })
                        }
                    }
                }
            } else if (isWater) {
                CalcField("الاستهلاك خلال الفترة (m³)", first) { first = it }
                Text("التعريفة المنزلية محسوبة حسب شرائح الاستهلاك الفصلية، مع الماء والتطهير والاشتراك.", color = Muted, fontSize = 11.sp)
            } else {
                CalcField("سعر الاشتراك للفترة (دج)", telecomPrice) { telecomPrice = it }
                CalcField("إضافات أو مستحقات أخرى (دج)", extra) { extra = it }
                Text("اكتب سعر عرض Idoom الذي تشترك فيه كما يظهر في فضاء الزبون أو وصل الدفع؛ الأسعار تتغير حسب العرض.", color = Muted, fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("مدة الفوترة بالأشهر", color = Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1", "2", "3", "6", "12").forEach { v ->
                    FilterChip(selected = months == v, onClick = { months = v }, label = { Text(v) })
                }
            }
            Spacer(Modifier.height(18.dp))
            if (isTelecom && telecomPrice.isNotBlank()) {
                EstimatePanel(telecomTotal, "حساب اشتراك تقريبي", "السعر المدخل × $count أشهر + الإضافات. تأكد من سعر العرض قبل الدفع.")
            } else if (estimate != null) {
                EstimatePanel(estimate.total, "المبلغ التقديري", estimate.note)
                Spacer(Modifier.height(8.dp))
                Text("تفصيل الحساب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("الاستهلاك: %.2f".format(estimate.consumption), color = Muted, fontSize = 12.sp)
                Text("المبلغ HT: %.2f دج".format(estimate.subtotalHt), color = Muted, fontSize = 12.sp)
                Text("TVA: %.2f دج".format(estimate.vat), color = Muted, fontSize = 12.sp)
                if (estimate.extra > 0) Text("رسوم إضافية: %.2f دج".format(estimate.extra), color = Muted, fontSize = 12.sp)
            } else {
                Text("أدخل القراءات أو الاستهلاك لعرض الحساب.", color = Muted, fontSize = 12.sp)
            }
        }
        Text("تنبيه: هذه حاسبة تقديرية وليست فاتورة رسمية. لا تشمل الديون السابقة أو الطابع أو كل الرسوم الخاصة.", color = Gold, fontSize = 10.sp)
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
