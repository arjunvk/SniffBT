package millennia.sniffbt;

import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CommonFunctions {

    /**
     * Method to put application in sleep for specified number of seconds
     * @param intSleepTimeInSeconds - The {@link int} number of seconds
     */
    public void sleepForNSeconds(int intSleepTimeInSeconds){
        try {
            Thread.sleep(intSleepTimeInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to store a complex object in the Shared Preferences class
     * @param  mPref - the {@link SharedPreferences} object
     * @param strReferenceName - The {@link String} reference name which will be used to retrieve the object
     * @param obj - The {@link Object}
     */
    public void setSharedPreferences(SharedPreferences mPref, String strReferenceName, Object obj) {
        SharedPreferences.Editor prefEditor = mPref.edit();
        Gson gson = new Gson();
        String json;

        try {
            int intNumber = (int) obj;
            prefEditor.putInt(strReferenceName, intNumber);
        }
        catch (ClassCastException cce) {
            json = gson.toJson(obj);
            prefEditor.putString(strReferenceName, json);
        }

        prefEditor.apply();
    }

    /**
     * Method to retireve a saved Shared Preferences object
     * @param  mPref - the {@link SharedPreferences} object
     * @param strReferenceName - The {@link String} reference name
     * @return - The {@link Object}
     */
    public Object getSharedPreferences(SharedPreferences mPref, String strReferenceName, Class<?> objClass) {
        Gson gson = new Gson();
        if(objClass.getCanonicalName().contains("Integer") || objClass.getCanonicalName().contains("int")) {
            return mPref.getInt(strReferenceName, 0);
        }
        else {
            String json = mPref.getString(strReferenceName, "");
            return gson.fromJson(json, objClass);
        }
    }

    /**
     * Method to display a SnackBar message
     * @param strMsg - The {@link String} message to be displayed
     */
    public void showSnackBar(View view, String strMsg, int intTime) {
        Snackbar snackbar = Snackbar.make(view, strMsg, intTime);
        snackbar.show();
    }

    /**
     * Method to serialize an object as a byte[]
     * @param obj - The Object to serialize
     * @return - The {@link byte[]} object
     */
    byte[] serialize(Object obj){
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * Method to deserialize an object
     * @param data - The {@link byte[]} object
     * @return - The Object
     */
    Object deserialize(byte[] data){
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



}
