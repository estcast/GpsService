package cr.ac.gpsservice.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import cr.ac.gpsservice.db.LocationDatabase
import com.google.android.gms.location.*
import cr.ac.gpsservice.entity.Location

class GpsService : IntentService("GpsService") {

    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationCliente: FusedLocationProviderClient

    private lateinit var locationDatabase : LocationDatabase

    companion object{
        val GPS = "cr.ac.gpsservice.GPS_EVENT"
    }

    override fun onHandleIntent(intent: Intent?) {
        locationDatabase = LocationDatabase.getInstance(this)
        getLocation()
    }



    /*
    Inicializa los atributos locationCallback y fusedLocationClient
    coloca un intervalo de actualizacion de 10000 y una prioridad de PRIORITY_HIGH_ACCURACY
    recibe la ubicacion de gps mediante un onLocationResult
    y envía un broadcast con una instancia de Location y la acción GPS
    además guarda la localización en la BD
     */
    @SuppressLint("MissingPermission")
    fun getLocation(){
        fusedLocationCliente = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority =LocationRequest.PRIORITY_HIGH_ACCURACY

       /* LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        fusedLocationCliente = LocationServices.getFusedLocationProviderClient(this)
*/
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        locationCallback = object : LocationCallback(){

            override fun onLocationResult(locationResult: LocationResult) {
                if(locationResult.equals(null)){
                    return
                }
                for(location in locationResult.locations){
                    val sydney = Location(null,location.latitude, location.longitude)
                    val bcIntent = Intent()
                    bcIntent.action = GpsService.GPS
                    bcIntent.putExtra("latitude", location.latitude)
                    bcIntent.putExtra("longitude", location.longitude)
                    sendBroadcast(bcIntent)

                    locationDatabase.locationDao
                        .insert(Location(null,location.latitude,location.longitude))
                }

            }
        }

        //Looper.prepare()
        fusedLocationCliente.requestLocationUpdates(
            locationRequest,
            locationCallback,Looper.getMainLooper())

    }


}