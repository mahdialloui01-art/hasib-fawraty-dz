package dz.hasibfawraty.app

import kotlin.math.ceil
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

    private fun roundUpCent(value: Double): Double = ceil(value * 100.0 - 1e-9) / 100.0

    fun electricity(oldIndex: Double, newIndex: Double, subscribedKw: Double, months: Int, otherBillLinesTtc: Double = 0.0, household: Boolean = true): Estimate {
        val period = months.coerceIn(1, 12) / 3.0
        val used = max(0.0, newIndex - oldIndex)
        val lowBandLimit = (if (household) 250.0 else 250.0) * period
        val middleBandLimit = 1000.0 * period
        val limits = if (household) listOf(125.0, 250.0, 1000.0).map { it * period }
                     else listOf(250.0, 1000.0).map { it * period }
        val rates = if (household) listOf(1.7787, 4.1789, 4.8120, 5.4796)
                    else listOf(4.1789, 4.8120, 5.4796)
        val energy = progressive(used, limits, rates)
        val fixed = max(0.0, subscribedKw) * 4.37 * months.coerceIn(1, 12)
        val lowVatLimit = if (household) 250.0 * period else 250.0 * period
        val lowRate = if (household) listOf(125.0, 250.0).map { it * period } else listOf(lowVatLimit)
        val lowRates = if (household) listOf(1.7787, 4.1789) else listOf(4.1789)
        val lowVatBase = progressive(used, lowRate, lowRates)
        val highVatBase = if (household) {
            progressive(max(0.0, used - lowVatLimit), listOf(750.0 * period), listOf(4.8120, 5.4796))
        } else {
            progressive(max(0.0, used - lowVatLimit), listOf(750.0 * period), listOf(4.8120, 5.4796))
        }
        val vat = lowVatBase * 0.09 + highVatBase * 0.19 + fixed * 0.09
        val invoiceExtras = max(0.0, otherBillLinesTtc)
        return Estimate(used, energy + fixed, vat, 0.0, invoiceExtras, energy + fixed + vat + invoiceExtras,
            if (household) "تعريفة 54M المنزلية." else "تعريفة 54NM لغير المنزلي. راجع تصنيف عقدك.")
    }

    fun gas(oldIndex: Double, newIndex: Double, pcs: Double, months: Int, otherBillLinesTtc: Double = 0.0, household: Boolean = true): Estimate {
        val period = months.coerceIn(1, 12) / 3.0
        val cubic = max(0.0, newIndex - oldIndex)
        val thermies = cubic * max(0.0, pcs)
        val limits = if (household) listOf(1125.0, 2500.0, 7500.0).map { it * period }
                     else listOf(2500.0, 7500.0).map { it * period }
        val rates = if (household) listOf(0.1682, 0.3245, 0.4025, 0.4599)
                    else listOf(0.3245, 0.4025, 0.4599)
        val energy = progressive(thermies, limits, rates)
        val fixed = 28.5 * months.coerceIn(1, 12)
        val lowLimit = 2500.0 * period
        val lowRates = if (household) listOf(1125.0, 2500.0).map { it * period } else listOf(lowLimit)
        val lowVatRates = if (household) listOf(0.1682, 0.3245) else listOf(0.3245)
        val lowVatBase = progressive(thermies, lowRates, lowVatRates)
        val highVatBase = progressive(max(0.0, thermies - lowLimit), listOf(5000.0 * period), listOf(0.4025, 0.4599))
        val vat = lowVatBase * 0.09 + highVatBase * 0.19 + fixed * 0.09
        val extras = max(0.0, otherBillLinesTtc)
        return Estimate(cubic, energy + fixed, vat, 0.0, extras, energy + fixed + vat + extras,
            if (household) "تعريفة 23M المنزلية. التحويل باستعمال PCS المدون في الفاتورة." else "تعريفة 23NM لغير المنزلي. التحويل باستعمال PCS المدون في الفاتورة.")
    }

    // Base rates and quarterly progressive bands follow Executive Decree 05-13.
    // El Tarf belongs to the Constantine tariff zone (Article 12).
    fun water(cubicMeters: Double, months: Int, zone: String, category: String, southLevyRate: Boolean = false, otherBillLinesTtc: Double = 0.0): Estimate {
        val m = max(0.0, cubicMeters)
        val (waterBase, sanitationBase) = when (zone) {
            "CHLEF" -> 6.10 to 2.20
            "OUARGLA" -> 5.80 to 2.10
            else -> 6.30 to 2.35
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
        val ht = water + sanitation + waterFixed + sanitationFixed

        // ADE reference invoice: 9% on the combined household HT base.
        // Non-household tax treatment remains provisional pending matching invoices.
        val vatRate = if (category == "HOME") 0.09 else 0.19
        val vat = roundUpCent(ht * vatRate)

        // The invoice assesses management at 3 DA/m3 and each resource levy
        // at 4% of drinking-water consumption (2% in the listed southern wilayas).
        val managementLevy = roundUpCent(m * 3.0)
        val resourceLevyRate = if (southLevyRate) 0.02 else 0.04
        val qualityLevy = roundUpCent(water * resourceLevyRate)
        val economyLevy = roundUpCent(water * resourceLevyRate)
        val levies = managementLevy + qualityLevy + economyLevy
        return Estimate(m, ht, vat, levies, max(0.0, otherBillLinesTtc), ht + vat + levies + max(0.0, otherBillLinesTtc),
            "للمنزلي: TVA بنسبة 9% على أساس HT الإجمالي وفق فاتورة ADE المرجعية. إتاوتا النوعية والاقتصاد محسوبتان كل واحدة على مبلغ استهلاك الماء فقط. الفئات الأخرى تحتاج مطابقة فاتورة.")
    }
}
