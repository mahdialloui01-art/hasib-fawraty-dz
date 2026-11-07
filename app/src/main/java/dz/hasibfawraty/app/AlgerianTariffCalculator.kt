package dz.hasibfawraty.app

import kotlin.math.max
import kotlin.math.min

data class Estimate(val consumption: Double, val subtotalHt: Double, val vat: Double, val extra: Double, val otherBillLines: Double, val total: Double, val note: String)

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

    fun electricity(oldIndex: Double, newIndex: Double, subscribedKw: Double, months: Int, otherBillLinesTtc: Double = 0.0): Estimate {
        val used = max(0.0, newIndex - oldIndex)
        val energy = progressive(used, listOf(125.0, 250.0, 1000.0).map { it * months / 3.0 }, listOf(1.7787, 4.1789, 4.8120, 5.4796))
        val fixed = max(0.0, subscribedKw) * 4.37 * months
        val ht = energy + fixed
        val vat = (progressive(used, listOf(125.0, 250.0).map { it * months / 3.0 }, listOf(1.7787, 4.1789, 0.0)) * 0.09) +
                (progressive(max(0.0, used - 250.0 * months / 3.0), listOf(750.0 * months / 3.0), listOf(4.8120, 5.4796)) * 0.19) + fixed * 0.09
        val periodFactor = months.coerceAtLeast(1) / 3.0
        val radio = when {
            used > 390.0 * periodFactor -> 100.0
            used > 190.0 * periodFactor -> 50.0
            used > 70.0 * periodFactor -> 25.0
            else -> 0.0
        }
        return Estimate(used, ht, vat, radio, max(0.0, otherBillLinesTtc), ht + vat + radio + max(0.0, otherBillLinesTtc),
            "تقدير حسب تعريفة 54M المنزلية. لا يشمل الطابع والديون والرسوم الاستثنائية أو ضريبة السكن.")
    }

    fun gas(oldIndex: Double, newIndex: Double, pcs: Double, months: Int, otherBillLinesTtc: Double = 0.0): Estimate {
        val cubic = max(0.0, newIndex - oldIndex)
        val thermies = cubic * max(0.0, pcs)
        val energy = progressive(thermies, listOf(1125.0, 2500.0, 7500.0).map { it * months / 3.0 }, listOf(0.1682, 0.3245, 0.4025, 0.4599))
        val fixed = 28.5 * months
        val vat = (progressive(thermies, listOf(1125.0, 2500.0).map { it * months / 3.0 }, listOf(0.1682, 0.3245, 0.0)) * 0.09) +
                (progressive(max(0.0, thermies - 2500.0 * months / 3.0), listOf(5000.0 * months / 3.0), listOf(0.4025, 0.4599)) * 0.19) + fixed * 0.09
        val ht = energy + fixed
        return Estimate(cubic, ht, vat, 0.0, max(0.0, otherBillLinesTtc), ht + vat + max(0.0, otherBillLinesTtc),
            "التحويل إلى حراريات باستعمال معامل PCS المدون في فاتورتك. لا يشمل الطابع والديون والرسوم الاستثنائية.")
    }

    // Base rates are set by territorial tariff zone under Executive Decree 05-13.
    fun water(cubicMeters: Double, months: Int, zone: String, category: String, southLevyRate: Boolean = false, otherBillLinesTtc: Double = 0.0): Estimate {
        val m = max(0.0, cubicMeters)
        val (waterBase, sanitationBase) = when (zone) {
            "CHLEF" -> 6.10 to 2.20
            "OUARGLA" -> 5.80 to 2.10
            else -> 6.30 to 2.35 // Alger, Oran and Constantine
        }
        val multiplier = when (category) {
            "ADMIN" -> 5.5
            "INDUSTRY" -> 6.5
            else -> 1.0
        }
        val water = if (category == "HOME") {
            progressive(m, listOf(25.0, 55.0, 82.0).map { it * months / 3.0 },
                listOf(waterBase, waterBase * 3.25, waterBase * 5.5, waterBase * 6.5))
        } else m * waterBase * multiplier
        val sanitation = if (category == "HOME") {
            progressive(m, listOf(25.0, 55.0, 82.0).map { it * months / 3.0 },
                listOf(sanitationBase, sanitationBase * 3.25, sanitationBase * 5.5, sanitationBase * 6.5))
        } else m * sanitationBase * multiplier
        val fixedPerQuarter = when (category) {
            "ADMIN" -> 450.0 + 60.0
            "INDUSTRY" -> 4500.0 + 2100.0
            else -> 240.0 + 60.0
        }
        val fixed = fixedPerQuarter * months / 3.0
        val ht = water + sanitation + fixed
        // Water management levy: 3 DA per consumed m3. Resource-protection and water-economy levies:
        // 4% each in the North, 2% each in the specifically listed Southern wilayas.
        val managementLevy = m * 3.0
        val resourceLevyRate = if (southLevyRate) 0.02 else 0.04
        val resourceLevies = ht * resourceLevyRate * 2.0
        val levies = managementLevy + resourceLevies
        val vat = ht * 0.09
        return Estimate(m, ht, vat, levies, max(0.0, otherBillLinesTtc), ht + vat + levies + max(0.0, otherBillLinesTtc),
            "تقدير HT حسب المرسوم 05-13، مع إتاوة التسيير 3 دج/م³ وإتاوتي حماية النوعية واقتصاد الماء (4% أو 2% لكل واحدة حسب الولاية) وTVA 9%. الطابع والديون والخدمات الخاصة تُضاف يدوياً.")
    }
}
