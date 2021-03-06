package com.github.travelplannerapp.scanner

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.travelplannerapp.R
import com.github.travelplannerapp.communication.appmodel.Scan
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_scanner.*
import org.opencv.android.OpenCVLoader
import javax.inject.Inject

class ScannerActivity : AppCompatActivity(), ScannerContract.View {

    @Inject
    lateinit var presenter: ScannerContract.Presenter

    private var photoPath: String? = null

    companion object {
        const val REQUEST_SCANNER = 3
        const val REQUEST_SCANNER_RESULT_MESSAGE = "REQUEST_SCANNER_RESULT_MESSAGE"
        const val REQUEST_SCANNER_RESULT_SCAN = "REQUEST_SCANNER_RESULT_SCAN"
        const val EXTRA_PHOTO_PATH = "EXTRA_PHOTO_PATH"
        const val EXTRA_TRAVEL_ID = "EXTRA_TRAVEL_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        initOpenCv()

        buttonScan.setOnClickListener {
            photoPath?.let {
                presenter.takeScan(it, imageViewSelection.getPoints(),
                        imageViewSelection.scaleRatio)
            }
        }

        photoPath = intent.getStringExtra(EXTRA_PHOTO_PATH)
        if (photoPath != null) {
            val (bitmap, scaleRatio) = BitmapHelper.decodeBitmapFromFile(photoPath!!,
                    resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            imageViewSelection.setImageBitmap(bitmap, scaleRatio)
            //TODO [Dorota] imageViewSelection.setPoints(Scanner.findCorners(photoPath))
        } else returnResultAndFinish(R.string.scanner_initialization_error)
    }

    override fun returnResultAndFinish(messageCode: Int) {
        returnResultAndFinish(messageCode, null)
    }

    override fun returnResultAndFinish(messageCode: Int, scan: Scan?) {
        val resultIntent = Intent().apply {
            putExtra(REQUEST_SCANNER_RESULT_MESSAGE, messageCode)
            scan?.let { putExtra(REQUEST_SCANNER_RESULT_SCAN, it) }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun initOpenCv() {
        if (!OpenCVLoader.initDebug()) {
            returnResultAndFinish(R.string.scanner_initialization_error)
        }
    }

    override fun showScanResultDialog(scan: Bitmap) {
        val dialog = AlertDialog.Builder(this)
        val imageView = ImageView(this)
        imageView.setImageBitmap(scan)
        dialog.setView(imageView)

        dialog.setPositiveButton(R.string.save) { _, _ ->
            val scanFile = BitmapHelper.bitmapToFile(scan, cacheDir)
            presenter.uploadScan(scanFile)
        }

        dialog.setNegativeButton(R.string.cancel) { _, _ ->
            finish()
        }

        dialog.show()
    }

}
