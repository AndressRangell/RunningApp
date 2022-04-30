package andres.rangel.runningapp.ui

import andres.rangel.runningapp.R
import andres.rangel.runningapp.databinding.ActivityMainBinding
import andres.rangel.runningapp.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import andres.rangel.runningapp.utils.Constants.KEY_NAME
import andres.rangel.runningapp.utils.setNameInToolbar
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(binding.toolbar)
        binding.bottomNavigationView.setupWithNavController(
            binding.navigationHostFragment.getFragment<NavHostFragment>().navController
        )

        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */ }

        binding.navigationHostFragment.getFragment<NavHostFragment>().navController
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    else -> binding.bottomNavigationView.visibility = View.GONE
                }
            }

        sharedPreferences.getString(KEY_NAME, "")?.let { this.setNameInToolbar(it) }

    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            binding.navigationHostFragment.getFragment<NavHostFragment>().navController
                .navigate(R.id.action_global_trackingFragment)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

}