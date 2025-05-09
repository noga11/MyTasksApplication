package com.example.SynCalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class Model {
    private static final String TAG = "Model";
    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private static Model instance;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private Context context;
    private User currentUser;
    private ArrayList<Event> events = new ArrayList<>();

    public Model(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        eventRef = firestore.collection(EVENTS_COLLECTION);
        userRef = firestore.collection(USERS_COLLECTION);
    }

    public static Model getInstance(Context context) {
        if (instance == null) instance = new Model(context);
        return instance;
    }

    // -------------------------------------- User Functions --------------------------------------

    public User getCurrentUser(){
        if (currentUser != null) {
            return currentUser;
        }
        return null;
    }

    public void createUser(String displayName, String email, String password, boolean privacy, Bitmap profilePic, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        currentUser = new User(displayName, email, profilePic, firebaseUser.getUid(), null, null, null, privacy);
                        DocumentReference userDoc = firestore.collection(USERS_COLLECTION).document(firebaseUser.getUid());
                        userDoc.set(currentUser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Model", "User details saved to Firestore.");
                                        onSuccess.onSuccess(currentUser);
                                    }
                                })
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void login(String email, String password, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        getUserFromFirebase(firebaseUser.getUid(), onSuccess, onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    private void getUserFromFirebase(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                            Log.d("Model", "User data retrieved: " + currentUser.getuName());
                            onSuccess.onSuccess(currentUser);
                        } else {
                            Log.e("Model", "No such user in Firestore");
                            onFailure.onFailure(new Exception("User not found in database"));
                        }
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void getUserById(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        DocumentReference userDocRef = firestore.collection(USERS_COLLECTION).document(userId);
        userDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            onSuccess.onSuccess(user);
                        } else {
                            onSuccess.onSuccess(null); // User not found
                        }
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void logout() {
        mAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
        //      raiseUserUpdate();
    }

    public void updateUser(String uName, String email, boolean privacy, Bitmap profilePic) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();  // Get the current FirebaseUser

        // Update display name in Firebase Authentication
        if (uName != null && !uName.isEmpty()) {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(uName)
                    .build();

            firebaseUser.updateProfile(profileUpdate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "User display name updated in Firebase Authentication.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Failed to update display name in Firebase Authentication.", e);
                        }
                    });
        }

        if (email != null && !email.equals(firebaseUser.getEmail())) {
            firebaseUser.updateEmail(email) // need to change
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "User email updated in Firebase Authentication.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Failed to update email in Firebase Authentication.", e);
                        }
                    });
        }

        // Update currentUser (not in the firebase)
        currentUser.setPrivacy(privacy);
        currentUser.setuName(uName);
        currentUser.setEmail(email);
        currentUser.setPrivacy(privacy);
        currentUser.setProfilePic(profilePic);

        // Update the user document in Firestore
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.getUid());
        userRef.set(currentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Model", "User details updated in Firestore.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Model", "Error updating user in Firestore", e);
                    }
                });
    }


    // -------------------------------------- Event Functions --------------------------------------

    public void createEvent(Event event) {
        eventRef.add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        event.setId(documentReference.getId());
                        events.add(event);
                        raiseEventDataChange();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        Log.e(TAG, "createEvent: failed ", ex);
                    }
                });
    }

    public void deleteEvent(Event event) {
        // delete event for current user
        String eventId = event.getId();
        event.getUsersId().remove(currentUser.getId());
        updateEvent(event);

        //delete event for everyone
        if(event.getUsersId().isEmpty()) {
            DocumentReference eventRef = firestore.collection(EVENTS_COLLECTION).document(eventId);
            eventRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "Event deleted successfully.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Error deleting event", e);
                        }
                    });
        }
    }

    public void updateEvent(Event event) {
        DocumentReference eventRef = firestore.collection(EVENTS_COLLECTION).document(event.getId());
        eventRef.set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Model", "Event updated successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Model", "Error updating event", e);
                    }
                });
    }

    public ArrayList<Event> getEventsByUserId(String userId) {
        firestore.collection(EVENTS_COLLECTION)
                .whereArrayContains("users", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                            Event event = documentChange.getDocument().toObject(Event.class);
                            event.setId(documentChange.getDocument().getId());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Model", "Error updating user in Firestore", e);
                    }
                });

        return events;
    }

    public void raiseEventDataChange() {
        firestore.collection(EVENTS_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Model", "Error listening for event changes", e);
                            return;
                        }
                        if (querySnapshot != null) {
                            Log.d("Model", "Events updated.");
                        }
                    }
                });
    }

    public ArrayList<String> getGroups() {
        ArrayList<String> topics = new ArrayList<>();
        for (Event event : events) {
            if (event.getGroup() != null && !topics.contains(event.getGroup())) {
                topics.add(event.getGroup());
            }
        }
        return topics;
    }


}
