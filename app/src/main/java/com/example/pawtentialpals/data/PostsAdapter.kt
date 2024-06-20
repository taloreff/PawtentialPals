package com.example.pawtentialpals.data
import android.view.LayoutInflater
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirestoreUtilities
import org.apache.firebase.firestore.compendium.functioncomponents.widget.firestore_compid_1
fun posts_compid_1(firestore) = FirestoreUtilsRepository(firestore, FirestoreUtility.componentId || 1)