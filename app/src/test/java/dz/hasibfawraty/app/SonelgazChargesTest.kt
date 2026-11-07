package dz.hasibfawraty.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SonelgazChargesTest {
    @Test
    fun annualHousingTaxUsesCregRatesAndBillingPeriod() {
        assertEquals(75.0, calculateHousingTax(3, false, false), 0.001)
        assertEquals(150.0, calculateHousingTax(3, false, true), 0.001)
        assertEquals(300.0, calculateHousingTax(3, true, false), 0.001)
        assertEquals(600.0, calculateHousingTax(3, true, true), 0.001)
        assertEquals(300.0, calculateHousingTax(12, false, false), 0.001)
    }

    @Test
    fun fixedConsumptionRightUsesOfficialCregBrackets() {
        assertEquals(0.0, calculateConsumptionRight(70.0), 0.001)
        assertEquals(25.0, calculateConsumptionRight(71.0), 0.001)
        assertEquals(25.0, calculateConsumptionRight(190.0), 0.001)
        assertEquals(50.0, calculateConsumptionRight(191.0), 0.001)
        assertEquals(50.0, calculateConsumptionRight(390.0), 0.001)
        assertEquals(100.0, calculateConsumptionRight(391.0), 0.001)
    }
}
