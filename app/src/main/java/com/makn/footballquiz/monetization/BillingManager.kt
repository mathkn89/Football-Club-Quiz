package com.makn.footballquiz.monetization

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.makn.footballquiz.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val Context.entitlementStore by preferencesDataStore(name = "entitlements")

/**
 * The one-time "Remove ads" purchase (Google Play Billing). Ownership is cached locally so ads
 * never flash on launch for paying players, and re-checked with Play on every start — which also
 * restores the purchase after a reinstall or on a new phone.
 */
class BillingManager(private val context: Context, private val scope: CoroutineScope) : PurchasesUpdatedListener {

    data class State(
        val adFree: Boolean = false,
        /** Localised price from Play, e.g. "29,00 kr"; null until the product is found. */
        val price: String? = null,
        /** False until Play returns the product (e.g. before the app is set up in Play Console). */
        val purchaseAvailable: Boolean = false,
        val pending: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val adFreeKey = booleanPreferencesKey("ad_free")
    private val debugAdFreeKey = booleanPreferencesKey("debug_ad_free")
    private var productDetails: ProductDetails? = null

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun start() {
        scope.launch {
            val cached = context.entitlementStore.data.map { it[adFreeKey] == true || it[debugAdFreeKey] == true }.first()
            _state.update { it.copy(adFree = cached) }
        }
        connect()
    }

    private fun connect() {
        if (client.isReady) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) refresh()
            }

            override fun onBillingServiceDisconnected() = Unit // reconnect on next start/restore
        })
    }

    /** Re-checks the product and ownership with Play ("Restore purchase"). */
    fun refresh() {
        if (!client.isReady) {
            connect()
            return
        }
        scope.launch {
            val details = client.queryProductDetails(
                QueryProductDetailsParams.newBuilder().setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(BuildConfig.REMOVE_ADS_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    ),
                ).build(),
            ).productDetailsList?.firstOrNull()
            productDetails = details
            _state.update {
                it.copy(price = details?.oneTimePurchaseOfferDetails?.formattedPrice, purchaseAvailable = details != null)
            }

            val owned = client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            )
            if (owned.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(owned.purchasesList, authoritative = true)
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails ?: return
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()),
        ).build()
        client.launchBillingFlow(activity, params)
    }

    /** Debug builds only: grants or removes ad-free locally, since real purchases need Play Console. */
    fun setDebugAdFree(adFree: Boolean) {
        if (!BuildConfig.DEBUG) return
        scope.launch {
            context.entitlementStore.edit { it[debugAdFreeKey] = adFree }
            _state.update { it.copy(adFree = adFree || isOwnedOnPlay) }
        }
    }

    private var isOwnedOnPlay = false

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { handlePurchases(purchases, authoritative = false) }
        } else if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            refresh()
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>, authoritative: Boolean) {
        val ours = purchases.filter { BuildConfig.REMOVE_ADS_PRODUCT_ID in it.products }
        val purchased = ours.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        purchased.filterNot { it.isAcknowledged }.forEach { purchase ->
            // Unacknowledged purchases are refunded by Play after three days.
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build())
        }
        val pending = ours.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        if (purchased.isNotEmpty() || authoritative) {
            isOwnedOnPlay = purchased.isNotEmpty()
            val debugAdFree = context.entitlementStore.data.map { it[debugAdFreeKey] == true }.first()
            context.entitlementStore.edit { it[adFreeKey] = isOwnedOnPlay }
            _state.update { it.copy(adFree = isOwnedOnPlay || debugAdFree, pending = pending && !isOwnedOnPlay) }
        } else {
            _state.update { it.copy(pending = pending) }
        }
    }
}
