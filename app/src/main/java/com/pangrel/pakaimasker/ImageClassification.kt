package com.pangrel.pakaimasker

import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.util.Log
import com.chaquo.python.PyObject
import java.io.ByteArrayOutputStream

interface OnEventListener<T> {
    fun onSuccess(`object`: T)
    fun onFailure(e: Exception?)
}

interface ClassificationResult {
    val face: Int
    val classification: Int
    val accuracy: Double
    val minAccuracy: Double
    val maxAccuracy: Double
    val duration: Long
    val sample: Int
    val raw: String?
}

class ImageClassification : AsyncTask<MutableList<Bitmap>, Void, ClassificationResult>(){
    private var mCallBack: OnEventListener<ClassificationResult>? = null
    private var mContext: Context? = null
    private var classificationModule: PyObject? = null

    private var mException: java.lang.Exception? = null


    fun setCallback(callback: OnEventListener<ClassificationResult>) {
        this.mCallBack = callback
    }

    fun setModule(module: PyObject) {
        this.classificationModule = module
    }

    override fun doInBackground(vararg p0: MutableList<Bitmap>): ClassificationResult? {
        val images = p0.first()
        val results = arrayListOf<MutableList<PyObject>>()
        var lastImage : ByteArray? = null

        if (images.size == 0) return null;

        try {
            val startTime = System.currentTimeMillis()
            for (image in images) {
                val bytes = BitmapToByte(image)
                if (bytes != null) {
                    lastImage = bytes
                    if (classificationModule != null) {
                        val result: MutableList<PyObject> =
                            classificationModule!!.callAttr("predict", bytes).asList()
                        results.add(result)
                    }
                }
            }
            val endTime = System.currentTimeMillis()

            val output = object : ClassificationResult {
                override var face = 0
                override var classification = UNSURE
                override var accuracy = 1.0
                override var minAccuracy = 1.0
                override var maxAccuracy = 0.0
                override val duration = (endTime - startTime)
                override val sample = results.size
                override val raw = results.toString()

                override fun toString(): String {
                    return "Face: $face, Classification: $classification, Accuracy: $accuracy, Min Accuracy: $minAccuracy, Max Accuracy: $maxAccuracy, Duration: $duration, Sample: $sample"
                }
            }

            val firstTotalFace = results.get(0).size
            val firstClass = if (firstTotalFace > 0) results[0][0].asList()[0].toInt() else NOT_FOUND

            var totalAcc = 0.0
            for (result in results) {
                val totalFace = result.size

                if (totalFace != firstTotalFace) {
                    Log.d("ImageClassification FACE", output.toString())
                    return output
                }

                if (totalFace > 0) {
                    for (face in result) {
                        val cls = face.asList()[0].toInt()
                        val acc = face.asList()[1].toDouble()

                        if (cls != firstClass) {
                            Log.d("ImageClassification CLASS", output.toString())
                            return output
                        }

                        if (acc < output.minAccuracy) {
                            output.minAccuracy = acc
                        }

                        if (acc > output.maxAccuracy) {
                            output.maxAccuracy = acc
                        }

                        totalAcc += acc
                    }
                }
            }

            output.face = firstTotalFace
            output.classification = firstClass
            if (output.face > 0)
                output.accuracy = totalAcc / (output.sample * output.face)

            Log.d("ImageClassification", output.toString())

            return output
        } catch (e: Exception) {
            mException = e;
        }

        return null
    }

    override fun onPostExecute(result: ClassificationResult?) {
        if (mCallBack != null) {
            if (mException == null) {
                if (result != null) {
                    mCallBack!!.onSuccess(result)
                }
            } else {
                mCallBack!!.onFailure(mException)
            }
        }
    }

    private fun BitmapToByte(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    companion object {
        const val UNDEFINED = -3
        const val UNSURE = -2
        const val NOT_FOUND = -1
        const val WITH_MASK = 0
        const val WITHOUT_MASK = 1
    }
}