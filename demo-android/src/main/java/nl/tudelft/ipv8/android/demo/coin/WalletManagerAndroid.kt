package nl.tudelft.ipv8.android.demo.coin

import android.app.Application
import android.content.Context
import android.os.Handler
import org.bitcoinj.utils.Threading
import java.util.concurrent.Executor

/**
 * Singleton class for WalletManager which also sets-up Android specific things.
 */
// TODO: Clean up Thread usage.
object WalletManagerAndroid {
    private var walletManager: WalletManager? = null
    private var application: Application? = null

    fun getInstance(): WalletManager {
        return walletManager
            ?: throw IllegalStateException("WalletManager is not initialized")
    }

    class Factory(
        private val application: Application
    ) {
        private var configuration: WalletManagerConfiguration? = null

        fun setConfiguration(configuration: WalletManagerConfiguration): Factory {
            this.configuration = configuration
            return this
        }

        fun init(): WalletManager {
            val walletDir = application.applicationContext.filesDir
            val configuration = configuration
                ?: throw IllegalStateException("Configuration is not set")

            WalletManagerAndroid.application = application

            val walletManager = WalletManager(configuration, walletDir)
//            setupThread(application.applicationContext)

            WalletManagerAndroid.walletManager = walletManager

            return walletManager
        }

    }

    val runInUIThread: Executor = object : Executor {
        override fun execute(runnable: Runnable) {
            val handler = Handler(application?.applicationContext?.mainLooper)
            handler.post(runnable)
        }
    }

    /**
     * Sets up in which thread BitcoinJ will conduct its background activities.
     */
    fun setupThread(applicationContext: Context) {
        val runInUIThread: Executor = Executor { runnable ->
            val handler = Handler(applicationContext.mainLooper)
            handler.post(runnable)
        }

        Threading.USER_THREAD = runInUIThread
    }
}
