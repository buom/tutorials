package parts.code.money

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import parts.code.money.Currency.EUR
import parts.code.money.Currency.USD
import java.math.BigDecimal
import java.math.RoundingMode

class MoneyTest {

    @Test
    fun `instantiation with different types`() {
        val moneyFromInt = Money(11, USD)
        val moneyFromDouble = Money(0.11, USD)
        val moneyFromBigDecimal = Money(BigDecimal.valueOf(0.11), USD)
        val moneyFromString = Money("0.11", USD)

        assertEquals(BigDecimal(11), moneyFromInt.amount)
        assertEquals(BigDecimal.valueOf(0.11), moneyFromDouble.amount)
        assertEquals(BigDecimal.valueOf(0.11), moneyFromBigDecimal.amount)
        assertEquals(BigDecimal("0.11"), moneyFromString.amount)

        val money = Money.zero(USD)

        assertEquals(BigDecimal.ZERO, money.amount)
    }

    @Test
    fun `arithmetic operations`() {
        val moneySum = Money(0.11, USD) + Money(2.35, USD)
        val moneySubtract = Money(0.11, USD) - Money(2.35, USD)

        assertEquals(Money(2.46, USD), moneySum)
        assertEquals(Money(-2.24, USD), moneySubtract)

        assertThrows<IllegalArgumentException> {
            val moneyException = Money(0.11, USD) + Money(2.35, EUR)
        }

        val moneyMultiply = Money(0.11, USD) * 3
        val moneyDivide = Money(0.11, USD) / 3

        assertEquals(Money(0.33, USD), moneyMultiply)
        assertEquals(Money(0.03666666666666667, USD), moneyDivide)

        val listFold = listOf(Money(1, USD), Money(2, USD), Money(3, USD))
        val listFoldSum = listFold.fold(Money.zero(USD)) { sum, element -> sum + element }

        assertEquals(listFoldSum, Money(6, USD))

        val listReduce = listOf(Money(5, USD), Money(8, USD), Money(13, USD))
        val listReduceSum = listReduce.reduce { sum, element -> sum + element }

        assertEquals(listReduceSum, Money(26, USD))
    }

    @Test
    fun comparisons() {
        val isGreater = Money(2.35, USD) > Money(0.11, USD)
        val isGreaterOrEquals = Money(0.11, USD) >= Money(0.11, USD)
        val isLower = Money(0.11, USD) < Money(2.35, USD)
        val isLowerOrEquals = Money(0.11, USD) <= Money(0.11, USD)
        val isEquals = Money(0.11, USD) == Money(0.11, USD)
        val isNotEquals = Money(0.11, USD) != Money(2.35, USD)

        assertTrue(isGreater)
        assertTrue(isGreaterOrEquals)
        assertTrue(isLower)
        assertTrue(isLowerOrEquals)
        assertTrue(isEquals)
        assertTrue(isNotEquals)
    }

    @Test
    fun rounding() {
        val moneyRound = Money(0.112358, USD).round(3, RoundingMode.HALF_EVEN)

        assertEquals(Money(0.112, USD), moneyRound)
    }
}
