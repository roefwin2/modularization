package com.ellcie.ellciefirebaselibrary.firebasemanager

import com.ellcie.ellciefirebaselibrary.network.models.NetworkUserRole.Companion.toNetworkUserRole
import com.ellcie.ellciefirebaselibrary.utils.Field
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.NODE_PROFILES
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.NODE_USER_GLASSES_BATTERY
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.NODE_USER_GLASSES_BATTERY_LAST_UPDATE
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.NODE_USER_GLASSES_BATTERY_LEVEL
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.NODE_USER_GLASSES_BLE_STATUS
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.NODE_USER_GLASSES_BLE_UPDATE_TS
import com.ellcie.toolkitlibrary.datarequest.EllcieDataRequest
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase manager for authentication and database to inject in your DemoApp
 */
class EllcieFirebaseDataManager @Inject constructor() : EllcieDataRequest {

    @Inject
    internal lateinit var auth: FirebaseAuth

    @Inject
    internal lateinit var database: DatabaseReference


    override suspend fun signIn(customToken: String): Flow<Resource<UserAuth>> = flow {
        emit(Resource.Loading)
        try {
            auth.signInWithCustomToken(customToken).await()
            emit(
                Resource.Success(
                    UserAuth.Authenticated(
                        auth.uid ?: "firebase user id null",
                        auth.currentUser?.email ?: "firebase user email null",
                        this@EllcieFirebaseDataManager
                    )
                )
            )
        } catch (e: FirebaseAuthException) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun getFirebaseAuthState() = callbackFlow {
        val listener = FirebaseAuth.IdTokenListener {
            trySend(it.tenantId)
        }
        awaitClose {
            auth.removeIdTokenListener(listener)
        }
    }

    suspend fun pushBleConnectionStatus(serialNumber: String, isConnected: Boolean) = flow {
        emit(Resource.Loading)
        try {
            val map: MutableMap<String, Any> = HashMap()
            map[NODE_USER_GLASSES_BLE_UPDATE_TS] =
                System.currentTimeMillis()
            map[NODE_USER_GLASSES_BLE_STATUS] =
                if (isConnected) "CONNECTED" else "DISCONNECTED"

            database.child(Field.DEVICES_KEY.name).child(serialNumber)
                .child(FirebaseConstants.NODE_USER_GLASSES_BLE).updateChildren(map).await()
            emit(Resource.Success("Successfully update connection Status"))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    suspend fun getDefaultUserConfig() = callbackFlow {
        auth.uid?.let { database.child("${FirebaseConstants.mDbPrefix}/$NODE_PROFILES/$it/role") }
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(Resource.Success((snapshot.value as String)))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.Error(error.toException().toString()))
                }
            })

        awaitClose { }
    }

    suspend fun updateBatteryLevel(serialNumber: String, batteryLevel: Int) = flow {
        emit(Resource.Loading)
        try {
            val map: MutableMap<String, Any> = HashMap()
            map[NODE_USER_GLASSES_BATTERY_LAST_UPDATE] =
                System.currentTimeMillis()
            map[NODE_USER_GLASSES_BATTERY_LEVEL] =
                batteryLevel
            database.child(Field.DEVICES_KEY.toString()).child(serialNumber).child(
                NODE_USER_GLASSES_BATTERY
            ).updateChildren(map).await()
            emit(Resource.Success("Success to update battery"))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

}