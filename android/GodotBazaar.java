package com.android.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.app.Activity;
import android.os.Bundle;

import java.util.Iterator;

import javax.microedition.khronos.opengles.GL10;

import com.android.godot.IabHelper;
import com.android.godot.IabResult;
import com.android.godot.Inventory;
import com.android.godot.Purchase;



public class GodotBazaar extends Godot.SingletonBase {


    IabHelper mHelper;
    int callbackId;
    Activity activity;
    String payload;    
    
    
    static public Godot.SingletonBase initialize(Activity p_activity) {

                return new GodotBazaar(p_activity);
    } 

    public GodotBazaar(Activity p_activity) {
        this.activity = p_activity;
       registerClass("Bazaar", new String[]{"init","purchase","getInventory"});
    }

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
    new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
    if (result.isSuccess()) {
    // provision the in-app purchase to the user
    // (for example, credit 50 gold coins to player's character)
    }
    else {
    // handle error
    }
    }
    };
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                call_error("{\"status:\"300\",\"message\":\"Failed to query inventory: " + result.getMessage()+"\"");
                return;
            }
            String json = "{\"purchaseData\":[";
            
            for(Iterator<Purchase> i = inventory.getAllPurchases().iterator(); i.hasNext(); ) {
                Purchase item = i.next();
                json +=item.toString()+",";
            }
            
            json+="],\"skuData\":[";
            for(Iterator<SkuDetails> i = inventory.getAllSku().iterator(); i.hasNext(); ) {
                SkuDetails item = i.next();
                json +=item.toString()+",";
            }
            json+="]}";
            call_inventory_callback(json);
        }
    };

    public void getInventory()
    {
        activity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
        
    }
    public void purchase(final String sku)
    {
        
        this.payload = Math.random()+"NEGAHNAKON"+ Math.random();
        final String temppay = this.payload;
        activity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mHelper !=null)
                    mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(activity, sku, 1001,
                        mPurchaseFinishedListener, temppay);
            }
        });
        
    }

    public void call_inventory_callback(String message)
    {
        GodotLib.calldeferred(callbackId, "inventory_callback", new Object[]{message});     
    }
    public void call_purchase_success(String message)
    {
        GodotLib.calldeferred(callbackId, "purchase_success", new Object[]{message});   
    }
    public void call_error(String message)
    {
        GodotLib.calldeferred(callbackId, "error", new Object[]{message});
    }

    public void ready()
    {
        GodotLib.calldeferred(callbackId, "ready", new Object[]{});
    }

    
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

       return this.payload.equals(payload);
    }
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                call_error("{\"status\":\"300\",\"message\":\"Error purchasing: " + result.getMessage()+"\"");
                
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                call_error("{\"status\":\"300\",\"message\":\"Error purchasing. Authenticity verification failed.\"");
                return;
            }

            call_purchase_success(purchase.toString());

            

        }
    };


    public void init(String base64EncodedPublicKey,int callid)
    {
        this.callbackId = callid;
        mHelper = new IabHelper(activity, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                     call_error("{\"status\":\"300\",\"message\":\"Problem setting up in-app billing: " + result.getMessage()+"\"");
                    return;
                }

                if (mHelper == null) return;

                ready();
            }
        });

    }

     protected void onMainActivityResult(int requestCode, int resultCode, Intent data) 
     {
         if (mHelper == null) return;

         // Pass on the activity result to the helper for handling
         if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
             // not handled, so handle it ourselves (here's where you'd
             // perform any handling of activity results not related to in-app
             // billing...
          
         }
         else {
          
         }
     }

     protected void onMainPause() {}
     protected void onMainResume() {}
     protected void onMainDestroy() {
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

     protected void onGLDrawFrame(GL10 gl) {}
     protected void onGLSurfaceChanged(GL10 gl, int width, int height) {} // singletons will always miss first onGLSurfaceChanged call

}