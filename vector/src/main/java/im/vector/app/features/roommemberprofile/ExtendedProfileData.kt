/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package im.vector.app.features.roommemberprofile

/**
 * Extended profile data for healthcare professionals.
 * These fields are fetched from the Matrix profile API with custom keys.
 */
data class ExtendedProfileData(
        val title: String? = null,
        val specialization: String? = null,
        val practiceName: String? = null,
        val businessStreet: String? = null,
        val businessStreetNr: String? = null,
        val businessPlz: String? = null,
        val businessCity: String? = null,
        val businessEmail: String? = null,
        val businessTel: String? = null,
        val website: String? = null,
        val furtherInfo: String? = null
) {
    companion object {
        const val KEY_TITLE = "io.element.profile.title"
        const val KEY_SPECIALIZATION = "io.element.profile.specialization"
        const val KEY_PRACTICE_NAME = "io.element.profile.practice_name"
        const val KEY_BUSINESS_STREET = "io.element.profile.business_street"
        const val KEY_BUSINESS_STREET_NR = "io.element.profile.business_street_nr"
        const val KEY_BUSINESS_PLZ = "io.element.profile.business_plz"
        const val KEY_BUSINESS_CITY = "io.element.profile.business_city"
        const val KEY_BUSINESS_EMAIL = "io.element.profile.business_email"
        const val KEY_BUSINESS_TEL = "io.element.profile.business_tel"
        const val KEY_WEBSITE = "io.element.profile.website"
        const val KEY_FURTHER_INFO = "io.element.profile.further_info"

        fun fromProfileJson(json: Map<String, Any?>): ExtendedProfileData {
            return ExtendedProfileData(
                    title = json[KEY_TITLE] as? String,
                    specialization = json[KEY_SPECIALIZATION] as? String,
                    practiceName = json[KEY_PRACTICE_NAME] as? String,
                    businessStreet = json[KEY_BUSINESS_STREET] as? String,
                    businessStreetNr = json[KEY_BUSINESS_STREET_NR] as? String,
                    businessPlz = json[KEY_BUSINESS_PLZ] as? String,
                    businessCity = json[KEY_BUSINESS_CITY] as? String,
                    businessEmail = json[KEY_BUSINESS_EMAIL] as? String,
                    businessTel = json[KEY_BUSINESS_TEL] as? String,
                    website = json[KEY_WEBSITE] as? String,
                    furtherInfo = json[KEY_FURTHER_INFO] as? String
            )
        }
    }

    fun hasAnyData(): Boolean = listOfNotNull(
            title, specialization, practiceName, businessStreet,
            businessStreetNr, businessPlz, businessCity, businessEmail,
            businessTel, website, furtherInfo
    ).isNotEmpty()

    fun formattedAddress(): String? {
        val streetPart = listOfNotNull(businessStreet, businessStreetNr)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" ")
        val cityPart = listOfNotNull(businessPlz, businessCity)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" ")
        return listOfNotNull(streetPart, cityPart)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
    }
}
