package dz.hasibfawraty.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AlgerianTariffCalculatorTest {
    @Test
    fun elTarfHouseholdReferenceInvoiceMatchesBeforeStamp() {
        val estimate = AlgerianTariffs.water(
            cubicMeters = 27.0,
            months = 3,
            zone = "CONSTANTINE",
            category = "HOME",
            southLevyRate = false,
            otherBillLinesTtc = 0.0
        )
        assertEquals(572.49, estimate.subtotalHt, 0.01)
        assertEquals(51.53, estimate.vat, 0.01)
        assertEquals(81.00, estimate.extra, 0.01)
        assertEquals(720.90, estimate.total, 0.01)
    }
}
