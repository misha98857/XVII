package com.twoeightnine.root.xvii.views.photoviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_image_viewer.*
import javax.inject.Inject

class ImageViewerActivity : AppCompatActivity() {

    private var adapter: FullScreenImageAdapter? = null
    private var photos: MutableList<Photo>? = null
    private var filePath: String? = null
    private var position: Int = 0
    private var fileMode = false

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_image_viewer)
        App.appComponent?.inject(this)
        initData()
        val urls = when{
            photos != null -> getUrlsFromPhotos(photos!!)
            filePath != null -> arrayListOf(filePath!!)
            else -> arrayListOf()
        }
        adapter = FullScreenImageAdapter(this, urls, ::onDismiss, ::onTap)
        vpImage.adapter = adapter
        setPosition(position)
        vpImage.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                setPosition(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        vpImage.currentItem = position
        btnDownload.setOnClickListener {
            if (photos == null || photos!!.size == 0) return@setOnClickListener

            val url = photos!![vpImage.currentItem].maxPhoto
            val fileName = getNameFromUrl(url)
            downloadFile(
                    this,
                    url,
                    fileName,
                    DownloadFileAsyncTask.PIC,
                    { showCommon(this, getString(R.string.doenloaded, fileName)) }
            )
        }
        btnSaveToAlbum.setOnClickListener {
            if (photos == null || photos!!.size == 0) return@setOnClickListener

            val photo = photos!![vpImage.currentItem]
            Lg.i("photo: ${photo.ownerId} ${photo.id} ${photo.accessKey}")
            apiUtils.saveToAlbum(this, photo.ownerId ?: 0, photo.id ?: 0, photo.accessKey ?: "")
        }
        if (fileMode) {
            onTap()
        }
    }

    private fun initData() {
        val photosRaw = intent.getSerializableExtra(PHOTOS)
        if (photosRaw != null) {
            photos = (photosRaw as Array<Parcelable>)
                    .map { it as Photo }
                    .toMutableList()
        } else {
            fileMode = true
        }
        filePath = intent.getStringExtra(PATH)
        position = intent.getIntExtra(POSITION, 0)
    }

    private fun onDismiss() {
        finish()
    }

    private fun onTap() {
        visibilitor(rlTop)
        visibilitor(llBottom)
    }

    private fun setPosition(position: Int) {
        tvPosition.text = "${position + 1}/${photos?.size ?: 1}"
    }

    private fun getUrlsFromPhotos(photos: MutableList<Photo>) = photos
            .map { it.almostMax }
            .toMutableList()

    private fun visibilitor(vg: ViewGroup) {
        vg.visibility = if (vg.visibility == View.VISIBLE || fileMode) View.INVISIBLE else View.VISIBLE
    }

    companion object {

        const val PHOTOS = "urls"
        const val POSITION = "position"
        const val PATH = "path"

        fun viewImages(context: Context, photos: MutableList<Photo>, position: Int = 0) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(PHOTOS, photos.toTypedArray())
            intent.putExtra(POSITION, position)
            context.startActivity(intent)
        }

        fun viewImage(context: Context, filePath: String) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(PATH, filePath)
            context.startActivity(intent)
        }
    }

}
