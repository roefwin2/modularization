# ellcie-mobile-app-ble-lib-android

Repo used to host the ble library for App android Driver/Fall

## How to set the Library on a android project:

1- Go to the folder of the app project.

2- Clone ble lib project 
~~~~
git clone git@bitbucket.org:ellcie-healthy/ellcie-mobile-app-ble-lib-android.git ble_library
~~~~

3- You can use the ble library on your android project.

## How to use the Ble Library

1- To connect the app to the glasses use the method __startService__ of the class __Glasses.java__

~~~~
Glasses.getInstance().setMacAdress("FF:FF:FF:FF:FF:FF");
Glasses.getInstance().addSubscriber(HomeActivity.this); // Activity must implements IEllcieEventSubscriber to be notified when the glasses return a value.

Glasses.getInstance().setFirebaseDb(FirebaseDataHelper.getInstance()); // set firebase db helper
Glasses.getInstance().setAuthFirebase(FirebaseAuthHelper.getInstance()); // set firebase auth helper

Glasses.getInstance().startService(MainActivity.this, ServiceMode.FALL); // specify FALL or DRIVER
~~~~