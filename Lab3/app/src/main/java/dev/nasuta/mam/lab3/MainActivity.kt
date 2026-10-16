package dev.nasuta.mam.lab3

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.exceptions.NotYetAvailableException
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.arcore.quaternion
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import java.nio.ByteOrder
import kotlin.math.sin
import kotlin.math.sqrt

const val ufoModelFile = "models/ufo.glb"

private const val ROTATION_SPEED_DEG = 90f // degrees per second
private const val HOVER_AMPLITUDE = 0.05f // meters
private const val HOVER_HEIGHT = 0.1f // meters above the plane
private const val HOVER_SPEED = 2f // radians per second
private const val TRAVEL_SPEED = 0.4f // meters per second
private const val MAX_DEPTH_MM = 5000 // depth values beyond this are drawn as white

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: ARSceneView
    private var lastFrame: Frame? = null
    private var anchorNodes = mutableListOf<AnchorNode>()
    private lateinit var depthView: CustomView
    private var modelNode: ModelNode? = null
    private var depthBitmap: Bitmap? = null
    private var depthPixels: IntArray? = null

    // Animation state
    private var animationAngle = 0f
    private var lastFrameTimeNanos = 0L

    // Position of the UFO's projection on the plane and the point it is flying to
    private var groundPosition: Position? = null
    private var targetPosition: Position? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Fullscreen
        val rootView = findViewById<ConstraintLayout>(R.id.main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, rootView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        depthView = findViewById(R.id.depthView)
        sceneView = findViewById<ARSceneView>(R.id.sceneView).apply {
            planeRenderer.isEnabled = true
            configureSession { session, config ->
                config.depthMode =
                    when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        true -> Config.DepthMode.AUTOMATIC
                        else -> Config.DepthMode.DISABLED
                    }
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
            onSessionUpdated = this@MainActivity::onSessionUpdated
            onGestureListener = GestureListener(this@MainActivity)
        }
    }

    private fun onSessionUpdated(session: Session, frame: Frame) {
        lastFrame = frame

        if (anchorNodes.isEmpty()) {
            frame.getUpdatedPlanes()
                .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                    Log.d(javaClass.simpleName, "anchor.pose: ${anchor.pose}")
                    anchorNodes += createAnchorNode(
                        anchor = anchor,
                        assetModelLocation = ufoModelFile,
                        modelScale = 0.3f)
                    groundPosition = anchor.pose.position
                }
        }

        updateAnimation(frame.timestamp)
        updateDepthView(frame)
    }

    private fun updateDepthView(frame: Frame) {
        try {
            frame.acquireDepthImage16Bits().use { depthImage ->
                val width = depthImage.width
                val height = depthImage.height
                val plane = depthImage.planes[0]
                val buffer = plane.buffer.order(ByteOrder.nativeOrder())

                // The depth image is landscape, so the bitmap is rotated to match portrait mode
                val bitmap = depthBitmap?.takeIf { it.width == height && it.height == width }
                    ?: Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888).also {
                        depthBitmap = it
                        depthPixels = IntArray(width * height)
                    }
                val pixels = depthPixels ?: return

                for (xx in 0..<width) {
                    for (yy in 0..<height) {
                        val index = xx * plane.pixelStride + yy * plane.rowStride
                        // Depth in millimeters, 0 means no data
                        val depthValue = buffer.getShort(index).toInt() and 0xFFFF
                        val gray = if (depthValue == 0) 0
                        else 255 - (depthValue.coerceAtMost(MAX_DEPTH_MM) * 255 / MAX_DEPTH_MM)
                        pixels[xx * height + (height - 1 - yy)] = Color.rgb(gray, gray, gray)
                    }
                }
                bitmap.setPixels(pixels, 0, height, 0, 0, height, width)
                depthView.bitmap = bitmap
                depthView.postInvalidate()
            }
        } catch (e: NotYetAvailableException) {
            // Depth image is not available during the first frames
        }
    }

    private fun updateAnimation(timestampNanos: Long) {
        val model = modelNode ?: return
        val ground = groundPosition ?: return
        val dt = if (lastFrameTimeNanos == 0L) 0f else (timestampNanos - lastFrameTimeNanos) / 1e9f
        lastFrameTimeNanos = timestampNanos

        animationAngle += dt

        // Fly towards the point chosen by the user
        targetPosition?.let { target ->
            val dx = target.x - ground.x
            val dz = target.z - ground.z
            val distance = sqrt(dx * dx + dz * dz)
            val step = TRAVEL_SPEED * dt
            groundPosition = if (distance <= step) {
                targetPosition = null
                Position(target.x, target.y, target.z)
            } else {
                Position(
                    ground.x + dx / distance * step,
                    target.y,
                    ground.z + dz / distance * step)
            }
        }

        // Constant rotation and sinusoidal hovering
        val current = groundPosition ?: ground
        model.worldPosition = Position(
            current.x,
            current.y + HOVER_HEIGHT + HOVER_AMPLITUDE * sin(animationAngle * HOVER_SPEED),
            current.z)
        model.rotation = Rotation(y = (animationAngle * ROTATION_SPEED_DEG) % 360f)
    }

    fun moveToHit(x: Float, y: Float) {
        if (anchorNodes.isEmpty()) return
        lastFrame?.hitTest(x, y)
            ?.firstOrNull { it.isValid(depthPoint = false, point = false) }
            ?.let { targetPosition = it.hitPose.position }
    }

    private fun createAnchorNode(anchor: Anchor, assetModelLocation: String,
                                 modelScale: Float? = null): AnchorNode {
        Log.d(javaClass.simpleName, "createAnchorNode: $assetModelLocation")
        val anchorNode = AnchorNode(engine = sceneView.engine, anchor = anchor)
        val modelNode = ModelNode(
            modelInstance = sceneView.modelLoader.createModelInstance(assetModelLocation),
            scaleToUnits = modelScale
        ).apply {
            isEditable = true
            editableScaleRange = 0.05f..1.5f
        }

        val colorInt = Color.argb(0.5f, 1.0f, 1.0f, 1.0f)
        val boundingBoxNode = CubeNode(
            sceneView.engine,
            size = modelNode.extents,
            center = modelNode.center,
            materialInstance = sceneView.materialLoader.createColorInstance(colorInt)
        ).apply {
            isVisible = false
        }

        this.modelNode = modelNode
        modelNode.addChildNode(boundingBoxNode)
        anchorNode.addChildNode(modelNode)
        sceneView.addChildNode(anchorNode)

        listOf(modelNode, anchorNode).forEach {
            it.onEditingChanged = { editingTransforms ->
                boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
            }
        }
        return anchorNode
    }

    private fun getNodePosition(node: AnchorNode): Position {
        return node.pose.position
    }

    private fun setNodePosition(node: AnchorNode, x: Float, y: Float, z: Float) {
        val newPosition = floatArrayOf(x, y, z)
        val newPose = Pose(newPosition, node.pose.quaternion.toFloatArray())
        node.pose = newPose
    }

    private fun getAnchorPosition(node: AnchorNode): Position {
        return node.anchor.pose.position
    }

    private fun setAnchorPosition(node: AnchorNode, x: Float, y: Float, z: Float) {
        val newPosition = floatArrayOf(x, y, z)
        val newPose = Pose(newPosition, node.anchor.pose.quaternion.toFloatArray())
        node.session?.let {
            node.anchor.detach()
            node.anchor = it.createAnchor(newPose)
        }
    }
}