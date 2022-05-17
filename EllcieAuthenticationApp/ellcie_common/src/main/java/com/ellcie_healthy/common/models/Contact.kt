package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.firebase.database.DataSnapshot
import java.util.*


/**
 * Created by Julien on 10/05/2021.
 *
 * This class is used by Profile class.
 * A Contact represent the contact that must be notified when falling asleep is detected.
 */
class Contact {
    /**
     * generate a contact id.
     *
     * @return
     */
    @Expose
    @SerializedName(value = "contact_id")
    var contactId = ""

    /**
     *
     */
    @Expose
    @SerializedName(value = "first_name")
    var firstName = ""

    /**
     *
     */
    @Expose
    @SerializedName(value = "last_name")
    var lastName = ""

    @Expose
    @SerializedName(value = "mail")
    var mMail = ""

    @Expose
    @SerializedName(value = "priority")
    var mPriority = -1

    @Expose
    @SerializedName(value = "state")
    var state = ""

    constructor()

    /**
     *
     * @param firstName
     * @param lastName
     * @param mail
     * @param state
     * @param priority
     */
    constructor(contactId: String?, firstName: String?, lastName: String?, mail: String?,
                state: String?, priority: Int) {
        this.contactId = contactId ?: ""
        this.firstName = firstName ?: ""
        this.lastName = lastName ?: ""
        mMail = mail ?: ""
        this.state = state ?: ""
        mPriority = priority
    }

    /**
     *
     * @param dataSnapshot root of dataSnapshot is the contact_id @see: data model of Profile in Firebase.
     */
    constructor(dataSnapshot: DataSnapshot) {
        contactId = dataSnapshot.key.toString()
        if (dataSnapshot.hasChild(FIRST_NAME_KEY)) {
            firstName = dataSnapshot.child(FIRST_NAME_KEY).value.toString()
        }
        if (dataSnapshot.hasChild(LAST_NAME_KEY)) {
            lastName = dataSnapshot.child(LAST_NAME_KEY).value.toString()
        }
        if (dataSnapshot.hasChild(MAIL_KEY)) {
            mMail = dataSnapshot.child(MAIL_KEY).value.toString()
        }
        if (dataSnapshot.hasChild(PRIORITY_KEY)) {
            mPriority = (dataSnapshot.child(PRIORITY_KEY).value as Long).toInt()
        }
        if (dataSnapshot.hasChild(STATE_KEY)) {
            state = dataSnapshot.child(STATE_KEY).value.toString()
        }
    }

    val displayName: String
        get() = if (firstName == "" || lastName == "") {
            mMail
        } else {
            firstName + " " + lastName.substring(0, 1) + "."
        }
    var mail: String?
        get() = mMail
        set(mail) {
            if (mail != null) {
                mMail = mail
            }
        }
    var priority: Int
        get() = mPriority
        set(priority) {
            if (priority >= 0) {
                mPriority = priority
            }
        }

    override fun toString(): String {
        return "Contact{" +
                "FirstName='" + firstName + '\'' +
                ", LastName='" + lastName + '\'' +
                ", Mail='" + mMail + '\'' +
                ", Priority='" + mPriority + '\'' +
                ", State='" + state + '\'' +
                '}'
    }

    /**
     * Returns a object for firebase database, this object represents the current object.
     * @return
     */
    @Suppress("unused")
    fun convertForFirebase(): Dictionary<String, Any> {
        val dictionaryContact: Dictionary<String, Any> = Hashtable()
        dictionaryContact.put(FIRST_NAME_KEY, firstName)
        dictionaryContact.put(LAST_NAME_KEY, lastName)
        dictionaryContact.put(PRIORITY_KEY, mPriority)
        return dictionaryContact
    }

    companion object {
        private const val FIRST_NAME_KEY = "firstName"
        private const val LAST_NAME_KEY = "lastName"
        private const val MAIL_KEY = "email"
        private const val PRIORITY_KEY = "priority"
        private const val STATE_KEY = "state"
    }
}