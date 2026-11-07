package dz.hasibfawraty.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AlgerianTariffCalculatorTest {
    @Test
    fun householdWaterReferenceCalculationUsesExpectedComponents() {
        val estimate = AlgerianTariffs.water(
            cubicMeters = 27.0,
            months = 3,
            zone = "CONSTANTINE",
            category = "HOME",
            southLevyRate = false,
            otherBillLinesTtc = 0.0
        )
        // Recomputed from the tariff constants and progressive bands in this calculator.
        assertEquals(572.475, estimate.subtotalHt, 0.001)
        assertEquals(51.53, estimate.vat, 0.001)
        assertEquals(96.88, estimate.extra, 0.001)
        assertEquals(720.885, estimate.total, 0.001)
    }

    @Test
    fun waterFixedChargesScaleWithBillingMonths() {
        val oneMonth = AlgerianTariffs.water(0.0, 1, "CONSTANTINE", "HOME")
        val threeMonths = AlgerianTariffs.water(0.0, 3, "CONSTANTINE", "HOME")
        assertEquals(oneMonth.subtotalHt * 3.0, threeMonths.subtotalHt, 0.001)
    }

    @Test
    fun electricityDoesNotProduceNegativeConsumptionForReversedReadings() {
        val estimate = AlgerianTariffs.electricity(120.0, 100.0, 6.0, 3)
        assertEquals(0.0, estimate.consumption, 0.001)
    }
    @Test
    fun nonHouseholdElectricityUsesNonHouseholdFirstBand() {
        val estimate = AlgerianTariffs.electricity(0.0, 250.0, 6.0, 3, household = false)
        assertEquals(250.0 * 4.1789 + 6.0 * 4.37 * 3.0, estimate.subtotalHt, 0.001)
        assertEquals(250.0 * 4.1789 * 0.09 + 6.0 * 4.37 * 3.0 * 0.09, estimate.vat, 0.001)
    }

    @Test
    fun nonHouseholdGasUsesNonHouseholdFirstBand() {
        val estimate = AlgerianTariffs.gas(0.0, 2500.0, 1.0, 3, household = false)
        assertEquals(2500.0 * 0.3245 + 28.5 * 3.0, estimate.subtotalHt, 0.001)
        assertEquals(2500.0 * 0.3245 * 0.09 + 28.5 * 3.0 * 0.09, estimate.vat, 0.001)
    }

}
