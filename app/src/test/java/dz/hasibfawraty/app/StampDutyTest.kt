package dz.hasibfawraty.app

import org.junit.Assert.assertEquals
import org.junit.Test

class StampDutyTest {
    @Test
    fun cashStampUses2025Article100BracketsAndMinimum() {
        assertEquals(0.0, calculateCashStamp(300.0), 0.001)
        assertEquals(5.0, calculateCashStamp(300.01), 0.001)
        assertEquals(300.0, calculateCashStamp(30_000.0), 0.001)
        assertEquals(451.5, calculateCashStamp(30_000.01), 0.001)
        assertEquals(1_500.0, calculateCashStamp(100_000.0), 0.001)
        assertEquals(2_002.0, calculateCashStamp(100_000.01), 0.001)
    }
}
