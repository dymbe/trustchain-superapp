package nl.tudelft.trustchain.currencyii.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.currencyii.CoinCommunity
import nl.tudelft.trustchain.common.DemoCommunity
import nl.tudelft.trustchain.currencyii.TrustChainHelper
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity

abstract class BaseFragment(@LayoutRes contentLayoutId: Int = 0) : Fragment(contentLayoutId) {
    protected val trustchain: TrustChainHelper by lazy {
        TrustChainHelper(getTrustChainCommunity())
    }

    protected fun getIpv8(): IPv8 {
        return IPv8Android.getInstance()
    }

    protected fun getTrustChainCommunity(): TrustChainCommunity {
        return getIpv8().getOverlay() ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }

    protected fun getDemoCommunity(): DemoCommunity {
        return getIpv8().getOverlay() ?: throw IllegalStateException("DemoCommunity is not configured")
    }

    protected fun getCoinCommunity(): CoinCommunity {
        return getIpv8().getOverlay() ?: throw IllegalStateException("CoinCommunity is not configured")
    }
}
