package cr.ac.gpsservice

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cr.ac.gpsservice.databinding.ActivityMapsBinding
import cr.ac.gpsservice.db.LocationDatabase
import cr.ac.gpsservice.entity.Location
import cr.ac.gpsservice.service.GpsService

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val SOLICITAR_GPS = 1

    private lateinit var locationDatabase : LocationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationDatabase = LocationDatabase.getInstance(this)
        validaPermisos()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        iniciaServicio()
        recuperarPuntos()
    }


    /*
    Obtener los puntos  almacenados en la BD y mostrarlos en el mapa
     */
    fun recuperarPuntos(){
        var lista : List<Location>  = locationDatabase.locationDao.query()

        for(loc in lista) {
            // Add a marker in Sydney and move the camera
            val sydney = LatLng(loc.latitude, loc.longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        }
    }


    /*
    Hace un filtro del broadcast/acción GPS (cr.ac.apservice.GPS_EVENT)
    E inicia el servicio (startService) GpsService
     */
    fun iniciaServicio(){
        val filter = IntentFilter()
        filter.addAction(GpsService.GPS)
        val progress = ProgressReceiver()
        registerReceiver(progress, filter)
        startService(Intent(this,GpsService::class.java))
    }

    /*
    valida si la app tiene permisos de ACCESS_FINE_LOCATION y de ACCESS_COARSE_LOCATION
    Si no tiene permisos solicita al usuario permisos (requestPermissions)
     */
    fun validaPermisos(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //No tengo permisos
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                SOLICITAR_GPS
            )
        }
    }


    /*
    Validar que se le dieron los permisos a la app, en caso contrario salir
     */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        when(requestCode){
            SOLICITAR_GPS ->{
                //Usuario no dio permisos
                if(grantResults.isEmpty() ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    System.exit(1)
                }
            }
        }
    }



    /*
    Es la clase para recibir los mensajes de broadcast
     */
    class ProgressReceiver : BroadcastReceiver() {

        /*
        Se obtiene el parametro eviado por el servicio (location)
        Coloca en el mapa la localización
        Mueve la cámara a la localización
         */
        override fun onReceive(p0: Context?, p1: Intent?){

        }
    }


}