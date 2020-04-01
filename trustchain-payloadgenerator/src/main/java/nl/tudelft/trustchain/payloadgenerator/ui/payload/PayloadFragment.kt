package nl.tudelft.trustchain.payloadgenerator.ui.payload

import nl.tudelft.trustchain.common.ui.BaseFragment
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mattskala.itemadapter.ItemAdapter
import kotlinx.android.synthetic.main.fragment_payload.*
import kotlinx.coroutines.*
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.trustchain.common.constants.Currency
import nl.tudelft.trustchain.common.messaging.TradePayload
import nl.tudelft.trustchain.common.util.viewBinding
import nl.tudelft.trustchain.payloadgenerator.R
import nl.tudelft.trustchain.payloadgenerator.databinding.FragmentPayloadBinding
import androidx.navigation.fragment.findNavController
import nl.tudelft.trustchain.payloadgenerator.ui.TrustChainPayloadGeneratorActivity

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [PayloadFragment.OnListFragmentInteractionListener] interface.
 */
class PayloadFragment : BaseFragment(R.layout.fragment_payload) {

    private val adapter = ItemAdapter()

    private lateinit var publicKey: ByteArray

    protected val binding: FragmentPayloadBinding by viewBinding(FragmentPayloadBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.registerRenderer(PayloadItemRenderer {

            trustchain.createTxProposalBlock(it.availableAmount?.toFloat(), it.publicKey)
            Log.d("PayloadFragment::onCreate","TX block send to: ${it.publicKey}!")
        })
    }

    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewPayload.adapter = adapter
        binding.recyclerViewPayload.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPayload.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayout.VERTICAL
            )
        )

        val marketCommunity = getMarketCommunity()
        marketCommunity.addListener(TradePayload.Type.ASK, ::askListener)
        marketCommunity.addListener(TradePayload.Type.BID, ::bidListener)

        var availableAmount = arguments?.getDouble("available amount")
        var requiredAmount = arguments?.getDouble("required amount")
        val type = arguments?.getString("type")
        if (availableAmount == null) {
            availableAmount = 0.0
        }
        if (requiredAmount == null) {
            requiredAmount = 0.0
        }
        if (availableAmount != 0.0 && requiredAmount !=0.0) {
            var payload_serializable =
                createPayloadSerializable(availableAmount, requiredAmount, type)
            var payload = createPayload(availableAmount, requiredAmount, type)
            (TrustChainPayloadGeneratorActivity.PayloadsList).payloads.add(payload)
            marketCommunity.broadcast(payload_serializable)
        }
        val trustchain = getTrustChainCommunity()

        buttonPayload.setOnClickListener {
            view.findNavController().navigate(R.id.action_payloadFragment_to_payloadCreateFragment)
        }

        loadCurrentPayloads((TrustChainPayloadGeneratorActivity.PayloadsList).payloads)
    }


    private fun createPayload(
        availableAmount: Double,
        requiredAmount: Double,
        type: String?
    ): TradePayload {
        var primaryCurrency = Currency.DYMBE_DOLLAR
        var secondaryCurrency = Currency.BTC
        var type2 = TradePayload.Type.BID
        if (type.equals("Ask")) {
            primaryCurrency = Currency.BTC
            secondaryCurrency = Currency.DYMBE_DOLLAR
            type2 = TradePayload.Type.ASK
        }
        return TradePayload(
            trustchain.getMyPublicKey(),
            primaryCurrency,
            secondaryCurrency,
            availableAmount,
            requiredAmount,
            type2
        )
    }

    private fun createPayloadSerializable(
        availableAmount: Double,
        requiredAmount: Double,
        type: String?
    ): Serializable {
        var primaryCurrency = Currency.DYMBE_DOLLAR
        var secondaryCurrency = Currency.BTC
        var type2 = TradePayload.Type.BID
        if (type.equals("Ask")) {
            primaryCurrency = Currency.BTC
            secondaryCurrency = Currency.DYMBE_DOLLAR
            type2 = TradePayload.Type.ASK
        }
        return TradePayload(
            trustchain.getMyPublicKey(),
            primaryCurrency,
            secondaryCurrency,
            availableAmount,
            requiredAmount,
            type2
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        val marketCommunity = getMarketCommunity()
        marketCommunity.removeListener(TradePayload.Type.ASK, ::askListener)
    }

    private fun askListener(payload: TradePayload) {
        Log.d(
            "PayloadFragment::onViewCreated",
            "New ask came in! They are selling ${payload.amount} ${payload.primaryCurrency}. The price is ${payload.price} ${payload.secondaryCurrency} per ${payload.primaryCurrency}"
        )
        (TrustChainPayloadGeneratorActivity.PayloadsList).payloads.add(payload)
    }
    private fun bidListener(payload: TradePayload) {
        Log.d(
            "PayloadFragment::onViewCreated",
            "New bid came in! They are asking ${payload.amount} ${payload.primaryCurrency}. The price is ${payload.price} ${payload.secondaryCurrency} per ${payload.primaryCurrency}"
        )
        if(!(TrustChainPayloadGeneratorActivity.PayloadsList).payloads.contains(payload)) {
            (TrustChainPayloadGeneratorActivity.PayloadsList).payloads.add(payload)
        }
    }

    private fun loadCurrentPayloads(payloads: List<TradePayload>) {
        lifecycleScope.launchWhenStarted {
            while (isActive) {
                val items = withContext(Dispatchers.IO) {
                    payloads.map {
                        PayloadItem(
                            it.publicKey,
                            it.primaryCurrency,
                            it.secondaryCurrency,
                            it.amount,
                            it.price,
                            it.type
                        )
                    }
                }
                adapter.updateItems(items)

                binding.imgEmpty.isVisible = items.isEmpty()

                delay(1000)
            }
        }
    }
}


