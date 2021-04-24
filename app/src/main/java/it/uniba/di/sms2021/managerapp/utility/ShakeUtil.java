package it.uniba.di.sms2021.managerapp.utility;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class ShakeUtil {
    private static final String TAG = "ShakeUtil";

    private static SensorManager mSensorManager;
    private static Sensor sensor;
    private static float x, y, z;
    private static boolean itIsNotFirstTime = false;
    private static float accelerationPrev= 0.0f;
    private static float acceleration = 0.0f;

    //Soglia massima della variazione di accelerazione affinchè venga considerato lo scuotimento
    private static final float THRESHOLD = 5f;
    //Soglia minima della accelerazione corrente affinchè venga considerato lo scuotimento
    private static final float THRESHOLD_ACCELERATION = 15f;
    //Soglia minima della accelerazione precedente affinchè venga considerato lo scuotimento
    private static final float THRESHOLD_ACCELERATION_PREV = 15f;

    private static Vibrator vibrator;

    public static void inizializeShake(Context context){

        //Inizializza mSensorManager che gestisce il sensore
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        //Inizializza sensor, usata per incapsulare il sensore. In questo caso l'accelerometro
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        /*Stabilisce i valori di riferimento per determinare le accelerazioni.
        In questo caso uguale per tutte e due le variabili:la gravità terrestre*/
        accelerationPrev = SensorManager.GRAVITY_EARTH;
        acceleration = SensorManager.GRAVITY_EARTH;
    }

    public static void registerShakeListener(SensorEventListener listener){
        //Registrazione del Listener per il sensore
        mSensorManager.registerListener(listener, sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static void unRegisterShakeListener(SensorEventListener listener) {
        //Cancellazione del Listener per il sensore
        mSensorManager.unregisterListener(listener);
    }

    public static void checkShake(SensorEvent sensorEvent, OnShakeListener onShakeListener) {
        /*Inizializzati i valori delle accelerazioni
            misurati per tutte e tre le dimensioni dello spazio*/
        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];

        //Viene salvata l'accelerazione precedente per poi ricalcolare la nuova
        accelerationPrev = acceleration;

        if (itIsNotFirstTime) {
            /*Calcola la somma delle componenti cartesiane x, y e z in valore
            assoluto per determinare l'accelerazione corrente.
            Accelerazione: a = a_x + a_y + a_z */
            acceleration = Math.abs(x + y + z);
            Log.d(TAG, "accelerazione" + acceleration);
            Log.d(TAG, "accelerationPrev" + accelerationPrev);

            //Calcola la differenza tra accelerazione e accelerazione precendente in valore assoluto
            float differenceAcc = Math.abs(acceleration - accelerationPrev);
            Log.d(TAG, "differenza accelerazione" + differenceAcc);

                /*Lo scuotimento viene rilevato nel caso in cui:
                 -la differenza delle accelerazioni è minore di una soglia (in questo caso 5)
                 tale per cui i valori di entrambe le accelerazioni applicate non si discostino di molto;
                 - I valori delle accelerazioni applicate siano maggiori di una certa soglia (in queso caso 20)*/
            if (differenceAcc < THRESHOLD && acceleration > THRESHOLD_ACCELERATION && accelerationPrev > THRESHOLD_ACCELERATION_PREV) {

                //Se si sta eseguendo su Oreo (Api level 26) o successivi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //Deprecato in Api 26
                    vibrator.vibrate(500);
                }
                onShakeListener.doActionAfterShake();
            }
        }
        //Usato per indicare che in precedenza c'è stato almeno un rilevamento
        itIsNotFirstTime = true;
    }

    /**
     * Listener che specifica l'azione da eseguire quando viene rilevato lo scuotimento
     */
    public interface OnShakeListener {
        void doActionAfterShake();
    }

}
