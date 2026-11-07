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
        // Invoice-specific rights, housing tax, stamp and debts are entered once as TTC extras.
        val invoiceExtras = max(0.0, otherBillLinesTtc)
        return Estimate(used, ht, vat, 0.0, invoiceExtras, ht + vat + invoiceExtras,
            "تقدير حسب تعريفة 54M المنزلية. أدخل الحقوق والرسوم والديون الظاهرة في الفاتورة مرة واحدة ضمن البنود الإضافية.")
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

    // Base rates and quarterly progressive bands follow Executive Decree 05-13.
    // El Tarf belongs to the Constantine tariff zone (Article 12).
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
        val (waterFixedPerQuarter, sanitationFixedPerQuarter) = when (category) {
            "ADMIN" -> 450.0 to 60.0
            "INDUSTRY" -> 4500.0 to 2100.0
            else -> 240.0 to 60.0
        }
        val waterFixed = waterFixedPerQuarter * months / 3.0
        val sanitationFixed = sanitationFixedPerQuarter * months / 3.0
        val fixed = waterFixed + sanitationFixed
        val ht = water + sanitation + fixed

        // The supplied ADE household invoice applies 9% to the full combined HT
        // assessment (water + sanitation + fixed subscription charges).
        // Keep 19% for non-household estimates until an official matching invoice
        // confirms the applicable tax base/rate for those customer categories.
        val vatRate = if (category == "HOME") 0.09 else 0.19
        val vat = ht * vatRate

        // The supplied invoice shows 3 DA/m3 management levy and two 4% levies
        // calculated on drinking-water consumption only (not sanitation/fixed fees).
        val managementLevy = m * 3.0
        val resourceLevyRate = if (southLevyRate) 0.02 else 0.04
        val resourceLevies = water * resourceLevyRate * 2.0
        val levies = managementLevy + resourceLevies
        return Estimate(m, ht, vat, levies, max(0.0, otherBillLinesTtc), ht + vat + levies + max(0.0, otherBillLinesTtc),
            "للمنزلي: TVA بنسبة 9% على أساس HT الإجمالي كما في فاتورة ADE المرجعية. إتاوتا النوعية والاقتصاد محسوبتان على مبلغ استهلاك الماء فقط. الفئات الأخرى تحتاج مطابقة فاتورة.")
    }
}
