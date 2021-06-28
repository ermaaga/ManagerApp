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
import java.util.Calendar;

public class ShakeUtil {
    private static final String TAG = "ShakeUtil";

    private static SensorManager mSensorManager;
    private static Sensor sensor;
    private static boolean isAccelerometerAvailable = false;
    private static float x, y, z;
    private static boolean itIsNotFirstTime = false;
    private static boolean wasShaken = false;
    private static float accelerationPrev= 0.0f;
    private static float acceleration = 0.0f;
    private static Calendar timeOfShaking=null;

    //Soglia minima della variazione di accelerazione affinchè venga considerato lo scuotimento
    private static final float THRESHOLD = 18f;

    private static Vibrator vibrator;

    public static void inizializeShake(Context context){

        //Inizializza mSensorManager che gestisce il sensore
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        //Inizializza variabile sensor, usata per incapsulare il sensore (accelerometro), nel caso in cui esso sia disponibile.
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerAvailable=true;
        }else{
            Log.d(TAG, "Accelerometer sensor is not available");
            isAccelerometerAvailable=false;
        }
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        /*Stabilisce i valori di riferimento per determinare le accelerazioni.
        In questo caso uguale per tutte e due le variabili:la gravità terrestre.*/
        accelerationPrev = SensorManager.GRAVITY_EARTH;
        acceleration = SensorManager.GRAVITY_EARTH;
    }

    public static void registerShakeListener(SensorEventListener listener){
        //Registrazione del Listener per il sensore, nel caso in cui esso sia disponibile.
        if(isAccelerometerAvailable){
            mSensorManager.registerListener(listener, sensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public static void unRegisterShakeListener(SensorEventListener listener) {
        //Cancellazione del Listener per il sensore, nel caso in cui esso sia disponibile.
        if(isAccelerometerAvailable){
            mSensorManager.unregisterListener(listener);
        }
    }

    public static void checkShake(SensorEvent sensorEvent, OnShakeListener onShakeListener) {
        //Inizializzati i valori delle accelerazioni misurati per tutte e tre le dimensioni dello spazio
        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];

        //Viene salvata l'accelerazione precedente per poi ricalcolare la nuova
        accelerationPrev = acceleration;

        if (itIsNotFirstTime) {
            /*Calcola la radice quadrata della somma dei quadrati delle componenti
            cartesiane x, y e z, per determinare l'accelerazione corrente.
            Accelerazione: a = √((a_x)^2+(a_y)^2 +(a_z)^2) */
            acceleration = (float) Math.sqrt(x*x + y*y + z*z);

            //Calcola la differenza tra accelerazione e accelerazione precendente in valore assoluto
            float differenceAcc = Math.abs(acceleration - accelerationPrev);

            /*Lo scuotimento viene rilevato nel caso in cui:
              -la differenza delle accelerazioni è maggiore di una soglia (in questo caso 18)
              -Siano passati almeno 6 secondi dal precedente scuotimento, se esso è stato già rilevato*/
            if (differenceAcc > THRESHOLD) {
                Calendar now = Calendar.getInstance();
                if(wasShaken==true && now.before(timeOfShaking)){
                    return;
                }

                /*Memorizza l'istante in cui si potrà iniziare a rilevare il prossimo scuotimento
                 (dopo sei secondi dall'ultimo). In questo modo si evita il rilevamento di più scuotimenti
                 nel lasso di tempo che precede l'esecuzione dell'azione.*/
                timeOfShaking = Calendar.getInstance();
                timeOfShaking.setTimeInMillis(now.getTimeInMillis());
                timeOfShaking.add(Calendar.SECOND, 6);

                //Usato per indicare che in precedenza c'è stato almeno uno scuotimento
                wasShaken = true;

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

    /*Metodo usato per resettare l'accelerazione, in modo che, la differenza di accelerazione dopo
    *che l'azione è stata eseguita, non dipenda dall' accelerazione che ha causato lo scuotimento.*/
    public static void resetAcceleration(){
        acceleration = SensorManager.GRAVITY_EARTH;
    }

    /**
     * Listener che specifica l'azione da eseguire quando viene rilevato lo scuotimento
     */
    public interface OnShakeListener {
        void doActionAfterShake();
    }

}