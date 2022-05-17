package com.ellcie.ellcieauthenticationapp.injection.components

import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Scope


//Define a scope for my AuthUser(unauthenticated, authenticated)
@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class AuthUserScope


//declare the custom component which use the scope define before
@AuthUserScope
@DefineComponent(parent = SingletonComponent::class)
interface AuthUserComponent{
    @DefineComponent.Builder
    interface Builder {
        fun build(): AuthUserComponent
    }
}