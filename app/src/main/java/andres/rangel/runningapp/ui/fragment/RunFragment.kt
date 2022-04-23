package andres.rangel.runningapp.ui.fragment

import andres.rangel.runningapp.R
import andres.rangel.runningapp.ui.viewmodel.MainViewModel
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run) {

    private val viewModel: MainViewModel by viewModels()

}