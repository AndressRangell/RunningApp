package andres.rangel.runningapp.adapters

import andres.rangel.runningapp.R
import andres.rangel.runningapp.databinding.ItemRunBinding
import andres.rangel.runningapp.db.Run
import andres.rangel.runningapp.utils.TrackingUtility
import andres.rangel.runningapp.utils.dateFormat
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RunViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_run, parent, false)
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val binding = ItemRunBinding.bind(holder.itemView)
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.image).into(binding.ivRunImage)

            binding.apply {
                tvDate.text = run.timestamp.dateFormat()
                tvAvgSpeed.text = "${run.averageSpeedInKmH} km/h"
                tvDistance.text = "${run.distanceInMeters / 1000f} km"
                tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
                tvCalories.text = "${run.caloriesBurned} kcal"
            }
        }
    }

    override fun getItemCount() = differ.currentList.size

}