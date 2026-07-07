package com.uynne.lunarcalendar.lunar

import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Vietnamese lunisolar calendar engine, ported from Ho Ngoc Duc's amlich-aa98.js.
 * Pure Kotlin, no Android dependencies. All computations default to UTC+7,
 * the fixed timezone of the Vietnamese lunar calendar since 1968.
 */
object LunarCalendar {

    const val VN_TIME_ZONE = 7.0

    private const val SYNODIC_MONTH = 29.530588853
    private const val NEW_MOON_EPOCH = 2415021.076998695 // JD of the first new moon of 1900

    // --- Julian day ---

    fun jdFromDate(day: Int, month: Int, year: Int): Int {
        val a = (14 - month).floorDiv(12)
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        var jd = day + (153 * m + 2).floorDiv(5) + 365 * y +
            y.floorDiv(4) - y.floorDiv(100) + y.floorDiv(400) - 32045
        if (jd < 2299161) {
            jd = day + (153 * m + 2).floorDiv(5) + 365 * y + y.floorDiv(4) - 32083
        }
        return jd
    }

    fun jdToDate(jd: Int): SolarDate {
        val b: Int
        val c: Int
        if (jd > 2299160) {
            val a = jd + 32044
            b = (4 * a + 3).floorDiv(146097)
            c = a - (b * 146097).floorDiv(4)
        } else {
            b = 0
            c = jd + 32082
        }
        val d = (4 * c + 3).floorDiv(1461)
        val e = c - (1461 * d).floorDiv(4)
        val m = (5 * e + 2).floorDiv(153)
        val day = e - (153 * m + 2).floorDiv(5) + 1
        val month = m + 3 - 12 * m.floorDiv(10)
        val year = b * 100 + d - 4800 + m.floorDiv(10)
        return SolarDate(day, month, year)
    }

    // --- Astronomy (coefficients verbatim from the reference) ---

    /** JD (fractional, UT) of the k-th new moon after the start of 1900. */
    internal fun newMoon(k: Int): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 = c1 - 0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2 * mpr)
        c1 = c1 - 0.0004 * sin(dr * 3 * mpr)
        c1 = c1 + 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 = c1 - 0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2 * f + m))
        c1 = c1 - 0.0004 * sin(dr * (2 * f - m)) - 0.0006 * sin(dr * (2 * f + mpr))
        c1 = c1 + 0.0010 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltaT = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        return jd1 + c1 - deltaT
    }

    /** Apparent ecliptic longitude of the sun in radians, normalized to [0, 2π). */
    internal fun sunLongitude(jdn: Double): Double {
        val t = (jdn - 2451545.0) / 36525
        val t2 = t * t
        val dr = PI / 180
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        var l = (l0 + dl) * dr
        l -= PI * 2 * floor(l / (PI * 2))
        return l
    }

    /** Zodiac sector (0..11) of the sun at local midnight starting the given day. */
    internal fun getSunLongitude(dayNumber: Int, timeZone: Double): Int =
        floor(sunLongitude(dayNumber - 0.5 - timeZone / 24.0) / PI * 6).toInt()

    /** Local calendar day (JDN) containing the k-th new moon. */
    internal fun getNewMoonDay(k: Int, timeZone: Double): Int =
        floor(newMoon(k) + 0.5 + timeZone / 24.0).toInt()

    /** JDN of the first day of the lunar month containing the winter solstice of [year]. */
    internal fun getLunarMonth11(year: Int, timeZone: Double): Int {
        val off = jdFromDate(31, 12, year) - 2415021
        val k = floor(off / SYNODIC_MONTH).toInt()
        var nm = getNewMoonDay(k, timeZone)
        if (getSunLongitude(nm, timeZone) >= 9) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    /** Offset (in months after month 11 starting at [a11]) of the leap month — the first month without a major solar term. */
    internal fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        val k = floor((a11 - NEW_MOON_EPOCH) / SYNODIC_MONTH + 0.5).toInt()
        var i = 1
        var last: Int
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    // --- Conversions ---

    fun solarToLunar(day: Int, month: Int, year: Int, timeZone: Double = VN_TIME_ZONE): LunarDate {
        val dayNumber = jdFromDate(day, month, year)
        // Estimate the lunation index, then bracket robustly: the mean-length estimate
        // can be off by one near month boundaries (e.g. 2054-05-07).
        var k = floor((dayNumber - NEW_MOON_EPOCH) / SYNODIC_MONTH).toInt()
        while (getNewMoonDay(k + 1, timeZone) <= dayNumber) k++
        while (getNewMoonDay(k, timeZone) > dayNumber) k--
        val monthStart = getNewMoonDay(k, timeZone)
        var a11 = getLunarMonth11(year, timeZone)
        var b11 = a11
        var lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = year
            a11 = getLunarMonth11(year - 1, timeZone)
        } else {
            lunarYear = year + 1
            b11 = getLunarMonth11(year + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = (monthStart - a11).floorDiv(29)
        var isLeap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) isLeap = true
            }
        }
        if (lunarMonth > 12) lunarMonth -= 12
        if (lunarMonth >= 11 && diff < 4) lunarYear -= 1
        return LunarDate(lunarDay, lunarMonth, lunarYear, isLeap)
    }

    fun solarToLunar(date: LocalDate): LunarDate =
        solarToLunar(date.dayOfMonth, date.monthValue, date.year)

    /** Returns null when [isLeapMonth] is set but the given month is not that year's leap month. */
    fun lunarToSolar(
        day: Int,
        month: Int,
        year: Int,
        isLeapMonth: Boolean = false,
        timeZone: Double = VN_TIME_ZONE,
    ): SolarDate? {
        val a11: Int
        val b11: Int
        if (month < 11) {
            a11 = getLunarMonth11(year - 1, timeZone)
            b11 = getLunarMonth11(year, timeZone)
        } else {
            a11 = getLunarMonth11(year, timeZone)
            b11 = getLunarMonth11(year + 1, timeZone)
        }
        val k = floor(0.5 + (a11 - NEW_MOON_EPOCH) / SYNODIC_MONTH).toInt()
        var off = month - 11
        if (off < 0) off += 12
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11, timeZone)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) leapMonth += 12
            if (isLeapMonth && month != leapMonth) return null
            if (isLeapMonth || off >= leapOff) off += 1
        } else if (isLeapMonth) {
            return null
        }
        val monthStart = getNewMoonDay(k + off, timeZone)
        return jdToDate(monthStart + day - 1)
    }

    /** Leap month number of the given lunar year, or 0 if the year has none. */
    fun leapMonthOf(lunarYear: Int, timeZone: Double = VN_TIME_ZONE): Int {
        // Leap months 1..10 fall between month 11 of the previous year and month 11 of this year.
        val a11 = getLunarMonth11(lunarYear - 1, timeZone)
        val b11 = getLunarMonth11(lunarYear, timeZone)
        if (b11 - a11 > 365) {
            val leapMonth = getLeapMonthOffset(a11, timeZone) - 2
            if (leapMonth in 1..10) return leapMonth
        }
        // Leap months 11..12 fall between month 11 of this year and month 11 of the next.
        val c11 = getLunarMonth11(lunarYear + 1, timeZone)
        if (c11 - b11 > 365) {
            val leapMonth = getLeapMonthOffset(b11, timeZone) - 2
            if (leapMonth <= 0) return leapMonth + 12
        }
        return 0
    }

    // --- Can Chi (sexagenary cycle), index 0 = Giáp / Tý ---

    fun canChiOfYear(lunarYear: Int): CanChi =
        CanChi(Can.values()[(lunarYear + 6).mod(10)], Chi.values()[(lunarYear + 8).mod(12)])

    /** A leap month shares its host month's Can Chi. */
    fun canChiOfMonth(lunarMonth: Int, lunarYear: Int): CanChi =
        CanChi(
            Can.values()[(lunarYear * 12 + lunarMonth + 3).mod(10)],
            Chi.values()[(lunarMonth + 1).mod(12)],
        )

    fun canChiOfDay(day: Int, month: Int, year: Int): CanChi {
        val jd = jdFromDate(day, month, year)
        return CanChi(Can.values()[(jd + 9).mod(10)], Chi.values()[(jd + 1).mod(12)])
    }

    // --- Moon phase ---

    fun moonPhase(day: Int, month: Int, year: Int, timeZone: Double = VN_TIME_ZONE): MoonPhase {
        val jdNoonLocal = jdFromDate(day, month, year) - timeZone / 24.0
        var k = floor((jdNoonLocal - NEW_MOON_EPOCH) / SYNODIC_MONTH).toInt()
        while (newMoon(k + 1) <= jdNoonLocal) k++
        while (newMoon(k) > jdNoonLocal) k--
        val age = jdNoonLocal - newMoon(k)
        return MoonPhase.values()[(age / SYNODIC_MONTH * 8).roundToInt() % 8]
    }

    // --- Good/bad days (ngày hoàng đạo / hắc đạo), 12-star table ---

    private val DAY_STARS = listOf(
        DayStar("Thanh Long", DayQuality.HOANG_DAO),
        DayStar("Minh Đường", DayQuality.HOANG_DAO),
        DayStar("Thiên Hình", DayQuality.HAC_DAO),
        DayStar("Chu Tước", DayQuality.HAC_DAO),
        DayStar("Kim Quỹ", DayQuality.HOANG_DAO),
        DayStar("Kim Đường", DayQuality.HOANG_DAO),
        DayStar("Bạch Hổ", DayQuality.HAC_DAO),
        DayStar("Ngọc Đường", DayQuality.HOANG_DAO),
        DayStar("Thiên Lao", DayQuality.HAC_DAO),
        DayStar("Huyền Vũ", DayQuality.HAC_DAO),
        DayStar("Tư Mệnh", DayQuality.HOANG_DAO),
        DayStar("Câu Trận", DayQuality.HAC_DAO),
    )

    fun dayStar(lunarMonth: Int, dayChi: Chi): DayStar {
        val startChi = ((lunarMonth - 1) % 6) * 2 // months 1&7 start Thanh Long at Tý, 2&8 at Dần, ...
        return DAY_STARS[(dayChi.ordinal - startChi + 12) % 12]
    }

    fun dayQuality(day: Int, month: Int, year: Int): DayQuality {
        val lunar = solarToLunar(day, month, year)
        val chi = canChiOfDay(day, month, year).chi
        return dayStar(lunar.month, chi).quality
    }
}
