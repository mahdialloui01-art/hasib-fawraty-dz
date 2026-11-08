package dz.hasibfawraty.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

@Composable
internal fun AdBanner(
    adsReady: Boolean,
    status: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    if (isPreview) return

    if (!adsReady || adUnitId.isBlank()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF192736))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = status,
                color = Color(0xFF9EAFBD),
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtMost(600)
    val adSize = remember(screenWidthDp) {
        AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }
    val adView = remember(adUnitId, adSize) { AdView(context) }
    var adLoaded by remember(adView) { mutableStateOf(false) }
    var requestFinished by remember(adView) { mutableStateOf(false) }
    var loadStatus by remember(adView) { mutableStateOf("جارٍ تحميل الإعلان…") }

    LaunchedEffect(adView, adSize, adUnitId) {
        Log.i("HasibFawratyAds", "Loading banner. screenWidthDp=$screenWidthDp")
        val request = BannerAdRequest.Builder(adUnitId, adSize).build()
        adView.loadAd(request, object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) {
                Log.i("HasibFawratyAds", "Banner ad loaded successfully")
                adLoaded = true
                requestFinished = true
                loadStatus = "تم تحميل الإعلان"
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("HasibFawratyAds", "Banner ad failed to load: $adError")
                adLoaded = false
                requestFinished = true
                loadStatus = "تعذر تحميل الإعلان: $adError"
            }
        })
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF101A25))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (adLoaded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إعلان",
                    color = Color(0xFF9EAFBD),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFD32F2F), CircleShape)
                ) {
                    Text(
                        text = "×",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (requestFinished && !adLoaded) {
            Text(
                text = loadStatus,
                color = Color(0xFFFFC857),
                fontSize = 10.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        AndroidView(
            factory = { adView },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (requestFinished && !adLoaded) Modifier.height(0.dp) else Modifier.wrapContentHeight())
        )
    }
}
