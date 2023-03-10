package com.example.ddtapp.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.ddtapp.BuildConfig
import com.example.ddtapp.R
import com.example.ddtapp.di.Injectable
import com.example.ddtapp.model.House
import com.example.ddtapp.ui.menu.MenuActivity
import com.example.ddtapp.utils.Constants
import com.example.ddtapp.utils.LocationUtils
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject

class MainDetailFragment : Fragment(), Injectable, OnMapReadyCallback {

    private var modelId: Int ?= null
    private var distance: String? = ""
    private lateinit var searchToolbar: Toolbar
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var expandedImage: ImageView
    private lateinit var price: TextView
    private lateinit var bed: TextView
    private lateinit var bath: TextView
    private lateinit var size: TextView
    private lateinit var location: TextView
    private lateinit var description: TextView

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        distance = args?.getString(Constants.HOUSE_DISTANCE)
        modelId = args?.getInt(Constants.HOUSE_ID)
    }

    companion object {
        fun newInstance(data: Bundle? = null): MainDetailFragment =
            MainDetailFragment().apply {
                arguments = data
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setData()
    }

    private fun bindViews(view: View) = with(view) {
        expandedImage = findViewById(R.id.expandedImage)
        price = findViewById(R.id.price)
        bed = findViewById(R.id.bed)
        bath = findViewById(R.id.bath)
        size = findViewById(R.id.size)
        location = findViewById(R.id.location)
        description = findViewById(R.id.description)
        searchToolbar = findViewById(R.id.searchToolbar)
    }

    private fun setData() {
        //getting single house data from database using house model ID
        viewModel.getHouseById(modelId.toString())
        viewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is MainViewModel.Result.HouseModel -> {
                    setModelData(result.house)
                }
                is MainViewModel.Result.HousesFiltered -> {

                }
                is MainViewModel.Result.Houses -> {

                }
                is MainViewModel.Result.Error -> {

                }
            }
        })
    }

    private fun setModelData(model: House?) {
        val latLng = LatLng(model?.latitude!!.toDouble(), model.longitude.toDouble())
        val priceText = resources.getString(R.string.usd_sign) + LocationUtils.formatDecimalSeparator(model.price)
        val locationText = distance + resources.getString(R.string.km)

        price.text = priceText
        bed.text = model.bedrooms.toString()
        bath.text = model.bathrooms.toString()
        size.text = model.size.toString()
        description.text = model.description
        location.text = locationText
        Glide.with(this)
            .load(BuildConfig.BASE_URL + model.image)
            .into(expandedImage)

        setMap(latLng)
        searchToolbar.setOnClickListener {
            (activity as MenuActivity).onBackPressed()
        }
    }

    //configuring Google Maps, adding marker, setting camera position
    private fun setMap(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.ZOOM))
        map.addMarker(MarkerOptions().position(latLng))
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = false
        map.setOnMapClickListener {
            //navigation of user from current location to house location using Google maps app
            val url = Uri.parse("google.navigation:q=${it.latitude},${it.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        }
        mapView.onResume()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
}