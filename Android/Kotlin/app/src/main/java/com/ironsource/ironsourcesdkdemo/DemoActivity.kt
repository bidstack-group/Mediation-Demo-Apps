package com.ironsource.ironsourcesdkdemo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ironsource.adapters.supersonicads.SupersonicConfig
import com.ironsource.ironsourcesdkdemo.databinding.ActivityDemoBinding
import com.ironsource.mediationsdk.*
import com.ironsource.mediationsdk.impressionData.ImpressionData
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.model.Placement
import com.ironsource.mediationsdk.sdk.*
import com.ironsource.mediationsdk.utils.IronSourceUtils
import java.util.*

class DemoActivity : Activity(), RewardedVideoListener, ImpressionDataListener {

    companion object {
        const val APP_KEY = "1200b8e5d"
        const val TAG = "DemoActivity"
    }

    private lateinit var binding: ActivityDemoBinding

    private var mPlacement: Placement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //The integrationHelper is used to validate the integration. Remove the integrationHelper before going live!
        IntegrationHelper.validateIntegration(this)
        initUIElements()

        val advertisingId = IronSource.getAdvertiserId(this@DemoActivity)
        // we're using an advertisingId as the 'userId'
        initIronSource(advertisingId)

        //Network Connectivity Status
        IronSource.shouldTrackNetworkState(this, true)
    }



    private fun initIronSource(userId: String?) {
        // Be sure to set a listener to each product that is being initiated
        // set the IronSource rewarded video listener
        IronSource.setRewardedVideoListener(this)

        // set the IronSource user id
        IronSource.setUserId(userId)
        // init the IronSource SDK
        IronSource.init(this, APP_KEY)
        updateButtonsState()
    }

    override fun onResume() {
        super.onResume()
        // call the IronSource onResume method
        IronSource.onResume(this)
        updateButtonsState()
    }

    override fun onPause() {
        super.onPause()
        // call the IronSource onPause method
        IronSource.onPause(this)
        updateButtonsState()
    }

    /**
     * Handle the button state according to the status of the IronSource producs
     */
    private fun updateButtonsState() {
        handleVideoButtonState(IronSource.isRewardedVideoAvailable())
    }

    /**
     * initialize the UI elements of the activity
     */
    private fun initUIElements() {
        binding.rvButton.setOnClickListener {
            // check if video is available
            if (IronSource.isRewardedVideoAvailable()) //show rewarded video
                IronSource.showRewardedVideo()
        }
        binding.versionTxt.text = resources.getString(R.string.version, IronSourceUtils.getSDKVersion())
    }

    /**
     * Set the Rewareded Video button state according to the product's state
     *
     * @param available if the video is available
     */
    private fun handleVideoButtonState(available: Boolean) {
        val text: String
        val color: Int
        if (available) {
            color = Color.BLUE
            text = resources.getString(R.string.show) + " " + resources.getString(R.string.rv)
        } else {
            color = Color.BLACK
            text = resources.getString(R.string.initializing) + " " + resources.getString(R.string.rv)
        }
        runOnUiThread {
            binding.rvButton.setTextColor(color)
            binding.rvButton.text = text
            binding.rvButton.isEnabled = available
        }
    }

    // --------- IronSource Rewarded Video Listener ---------
    override fun onRewardedVideoAdOpened() {
        // called when the video is opened
        Log.d(TAG, "onRewardedVideoAdOpened")
    }

    override fun onRewardedVideoAdClosed() {
        // called when the video is closed
        Log.d(TAG, "onRewardedVideoAdClosed")
        // here we show a dialog to the user if he was rewarded
        mPlacement?.let {
            // if the user was rewarded
            showRewardDialog(it)
            mPlacement = null
        }

    }

    override fun onRewardedVideoAvailabilityChanged(b: Boolean) {
        // called when the video availbility has changed
        Log.d(TAG, "onRewardedVideoAvailabilityChanged $b")
        handleVideoButtonState(b)
    }

    override fun onRewardedVideoAdStarted() {
        // called when the video has started
        Log.d(TAG, "onRewardedVideoAdStarted")
    }

    override fun onRewardedVideoAdEnded() {
        // called when the video has ended
        Log.d(TAG, "onRewardedVideoAdEnded")
    }

    override fun onRewardedVideoAdRewarded(placement: Placement) {
        // called when the video has been rewarded and a reward can be given to the user
        Log.d(TAG, "onRewardedVideoAdRewarded $placement")
        mPlacement = placement
    }

    override fun onRewardedVideoAdShowFailed(ironSourceError: IronSourceError) {
        // called when the video has failed to show
        // you can get the error data by accessing the IronSourceError object
        // IronSourceError.getErrorCode();
        // IronSourceError.getErrorMessage();
        Log.d(TAG, "onRewardedVideoAdShowFailed $ironSourceError")
    }

    override fun onRewardedVideoAdClicked(placement: Placement) {}

    // --------- Impression Data Listener ---------

    override fun onImpressionSuccess(impressionData: ImpressionData?) {
        // The onImpressionSuccess will be reported when the rewarded video and interstitial ad is opened.
        // For banners, the impression is reported on load success.
        if (impressionData != null) {
            Log.d(TAG, "onImpressionSuccess $impressionData")
        }
    }

    private fun showRewardDialog(placement: Placement) {
        val builder = AlertDialog.Builder(this@DemoActivity)
        builder.setPositiveButton("ok") { dialog, _ -> dialog.dismiss() }
        builder.setTitle(resources.getString(R.string.rewarded_dialog_header))
        builder.setMessage(resources.getString(R.string.rewarded_dialog_message) + " " + placement.rewardAmount + " " + placement.rewardName)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }


}



