package com.ellcie.ellcieauthenticationapp.network.repositories

import com.ellcie.ellciefirebaselibrary.firebasemanager.EllcieFirebaseDataManager
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface  FirebaseRepository

class FirebaseRepositoryImpl @Inject constructor(private val ellcieFirebaseDataManager: EllcieFirebaseDataManager) : FirebaseRepository{


    suspend fun firebaseSignIn(customToken: String) : Flow<Resource<UserAuth>> {
      return try {
            ellcieFirebaseDataManager.signIn(customToken).map{
                it
            }
        }catch (e : Exception){
            flowOf(Resource.Error(e.toString()))
        }
    }

    suspend fun getDefaultProfile() = ellcieFirebaseDataManager.getDefaultUserConfig()
}