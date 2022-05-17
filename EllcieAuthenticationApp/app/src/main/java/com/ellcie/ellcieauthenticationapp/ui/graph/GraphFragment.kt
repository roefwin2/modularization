package com.ellcie.ellcieauthenticationapp.ui.graph

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ellcie.ellcieauthenticationapp.databinding.GraphFragmentBinding
import com.ellcie.toolkitlibrary.actionstate.ActionState
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GraphFragment : Fragment() {
    private var _binding: GraphFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    private val set1 : LineDataSet = LineDataSet(mutableListOf(),"")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            viewModel.graphState.collect {
                processSensorData(it)
            }
        }
        viewModel.getSensorsData()
    }

    private fun processSensorData(graphScreen: GraphScreen) {
        val sensorData = graphScreen.sensorData
        if (binding.chart.data != null && binding.chart.data.dataSetCount > 0) {
            set1.addEntry(Entry(sensorData.timestamp.toFloat(),sensorData.data.toFloat()))
            binding.chart.data = LineData(set1)
            if(set1.entryCount > 60){
                set1.removeFirst()
            }
            binding.chart.moveViewToX(sensorData.timestamp.toFloat())
            binding.chart.notifyDataSetChanged()
            binding.chart.invalidate()
        } else {
          val set2 = LineDataSet(mutableListOf(), "Sample Data")
            set2.setDrawIcons(false)
            set2.enableDashedLine(10f, 5f, 0f)
            set2.enableDashedHighlightLine(10f, 5f, 0f)
            set2.setColor(Color.DKGRAY)
            set2.setCircleColor(Color.DKGRAY)
            set2.setLineWidth(1f)
            set2.setCircleRadius(3f)
            binding.chart.data = LineData(set2)
            binding.chart.data.notifyDataChanged()
            binding.chart.notifyDataSetChanged()
        }

        when(graphScreen.stopTripAction){
            ActionState.NOT_STARTED -> {}
            ActionState.PENDING -> {}
            ActionState.SUCCESS -> {Toast.makeText(requireContext(),"trip stopped",Toast.LENGTH_LONG).show()}
            ActionState.STOP -> {}
            ActionState.ERROR -> Toast.makeText(requireContext(),"Error stop trip",Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GraphFragmentBinding.inflate(inflater, container, false)
        binding.chart.setMaxVisibleValueCount(60)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopTrip()
    }

}