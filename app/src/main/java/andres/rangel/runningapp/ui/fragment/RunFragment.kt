package andres.rangel.runningapp.ui.fragment

import andres.rangel.runningapp.R
import andres.rangel.runningapp.adapters.RunAdapter
import andres.rangel.runningapp.databinding.FragmentRunBinding
import andres.rangel.runningapp.ui.viewmodel.MainViewModel
import andres.rangel.runningapp.utils.Constants.REQUEST_CODE_LOCATION_PERMISSIONS
import andres.rangel.runningapp.utils.SortType
import andres.rangel.runningapp.utils.TrackingUtility
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()
    private val className = "RunFragment"

    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        initRecyclerView()

        when (viewModel.sortType) {
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.AVERAGE_SPEED -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }
        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.AVERAGE_SPEED)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Timber.d("onNothingSelected: No item selected")
            }

        }

        viewModel.runs.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }

        binding.btnNewRun.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun initRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.e(className, "Permissions granted")
    }

    override fun onPermissionsDenied(requestCode: Int, permissions: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, permissions)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}