package com.makn.footballquiz.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.makn.footballquiz.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google's User Messaging Platform: shows the GDPR/UK consent form where required (EEA, UK,
 * Switzerland) before any ad is requested, and offers "Privacy settings" to change it later.
 */
class ConsentManager(context: Context) {

    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    private val _canRequestAds = MutableStateFlow(consentInformation.canRequestAds())
    /** True once consent (or "not required") allows ad requests; may already be true from a previous run. */
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(isPrivacyOptionsRequired())
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    /** Refreshes consent status and shows the form if needed; [onDone] runs either way. */
    fun gather(activity: Activity, onDone: () -> Unit) {
        val params = ConsentRequestParameters.Builder().apply {
            if (BuildConfig.DEBUG) {
                // Debug builds behave as if in the EEA so the form can be tested anywhere
                // (applies to emulators and registered test devices only).
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .build(),
                )
            }
        }.build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                log("status=${consentInformation.consentStatus} formAvailable=${consentInformation.isConsentFormAvailable}")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    error?.let { log("form error ${it.errorCode}: ${it.message}") }
                    refresh()
                    onDone()
                }
            },
            { error ->
                // Offline or misconfigured: fall back to whatever consent we already had.
                log("update error ${error.errorCode}: ${error.message}")
                refresh()
                onDone()
            },
        )
    }

    fun showPrivacyOptions(activity: Activity, onDone: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            refresh()
            onDone()
        }
    }

    private fun log(message: String) {
        if (BuildConfig.DEBUG) Log.d("Consent", message)
    }

    private fun refresh() {
        _canRequestAds.value = consentInformation.canRequestAds()
        _privacyOptionsRequired.value = isPrivacyOptionsRequired()
    }

    private fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
}
