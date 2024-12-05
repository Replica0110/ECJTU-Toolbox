package com.lonx.ecjtutoolbox.utils

import android.net.LinkAddress
import androidx.annotation.IntRange
import com.lonx.ecjtutoolbox.extension.fetchFromUrl
import okhttp3.OkHttpClient
import okio.IOException
import slimber.log.i

internal data class IPAddress(
    @IntRange(from = 0, to = 128)
    val prefixLength: Int,
    private val hostAddress: String?,
    val isLinkLocal: Boolean,
    val isSiteLocal: Boolean,
    val isAnyLocal: Boolean,
    val isLoopback: Boolean,
    val isMulticast: Boolean
) {
    constructor(linkAddress: LinkAddress) : this(
        prefixLength = linkAddress.prefixLength,
        hostAddress = linkAddress.address.hostAddress,
        isLinkLocal = linkAddress.address.isLinkLocalAddress,
        isSiteLocal = linkAddress.address.isSiteLocalAddress,
        isAnyLocal = linkAddress.address.isAnyLocalAddress,
        isLoopback = linkAddress.address.isLoopbackAddress,
        isMulticast = linkAddress.address.isMulticastAddress
    )

    /**
     * @see <a href="https://stackoverflow.com/a/33094601/12083276">SO reference</a>
     */
    val subnetMask by lazy {
        if (prefixLength > 32) {
            throw IllegalArgumentException("Attempting to create a subnetMask for an IPAddress with prefixLength > 32 $this")
        }
        val shift = 0xffffffff shl (32 - prefixLength)
        "${((shift and 0xff000000) shr 24) and 0xff}" +
            ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
            ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
            ".${(shift and 0x000000ff) and 0xff}"
    }

    val version: Version = if (prefixLength < Version.V6.minPrefixLength) Version.V4 else Version.V6

    val isV4: Boolean get() = version == Version.V4
    val isV6: Boolean get() = version == Version.V6

    val hostAddressRepresentation: String = hostAddress ?: version.fallbackAddress

    val isUniqueLocal: Boolean
        get() = ulaIdentificationRegex.matches(hostAddressRepresentation.substring(0, 2))

    val isGlobalUnicast: Boolean
        get() = !isLocal && !isMulticast

    private val isLocal: Boolean
        get() = isSiteLocal || isLinkLocal || isAnyLocal || isUniqueLocal

    enum class Version(
        val minPrefixLength: Int,
        val fallbackAddress: String,
        val ofCorrectFormat: (String) -> Boolean
    ) {
        V4(0, "0.0.0.0", { it.removeAlphanumeric() == "..." }),
        V6(64, ":::::::", { it.removeAlphanumeric() == ":::::::" })
    }

    companion object {
        suspend fun fetchPublic(httpClient: OkHttpClient, version: Version): Result<IPAddress> {
            i { "Fetching public $version address" }
            return httpClient.fetchFromUrl(
                when (version) {
                    Version.V4 -> "https://api64.ipify.org"
                    Version.V6 -> "https://api64.ipify.org"
                }
            ) { address ->
                if (version.ofCorrectFormat(address)) {
                    IPAddress(
                        prefixLength = version.minPrefixLength,
                        hostAddress = address.log { "Got public $version address $it" },
                        isLinkLocal = false,
                        isSiteLocal = false,
                        isAnyLocal = false,
                        isLoopback = false,
                        isMulticast = false
                    )
                } else {
                    throw IOException("Obtained $version address $address of incorrect format")
                }
            }
        }
    }
}
private val ulaIdentificationRegex = Regex("^(fc|fd)")

fun String.removeAlphanumeric(): String =
    replace(Regex("\\w"), "")

