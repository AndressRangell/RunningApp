package andres.rangel.runningapp.ui.fragment

import andres.rangel.runningapp.R
import andres.rangel.runningapp.databinding.FragmentRunBinding
import andres.rangel.runningapp.ui.viewmodel.MainViewModel
import andres.rangel.runningapp.utils.Constants.REQUEST_CODE_LOCATION_PERMISSIONS
import andres.rangel.runningapp.utils.TrackingUtility
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()
    private val className = "RunFragment"

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

        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())){
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else {
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
        if(EasyPermissions.somePermissionPermanentlyDenied(this, permissions)){
            AppSettingsDialog.Builder(this).build().show()
        }else {
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