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
        BillCategory("سونلغاز", "فاتورة الكهرباء والغاز", Icons.Default.Bolt, Color(0xFFFFC857)),
        BillCategory("ADE", "الجزائرية للمياه", Icons.Default.WaterDrop, Color(0xFF54B9F2)),
        BillCategory("SEATA", "مياه عنابة والطارف", Icons.Default.WaterDrop, Color(0xFF42C4E8)),
        BillCategory("SEAAL", "مياه الجزائر وتيبازة", Icons.Default.WaterDrop, Color(0xFF54B9F2)),
        BillCategory("SEACO", "مياه قسنطينة", Icons.Default.WaterDrop, Color(0xFF54B9F2)),
        BillCategory("SEOR", "مياه وهران", Icons.Default.WaterDrop, Color(0xFF54B9F2)),
        BillCategory("اتصالات الجزائر", "Idoom • الهاتف والإنترنت", Icons.Default.Wifi, Color(0xFF9B8CFF)),
        BillCategory("موبيليس", "الهاتف النقال والإنترنت", Icons.Default.Smartphone, Color(0xFF58D6A5)),
        BillCategory("جازي Djezzy", "الهاتف النقال والإنترنت", Icons.Default.Smartphone, Color(0xFFFF8E72)),
        BillCategory("أوريدو Ooredoo", "الهاتف النقال والإنترنت", Icons.Default.Smartphone, Color(0xFFE85D75)),
        BillCategory("البريد والمواصلات", "رسوم وخدمات بريدية", Icons.Default.MarkunreadMailbox, Color(0xFF80A9FF)),
        BillCategory("التأمينات", "SAA • CAAT • CAAR وغيرها", Icons.Default.VerifiedUser, Color(0xFF61C6C2)),
        BillCategory("الضرائب والرسوم", "ضرائب • طابع • رسوم إدارية", Icons.Default.AccountBalance, Color(0xFF80A9FF)),
        BillCategory("السكن والإيجار", "إيجار • أعباء العمارة", Icons.Default.Home, Color(0xFFE8B86D)),
        BillCategory("النقل والسيارة", "تأمين • وقود • مواقف", Icons.Default.DirectionsCar, Color(0xFFFF9B54)),
        BillCategory("التلفزيون والاشتراكات", "اشتراكات وخدمات رقمية", Icons.Default.LiveTv, Color(0xFFFF8E72)),
        BillCategory("التعليم والصحة", "مدرسة • جامعة • عيادات", Icons.Default.School, Color(0xFFD59AFF)),
        BillCategory("فواتير أخرى", "أضف شركة أو مصروفاً خاصاً", Icons.Default.AddCircle, Color(0xFFB4C0CC))
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
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White) }
            Text(item.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Text("إضافة فاتورة جديدة", color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("سجّل المبلغ وتاريخ الاستحقاق للاحتفاظ بسجل مصاريفك.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = amount, onValueChange = { amount = it }, modifier = Modifier.fillMaxWidth(), label = { Text("المبلغ بالدينار الجزائري (دج)") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, modifier = Modifier.fillMaxWidth(), label = { Text("تاريخ الفاتورة / الاستحقاق") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ملاحظة أو رقم الفاتورة") }, minLines = 2)
        Spacer(Modifier.height(18.dp))
        Text("ملاحظة: هذه الواجهة تسجّل بيانات الفاتورة؛ الحساب الآلي للتعريفات الرسمية سيضاف في مرحلة الحسابات.", color = Gold, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(14.dp)) {
            Text("حفظ مبدئي", color = Ink, fontWeight = FontWeight.Bold)
        }
    }
}
