package com.example.mytasksapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class NewModel { // temporary class
    private static Model instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private Context context;
    private User currentUser;

    public NewModel(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static Model getInstance(Context context) {
        if (instance == null) instance = new Model(context);
        return instance;
    }

    // -------------------------------------- User Functions --------------------------------------

    // Upload the profile picture to Firebase Storage
    private String getProfilePictureUrl(Bitmap profilePic, String userId) {
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + userId + ".jpg");

        // Upload the picture
        profilePicRef.putBytes(Model.BitmapUtils.bitmapToByteArray(profilePic))
                .addOnSuccessListener(eventSnapshot -> {
                    Log.d("Model", "Profile picture uploaded successfully!");

                    // After uploading, get the download URL
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profilePicUrl = uri.toString();
                        return profilePicUrl; // doesnt work because it is asynchronous
                    }).addOnFailureListener(e -> {
                        Log.e("Model", "Error getting download URL", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Model", "Error uploading profile picture", e);
                });
        return null;
    }

    public User getCurrentUser(){
        if (currentUser != null) {
            return currentUser;
        }
        return null;
    }

    public void createUser(String uName, String email, String password, boolean privacy, Bitmap profilePic) throws Exception {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(uName).build();
                        String profilePicUrl = getProfilePictureUrl(profilePic, firebaseUser.getUid());

                        currentUser = new User(uName, email, password, profilePicUrl, firebaseUser.getUid(), null, null, privacy);
                        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
                        userRef.set(currentUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Model", "User details saved to Firestore.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Model", "Error saving user to Firestore", e);
                                });
                        //                        raiseUserUpdate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"create currentUser failed"+ e.getMessage(),Toast.LENGTH_SHORT);
                    }
                });
    }

    public User login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
                        getUserFromFirebase(firebaseUser.getUid());
//                            raiseUserUpdate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "login failed", Toast.LENGTH_SHORT).show();
                    }
                });
        return null;
    }
    private void getUserFromFirebase(String userId) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUser = documentSnapshot.toObject(User.class);
                Log.d("Model", "User data retrieved: " + currentUser.getuName());
            } else {
                Log.e("Model", "No such user in Firestore");
            }
        }).addOnFailureListener(e -> {
            Log.e("Model", "Error retrieving user data from Firestore", e);
        });
    }

    public void logout() {
        firebaseAuth.signOut();
        Log.d("Model", "User logged out");
        currentUser = null;
//        raiseUserUpdate();
    }

    public void updateUser(String uName, String email, String password, boolean privacy, Bitmap profilePic) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();  // Get the current FirebaseUser

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

        if (password != null && !password.isEmpty()) {
            firebaseUser.updatePassword(password)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Model", "User password updated in Firebase Authentication.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Model", "Failed to update password in Firebase Authentication.", e);
                        }
                    });
        }

        // Update currentUser (not in the firebase)
        currentUser.setPrivacy(privacy);
        currentUser.setuName(uName);
        currentUser.setEmail(email);
        currentUser.setPassword(password);
        currentUser.setPrivacy(privacy);

        // Upload profile picture and update the URL if a new profile picture is provided
        if (profilePic != null) {
            String profilePicUrl = getProfilePictureUrl(profilePic, firebaseUser.getUid());
            currentUser.setProfilePicUrl(profilePicUrl);
        }

        // Update the user document in Firestore
        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        userRef.set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Model", "User details updated in Firestore.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Model", "Error updating user in Firestore", e);
                });
    }


    // -------------------------------------- Event Functions --------------------------------------


    //-------------------------------------------------------------------------------------------


        @Override
        public ArrayList<Note>  getNotes() {
            ArrayList<Note> notes = new ArrayList<>();
            dbnotes.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Note note = document.toObject(Note.class);
                                if (note != null) {
                                    notes.add(note);
                                }
                            }
                            raiseNoteUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to retrieve notes", Toast.LENGTH_SHORT).show();
                        }
                    });
            return notes;
        }


        @Override
        public void addNote(String title, String content, boolean done) {
            Note note = new Note(title, content, "0", done);
            dbnotes.add(note)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(context, "Note has been added",Toast.LENGTH_SHORT);
                            raiseNoteUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Note adding failed",Toast.LENGTH_SHORT);
                        }
                    });
        }

        @Override
        public void updateNote(String noteId, String title, String content, boolean done) {
            DocumentReference noteRef = dbnotes.document(noteId);
            noteRef.update("title", title, "content", content, "done", done)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show();
                            raiseNoteUpdate(); // Notify listeners
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to update note", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        @Override
        public Note getNoteById(String noteId) {
            final Note[] note = {null};
            DocumentReference noteRef = dbnotes.document(noteId);
            noteRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        note[0] = documentSnapshot.toObject(Note.class);
                        if (note[0] != null) {
                            Toast.makeText(context, "Note retrieved successfully", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No such note exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed to retrieve note", Toast.LENGTH_SHORT).show();
                }
            });
            return note[0];
        }


        @Override
        public void deleteNote(String noteId) {
            DocumentReference noteRef = dbnotes.document(noteId);
            noteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                    raiseNoteUpdate();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
