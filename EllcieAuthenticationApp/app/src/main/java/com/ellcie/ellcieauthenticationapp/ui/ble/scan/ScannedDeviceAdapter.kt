package com.ellcie.ellcieauthenticationapp.ui.ble

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ellcie.ellcieauthenticationapp.databinding.ScannedDeviceItemBinding

class ScannedDeviceAdapter : RecyclerView.Adapter<ScannedDeviceViewHolder>() {
    var data : List<Pair<String,String>> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }


    var listener : ((String) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedDeviceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        //put parent and false
        val binding  = ScannedDeviceItemBinding.inflate(layoutInflater,parent,false)
        return ScannedDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScannedDeviceViewHolder, position: Int) {
        val deviceName = data.get(position)
        if (deviceName.second != null){
            holder.binding.scannedDeviceName.text = deviceName.second
            holder.binding.scannedDeviceName.setOnClickListener {
                listener?.invoke(deviceName.first)
            }
        }else{
            holder.binding.scannedDeviceName.isVisible = false
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class ScannedDeviceViewHolder(val binding : ScannedDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)