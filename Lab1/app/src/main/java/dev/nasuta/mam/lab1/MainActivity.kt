package dev.nasuta.mam.lab1

import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var playerFileUri: Uri = Uri.EMPTY
    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val data: Intent? = result.data
        data?.let {
            val uri: Uri? = data.data
            uri?.let {
                playerFileUri = uri
                setLastOpenedFilename(uri)
            }
        }
    }

    private var player: MediaPlayer? = null

    private var recorderFileDescriptor: ParcelFileDescriptor? = null
    private var recorder: MediaRecorder? = null
    private val setDestinationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val data: Intent? = result.data
        data?.let {
            val uri: Uri? = data.data
            uri?.let {
                try {
                    recorderFileDescriptor = contentResolver.openFileDescriptor(uri, "w")
                    setLastDestinationFilename(uri)
                } catch (e: FileNotFoundException) {
                    Log.e(javaClass.simpleName, e.stackTraceToString())
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val openFileButton: Button = findViewById(R.id.openFileButton)
        openFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            try {
                openFileLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(javaClass.simpleName, e.stackTraceToString())
            }
        }

        val playButton: ImageButton = findViewById(R.id.playButton)
        playButton.setOnClickListener {
            if (player == null) {
                player = MediaPlayer().apply {
                    try {
                        setDataSource(applicationContext, playerFileUri)
                        prepare()
                    } catch (e: IOException) {
                        Log.e(javaClass.simpleName, "prepare() failed")
                    }
                }
            }
            player?.start()
        }

        val pauseButton: ImageButton = findViewById(R.id.pauseButton)
        pauseButton.setOnClickListener {
            player?.pause()
        }

        val stopButton: ImageButton = findViewById(R.id.stopButton)
        stopButton.setOnClickListener {
            player?.stop()
            player?.release()
            player = null
        }

        val setDestinationButton: Button = findViewById(R.id.setDestinationButton)
        setDestinationButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("audio/*")
            intent.putExtra(Intent.EXTRA_TITLE, "output.3gpp")
            try {
                setDestinationLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(javaClass.simpleName, e.stackTraceToString())
            }
        }

        val startRecordingButton: ImageButton = findViewById(R.id.startRecordingButton)
        startRecordingButton.setOnClickListener {
            if (recorderFileDescriptor == null) return@setOnClickListener
            recorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(recorderFileDescriptor!!.fileDescriptor)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, "prepare() failed")
                }
                start()
            }
        }

        val stopRecordingButton: ImageButton = findViewById(R.id.stopRecordingButton);
        stopRecordingButton.setOnClickListener {
            recorder?.stop()
            recorder?.release()
            recorder = null
            recorderFileDescriptor?.close()
            recorderFileDescriptor = null
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        recorder?.release()
        player = null
        recorder = null
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) ==
            PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                .requestPermissions(this, arrayOf(RECORD_AUDIO), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setLastOpenedFilename(file: Uri) {
        val lastOpenedFileTextView: TextView = findViewById(R.id.openedFileLabel)
        lastOpenedFileTextView.text = getFileName(file)
    }

    fun setLastDestinationFilename(file: Uri) {
        val lastDestinationFileTextView: TextView = findViewById(R.id.destinationFileLabel)
        lastDestinationFileTextView.text = getFileName(file)
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.getScheme() == "content") {
            val cursor = getContentResolver().query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.getPath()
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

}
