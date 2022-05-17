@file:Suppress("unused")

package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase

import com.google.firebase.auth.FirebaseUser

interface IFirebaseAuth {

    val currentUser: FirebaseUser?



    fun serviceDestroyed()
    fun serviceCreated()
}
