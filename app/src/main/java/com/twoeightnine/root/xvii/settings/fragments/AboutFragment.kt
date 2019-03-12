package com.twoeightnine.root.xvii.settings.fragments

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.TestLandscapeActivity
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.fragments.WebFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_about.*
import javax.inject.Inject

/**
 * Created by root on 2/2/17.
 */

class AboutFragment : BaseFragment() {

    private lateinit var crashTest: String

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun bindViews(view: View) {
        App.appComponent?.inject(this)
        initViews()
        if (!equalsDevUids(Session.uid)) {
            apiUtils.checkMembership { if (!it) showJoinDialog() }
        }
    }

    override fun getLayout() = R.layout.fragment_about

    private fun initViews() {
        tvFeedback.setOnClickListener { _ ->
            val message = Message(
                    0, 0, -App.GROUP, 0, 0, getString(R.string.app_name), "", null
            )
            rootActivity.loadFragment(ChatFragment.newInstance(message))
        }
        tvRate.setOnClickListener { _ -> rate(safeActivity) }
        tvPrivacy.setOnClickListener { _ ->
            rootActivity.loadFragment(WebFragment.newInstance(
                    "file:///android_asset/privacy.html",
                    getString(R.string.privacy_policy)))
        }
        tvShare.setOnClickListener { share() }
        tvAboutApp.text = getString(R.string.aboutBig, BuildConfig.VERSION_NAME)
        tvAboutApp.setOnLongClickListener { _ ->
            showLogDialog()
            true
        }
        tvAboutApp.setOnClickListener { showLogDialog() }
        if (equalsDevUids(Session.uid)) {
            tvCrash.visibility = View.VISIBLE
            tvLandscape.visibility = View.VISIBLE
            tvCrash.setOnLongClickListener {
                val r = crashTest[7]
                true
            }
            tvLandscape.setOnLongClickListener {
                startActivity(Intent(rootActivity, TestLandscapeActivity::class.java))
                true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.about))
    }

    private fun showLogDialog() {
        val logs = Lg.logs.joinToString(separator = "\n")
        val dialog = AlertDialog.Builder(safeActivity)
                .setMessage(logs)
                .setPositiveButton("copy", { _, _ -> copyToClip(logs)})
                .setNegativeButton("close", null)
                .create()
        dialog.show()
        Style.forDialog(dialog)
    }

    private fun showJoinDialog() {
        val dialog = AlertDialog.Builder(safeActivity)
                .setMessage(R.string.join_us)
                .setPositiveButton(android.R.string.ok, { _, _ -> apiUtils.joinGroup() })
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.show()
        Style.forDialog(dialog)
    }

    private fun share() {
        apiUtils.repost(
                App.SHARE_POST,
                { showCommon(context, R.string.shared) },
                { showError(context, it) }
        )
    }
}
