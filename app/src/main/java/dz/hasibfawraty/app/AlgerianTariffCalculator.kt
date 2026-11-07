package dz.hasibfawraty.app

import kotlin.math.max
import kotlin.math.min

data class Estimate(val consumption: Double, val subtotalHt: Double, val vat: Double, val extra: Double, val total: Double, val note: String)

object AlgerianTariffs {
    private fun progressive(value: Double, limits: List<Double>, rates: List<Double>): Double {
        var remaining = max(0.0, value)
        var previous = 0.0
        var sum = 0.0
        for (i in rates.indices) {
            val cap = if (i < limits.size) limits[i] else Double.POSITIVE_INFINITY
            val part = min(remaining, cap - previous)
            if (part > 0) sum += part * rates[i]
            remaining -= part
            previous = cap
            if (remaining <= 0) break
        }
        return sum
    }

    fun electricity(oldIndex: Double, newIndex: Double, subscribedKw: Double, months: Int): Estimate {
        val used = max(0.0, newIndex - oldIndex)
        val energy = progressive(used, listOf(125.0, 250.0, 1000.0), listOf(1.7787, 4.1789, 4.8120, 5.4796))
        val fixed = max(0.0, subscribedKw) * 4.37 * months
        val ht = energy + fixed
        val vat = (progressive(used, listOf(125.0, 250.0), listOf(1.7787, 4.1789, 0.0)) * 0.09) +
                (progressive(max(0.0, used - 250.0), listOf(750.0), listOf(4.8120, 5.4796)) * 0.19) + fixed * 0.09
        val radio = when {
            used > 390 -> 100.0
            used > 190 -> 50.0
            used > 70 -> 25.0
            else -> 0.0
        }
        return Estimate(used, ht, vat, radio, ht + vat + radio,
            "تقدير حسب تعريفة 54M المنزلية. لا يشمل الطابع والديون والرسوم الاستثنائية أو ضريبة السكن.")
    }

    fun gas(oldIndex: Double, newIndex: Double, pcs: Double, months: Int): Estimate {
        val cubic = max(0.0, newIndex - oldIndex)
        val thermies = cubic * max(0.0, pcs)
        val energy = progressive(thermies, listOf(1125.0, 2500.0, 7500.0), listOf(0.1682, 0.3245, 0.4025, 0.4599))
        val fixed = 28.5 * months
        val vat = (progressive(thermies, listOf(1125.0, 2500.0), listOf(0.1682, 0.3245, 0.0)) * 0.09) +
                (progressive(max(0.0, thermies - 2500.0), listOf(5000.0), listOf(0.4025, 0.4599)) * 0.19) + fixed * 0.09
        val ht = energy + fixed
        return Estimate(cubic, ht, vat, 0.0, ht + vat,
            "التحويل إلى حراريات باستعمال معامل PCS المدون في فاتورتك. لا يشمل الطابع والديون والرسوم الاستثنائية.")
    }

    fun water(cubicMeters: Double, months: Int): Estimate {
        val m = max(0.0, cubicMeters)
        val water = progressive(m, listOf(25.0, 55.0, 82.0), listOf(6.30, 20.48, 34.65, 40.95))
        val sanitation = progressive(m, listOf(25.0, 55.0, 82.0), listOf(2.35, 7.64, 12.93, 15.28))
        val fixed = (240.0 + 60.0) * months / 3.0
        val ht = water + sanitation + fixed
        return Estimate(m, ht, 0.0, 0.0, ht,
            "تقدير HT وفق جدول ADE المنزلي للفوترة الثلاثية؛ قد تختلف الفاتورة النهائية حسب المنطقة والموزع والضرائب.")
    }
}
