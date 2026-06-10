package com.weatherboi

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.sin

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@Serializable
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

@Serializable
data class WeatherResponse(
    val current: CurrentWeather,
    val daily: DailyForecast
)

@Serializable
data class CurrentWeather(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Double,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("is_day") val isDay: Int,
    val precipitation: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double
)

@Serializable
data class DailyForecast(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val tempMax: List<Double>,
    @SerialName("temperature_2m_min") val tempMin: List<Double>
)

data class WeatherInfo(
    val description: String,
    val icon: String,
    val asciiArt: List<String>,
    val colorCode: String
)

val jsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(jsonConfig)
    }
}

fun getWeatherInfo(code: Int, isDay: Boolean): WeatherInfo {
    val yellow = "\u001B[1;33m"
    val blue = "\u001B[1;34m"
    val cyan = "\u001B[1;36m"
    val white = "\u001B[1;37m"
    val gray = "\u001B[1;90m"
    val purple = "\u001B[1;35m"

    val sunnyAscii = listOf(
        "    \\   /     ",
        "     .-.      ",
        "   --(   )--  ",
        "     `-'      ",
        "    /   \\     "
    )
    val moonAscii = listOf(
        "   .-.         ",
        "  /   \\        ",
        "  \\    `-.    ",
        "   \\      )   ",
        "    `----'     "
    )
    val cloudyAscii = listOf(
        "              ",
        "     .--.     ",
        "  .-(    )--. ",
        " (           )",
        "  `---------' "
    )
    val rainAscii = listOf(
        "     .--.     ",
        "  .-(    )--. ",
        " (           )",
        "  `---------' ",
        "  ` ` ` ` ` ` "
    )
    val snowAscii = listOf(
        "     .--.     ",
        "  .-(    )--. ",
        " (           )",
        "  `---------' ",
        "  *  *  *  *  "
    )
    val stormAscii = listOf(
        "     .--.     ",
        "  .-(    )--. ",
        " (           )",
        "  `---------' ",
        "     !   !    "
    )
    val fogAscii = listOf(
        "  = = = = = = ",
        "   = = = = =  ",
        "  = = = = = = ",
        "   = = = = =  ",
        "  = = = = = = "
    )

    return when (code) {
        0 -> if (isDay) {
            WeatherInfo("Clear Sky", "(sun)", sunnyAscii, yellow)
        } else {
            WeatherInfo("Clear Night", "(moon)", moonAscii, blue)
        }
        1, 2 -> if (isDay) {
            WeatherInfo("Partly Cloudy", "(cloud)", listOf(
                "   \\  /      ",
                "  _ /\"\"\\     ",
                "   (_  _)-.  ",
                "  (        ) ",
                "   `------'  "
            ), yellow)
        } else {
            WeatherInfo("Partly Cloudy", "(cloud)", listOf(
                "   .-.       ",
                "  /   \\.-.   ",
                "  \\  (_ _)-. ",
                "   (        )",
                "    `------' "
            ), blue)
        }
        3 -> WeatherInfo("Overcast", "(cloud)", cloudyAscii, gray)
        45, 48 -> WeatherInfo("Foggy", "(fog)", fogAscii, gray)
        51, 53, 55 -> WeatherInfo("Drizzle", "(rain)", rainAscii, cyan)
        56, 57 -> WeatherInfo("Freezing Drizzle", "(snow)", snowAscii, white)
        61, 63 -> WeatherInfo("Light Rain", "(rain)", rainAscii, cyan)
        65 -> WeatherInfo("Heavy Rain", "(rain)", rainAscii, cyan)
        66, 67 -> WeatherInfo("Freezing Rain", "(snow)", snowAscii, white)
        71, 73 -> WeatherInfo("Light Snow", "(snow)", snowAscii, white)
        75 -> WeatherInfo("Heavy Snow", "(snow)", snowAscii, white)
        77 -> WeatherInfo("Snow Grains", "(snow)", snowAscii, white)
        80, 81, 82 -> WeatherInfo("Showers", "(rain)", rainAscii, cyan)
        85, 86 -> WeatherInfo("Snow Showers", "(snow)", snowAscii, white)
        95, 96, 99 -> WeatherInfo("Thunderstorm", "(storm)", stormAscii, purple)
        else -> WeatherInfo("Unknown", "(?)", cloudyAscii, gray)
    }
}

suspend fun geocode(city: String): GeocodingResult? {
    val encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString())
    val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json"
    return try {
        val response: GeocodingResponse = client.get(url).body()
        response.results?.firstOrNull()
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse? {
    val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
              "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m" +
              "&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
    return try {
        client.get(url).body()
    } catch (e: Exception) {
        null
    }
}

suspend fun <T> withSpinner(message: String, block: suspend () -> T): T {
    val frames = listOf("-", "\\", "|", "/")
    var frameIndex = 0
    val job = CoroutineScope(Dispatchers.Default).launch {
        while (isActive) {
            print("\r\u001B[1;36m${frames[frameIndex]}\u001B[0m $message")
            frameIndex = (frameIndex + 1) % frames.size
            delay(120)
        }
    }
    try {
        return block()
    } finally {
        job.cancelAndJoin()
        print("\r\u001B[K") // Clear the line
    }
}

fun renderWeatherCard(city: String, country: String?, state: String?, weather: WeatherResponse) {
    val reset = "\u001B[0m"
    val bold = "\u001B[1m"
    val cyan = "\u001B[1;36m"

    val current = weather.current
    val isDay = current.isDay == 1
    val info = getWeatherInfo(current.weatherCode, isDay)

    val locationName = buildString {
        append(city)
        if (!state.isNullOrEmpty()) append(", $state")
        if (!country.isNullOrEmpty()) append(", $country")
    }.uppercase()

    val ascii = info.asciiArt
    val color = info.colorCode

    // Build the weather data lines (no ANSI in length calc)
    val rightSideRaw = listOf(
        locationName,
        "${info.icon}  ${info.description}",
        "Temp:        ${current.temperature} C  (Feels like ${current.apparentTemperature} C)",
        "Humidity:    ${current.humidity} %",
        "Wind Speed:  ${current.windSpeed} km/h",
        "Precip:      ${current.precipitation} mm"
    )
    val rightSideStyled = listOf(
        "$bold$locationName$reset",
        "$bold$color${info.icon}  ${info.description}$reset",
        "Temp:        $bold${current.temperature} C$reset  (Feels like ${current.apparentTemperature} C)",
        "Humidity:    $bold${current.humidity} %$reset",
        "Wind Speed:  $bold${current.windSpeed} km/h$reset",
        "Precip:      $bold${current.precipitation} mm$reset"
    )

    val padding = 2
    val maxRightLength = rightSideRaw.maxOf { it.length }
    val asciiWidth = ascii.firstOrNull()?.length ?: 0
    // innerWidth = left border + padding + ascii + separator + right text + padding + right border = total visible width of box
    val innerWidth = asciiWidth + 3 + maxRightLength + (padding * 2)
    val boxWidth = innerWidth + 2 // +2 for the two border chars

    val horizontal = "-".repeat(innerWidth)

    println()
    println("$cyan+$horizontal+$reset")

    val maxLines = maxOf(ascii.size, rightSideStyled.size)
    for (i in 0 until maxLines) {
        val asciiLine = ascii.getOrElse(i) { " ".repeat(asciiWidth) }
        val rightLine = rightSideStyled.getOrElse(i) { "" }

        val coloredAscii = "$color$asciiLine$reset"
        val separator = " | "

        // Pad right side to maxRightLength using plain text length
        val plainRight = rightSideRaw.getOrElse(i) { "" }
        val spacesNeeded = maxRightLength - plainRight.length
        val paddedRightLine = rightLine + " ".repeat(spacesNeeded)

        val leftSpacing = " ".repeat(padding)
        val rightSpacing = " ".repeat(padding)

        println("$cyan|$reset$leftSpacing$coloredAscii$separator$paddedRightLine$rightSpacing$cyan|$reset")
    }

    // Render Forecast Section
    println("$cyan|$horizontal|$reset")
    val headerText = "3-DAY FORECAST"
    val headerPadding = (innerWidth - headerText.length) / 2
    val headerLine = " ".repeat(headerPadding) + "$bold$headerText$reset" + " ".repeat(innerWidth - headerPadding - headerText.length)
    println("$cyan|$reset$headerLine$cyan|$reset")
    println("$cyan|$horizontal|$reset")

    val daily = weather.daily
    val limit = minOf(3, daily.time.size)
    for (i in 0 until limit) {
        val dateStr = daily.time[i]
        val date = LocalDate.parse(dateStr)
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

        val code = daily.weatherCode[i]
        val fInfo = getWeatherInfo(code, true)

        val tempMin = daily.tempMin[i]
        val tempMax = daily.tempMax[i]

        val forecastPlain = "${fInfo.icon}  ${fInfo.description}"
        val forecastStyled = "${fInfo.colorCode}${fInfo.icon}  ${fInfo.description}$reset"
        val tempText = "Min: ${tempMin} C  Max: ${tempMax} C"

        // Build left part: "  Tue:  (rain)  Showers"
        val leftPlain = "  $dayOfWeek:  $forecastPlain"
        val leftStyled = "  $dayOfWeek:  $forecastStyled"

        // Calculate spaces between left and temp text
        val middleSpaces = innerWidth - leftPlain.length - tempText.length - 2 // -2 for trailing spaces after temp
        val finalLine = leftStyled + " ".repeat(maxOf(1, middleSpaces)) + tempText + "  "

        // Pad to exact innerWidth
        val printableLen = stripAnsi(finalLine).length
        val trailingSpaces = innerWidth - printableLen
        val outputLine = finalLine + " ".repeat(maxOf(0, trailingSpaces))

        println("$cyan|$reset$outputLine$cyan|$reset")
    }

    println("$cyan+$horizontal+$reset")
    println()
}

fun stripAnsi(str: String): String {
    return str.replace("\u001B\\[[;\\d]*[a-zA-Z]".toRegex(), "")
}

suspend fun fetchAndDisplayWeather(city: String) {
    val geocodeResult = withSpinner("Geocoding '$city'...") {
        geocode(city)
    }

    if (geocodeResult == null) {
        println("  \u001B[1;31m[!] Couldn't find that city, boi! Make sure the spelling is right.\u001B[0m")
        return
    }

    val weatherResult = withSpinner("Fetching weather for ${geocodeResult.name}...") {
        fetchWeather(geocodeResult.latitude, geocodeResult.longitude)
    }

    if (weatherResult == null) {
        println("  \u001B[1;31m[!] Couldn't fetch the weather data, boi! Check your internet connection.\u001B[0m")
        return
    }

    renderWeatherCard(
        city = geocodeResult.name,
        country = geocodeResult.country,
        state = geocodeResult.admin1,
        weather = weatherResult
    )
}

fun playPing() {
    try {
        val sampleRate = 8000f
        val af = AudioFormat(sampleRate, 8, 1, true, false)
        val line = AudioSystem.getSourceDataLine(af)
        line.open(af)
        line.start()
        val durationMs = 250
        val numSamples = (durationMs * sampleRate / 1000).toInt()
        val buf = ByteArray(numSamples)
        val frequency = 880.0 // A5 note (crystal-clear ping)
        for (i in 0 until numSamples) {
            val angle = i / (sampleRate / frequency) * 2.0 * Math.PI
            val fade = 1.0 - (i.toDouble() / numSamples)
            buf[i] = (sin(angle) * 127.0 * fade).toInt().toByte()
        }
        line.write(buf, 0, buf.size)
        line.drain()
        line.close()
    } catch (e: Exception) {
        print("\u0007")
        System.out.flush()
    }
}

fun main(args: Array<String>) = runBlocking {
    playPing()
    val bannerColor = "\u001B[1;36m"
    val bannerReset = "\u001B[0m"
    println("""
 __        _______    _  _____ _   _ _____ ____    ____   ___ ___ 
 \ \      / / ____|  / \|_   _| | | | ____|  _ \  | __ ) / _ \_ _|
  \ \ /\ / /|  _|   / _ \ | | | |_| |  _| | |_) | |  _ \| | | | | 
   \ V  V / | |___ / ___ \| | |  _  | |___|  _ <  | |_) | |_| | | 
    \_/\_/  |_____/_/   \_\_| |_| |_|_____|_| \_\ |____/ \___/___|
""".trimIndent())

    println("        \u001B[1;32mWelcome to Weather Boi - Your Aesthetic Weather Companion!\u001B[0m")
    println()

    if (args.isNotEmpty()) {
        val city = args.joinToString(" ")
        fetchAndDisplayWeather(city)
        client.close()
        return@runBlocking
    }

    while (true) {
        print("\u001B[1;35mWho's your city, boi? \u001B[0m(or type 'exit' to quit) \u001B[1;33m>\u001B[0m ")
        val input = readlnOrNull()?.trim()
        if (input.isNullOrEmpty() || input.equals("exit", ignoreCase = true)) {
            println("\u001B[1;36mGoodbye, boi!\u001B[0m")
            break
        }
        fetchAndDisplayWeather(input)
        println()
    }
    client.close()
}