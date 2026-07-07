package com.uynne.lunarcalendar.lunar

import java.time.LocalDate

data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean = false,
)

data class SolarDate(val day: Int, val month: Int, val year: Int) {
    fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)
}

enum class Can(val vn: String) {
    GIAP("Giáp"), AT("Ất"), BINH("Bính"), DINH("Đinh"), MAU("Mậu"),
    KY("Kỷ"), CANH("Canh"), TAN("Tân"), NHAM("Nhâm"), QUY("Quý"),
}

enum class Chi(val vn: String) {
    TY("Tý"), SUU("Sửu"), DAN("Dần"), MAO("Mão"), THIN("Thìn"), TI("Tỵ"),
    NGO("Ngọ"), MUI("Mùi"), THAN("Thân"), DAU("Dậu"), TUAT("Tuất"), HOI("Hợi"),
}

data class CanChi(val can: Can, val chi: Chi) {
    val display: String get() = "${can.vn} ${chi.vn}"
}

enum class MoonPhase(val vn: String) {
    NEW_MOON("Trăng non"),
    WAXING_CRESCENT("Trăng lưỡi liềm đầu tháng"),
    FIRST_QUARTER("Trăng thượng huyền"),
    WAXING_GIBBOUS("Trăng khuyết đầu tháng"),
    FULL_MOON("Trăng tròn"),
    WANING_GIBBOUS("Trăng khuyết cuối tháng"),
    LAST_QUARTER("Trăng hạ huyền"),
    WANING_CRESCENT("Trăng lưỡi liềm cuối tháng"),
}

enum class DayQuality { HOANG_DAO, HAC_DAO, NEUTRAL }

data class DayStar(val name: String, val quality: DayQuality)
