package dz.hasibfawraty.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

@Composable
internal fun AdBanner(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    if (isPreview || adUnitId.isBlank()) return

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtMost(600)
    val adSize = remember(screenWidthDp) {
        AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }
    val adView = remember { AdView(context) }

    LaunchedEffect(adView, adSize, adUnitId) {
        val request = BannerAdRequest.Builder(adUnitId, adSize).build()
        adView.loadAd(request, object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) {
                Log.d("HasibFawratyAds", "Banner ad loaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.w("HasibFawratyAds", "Banner ad failed to load: $adError")
            }
        })
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF101A25)).padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "إعلان",
                color = Color(0xFF9EAFBD),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إخفاء الإعلان",
                    tint = Color(0xFF9EAFBD)
                )
            }
        }
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }
}
