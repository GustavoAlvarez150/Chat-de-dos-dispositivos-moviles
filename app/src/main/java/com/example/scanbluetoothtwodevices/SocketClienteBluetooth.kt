package com.example.scanbluetoothtwodevices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Message
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class SocketClienteBluetooth(var context: Context, var txtStatus: TextView, var dispositivos: BluetoothDevice, var socketCliente: BluetoothSocket, var mMensaje: TextView) {


    var buffer: String? = null
    var btSocketCliente: BluetoothSocket? = null
    var r: Boolean? = null
    var STATE_LISTENING = 1
    var STATE_CONNECTING = 2
    var STATE_CONNECTED = 3
    var STATE_CONNECTION_FAILED = 4
    var STATE_MESSAGE_RECIVED = 5

    init {
        try {


        }catch (e: IOException){
            //nos da el informacion de algun error
            e.printStackTrace()
        }

    }

    public fun Iniciarconexion(){

        conectar()

    }

    @SuppressLint("MissingPermission")
    fun conectar(){

        var mensaje: Message? = null
        try {
            socketCliente!!.connect()
            btSocketCliente = socketCliente
            mensaje = Message.obtain()
            mensaje.what = STATE_CONNECTED
            r = true
            Thread.sleep(1000)
            GlobalScope.launch (Dispatchers.IO){ leer()  }


        }catch (e: IOException){
            e.printStackTrace()
            mensaje = Message.obtain()
            mensaje.what = STATE_CONNECTION_FAILED
            r = false
        }
    }

    public fun enviar(m: String){
        var r = "!$m"
        btSocketCliente!!.outputStream.write(r.toByteArray())

    }

    suspend fun leer(){

        while (btSocketCliente != null){

            try {
                var a: Char? = null
                println("1##########################################################")
                a = btSocketCliente!!.inputStream.read().toChar()
                println("2 $a ######################################################")
                if (a == '!'){

                    buffer = ""
                }else{
                    Thread.sleep(3)
                    buffer = "$buffer $a"
                    mMensaje.text = buffer

                }

            }catch (e:IOException){
                e.printStackTrace()

            }
        }


    }







}