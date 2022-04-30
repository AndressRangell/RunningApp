package andres.rangel.runningapp.ui.fragment

import andres.rangel.runningapp.R
import andres.rangel.runningapp.databinding.FragmentTrackingBinding
import andres.rangel.runningapp.db.Run
import andres.rangel.runningapp.services.Polyline
import andres.rangel.runningapp.services.TrackingService
import andres.rangel.runningapp.ui.viewmodel.MainViewModel
import andres.rangel.runningapp.utils.Constants.ACTION_PAUSE_SERVICE
import andres.rangel.runningapp.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import andres.rangel.runningapp.utils.Constants.ACTION_STOP_SERVICE
import andres.rangel.runningapp.utils.Constants.MAP_ZOOM
import andres.rangel.runningapp.utils.Constants.POLYLINE_COLOR
import andres.rangel.runningapp.utils.Constants.POLYLINE_WIDTH
import andres.rangel.runningapp.utils.TrackingUtility
import andres.rangel.runningapp.utils.TrackingUtility.getFormattedStopWatchTime
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private lateinit var binding: FragmentTrackingBinding
    private val viewModel: MainViewModel by viewModels()
    private val className = "TrackingFragment"

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null
    private var currentTimeInMillis = 0L
    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            mapView.onCreate(savedInstanceState)

            btnToggleRun.setOnClickListener {
                toggleRun()
            }
            btnFinishRun.setOnClickListener {
                zoomToSeeWholeTrack()
                endRunAndSaveToDatabase()
            }
            mapView.getMapAsync {
                map = it
                addAllPolylines()
            }
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            val formattedTime = getFormattedStopWatchTime(currentTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancelTracking -> showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
            .show()
    }

    private fun stopRun() {
        binding.tvTimer.text = getString(R.string.initial_time)
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L) {
            binding.apply {
                btnToggleRun.text = "Start"
                btnFinishRun.visibility = View.VISIBLE
            }
        } else if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            binding.apply {
                btnToggleRun.text = "Stop"
                btnFinishRun.visibility = View.GONE
            }
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    @SuppressLint("TimberArgCount")
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        try {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds.build(),
                    binding.mapView.width,
                    binding.mapView.height,
                    (binding.mapView.height * 0.05f).toInt()
                )
            )
        } catch (e: IllegalStateException) {
            Timber.e(className, "Error is moveCamera map")
        }
    }

    private fun endRunAndSaveToDatabase() {
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val averageSpeed = round(
                (distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10
            ) / 10
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(
                bitmap, dateTimestamp, averageSpeed, distanceInMeters,
                currentTimeInMillis, caloriesBurned
            )

            viewModel.insertRun(run)
            Snackbar.make(binding.root, "Run saved successfully", Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}