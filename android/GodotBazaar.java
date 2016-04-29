

import android.app.Activity;
import android.content.Intent;
import android.util.Log;


import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.GodotLib;
import org.json.JSONException;

import java.util.Iterator;

import javax.microedition.khronos.opengles.GL10;



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
       registerClass("Bazaar", new String[]{"init", "purchase", "get_inventory", "consume"});
    }


    public void init(int callid)
    {

        String base64EncodedPublicKey = GodotLib.getGlobal("bazaar/key");

        this.callbackId = callid;
        mHelper = new IabHelper(activity, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    call_error("{\"status\":\"300\",\"message\":\"Problem setting up in-app billing: " + result.getMessage() + "\"");
                    return;
                }

                if (mHelper == null) return;

                ready();
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
                if (mHelper != null) mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(activity, sku, 1001,
                        mPurchaseFinishedListener, temppay);
            }
        });

    }
    public void get_inventory() {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (! mHelper.mAsyncInProgress)
                    mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    public void consume(final String itemtype, final String json, final String signature)
    {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    Purchase purchase = new Purchase(itemtype, json, signature);
                    if(!mHelper.mAsyncInProgress)
                        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (JSONException e) {
                    e.printStackTrace();
                    call_error(e.getMessage());
                }

            }
        });



    }

    public void call_success(String mode,String message)
    {
        GodotLib.calldeferred(callbackId, "on_success", new Object[]{mode, message});
    }

    public void call_error(String message)
    {
        GodotLib.calldeferred(callbackId, "on_error", new Object[]{message});
    }

    public void ready()
    {

        GodotLib.calldeferred(callbackId, "on_ready", new Object[]{});
    }
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
        new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
                if (result.isSuccess()) {
                    call_success("consume",purchase.toString());
                }   
                else {
                    call_error(result.getMessage());
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
            String json = "{\"purchase_data\":[";
            for(Iterator<Purchase> i = inventory.getAllPurchases().iterator(); i.hasNext(); ) {
                Purchase item = i.next();
                json+="{\"itemtype\":\""+item.getItemType()+"\", \"signature\":\""+item.getSignature()+"\",\"json\":";

                json +=item.toString()+"},";
            }
            
            json+="],\"sku_data\":[";
            for(Iterator<SkuDetails> i = inventory.getAllSku().iterator(); i.hasNext(); ) {
                SkuDetails item = i.next();
                json +=item.toString()+",";
            }
            json+="]}";


            call_success("inventory",json);

        }
    };


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

            call_success("purchase","{\"json\":"+purchase.toString()+",\"itemtype\":\""+purchase.getItemType()+"\",\"signature\":\""+purchase.getSignature()+"\"}");

        }
    };


    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        return this.payload.equals(payload);
    }
     protected void onMainActivityResult(int requestCode, int resultCode, Intent data) 
     {
         if (mHelper == null) return;


         if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {

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