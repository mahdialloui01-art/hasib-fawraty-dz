package dz.hasibfawraty.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SonelgazChargesTest {
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
