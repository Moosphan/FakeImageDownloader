package com.moosphon.downloader

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.moosphon.downloader.download.SimpleDownloader
import com.moosphon.downloader.listener.TasksDownloadStateListener
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

/**
 * Q: Download 3 images with multithreading technique.
 * @author Moosphon
 */
class MainActivity : AppCompatActivity(), TasksDownloadStateListener {

    // original image resources from network
    private val remoteImages = arrayOf(
        "http://img2.imgtn.bdimg.com/it/u=1779655133,2327716287&fm=26&gp=0.jpg",
        "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=2488102644,4095521058&fm=26&gp=0.jpg",
        "http://img1.imgtn.bdimg.com/it/u=2942945378,442701149&fm=26&gp=0.jpg"
    )

    // image resources to display after download
    private val bitmapsDownloaded: MutableList<Bitmap> = ArrayList()

    // call this method after all images' download
    override fun allComplete() {
        Log.e(TAG, "[---- We have got all 3 images. ----]")
        runOnUiThread {
            downloadProgressBar.visibility = View.INVISIBLE
            imageContainer.visibility = View.VISIBLE
            for (index in imageContainer.children.toList().indices) {
                (imageContainer.getChildAt(index) as ImageView).setImageBitmap(bitmapsDownloaded[index])
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadButton.setOnClickListener {
            downloadProgressBar.visibility = View.INVISIBLE
            SimpleDownloader.getInstance()
                .init(remoteImages.toMutableList(), 3, this)
                .bindMessager(mHandler)
                .startWorking()
        }
    }

    private var mHandler: Handler? = WithoutLeakHandler(this)

    companion object {
        const val TAG = "MainActivity"
        private class WithoutLeakHandler( activity: MainActivity) : Handler(){
            private var mActivity: WeakReference<MainActivity> = WeakReference(activity)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val activity = mActivity.get()
                when(msg.what){
                    SimpleDownloader.MSG_SUCCESS -> {
                        val bitmap = msg.obj as Bitmap
                        Log.d(TAG, "Download successfully : $bitmap")
                        activity?.bitmapsDownloaded?.add(bitmap)
                    }

                    SimpleDownloader.MSG_PROGRESS -> {
                        val progress = msg.obj as Float
                        Log.d(TAG, "Download underway with progress: " + (progress * 100).toInt())
                    }

                    SimpleDownloader.MSG_FAILED -> {
                        val error = msg.obj as String
                        Log.d(TAG, "Download failed : $error")
                    }
                }
            }
        }
    }
}
