package com.makn.footballquiz.monetization

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.makn.footballquiz.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * All ad decisions in one place. Ads only load once consent allows it and never for players who
 * bought "Remove ads". Full-screen ads are rationed the way well-reviewed quiz apps do it:
 * never during a question, only between rounds, at most every [ROUNDS_PER_INTERSTITIAL] rounds
 * and never twice within [MIN_INTERSTITIAL_GAP_MS].
 */
class AdsManager(
    private val context: Context,
    consent: ConsentManager,
    billing: BillingManager,
    scope: CoroutineScope,
) {
    /** Banners and interstitials are allowed right now. */
    val adsEnabled: StateFlow<Boolean> = combine(consent.canRequestAds, billing.state) { canRequest, purchase ->
        canRequest && !purchase.adFree
    }.stateIn(scope, SharingStarted.Eagerly, false)

    /** Rewarded ads also need consent, but stay available to ad-free players as an optional choice. */
    private val canRequestAds = consent.canRequestAds
    private val isAdFree: () -> Boolean = { billing.state.value.adFree }

    private val _rewardedReady = MutableStateFlow(false)
    val rewardedReady: StateFlow<Boolean> = _rewardedReady.asStateFlow()

    private var initialized = false
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private var loadingInterstitial = false
    private var loadingRewarded = false
    private var roundsSinceInterstitial = 0
    private var lastInterstitialAt = 0L

    /** Call after consent has been gathered; safe to call repeatedly. */
    fun initializeIfAllowed() {
        if (initialized || !canRequestAds.value) return
        initialized = true
        MobileAds.initialize(context) { preload() }
    }

    private fun preload() {
        if (!isAdFree()) loadInterstitial()
        loadRewarded()
    }

    private fun loadInterstitial() {
        if (!initialized || interstitial != null || loadingInterstitial || !adsEnabled.value) return
        loadingInterstitial = true
        InterstitialAd.load(context, BuildConfig.ADMOB_INTERSTITIAL_ID, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
                loadingInterstitial = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loadingInterstitial = false
            }
        })
    }

    private fun loadRewarded() {
        if (!initialized || rewarded != null || loadingRewarded || !canRequestAds.value) return
        loadingRewarded = true
        RewardedAd.load(context, BuildConfig.ADMOB_REWARDED_ID, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewarded = ad
                loadingRewarded = false
                _rewardedReady.value = true
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loadingRewarded = false
                _rewardedReady.value = false
            }
        })
    }

    /** A round reached its results screen. */
    fun onRoundCompleted() {
        roundsSinceInterstitial++
        loadInterstitial()
    }

    /**
     * Shows an interstitial if one is due and ready, then runs [onContinue]; otherwise runs it
     * straight away. Used when leaving the results screen — never mid-round.
     */
    fun showInterstitialIfDue(activity: Activity, onContinue: () -> Unit) {
        val ad = interstitial
        val due = roundsSinceInterstitial >= ROUNDS_PER_INTERSTITIAL &&
            SystemClock.elapsedRealtime() - lastInterstitialAt >= MIN_INTERSTITIAL_GAP_MS
        if (ad == null || !due || !adsEnabled.value) {
            onContinue()
            return
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onContinue()
                loadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                onContinue()
                loadInterstitial()
            }
        }
        roundsSinceInterstitial = 0
        lastInterstitialAt = SystemClock.elapsedRealtime()
        ad.show(activity)
    }

    /** Plays a rewarded ad; [onRewarded] only runs if the player watched it to the end. */
    fun showRewarded(activity: Activity, onRewarded: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewarded ?: run {
            onUnavailable()
            loadRewarded()
            return
        }
        rewarded = null
        _rewardedReady.value = false
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                if (earned) onRewarded()
                loadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                onUnavailable()
                loadRewarded()
            }
        }
        ad.show(activity) { earned = true }
    }

    private companion object {
        const val ROUNDS_PER_INTERSTITIAL = 3
        const val MIN_INTERSTITIAL_GAP_MS = 3 * 60 * 1000L
    }
}
