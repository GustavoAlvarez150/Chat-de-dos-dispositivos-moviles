package com.example.scanbluetoothtwodevices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.scanbluetoothtwodevices.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.lsView
import kotlinx.android.synthetic.main.activity_main.txtMensaje
import kotlinx.android.synthetic.main.activity_main.txtMsg
import kotlinx.android.synthetic.main.activity_main.txtStatus
import java.util.ArrayList
import java.util.UUID
import java.util.jar.Manifest

private var myBluetoothAdapter: BluetoothAdapter?= null
var socketAux: BluetoothServerSocket? = null
val APP_NAME = "BTChat"
var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-008005F9B34FB")
val REQUEST_ENABLE_BLUETOOTH = 1
lateinit var binding: ActivityMainBinding
var servidor : Boolean = false
lateinit var  n_pairedDevice: Set<BluetoothDevice>
var socketCliente: BluetoothSocket? = null
var conectarCliente: SocketClienteBluetooth? = null
var escuchar: SocketServidorBluetooth? = null

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificarPerimisos()
        habilitarBluetooth()
        implementarListeners()




        binding.btnEscuchar!!.setOnClickListener {


            binding.txtStatus.text = escuchar!!.conexionStatus

            escuchar!!.IniciarConexion()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                while (escuchar!!.conexionStatus == "Esperar conexion") {
                }
                binding.txtStatus.text = escuchar!!.conexionStatus
                servidor=true
            }else{
                servidor=true
            }



        }

        binding.btnEnviar!!.setOnClickListener {
            if(servidor){
                escuchar!!.enviar(txtMsg!!.text.toString())
                txtMsg!!.text.clear()
            }else{
                conectarCliente!!.enviar(txtMsg!!.text.toString())
                txtMsg!!.text.clear()
            }
        }

    }



    private fun verificarPerimisos() {

        val permsRequestCode = 100
        val perms = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN,

            )


        val accessFinePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            val accessCoarsePermission =
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            val BluetoothPermission = checkSelfPermission(android.Manifest.permission.BLUETOOTH)
            if (BluetoothPermission == PackageManager.PERMISSION_GRANTED && accessCoarsePermission ==
                PackageManager.PERMISSION_GRANTED
            ) {

                //SE REALIZA METODO SI ES NECESARIO

            } else {

                requestPermissions(perms, permsRequestCode)
            }

        } else {

            val bluetoothPermission =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {

                //El permiso no está aceptado. Solicitar permiso de Ubicación
                bluetoothPermission.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)

            } else {


            }
        }


    }

    @Suppress("DEPRECATION")
    private fun habilitarBluetooth(){

        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.getAdapter()


        myBluetoothAdapter = bluetoothManager.adapter
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        socketAux = myBluetoothAdapter!!.listenUsingRfcommWithServiceRecord(APP_NAME, myUUID)

        //habilitar bluetooth
        if (!myBluetoothAdapter!!.isEnabled){

            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_LONG).show()
        }


    }


    @SuppressLint("SuspiciousIndentation")
    private fun implementarListeners(){

        binding.btnListaDispositivos!!.setOnClickListener{

        servidor = false
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            n_pairedDevice = myBluetoothAdapter!!.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()
        var listaNombre: ArrayList<String> = ArrayList()

        if (!n_pairedDevice.isEmpty()){
            for (device: BluetoothDevice in n_pairedDevice){
                var nombre: String = device.name

                if (nombre == null){

                    nombre = device.address
                }
                listaNombre.add(nombre.toString())
                list.add(device)

            }

        }else{
            Toast.makeText(this, "No se encontraron dispositivos Bluetooth", Toast.LENGTH_LONG).show()
        }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaNombre)
            lsView!!.adapter = adapter

            lsView!!.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->

                val device: BluetoothDevice = list[position]

                socketCliente = device.createRfcommSocketToServiceRecord(myUUID)

                conectarCliente = SocketClienteBluetooth(this, txtStatus!!, device, socketCliente!!, txtMensaje!!)
                Toast.makeText(this,"Aquí", Toast.LENGTH_LONG).show()
                conectarCliente!!.Iniciarconexion()
                if (conectarCliente!!.r!!){

                    txtStatus!!.text = "Conectando"
                }else{

                    txtStatus!!.text = "No se logro conectar..."
                }

            }

    }

        escuchar = SocketServidorBluetooth(this, txtStatus!!, myBluetoothAdapter!!, socketAux!!, txtMsg!!, txtMensaje!!)

        binding.btnEscuchar!!.setOnClickListener{



        }


    }
}