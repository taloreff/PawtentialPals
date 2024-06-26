import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.example.pawtentialpals.services.PostRepository
import com.example.pawtentialpals.services.UploadService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPostsViewModel(private val context: Context) : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val postRepository: PostRepository = PostRepository(context)

    private val _posts = MutableLiveData<List<PostModel>>()
    val posts: LiveData<List<PostModel>> get() = _posts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadUserPosts() {
        _loading.value = true
        val userId = firebaseAuth.currentUser?.uid ?: run {
            _error.value = "User not authenticated"
            _loading.value = false
            return
        }

        // Load posts from SQLite
        val localPosts = postRepository.getAllPosts()
        if (localPosts.isNotEmpty()) {
            val sortedPosts = localPosts.sortedByDescending { it.timestamp }
            _posts.value = sortedPosts
        }

        // Sync with Firestore
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.mapNotNull { it.toObject(PostModel::class.java) }
                val sortedPosts = posts.sortedByDescending { it.timestamp }
                _posts.value = sortedPosts
                posts.forEach { postRepository.insertPost(it) } // Save to SQLite
                _loading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message
                _loading.value = false
            }
    }


    fun updatePost(post: PostModel, newDescription: String, imageUri: Uri?, context: Context) {
        val db = firestore
        _loading.value = true

        if (imageUri != null) {
            viewModelScope.launch {
                val imageUrl = uploadImage(imageUri, context)
                if (imageUrl != null) {
                    val updatedPost = post.copy(description = newDescription, postImage = imageUrl)
                    db.collection("posts").document(post.id)
                        .set(updatedPost)
                        .addOnSuccessListener {
                            updatePostInUserCollection(post, updatedPost)
                            postRepository.insertPost(updatedPost) // Update SQLite
                        }
                        .addOnFailureListener { e ->
                            _error.value = e.message
                            _loading.value = false
                        }
                } else {
                    _error.value = "Failed to upload image"
                    _loading.value = false
                }
            }
        } else {
            db.collection("posts").document(post.id)
                .update("description", newDescription)
                .addOnSuccessListener {
                    loadUserPosts()
                }
                .addOnFailureListener { e ->
                    _error.value = e.message
                    _loading.value = false
                }
        }
    }

    fun updatePostWithImage(post: PostModel, imageUri: Uri, context: Context) {
        _loading.value = true
        viewModelScope.launch {
            val imageUrl = uploadImage(imageUri, context)
            if (imageUrl != null) {
                val updatedPost = post.copy(postImage = imageUrl)
                firestore.collection("posts").document(post.id)
                    .set(updatedPost)
                    .addOnSuccessListener {
                        updatePostInUserCollection(post, updatedPost)
                        postRepository.insertPost(updatedPost) // Update SQLite
                    }
                    .addOnFailureListener { e ->
                        _error.value = e.message
                        _loading.value = false
                    }
            } else {
                _error.value = "Failed to upload image"
                _loading.value = false
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            val filePath = getPathFromUri(imageUri, context)
            if (filePath != null) {
                UploadService.uploadImg(filePath)
            } else {
                null
            }
        }
    }

    private fun getPathFromUri(uri: Uri, context: Context): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun updatePostInUserCollection(oldPost: PostModel, updatedPost: PostModel) {
        val userRef = firestore.collection("users").document(updatedPost.userId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(UserModel::class.java)
            user?.let {
                val updatedPosts = it.posts.map { if (it.id == oldPost.id) updatedPost else it }
                userRef.update("posts", updatedPosts)
                    .addOnSuccessListener {
                        loadUserPosts()
                    }
                    .addOnFailureListener { e ->
                        _error.value = e.message
                        _loading.value = false
                    }
            }
        }.addOnFailureListener { e ->
            _error.value = e.message
            _loading.value = false
        }
    }

    fun deletePost(post: PostModel) {
        val db = firestore
        val postId = post.id
        val userId = post.userId

        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(UserModel::class.java)
            user?.let {
                val updatedPosts = it.posts.filter { it.id != postId }
                userRef.update("posts", updatedPosts)
                    .addOnSuccessListener {
                        db.collection("posts").document(postId).delete()
                            .addOnSuccessListener {
                                loadUserPosts()
                                postRepository.deletePost(post) // Delete from SQLite
                            }
                            .addOnFailureListener { e ->
                                _error.value = e.message
                                _loading.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        _error.value = e.message
                        _loading.value = false
                    }
            }
        }.addOnFailureListener { e ->
            _error.value = e.message
            _loading.value = false
        }
    }
}
